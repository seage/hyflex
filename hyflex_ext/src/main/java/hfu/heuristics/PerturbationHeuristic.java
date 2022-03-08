package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.ParameterUsage;

public abstract class PerturbationHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo> extends Heuristic<P> implements ParameterUsage {
  public abstract T apply(T paramT);
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\PerturbationHeuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */