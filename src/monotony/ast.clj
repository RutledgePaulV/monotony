(ns monotony.ast
  (:require [clojure.string :as strings]))

(defmulti parse-node->ast (fn [node] (first node)))

(defn recurse [node]
  (if (and (or (seq? node) (seqable? node)) (keyword? (first node)))
    (parse-node->ast node)
    node))

(defn recurse-to-string [node]
  (let [converted (recurse node)]
    (if (and (map? converted) (= :identifier (:kind converted)))
      (:value converted)
      converted)))

(defmethod parse-node->ast :default [node] node)

(defmethod parse-node->ast :string_literal_ [node]
  (recurse (second node)))

(defmethod parse-node->ast :string_literal [node]
  (let [children (map recurse (butlast (drop 2 node)))]
    (cond
      (= 1 (bounded-count 2 children))
      (first children)
      (empty? children)
      ""
      :else
      {:kind     :string-concatenation
       :children children})))

(defmethod parse-node->ast :string_interpolate [node]
  (recurse (nth node 2)))

(defmethod parse-node->ast :string_content [node]
  (second node))

(defmethod parse-node->ast :string_content_ [node]
  (second node))

(defmethod parse-node->ast :identifier [node]
  {:kind  :identifier
   :value (second node)})

(defmethod parse-node->ast :identifier_ [node]
  (recurse (second node)))

(defmethod parse-node->ast :output_block [node]
  {:kind     :output
   :name     (recurse (nth node 2))
   :children (map recurse (remove string? (drop 3 node)))})

(defmethod parse-node->ast :assignment [node]
  {:kind  :entry
   :key   (recurse (second node))
   :value (recurse (nth node 3))})

(defmethod parse-node->ast :block_entry [node]
  (recurse (second node)))

(defmethod parse-node->ast :terraform_block [node]
  {:kind     :terraform-block
   :children (map recurse (remove string? (rest node)))})

(defmethod parse-node->ast :namespaced_block [node]
  {:kind     :namespaced-block
   :name     (recurse (nth node 1))
   :children (map recurse (remove string? (drop 2 node)))})

(defmethod parse-node->ast :dynamic_block [node]
  {:kind     :dynamic-block
   :name     (recurse (nth node 1))
   :children (map recurse (remove string? (drop 2 node)))})

(defmethod parse-node->ast :typed_block [node]
  {:kind     :typed-block
   :type     (recurse (nth node 1))
   :name     (recurse (nth node 2))
   :children (map recurse (remove string? (drop 2 node)))})

(defmethod parse-node->ast :variable_block [node]
  {:kind     :variable
   :name     (recurse (nth node 2))
   :children (map recurse (remove string? (drop 4 node)))})

(defmethod parse-node->ast :locals_block [node]
  {:kind     :locals
   :children (map recurse (remove string? (drop 3 node)))})

(defmethod parse-node->ast :data_block [node]
  {:kind     :data-block
   :type     (recurse (nth node 2))
   :name     (recurse (nth node 3))
   :children (map recurse (remove string? (drop 4 node)))})

(defmethod parse-node->ast :boolean [node]
  (Boolean/parseBoolean (second node)))

(defmethod parse-node->ast :boolean_ [node]
  (recurse (second node)))

(defmethod parse-node->ast :number_literal [node]
  (try
    (Long/parseLong (strings/join "" (rest node)))
    (catch NumberFormatException e
      (Double/parseDouble (strings/join "" (rest node))))))

(defmethod parse-node->ast :number_literal_ [node]
  (recurse (second node)))

(defmethod parse-node->ast :expression_dot [node]
  {:kind  :dotted-access
   :left  (recurse (second node))
   :right (recurse (nth node 3))})

(defmethod parse-node->ast :block_name [node]
  (recurse (second node)))

(defmethod parse-node->ast :block_type [node]
  (recurse (second node)))

(defmethod parse-node->ast :file_ [node]
  {:kind     :directory
   :children (remove string? (map recurse (drop 1 node)))})

(defmethod parse-node->ast :resource_block [node]
  {:kind     :resource
   :type     (recurse-to-string (nth node 2))
   :name     (recurse-to-string (nth node 3))
   :children (map recurse (butlast (drop 5 node)))})

(defmethod parse-node->ast :top_level_block [node]
  (recurse (second node)))

(defmethod parse-node->ast :map_ [node]
  (recurse (second node)))

(defmethod parse-node->ast :map_entries [node]
  {:kind     :map
   :children (map recurse (remove string? (rest node)))})

(defmethod parse-node->ast :map_comprehension [node]
  {:kind           :map-comprehension
   :variable       (recurse (nth node 3))
   :iterator       (recurse (nth node 5))
   :key-emission   (recurse (nth node 7))
   :value-emission (recurse (nth node 9))})

(defmethod parse-node->ast :destructured_map_comprehension [node]
  {:kind           :destructured-map-comprehension
   :key            (recurse (nth node 3))
   :value          (recurse (nth node 3))
   :iterator       (recurse (nth node 7))
   :key-emission   (recurse (nth node 9))
   :value-emission (recurse (nth node 11))})

(defmethod parse-node->ast :conditional_destructured_map_comprehension [node]
  {:kind           :conditional-destructured-map-comprehension
   :key            (recurse (nth node 3))
   :value          (recurse (nth node 3))
   :iterator       (recurse (nth node 7))
   :key-emission   (recurse (nth node 9))
   :value-emission (recurse (nth node 11))
   :condition      (recurse (nth node 13))})

(defmethod parse-node->ast :conditional_map_comprehension [node]
  {:kind           :conditional-map-comprehension
   :variable       (recurse (nth node 3))
   :iterator       (recurse (nth node 5))
   :key-emission   (recurse (nth node 7))
   :value-emission (recurse (nth node 9))
   :condition      (recurse (nth node 11))})

(defmethod parse-node->ast :map__ [node]
  (recurse (second node)))

(defmethod parse-node->ast :list_entries [node]
  {:kind     :list
   :children (map recurse (remove string? (rest node)))})

(defmethod parse-node->ast :list_comprehension [node]
  {:kind       :list-comprehension
   :variable   (recurse (nth node 3))
   :iterator   (recurse (nth node 5))
   :expression (recurse (nth node 7))})

(defmethod parse-node->ast :conditional_list_comprehension [node]
  {:kind       :conditional-list-comprehension
   :variable   (recurse (nth node 3))
   :iterator   (recurse (nth node 5))
   :expression (recurse (nth node 7))
   :condition  (recurse (nth node 9))})

(defmethod parse-node->ast :destructured_list_comprehension [node]
  {:kind       :destructured-list-comprehension
   :key        (recurse (nth node 3))
   :value      (recurse (nth node 5))
   :iterator   (recurse (nth node 7))
   :expression (recurse (nth node 9))})

(defmethod parse-node->ast :conditional_destructured_list_comprehension [node]
  {:kind       :conditional-destructured-list-comprehension
   :key        (recurse (nth node 3))
   :value      (recurse (nth node 5))
   :iterator   (recurse (nth node 7))
   :expression (recurse (nth node 9))
   :condition  (recurse (nth node 11))})

(defmethod parse-node->ast :list_ [node]
  (recurse (second node)))

(defmethod parse-node->ast :list__ [node]
  (recurse (second node)))

(defmethod parse-node->ast :map_entry [node]
  (let [children (map recurse (remove string? (drop 1 node)))]
    {:kind  :map-entry
     :key   (first children)
     :value (second children)}))

(defmethod parse-node->ast :map_key [node]
  (recurse (second node)))

(defmethod parse-node->ast :function_call [node]
  {:kind      :function-call
   :function  (recurse (second node))
   :arguments (map recurse (remove string? (drop 3 node)))})

(defmethod parse-node->ast :function_call_ [node]
  (recurse (second node)))

(defmethod parse-node->ast :expression_equals [node]
  {:kind  :equals
   :left  (recurse (nth node 1))
   :right (recurse (nth node 3))})

(defmethod parse-node->ast :module_block [node]
  {:kind     :module
   :name     (recurse-to-string (nth node 2))
   :children (map recurse (remove string? (drop 4 node)))})


(defmethod parse-node->ast :expression_ternary [node]
  (let [condition (recurse (nth node 1))
        then      (recurse (nth node 3))
        else      (recurse (nth node 5))]
    {:kind      :ternary
     :condition condition
     :then      then
     :else      else}))

(defmethod parse-node->ast :expression_bracket [node]
  {:kind  :index-access
   :left  (recurse (second node))
   :right (recurse (nth node 3))})