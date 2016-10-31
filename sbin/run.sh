#!/usr/bin/env bash
set -ex

SLNER_VERSION=1.1
SLNER_JAR="build/libs/slner-$SLNER_VERSION.jar"

java -cp "$SLNER_JAR:lib/*" \
  si.ijs.slner.SloveneNER $@
