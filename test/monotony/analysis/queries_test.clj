(ns monotony.analysis.queries-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [monotony.analysis.files :as files]
            [monotony.analysis.queries :as q]))


(defn get-test-plans []
  (files/find-terraform-plans (io/file (io/resource "terraform"))))


(deftest query-test
  (testing "I can query for provider definitions"
    (doseq [dir (map #(.getAbsolutePath %) (get-test-plans))]
      (is (= #{"local" "tls"}
             (set (q/find-deep-module-tree dir
                    {:kind :provider-block :type ?type}
                    ?type)))))))