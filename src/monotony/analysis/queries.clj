(ns monotony.analysis.queries
  "Common questions answered via AST about a terraform codebase."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [meander.epsilon :as m]
            [missing.core :as miss]
            [missing.topology :as top]
            [monotony.analysis.files :as fs]
            [monotony.analysis.parse :as parse]
            [monotony.utils :as utils]))

(defmacro find-shallow
  "Search the top-level files in a directory for a pattern in the AST."
  [directory pattern expression]
  `(let [dir# ~directory]
     (->> (fs/get-flattened-dir dir#)
          (parse/parse)
          (miss/walk-seq)
          (mapcat (fn [form#] (m/search form# ~pattern ~expression))))))

(defn find-module-dependency-tree
  [root-path]
  (let [manifest (io/file root-path ".terraform/modules/modules.json")]
    (if (.exists manifest)
      (let [modules-by-name (miss/index-by :Key (:Modules (json/read-str (slurp manifest) :key-fn keyword)))]
        (letfn [(expand [parent-node]
                  (let [parent-fqdn (:id parent-node)]
                    (->> (find-shallow
                           (:directory parent-node)
                           {:kind     :module
                            :name     ?module
                            :children (m/scan {:key {:value "source"} :value ?source})}
                           (miss/keyed ?module ?source))
                         (map (fn [{:keys [?module ?source ?version]}]
                                (let [qualified (utils/join-segments parent-fqdn ?module)
                                      spec      (get modules-by-name qualified)]
                                  {:id        qualified
                                   :source    ?source
                                   :directory (utils/resolve-to root-path (get spec :Dir))
                                   :parent    (utils/join-segments parent-fqdn)}))))))]
          (let [branch+children (comp not-empty expand)
                nodes           (tree-seq branch+children branch+children {:directory root-path :source "." :id ""})
                indexed         (->> (miss/index-by :id nodes))]
            {:graph (->> nodes
                         (reduce
                           (fn [graph node]
                             (update graph (:parent node) (fnil conj #{}) (:id node)))
                           {})
                         (top/normalize)
                         (miss/remove-keys nil?))
             :nodes indexed}))))))

(defmacro find-deep-dir-tree
  "Search a directory tree of terraform code for a pattern in the AST."
  [directory pattern expression]
  `(->> (for [[dir# content#] (fs/get-flattened-dirs ~directory)]
          (->> (parse/parse content#)
               (miss/walk-seq)
               (mapcat (fn [form#] (m/search form# ~pattern ~expression)))))
        (mapcat identity)))

(defmacro find-deep-module-tree
  "Search a logical dependency tree of terraform code for a pattern in the AST."
  [directory pattern expression]
  `(let [root-dir# ~directory]
     (if-some [analysis# (find-module-dependency-tree root-dir#)]
       (->> (top/topological-sort (:graph analysis#))
            (map (fn [node#] (get-in (:nodes analysis#) [node# :directory])))
            (distinct)
            (mapcat (fn [dir#] (find-shallow dir# ~pattern ~expression))))
       (find-shallow root-dir# ~pattern ~expression))))

(comment

  ; find all modules in the AST
  (find-module-dependencies "/Users/pvr/IdeaProjects/infra/infrastructure/modules/network_ipv6")

  )