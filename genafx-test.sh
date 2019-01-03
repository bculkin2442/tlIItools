#!/bin/bash

set -e

baseout="$1"
shift 1
fileset="$@"
listopts="-z -n named -r synfiles"

cd src;

javac -g AffixLister.java

mv -t .. *.class

cd ..

java AffixLister $listopts -- $fileset > output/"$baseout".txt 2> output/"$baseout".err

tail -n 2 output/"$baseout".err

a2ps --file-align=virtual --header="Affixes" --tabsize=2 -E -g -o output/"$baseout".ps output/"$baseout".txt

ps2pdf output/"$baseout".ps output/"$baseout".pdf

mupdf output/"$baseout".pdf
