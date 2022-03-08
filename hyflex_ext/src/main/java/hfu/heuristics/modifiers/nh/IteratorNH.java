package hfu.heuristics.modifiers.nh;

import java.util.Iterator;

public abstract class IteratorNH implements Iterator<int[]> {
  public void remove() {}
  
  public static IteratorNH fromIterator(Iterator<Integer> it) {
    return new SimpleIterator(it);
  }
  
  static class SimpleIterator extends IteratorNH {
    Iterator<Integer> it;
    
    SimpleIterator(Iterator<Integer> it) {
      this.it = it;
    }
    
    public boolean hasNext() {
      return this.it.hasNext();
    }
    
    public int[] next() {
      return new int[] { ((Integer)this.it.next()).intValue() };
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\modifiers\nh\IteratorNH.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */