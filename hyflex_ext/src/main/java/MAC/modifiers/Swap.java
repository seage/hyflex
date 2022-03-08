package MAC.modifiers;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import hfu.BasicSolution;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public class Swap extends PerturbativeModifier<SolutionMAC, InfoMAC, SolutionMAC.SwapNH> {
  public SolutionMAC.SwapNH getNeightbourhood(SolutionMAC c) {
    return new SolutionMAC.SwapNH((InfoMAC)this.instance);
  }
  
  public SolutionMAC apply(SolutionMAC c, int... param) {
    c.swap(param[0]);
    return c;
  }
  
  public int interpretIOM(double iom, SolutionMAC c) {
    return Math.max((int)Math.ceil(iom * 10.0D), 1);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\modifiers\Swap.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */