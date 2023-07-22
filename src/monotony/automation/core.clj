(ns monotony.automation.core
  (:refer-clojure :exclude [apply])
  (:require [babashka.process :as process]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as strings]
            [monotony.utils :as utils])
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream File)
           (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)))

(set! *warn-on-reflection* true)

(def REGEX #"plugin_cache_dir\s*=\s*\"?([^\s\"]+)\"?(\R|$)")

(defn detect-plugin-cache-dir []
  (when-some [file (io/file (System/getProperty "user.home") ".terraformrc")]
    (when (and (.exists file) (.canRead file))
      (second (re-find REGEX (slurp file))))))

(defn get-tf-plugin-cache-dir []
  (or (System/getenv "TF_PLUGIN_CACHE_DIR")
      (some-> (detect-plugin-cache-dir)
              (strings/replace "$HOME" (System/getProperty "user.home")))
      (some-> (System/getProperty "user.home")
              (io/file ".terraform.d" "plugin-cache")
              (.getAbsolutePath))))

(defn new-temp-dir []
  (.toFile (Files/createTempDirectory "monotony" (into-array FileAttribute []))))

(defn new-context
  ([] (new-context {}))
  ([context-to-clone]
   (merge context-to-clone {:dir (new-temp-dir)})))

(defn file->bytes [^File f]
  (let [out (ByteArrayOutputStream.)]
    (with-open [in  (io/input-stream (io/file f))
                out out]
      (io/copy in out))
    (.toByteArray out)))

(defn is-in-terraform-dir? [path]
  (or
    (= ".terraform" path)
    (strings/ends-with? path "/.terraform")
    (strings/starts-with? path ".terraform/")
    (strings/includes? path "/.terraform/")))

(defn dir->content [dir]
  (let [root (io/file dir)]
    (->> (file-seq root)
         (filter #(.isFile ^File %))
         (remove #(is-in-terraform-dir? (.getAbsolutePath ^File %)))
         (map (fn [^File x] [(str (.relativize (.toPath root) (.toPath x))) (file->bytes x)]))
         (into {}))))

(defn content->dir [dir content]
  (doseq [[path bites] content
          :let [f (io/file dir path)]]
    (io/make-parents f)
    (with-open [in  (ByteArrayInputStream. bites)
                out (io/output-stream f)]
      (io/copy in out))))

(defn empty-dir [dir]
  (->> (file-seq (io/file dir))
       (remove #{(io/file dir)})
       (remove #(is-in-terraform-dir? (.getAbsolutePath ^File %)))
       (run! utils/delete)))

(defn dir->context [dir]
  {:dir (io/file dir)})

(defn context->dir [context]
  (:dir context))

(defn context->env [context]
  (:env context {}))

(defn context->content [context]
  (:content context))

(defn with-file [context filename file]
  (assoc-in context [:content filename] (.getBytes ^String file)))

(defn with-edn-file [context filename edn]
  (assoc-in context [:content filename] (.getBytes (json/write-str edn))))

(defn spit-context! [context]
  (empty-dir (context->dir context))
  (content->dir (context->dir context) (context->content context)))

(defn slurp-context! [context]
  (-> context (assoc :content (dir->content (context->dir context)))))

(defn execute
  ([context command]
   (execute context command {}))
  ([context command overrides]
   (spit-context! context)
   (let [options  {:out       :inherit
                   :dir       (context->dir context)
                   :err       :inherit
                   :env       (into {} (System/getenv))
                   :extra-env (cond-> (context->env context)
                                :always
                                (merge {"TF_PLUGIN_CACHE_DIR" (get-tf-plugin-cache-dir) "AWS_SDK_LOAD_CONFIG" "1"})
                                (not (.exists (io/file (context->dir context) ".terraform.lock.hcl")))
                                (merge {"TF_PLUGIN_CACHE_MAY_BREAK_DEPENDENCY_LOCK_FILE" "true"}))}
         response (clojure.core/apply process/sh (merge options overrides) (process/tokenize command))]
     (assoc (slurp-context! context) :result response))))

(defn options->args [options]
  (map (fn [[k v]] (if (true? v)
                     (str "-" (name k))
                     (str "-" (name k) "=" v))) options))

(defn terraform-command [& args+options]
  (->> (mapcat (fn [x] (if (map? x) (options->args x) [x])) args+options)
       (into ["terraform"])
       (strings/join \space)))

(defn terraform [context options & args+options]
  (execute context (clojure.core/apply terraform-command args+options) options))

(defn init [context]
  (terraform context {} "init"))

(defn show [context input-file]
  (terraform context {:out :string} "show" {:json true} input-file))

(defn plan [context output-file]
  (terraform context {} "plan" {:no-color true :input false :out output-file}))

(defn plan+json [context]
  (-> context
      (plan "plan.tfplan")
      (show "plan.tfplan")
      (update-in [:result :out] json/read-str)))

(defn schemas [context]
  (-> context
      (terraform {:out :string} "providers schema" {:json true})
      (update-in [:result :out] json/read-str)))

(defn apply [context input-file]
  (terraform context {} "apply" {:no-color true :input false :auto-approve true} input-file))

(defn view-state [context]
  (-> (terraform context {:out :string} "show" {:json true})
      (update-in [:result :out] json/read-str)))

(defn view-plan [context]
  {:schema (get-in (schemas context) [:result :out])
   :plan   (get-in (plan+json context) [:result :out])})

(comment
  (-> (new-context)
      (with-edn-file "main.tf.json"
                     {:terraform
                      {:required_version "1.4.4"
                       :required_providers
                       {:aws {:source  "hashicorp/aws"
                              :version "4.61.0"}}}

                      :provider
                      {:aws [{:region  "us-east-2"
                              :profile "dev:AdministratorAccess"}]}

                      :resource
                      {:aws_s3_bucket {:this {:bucket "this-is-a-test"}}}})
      (init))

  )