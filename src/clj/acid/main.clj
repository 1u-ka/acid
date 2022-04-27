(ns acid.main
  (:require [acid.hordeq :as hq]
            [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            io.fs)
  (:gen-class))

(def
  ^{}
  options
  [["-f" "--for"         "Manage HORDEQ/stack for another person"]
   ["-p" "--problem"     "Specify a (sub)problem to reprioritize"]
   ["-d" "--dissolve"    "Pop off the focused problem"]
   ["-l" "--all"         "List all problems"]
   ["-c" "--ctx CONTEXT" "Which context to operate on"]
   ["-s" "--save"        "Save the solution to a knowledgebase."]
   ["-h" "--help" "This helps you (heopfully)"]])

(defmulti
  ^{}
  render!
  (fn [x & _] x))

(defmethod
  ^{}
  render!
  :str
  [_ data]
  (println data))

(defmethod
  ^{:doc      "Renders the focused-in problem stack"
    :since    "0.1"
    :arglists '([stack])
    :return   nil}
  render!
  :vec
  [_ stack]
  #_(sh "clear")
  (if (empty? stack)
    nil
    (let [general ">"
          focused "\u001b[31m->>\u001b[0m"
          total (count stack)]
      (println "  ...")
      (loop [i 0]
        (->> (stack i)
             (format "%s %s" (if (= (inc i) total) focused general))
             (format "%s%s" (str/join "" (repeat i "  ")))
             (format "\n%s")
             (println))
        (if (< (inc i) total)
          (recur (inc i))
          nil)))))

(defmacro
  ^{}
  genfp [& [ctx]]
  `(io.fs/expandfp (str "/.acid."
                        (or ~ctx "primary")
                        ".edn")))

(defmulti
  ^{:doc "?"}
  dissolved
  (fn [x & _] x))

(defmethod
  ^{:doc "stack bassed task dissolution"}
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
  (let [flags (:options opts)]
    (cond (:also flags)
          (hq/alsoed queue (:arguments opts)))
          (:append flags)
          (hq/appended queue (:arguments opts))
          (:peek flags)
          (hq/peek queue)
          (:pop flags)
          (hq/popped queue)
          (:prepend flags)
          (hq/prepended queue)
          (:sub flags)
          (hq/deepened queue)
          (:todo flags)
          (hq/noted queue)))))

(def
  ^{:doc   "Main"
    :since "0.1.0"}
  acid!
  (fn [argv]
    (let [opts     (parse-opts argv options)
          cold     (io.fs/read! (genfp (get-in opts [:options :ctx])))
          dat      (if (vector? cold) {:self cold} cold)
          for      (get-in [:options :for])
          for      (if for (keyword for) :self)
          stack    (:self dat)
          changed? (-> (fn [e] (contains? (:options opts) e))
                       (filter [:problem :dissolve])
                       (count)
                       (> 0))]
      (if (get-in opts [:options :help])
        (str "USAGE: acid [OPTION] [problem]\n\nOPTIONS:\n\n"
             (:summary opts))
        (let [processed (dissolved (if for :hordeq :stack) opts stack)]
          (if changed?
            (-> (genfp (get-in opts [:options :ctx]))
                (io.fs/write! (assoc dat for processed)))
          processed)))))

(def
  ^{}
  -main
  (fn [& argv]
    (let [res (acid! (if argv argv '()))]
      (render! (if (vector? res) :vec :str) res))))

(comment
  (acid! '("-c acid"))
  )
