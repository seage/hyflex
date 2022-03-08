package hfu.heuristics.modifiers;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public abstract class ConstructiveModifier<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P>> extends Modifier<T, P, N> {
  public boolean isApplicable(T c) {
    return c.isPartial();
  }
  
  public int interpretIOM(double iom, T c) {
    return 1;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\modifiers\ConstructiveModifier.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */