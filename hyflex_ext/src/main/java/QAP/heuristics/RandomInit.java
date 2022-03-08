package QAP.heuristics;

import QAP.InfoQAP;
import QAP.SolutionQAP;
import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.ConstructionHeuristic;

public class RandomInit extends ConstructionHeuristic<SolutionQAP, InfoQAP> {
  public void init(InfoQAP instance) {
    super.init((BenchmarkInfo)instance);
  }
  
  public SolutionQAP apply() {
    SolutionQAP c = new SolutionQAP((InfoQAP)this.instance);
    c.randomInit();
    return c;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\QAP\heuristics\RandomInit.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */