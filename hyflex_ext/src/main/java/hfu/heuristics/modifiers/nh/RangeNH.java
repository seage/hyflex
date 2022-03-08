package hfu.heuristics.modifiers.nh;

import hfu.BenchmarkInfo;
import hfu.RNG;
import hfu.heuristics.modifiers.nh.filter.Filter;

public class RangeNH<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH {
  int lo;
  
  int hi;
  
  Filter filter;
  
  public RangeNH(int lo, int hi, P instance) {
    super(instance);
    this.lo = lo;
    this.hi = hi;
  }
  
  public RangeNH(int lo, int hi, Filter filter, P instance) {
    this(lo, hi, instance);
    this.filter = filter;
  }
  
  public int[] sample() {
    int[] result;
    if (this.hi <= this.lo) {
      result = null;
    } else if (this.filter != null) {
      int[] range = new int[this.hi - this.lo];
      for (int i = 0; i < this.hi - this.lo; i++)
        range[i] = i; 
      int max = this.hi - this.lo;
      int pick = RNG.get().nextInt(max);
      result = new int[] { this.lo + range[pick] };
      while (!this.filter.include(result) && max > 1) {
        max--;
        int temp = range[max];
        range[max] = range[pick];
        range[pick] = temp;
        pick = RNG.get().nextInt(max);
        result[0] = this.lo + range[pick];
      } 
      if (max == 1 && !this.filter.include(result))
        result = null; 
    } else {
      result = new int[] { this.lo + RNG.get().nextInt(this.hi - this.lo) };
    } 
    return result;
  }
  
  public int getDimensionality() {
    return 1;
  }
  
  public int getUnFilteredSize() {
    return this.hi - this.lo;
  }
  
  public IteratorNH getIterator() {
    return (this.filter == null) ? new RangeIterator() : new FilteredIterator(new RangeIterator(), this.filter);
  }
  
  public IteratorNH getIterator(int init) {
    return (this.filter == null) ? new RangeIterator(init) : new FilteredIterator(new RangeIterator(init), this.filter);
  }
  
  public IteratorNH getRandomIterator() {
    int init = this.lo + RNG.get().nextInt(this.hi - this.lo);
    return getIterator(init);
  }
  
  public int getHigh() {
    return this.hi;
  }
  
  public int getLow() {
    return this.lo;
  }
  
  class RangeIterator extends IteratorNH {
    int init;
    
    int current;
    
    boolean first = true;
    
    RangeIterator() {
      this.init = RangeNH.this.lo;
      this.current = RangeNH.this.lo;
    }
    
    RangeIterator(int init) {
      this.init = init;
      this.current = init;
    }
    
    public boolean hasNext() {
      return !(!this.first && this.current == this.init);
    }
    
    public int[] next() {
      this.first = false;
      int result = this.current;
      this.current++;
      if (this.current == RangeNH.this.hi)
        this.current = RangeNH.this.lo; 
      return new int[] { result };
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\modifiers\nh\RangeNH.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */