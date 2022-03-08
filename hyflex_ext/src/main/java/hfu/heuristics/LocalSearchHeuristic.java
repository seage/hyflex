package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

public abstract class LocalSearchHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo> extends PerturbationHeuristic<T, P> {
  public abstract T improve(T paramT);
  
  public boolean usesIntensityOfMutation() {
    return false;
  }
  
  public T apply(T c) {
    T c_proposed = improve(c);
    return (c_proposed != null && c.getFunctionValue() > c_proposed.getFunctionValue()) ? c_proposed : c;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\LocalSearchHeuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */