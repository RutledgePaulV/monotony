(ns monotony.parse
  (:require
    [clojure.java.io :as io]
    [clj-antlr.core :as antlr]
    [missing.core :as miss]))

(defn create-parser []
  (antlr/parser
    (.getAbsolutePath (io/file (io/resource "monotony/TerraformLexer.g4")))
    (.getAbsolutePath (io/file (io/resource "monotony/TerraformParser.g4")))
    {:use-alternates? true}))

(def parser
  (delay (create-parser)))

(defn get-parser []
  (if (bound? #'*ns*) (create-parser) (force parser)))

(def terraform-parser
  (fn parser
    ([] ((get-parser)))
    ([x] ((get-parser) x))
    ([x & more] ((get-parser) x))))

(defn is-node-of-type? [type node]
  (and (seq? node) (= type (first node))))

(defn is-module-node? [node]
  (is-node-of-type? :module node))

(defn is-blockbody-node? [node]
  (is-node-of-type? :blockbody node))

(defn is-argument-node? [node]
  (is-node-of-type? :blockargument node))

(defn is-identifier-node? [node]
  (is-node-of-type? :identifier node))

(defn is-source-attribute-argument? [node]
  (and (is-argument-node? node)
       (when-some [found (miss/seek is-identifier-node? (rest node))]
         (= '(:identifierchain "source") (second found)))))

(defn extract-module-references [ast]
  (let [results (atom {})]
    (clojure.walk/postwalk
      (fn [form]
        (if (is-module-node? form)
          (do
            (miss/when-some* [blockbody (miss/seek is-blockbody-node? form)]
              (miss/when-some* [source-node (miss/seek is-source-attribute-argument? blockbody)]
                (let [value (nth source-node 3)]
                  (swap! results assoc
                         (second (nth form 2))
                         (-> value second second second second))))))
          form))
      ast)
    (deref results)))