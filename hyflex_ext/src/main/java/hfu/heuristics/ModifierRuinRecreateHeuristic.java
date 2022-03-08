package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.ConstructiveModifier;
import hfu.heuristics.modifiers.DestructiveModifier;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Proposal;
import hfu.heuristics.selector.Selector;

public class ModifierRuinRecreateHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo, CN extends NeighbourHood<P>, DN extends NeighbourHood<P>> extends RuinRecreateHeuristic<T, P> {
  ConstructiveModifier<T, P, CN> cmodifier;
  
  DestructiveModifier<T, P, DN> dmodifier;
  
  Selector<T, P, CN> csel;
  
  Selector<T, P, DN> dsel;
  
  public ModifierRuinRecreateHeuristic(Selector<T, P, CN> csel, ConstructiveModifier<T, P, CN> cmodifier, Selector<T, P, DN> dsel, DestructiveModifier<T, P, DN> dmodifier) {
    this.cmodifier = cmodifier;
    this.dmodifier = dmodifier;
    this.csel = csel;
    this.dsel = dsel;
  }
  
  public void init(P instance) {
    this.cmodifier.init((BenchmarkInfo)instance);
    this.dmodifier.init((BenchmarkInfo)instance);
    this.csel.init((BenchmarkInfo)instance, this.params);
    this.dsel.init((BenchmarkInfo)instance, this.params);
  }
  
  public T apply(T c) {
    BasicSolution basicSolution;
    int iom = this.dmodifier.interpretIOM(this.params.getIOM(this), (BasicSolution)c);
    while (iom > 0) {
      Proposal<T, P> proposal = this.dsel.select((BasicSolution)c, (Modifier)this.dmodifier, iom);
      iom -= proposal.nModifications;
      basicSolution = proposal.c_proposed;
    } 
    while (basicSolution.isPartial())
      basicSolution = this.csel.select(basicSolution, (Modifier)this.cmodifier); 
    return (T)basicSolution;
  }
  
  public boolean usesDepthOfSearch() {
    return !(!this.csel.usesDepthOfSearch() && !this.dsel.usesDepthOfSearch());
  }
  
  public boolean usesIntensityOfMutation() {
    return true;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\ModifierRuinRecreateHeuristic.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */