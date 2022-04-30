(ns acid.output 
  (:require [clojure.string :as str]))

(def
  ^{:private true}
  renderln
  (fn [ln i total & [options]]
    (let [general "└──"
          focused (if (:focus options) "\u001b[31m└──\u001b[0m" "└──")]
      (->> ln
           (format "%s %s" (if (= (inc i) total) focused general))
           (format "%s%s" (str/join "" (repeat i "    ")))
           (format "\n%s")))))

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
    (let [total (count stack)]
      (println "  ...")
      (loop [i 0]
        (println (renderln (stack i) i total {:focus true}))
        (if (< (inc i) total)
          (recur (inc i))
          nil)))))

(def
  ^{}
  rendered
  (fn [stack]
    (let [total (count stack)]
      (str/join "\n"
       (map-indexed
        (fn [i e] (renderln e i total))
        stack)))))