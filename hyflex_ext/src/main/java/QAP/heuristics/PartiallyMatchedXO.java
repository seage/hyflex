package QAP.heuristics;

import QAP.InfoQAP;
import QAP.SolutionQAP;
import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.CrossoverHeuristic;

public class PartiallyMatchedXO extends CrossoverHeuristic<SolutionQAP, InfoQAP> {
  public boolean usesDepthOfSearch() {
    return false;
  }
  
  public boolean usesIntensityOfMutation() {
    return false;
  }
  
  public SolutionQAP apply(SolutionQAP c1, SolutionQAP c2) {
    SolutionQAP c_res = (SolutionQAP)c1.deepCopy();
    c_res.pmx(c2);
    return c_res;
  }
  
  public void init(InfoQAP instance) {}
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\QAP\heuristics\PartiallyMatchedXO.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */