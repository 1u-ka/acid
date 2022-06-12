(ns acid.main
  (:require acid.output
            [clojure.string :as str]
            io.fs
            [acid.dissolver :refer [dissolve!]])
  (:gen-class))

(def
  ^{}
  -main
  (fn [& argv]
    (let [res (dissolve! (if argv argv '()))]
      (acid.output/render! (if (vector? res) :vec :str) res))))
