(ns licensing.distributable 
  (:gen-class))

(defprotocol Authorizes 
  (permits? [this key] ""))

(deftype
  ^{:todos ["add logic which permits selling of
             licenses for use of niche features"]}
  License []
  
  Authorizes

  (permits? [this key] true))