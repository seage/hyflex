#!/bin/bash


runAllHyperHeuristics(){
    cd ..
    #Create directory for output of processes
    mkdir log

    #All hyper-heuristics names
    hhIDs="GIHH LeanGIHH PearlHunter EPH ISEA"

    #Run all hyper-heuristics in parallel
    for hhID in $hhIDs
    do
        java -jar hyflex-chesc-2011/target/hyflex-chesc-2011-1.0.jar $@ --hyperheuristics $hhID > log/${hhID}_log.txt 2> log/${hhID}_err.txt &
    done
}

cd $(dirname "$0")

#Test if project jar exists
if [ ! -f ../hyflex-chesc-2011/target/hyflex-chesc-2011*.jar ]; then
    echo "Project needs to be compiled!"
    exit
fi

#Evaluate competition
if [[ $@ == *"competition-evaluate"* ]]; then
    echo "Use run.sh instead."
    exit
fi


#Run competition
if [[ $@ == *"competition-run"* ]]; then
    #Is hh set by user
    if [[ $@ == *"-h"* || $@ == *"--hyperheuristics"* ]]; then
        echo "Don't define hyper-heuristic."
        exit
    fi

    #Is id defined by the user
    if [[ $@ == *"--id"* ]]; then
        runAllHyperHeuristics $@
        exit
    fi

    #Find the highest directory id
    maxId=0
    idDirs=$(find  ../output/results/ -type d -name "[0-9]*" -exec basename \{} \; 2>/dev/null > /dev/null)
    if [ $? == 0 ]; then 
        for idDir in $idDirs
        do
            if [ $idDir -gt $maxId ]; then
                maxId=$idDir
            fi
        done
    fi

    runAllHyperHeuristics "$@ --id $(expr $maxId + 1 )"

    exit
fi

echo "Bad command."

