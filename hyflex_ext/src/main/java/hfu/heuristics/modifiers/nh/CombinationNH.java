package hfu.heuristics.modifiers.nh;

import hfu.BenchmarkInfo;
import hfu.RNG;
import hfu.heuristics.modifiers.nh.filter.Filter;
import java.util.Arrays;

public class CombinationNH<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH {
  RangeNH<P> nh;
  
  int lo;
  
  int hi;
  
  Filter filter;
  
  int k;
  
  public CombinationNH(RangeNH<P> nh, int k, P instance) {
    super(instance);
    this.nh = nh;
    this.k = k;
    this.lo = nh.getLow();
    this.hi = nh.getHigh();
  }
  
  public CombinationNH(RangeNH<P> nh, int k, Filter filter, P instance) {
    this(nh, k, instance);
    this.filter = filter;
  }
  
  private int[] sample_unfiltered() {
    int[] sample = new int[this.k];
    for (int i = 0; i < this.k; i++) {
      sample[i] = this.lo + RNG.get().nextInt(this.hi - this.lo - i);
      for (int j = 0; j < i; j++) {
        if (sample[i] >= sample[j])
          sample[i] = sample[i] + 1; 
      } 
    } 
    return sample;
  }
  
  public int[] sample() {
    int[] result = sample_unfiltered();
    while (this.filter != null && !this.filter.include(result))
      result = sample_unfiltered(); 
    return result;
  }
  
  public int getDimensionality() {
    return this.k;
  }
  
  public IteratorNH getIterator() {
    return (this.filter == null) ? new CombinationIterator() : new FilteredIterator(new CombinationIterator(), this.filter);
  }
  
  public IteratorNH getRandomIterator() {
    int[] init = sample_unfiltered();
    Arrays.sort(init);
    return (this.filter == null) ? new CombinationIterator(init) : new FilteredIterator(new CombinationIterator(init), this.filter);
  }
  
  class CombinationIterator extends IteratorNH {
    int[] current;
    
    int[] init;
    
    boolean done;
    
    CombinationIterator() {
      this.init = new int[CombinationNH.this.k];
      this.current = new int[CombinationNH.this.k];
      for (int i = 0; i < CombinationNH.this.k; i++) {
        this.current[i] = CombinationNH.this.lo + i;
        this.init[i] = CombinationNH.this.lo + i;
      } 
      if (CombinationNH.this.k > CombinationNH.this.hi - CombinationNH.this.lo)
        this.done = true; 
    }
    
    CombinationIterator(int[] init) {
      this.current = init;
      this.init = new int[CombinationNH.this.k];
      System.arraycopy(init, 0, this.init, 0, CombinationNH.this.k);
    }
    
    public boolean hasNext() {
      return !this.done;
    }
    
    public int[] next() {
      int[] result = new int[CombinationNH.this.k];
      System.arraycopy(this.current, 0, result, 0, CombinationNH.this.k);
      for (int i = CombinationNH.this.k - 1; i >= 0; i--) {
        if (this.current[i] < CombinationNH.this.hi - CombinationNH.this.k + i) {
          this.current[i] = this.current[i] + 1;
          for (int j = i + 1; j < CombinationNH.this.k; j++)
            this.current[j] = this.current[i] + j - i; 
          break;
        } 
        if (i == 0)
          for (int j = 0; j < CombinationNH.this.k; j++)
            this.current[j] = CombinationNH.this.lo + j;  
      } 
      if (Arrays.equals(this.current, this.init))
        this.done = true; 
      return result;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\modifiers\nh\CombinationNH.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */