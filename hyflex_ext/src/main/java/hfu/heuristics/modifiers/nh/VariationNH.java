package hfu.heuristics.modifiers.nh;

import hfu.BenchmarkInfo;
import hfu.RNG;
import hfu.heuristics.modifiers.nh.filter.Filter;
import java.util.Arrays;

public class VariationNH<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH {
  RangeNH<P> nh;
  
  int lo;
  
  int hi;
  
  Filter filter;
  
  int k;
  
  public VariationNH(RangeNH<P> nh, int k, P instance) {
    super(instance);
    this.nh = nh;
    this.k = k;
    this.lo = nh.getLow();
    this.hi = nh.getHigh();
  }
  
  public VariationNH(RangeNH<P> nh, int k, Filter filter, P instance) {
    this(nh, k, instance);
    this.filter = filter;
  }
  
  private int[] sample_unfiltered() {
    int[] sample = new int[this.k];
    for (int i = 0; i < this.k; i++)
      sample[i] = this.lo + RNG.get().nextInt(this.hi - this.lo); 
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
    return (this.filter == null) ? new VariationIterator() : new FilteredIterator(new VariationIterator(), this.filter);
  }
  
  public IteratorNH getRandomIterator() {
    int[] init = sample_unfiltered();
    return (this.filter == null) ? new VariationIterator(init) : new FilteredIterator(new VariationIterator(init), this.filter);
  }
  
  class VariationIterator extends IteratorNH {
    int[] current;
    
    int[] init;
    
    boolean done;
    
    VariationIterator() {
      this.current = new int[VariationNH.this.k];
      this.init = new int[VariationNH.this.k];
      for (int i = 0; i < VariationNH.this.k; i++) {
        this.init[i] = 0;
        this.current[i] = 0;
      } 
    }
    
    VariationIterator(int[] init) {
      this.init = init;
      this.current = new int[VariationNH.this.k];
      System.arraycopy(init, 0, this.current, 0, init.length);
    }
    
    public boolean hasNext() {
      return !this.done;
    }
    
    public int[] next() {
      int[] result = new int[VariationNH.this.k];
      System.arraycopy(this.current, 0, result, 0, VariationNH.this.k);
      for (int i = 0; i < VariationNH.this.k; i++) {
        if (this.current[i] < VariationNH.this.hi - 1) {
          this.current[i] = this.current[i] + 1;
          break;
        } 
        this.current[i] = VariationNH.this.lo;
      } 
      if (Arrays.equals(this.current, this.init))
        this.done = true; 
      return result;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\modifiers\nh\VariationNH.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */