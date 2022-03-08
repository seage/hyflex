package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.IterableNH;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Selector;

public class ModifierLocalSearchHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P> & IterableNH> extends LocalSearchHeuristic<T, P> {
  PerturbativeModifier<T, P, N> modifier;
  
  Selector<T, P, N> sel;
  
  public ModifierLocalSearchHeuristic(Selector<T, P, N> sel, PerturbativeModifier<T, P, N> modifier) {
    this.modifier = modifier;
    this.sel = sel;
  }
  
  public void init(P instance) {
    this.modifier.init((BenchmarkInfo)instance);
    this.sel.init((BenchmarkInfo)instance, this.params);
  }
  
  public boolean usesDepthOfSearch() {
    return this.sel.usesDepthOfSearch();
  }
  
  public T improve(T c) {
    return (T)this.sel.select((BasicSolution)c, (Modifier)this.modifier);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\ModifierLocalSearchHeuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */