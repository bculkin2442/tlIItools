#!/bin/bash

set -e

baseout="$1"
shift 1
fileset="$@"
listopts="-l -z -n named"
outputfle=output/"$baseout".txt
errfle=output/"$baseout".err
outputopts="-o $outputfle -e $errfle"

mvn clean compile exec:java -Dexec.args="$outputopts $listopts $fileset"
#java AffixLister $listopts $fileset > output/"$baseout".txt 2> output/"$baseout".err

tail -n 2 "$errfle"

a2ps --file-align=virtual --header="Affixes" --tabsize=2 -E -g -o output/"$baseout".ps "$outputfle"

ps2pdf output/"$baseout".ps output/"$baseout".pdf

mupdf output/"$baseout".pdf
