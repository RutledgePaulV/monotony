(ns user
  (:require [clojure.string :as strings]
            [missing.core :as miss]
            [monotony.analysis.files :as fs]
            [monotony.analysis.parse :as parse]))

(defn find-unparseable-files [root]
  (for [file (fs/get-tf-files-deep root)
        :let [content (slurp file)
              e       (try (parse/text->ast content) nil (catch Exception e e))]
        :when (some? e)]
    {:file   (.getAbsolutePath file)
     :errors (->> (deref e)
                  (miss/distinct-by :message)
                  (miss/distinct-by :line)
                  (remove nil?)
                  (mapv #(select-keys % [:line :message])))}))

(defn numbers-to-ranges [xs]
  (->> (miss/contiguous-by identity inc xs)
       (map (juxt first last))
       (sort)))

(defn view-unparseable-sections [dir]
  (->> (find-unparseable-files dir)
       (partition-by :file)
       (sort-by (comp :file first))
       (run! (fn [xs]
               (let [file    (:file (first xs))
                     content (vec (strings/split-lines (slurp file)))
                     ranges  (numbers-to-ranges (map :line xs))]
                 (println (.getAbsolutePath file))
                 (println)
                 (doseq [[start stop] ranges]
                   (println (str "  " start " - " stop))
                   (println (strings/join \newline (map strings/trim (subvec content (dec start) stop))))
                   (println))
                 (println)
                 (println))))))

