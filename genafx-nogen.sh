#!/bin/bash

set -e

baseout="$1"
shift 1

a2ps --file-align=virtual --header="Affixes" --tabsize=2 -E -g -o output/"$baseout".ps output/"$baseout".txt

ps2pdf output/"$baseout".ps output/"$baseout".pdf

mupdf output/"$baseout".pdf
