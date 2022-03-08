package hfu.parsers.cfg.pep;

public class Category {
  String name;
  
  boolean terminal;
  
  public static final Category START = new Category("<start>", false) {
      public boolean equals(Object obj) {
        return (this == obj);
      }
    };
  
  public Category(String name) {
    this(name, false);
  }
  
  public Category(String name, boolean terminal) {
    if (!terminal && (name == null || name.length() == 0))
      throw new IllegalArgumentException(
          "empty name specified for category"); 
    this.name = name;
    this.terminal = terminal;
  }
  
  public String getName() {
    return this.name;
  }
  
  public boolean isTerminal() {
    return this.terminal;
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Category) {
      Category oc = (Category)obj;
      return (oc != START && 
        this.terminal == oc.terminal && this.name.equals(oc.name));
    } 
    return false;
  }
  
  public int hashCode() {
    return 31 * this.name.hashCode() * Boolean.valueOf(this.terminal).hashCode();
  }
  
  public String toString() {
    return (this.name.length() == 0) ? "<empty>" : this.name;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\Category.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */