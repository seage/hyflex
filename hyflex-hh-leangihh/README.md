# Lean-GIHH
This project contains the source code of Lean-GIHH, a hyper-heuristic for the HyFlex framework.

Lean-GIHH is a simpler variant of GIHH (aka Adap-HH), winner of the CHeSC 2011 competition.
It was derived in [1] from the original by means of Accidental Complexity Analysis (ACA), 
a technique for reducing algorithmic complexity, without loss of performance.
The implementation of Lean-GIHH provided in the src directory counts 288 lines of code (as determined using the cloc tool) as opposed to the 2324 lines of code in the original implementation (see links).

Feel free to use Lean-GIHH in your own research.
If you do, we kindly ask you to acknowledge this, by citing the following article:

[1] Steven Adriaensen, and Ann Nowé. "Case Study: An Analysis of Accidental Complexity in a State-of-the-art Hyper-heuristic for HyFlex." 
Evolutionary Computation (CEC), 2016 IEEE Congress on. IEEE, 2016.

@inproceedings{adriaensen2016case, <br />
  title={Case Study: An Analysis of Accidental Complexity in a State-of-the-art Hyper-heuristic for HyFlex}, <br />
  author={Adriaensen, Steven and Now{\'e}, Ann}, <br />
  booktitle={Evolutionary Computation (CEC), 2016 IEEE Congress on}, <br />
  year={2016}, <br />
  organization={IEEE} <br />
}

**Content**:
- src/LeanGIHH.java: Source code of the Lean-GIHH hyper-heuristic
- src/CircularBuffer.java: An auxiliary data-structure used by LeanGIHH.
- src/ExampleRun.java: Usage example.
- lib/chesc.jar: CHeSC HyFlex framework
- LeanGIHH-eclipse.zip: Archive to import as project in Eclipse IDE

See the extensive documentation in these files for more details.

**Links**:
- CHeSC 2011 competition website: [http://www.asap.cs.nott.ac.uk/external/chesc2011/](http://www.asap.cs.nott.ac.uk/external/chesc2011/)
- Original version of GIHH by Mustafa Misir: [https://code.google.com/p/generic-intelligent-hyper-heuristic/](https://code.google.com/p/generic-intelligent-hyper-heuristic/)