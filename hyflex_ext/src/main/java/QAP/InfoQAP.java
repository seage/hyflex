package QAP;

import hfu.BenchmarkInfo;
import java.util.ArrayList;
import java.util.Collections;

public class InfoQAP implements BenchmarkInfo {
  private int n;
  
  private int[][] distances;
  
  private int[][] flows;
  
  private int[] sum_flows;
  
  private int[] sum_dists;
  
  private int[] f_ordered;
  
  private int[] l_ordered;
  
  public InfoQAP(int[][] distances, int[][] flows) {
    this.n = distances.length;
    this.distances = distances;
    this.flows = flows;
    this.sum_flows = new int[this.n];
    for (int f = 0; f < this.n; f++) {
      for (int fo = 0; fo < this.n; fo++)
        this.sum_flows[f] = this.sum_flows[f] + flows[f][fo] + flows[fo][f]; 
    } 
    this.sum_dists = new int[this.n];
    for (int l = 0; l < this.n; l++) {
      for (int lo = 0; lo < this.n; lo++)
        this.sum_dists[l] = this.sum_dists[l] + distances[l][lo] + distances[lo][l]; 
    } 
    ArrayList<KeyValuePair> pairs = new ArrayList<>(this.n);
    int i;
    for (i = 0; i < this.n; i++)
      pairs.add(new KeyValuePair(i, this.sum_flows[i])); 
    Collections.sort(pairs);
    this.f_ordered = new int[this.n];
    for (i = 0; i < this.n; i++)
      this.f_ordered[i] = ((KeyValuePair)pairs.get(i)).k; 
    pairs.clear();
    for (i = 0; i < this.n; i++)
      pairs.add(new KeyValuePair(i, this.sum_dists[i])); 
    Collections.sort(pairs);
    this.l_ordered = new int[this.n];
    for (i = 0; i < this.n; i++)
      this.l_ordered[i] = ((KeyValuePair)pairs.get(i)).k; 
  }
  
  public int getN() {
    return this.n;
  }
  
  public int getDistance(int li, int lj) {
    return this.distances[li][lj];
  }
  
  public int getFlow(int fi, int fj) {
    return this.flows[fi][fj];
  }
  
  public int getSumOfFlow(int f) {
    return this.sum_flows[f];
  }
  
  public int getSumOfDist(int l) {
    return this.sum_dists[l];
  }
  
  public int[] getWeightedFacilities() {
    return this.f_ordered;
  }
  
  public int[] getWeightedLocations() {
    return this.l_ordered;
  }
  
  class KeyValuePair implements Comparable<KeyValuePair> {
    int k;
    
    double v;
    
    KeyValuePair(int k, double v) {
      this.k = k;
      this.v = v;
    }
    
    public int compareTo(KeyValuePair o) {
      if (this.v < o.v)
        return 1; 
      if (this.v > o.v)
        return -1; 
      return 0;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\QAP\InfoQAP.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */