#!/usr/bin/env bash

if [ -z "$GRAALVM_HOME" ]; then
    echo 'Please set $GRAALVM_HOME'
    exit 1
fi

# GraalVM steps
"$GRAALVM_HOME/bin/gu" install native-image
"$GRAALVM_HOME/bin/native-image" \
    -H:Name=bin/acid \
    -H:+ReportExceptionStackTraces \
    --initialize-at-build-time \
    --verbose \
    --no-fallback \
    --no-server \
    "-J-Xmx3g" \
    -jar target/acid.main-0-standalone.jar \
    bin/acid