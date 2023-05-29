(ns monotony.files
  (:require [clojure.java.io :as io]
            [clojure.string :as strings]
            [missing.core :as miss]
            [monotony.parse :as parse]
            [meander.epsilon :as m])
  (:refer-clojure :exclude [find])
  (:import (java.io File)))

(defn is-tf-file? [^File file]
  (strings/ends-with? (.getName file) ".tf"))

(defn is-hidden-tf-file? [^File file]
  (or (strings/starts-with? (.getAbsolutePath file) ".terraform/")
      (strings/includes? (.getAbsolutePath file) "/.terraform/")))

(defn get-tf-files-shallow [dir]
  (->> (.listFiles (io/file dir))
       (remove is-hidden-tf-file?)
       (filter is-tf-file?)))

(defn get-tf-files-deep [dir]
  (->> (file-seq (io/file dir))
       (remove is-hidden-tf-file?)
       (filter is-tf-file?)))

(defn is-tf-lock-file? [^File f]
  (= ".terraform.lock.hcl" (.getName f)))

(defn is-plan-dir? [^File dir]
  (miss/seek is-tf-lock-file? (.listFiles dir)))

(defn get-terraform-directories [root]
  (->> (get-tf-files-deep root)
       (remove is-hidden-tf-file?)
       (map #(.getParentFile %))
       (distinct)))

(defn get-flattened-dirs [root]
  (->> (get-tf-files-deep root)
       (group-by #(.getParentFile %))
       (miss/map-groups slurp)
       (miss/reduce-groups (fn [a x] (strings/join \newline [a x])))
       (miss/map-vals parse/terraform-parser)))

(defn find-terraform-modules [root]
  (->> (get-terraform-directories root)
       (remove is-plan-dir?)))

(defn find-terraform-plans [root]
  (->> (get-terraform-directories root)
       (filter is-plan-dir?)))

(defn find-unparseable-files [root]
  (for [file (get-tf-files-deep root)
        :let [content (slurp file)
              e       (try (parse/terraform-parser content) nil (catch Exception e e))]
        :when (some? e)]
    {:file   (.getAbsolutePath file)
     :errors (->> (deref e)
                  (miss/distinct-by :message)
                  (miss/distinct-by :line)
                  (remove nil?)
                  (mapv #(select-keys % [:line :message])))}))


(defn numbers-to-ranges [xs]
  (->> (miss/contiguous-by identity inc xs)
       (map (juxt first last))
       (sort)))

(defmacro find [directory pattern expression]
  `(for [[dir# content#] (get-flattened-dirs ~directory)]
     (->> (miss/walk-seq content#)
          (mapcat (fn [form#] (m/search form# ~pattern ~expression))))))

(defn view-unparseable-sections [dir]
  (->> (find-unparseable-files dir)
       (partition-by :file)
       (sort-by (comp :file first))
       (run! (fn [xs]
               (let [file    (:file (first xs))
                     content (vec (strings/split-lines (slurp file)))
                     ranges  (numbers-to-ranges (map :line xs))]
                 (println (.getAbsolutePath file))
                 (println)
                 (doseq [[start stop] ranges]
                   (println (str "  " start " - " stop))
                   (println (strings/join \newline (map strings/trim (subvec content (dec start) stop))))
                   (println))
                 (println)
                 (println))))))