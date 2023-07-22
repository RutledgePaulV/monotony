(ns monotony.utils-test
  (:require [clojure.test :refer :all])
  (:require [clojure.set :as sets]
            [missing.core :as miss]
            [missing.topology :as top]
            [monotony.utils :refer [visit-graph]]))

(def TestSampleSize 100)

(defn partially-ordered? [partial-ordering xs]
  (:ordered
    (reduce (fn [{[head & remainder :as ordering] :ordering ordered :ordered} x]
              (if (contains? head x)
                (let [new-head (disj head x)]
                  (if (empty? new-head)
                    {:ordering remainder :ordered (and ordered true)}
                    {:ordering (cons new-head remainder) :ordered ordered}))
                (reduced {:ordering [] :ordered false})))
            {:ordering partial-ordering :ordered true}
            xs)))

(defmacro attempt [times & body]
  `(miss/dowork [_# [(range ~times) ~times]] ~@body))

(defn random-dag []
  (let [nodes (random-sample (rand) (map keyword (map (comp str char) (range 97 123))))]
    (reduce (fn [graph node]
              (loop [neighbors (disj (set (random-sample (rand) nodes)) node)]
                (let [graph' (update graph node (fnil sets/union #{}) neighbors)]
                  (if (top/cyclical? graph')
                    (recur (disj (set (random-sample (rand) nodes)) node))
                    graph'))))
            {}
            nodes)))

(deftest visit-graph-test
  (testing "time taken is bounded by the diameter of the graph when using an unbounded thread pool"
    (attempt TestSampleSize
      (let [graph           (random-dag)
            sleep           100
            start           (System/currentTimeMillis)
            result          (visit-graph graph (fn [node] (Thread/sleep sleep)))
            end             (System/currentTimeMillis)
            max-serial-deps (count (top/topological-sort-with-grouping graph))]
        (is (< (- end start) (* sleep (inc max-serial-deps))))
        (is (= (set (keys (:results result))) (top/nodes graph)))
        (is (empty? (:errors result)))
        (is (empty? (:abandoned result))))))

  (testing "nodes are processed in an order agreeable with their topological sort"
    (attempt TestSampleSize
      (let [graph  (random-dag)
            order  (atom [])
            result (visit-graph graph (fn [node] (peek (swap! order conj node)) (Thread/sleep 10)))]
        (is (partially-ordered? (top/topological-sort-with-grouping graph) @order))
        (is (= (set (keys (:results result))) (top/nodes graph)))
        (is (empty? (:errors result)))
        (is (empty? (:abandoned result)))))))
