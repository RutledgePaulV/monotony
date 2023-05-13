(ns monotony.parse-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [monotony.parse :refer :all]))


(defn is-ast? [x]
  (and (is (seq? x)) (= :file_ (first x))))

(deftest terraform-parser-test
  (doseq [file (file-seq (io/file (io/resource "terraform-samples")))
          :when (.isFile file)]
    (is (is-ast? (terraform-parser (slurp file))))))
