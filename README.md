# HyFlex
HyFlex (Hyper-heuristics Flexible framework) is a Java object oriented framework for the implementation and comparison of different iterative general-purpose heuristic search algorithms (also called hyper-heuristics).

See the HyFlex web site for details: http://www.asap.cs.nott.ac.uk/external/chesc2011 - [web archive](https://web.archive.org/web/20210518021139/http://www.asap.cs.nott.ac.uk/external/chesc2011/)

The goal of this project is to collect hyper-heurisics from CHeSC 2011 (Cross-domain Heuristic Search Challenge) and enable  to reproduce the results of the challenge.

Hyper-heuristic implementers might find this environment helpful for comparing own results with other approaches.

## Unit metric
We have developed a metric that assigns to the hyper-heuristic's results quality a number from the unit interval `<0,1>` (that's why we call it the unit metric).

The lower bound value of the interval corresponds to the quality of an easily obtainable solution (e.g. a greedy solution) and the upper bound is the quality of the optimal solution. 

The more the hyper-heuristic gets the evaluation closer to `1.0` the better results it provides closer to the optimal solutions.

On the other hand, closer to the `0.0` value the hyper-heuristic is not much better than a greedy solution generator.

The overall unit metric value is an aggregation of partial evaluations.
First, solutions for each problem instance (provided by the hyper-heuristic) are evaluated and mapped onto the unit interval. 
Second, the problem evaluation is an aggregation of the problem instances evaluations obtained in the previous step. The weighted mean is calculated with weights corresponding to instance sizes.
Finally, the overall value is obtained as a simple mean of the problem evaluations. See the formula below.

![Unit-metric](docs/unit-metric/unit-metric-formula.svg)

## List of available hyper-heuristics
The following is the list of CHeSC 2011 competition hyper-heuristics that are available in this repository. See details in the `hyflex-hyperheuristics` folder for each hyper-heuristic.

The table shows evaluated solutions for each hyper-heuristic (each run with 120s timeout and 5 repeats) using our unit metric.

![Hyper-heuristics](docs/heatmap_120_5.svg)

---
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
./scripts/run.sh competition -h ISEA -p TSP SAT -t 10 -n 10
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

The final result will be stores as xml file in the `results/competiton1` folder


### Create a heat map of competition results
```
./scripts/create-heatmap.sh competition1
```

The heat map will be stored in the `./results/competition1/heatmap.svg` file.

