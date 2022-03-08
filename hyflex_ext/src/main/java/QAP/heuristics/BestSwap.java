package QAP.heuristics;

import QAP.InfoQAP;
import QAP.SolutionQAP;
import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.ParameterUsage;
import hfu.heuristics.MutationHeuristic;
import java.util.HashSet;
import java.util.Set;

public class BestSwap extends MutationHeuristic<SolutionQAP, InfoQAP> {
  public boolean usesDepthOfSearch() {
    return false;
  }
  
  public boolean usesIntensityOfMutation() {
    return true;
  }
  
  public SolutionQAP apply(SolutionQAP c) {
    SolutionQAP c_res = (SolutionQAP)c.deepCopy();
    double iom = this.params.getIOM((ParameterUsage)this);
    int repeated = (int)Math.ceil(1000.0D * iom * iom * iom);
    Set<Integer> tabu = new HashSet<>();
    for (int i = 0; i < repeated; i++) {
      tabu.add(Integer.valueOf(c_res.swapBest(tabu)));
      if (c_res.getFunctionValue() < c.getFunctionValue())
        return c_res; 
    } 
    return c_res;
  }
  
  public void init(InfoQAP instance) {}
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\QAP\heuristics\BestSwap.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */