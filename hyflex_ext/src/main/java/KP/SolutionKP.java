package KP;

import hfu.BasicSolution;
import hfu.heuristics.modifiers.nh.CompositeNH;
import hfu.heuristics.modifiers.nh.RangeNH;
import hfu.heuristics.modifiers.nh.filter.Filter;
import java.util.Arrays;

public class SolutionKP extends BasicSolution<InfoKP> {
  boolean[] knapsack;
  
  private int packed;
  
  private int packed_weight;
  
  private int packed_profit;
  
  public SolutionKP(InfoKP instance) {
    super(instance);
    this.knapsack = new boolean[instance.getNitems()];
    this.packed = 0;
    this.packed_weight = 0;
    this.packed_profit = 0;
  }
  
  protected SolutionKP(SolutionKP other) {
    super(other);
    this.knapsack = new boolean[other.knapsack.length];
    System.arraycopy(other.knapsack, 0, this.knapsack, 0, this.knapsack.length);
    this.packed = other.packed;
    this.packed_weight = other.packed_weight;
    this.packed_profit = other.packed_profit;
  }
  
  public boolean isEqualTo(BasicSolution<InfoKP> other) {
    SolutionKP other_ks = (SolutionKP)other;
    if (this.packed == other_ks.packed && this.packed_weight == other_ks.packed_weight)
      return Arrays.equals(this.knapsack, other_ks.knapsack); 
    return false;
  }
  
  public BasicSolution<InfoKP> deepCopy() {
    return new SolutionKP(this);
  }
  
  public String toText() {
    String result = "knapsack: ";
    for (int i = 0; i < this.knapsack.length; i++) {
      if (this.knapsack[i])
        result = String.valueOf(result) + (i + 1) + " "; 
    } 
    return String.valueOf(result) + " (" + getFunctionValue() + ")";
  }
  
  protected double evaluateFunctionValue() {
    return -this.packed_profit;
  }
  
  public boolean isPartial() {
    return false;
  }
  
  public boolean isEmpty() {
    return false;
  }
  
  public boolean insert(int i) {
    if (fits(i)) {
      this.knapsack[i] = true;
      this.packed++;
      this.packed_weight += ((InfoKP)this.instance).getWeight(i);
      this.packed_profit += ((InfoKP)this.instance).getProfit(i);
      setFunctionValue(evaluateFunctionValue());
      return true;
    } 
    return false;
  }
  
  public boolean swap(int i, int j) {
    int w_diff = ((InfoKP)this.instance).getWeight(j) - ((InfoKP)this.instance).getWeight(i);
    if (getRemainingCapacity() >= w_diff) {
      this.knapsack[i] = false;
      this.knapsack[j] = true;
      this.packed_weight += w_diff;
      this.packed_profit += ((InfoKP)this.instance).getProfit(j) - ((InfoKP)this.instance).getProfit(i);
      setFunctionValue(evaluateFunctionValue());
      return true;
    } 
    return false;
  }
  
  public void remove(int i) {
    this.knapsack[i] = false;
    this.packed--;
    this.packed_weight -= ((InfoKP)this.instance).getWeight(i);
    this.packed_profit -= ((InfoKP)this.instance).getProfit(i);
    setFunctionValue(evaluateFunctionValue());
  }
  
  public void intersect(SolutionKP other) {
    boolean[] other_knapsack = other.knapsack;
    for (int i = 0; i < this.knapsack.length; i++) {
      if (this.knapsack[i] && !other_knapsack[i])
        remove(i); 
    } 
  }
  
  public int getRemainingCapacity() {
    return ((InfoKP)this.instance).getCapacity() - this.packed_weight;
  }
  
  public boolean fits(int i) {
    return (((InfoKP)this.instance).getWeight(i) <= getRemainingCapacity());
  }
  
  public int getPacked() {
    return this.packed;
  }
  
  public int getPackedWeight() {
    return this.packed_weight;
  }
  
  public int getPackedProfit() {
    return this.packed_profit;
  }
  
  public static class KnapSackNH extends RangeNH<InfoKP> {
    public KnapSackNH(InfoKP instance, SolutionKP c) {
      super(0, instance.getNitems(), new IsPacked(c), instance);
    }
    
    static class IsPacked implements Filter {
      SolutionKP c;
      
      IsPacked(SolutionKP c) {
        this.c = c;
      }
      
      public boolean include(int[] param) {
        return this.c.knapsack[param[0]];
      }
    }
  }
  
  public static class UnpackedNH extends RangeNH<InfoKP> {
    public UnpackedNH(InfoKP instance, SolutionKP c) {
      super(0, instance.getNitems(), new IsUnPacked(c), instance);
    }
    
    static class IsUnPacked implements Filter {
      SolutionKP c;
      
      IsUnPacked(SolutionKP c) {
        this.c = c;
      }
      
      public boolean include(int[] param) {
        return !this.c.knapsack[param[0]];
      }
    }
  }
  
  public static class UnionNH extends RangeNH<InfoKP> {
    public UnionNH(InfoKP instance, SolutionKP c, SolutionKP c1, SolutionKP c2) {
      super(0, instance.getNitems(), new IsUnPackedUnion(c, c1, c2), instance);
    }
    
    static class IsUnPackedUnion implements Filter {
      SolutionKP c;
      
      SolutionKP c1;
      
      SolutionKP c2;
      
      IsUnPackedUnion(SolutionKP c, SolutionKP c1, SolutionKP c2) {
        this.c = c;
        this.c1 = c1;
        this.c2 = c2;
      }
      
      public boolean include(int[] param) {
        return (!this.c.knapsack[param[0]] && (this.c1.knapsack[param[0]] || this.c2.knapsack[param[0]]));
      }
    }
  }
  
  public static class SwapNH extends CompositeNH<InfoKP> {
    public SwapNH(InfoKP instance, SolutionKP c) {
      super(instance, new RangeNH[] { new SolutionKP.KnapSackNH(instance, c), new SolutionKP.UnpackedNH(instance, c) });
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\SolutionKP.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */