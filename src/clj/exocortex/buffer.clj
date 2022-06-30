(ns exocortex.buffer
  (:require [clojure.string :as str])
  (:gen-class))

(defprotocol FilesystemLogger
  (push! [this event]))

(deftype Buffer [path]

  FilesystemLogger

  (push!
    [this event]
    (if (-> (fn [e] (contains? (:options event) e))
            (filter [:problem :dissolve
                     :also :append :pop :prepend :sub :todo])
            (count)
            (> 0))
      (do
        (spit path
              (str (assoc event :arguments (str/join " " (:arguments event))) "\n")
              :append true)))))