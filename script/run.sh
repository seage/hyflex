#!/bin/bash

cd $(dirname "$0")

#Test if project jar exists
if [ ! -f ../hyflex-chesc-2011/target/hyflex-chesc-2011*.jar ]; then
    echo "Project needs to be compiled!"
    exit
fi

#Run the project
java -jar ../hyflex-chesc-2011/target/hyflex-chesc-2011-1.0.jar $@