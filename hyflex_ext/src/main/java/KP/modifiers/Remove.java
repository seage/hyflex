package KP.modifiers;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.BasicSolution;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public class Remove extends PerturbativeModifier<SolutionKP, InfoKP, SolutionKP.KnapSackNH> {
  public SolutionKP.KnapSackNH getNeightbourhood(SolutionKP c) {
    return new SolutionKP.KnapSackNH((InfoKP)this.instance, c);
  }
  
  public SolutionKP apply(SolutionKP c, int... param) {
    c.remove(param[0]);
    return c;
  }
  
  public int interpretIOM(double iom, SolutionKP c) {
    return (int)Math.max(Math.ceil(iom * c.getPacked()), 1.0D);
  }
  
  public int interpretDOS(double dos, SolutionKP c) {
    return 1;
  }
  
  public boolean isApplicable(SolutionKP c) {
    return (super.isApplicable((BasicSolution)c) && c.getPacked() > 0);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\modifiers\Remove.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */