package hfu.parsers.cfg.pep;

import java.util.Arrays;
import java.util.Iterator;

public class ParseTree {
  Category node;
  
  ParseTree parent;
  
  ParseTree[] children = null;
  
  int origin;
  
  public int getOrigin() {
    return this.origin;
  }
  
  public ParseTree(Category node, ParseTree parent, int origin) {
    this(node, parent, null, origin);
  }
  
  public ParseTree(Category node, ParseTree parent, ParseTree[] children, int origin) {
    this.node = node;
    this.parent = parent;
    this.children = children;
    this.origin = origin;
  }
  
  public static ParseTree newParseTree(Edge edge) {
    return newParseTree(edge, null);
  }
  
  public static ParseTree newParseTree(Edge edge, ParseTree parent) {
    Edge e;
    ParseTree parentTree;
    if (edge.dottedRule.left.equals(Category.START)) {
      e = edge.bases.iterator().next();
      parentTree = null;
    } else {
      e = edge;
      parentTree = (parent != null && parent.node.equals(Category.START)) ? 
        null : parent;
    } 
    DottedRule dr = e.dottedRule;
    ParseTree newTree = null;
    if (e.isPassive()) {
      int basisCount = e.bases.size();
      newTree = new ParseTree(dr.left, parentTree, 
          (basisCount == 0) ? null : new ParseTree[basisCount], edge.origin);
      if (basisCount > 0) {
        int i = 0;
        Iterator<Edge> itr = e.bases.iterator();
        while (itr.hasNext())
          newTree.children[i++] = 
            newParseTree(itr.next(), newTree); 
      } 
    } else {
      newTree = new ParseTree(dr.activeCategory, parentTree, null, edge.origin);
    } 
    return newTree;
  }
  
  public Category getNode() {
    return this.node;
  }
  
  public ParseTree getParent() {
    return this.parent;
  }
  
  public ParseTree[] getChildren() {
    return this.children;
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof ParseTree) {
      ParseTree op = (ParseTree)obj;
      return (this.node.equals(op.node) && ((
        this.parent == null && op.parent == null) || 
        this.parent.node.equals(op.parent.node)) && ((
        this.children == null && op.children == null) || 
        Arrays.equals((Object[])this.children, (Object[])op.children)));
    } 
    return false;
  }
  
  public int hashCode() {
    int hash = 31 * this.node.hashCode();
    if (this.parent != null)
      hash *= 17 * this.parent.node.hashCode(); 
    if (this.children != null)
      hash *= Arrays.hashCode((Object[])this.children); 
    return hash;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    sb.append(this.node.toString());
    if (this.children != null) {
      byte b;
      int i;
      ParseTree[] arrayOfParseTree;
      for (i = (arrayOfParseTree = this.children).length, b = 0; b < i; ) {
        ParseTree child = arrayOfParseTree[b];
        sb.append(child.toString());
        b++;
      } 
    } 
    sb.append(']');
    return sb.toString();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\ParseTree.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */