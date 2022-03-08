package hfu.heuristics.selector;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.modifiers.nh.SamplableNH;

public class SelectRandom<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P> & SamplableNH> extends Selector<T, P, N> {
  public void init(P instance) {}
  
  public Proposal<T, P> select(T c, Modifier<T, P, N> modifier, int max) {
    Proposal<T, P> proposal = new Proposal<>();
    if (modifier.isApplicable((BasicSolution)c)) {
      proposal.c_proposed = (T)modifier.apply(c.deepCopy(), ((SamplableNH)modifier.getNeightbourhood((BasicSolution)c)).sample());
      proposal.nModifications = 1;
    } 
    return proposal;
  }
  
  public boolean usesDepthOfSearch() {
    return false;
  }
  
  public boolean usesIntensityOfMutation() {
    return false;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\selector\SelectRandom.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */