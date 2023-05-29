(ns monotony.parse
  (:require
    [clj-antlr.core :as antlr]
    [clojure.java.io :as io]
    [monotony.ast :as ast]))

(defn create-parser []
  (antlr/parser
    (.getAbsolutePath (io/file (io/resource "monotony/TerraformLexer.g4")))
    (.getAbsolutePath (io/file (io/resource "monotony/TerraformParser.g4")))
    {:use-alternates? true}))

(def parser
  (delay (create-parser)))

(defn get-parser []
  (comp ast/recurse (if (bound? #'*ns*) (create-parser) (force parser))))

(def terraform-parser
  (fn parser
    ([] ((get-parser)))
    ([x] ((get-parser) x))
    ([x & more] ((get-parser) x))))