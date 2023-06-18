(ns monotony.automation.plans
  (:require [missing.core :as miss]))

(defn change->actions [change]
  (get-in change ["change" "actions"]))

(defn dispatch [x]
  (change->actions x))

(defn maybe-sensitive [x]
  (if (contains? #{{} []} x) x "(sensitive)"))

(defmulti print-action #'dispatch)

(defmethod print-action ["create"] [x]
  (println (format "# %s will be created" (get x "address")))
  (println (format "+ resource \"%s\" \"%s\" {" (get x "type") (get x "name")))
  (doseq [[k v] (into (sorted-map)
                      (merge (get-in x ["change" "after"])
                             (miss/map-vals
                               (get-in x ["change" "after_unknown"])
                               (constantly "(known after apply)"))
                             (miss/map-vals
                               (get-in x ["change" "after_sensitive"])
                               maybe-sensitive)))]
    (println (format "    %s = %s" k v)))
  (println "}"))

(defn view-plan [{:strs [resource_changes]
                  :or   {resource_changes []}}]
  (run! print-action (sort-by dispatch resource_changes)))