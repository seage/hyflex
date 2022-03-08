package hfu.parsers.cfg.pep;

public class DottedRule extends Rule {
  int position;
  
  Category activeCategory;
  
  public DottedRule(Rule rule) {
    this(rule, 0);
  }
  
  public DottedRule(Rule rule, int position) {
    super(rule.left, rule.right);
    if (position < 0 || position > this.right.length)
      throw new IndexOutOfBoundsException(
          "illegal position: " + position); 
    this.position = position;
    this.activeCategory = (position < this.right.length) ? this.right[position] : null;
  }
  
  public static DottedRule advanceDot(DottedRule dottedRule) {
    return new DottedRule(dottedRule, dottedRule.position + 1);
  }
  
  public static DottedRule startRule(Category seed) {
    if (seed == null)
      throw new NullPointerException("null seed"); 
    if (seed.terminal)
      throw new IllegalArgumentException("seed is a terminal: " + seed); 
    return new DottedRule(new Rule(Category.START, new Category[] { seed }), 0);
  }
  
  public int getPosition() {
    return this.position;
  }
  
  public Category getActiveCategory() {
    return this.activeCategory;
  }
  
  public boolean equals(Object obj) {
    return (obj instanceof DottedRule && super.equals(obj) && 
      this.position == ((DottedRule)obj).position);
  }
  
  public int hashCode() {
    return super.hashCode() * (31 + this.position);
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder(this.left.toString());
    sb.append(" ->");
    for (int i = 0; i <= this.right.length; i++) {
      if (i == this.position)
        sb.append(" *"); 
      if (i < this.right.length) {
        sb.append(' ');
        sb.append(this.right[i].toString());
      } 
    } 
    return sb.toString();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\DottedRule.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */