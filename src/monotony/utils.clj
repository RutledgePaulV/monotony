(ns monotony.utils
  (:require [clojure.java.io :as io]
            [clojure.string :as strings])
  (:import (clojure.lang IReduceInit)
           (java.io FilterInputStream InputStream)
           (java.nio.file Paths)
           (java.util.zip ZipEntry ZipInputStream)))

(set! *warn-on-reflection* true)

(defn delete [& fs]
  (when-some [f (first fs)]
    (if-some [cs (seq (.listFiles (io/file f)))]
      (recur (concat cs fs))
      (do (io/delete-file f true)
          (recur (rest fs))))))

(defn resolve-to [base path]
  (-> (Paths/get ^String base (into-array String []))
      (.resolve ^String path)
      (.normalize)
      (.toFile)
      (.getAbsolutePath)))

(defn relative-to [base path]
  (-> (Paths/get ^String base (into-array String []))
      (.relativize ^String (Paths/get path (into-array String [])))
      (.toString)))

(defn join-segments [& xs]
  (strings/join "." (drop-while strings/blank? xs)))

(defn zip-stream->reducing [^InputStream zip-stream]
  (letfn [(entry->data [^ZipEntry x]
            {:name               (.getName x)
             :size               (.getSize x)
             :time               (.getTime x)
             :comment            (.getComment x)
             :dir                (.isDirectory x)
             :crc                (.getCrc x)
             :last-access-time   (.getLastAccessTime x)
             :last-modified-time (.getLastModifiedTime x)})]
    (reify IReduceInit
      (reduce [this rf init]
        (with-open [stream (ZipInputStream. zip-stream)]
          (loop [aggregate (unreduced init)]
            (if (reduced? aggregate)
              aggregate
              (if-some [entry (.getNextEntry stream)]
                (recur (rf aggregate
                           (assoc (entry->data entry) :stream (proxy [FilterInputStream] [stream]
                                                                (close [])))))
                aggregate))))))))
