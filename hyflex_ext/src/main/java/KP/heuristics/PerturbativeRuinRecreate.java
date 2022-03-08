package KP.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.ModifierFullLocalSearchHeuristic;
import hfu.heuristics.ModifierMutationHeuristic;
import hfu.heuristics.RuinRecreateHeuristic;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.IterableNH;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Selector;

public class PerturbativeRuinRecreate<T extends BasicSolution<P>, P extends BenchmarkInfo, CN extends NeighbourHood<P> & IterableNH, DN extends NeighbourHood<P>> extends RuinRecreateHeuristic<T, P> {
  ModifierMutationHeuristic<T, P, DN> dh;
  
  ModifierFullLocalSearchHeuristic<T, P, CN> ch;
  
  public PerturbativeRuinRecreate(Selector<T, P, CN> csel, PerturbativeModifier<T, P, CN> cmodifier, Selector<T, P, DN> dsel, PerturbativeModifier<T, P, DN> dmodifier) {
    this.dh = new ModifierMutationHeuristic(dsel, dmodifier);
    this.ch = new ModifierFullLocalSearchHeuristic(csel, cmodifier);
  }
  
  public void init(P instance) {
    this.dh.init((BenchmarkInfo)instance, this.params);
    this.ch.init((BenchmarkInfo)instance, this.params);
  }
  
  public T apply(T c) {
    BasicSolution basicSolution = this.dh.apply((BasicSolution)c);
    basicSolution = this.ch.apply(basicSolution);
    return (T)basicSolution;
  }
  
  public boolean usesDepthOfSearch() {
    return this.ch.usesDepthOfSearch();
  }
  
  public boolean usesIntensityOfMutation() {
    return true;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\heuristics\PerturbativeRuinRecreate.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */