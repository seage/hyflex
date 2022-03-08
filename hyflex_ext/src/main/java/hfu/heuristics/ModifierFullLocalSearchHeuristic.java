package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.IterableNH;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Selector;

public class ModifierFullLocalSearchHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P> & IterableNH> extends LocalSearchHeuristic<T, P> {
  PerturbativeModifier<T, P, N> modifier;
  
  Selector<T, P, N> sel;
  
  boolean limited_depth;
  
  public ModifierFullLocalSearchHeuristic(Selector<T, P, N> sel, PerturbativeModifier<T, P, N> modifier) {
    this.modifier = modifier;
    this.sel = sel;
    this.limited_depth = false;
  }
  
  public ModifierFullLocalSearchHeuristic(Selector<T, P, N> sel, PerturbativeModifier<T, P, N> modifier, boolean limited_depth) {
    this.modifier = modifier;
    this.sel = sel;
    this.limited_depth = limited_depth;
  }
  
  public void init(P instance) {
    this.modifier.init((BenchmarkInfo)instance);
    this.sel.init((BenchmarkInfo)instance, this.params);
  }
  
  public boolean usesDepthOfSearch() {
    return !(!this.limited_depth && !this.sel.usesDepthOfSearch());
  }
  
  public T improve(T c) {
    int max_depth = Integer.MAX_VALUE;
    if (this.limited_depth) {
      double dos = this.params.getDOS(this);
      max_depth = Math.max((int)Math.ceil(100.0D * dos), 1);
    } 
    BasicSolution basicSolution = c.deepCopy();
    for (int i = 0; i < max_depth; i++) {
      BasicSolution basicSolution1 = this.sel.select((BasicSolution)c, (Modifier)this.modifier);
      if (basicSolution1 == null || basicSolution1.getFunctionValue() >= basicSolution.getFunctionValue())
        break; 
      basicSolution = basicSolution1.deepCopy();
    } 
    return (T)basicSolution;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\ModifierFullLocalSearchHeuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */