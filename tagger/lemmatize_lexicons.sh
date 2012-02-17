#!/bin/sh

find ../lexicons -name "*sl.txt" | xargs -P 2 -I {} sh tag.sh {} {}.tei.xml
find ../lexicons -name "*.tei.xml" | xargs -P 2 -I {} python lemmas.py {} {}.lemmas.txt

