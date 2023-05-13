(ns monotony.files
  (:require [clojure.java.io :as io]
            [clojure.string :as strings]
            [missing.core :as miss]
            [monotony.parse :as parse])
  (:import (java.io File)
           (java.nio.file Paths)))

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

(defn find-terraform-modules [root]
  (->> (get-terraform-directories root)
       (remove is-plan-dir?)))

(defn find-terraform-plans [root]
  (->> (get-terraform-directories root)
       (filter is-plan-dir?)))

(defn clean-string [s]
  (-> s (miss/lstrip "\"") (miss/rstrip "\"")))

(defn extract-dependencies [dir]
  (->> (get-tf-files-shallow dir)
       (map slurp)
       (mapcat parse/terraform-parser)
       (parse/extract-module-references)
       (miss/map-keys clean-string)
       (miss/map-vals clean-string)))

(defn find-unparseable-files [root]
  (sort-by :file
           (for [file  (get-tf-files-deep root)
                 :let [content (slurp file)
                       lines   (vec (strings/split-lines content))
                       e       (try (parse/terraform-parser content) nil (catch Exception e e))]
                 :when (some? e)
                 error (deref e)]
             (merge
               {:file file
                :text (when (<= (:line error) (count lines))
                        (nth lines (dec (:line error))))}
               error))))

(defn analyze [dir]
  (letfn [(is-git-path? [path]
            (strings/starts-with? path "git@"))
          (inner-analyze [resolved]
            {:path         resolved
             :dependencies (into {}
                                 (for [[module path] (extract-dependencies resolved)]
                                   [module (if (is-git-path? path)
                                             (inner-analyze
                                               (str (.resolve (Paths/get ^String dir (into-array String []))
                                                              ^String (cond-> (str ".terraform/" module)
                                                                        (strings/includes? path "//")
                                                                        (str "/" (second (re-find path "//([^?]+)")))))))
                                             (inner-analyze (str (.resolve (Paths/get ^String resolved (into-array String [])) ^String path))))]))})]
    (inner-analyze dir)))


(defn numbers-to-ranges [xs]
  (->> (miss/contiguous-by identity inc xs)
       (map (juxt first last))
       (sort)))

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
