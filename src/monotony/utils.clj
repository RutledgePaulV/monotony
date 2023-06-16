(ns monotony.utils
  (:require [clojure.java.io :as io]))


(defn delete [& fs]
  (when-some [f (first fs)]
    (if-some [cs (seq (.listFiles (io/file f)))]
      (recur (concat cs fs))
      (do (io/delete-file f true)
          (recur (rest fs))))))
