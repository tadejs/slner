#!/bin/sh

find ../lexicons -name "*sl.txt" | xargs -P 4 -I {} sh tag.sh {} {}.tei.xml
find ../lexicons -name "*.tei.xml" | xargs -P 4 -I {} python lemma.py {} {}.lemmas.txt

