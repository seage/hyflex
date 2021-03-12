# HyFlex
HyFlex (Hyper-heuristics Flexible framework) is a Java object oriented framework for the implementation and comparison of different iterative general-purpose heuristic search algorithms (also called hyper-heuristics).

See the HyFlex web site for details: http://www.asap.cs.nott.ac.uk/external/chesc2011 

The goal of this project is to collect hyper-heurisics from CHeSC 2011 (Cross-domain Heuristic Search Challenge) and enable  to reproduce the results of the challenge.

Hyper-heuristic implementers might find this environment helpful for comparing own results with other approaches.

## Build and run the project
```
./scripts/build.sh
```

Run the script and follow the help
```
./scripts/run.sh
```

Example run
```
./scripts/run.sh competition-run -h ISEA -t 10 -n 10
```

This runs the ISEA hyper-heuristic with timeout 10s and 10 trials
