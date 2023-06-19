(ns monotony.cli.schemas
  (:require [clojure.java.io :as io]))

(def ExistingFile
  [:fn {:error/message "directory must exist"} (fn [x] (.exists (io/file x)))])

(def IsDirectory
  [:fn {:error/message "must be a directory"} (fn [x] (.isDirectory (io/file x)))])

(def DirectoryThatExists
  [:schema {:default-fn (fn [] (System/getenv "PWD"))}
   [:and :string IsDirectory ExistingFile]])