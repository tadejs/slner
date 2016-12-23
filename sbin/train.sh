#!/bin/sh

parent_path=$( cd "$(dirname "${BASH_SOURCE}")" ; pwd -P )

SLNER_VERSION=1.1
SLNER_JAR="${parent_path}/build/libs/slner-$SLNER_VERSION.jar"

java -jar $SLNER_JAR --in corpus/ssj500kv1_1.zip --out-model model.ser.gz
