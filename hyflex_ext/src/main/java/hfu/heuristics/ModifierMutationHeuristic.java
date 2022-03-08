package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Proposal;
import hfu.heuristics.selector.Selector;

public class ModifierMutationHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P>> extends MutationHeuristic<T, P> {
  PerturbativeModifier<T, P, N> modifier;
  
  Selector<T, P, N> sel;
  
  public ModifierMutationHeuristic(Selector<T, P, N> sel, PerturbativeModifier<T, P, N> modifier) {
    this.modifier = modifier;
    this.sel = sel;
  }
  
  public void init(P instance) {
    this.sel.init((BenchmarkInfo)instance, this.params);
    this.modifier.init((BenchmarkInfo)instance);
  }
  
  public T apply(T c) {
    BasicSolution basicSolution;
    int iom = this.modifier.interpretIOM(this.params.getIOM(this), (BasicSolution)c);
    while (iom > 0) {
      Proposal<T, P> proposal = this.sel.select((BasicSolution)c, (Modifier)this.modifier, iom);
      iom -= proposal.nModifications;
      if (proposal.c_proposed != null) {
        basicSolution = proposal.c_proposed;
        continue;
      } 
      break;
    } 
    return (T)basicSolution;
  }
  
  public boolean usesIntensityOfMutation() {
    return true;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\ModifierMutationHeuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */