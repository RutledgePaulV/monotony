(ns monotony.registry.versions
  (:require [clojure.string :as strings]
            [missing.core :as miss]))

(defn parse-version
  ([version] (parse-version false version))
  ([pad? version]
   (let [[version] (strings/split version #"-" 2)]
     (let [components (mapv parse-long (strings/split version #"\."))]
       (with-meta
         (loop [components components]
           (if (and pad? (< (count components) 3))
             (recur (conj components 0))
             components))
         {:unpadded components})))))

(defn normalize-constraint [x]
  (-> (if (re-find #"^\d+\.\d+\.\d+$" x) (str "= " x) x)
      (strings/split #"\s+" 2)
      (update 1 (partial parse-version true))))

(defn expand-constraint [x]
  (->> (strings/split (strings/trim x) #"\s*,\s*")
       (remove strings/blank?)
       (map normalize-constraint)))

(defn upper-bound [version]
  (case (count version)
    3 [(first version) (inc (second version)) 0]
    2 [(inc (first version)) 0 0]
    1 [Long/MAX_VALUE Long/MAX_VALUE Long/MAX_VALUE]))

(defn solve-version-constraints [possibilities constraints]
  (let [sorted (vec (sort (map (fn [x] (if (string? x) (parse-version false x) x)) possibilities)))]
    (-> (reduce
          (fn [possibilities [operator version]]
            (case operator
              "=" (filter #{version} possibilities)
              "!=" (remove #{version} possibilities)
              ">" (filter #(miss/gt % version) possibilities)
              "<" (filter #(miss/lt % version) possibilities)
              ">=" (filter #(miss/gte % version) possibilities)
              "<=" (filter #(miss/lte % version) possibilities)
              "~>" (->> possibilities
                        (filter #(miss/gte % version))
                        (filter #(miss/lt % (upper-bound (:unpadded (meta version))))))))
          sorted
          (mapcat expand-constraint constraints))
        (miss/greatest))))