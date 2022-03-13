# hyflext
Resources (data + logic) used during the experiments performed in scope of the research article: <br />
"*A Benchmark Set Extension and Comparative Study for the HyFlex Framework*" (Adriaensen et al, 2015)

Feel free to use any of these in your own research. 
If you do use any of these resources for your research, we kindly ask you to acknowledge this, by citing the following article:

Adriaensen, Steven, Gabriela Ochoa, and Ann Nowé. "*A Benchmark Set Extension and Comparative Study for the HyFlex Framework.*" Evolutionary Computation (CEC), 2015 IEEE Congress on. IEEE, 2015.

@inproceedings{adriaensen2015benchmark, <br /> 
  title={A Benchmark Set Extension and Comparative Study for the HyFlex Framework}, <br /> 
  author={Adriaensen, Steven and Ochoa, Gabriela and Now{\'e}, Ann}, <br /> 
  booktitle={Evolutionary Computation (CEC), 2015 IEEE Congress on}, <br /> 
  year={2015}, <br /> 
  organization={IEEE} <br /> 
}

**Content**:
- domains: Provides the resources (jars + descriptions) for all 3 domains extending the HyFlex benchmark set:
  - kp: 0-1 Knapsack Problem
  - qap: Quadratic Assignment Problem
  - mac: Max Cut Problem
  
- naive: Provides the 'naive' hyperheuristics that were used as a baseline for comparison.
  The source code of the other hyperheuristics used can be found:
  - (NR-)FS-ILS: [https://github.com/Steven-Adriaensen/FS-ILS](https://github.com/Steven-Adriaensen/FS-ILS)
  - Adap-HH/GIHH:[http://allserv.kahosl.be/~mustafa.misir/gihh.html](http://allserv.kahosl.be/~mustafa.misir/gihh.html)
  - EPH: [http://www.lalea.fr/public/index.php?cmd=smarty&id=11_len](http://www.lalea.fr/public/index.php?cmd=smarty&id=11_len)

- data: Provides the experimental data used in our comparative study
  - data.csv: The best solution quality obtained for 31, 10 minutes runs, on each of the 98 instances, for all 6 methods (total of 18228 runs).
  - medians.csv: The median (best) solution quality obtained by each method, per instance, after 10 minutes.