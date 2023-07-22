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
  (create-parser))

(defn text->ast [content]
  (parser content))

(defn text->ir [content]
  (ast/recurse (text->ast content)))