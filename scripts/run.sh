#!/bin/bash

cd $(dirname "$0")
cd ..

# Test if project jar exists
if [ ! -f hyflex-chesc-2011/build/install/hyflex-chesc-2011/lib/hyflex-chesc-2011*.jar ]; then
    echo "Project needs to be compiled!"
    exit
fi

# Run the project
./hyflex-chesc-2011/build/install/hyflex-chesc-2011/bin/hyflex-chesc-2011 $@