(ns exocortex.cypher
  (:require [clojure.string :as str]
            [neo4j-clj.core :as neo])
  (:import (java.net URI))
  (:gen-class))

(def
  ^{}
  db
  (atom
   (neo/connect
    (new URI "bolt://localhost:7687")
    "neo4j"
    "neo4java")))

(def
  ^{}
  make-session
  (fn []
    (neo/get-session @db)))

(def
  ^{}
  close-session
  (fn []
    (neo/disconnect @db)))

(defprotocol CypherProtocol

  (ping- [this] "")
  (offline? [this] "")
  (online? [this] ""))

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
   (not (offline? this))))

#_(neo/defquery
    create-user
    "CREATE (u:user $user)")

#_(neo/defquery list-users
    "MATCH (u:user) RETURN u as user")