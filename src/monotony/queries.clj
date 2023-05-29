(ns monotony.queries
  "Common questions answered via AST about a terraform codebase."
  (:require [clojure.string :as string]
            [meander.epsilon :as m]
            [monotony.files :as fs])
  (:import (java.nio.file Paths)))

(defn relative-to [base path]
  (-> (Paths/get ^String base (into-array String []))
      (.resolve ^String path)
      (.normalize)
      (.toFile)
      (.getAbsolutePath)))

(defn normalize-module-source [root x]
  (cond
    (string/starts-with? x "/")
    {:file x}
    (string/starts-with? x "git@")
    {:git x}
    :else
    {:file (relative-to root x)}))


(defn find-module-dependencies
  "Uses meander to walk the syntax tree and discover all module sources."
  [root]
  (fs/find root
    {:kind     :module
     :name     ?module
     :children (m/scan {:kind :entry :key {:value "source"} :value ?value})}
    {:module ?module :source (normalize-module-source root ?value)}))


(comment

  ; find all modules in the AST
  (find-module-dependencies "/Users/pvr/IdeaProjects/infra/infrastructure/modules/network_ipv6")

  )