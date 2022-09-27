#!/bin/bash

# Runs AffixLister with the provided arguments
set -e

mvn compile exec:java -Dexec.args="$@"
