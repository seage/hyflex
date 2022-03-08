package hfu.parsers.cfg.pep;

import java.util.Arrays;

public class Rule {
  Category left;
  
  Category[] right;
  
  public Rule(Category left, Category... right) {
    if (left == null)
      throw new IllegalArgumentException("empty left category"); 
    if (left.terminal)
      throw new IllegalArgumentException("left category is terminal"); 
    if (right == null || right.length == 0)
      throw new IllegalArgumentException("no right categories"); 
    byte b;
    int i;
    Category[] arrayOfCategory;
    for (i = (arrayOfCategory = right).length, b = 0; b < i; ) {
      Category r = arrayOfCategory[b];
      if (r == null)
        throw new IllegalArgumentException(
            "right contains null category: " + Arrays.toString(right)); 
      b++;
    } 
    this.left = left;
    this.right = right;
  }
  
  public Category getLeft() {
    return this.left;
  }
  
  public Category[] getRight() {
    return this.right;
  }
  
  public boolean isPreterminal() {
    byte b;
    int i;
    Category[] arrayOfCategory;
    for (i = (arrayOfCategory = this.right).length, b = 0; b < i; ) {
      Category r = arrayOfCategory[b];
      if (r.terminal)
        return true; 
      b++;
    } 
    return false;
  }
  
  public boolean isSingletonPreterminal() {
    return (isPreterminal() && this.right.length == 1);
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Rule) {
      Rule or = (Rule)obj;
      return (this.left.equals(or.left) && Arrays.equals((Object[])this.right, (Object[])or.right));
    } 
    return false;
  }
  
  public int hashCode() {
    return 31 * this.left.hashCode() * Arrays.hashCode((Object[])this.right);
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder(this.left.toString());
    sb.append(" ->");
    for (int i = 0; i < this.right.length; i++) {
      sb.append(' ');
      sb.append(this.right[i].toString());
    } 
    return sb.toString();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\Rule.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */