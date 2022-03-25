# HyFlex
HyFlex (Hyper-heuristics Flexible framework) is a Java object oriented framework for the implementation and comparison of different iterative general-purpose heuristic search algorithms (also called hyper-heuristics).

See the HyFlex web site for details: http://www.asap.cs.nott.ac.uk/external/chesc2011 

The goal of this project is to collect hyper-heurisics from CHeSC 2011 (Cross-domain Heuristic Search Challenge) and enable  to reproduce the results of the challenge.

Hyper-heuristic implementers might find this environment helpful for comparing own results with other approaches.

## Unit metric
![Unit-metric](docs/unit-metric/unit-metric-formula.svg)

## List of available hyper-heuristics

See details for each hyper-heuristic in the `hyflex-hyperheuristics` folder. 

The following table shows evaluated solutions of each hhs from competition run with 120s timeout and 5 repeats.

Every hhs solves 5 of instances of each domain. The results are then evaluated using our new Unit metric.

The equation of Unit metric is quite simple. All the neccessary parts for it are stored in metadata. For each instance of problem there is the optimal solution, greedy solution and few more infos. 

**Instance = unit_metric_instance(instance, optimal, greedy) e [0,1]**

After having evaluated results the overall result of each domain is then calculated using weighted mean. Some instances are larger than other, therefore the size is used as weight.

**Problem = weighted_mean({allUi(instance_i}, instances_sizes)**

And finaly with all evaluated domain the score of the hhs is calculated using simple mean.

**Score = 1/n * sum(ProblemScores)**

![Hyper-heuristics](docs/heatmap_120_5.svg)

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

