(ns monotony.analysis.files
  (:require [clojure.java.io :as io]
            [clojure.string :as strings]
            [missing.core :as miss])
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
       (keep #(.getParentFile %))
       (distinct)))

(defn get-flattened-dirs [root]
  (->> (get-tf-files-deep root)
       (group-by #(.getParentFile %))
       (miss/map-groups slurp)
       (miss/reduce-groups (fn [a x] (strings/join \newline [a x])))))

(defn get-flattened-dir [root]
  (->> (get-tf-files-shallow root)
       (map slurp)
       (strings/join \newline)))

(defn find-terraform-modules [root]
  (->> (get-terraform-directories root)
       (remove is-plan-dir?)))

(defn find-terraform-plans [root]
  (->> (get-terraform-directories root)
       (filter is-plan-dir?)))