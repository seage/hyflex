package hfu.datastructures;

import java.util.ArrayList;

public interface Graph {
  void addEdge(int paramInt1, int paramInt2, int paramInt3);
  
  int getNedges();
  
  int getNvertices();
  
  ArrayList<AdjecencyList.Neighbor> getNeighbors(int paramInt);
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\datastructures\Graph.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */