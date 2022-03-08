package KP.modifiers;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.BasicSolution;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public class Insert extends PerturbativeModifier<SolutionKP, InfoKP, SolutionKP.UnpackedNH> {
  public SolutionKP.UnpackedNH getNeightbourhood(SolutionKP c) {
    return new SolutionKP.UnpackedNH((InfoKP)this.instance, c);
  }
  
  public SolutionKP apply(SolutionKP c, int... param) {
    c.insert(param[0]);
    return c;
  }
  
  public int interpretIOM(double iom, SolutionKP c) {
    return (int)Math.ceil(5.0D * iom);
  }
  
  public int interpretDOS(double dos, SolutionKP c) {
    return 1;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\modifiers\Insert.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */