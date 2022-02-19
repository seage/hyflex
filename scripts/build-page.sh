#!/bin/bash

# Crate a page with results from given folder
if [ -d "results/$1" ]; then
    python3 docs/pages/main.py $1
else
    echo "Error: results/$1 not found"
fi