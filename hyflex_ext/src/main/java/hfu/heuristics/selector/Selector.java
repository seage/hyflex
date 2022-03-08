package hfu.heuristics.selector;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.ParameterUsage;
import hfu.Parameters;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public abstract class Selector<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P>> implements ParameterUsage {
  protected Parameters params;
  
  public void init(P instance, Parameters params) {
    this.params = params;
    init(instance);
  }
  
  abstract void init(P paramP);
  
  public abstract Proposal<T, P> select(T paramT, Modifier<T, P, N> paramModifier, int paramInt);
  
  public T select(T c, Modifier<T, P, N> modifier) {
    return (select(c, modifier, 2147483647)).c_proposed;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\selector\Selector.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */