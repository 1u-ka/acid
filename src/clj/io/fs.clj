(ns io.fs
  (:require [clojure.java.io :as io])
  (:gen-class))

(def
  ^{}
  expandfp
  (fn
    ([filename]
     (str (get (System/getenv) "HOME") filename))
    ([]
     (expandfp ""))))

(def
  ^{}
  read!
  (fn [fp]
    (let [default {:self [["hi world!"]]}]
      (if-not (.exists (io/file fp))
        (do (spit fp default)
            default)
        (read-string (slurp fp))))))

(def ^{}
  write!
  (fn [fp dat]
    (spit fp dat)
    dat))

(def
  ^{}
  files
  (fn [path]
    (->> path
         (io/file)
         (.listFiles)
         (map #(.getPath %)))))

(def
  ^{}
  glob
  (fn [path pattern]
    (filter (fn [e]
              (re-find pattern e))
            (files path))))