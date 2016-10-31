#!/usr/bin/env bash

java -cp "slner.jar:lib/*" \
  si.ijs.slner.SloveneNER $@
