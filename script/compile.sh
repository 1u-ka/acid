#!/usr/bin/env bash

if [ -z "$GRAALVM_HOME" ]; then
    echo 'Please set $GRAALVM_HOME'
    exit 1
fi

#  Clojure steps
clj -M -e "(compile 'acid.main)" \
    && java -cp "$(clj -Spath)":classes acid.main

# GraalVM steps
"$GRAALVM_HOME/bin/gu" install native-image
"$GRAALVM_HOME/bin/native-image" \
    -cp $(clj -Spath):classes \
    -H:Name=bin/acid \
    -H:+ReportExceptionStackTraces \
    --initialize-at-build-time \
    --verbose \
    --no-fallback \
    --no-server \
    "-J-Xmx3g" \
    acid.main
