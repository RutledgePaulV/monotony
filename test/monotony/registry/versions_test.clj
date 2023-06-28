(ns monotony.registry.versions-test
  (:require [clojure.test :refer :all])
  (:require [monotony.registry.versions :as versions]))

(deftest solve-version-constraints-test
  (is (= [1 0 9]
         (versions/solve-version-constraints
           ["0.12.0" "0.13.0" "1.0.0" "1.0.1" "1.0.5" "1.0.9"]
           ["> 0.12" "< 1.2"])))

  (is (= [1 0 5]
         (versions/solve-version-constraints
           ["0.12.0" "0.13.0" "1.0.0" "1.0.1" "1.0.5" "1.0.9"]
           ["> 0.12" "< 1.0.9"])))

  (is (= [0 13 0]
         (versions/solve-version-constraints
           ["0.12.0" "0.13.0" "1.0.0" "1.0.1" "1.0.5" "1.0.9"]
           ["~> 0.12"]))))
