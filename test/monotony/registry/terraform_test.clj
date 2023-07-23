(ns monotony.registry.terraform-test
  (:require [clojure.set :as sets]
            [clojure.test :refer :all]
            [monotony.registry.terraform :as tf]
            [monotony.support :as support]))


(deftest available-versions-test
  (let [results (tf/available-versions)]
    (is (not-empty results))
    (doseq [result results]
      (is (sets/subset? #{"arch" "filename" "name" "os" "url" "version"} (set (keys result)))))))

(deftest get-terraform-ranges-from-terraform-files-test
  (testing "I can discover version constraints"
    (doseq [dir (map #(.getAbsolutePath %) (support/get-test-plans))]
      (= #{"1.1.5"} (set (tf/get-terraform-ranges-from-terraform-files dir)))))

  (testing "i can get the most appropriate tf version"
    (support/with-props {"os.arch" "aarch64" "os.name" "mac os x"}
      (is (not-empty (tf/available-versions))))))
