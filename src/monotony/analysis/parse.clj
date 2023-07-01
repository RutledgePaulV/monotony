(ns monotony.analysis.parse
  (:require
    [clj-antlr.core :as antlr]
    [monotony.analysis.intermediate :as ast]))

(set! *warn-on-reflection* true)

(defn create-parser []
  (antlr/parser
    "resources/monotony/TerraformLexer.g4"
    "resources/monotony/TerraformParser.g4"
    {:use-alternates? true}))

(def parser
  (comp ast/recurse (create-parser)))

(defn parse [content]
  (parser content))