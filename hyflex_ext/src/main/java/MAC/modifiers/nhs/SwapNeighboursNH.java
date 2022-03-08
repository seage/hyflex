package MAC.modifiers.nhs;

import MAC.InfoMAC;
import hfu.BenchmarkInfo;
import hfu.RNG;
import hfu.datastructures.AdjecencyList;
import hfu.heuristics.modifiers.nh.IterableNH;
import hfu.heuristics.modifiers.nh.IteratorNH;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.modifiers.nh.RangeNH;
import hfu.heuristics.modifiers.nh.SamplableNH;
import java.util.ArrayList;

public class SwapNeighboursNH extends NeighbourHood<InfoMAC> implements IterableNH, SamplableNH {
  RangeNH<InfoMAC> rnh;
  
  public SwapNeighboursNH(InfoMAC instance) {
    super((BenchmarkInfo)instance);
    this.rnh = new RangeNH(0, instance.getNvertices(), (BenchmarkInfo)instance);
  }
  
  public IteratorNH getIterator() {
    return new NeighboursIterator();
  }
  
  public int[] sample() {
    ArrayList<AdjecencyList.Neighbor> neighbours = null;
    int vi = 0;
    while (neighbours == null || neighbours.size() == 0) {
      vi = this.rnh.sample()[0];
      neighbours = ((InfoMAC)this.instance).getGraph().getNeighbors(vi);
    } 
    int vj = ((AdjecencyList.Neighbor)neighbours.get(RNG.get().nextInt(neighbours.size()))).getID();
    return new int[] { vi, vj };
  }
  
  public int getDimensionality() {
    return 2;
  }
  
  class NeighboursIterator extends IteratorNH {
    boolean done = false;
    
    int vj;
    
    int vi;
    
    ArrayList<AdjecencyList.Neighbor> neighbours;
    
    IteratorNH vis = SwapNeighboursNH.this.rnh.getIterator();
    
    NeighboursIterator() {
      while (this.neighbours == null || this.neighbours.size() < 1) {
        if (this.vis.hasNext()) {
          this.vi = ((int[])this.vis.next())[0];
          this.neighbours = ((InfoMAC)SwapNeighboursNH.this.instance).getGraph().getNeighbors(this.vi);
          this.vj = 0;
          continue;
        } 
        this.done = true;
        break;
      } 
    }
    
    public boolean hasNext() {
      return !this.done;
    }
    
    public int[] next() {
      int[] result = { this.vi, this.vj };
      this.vj++;
      if (this.vj == this.neighbours.size()) {
        this.neighbours = null;
        while (this.neighbours == null || this.neighbours.size() < 1) {
          if (this.vis.hasNext()) {
            this.vi = ((int[])this.vis.next())[0];
            this.neighbours = ((InfoMAC)SwapNeighboursNH.this.instance).getGraph().getNeighbors(this.vi);
            this.vj = 0;
            continue;
          } 
          this.done = true;
          break;
        } 
      } 
      return result;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\modifiers\nhs\SwapNeighboursNH.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */