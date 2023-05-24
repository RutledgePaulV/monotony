(ns monotony.query
  (:require [meander.epsilon :as m]
            [missing.core :as miss]
            [monotony.files :as files]))

(defmacro find [directory pattern expression]
  `(for [[dir# content#] (files/get-flattened-dirs ~directory)]
     (->> (miss/walk-seq content#)
          (mapcat
            (fn [form#]
              (m/search form# ~pattern ~expression))))))

(defmulti parse-node->ast (fn [node] (first node)))

(defn recurse [node]
  (if (and (or (seq? node) (seqable? node)) (keyword? (first node)))
    (parse-node->ast node)
    node))

(defmethod parse-node->ast :default [node] node)

(defmethod parse-node->ast :string_literal_ [node]
  (recurse (second node)))

(defmethod parse-node->ast :string_literal [node]
  (recurse (nth node 2)))

(defmethod parse-node->ast :string_content [node]
  (second node))

(defmethod parse-node->ast :identifier [node]
  (second node))

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
  (Long/parseLong (second node)))

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
   :type     (recurse (nth node 2))
   :name     (recurse (nth node 3))
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

(defmethod parse-node->ast :map__ [node]
  (recurse (second node)))

(defmethod parse-node->ast :list_ [node]
  {:kind     :list
   :children (map recurse (remove string? (drop 1 node)))})

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
   :arguments (map recurse (remove #{","} (butlast (drop 3 node))))})

(defmethod parse-node->ast :function_call_ [node]
  (recurse (second node)))

(defmethod parse-node->ast :expression_equals [node]
  {:kind  :equals
   :left  (recurse (nth node 1))
   :right (recurse (nth node 3))})

(defmethod parse-node->ast :module_block [node]
  {:kind     :module
   :name     (recurse (nth node 2))
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

(defn parse->ast [directory]
  (->> (files/get-flattened-dirs directory)
       (miss/map-vals recurse)
       (miss/map-keys #(.getAbsolutePath %))))