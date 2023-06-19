(ns monotony.registry.versions
  (:require [clojure.string :as strings]
            [missing.core :as miss]))

(defn version-number? [x]
  (some? (re-find #"^\d+\.\d+\.\d+$" x)))

(defn parse-version [version]
  (let [[version] (strings/split version #"-" 2)]
    (mapv parse-long (strings/split version #"\."))))

(defn normalize-constraint [x]
  (-> (if (version-number? x) (str "= " x) x)
      (strings/split #"\s+" 2)
      (update 1 parse-version)))

(defn expand-constraint [x]
  (->> (strings/split x #"\s*,\s*")
       (remove strings/blank?)
       (map normalize-constraint)))

(defn upper-bound [version]
  (case (count version)
    3 [(first version) (inc (second version)) 0]
    2 [(inc (first version)) 0 0]
    1 [Long/MAX_VALUE Long/MAX_VALUE Long/MAX_VALUE]))

(defn solve-version-constraints [possibilities constraints]
  (let [sorted (vec (sort possibilities))]
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
                        (filter #(miss/lt % (upper-bound version))))))
          sorted
          (mapcat expand-constraint constraints))
        (miss/greatest))))