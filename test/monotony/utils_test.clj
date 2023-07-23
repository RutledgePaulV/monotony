(ns monotony.utils-test
  (:require [clojure.test :refer :all]
            [missing.topology :as top]
            [monotony.support :as support]
            [monotony.utils :refer [visit-graph]]))

(def TestSampleSize 100)

(deftest visit-graph-test
  (testing "time taken is less than sequential"
    (support/attempt TestSampleSize
      (let [graph           (support/random-dag)
            sleep           200
            start           (System/currentTimeMillis)
            result          (visit-graph graph (fn [node] (Thread/sleep sleep)))
            end             (System/currentTimeMillis)]
        (is (<= (- end start) (* sleep (+ 2 (count (top/nodes graph))))))
        (is (= (set (keys (:results result))) (top/nodes graph)))
        (is (empty? (:errors result)))
        (is (empty? (:abandoned result))))))

  (testing "nodes are processed in an order agreeable with their topological sort"
    (support/attempt TestSampleSize
      (let [graph  (support/random-dag)
            order  (atom [])
            result (visit-graph graph (fn [node] (peek (swap! order conj node))))]
        (is (support/respects-graph-order? graph @order))
        (is (= (set (keys (:results result))) (top/nodes graph)))
        (is (empty? (:errors result)))
        (is (empty? (:abandoned result)))))))
