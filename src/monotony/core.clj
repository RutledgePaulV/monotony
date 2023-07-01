(ns monotony.core
  (:require [io.github.rutledgepaulv.cli.core :as cli]
            [monotony.cli.core :as mcli])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn maybe-exit [status]
  (if-not (bound? #'*ns*)
    (System/exit status)
    (throw (ex-info "Exit!" {:status status}))))

(defn handle-execution [[action value]]
  (case action
    :documentation (println value)
    :error (do (.printStackTrace ^Exception value) (maybe-exit 1))
    :result nil
    :invalid (println value)))

(defn -main [& args]
  (handle-execution (cli/run mcli/MainCommand (cons "monotony" args))))

(comment

  (handle-execution (cli/run-string mcli/MainCommand "monotony help"))

  )