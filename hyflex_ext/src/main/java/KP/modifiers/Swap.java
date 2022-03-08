package KP.modifiers;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.BasicSolution;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public class Swap extends PerturbativeModifier<SolutionKP, InfoKP, SolutionKP.SwapNH> {
  public SolutionKP.SwapNH getNeightbourhood(SolutionKP c) {
    return new SolutionKP.SwapNH((InfoKP)this.instance, c);
  }
  
  public SolutionKP apply(SolutionKP c, int... param) {
    c.swap(param[0], param[1]);
    return c;
  }
  
  public int interpretIOM(double iom, SolutionKP c) {
    return (int)Math.ceil(5.0D * iom);
  }
  
  public int interpretDOS(double dos, SolutionKP c) {
    return 1;
  }
  
  public boolean isApplicable(SolutionKP c) {
    return (super.isApplicable((BasicSolution)c) && c.getPacked() > 0);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\modifiers\Swap.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */