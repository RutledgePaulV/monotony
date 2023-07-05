(ns monotony.automation.batch
  (:require [missing.core :as miss]
            [monotony.analysis.files :as files]
            [monotony.utils :as utils]))


(defn assemble-plan-graph [xs dependencies]
  )


(assemble-plan-graph
  ["plans/dev" "plans/prod" "plans/org" "plans/artifacts"]
  {"plans/dev"  ["plans/org" "plans/artifacts"]
   "plans/prod" ["plans/org" "plans/artifacts"]})


(defn plan [{:keys [root destroy target exclusions dependencies]}]
  (let [excluded? (apply some-fn (map #(miss/glob-matcher root %)))]
    (->> (files/find-terraform-plans root)
         (remove excluded?)
         )))
