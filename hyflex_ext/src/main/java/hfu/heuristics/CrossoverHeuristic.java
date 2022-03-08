package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

public abstract class CrossoverHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo> extends PerturbationHeuristic<T, P> {
  public abstract T apply(T paramT1, T paramT2);
  
  public T apply(T c) {
    return apply(c, (T)c.deepCopy());
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\CrossoverHeuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */