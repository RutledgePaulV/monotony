(ns monotony.core
  (:require
    [clojure.java.io :as io]
    [missing.core :as miss]
    [monotony.files :as files]
    [cli-matic.core :as matic])
  (:gen-class))


(defn list-plans [_]
  (doseq [file (->> (files/find-terraform-plans (io/file "."))
                    (map #(.toPath %))
                    (map #(.normalize %))
                    (map str)
                    (sort))]
    (println file)))

(defn list-modules [_]
  (doseq [file (->> (files/find-terraform-modules (io/file "."))
                    (map #(.toPath %))
                    (map #(.normalize %))
                    (map str)
                    (sort))]
    (println file)))

(defn list-plans-command []
  {:command     "list-plans"
   :description "List the available terraform plans within the current directory."
   :opts        []
   :runs        list-plans})

(defn list-modules-command []
  {:command     "list-modules"
   :description "List the available terraform modules within the current directory."
   :opts        []
   :runs        list-modules})

(defn list-dependencies-command []
  {:command     "list-dependencies"
   :description "Show the dependency tree of any terraform within the current directory."
   :opts        []
   :runs        identity})

(defn upgrade-modules []
  )

(defn upgrade-providers []
  )

(defn main-command []
  {:command     "monotony"
   :description "A supplementary cli tool for users of Terraform."
   :version     (miss/get-jar-version 'monotony)
   :opts        []
   :subcommands [(list-plans-command)
                 (list-modules-command)
                 (list-dependencies-command)]})

(defn -main [& args]
  (matic/run-cmd args (main-command)))
