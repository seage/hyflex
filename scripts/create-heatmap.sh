#!/bin/bash

printHelpInfo(){
    echo "Maker of heatmap from results"
    echo "The syntax of this command is as follows"
    echo "./build-page.sh {folder name}"
}

# Crate a page with results from given folder
if [ -d "results/$1" ]; then
    python3 docs/heatmap/main.py $1
else
    echo "Error: results/$1 not found"
    printHelpInfo
fi
 