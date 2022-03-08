package hfu.heuristics;

import hfu.BenchmarkInfo;
import hfu.Parameters;

public abstract class Heuristic<P extends BenchmarkInfo> {
  protected Parameters params;
  
  public abstract void init(P paramP);
  
  public void init(P instance, Parameters params) {
    this.params = params;
    init(instance);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\Heuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */