package hfu.heuristics.modifiers;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public abstract class Modifier<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P>> {
  protected P instance;
  
  public void init(P instance) {
    this.instance = instance;
  }
  
  public abstract boolean isApplicable(T paramT);
  
  public abstract N getNeightbourhood(T paramT);
  
  public abstract T apply(T paramT, int... paramVarArgs);
  
  public abstract int interpretIOM(double paramDouble, T paramT);
  
  public int interpretDOS(double dos, T c) {
    return (int)Math.ceil(5.0D * dos);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\modifiers\Modifier.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */