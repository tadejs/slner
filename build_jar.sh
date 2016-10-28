#!/usr/bin/env bash
set -ex
TARGET_JAR=slner.jar
TARGET_DIR=./build
FLAGS=""

rm -rf ./build
mkdir -p ./build

javac $FLAGS -cp "./lib/*" \
  -sourcepath src \
  -d $TARGET_DIR \
  src/si/ijs/slner/SloveneNER.java

cd $TARGET_DIR

jar cfe ../$TARGET_JAR \
  si/ijs/slner/SloveneNER \
  si

cd --
