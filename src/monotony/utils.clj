(ns monotony.utils
  (:require [clojure.java.io :as io]
            [clojure.set :as sets]
            [clojure.string :as strings]
            [missing.topology :as top])
  (:import (clojure.lang IReduceInit)
           (java.io FilterInputStream InputStream)
           (java.nio.file Paths)
           (java.util.concurrent ExecutorService Executors)
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

(defn visit-graph
  "Visits a graph concurrently while respecting dependency order. Waits
   until all parts of the graph have been processed and returns a map
   summarizing the execution including results, errors, and abandoned
   sections of the graph."
  ([graph visitor]
   (with-open [executor (Executors/newCachedThreadPool)]
     (visit-graph executor graph visitor)))
  ([^ExecutorService executor graph visitor]
   (if (empty? graph)
     {:errors {} :results {} :abandoned #{}}
     (let [prom   (promise)
           nodes  (top/nodes graph)
           graph' (top/inverse graph)
           state  (add-watch
                    (atom {:abandoned #{}
                           :errors    {}
                           :results   {}})
                    :watch
                    (fn [k r o n]
                      (when (= nodes
                               (sets/union
                                 (set (:abandoned n))
                                 (set (keys (:errors n)))
                                 (set (keys (:results n)))))
                        (deliver prom n))))]
       (letfn [(submit [node]
                 (.submit executor
                          (reify Callable
                            (call [this]
                              (try
                                (let [result    (visitor node)
                                      new-state (swap! state update :results assoc node result)]
                                  (doseq [next (shuffle (top/outgoing-neighbors graph node))]
                                    (when (sets/subset?
                                            (top/outgoing-neighbors graph' next)
                                            (set (keys (:results new-state))))
                                      (submit next))))
                                (catch Exception e
                                  (swap! state
                                         (fn [state]
                                           (-> state
                                               (update :errors assoc node e)
                                               (update :abandoned sets/union (disj (top/nodes (top/transitive-select-nodes graph [node])) node)))))))))))]
         (run! submit (shuffle (top/sources graph)))
         (deref prom))))))