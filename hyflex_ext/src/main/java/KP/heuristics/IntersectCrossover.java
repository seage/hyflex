package KP.heuristics;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.CrossoverHeuristic;

public class IntersectCrossover extends CrossoverHeuristic<SolutionKP, InfoKP> {
  public void init(InfoKP instance) {}
  
  public boolean usesDepthOfSearch() {
    return false;
  }
  
  public boolean usesIntensityOfMutation() {
    return false;
  }
  
  public SolutionKP apply(SolutionKP c1, SolutionKP c2) {
    SolutionKP c_res = (SolutionKP)c1.deepCopy();
    c_res.intersect(c2);
    return c_res;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\heuristics\IntersectCrossover.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */