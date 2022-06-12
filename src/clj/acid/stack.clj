(ns acid.stack)

(def
  ^{}
  substacked
  (fn [stack]
    (let [selected (last (sort-by count stack))
          index    (.indexOf stack selected)]
      (prn index)
      [selected index])))

(def
  ^{}
  pushed
  (fn [stack problem]
    (let [[substack idx] (substacked stack)]
      (assoc stack idx (conj substack problem)))))

(def
  ^{}
  popped
  (fn [stack]
    (let [[substack idx] (substacked stack)]
      (assoc stack idx (pop substack)))))