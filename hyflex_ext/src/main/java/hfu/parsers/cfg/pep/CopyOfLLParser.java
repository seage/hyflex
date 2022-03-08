package hfu.parsers.cfg.pep;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class CopyOfLLParser {
  Rule[] rules;
  
  Set<Category> terminals;
  
  Set<Category> categories;
  
  HashMap<Category, HashMap<Category, Integer>> table;
  
  public CopyOfLLParser(Grammar grammar) {
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
          this.terminals.add(c);
        } else {
          this.categories.add(c);
        } 
        b++;
      } 
    } 
    buildTable();
  }
  
  private Set<Category> getFi(int pos, Category[] right, HashMap<Category, Set<Category>> leftFI) {
    Set<Category> result = new HashSet<>();
    if (pos == right.length) {
      result.add(new Category("", true));
    } else {
      Category first = right[pos];
      if (first.isTerminal()) {
        result.add(first);
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
    System.out.print("");
  }
  
  public void parse(Iterable<String> tokens, Category seed) {
    LinkedList<Category> stack = new LinkedList<>();
    stack.push(seed);
    Iterator<String> it = tokens.iterator();
    ParseTree current = new ParseTree(seed.getName());
    String token = null;
    boolean next = true;
    while (stack.size() > 0) {
      if (next)
        if (it.hasNext()) {
          token = it.next();
          next = false;
        } else {
          break;
        }  
      if (((Category)stack.getFirst()).isTerminal()) {
        if (((Category)stack.getFirst()).getName().equals(token)) {
          stack.pop();
          next = true;
          current = current.next();
          continue;
        } 
        break;
      } 
      Integer ri = (Integer)((HashMap)this.table.get(new Category(token, true))).get(stack.getFirst());
      stack.pop();
      if (ri != null) {
        Category[] right = this.rules[ri.intValue()].getRight();
        ParseTree[] children = new ParseTree[right.length];
        for (int i = 0; i < right.length; i++) {
          stack.push(right[right.length - 1 - i]);
          children[i] = new ParseTree(right[i].getName());
        } 
        current = current.setChildren(children);
        continue;
      } 
      break;
    } 
    if (!it.hasNext() && stack.size() == 0) {
      System.out.println("ACCEPT");
    } else {
      System.out.println("REJECT");
    } 
  }
  
  class ParseTree {
    String name;
    
    ParseTree[] children;
    
    ParseTree parent;
    
    int i = 0;
    
    ParseTree(String root) {
      this.name = root;
    }
    
    ParseTree setChildren(ParseTree[] children) {
      this.children = children;
      for (int i = 0; i < children.length; i++)
        (children[i]).parent = this; 
      return this.children[0];
    }
    
    ParseTree next() {
      this.i++;
      if (this.children != null && this.i < this.children.length)
        return this.children[this.i]; 
      if (this.parent != null)
        return this.parent.next(); 
      return this;
    }
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\CopyOfLLParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */