#!/bin/bash

docker run \
    -it \
    -p 7474:7474 \
    -p 7687:7687 \
    -e NEO4J_AUTH=neo4j/neo4clojure \
    -e NEO4J_dbms_logs_debug_level=ERROR \
    -v $HOME/neo4j/data:/data \
    neo4j:4.4.9-community