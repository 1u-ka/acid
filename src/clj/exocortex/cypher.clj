(ns exocortex.cypher
  (:require [clojure.string :as str]
            [neo4j-clj.core :as neo])
  (:import (java.net URI))
  (:gen-class))

(def session-enabled (= "true" (System/getenv "ACID_CYPHER_SYNC")))

(def
  ^{}
  db
  (atom
   (if session-enabled
     (neo/connect
      (new URI "bolt://localhost:7687")
      "neo4j"
      "neo4java"))))

(def
  ^{}
  session-make
  (fn []
    (neo/get-session @db)))

(def
  ^{}
  session-close
  (fn []
    (if @db (neo/disconnect @db) @db)))

(defprotocol CypherProtocol

  (ping- [this] "")
  (offline? [this] "")
  (online? [this] "")
  (search [this query] ""))

(deftype Cypher [session]

  CypherProtocol

  (ping-
    ^{:private true}
    [this]
    ((neo/create-query
      "MATCH (p:pong) RETURN p as pong")
     session))

  (offline?
    ^{}
    [this]
    (try
      (= nil (ping- this))
      (catch Exception e
        (str/includes? (.getMessage e) "connection"))))
  
  (online?
   ^{}
   [this]
   (not (offline? this)))
  
  (search
   ^{}
   [this query]
   (prn query)))

#_(neo/defquery
    create-user
    "CREATE (u:user $user)")

#_(neo/defquery list-users
    "MATCH (u:user) RETURN u as user")