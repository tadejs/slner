#!/bin/sh
mono -O=all PosTaggerTag.exe -v -lem:lemmatizer-model.bin $1 tagger-model.bin $2

