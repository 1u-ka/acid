(ns acid.graph
  (:require [neo4j-clj.core :as neo]))

(neo/defquery
  search
  "match (n:entry)-[r:BLOCKS*0..5]->(p)
   where n.problem contains $input

   return distinct p")

(neo/defquery
 currently-active-stack
 "match (n:entry)->[r:BLOCKS*0..5]->(p)
  where n.solution is null
  
  return distinct p")

(neo/defquery
 push
 "")

(neo/defquery
 pop
 "")

(neo/defquery
 todo
 "")

(def
  ^{}
  pushed
  (fn []
    ))

(def
  ^{}
  popped
  (fn []))

(def
  ^{}
  noted 
  (fn []))