package hfu.heuristics.modifiers.nh;

import hfu.BenchmarkInfo;

public abstract class NeighbourHood<P extends BenchmarkInfo> {
  protected P instance;
  
  public NeighbourHood(P instance) {
    this.instance = instance;
  }
  
  public abstract int getDimensionality();
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\modifiers\nh\NeighbourHood.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */