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

### Example run for single and multiple hyper-heuristics competition

This runs the ISEA hyper-heuristic with timeout 10s and 10 trials

```
./scripts/run.sh competition -h ISEA -t 10 -n 10
```


This runs the ISEA hyper-heuristic on just TSP and SAT problems with timeout 10s and 10 trials

```
./scripts/run.sh competition-run -h ISEA -p TSP SAT -t 10 -n 10
```

This runs the ISEA and LeanGIHH hyper-heuristics with timeout 10s and 10 trials each

```
./scripts/run.sh competition -h ISEA LeanGIHH -t 10 -n 10
```

### Example run of all available hyper-heuristics competition

This runs all of the hyper-heuristics parallel with timeout 10s and 10 trials each

```
./scripts/run-all.sh competition -t 10 -n 10
```

To see all available hyper-heuristics run following command

```
./scripts/run.sh --help
```

### Example run for evaluation

Run the competition with the id e.g. `competition1`

```
./scripts/run.sh competition -h ISEA LeanGIHH -t 10 -n 10 --id competition1
```

To run all the hyper-heuristics with the id e.g. `competition1`

```
./scripts/run-all.sh competition -t 10 -n 10 --id competition1
```


Evaluate the competition with id `competition1`

```
./scripts/run.sh evaluation --id competition1
```

The final result will be stores as xml file in the competiton1 folder
