package hfu.parsers.cfg.pep;

import hfu.parsers.cfg.MyTokenizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LLParser {
  Rule[] rules;
  
  Set<Category> terminals;
  
  Set<Category> categories;
  
  HashMap<Category, HashMap<Category, Integer>> table;
  
  public LLParser(Grammar grammar) {
    Set<Rule> rule_set = grammar.getAllRules();
    this.rules = new Rule[rule_set.size()];
    this.terminals = new HashSet<>();
    this.categories = new HashSet<>();
    int k = 0;
    for (Rule r : rule_set) {
      this.rules[k] = r;
      k++;
      this.categories.add(r.getLeft());
      Category[] right = r.getRight();
      byte b;
      int i;
      Category[] arrayOfCategory1;
      for (i = (arrayOfCategory1 = right).length, b = 0; b < i; ) {
        Category c = arrayOfCategory1[b];
        if (c.isTerminal()) {
          this.terminals.add(getTerminal(c));
        } else {
          this.categories.add(c);
        } 
        b++;
      } 
    } 
    buildTable();
  }
  
  private Category getTerminal(Category c) {
    String name = c.getName();
    String[] split;
    if (name.length() > 0 && name.charAt(0) == '§' && (split = name.substring(1, name.length() - 1).split(":")).length == 2)
      c = new Category("§" + split[1] + "§", true); 
    return c;
  }
  
  private String[] hasType(Category c) {
    String name = c.getName();
    String[] split;
    if (name.length() > 0 && name.charAt(0) == '§' && (split = name.substring(1, name.length() - 1).split(":")).length == 2)
      return split; 
    return null;
  }
  
  private Set<Category> getFi(int pos, Category[] right, HashMap<Category, Set<Category>> leftFI) {
    Set<Category> result = new HashSet<>();
    if (pos == right.length) {
      result.add(new Category("", true));
    } else {
      Category first = right[pos];
      if (first.isTerminal()) {
        result.add(getTerminal(first));
      } else {
        result.addAll(leftFI.get(first));
        if (result.remove(new Category("", true)))
          result.addAll(getFi(pos + 1, right, leftFI)); 
      } 
    } 
    return result;
  }
  
  private void buildTable() {
    int n = this.rules.length;
    Set[] rightFi = new Set[n];
    HashMap<Category, Set<Category>> leftFi = new HashMap<>();
    for (int i = 0; i < n; i++) {
      leftFi.put(this.rules[i].getLeft(), new HashSet<>());
      rightFi[i] = new HashSet();
    } 
    boolean modified = true;
    while (modified) {
      modified = false;
      for (int k = 0; k < n; k++) {
        Rule r = this.rules[k];
        Set<Category> FiAi = leftFi.get(r.getLeft());
        rightFi[k] = getFi(0, r.getRight(), leftFi);
        modified = !(!FiAi.addAll(rightFi[k]) && !modified);
      } 
    } 
    HashMap<Category, Set<Category>> leftFo = new HashMap<>();
    int j;
    for (j = 0; j < n; j++)
      leftFo.put(this.rules[j].getLeft(), new HashSet<>()); 
    modified = true;
    while (modified) {
      modified = false;
      for (j = 0; j < n; j++) {
        Rule r = this.rules[j];
        Category[] right = r.getRight();
        for (int k = 0; k < right.length; k++) {
          if (!right[k].isTerminal()) {
            Set<Category> Fiwp = getFi(k + 1, right, leftFi);
            for (Category c : Fiwp) {
              if (c.equals(new Category("", true))) {
                modified = !(!((Set)leftFo.get(right[k])).addAll(leftFo.get(r.getLeft())) && !modified);
                continue;
              } 
              if (c.isTerminal())
                modified = !(!((Set<Category>)leftFo.get(right[k])).add(c) && !modified); 
            } 
          } 
        } 
      } 
    } 
    this.table = new HashMap<>();
    for (Category t : this.terminals) {
      this.table.put(t, new HashMap<>());
      for (Category c : this.categories) {
        ((HashMap)this.table.get(t)).put(c, null);
        for (int k = 0; k < this.rules.length; k++) {
          if (this.rules[k].getLeft().equals(c) && (rightFi[k].contains(t) || (rightFi[k].contains(new Category("", true)) && ((Set)leftFo.get(this.rules[k].getLeft())).contains(t))))
            if (((HashMap)this.table.get(t)).get(c) == null) {
              ((HashMap<Category, Integer>)this.table.get(t)).put(c, Integer.valueOf(k));
            } else {
              System.out.println("This grammar is not LL(1)");
            }  
        } 
      } 
    } 
  }
  
  public ParseTree parse(MyTokenizer tokens, Category seed) {
    MyTokenizer.MyTokenIterator it = tokens.getTokenIterator();
    ParseTree current = new ParseTree(seed);
    ParseTree result = current;
    MyTokenizer.MyTokenIterator.Token token = null;
    boolean next = true;
    int k = 0;
    while (current != null) {
      if (next)
        if (it.hasNext()) {
          token = it.next();
          k++;
          next = false;
        } else {
          token = it.getEOF();
        }  
      Category tos = current.getNode();
      if (tos.isTerminal()) {
        String[] r;
        if ((r = hasType(tos)) != null) {
          current.value = token.value;
          current.c = new Category(r[0], true);
          try {
            if (r[1].equals("int")) {
              Integer.parseInt(token.value);
            } else if (r[1].equals("float")) {
              Double.parseDouble(token.value);
            } else if (r[1].equals("string")) {
              if (token.value.equals("§newline§")) {
                System.out.println("ERROR @" + token.line + "(" + token.pos + "): Token " + token.value + " is not of type " + r[1]);
                break;
              } 
            } else {
              System.out.println("ERROR @" + token.line + "(" + token.pos + "): Unknown type " + r[1]);
              break;
            } 
          } catch (Exception e) {
            System.out.println("ERROR @" + token.line + "(" + token.pos + "): Token " + (token.value.equals("") ? "EOF" : token.value) + " is not of type " + r[1]);
            break;
          } 
          next = true;
          current = pop(current);
          continue;
        } 
        if (tos.getName().equals(token.value)) {
          next = true;
          current = pop(current);
          continue;
        } 
        System.out.println("ERROR @" + token.line + "(" + token.pos + "): Got " + (token.value.equals("") ? "EOF" : token.value) + ", expected " + tos);
        break;
      } 
      Integer ri = null;
      HashMap<Category, Integer> t;
      if ((t = this.table.get(new Category(token.value, true))) != null)
        ri = t.get(tos); 
      if (ri == null && !token.value.equals("§newline§") && (t = this.table.get(new Category("§string§", true))) != null)
        ri = t.get(tos); 
      if (ri == null && (t = this.table.get(new Category("§float§", true))) != null && (ri = t.get(tos)) != null)
        try {
          Double.parseDouble(token.value);
        } catch (Exception e) {
          ri = null;
        }  
      if (ri == null && (t = this.table.get(new Category("§int§", true))) != null && (ri = t.get(tos)) != null)
        try {
          Integer.parseInt(token.value);
        } catch (Exception e) {
          ri = null;
        }  
      if (ri != null) {
        Category[] right = this.rules[ri.intValue()].getRight();
        ParseTree[] children = new ParseTree[right.length];
        if (!right[0].getName().equals("")) {
          for (int i = 0; i < right.length; i++)
            children[i] = new ParseTree(right[i]); 
          current = current.setChildren(children);
          continue;
        } 
        current = pop(current);
        continue;
      } 
      System.out.println("ERROR @" + token.line + "(" + token.pos + "): Category " + tos + ", can't start with " + (token.value.equals("") ? "EOF" : token.value));
      break;
    } 
    if (it.hasNext() || current != null)
      System.out.println("REJECT"); 
    return result;
  }
  
  private ParseTree pop(ParseTree stack) {
    ParseTree new_stack = stack.next();
    while (new_stack == null && 
      stack.parent != null) {
      stack = stack.parent;
      new_stack = stack.next();
    } 
    return new_stack;
  }
  
  public class ParseTree {
    public Category c;
    
    public ParseTree[] children;
    
    public ParseTree parent;
    
    public int i = 0;
    
    public String value;
    
    ParseTree(Category c) {
      this.c = c;
    }
    
    ParseTree setChildren(ParseTree[] children) {
      this.children = children;
      for (int i = 0; i < children.length; i++)
        (children[i]).parent = this; 
      return this.children[0];
    }
    
    Category getNode() {
      return this.c;
    }
    
    ParseTree next() {
      this.i++;
      if (this.children != null && this.i < this.children.length)
        return this.children[this.i]; 
      return null;
    }
    
    public String toString() {
      String result = this.c.getName();
      if (this.children != null && this.children.length > 0) {
        result = String.valueOf(result) + "[";
        for (int i = 0; i < this.children.length - 1; i++) {
          result = String.valueOf(result) + this.children[i].toString();
          result = String.valueOf(result) + ",";
        } 
        result = String.valueOf(result) + this.children[this.children.length - 1].toString();
        result = String.valueOf(result) + "]";
      } else if (this.value != null) {
        result = String.valueOf(result) + "=" + this.value;
      } 
      return result;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\LLParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */