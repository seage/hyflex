#!/bin/bash


runAllHyperHeuristics(){
    # Create directory for output of processes
    mkdir -p results/log 2> /dev/null

    #All hyper-heuristics names
    hhIDs="GIHH LeanGIHH PearlHunter EPH ISEA GISS Clean Clean02 CSeneticHiveHH elomariSS HaeaHH HsiaoCHeSCHH sa_ilsHH JohnstonBiasILS JohnstonDynamicILS LaroseML LehrbaumHAHA MyHH Ant_Q ShafiXCJ ACO_HH SimSATS_HH Urli_AVEG_NeptuneHH McClymontMCHHS"
    
    #Run all hyper-heuristics in parallel
    for hhID in $hhIDs
    do
        ./hyflex-chesc-2011/build/install/hyflex-chesc-2011/bin/hyflex-chesc-2011 $@ --hyperheuristics $hhID > results/log/${hhID}_log.txt 2> results/log/${hhID}_err.txt &
    done
}

cd $(dirname "$0")
cd ..

# Test if project jar exists
if [ ! -f hyflex-chesc-2011/build/install/hyflex-chesc-2011/lib/hyflex-chesc-2011*.jar ]; then
    echo "Project needs to be compiled!"
    exit
fi

# Evaluate competition
if [[ $@ == *"evaluation"* ]]; then
    echo "Use run.sh instead."
    exit
fi


# Run competition
if [[ $@ == *"competition"* ]]; then
    #Is hh set by user
    if [[ $@ == *"-h"* || $@ == *"--hyperheuristics"* ]]; then
        echo "Don't define hyper-heuristic."
        exit
    fi

    # Is id defined by the user
    if [[ $@ == *"--id"* ]]; then
        runAllHyperHeuristics $@
        exit
    fi

    # Find the highest directory id
    maxId=0
    idDirs=$(find  ./results/ -type d -name "[0-9]*" -exec basename \{} \; 2>/dev/null)

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

