package QAP.heuristics;

import QAP.InfoQAP;
import QAP.SolutionQAP;
import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.ParameterUsage;
import hfu.heuristics.RuinRecreateHeuristic;

public class ReAssignRandom extends RuinRecreateHeuristic<SolutionQAP, InfoQAP> {
  public boolean usesDepthOfSearch() {
    return false;
  }
  
  public boolean usesIntensityOfMutation() {
    return true;
  }
  
  public SolutionQAP apply(SolutionQAP c) {
    c = (SolutionQAP)c.deepCopy();
    c.reAssignRandomFraction(this.params.getIOM((ParameterUsage)this) / 2.0D);
    return c;
  }
  
  public void init(InfoQAP instance) {}
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\QAP\heuristics\ReAssignRandom.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */