EPH-chesc
=========

The Evolutionary-Programming Hyper-heuristic (EPH) is a hyper-heuristic that has been submitted to the [CHeSC challenge](http://www.asap.cs.nott.ac.uk/external/chesc2011/index.html) held in 2011. EPH obtained the [5th place](http://www.asap.cs.nott.ac.uk/external/chesc2011/results.html).

EPH code
--------

For more information on the EPH method you can read the following article:

D. Meignan "An Evolutionary Programming Hyper-heuristic with Co-evolution for CHeSC'11"
In OR53 Annual Conference, CHeSC'11 Cross-domain Heuristic Search Challenge, Nottingham, United Kingdom, January 16-20, 2011.
[HTML](http://www.lalea.fr/?page_id=216), [PDF](http://www.lalea.fr/papers/Meignan2011_OR53_EPH_Hyperheuristic.pdf)

A simplified version of EPH with a graphical-user interface is available at [EPH-Demo](http://www.lalea.fr/?page_id=43).

This code is provided without warranties (see the License below). Please note that this version of EPH has been developed for the CHeSC challenge held in 2011. The code of the EPH method in the file `EPH.java` is the one that has been submitted to the CHeSC'2011 challenge. Hence, it is not particularly well structured and it was not intended to be published (but after several requests it is).

Running EPH on the CheSC benchmark
----------------------------------

You can run EPH on the CHeSC benchmark from the command line with the `EphCommand` class. Arguments are:
* `-t` The time limit in seconds.
* `-r` The number of run of EPH to perform for each problem instance.
* `-o` The output file in which results are recorded.
* `-dp` To disable the progress output in the console.
* `-s` The seed value used for random number generator.
* `-p` The list of problem instances to be solved by EPH. The syntax is `PROBLEM.INSTANCE [PROBLEM.INSTANCE ...]`. Valid problem names are: `SAT`, `BinPacking`, `PersonnelScheduling`, `FlowShop`, `TSP`, `VRP`. Instances are numbers between 0 and 11 for the first four problem domains, and between 0 and 4 for TSP and VRP. Example `-p SAT.0 SAT.2` runs EPH on instance 0 and 2 of the SAT problem.

Examples for running EPH on Bin-packing instances 0 and 1 and SAT instances 0 and 1, with a time limit of 600 seconds, 10 runs, and the results recorded in results.txt:

    java fr.lalea.eph.EphCommand -t 600 -r 10 -o results.txt -p BinPacking.0 BinPacking.1 SAT.0 SAT.1
    
With reference to libraries (on Windows):

    java -cp "./bin;./lib/hyflex_2011/hyflex_2013_03_15.jar;./lib/jcommander/jcommander_2014_11_14.jar" fr.lalea.eph.EphCommand -t 600 -r 10 -o results.txt -p BinPacking.0 BinPacking.1 SAT.0 SAT.1
    
With reference to libraries (Unix syntax):

    java -cp "./bin:./lib/hyflex_2011/hyflex_2013_03_15.jar:./lib/jcommander/jcommander_2014_11_14.jar" fr.lalea.eph.EphCommand -t 600 -r 10 -o results.txt -p BinPacking.0 BinPacking.1 SAT.0 SAT.1

License
=======
    Copyright 2011-2014 David Meignan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
