(ns monotony.analysis.queries
  "Common questions answered via AST about a terraform codebase."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [meander.epsilon :as m]
            [missing.core :as miss]
            [missing.topology :as top]
            [monotony.analysis.files :as fs]
            [monotony.analysis.parse :as parse]
            [monotony.utils :as utils])
  (:refer-clojure :exclude (find)))

(defmacro find [directory pattern expression]
  `(->> (for [[dir# content#] (fs/get-flattened-dirs ~directory)]
          (->> (parse/parse content#)
               (miss/walk-seq)
               (mapcat (fn [form#] (m/search form# ~pattern ~expression)))
               (map #(with-meta % {:dir dir#}))))
        (mapcat identity)))

(defmacro find-shallow [directory pattern expression]
  `(->> (fs/get-flattened-dir ~directory)
        (parse/parse)
        (miss/walk-seq)
        (mapcat (fn [form#] (m/search form# ~pattern ~expression)))
        (map #(with-meta % {:dir directory}))))

(defn summarize-shallow [x]
  (frequencies (find-shallow x {:kind :resource :type ?type} ?type)))

(defn summarize-deep [x]
  (->> (find x {:kind :resource
                :type ?type
                :name ?name}
             [?type ?name])
       (group-by first)
       (miss/map-groups second)
       (miss/map-vals distinct)
       (into (sorted-map))))

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
                                   :directory (utils/relative-to root-path (get spec :Dir))
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


(comment

  ; find all modules in the AST
  (find-module-dependencies "/Users/pvr/IdeaProjects/infra/infrastructure/modules/network_ipv6")

  )