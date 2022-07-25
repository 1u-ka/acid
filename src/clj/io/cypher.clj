(ns io.cypher
  (:require [clojure.string :as str]
            [neo4j-clj.core :as neo])
  (:import (org.neo4j.driver.internal.logging ConsoleLogging)
           (java.net URI)
           (java.util.logging Level))
  (:gen-class))

(def
  ^{}
  session-enabled?
  (fn []
    (= "true" (System/getenv "GRAPHDB"))))

(def
  ^{}
  db
  (atom
   (if (session-enabled?)
     (neo/connect
      (new URI "bolt://localhost:7687")
      "neo4j"
      "neo4clojure"
      {:logging (new ConsoleLogging Level/OFF)}))))

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
  (exec [this query params] ""))

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

  (exec
    ^{}
    [this query params]
    (query
     session
     {:input params})))
