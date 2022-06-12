(ns io.fs
  (:require [clojure.java.io :as io])
  (:gen-class))

(defmacro
  ^{}
  expandfp [filename]
  `(str (get (System/getenv) "HOME") ~filename))

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
