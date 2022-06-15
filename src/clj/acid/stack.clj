(ns acid.stack)

(def
  ^{:todo "move"}
  index-of
  (fn [haystack needle]
    (first (keep-indexed #(if (= %2 needle) %1 nil) haystack))))

(def
  ^{}
  substacked
  (fn [stack]
    (let [selected (last (sort-by count stack))
          index    (index-of stack selected)]
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