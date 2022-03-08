package hfu.heuristics.modifiers.nh;

import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.nh.filter.Filter;
import java.util.Arrays;

public class CompositeNH<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH {
  RangeNH<P>[] nhs;
  
  Filter filter;
  
  int k;
  
  @SafeVarargs
  public CompositeNH(P instance, RangeNH... nhs) {
    super(instance);
    this.nhs = (RangeNH<P>[])nhs;
    this.k = nhs.length;
  }
  
  @SafeVarargs
  public CompositeNH(P instance, Filter filter, RangeNH... nhs) {
    this(instance, (RangeNH<P>[])nhs);
    this.filter = filter;
  }
  
  private int[] sample_unfiltered() {
    int[] sample = new int[this.nhs.length];
    for (int i = 0; i < this.nhs.length; i++) {
      int[] param = this.nhs[i].sample();
      if (param != null) {
        sample[i] = param[0];
      } else {
        return null;
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
    return this.nhs.length;
  }
  
  public IteratorNH getIterator() {
    return (this.filter == null) ? new CompositeIterator() : new FilteredIterator(new CompositeIterator(), this.filter);
  }
  
  public IteratorNH getRandomIterator() {
    int[] init = sample_unfiltered();
    return (this.filter == null) ? new CompositeIterator(init) : new FilteredIterator(new CompositeIterator(init), this.filter);
  }
  
  class CompositeIterator extends IteratorNH {
    IteratorNH[] its;
    
    int[] current;
    
    int[] init;
    
    boolean done;
    
    CompositeIterator() {
      this.current = new int[CompositeNH.this.k];
      this.init = new int[CompositeNH.this.k];
      this.its = new IteratorNH[CompositeNH.this.k];
      for (int i = 0; i < CompositeNH.this.nhs.length; i++) {
        this.its[i] = CompositeNH.this.nhs[i].getIterator();
        if (this.its[i].hasNext()) {
          this.init[i] = this.its[i].next()[0];
          this.current[i] = this.init[i];
        } else {
          this.done = true;
        } 
      } 
    }
    
    CompositeIterator(int[] init) {
      if (init != null) {
        this.current = new int[CompositeNH.this.k];
        this.init = new int[CompositeNH.this.k];
        this.its = new IteratorNH[CompositeNH.this.k];
        for (int i = 0; i < CompositeNH.this.nhs.length; i++) {
          this.its[i] = CompositeNH.this.nhs[i].getIterator(init[i]);
          if (this.its[i].hasNext()) {
            this.init[i] = this.its[i].next()[0];
            this.current[i] = this.init[i];
          } else {
            this.done = true;
          } 
        } 
      } else {
        this.done = true;
      } 
    }
    
    public boolean hasNext() {
      return !this.done;
    }
    
    public int[] next() {
      int[] result = new int[CompositeNH.this.k];
      System.arraycopy(this.current, 0, result, 0, CompositeNH.this.k);
      for (int i = 0; i < CompositeNH.this.k; i++) {
        if (this.its[i].hasNext()) {
          this.current[i] = this.its[i].next()[0];
          break;
        } 
        this.its[i] = CompositeNH.this.nhs[i].getIterator();
        this.current[i] = this.its[i].next()[0];
      } 
      if (Arrays.equals(this.current, this.init))
        this.done = true; 
      return result;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\modifiers\nh\CompositeNH.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */