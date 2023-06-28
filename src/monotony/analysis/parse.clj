(ns monotony.analysis.parse
  (:require
    [clj-antlr.core :as antlr]
    [clojure.java.io :as io]
    [monotony.analysis.intermediate :as ast]
    [monotony.utils :as utils])
  (:import (java.io File)
           (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)))

(defn clone [resource-paths]
  (let [temp (Files/createTempDirectory "monotony" (into-array FileAttribute []))]
    (reduce
      (fn [m path]
        (let [out-file (io/file (str temp) path)]
          (io/make-parents out-file)
          (with-open [input  (io/input-stream (io/resource path))
                      output (io/output-stream out-file)]
            (io/copy input output))
          (assoc m path out-file)))
      {}
      resource-paths)))

(defn create-parser []
  (let [mappings (clone #{"monotony/TerraformLexer.g4"
                          "monotony/TerraformParser.g4"
                          "monotony/TerraformLexer.tokens"})]
    (try
      (antlr/parser
        (.getAbsolutePath ^File (get mappings "monotony/TerraformLexer.g4"))
        (.getAbsolutePath ^File (get mappings "monotony/TerraformParser.g4"))
        {:use-alternates? true})
      (finally
        (apply utils/delete (vals mappings))))))

(def parser
  (delay (create-parser)))

(defn get-parser []
  (comp ast/recurse (if (bound? #'*ns*) (create-parser) (force parser))))

(defn parse [content]
  ((get-parser) content))