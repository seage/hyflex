package QAP;

import hfu.BasicSolution;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public class Swap extends PerturbativeModifier<SolutionQAP, InfoQAP, SolutionQAP.SwapNH> {
  public SolutionQAP.SwapNH getNeightbourhood(SolutionQAP c) {
    return new SolutionQAP.SwapNH((InfoQAP)this.instance);
  }
  
  public SolutionQAP apply(SolutionQAP c, int... param) {
    c.swap(param[0], param[1]);
    return c;
  }
  
  public int interpretIOM(double iom, SolutionQAP c) {
    return Math.max((int)Math.ceil(iom * 5.0D), 1);
  }
  
  public int interpretDOS(double dos, SolutionQAP c) {
    int threshold = 1000000000;
    int n = ((InfoQAP)this.instance).getN();
    int swaps = n * (n - 1) / 2;
    int k = 1;
    while (Math.pow(swaps, k) < threshold)
      k++; 
    k--;
    return Math.max(1, k);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\QAP\Swap.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */