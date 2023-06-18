(ns monotony.analysis.parse-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [monotony.analysis.parse :as parse]))

(defn is-ast? [x]
  (and (is (seq? x)) (= :file_ (first x))))

(deftest parse-test
  (doseq [file (file-seq (io/file (io/resource "terraform-samples")))
          :when (.isFile file)]
    (is (is-ast? (parse/parse (slurp file))))))
