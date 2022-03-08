package hfu.parsers.cfg.pep;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Grammar {
  String name;
  
  Set<Category> nullable;
  
  Map<Category, Set<Rule>> rules;
  
  public boolean isNullable(Category c) {
    if (this.nullable == null)
      computeNullable(); 
    return this.nullable.contains(c);
  }
  
  private void computeNullable() {
    this.nullable = new HashSet<>();
    boolean modified = true;
    while (modified) {
      modified = false;
      for (Category c : this.rules.keySet()) {
        if (!this.nullable.contains(c)) {
          boolean isNullable = true;
          for (Rule r : this.rules.get(c)) {
            isNullable = true;
            if (!r.getRight()[0].getName().equals("")) {
              byte b;
              int i;
              Category[] arrayOfCategory;
              for (i = (arrayOfCategory = r.getRight()).length, b = 0; b < i; ) {
                Category rc = arrayOfCategory[b];
                isNullable = (isNullable && this.nullable.contains(rc));
                b++;
              } 
            } 
            if (isNullable)
              break; 
          } 
          if (isNullable) {
            this.nullable.add(c);
            modified = true;
            break;
          } 
        } 
      } 
    } 
  }
  
  public Grammar(String name) {
    this.name = name;
    this.rules = new HashMap<>();
  }
  
  public String getName() {
    return this.name;
  }
  
  public boolean addRule(Rule rule) {
    Set<Rule> r;
    this.nullable = null;
    if (rule == null)
      throw new NullPointerException("null rule"); 
    if (!this.rules.containsKey(rule.left)) {
      r = new HashSet<>();
      this.rules.put(rule.left, r);
    } else {
      r = this.rules.get(rule.left);
    } 
    return r.add(rule);
  }
  
  public boolean containsRules(Category left) {
    return this.rules.containsKey(left);
  }
  
  public Set<Rule> getRules(Category left) {
    return this.rules.get(left);
  }
  
  public Set<Rule> getAllRules() {
    Set<Rule> allRules = new HashSet<>();
    for (Set<Rule> s : this.rules.values())
      allRules.addAll(s); 
    return allRules;
  }
  
  Rule getSingletonPreterminal(Category left, String token, boolean ignoreCase) {
    if (this.rules.containsKey(left))
      for (Rule r : this.rules.get(left)) {
        if (r.isSingletonPreterminal() && extended_equals((r.right[0]).name, token, ignoreCase))
          return r; 
      }  
    return null;
  }
  
  private boolean extended_equals(String terminal, String token, boolean ignoreCase) {
    String[] split;
    if (terminal.length() > 0 && terminal.charAt(0) == '§' && (split = terminal.substring(1, terminal.length() - 1).split(":")).length == 2) {
      if (split[1].equals("int"))
        try {
          Integer.parseInt(token);
          return true;
        } catch (Exception e) {
          return false;
        }  
      if (split[1].equals("float"))
        try {
          Double.parseDouble(token);
          return true;
        } catch (Exception e) {
          return false;
        }  
      if (split[1].equals("string"))
        return !token.equals("§newline§"); 
      System.out.println("ERROR: Unknown type: " + split[1]);
      return false;
    } 
    return !(!terminal.equals(token) && (!ignoreCase || !terminal.equalsIgnoreCase(token)));
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    sb.append(getClass().getSimpleName());
    sb.append(' ');
    sb.append(this.name);
    sb.append(": {");
    Iterator<Set<Rule>> si = this.rules.values().iterator();
    while (si.hasNext()) {
      Iterator<Rule> ri = ((Set<Rule>)si.next()).iterator();
      while (ri.hasNext()) {
        sb.append(((Rule)ri.next()).toString());
        if (ri.hasNext())
          sb.append(", "); 
      } 
      if (si.hasNext())
        sb.append(", "); 
    } 
    sb.append("}]");
    return sb.toString();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\Grammar.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */