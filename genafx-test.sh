#!/bin/bash

# Runs the AffixLister, formats the resulting text to PDF format, and then
# displays it
# The first argument is the base output name; the rest of the arguments are the
# files to input
set -e

baseout="$1"
shift 1
fileset="$@"
listopts="--guess-groups -z -n named"
outputfle=output/"$baseout".txt
errfle=output/"$baseout".err
afxgroupfle=output/"$baseout".afxgroup
outputopts="-o $outputfle -e $errfle --output-affix-groups $afxgroupfle"

mvn compile exec:java -Dexec.args="$outputopts $listopts $fileset"
#java AffixLister $listopts $fileset > output/"$baseout".txt 2> output/"$baseout".err

tail -n 2 "$errfle"

a2ps --file-align=virtual --header="Affixes" --tabsize=2 -E -g -o output/"$baseout".ps "$outputfle"

ps2pdf output/"$baseout".ps output/"$baseout".pdf

mupdf output/"$baseout".pdf
