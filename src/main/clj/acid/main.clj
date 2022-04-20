(ns acid.main
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def options
  [[ "-p" "--problem"     "Specify a (sub)problem to reprioritize" ]
   [ "-d" "--dissolve"    "Pop off the focused problem"]
   [ "-l" "--all"         "List all problems"]
   [ "-c" "--ctx CONTEXT" "Which context to operate on"]
   [ "-s" "--save"        "Save the solution to a knowledgebase." ]
   [ "-h" "--help" "This helps you (heopfully)"]])

(defn gen-fp
  ([ctx solutions]
   (format "%s/.acid.%s%s.json"
           (get (System/getenv) "HOME")
           (if (str/blank? ctx)
             "primary"
             ctx)
           (if solutions
             ".solutions"
             "")))

  ([ctx]
   (gen-fp ctx false)))

(defmacro gen-solutions-fp [ctx]
  `(gen-fp ~ctx true))

(defn record
  ([problem solution ctx]
   (let [fp (gen-solutions-fp ctx)]
     (if-not (.exists (io/file fp))
       (do (spit fp "[]")))

     (spit fp
           (-> fp
               slurp
               json/read-str
               (conj {:p problem
                      :s solution})
               json/write-str)))))

(defn render [list]
  #_(sh "clear")
  (let [ general ">"
         focused "\u001b[31m->>\u001b[0m"
         total (count list) ]
    
    (loop [i 0]
      (->> (list i)
           (format "%s %s" (if (= (inc i) total) focused general))
           (format "%s%s" (str/join "" (repeat i "  ")))
           (format "\n%s")
           (println))
      (if (< (inc i) total)
        (recur (inc i))
        nil))))

(defn dissolve [opts stack]
  (cond (:problem (:options opts))
        [true (conj stack (str/join " " (:arguments opts)))]

        (:dissolve (:options opts))
        [true (let [tail (peek stack)]
                (if (:save (:options opts))
                  (record tail
                          (str/join " " (:arguments opts))
                          (:ctx (:options opts))))
                (pop stack))]

        (:all (:options opts))
        [false stack]
        
        :else
        (do (println "  ...")
            [false (into [] (take-last 6 stack))])))

(defn -main [& argv]
  (def   opts  (parse-opts argv options))
  (def   fp    (gen-fp (:ctx (:options opts))))

  (if-not (.exists (io/file fp))
    (do (spit fp "[\"initial\"]")))
  
  (def   stack (json/read-str (slurp fp)))
  (cond (empty? opts)
        (render stack)

        (:help (:options opts))
        (println (format "USAGE: acid [OPTION] [problem]\n\nOPTIONS:\n\n%s" (:summary opts)))

        :else
        (let [[changed processed] (dissolve opts stack)]
          (if changed
            (->> processed
                 (json/write-str)
                 (spit fp)))
          (render processed))))
