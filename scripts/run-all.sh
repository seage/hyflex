#!/bin/bash
cd $(dirname "$0")
cd ..

runHyperHeuristic(){
    hhID=$1
    params=$2
    ./hyflex-chesc-2011/build/install/hyflex-chesc-2011/bin/hyflex-chesc-2011 $params --hyperheuristics $hhID > results/log/${hhID}.log 2> results/log/${hhID}.err
}

runHyperHeuristics(){
    hhIDs=$1
    params=$2
    #Run the hyper-heuristics in parallel
    #echo "Running $hhIDs"
    IFS=" "
    for hhID in $hhIDs
    do
        runHyperHeuristic "$hhID" "$params"&
        pids[${i}]=$!
    done
    # wait for all pids
    for pid in ${pids[*]}; do
        wait $pid
    done
}

runAllHyperHeuristics(){
    params="$@"
    # Create directory for output of processes
    mkdir -p results/log 2> /dev/null

    #All hyper-heuristics names
    hhIDs=(
        "AdapHH LeanGIHH PHUNTER EPH ISEA HaeaHH"
        "GISS ACO_HH SelfSearch LaroseML SA_ILS HsiaoCHeSCHH"
        "GenHive DynILS_b DynILS HAHA NAHH Ant_Q"
        "XCJ KSATS_HH AVEG_Nep MCHH_S Clean Clean02"
    )
    IFS=""
    for hhBatch in ${hhIDs[*]} 
    do
        runHyperHeuristics "$hhBatch" "$params"
        # echo "$hhBatch"
    done
}

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
        runAllHyperHeuristics $@ &
        exit
    fi
    # No id provided, use the time in millis
    id=`date +%s%3N`
    # Store parameters this script has been run with
    mkdir ./results/${id}
    echo $@ > ./results/${id}/_run-all-params.log
    ### Run ###
    runAllHyperHeuristics "$@ --id $id" &
    echo "Competition id: $id"
    echo "Process id: $!"
    exit
fi

./scripts/run.sh
echo "'-h' can be ommited"
