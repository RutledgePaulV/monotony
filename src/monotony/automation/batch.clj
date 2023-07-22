(ns monotony.automation.batch
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as sets]
            [clojure.walk :as walk]
            [missing.core :as miss]
            [missing.topology :as top]
            [monotony.analysis.files :as files]
            [monotony.automation.core :as auto]
            [monotony.utils :as utils]))


(defn assemble-plan-graph [xs dependencies]
  (reduce
    (fn [graph [x y]]
      (if (contains? graph x)
        (update graph x (fnil sets/union #{})
                (sets/intersection (set y) (set (keys graph))))
        graph))
    (into {} (map (fn [x] [x #{}])) xs)
    dependencies))

(defn normalize-deps [root dependencies]
  (walk/postwalk
    (fn [form]
      (if (string? form)
        (.getAbsolutePath (io/file root form))
        form))
    (top/inverse dependencies)))

(defn get-manifest [root]
  (or (when-some [manifest (files/find-manifest root)]
        (some-> manifest
                slurp
                edn/read-string
                (update :dependencies (partial normalize-deps (.getParentFile manifest)))))
      {:dependencies {}}))

(defn plan [{:keys [root destroy target]}]
  (let [manifest (get-manifest root)
        graph    (assemble-plan-graph
                   (map #(.getAbsolutePath %) (files/find-terraform-plans root))
                   (:dependencies manifest))
        _        (clojure.pprint/pprint graph)
        results  (utils/visit-graph
                   graph
                   (fn [plan-dir]
                     (locking *out*
                       (println "planning dir" plan-dir))))]
    ))
