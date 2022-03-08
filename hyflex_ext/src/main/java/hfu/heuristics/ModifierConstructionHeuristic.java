package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.ConstructiveModifier;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Selector;

public abstract class ModifierConstructionHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P>> extends ConstructionHeuristic<T, P> {
  ConstructiveModifier<T, P, N> modifier;
  
  Selector<T, P, N> sel;
  
  public abstract T getEmptySolution();
  
  public ModifierConstructionHeuristic(Selector<T, P, N> sel, ConstructiveModifier<T, P, N> modifier) {
    this.modifier = modifier;
    this.sel = sel;
  }
  
  public void init(P instance) {
    this.instance = instance;
    this.sel.init((BenchmarkInfo)instance, this.params);
    this.modifier.init((BenchmarkInfo)instance);
  }
  
  public T apply() {
    BasicSolution basicSolution;
    T c = getEmptySolution();
    while (c.isPartial())
      basicSolution = this.sel.select((BasicSolution)c, (Modifier)this.modifier); 
    return (T)basicSolution;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\ModifierConstructionHeuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */