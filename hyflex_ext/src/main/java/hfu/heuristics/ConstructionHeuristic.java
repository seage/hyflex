package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

public abstract class ConstructionHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo> extends Heuristic<P> {
  protected P instance;
  
  public abstract T apply();
  
  public void init(P instance) {
    this.instance = instance;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\ConstructionHeuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */