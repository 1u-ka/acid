(ns exocortex.buffer
  (:require [clojure.string :as str]
            [io.fs :refer [expandfp glob]])
  (:gen-class))

(defmacro
  ^{:todos ["move"
            "duplicated"]}
  genfp [& [ctx]]
  `(io.fs/expandfp (str "/.acid." ~ctx ".edn")))


(defprotocol FilesystemLogger
  (exists? [this])
  (init! [this])
  (push! [this event]))

(deftype
  ^{:notes ["redesign in OOP java"]}
  Buffer [context]

  FilesystemLogger

  (exists?
    ^{:private true}
    [this]
    (< 0 (count (glob (expandfp) (re-pattern (str ".acid." context ".buffer.\\d{4}-\\d{2}.edn"))))))

  (push!
   [this event]
   (if (-> (fn [e] (contains? (:options event) e))
           (filter [:problem :dissolve
                    :also :append :pop :prepend :sub :todo])
           (count)
           (> 0))
     (do
       (spit (-> context
                 (str ".buffer."
                      (as-> (java.time.LocalDateTime/now) inst
                        (str (.getYear inst) "-" (format "%02d" (.getMonthValue inst)))))
                 (genfp))
             (str event "\n")
             :append true))))

  (init!
   ^{
     :notes ["to avoid re-creation during neo4j synchronization
              ensure at least one empty seqfile exists after sync"]
   } 
   [this]
   (if-not (exists? this)
     (let [stacklist (-> (slurp (genfp context))
                         (read-string)
                         (:self)
                         (sort))]
       (doseq [stack stacklist]
         (doseq [el stack]
           (push! this
                  {:options (if (= (first stack) el)
                              {:todo true} {:problem true})
                   :arguments el}))))
     (println "Event stream is queued for synchronization with graph datastore.\n"))))