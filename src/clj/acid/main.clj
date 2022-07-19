(ns acid.main
  (:require [acid.dissolver    :refer [dissolve!]]
            [acid.output]
            [clojure.string    :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [exocortex.buffer]
            [exocortex.cypher  :refer [session-enabled
                                       session-make
                                       session-close]]
            [io.fs]
            [licensing.distributable])
  (:import [exocortex.buffer        Buffer]
           [exocortex.cypher        Cypher]
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

          cypher-enabled (and
                          session-enabled
                          (.permits? license "knowledgebase-graph-synchronization"))

          cypher         (if cypher-enabled
                           (new Cypher (session-make))
                           nil)]

      (cond
        (and cypher-enabled
             (get-in opts [:options :search]))
        (do
          (reduced (.search cypher (opts :arguments))))
        
        :else
        (do
          ;; buffer
          (if (.permits? license "event-buffering")
            (let [buffer (new Buffer (or (get-in opts [:options :ctx]) "primary"))]
              (when (nil? (get-in opts [:options :for]))

                (.init! buffer)
                (.push! buffer (let [event (select-keys opts [:options :arguments])]
                                 (->> (:arguments event)
                                      (str/join " ")
                                      (assoc event :arguments)))))))

          ;; cypher
          (if cypher-enabled
            (do
              (if-not (.offline? cypher)
                (println "Cypher online, synchronizing event stream... \n"))
              (session-close)))

           ;; dissolver
          (let [res (dissolve! opts)]
            (acid.output/render! (if (vector? res) :vec :str) res)))))))

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
  
  ;;note the return from f must be from reduced for reduce to terminate early.

;;no early termination as the return of f is nil
(reduce (fn f [a b]
          (if (> b 2)
            (reduced "Done early!"))
          
          (reduced "What")
          ) [1 2 3 4 5])
;;2
;;3
;;4
;;5
;;=> nil

;;early termination as the return of f is "Done early!" wrapped in a reduce object.
(reduce (fn f [a b]
          (if (> b 2)
            (reduced "Done early!")
            (println b))) [1 2 3 4 5])
;;2
;;=> "Done early!"

  )