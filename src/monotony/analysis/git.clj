(ns monotony.analysis.git
  "For analyzing terraform trees between two git commits in order to determine
   the smallest subset of targets that must be applied."
  (:require [clojure.java.shell :as sh]
            [monotony.analysis.graph :as graph]
            [monotony.analysis.files :as files]
            [monotony.analysis.parse :as parse]))


(defn get-base-ref []
  (System/getenv "GITHUB_BASE_REF"))

(defn get-head-ref []
  (System/getenv "GITHUB_HEAD_REF"))

(defn checkout [dir ref]
  (sh/sh "git" "checkout" ref :dir dir))

(defn dir->graph [dir]
  (->> (files/get-flattened-dir dir)
       (parse/text->ir)
       (graph/ir->graph)))

(defn diff-graph [base-graph head-graph]
  )

(defn build-targets [plan-dir base-ref head-ref]
  (let [_          (checkout plan-dir head-ref)
        head-graph (dir->graph plan-dir)
        _          (checkout plan-dir base-ref)
        base-graph (dir->graph plan-dir)
        diff       (diff-graph base-graph head-graph)]
    ))
