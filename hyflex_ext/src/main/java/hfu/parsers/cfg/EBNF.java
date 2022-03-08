package hfu.parsers.cfg;

import hfu.parsers.cfg.pep.Category;
import hfu.parsers.cfg.pep.Grammar;
import hfu.parsers.cfg.pep.Rule;
import java.util.ArrayList;
import java.util.Set;

public class EBNF {
  Grammar grammar = new Grammar("ebnf");
  
  public void addRule(String left, String right) {
    Category right_cs[], left_c = null;
    left = left.trim();
    if (left.startsWith("[") && left.endsWith("]")) {
      left_c = new Category(left.substring(1, left.length() - 1).trim());
    } else {
      System.out.println("ERROR: Invalid rule head: " + left);
    } 
    String[] right_split = right.split("[ \\t\\x0B\\f\\r]+");
    if (right.length() == 0) {
      right_cs = new Category[] { new Category("", true) };
    } else {
      right_cs = new Category[right_split.length];
      for (int i = 0; i < right_split.length; i++) {
        char c = right_split[i].charAt(0);
        if (c == '<') {
          right_cs[i] = new Category("§" + right_split[i].substring(1, right_split[i].length() - 1) + "§", true);
        } else if (c == '[') {
          right_cs[i] = new Category(right_split[i].substring(1, right_split[i].length() - 1));
        } else if (c == '\n') {
          right_cs[i] = new Category("§newline§", true);
        } else if (c == '\\') {
          right_cs[i] = new Category(right_split[i].substring(1), true);
        } else {
          right_cs[i] = new Category(right_split[i], true);
        } 
      } 
    } 
    this.grammar.addRule(new Rule(left_c, right_cs));
  }
  
  public Grammar getGrammar() {
    return this.grammar;
  }
  
  private void addReductions(Category left, Rule r, int index, ArrayList<Category> output, Grammar proper_grammar, Set<Rule> empty_rules) {
    Category[] right = r.getRight();
    if (index < right.length) {
      if (right[index].equals(left)) {
        ArrayList<Category> output2 = new ArrayList<>(output);
        addReductions(left, r, index + 1, output2, proper_grammar, empty_rules);
      } 
      output.add(right[index]);
      addReductions(left, r, index + 1, output, proper_grammar, empty_rules);
    } else if (output.size() == 0) {
      if (!r.getLeft().getName().equals("S"))
        empty_rules.add(new Rule(r.getLeft(), new Category[] { new Category("", true) })); 
    } else {
      proper_grammar.addRule(new Rule(r.getLeft(), output.<Category>toArray(new Category[0])));
    } 
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\EBNF.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */