(ns monotony.registry.terraform
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as strings]
            [meander.epsilon :as m]
            [missing.core :as miss]
            [monotony.analysis.queries :as q]
            [monotony.registry.versions :as versions]
            [monotony.utils :as utils])
  (:import (java.net URL)))

(defonce version-data
  (delay (-> (slurp "https://releases.hashicorp.com/terraform/index.json")
             (json/read-str))))

(defn detect-current-architecture []
  (case (System/getProperty "os.arch")
    "aarch64" "arm64"
    "amd64" "amd64"
    "arm" "arm"
    "x86" "386"
    (throw (ex-info "Unsupported architecture" {:os (System/getProperty "os.arch")}))))

(defn detect-current-os []
  (condp (fn [test root]
           (strings/starts-with?
             (strings/lower-case root)
             (strings/lower-case test)))
         (System/getProperty "os.name")
    "mac os x" "darwin"
    "linux" "linux"
    "windows" "windows"
    "solaris" "solaris"
    "openbsd" "openbsd"
    "freebsd" "freebsd"
    (throw (ex-info "Unsupported OS" {:os (System/getProperty "os.name")}))))

(defn available-versions []
  (->> (get (force version-data) "versions")
       (vals)
       (mapcat #(get % "builds"))
       (filter #(not (strings/includes? (get % "version") "-")))
       (map #(update % "version" versions/parse-version))
       (filter (comp #{(detect-current-os)} #(get % "os")))
       (filter (comp #{(detect-current-architecture)} #(get % "arch")))
       (sort-by #(get % "version"))))

(defn get-terraform-ranges-from-terraform-files [plan-dir]
  (q/find-deep-module-tree plan-dir
    {:kind     :terraform-block
     :children (m/scan
                 {:kind  :entry
                  :key   {:value "required_version"}
                  :value ?version-constraint})}
    ?version-constraint))

(defn determine-most-appropriate-tf-version [plan-dir]
  (let [constraints     (get-terraform-ranges-from-terraform-files plan-dir)
        versions        (available-versions)
        version-numbers (map #(get % "version") (available-versions))
        by-version      (miss/index-by #(get % "version") versions)]
    (if-some [selected-version (versions/solve-version-constraints version-numbers constraints)]
      (get by-version selected-version)
      (throw (ex-info "Could not find a solution to the version constraints!" {:constraints constraints})))))


(defn ensure-version [{:strs [url version] :as version-descriptor}]
  (let [expected-path (io/file (System/getProperty "user.home") ".monotony" "bin" (strings/join "." version) "terraform")]
    (if (.exists expected-path)
      (str expected-path)
      (if-not (reduce
                  (fn [m {:keys [dir name stream] :as x}]
                    (when (and (not dir) (contains? #{"terraform"} name))
                      (io/make-parents expected-path)
                      (with-open [output (io/output-stream expected-path)]
                        (io/copy stream output))
                      (.setExecutable expected-path true)
                      (reduced true)))
                  false
                  (utils/zip-stream->reducing (.openStream (URL. url))))
        (throw (ex-info "Couldn't find terraform binary in zip file." version-descriptor))
        (str expected-path)))))

(defn terraform-binary [plan-dir]
  (ensure-version (determine-most-appropriate-tf-version plan-dir)))