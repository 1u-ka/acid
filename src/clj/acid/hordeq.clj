(ns acid.hordeq)

(defmacro
  ^{:doc ""}
  refreshed!
  [queue subqueue]
  `(assoc ~queue
          (dec (count ~queue))
          ~subqueue))

(def
  ^{:doc "."}
  appended
  (fn [queue issue]
    (refreshed! queue (vec (cons issue (last queue))))))

(defmacro
  ^{:doc "."}
  alsoed
  [queue issue]
  `(appended ~queue (str "also, " ~issue)))

(def
  ^{}
  current
  (fn [queue]
    (last (last queue))))

(def
  ^{:doc "w/ an issue popped off a subqueue"}
  popped
  (fn [queue]
    (if (not (and (= 1 (count queue))
                  (= 1 (count (last queue)))))
      (let [new (refreshed! queue (pop (last queue)))]
        (if (empty? (last new))
          (pop new)
          new)))))

(def
  ^{:doc "w/ a reprioritized subqueue"}
  prepended
  (fn [queue issue]
    (refreshed! queue (conj (last queue) issue))))

(def
  ^{:doc "w/ a new subqueue"}
  deepened
  (fn [queue issue]
    (conj queue [issue])))

(def
  ^{:doc "TODOing"}
  noted
  (fn [queue issue]
    (assoc queue 0 (vec (cons issue (first queue))))))
