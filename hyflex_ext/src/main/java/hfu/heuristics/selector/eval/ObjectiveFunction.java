package hfu.heuristics.selector.eval;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

public class ObjectiveFunction<T extends BasicSolution<P>, P extends BenchmarkInfo> implements EvaluationFunction<T, P> {
  public void init(P instance) {}
  
  public double evaluate(T c) {
    return c.getFunctionValue();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\selector\eval\ObjectiveFunction.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */