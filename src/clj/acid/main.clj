(ns acid.main
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [io.fs :as fs])
  (:gen-class))

(def
  ^{}
  options
  [["-p" "--problem"     "Specify a (sub)problem to reprioritize"]
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

(def
  ^{}
  dissolve
  (fn [opts stack]
    (let [flags (:options opts)]
      (cond (:problem flags)
            (conj stack (str/join " " (:arguments opts)))
            (:dissolve flags)
            (pop stack)
            (:all flags)
            stack
            :else
            (into [] (take-last 6 stack))))))

(def
  ^{:doc   "Main"
    :since "0.1.0"}
  acid!
  (fn [argv]
    (let [opts     (parse-opts argv options)
          cold     (fs/read!)
          dat      (if (vector? cold) {:self cold} cold)
          stack    (:self dat)
          changed? (-> (fn [e] (contains? (:options opts) e))
                       (filter [:problem :dissolve])
                       (count)
                       (> 0))]
      (if (get-in opts [:options :help])
        (str "USAGE: acid [OPTION] [problem]\n\nOPTIONS:\n\n"
             (:summary opts))
        (let [processed (dissolve opts stack)]
          (if changed?
            (fs/write! (assoc dat :self processed)))
          processed)))))

(def
  ^{}
  -main
  (fn [& argv]
    (let [res (acid! (if argv argv '()))]
      (render! (if (vector? res) :vec :str) res))))

(comment
  (acid! '(""))
  )