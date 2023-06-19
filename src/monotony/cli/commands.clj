(ns monotony.cli.commands
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [missing.core :as miss]
            [monotony.analysis.files :as fs])
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
  (doseq [file (->> (fs/find-terraform-plans (io/file directory))
                    (map #(.toPath %))
                    (map #(.normalize %))
                    (map str)
                    (sort))]
    (println file)))

(defn list-modules [{:keys [directory]
                     :or   {directory (System/getenv "PWD")}
                     :as   opts}]
  (doseq [file (->> (fs/find-terraform-modules (io/file directory))
                    (map #(.toPath %))
                    (map #(.normalize %))
                    (map str)
                    (sort))]
    (println file)))
