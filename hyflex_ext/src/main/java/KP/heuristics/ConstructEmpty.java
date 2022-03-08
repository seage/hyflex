package KP.heuristics;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.BasicSolution;
import hfu.heuristics.ConstructionHeuristic;

public class ConstructEmpty extends ConstructionHeuristic<SolutionKP, InfoKP> {
  public SolutionKP apply() {
    return new SolutionKP((InfoKP)this.instance);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\heuristics\ConstructEmpty.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */