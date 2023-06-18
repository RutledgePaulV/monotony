(ns monotony.core
  (:require [clojure.pprint :as pprint]
            [io.github.rutledgepaulv.cli.core :as cli]
            [monotony.cli.interface :as interface])
  (:gen-class))


(defn maybe-exit [status]
  (if-not (bound? #'*ns*)
    (System/exit status)
    (throw (ex-info "Exit!" {:status status}))))

(defn handle-execution [[action value]]
  (case action
    :documentation (println value)
    :error (do (.printStackTrace value) (maybe-exit 1))
    :result (pprint/pprint value)
    :invalid (do (println value) (maybe-exit 1))))

(defn -main [& args]
  (handle-execution (cli/run interface/MainCommand (cons "monotony" args))))

(comment

  (handle-execution (cli/run-string interface/MainCommand "monotony help"))

  )