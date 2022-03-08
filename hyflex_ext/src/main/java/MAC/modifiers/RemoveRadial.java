package MAC.modifiers;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import hfu.BasicSolution;
import hfu.heuristics.modifiers.DestructiveModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public class RemoveRadial extends DestructiveModifier<SolutionMAC, InfoMAC, SolutionMAC.RemoveNH> {
  public SolutionMAC.RemoveNH getNeightbourhood(SolutionMAC c) {
    return new SolutionMAC.RemoveNH((InfoMAC)this.instance, c);
  }
  
  public SolutionMAC apply(SolutionMAC c, int... param) {
    c.removeRadial(param[0]);
    return c;
  }
  
  public int interpretIOM(double iom, SolutionMAC c) {
    return Math.max((int)Math.ceil(iom * 5.0D), 1);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\modifiers\RemoveRadial.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */