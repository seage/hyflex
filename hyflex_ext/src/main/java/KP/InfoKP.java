package KP;

import hfu.BenchmarkInfo;

public class InfoKP implements BenchmarkInfo {
  int capacity;
  
  int[] profits;
  
  int[] weights;
  
  long total_profit = -1L;
  
  public InfoKP(int capacity, int[] profits, int[] weights) {
    this.capacity = capacity;
    this.profits = profits;
    this.weights = weights;
  }
  
  public int getWeight(int i) {
    return this.weights[i];
  }
  
  public int getProfit(int i) {
    return this.profits[i];
  }
  
  public int getPPW(int i) {
    return this.profits[i] / this.weights[i];
  }
  
  public int getNitems() {
    return this.profits.length;
  }
  
  public long getTotalProfit() {
    if (this.total_profit == -1L) {
      this.total_profit = 0L;
      for (int i = 0; i < this.profits.length; i++)
        this.total_profit += this.profits[i]; 
    } 
    return this.total_profit;
  }
  
  public int getCapacity() {
    return this.capacity;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\InfoKP.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */