package MAC.modifiers;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import hfu.BasicSolution;
import hfu.heuristics.modifiers.ConstructiveModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;

public class Insert extends ConstructiveModifier<SolutionMAC, InfoMAC, SolutionMAC.InsertNH> {
  int v;
  
  public Insert() {
    this.v = -1;
  }
  
  public Insert(int v) {
    this.v = v;
  }
  
  public SolutionMAC.InsertNH getNeightbourhood(SolutionMAC c) {
    return (this.v == -1) ? new SolutionMAC.InsertNH((InfoMAC)this.instance, c) : new SolutionMAC.InsertNH((InfoMAC)this.instance, c, this.v);
  }
  
  public SolutionMAC apply(SolutionMAC c, int... param) {
    c.insert(param[0], param[1]);
    return c;
  }
  
  public int interpretDOS(double dos, SolutionMAC c) {
    return (int)Math.ceil(5.0D * dos);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\modifiers\Insert.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */