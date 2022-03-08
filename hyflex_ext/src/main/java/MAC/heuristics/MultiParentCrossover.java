package MAC.heuristics;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.CrossoverHeuristic;

public class MultiParentCrossover extends CrossoverHeuristic<SolutionMAC, InfoMAC> {
  InfoMAC instance;
  
  public boolean usesDepthOfSearch() {
    return false;
  }
  
  public boolean usesIntensityOfMutation() {
    return false;
  }
  
  public SolutionMAC apply(SolutionMAC c1, SolutionMAC c2) {
    SolutionMAC c_res = (SolutionMAC)c1.deepCopy();
    c_res.mp_crossover(c2);
    return c_res;
  }
  
  public void init(InfoMAC instance) {
    this.instance = instance;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\heuristics\MultiParentCrossover.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */