package KP.evalfs;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.selector.eval.EvaluationFunction;

public class MinWeight implements EvaluationFunction<SolutionKP, InfoKP> {
  public void init(InfoKP instance) {}
  
  public double evaluate(SolutionKP c) {
    return c.getPackedWeight();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\evalfs\MinWeight.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */