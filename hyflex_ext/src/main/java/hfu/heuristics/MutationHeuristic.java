package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

public abstract class MutationHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo> extends PerturbationHeuristic<T, P> {
  public boolean usesDepthOfSearch() {
    return false;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\MutationHeuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */