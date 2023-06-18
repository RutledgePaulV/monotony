(ns monotony.utils
  (:require [clojure.java.io :as io]
            [clojure.string :as strings])
  (:import (java.nio.file Paths)))


(defn delete [& fs]
  (when-some [f (first fs)]
    (if-some [cs (seq (.listFiles (io/file f)))]
      (recur (concat cs fs))
      (do (io/delete-file f true)
          (recur (rest fs))))))

(defn relative-to [base path]
  (-> (Paths/get ^String base (into-array String []))
      (.resolve ^String path)
      (.normalize)
      (.toFile)
      (.getAbsolutePath)))

(defn join-segments [& xs]
  (strings/join "." (drop-while strings/blank? xs)))

