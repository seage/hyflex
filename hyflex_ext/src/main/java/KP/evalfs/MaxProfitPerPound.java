package KP.evalfs;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.selector.eval.EvaluationFunction;

public class MaxProfitPerPound implements EvaluationFunction<SolutionKP, InfoKP> {
  public void init(InfoKP instance) {}
  
  public double evaluate(SolutionKP c) {
    int weight = c.getPackedWeight();
    double profit = c.getPackedProfit();
    return (weight == 0) ? Double.MAX_VALUE : (-profit / weight);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\evalfs\MaxProfitPerPound.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */