(ns monotony.analysis.parse-test
  (:require
    [clojure.test :refer :all]
    [missing.core :as miss]
    [monotony.analysis.parse :as parse]
    [monotony.support :as support]))

(defn ast? [x]
  (and (seq? x) (= :file_ (first x))))

(defn ir? [x]
  (->> x
       (miss/walk-seq)
       (filter map?)
       (every? #(contains? % :kind))))

(deftest text->ast-test
  (doseq [file (support/get-test-files)]
    (is (ast? (parse/text->ast (slurp file))))))

(deftest text->ir-test
  (doseq [file (support/get-test-files)]
    (is (ir? (parse/text->ir (slurp file))))))
