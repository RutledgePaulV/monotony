(ns monotony.cli.commands
  (:require [clojure.java.io :as io]
            [monotony.analysis.files :as fs]))


(defn list-plans [_]
  (doseq [file (->> (fs/find-terraform-plans (io/file "."))
                    (map #(.toPath %))
                    (map #(.normalize %))
                    (map str)
                    (sort))]
    (println file)))

(defn list-modules [_]
  (doseq [file (->> (fs/find-terraform-modules (io/file "."))
                    (map #(.toPath %))
                    (map #(.normalize %))
                    (map str)
                    (sort))]
    (println file)))
