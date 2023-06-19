(ns monotony.cli.commands
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [missing.core :as miss]
            [monotony.analysis.files :as fs]
            [monotony.analysis.queries :as q]
            [monotony.utils :as utils])
  (:import (java.util.jar Manifest)))

(defn get-version [{:keys []}]
  (let [resource (io/resource "META-INF/MANIFEST.MF")]
    (with-open [stream (io/input-stream resource)]
      (-> (miss/map-keys str (into {} (.getMainAttributes (Manifest. stream))))
          (select-keys ["commit" "timestamp" "version"])
          (pprint/pprint)))))

(defn list-plans [{:keys [directory]
                   :or   {directory (System/getenv "PWD")}
                   :as   opts}]
  (->> (fs/find-terraform-plans (io/file directory))
       (map #(utils/relative-to directory (str %)))
       (sort)
       (run! println)))

(defn list-modules [{:keys [directory]
                     :or   {directory (System/getenv "PWD")}
                     :as   opts}]
  (->> (fs/find-terraform-modules (io/file directory))
       (map #(utils/relative-to directory (str %)))
       (sort)
       (run! println)))

(defn summarize [{:keys [directory]}]
  (->> (q/find-deep-module-tree directory
         {:kind :resource :type ?type :name ?name}
         [?type ?name])
       (group-by first)
       (miss/map-vals count)))


