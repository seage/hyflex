package MAC;

import hfu.BenchmarkInfo;
import hfu.datastructures.AdjecencyList;
import hfu.datastructures.Graph;

public class InfoMAC implements BenchmarkInfo {
  private AdjecencyList G;
  
  public InfoMAC(AdjecencyList G) {
    this.G = G;
  }
  
  public int getNvertices() {
    return this.G.getNvertices();
  }
  
  public int getNedges() {
    return this.G.getNvertices();
  }
  
  public Graph getGraph() {
    return (Graph)this.G;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\InfoMAC.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */