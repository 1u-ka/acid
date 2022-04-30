(ns acid.main
  (:require [acid.hordeq :as hq]
            [acid.knowledgebase :refer [note!]]
            acid.output
            [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            io.fs)
  (:gen-class))

(def
  ^{}
  options
  [["-p" "--problem"     "Specify a (sub)problem to reprioritize"]
   ["-d" "--dissolve"    "Pop off the focused problem"]
   ["-s" "--save LAST-N" "Save the solution for previous N problems to a ~/knowledgebase-{timestamp}.md file"]
   ["-l" "--all"         "List all problems"]
   ["-c" "--ctx CONTEXT" "Which context to operate on"]

   ["-f" "--for PERSON"  "Manage HORDEQ/stack for another person"]
   ["-a" "--append"      "Appends to active queue"]
   ["-i" "--prepend"     "Prepends active queue"]
   ["-x" "--pop"         "Pops an issue off the active queue"]
   ["-o" "--also"        "Appends queue with a lower-prioritized issue"]
   ["-n" "--sub"         "Prepends a new queue as active, prioritized before whatever was already there."]
   ["-t" "--todo"        "Notes a @todo at the very end of the queue"]
   
   ["-h" "--help" "This helps you (heopfully)"]])

(defmacro
  ^{}
  genfp [& [ctx]]
  `(io.fs/expandfp (str "/.acid." ~ctx ".edn")))

(defmulti
  ^{:doc "?"}
  dissolved
  (fn [x & _] x))

(defmethod
  ^{:doc  "stack bassed task dissolution"}
  dissolved
  :stack
  [_ opts stack]
  (let [flags (:options opts)]
    (cond (:problem flags)
          (conj stack (str/join " " (:arguments opts)))
          (:dissolve flags)
          (pop stack)
          (:all flags)
          stack
          :else
          (into [] (take-last 6 stack)))))

(defmethod
  ^{:doc  "task dissolution based on a hierarchical
           output-restricted double ended queue data structure"

    :todo "shorten the structural pattern segment by going
           through the flags then casting its key to a
           symbol, like so:

           (symbol (str \"ns/\" (str flag))))"
    }
  dissolved
  :hordeq
  [_ opts queue]
  (let [flags (:options opts)
        text  (str/join " " (:arguments opts))]
    (cond (:also flags)    (hq/alsoed queue text)
          (:append flags)  (hq/appended queue text)
          (:peek flags)    (hq/current queue)
          (:pop flags)     (hq/popped queue)
          (:prepend flags) (hq/prepended queue text)
          (:sub flags)     (hq/deepened queue text)
          (:todo flags)    (hq/noted queue text)
          :else queue)))

(def
  ^{:doc   "?"
    :since "0.1.0"
    :todos ["two different logic flows with a bunch of
             ternaries in use for control flow, not good"
            "get rid of the (sym) 'visual' let binding"]}
  acid!
  (fn [argv]
    (let [opts     (parse-opts argv options)
          ctx      (or (get-in opts [:options :ctx]) "primary")
          cold     (io.fs/read! (genfp ctx))
          dat      (if (vector? cold) {:self cold} cold)
          for-arg  (get-in opts [:options :for])
          for      (if (= nil for-arg) :self (keyword for-arg))
          stack    (get dat for)
          changed? (-> (fn [e] (contains? (:options opts) e))
                       (filter [:problem :dissolve
                                :also :append :pop :prepend :sub :todo])
                       (count)
                       (> 0))]

      (if (and (get-in opts [:options :save])
               (or (get-in opts [:options :dissolve])
                   (get-in opts [:options :pop])))
        (note! (acid.output/rendered
                (take-last (read-string (get-in opts [:options :save])) stack))
               ctx
               for
               (str/join " " (:arguments opts))))

      (if (not (= nil (get-in opts [:options :help])))
        (str "USAGE: acid [OPTION] [problem]\n\nOPTIONS:\n\n"
             (:summary opts))
        (let [processed (if (= for :self)
                          (dissolved :stack opts stack)
                          (dissolved :hordeq opts (or stack [["init"]])))]
          (if changed?
            (do (io.fs/write! (genfp ctx)
                              (assoc dat for processed))
                (if (= for :self) processed (last processed)))
            (if (= for :self) processed (last processed))))))))

(def
  ^{}
  -main
  (fn [& argv]
    (let [res (acid! (if argv argv '()))]
      (acid.output/render! (if (vector? res) :vec :str) res))))
