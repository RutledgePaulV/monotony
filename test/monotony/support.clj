(ns monotony.support
  (:require [clojure.java.io :as io]
            [clojure.set :as sets]
            [clojure.test :refer :all]
            [missing.core :as miss]
            [missing.topology :as top]
            [monotony.analysis.files :as files]))

(defn get-test-files []
  (files/get-tf-files-deep (io/file (io/resource "terraform"))))

(defn get-test-plans []
  (files/find-terraform-plans (io/file (io/resource "terraform"))))

(defn with-props* [props fun]
  (let [original (into {} (map (fn [[k v]] [k (System/getProperty k)]) props))]
    (try
      (doseq [[k v] props]
        (System/setProperty k v))
      (fun)
      (finally
        (doseq [[k v] original]
          (System/setProperty k v))))))

(defmacro with-props [props & body]
  `(with-props* ~props (^:once fn* [] ~@body)))

(defn respects-graph-order? [graph xs]
  (let [normalized (top/normalize graph)]
    (:ordered
      (reduce
        (fn [{:keys [seen ordered]} node]
          (let [dependencies (set (top/incoming-neighbors normalized node))]
            (if (and ordered (sets/subset? dependencies seen))
              {:seen (conj seen node) :ordered true}
              (reduced {:seen (conj seen node) :ordered false}))))
        {:seen #{} :ordered true}
        xs))))

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