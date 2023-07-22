(ns monotony.analysis.parse-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [missing.core :as miss]
    [monotony.analysis.files :as files]
    [monotony.analysis.parse :as parse]))

(defn ast? [x]
  (and (seq? x) (= :file_ (first x))))

(defn ir? [x]
  (->> x
       (miss/walk-seq)
       (filter map?)
       (every? #(contains? % :kind))))

(defn get-test-files []
  (files/get-tf-files-deep (io/file (io/resource "terraform"))))

(deftest text->ast-test
  (doseq [file (get-test-files)]
    (is (ast? (parse/text->ast (slurp file))))))

(deftest text->ir-test
  (doseq [file (get-test-files)]
    (is (ir? (parse/text->ir (slurp file))))))
