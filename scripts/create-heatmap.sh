#!/bin/bash
cd `dirname $0`

cd heatmap

# Install dependecies
python3 -m venv venv
. ./venv/bin/activate
pip install -r requirements.txt > /dev/null
cd ../..
# Run the python script
python3 ./scripts/heatmap/main.py $1

deactivate
 