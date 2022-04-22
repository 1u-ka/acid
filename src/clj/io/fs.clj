(ns io.fs
  (:require [clojure.java.io :as io])
  (:gen-class))

(defmacro ^{} genfp []
  (str (get (System/getenv) "HOME")
       "/.acid.edn"))

(def ^{}
  read!
  (fn []
    (let [default {:self ["hi world!"]}
          fp      (genfp)]
      (if-not (.exists (io/file fp))
        (do (spit fp default)
            default)
        (read-string (slurp fp))))))

(def ^{}
  write!
  (fn [dat]
    (spit (genfp) dat)))