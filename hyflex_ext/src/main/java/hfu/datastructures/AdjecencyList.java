package hfu.datastructures;

import java.util.ArrayList;

public class AdjecencyList implements Graph {
  ArrayList<ArrayList<Neighbor>> G;
  
  int nedges;
  
  public AdjecencyList(int n_vertices) {
    this.G = new ArrayList<>(n_vertices);
    for (int i = 0; i < n_vertices; i++)
      this.G.add(new ArrayList<>(1)); 
    this.nedges = 0;
  }
  
  public void addEdge(int v1, int v2, int w) {
    ((ArrayList<Neighbor>)this.G.get(v1 - 1)).add(new Neighbor(v2 - 1, w));
    ((ArrayList<Neighbor>)this.G.get(v2 - 1)).add(new Neighbor(v1 - 1, w));
    this.nedges++;
  }
  
  public int getNedges() {
    return this.nedges;
  }
  
  public int getNvertices() {
    return this.G.size();
  }
  
  public ArrayList<Neighbor> getNeighbors(int v) {
    return this.G.get(v);
  }
  
  public class Neighbor {
    int w;
    
    int id;
    
    Neighbor(int id, int w) {
      this.w = w;
      this.id = id;
    }
    
    public int getW() {
      return this.w;
    }
    
    public int getID() {
      return this.id;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\datastructures\AdjecencyList.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */