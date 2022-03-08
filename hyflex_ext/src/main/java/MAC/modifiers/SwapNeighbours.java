package MAC.modifiers;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import MAC.modifiers.nhs.SwapNeighboursNH;
import hfu.BasicSolution;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public class SwapNeighbours extends PerturbativeModifier<SolutionMAC, InfoMAC, SwapNeighboursNH> {
  public SwapNeighboursNH getNeightbourhood(SolutionMAC c) {
    return new SwapNeighboursNH((InfoMAC)this.instance);
  }
  
  public SolutionMAC apply(SolutionMAC c, int... param) {
    c.swapNeighbours(param[0], param[1]);
    return c;
  }
  
  public int interpretIOM(double iom, SolutionMAC c) {
    return Math.max((int)Math.ceil(iom * 5.0D), 1);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\modifiers\SwapNeighbours.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */