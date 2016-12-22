#!/usr/bin/env bash
set -ex

parent_path=$( cd "$(dirname "${BASH_SOURCE}")" ; pwd -P )


SLNER_VERSION=1.1
SLNER_JAR="${parent_path}/build/libs/slner-$SLNER_VERSION.jar"

java -cp "$SLNER_JAR:lib/*" \
  si.ijs.slner.SloveneNER $@
