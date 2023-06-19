(ns build
  (:require [clojure.tools.build.api :as b])
  (:import (java.time Instant)))

(def lib 'io.github.rutledgepaulv/monotony)
(def version "1.0.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/monotony.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uberjar [_]
  (clean nil)
  (b/copy-dir
    {:src-dirs   ["src" "resources"]
     :target-dir class-dir})
  (b/compile-clj
    {:basis     basis
     :class-dir class-dir})
  (b/uber
    {:class-dir class-dir
     :uber-file jar-file
     :basis     basis
     :manifest  {"commit"    (b/git-process {:git-args "rev-parse HEAD"})
                 "timestamp" (str (Instant/now))
                 "version"   version}
     :main      'monotony.core}))