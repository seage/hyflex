package hfu.heuristics.modifiers.nh;

import hfu.BenchmarkInfo;

public abstract class MultiLevelNH<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH {
  int nlevels;
  
  public MultiLevelNH(int nlevels, P instance) {
    super(instance);
    this.nlevels = nlevels;
  }
  
  public abstract RangeNH<P> getNeighbourhood(int[] paramArrayOfint, int paramInt);
  
  public int[] sample() {
    int[] result = new int[this.nlevels];
    for (int i = 0; i < this.nlevels; i++)
      result[i] = getNeighbourhood(result, i).sample()[0]; 
    return result;
  }
  
  public IteratorNH getIterator() {
    return new MultiLevelIterator();
  }
  
  public IteratorNH getRandomIterator() {
    return new MultiLevelIterator(sample());
  }
  
  public int getDimensionality() {
    return this.nlevels;
  }
  
  class MultiLevelIterator extends IteratorNH {
    IteratorNH[] its;
    
    int[] current;
    
    boolean done = false;
    
    MultiLevelIterator() {
      this.its = new IteratorNH[MultiLevelNH.this.nlevels];
      this.current = new int[MultiLevelNH.this.nlevels];
      for (int i = 0; i < MultiLevelNH.this.nlevels; i++) {
        RangeNH<P> nh = MultiLevelNH.this.getNeighbourhood(this.current, i);
        this.its[i] = nh.getIterator();
        if (this.its[i].hasNext()) {
          this.current[i] = this.its[i].next()[0];
        } else {
          if (i == 0) {
            this.done = true;
            break;
          } 
          i -= 2;
        } 
      } 
    }
    
    MultiLevelIterator(int[] init) {
      this.its = new IteratorNH[MultiLevelNH.this.nlevels];
      this.current = new int[MultiLevelNH.this.nlevels];
      for (int i = 0; i < MultiLevelNH.this.nlevels; i++) {
        RangeNH<P> nh = MultiLevelNH.this.getNeighbourhood(this.current, i);
        this.its[i] = nh.getIterator(init[i]);
        if (this.its[i].hasNext()) {
          this.current[i] = this.its[i].next()[0];
        } else {
          this.done = true;
          break;
        } 
      } 
    }
    
    public boolean hasNext() {
      return !this.done;
    }
    
    public int[] next() {
      int[] result = new int[MultiLevelNH.this.nlevels];
      System.arraycopy(this.current, 0, result, 0, MultiLevelNH.this.nlevels);
      for (int i = MultiLevelNH.this.nlevels - 1; i >= 0; i--) {
        if (this.its[i].hasNext()) {
          boolean next = true;
          this.current[i] = this.its[i].next()[0];
          for (int j = i + 1; j < MultiLevelNH.this.nlevels; j++) {
            this.its[j] = MultiLevelNH.this.getNeighbourhood(this.current, j).getIterator();
            if (this.its[j].hasNext()) {
              this.current[j] = this.its[j].next()[0];
            } else {
              if (j == i + 1) {
                i++;
                next = false;
                break;
              } 
              j -= 2;
            } 
          } 
          if (next)
            break; 
        } else if (i == 0) {
          this.done = true;
        } 
      } 
      return result;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\modifiers\nh\MultiLevelNH.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */