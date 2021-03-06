(ns acid.dissolver
  (:require [acid.graph]
            [acid.hordeq :as hq]
            [acid.knowledgebase :refer [note!]]
            acid.output
            [acid.stack :as stack]
            [clojure.string :as str]
            [io.cypher :refer [session-make]]
            io.fs)
  (:import [io.cypher Cypher])
  (:gen-class))

(defmacro
  ^{:todos ["move"
            "duplicated"]}
  genfp [& [ctx]]
  `(io.fs/expandfp (str "/.acid." ~ctx ".edn")))

(defmulti
  ^{:doc "?"}
  dissolved
  (fn [x & _] x))

(defmethod
  ^{:doc  "task dissolution based on a hierarchical
           output-restricted double ended queue data structure"

    :todos nil}
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

(defmethod
  ^{}
  dissolved
  :graph
  [_ opts]
  (let [flags (:options opts)
        graph (new Cypher (session-make))
        text  (:arguments opts)]
    (cond (:problem flags)  (acid.graph/pushed text)
          (:dissolve flags) (acid.graph/popped text)
          (:todo flags)     (acid.graph/noted  text))))

(defmethod
  ^{:doc  "stack bassed task dissolution"}
  dissolved
  :stack
  [_ opts stack]
  (let [flags (:options opts)]
    (cond (:problem flags)  (stack/pushed stack (str/join " " (:arguments opts)))
          (:dissolve flags) (stack/popped stack)
          (:todo flags)     (conj stack [(str/join " " (:arguments opts))]) ;; @todo   DRY!
          :else             stack)))

(def
  ^{:doc   "?"
    :since "?"
    :notes ["move the knowledgebase logic out to exocortex domain"]}
  dissolve!
  (fn [opts]
    (let [ctx    (or (get-in opts [:options :ctx]) "primary")
          cold   (io.fs/read! (genfp ctx))
          dat    (if (vector? cold) {:self cold} cold)
          person (keyword (or (get-in opts [:options :for]) :self))]

      ;; knowledgebase
      (if (and (get-in opts [:options :save])
               (or (get-in opts [:options :dissolve])
                   (get-in opts [:options :pop])))
        (note!
         (acid.output/rendered
          (take-last
           (read-string (get-in opts [:options :save]))
           (first (acid.stack/substacked (get dat person)))))
         ctx
         person
         (str/join " " (:arguments opts))))

      (let [processed (if (= person :self)
                        (dissolved :stack opts (:self dat))
                        (dissolved :hordeq opts (or (get dat person) [[(str "hi " person)]])))]
        (do (let [changed? (-> #(contains? (:options opts) %)
                               (filter [:problem :dissolve
                                        :also :append :pop :prepend :sub :todo])
                               (count)
                               (> 0))]
              (if changed?
                (do (io.fs/write! (genfp ctx)
                                  (assoc dat person processed))))
              (first (acid.stack/substacked processed))))))))