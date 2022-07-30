(ns exocortex.buffer
  (:require [clojure.string :as str]
            [io.fs :refer [expandfp glob]])
  (:gen-class))

(defn list-buffer-files
  [context]
  (glob (expandfp) (re-pattern (str ".acid." context ".buffer.\\d{4}-\\d{2}.edn"))))

(defmacro
  ^{:todos ["move"
            "duplicated"]}
  genfp [& [ctx]]
  `(io.fs/expandfp (str "/.acid." ~ctx ".edn")))


(defprotocol FilesystemLogger
  (clear! [this])
  (is-empty? [this])
  (exists? [this])
  (file-list [this])
  (push! [this event])
  (read [this])
  (init! [this]))

(deftype
 ^{:notes ["redesign in OOP java"]}
 Buffer [context]

  FilesystemLogger

  (is-empty?
   [this]
   (let [files (file-list this)]
     (cond
       (empty? files) true
       :else (reduce
              (fn [acc el]
                (if (not (str/blank? (slurp el)))
                  (reduced false)))
              true
              (file-list this)))))

  (exists?
    ^{:private true}
    [this]
    (< 0 (count (file-list this))))

  (file-list
    ^{:private true}
    [this]
    (glob (expandfp) (re-pattern (str ".acid." context ".buffer.\\d{4}-\\d{2}.edn"))))

  (clear!
    ^{:todos ["remove all buffer-files found for this context
              then ensure one blank buffer-file is created"]}
    [this]
    123)

  (push!
    [this event]
    (if (-> (fn [e] (contains? (:options event) e))
            (filter [:problem :dissolve
                     :also :append :pop :prepend :sub :todo])
            (count)
            (> 0))
      (let [event (assoc event :timestamp (System/currentTimeMillis))]
        (spit (-> context
                  (str ".buffer."
                       (as-> (java.time.LocalDateTime/now) inst
                         (str (.getYear inst) "-" (format "%02d" (.getMonthValue inst)))))
                  (genfp))
              (str event "\n")
              :append true))))

  (read
    ^{:todos ["return a lazy sequence of buffer-file
              contents parsed via read-string"]}
    [this]
    (flatten
     (map (fn [e]
            (as-> (slurp e) it
              (str/split it #"\n")
              (lazy-seq it)
              (map read-string it)))
          (file-list this))))

  (init!
    ^{:notes ["to avoid re-creation during neo4j synchronization
              ensure at least one empty seqfile exists after sync"]
      :todos ["does not take into account that the edn datfile
               might not exist"]}
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

(comment

  )