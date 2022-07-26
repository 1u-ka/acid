(ns acid.main
  (:require [acid.dissolver    :refer [dissolve!]]
            ;[acid.graph]
            [acid.output]
            [clojure.string    :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [exocortex.buffer]
            [io.cypher         :refer [session-enabled?
                                       session-make
                                       session-close]]
            [io.fs]
            [licensing.distributable])
  (:import [io.cypher               Cypher]
           [exocortex.buffer        Buffer]
           [licensing.distributable License])
  (:gen-class))

(defmacro
  ^{:todos ["move"
            "duplicated"]}
  genfp [& [ctx]]
  `(io.fs/expandfp (str "/.acid." ~ctx ".edn")))

(def
  ^{}
  switch
  (fn [opts]
    (let [license        (new License)

          buffer         (new Buffer (or (get-in opts [:options :ctx]) "primary"))
          buffer-enabled (.permits? license "event-buffering")
          cypher-enabled (and
                          (session-enabled?)
                          (.permits? license "knowledgebase-graph-synchronization"))

          cypher         (if cypher-enabled
                           (new Cypher (session-make))
                           nil)

          search-query   (get-in opts [:options :search])]

      ; buffer
      (when (nil? (get-in opts [:options :for]))
        (.init! buffer)
        (.push! buffer (let [event (select-keys opts [:options :arguments])]
                         (->> (:arguments event)
                              (str/join " ")
                              (assoc event :arguments)))))

      (def result
        (cond
          (and (not= nil search-query)
               (nil? cypher))
          (do
            (println "Cannot search, graphdb not enabled."))

          (and (not= nil search-query)
               (not (nil? cypher)))
          (do
            (->> (.exec cypher acid.graph/search search-query)
                 (map vals)
                 (map #(:problem (first %)))
                 (into [])
                 (acid.output/render! :vec)))

          ; cypher 
          (and (not (nil? cypher))
               (not (.offline? cypher))
               (not (.is-empty? buffer)))
          (do
            (println "Cypher online, synchronizing event stream... \n")
            (loop [buff (.read buffer)]
              (prn (first buff))
              (if (next buff)
                (recur (lazy-seq (next buff))))))

          :else
          (do
          ; dissolver
            (let [res (dissolve! opts)]
              (acid.output/render! (if (vector? res) :vec :str) res)))))

      ; cleanup
      (if (not (nil? cypher))
        (session-close))

      ; return
      result)))

(def
  ^{}
  -main
  (fn [& argv] 
    (switch (parse-opts (if argv argv '())
                        [["-p" "--problem"      "Specify a (sub)problem to reprioritize"]
                         ["-d" "--dissolve"     "Pop off the focused problem"]

                         ["-s" "--save LAST-N"  "Save the solution for previous N problems to a ~/knowledgebase-{timestamp}.md file"]

                         ["-c" "--ctx CONTEXT"  "Which context to operate on"]

                         ["-f" "--for PERSON"   "Manage HORDEQ/stack for another person"]
                         ["-a" "--append"       "Appends to active queue"]
                         ["-i" "--prepend"      "Prepends active queue"]
                         ["-x" "--pop"          "Pops an issue off the active queue"]
                         ["-o" "--also"         "Appends queue with a lower-prioritized issue"]
                         ["-n" "--sub"          "Prepends a new queue as active, prioritized before whatever was already there."]
                         ["-t" "--todo"         "Notes a @todo at the very end of the queue"]

                         [nil  "--search QUERY" "A search"]

                         ["-h" "--help"         "This helps you (heopfully)"]]))))

(comment 
  
  )