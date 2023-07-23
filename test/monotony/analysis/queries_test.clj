(ns monotony.analysis.queries-test
  (:require [clojure.test :refer :all]
            [monotony.analysis.queries :as q]
            [monotony.support :as support]))


(deftest query-test
  (testing "I can query for provider definitions"
    (doseq [dir (map #(.getAbsolutePath %) (support/get-test-plans))]
      (is (= #{"local" "tls"}
             (set (q/find-deep-module-tree dir
                    {:kind :provider-block :type ?type}
                    ?type)))))))