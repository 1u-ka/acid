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
  deduce
  (fn [acc opts]
    (let [license        (new License)

          cypher-enabled (and
                          session-enabled
                          (.permits? license "knowledgebase-graph-synchronization"))

          cypher         (if cypher-enabled
                           (new Cypher (session-make))
                           nil)]

      (when (and cypher-enabled
                 (get-in opts [:options :search]))
        (reduced (.search cypher (opts :arguments))))

      ;; buffer
      (when (.permits? license "event-buffering")
        (let [buffer (new Buffer (or (get-in opts [:options :ctx]) "primary"))]
          (when (nil? (get-in opts [:options :for]))

            (.init! buffer)
            (.push! buffer (let [event (select-keys opts [:options :arguments])]
                             (->> (:arguments event)
                                  (str/join " ")
                                  (assoc event :arguments)))))))

      ;; cypher
      (when cypher-enabled
        (when-not (.offline? cypher)
          (println "Cypher online, synchronizing event stream... \n"))
        (session-close))

         ;; dissolver
      (let [res (dissolve! opts)]
        (acid.output/render! (if (vector? res) :vec :str) res)))))

(def
  ^{}
  -main
  (fn [& argv]
    (reduce
     deduce
     nil
     [(parse-opts (if argv argv '())
                  [["-p" "--problem"     "Specify a (sub)problem to reprioritize"]
                   ["-d" "--dissolve"    "Pop off the focused problem"]

                   ["-s" "--save LAST-N" "Save the solution for previous N problems to a ~/knowledgebase-{timestamp}.md file"]

                   ["-c" "--ctx CONTEXT" "Which context to operate on"]

                   ["-f" "--for PERSON"  "Manage HORDEQ/stack for another person"]
                   ["-a" "--append"      "Appends to active queue"]
                   ["-i" "--prepend"     "Prepends active queue"]
                   ["-x" "--pop"         "Pops an issue off the active queue"]
                   ["-o" "--also"        "Appends queue with a lower-prioritized issue"]
                   ["-n" "--sub"         "Prepends a new queue as active, prioritized before whatever was already there."]
                   ["-t" "--todo"        "Notes a @todo at the very end of the queue"]

                   ["-h" "--help" "This helps you (heopfully)"]])])))

(comment
  (reduce
   (fn [acc passed-opts]
     (if-not (:search passed-opts)
       (reduced true))
     )
   nil
   {:options {:problem true} :arguments "what"}) 
  )