(ns monotony.analysis.parse
  (:require
    [clj-antlr.core :as antlr]
    [clojure.java.io :as io]
    [monotony.analysis.intermediate :as ast]))

(defn create-parser []
  (antlr/parser
    (.getAbsolutePath (io/file (io/resource "monotony/TerraformLexer.g4")))
    (.getAbsolutePath (io/file (io/resource "monotony/TerraformParser.g4")))
    {:use-alternates? true}))

(def parser
  (delay (create-parser)))

(defn get-parser []
  (comp ast/recurse (if (bound? #'*ns*) (create-parser) (force parser))))

(defn parse [content]
  ((get-parser) content))