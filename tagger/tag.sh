#!/bin/sh
mono PosTaggerTag.exe -v -lem:lemmatizer-model.bin $1 tagger-model.bin $2

