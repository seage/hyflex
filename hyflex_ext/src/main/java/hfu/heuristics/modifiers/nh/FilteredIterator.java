package hfu.heuristics.modifiers.nh;

import hfu.heuristics.modifiers.nh.filter.Filter;

public class FilteredIterator extends IteratorNH {
  IteratorNH it;
  
  Filter filter;
  
  int[] current;
  
  boolean done;
  
  public FilteredIterator(IteratorNH it, Filter filter) {
    this.it = it;
    this.filter = filter;
    this.done = true;
    while (it.hasNext()) {
      this.current = it.next();
      if (filter.include(this.current)) {
        this.done = false;
        break;
      } 
    } 
  }
  
  public boolean hasNext() {
    return !this.done;
  }
  
  public int[] next() {
    int[] result = null;
    if (this.current != null) {
      result = new int[this.current.length];
      System.arraycopy(this.current, 0, result, 0, this.current.length);
      this.done = true;
      while (this.it.hasNext()) {
        this.current = this.it.next();
        if (this.filter.include(this.current)) {
          this.done = false;
          break;
        } 
      } 
    } 
    return result;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\modifiers\nh\FilteredIterator.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */