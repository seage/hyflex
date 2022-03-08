package KP.heuristics;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.CrossoverHeuristic;
import hfu.heuristics.ModifierFullLocalSearchHeuristic;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Selector;

public class UnionCrossover extends CrossoverHeuristic<SolutionKP, InfoKP> {
  InfoKP instance;
  
  Selector<SolutionKP, InfoKP, SolutionKP.UnionNH> sel;
  
  public UnionCrossover(Selector<SolutionKP, InfoKP, SolutionKP.UnionNH> selectFirst) {
    this.sel = selectFirst;
  }
  
  public void init(InfoKP instance) {
    this.instance = instance;
  }
  
  public boolean usesDepthOfSearch() {
    return this.sel.usesDepthOfSearch();
  }
  
  public boolean usesIntensityOfMutation() {
    return this.sel.usesIntensityOfMutation();
  }
  
  public SolutionKP apply(SolutionKP c1, SolutionKP c2) {
    InsertUnion modifier = new InsertUnion(c1, c2);
    ModifierFullLocalSearchHeuristic<SolutionKP, InfoKP, SolutionKP.UnionNH> ls = 
      new ModifierFullLocalSearchHeuristic(this.sel, modifier);
    ls.init((BenchmarkInfo)this.instance, this.params);
    SolutionKP c = new SolutionKP(this.instance);
    c = (SolutionKP)ls.apply((BasicSolution)c);
    return c;
  }
  
  public class InsertUnion extends PerturbativeModifier<SolutionKP, InfoKP, SolutionKP.UnionNH> {
    SolutionKP c1;
    
    SolutionKP c2;
    
    InsertUnion(SolutionKP c1, SolutionKP c2) {
      this.c1 = c1;
      this.c2 = c2;
    }
    
    public SolutionKP.UnionNH getNeightbourhood(SolutionKP c) {
      return new SolutionKP.UnionNH((InfoKP)this.instance, c, this.c1, this.c2);
    }
    
    public SolutionKP apply(SolutionKP c, int... param) {
      c.insert(param[0]);
      return c;
    }
    
    public int interpretDOS(double iom, SolutionKP c) {
      return 1;
    }
    
    public int interpretIOM(double iom, SolutionKP c) {
      return 0;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\heuristics\UnionCrossover.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */