(ns acid.main
  (:require acid.output

            [acid.dissolver :refer [dissolve!]]
            [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]

            [exocortex.buffer]
            io.fs)
  (:import [exocortex.buffer Buffer])
  (:gen-class))

(defmacro
  ^{:todos ["move"
            "duplicated"]}
  genfp [& [ctx]]
  `(io.fs/expandfp (str "/.acid." ~ctx ".edn")))

(def
  ^{}
  -main
  (fn [& argv]
    (let [opts   (parse-opts (if argv argv '())
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

                              ["-h" "--help" "This helps you (heopfully)"]]) 
          buffer (new Buffer (-> (get-in [:options :context] opts)
                              (or "primary")
                              (str ".buffer")
                              (genfp)))]

      ;; exocortex
      (.push! buffer (select-keys opts [:options :arguments])) 
      
      ;; dissolver
      (let [res (dissolve! opts)]
        (acid.output/render! (if (vector? res) :vec :str) res)))))