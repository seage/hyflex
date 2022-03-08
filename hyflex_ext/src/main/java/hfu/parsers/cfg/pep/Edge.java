package hfu.parsers.cfg.pep;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Edge {
  DottedRule dottedRule;
  
  int origin;
  
  Set<Edge> bases;
  
  public Edge(DottedRule dottedRule, int origin) {
    this(dottedRule, origin, null);
  }
  
  public Edge(DottedRule dottedRule, int origin, Set<Edge> bases) {
    if (origin < 0)
      throw new IndexOutOfBoundsException("origin < 0: " + origin); 
    this.dottedRule = dottedRule;
    this.origin = origin;
    if (bases == null) {
      this.bases = Collections.emptySet();
    } else {
      this.bases = bases;
    } 
  }
  
  public static Edge predictFor(Rule rule, int origin) {
    if (rule == null)
      throw new NullPointerException("null rule"); 
    return new Edge(new DottedRule(rule), origin);
  }
  
  public static Edge scan(Edge edge, String token) {
    if (edge == null)
      throw new NullPointerException("null edge"); 
    if (token == null)
      throw new NullPointerException("null input token"); 
    if (edge.isPassive())
      throw new IllegalArgumentException("passive edge"); 
    DottedRule dr = edge.dottedRule;
    if (!dr.activeCategory.terminal)
      throw new IllegalArgumentException(
          "edge's active category is nonterminal: " + edge); 
    Edge scanEdge = new Edge(DottedRule.advanceDot(dr), edge.origin);
    scanEdge.bases = addBasisEdge(edge, edge);
    return scanEdge;
  }
  
  public static Edge complete(Edge toComplete, Edge basis) {
    if (toComplete == null)
      throw new NullPointerException("null edge to complete"); 
    if (toComplete.isPassive())
      throw new IllegalArgumentException(
          "attempt to complete passive edge: " + toComplete); 
    if (basis == null)
      throw new NullPointerException("null basis"); 
    if (!basis.isPassive())
      throw new IllegalArgumentException("basis is active: " + basis); 
    if (basis.dottedRule.position == 0 || 
      !basis.dottedRule.left.equals(
        toComplete.dottedRule.activeCategory))
      throw new IllegalArgumentException(toComplete + 
          " is not completed by basis " + basis); 
    Set<Edge> newBases = addBasisEdge(toComplete, basis);
    return new Edge(DottedRule.advanceDot(toComplete.dottedRule), 
        toComplete.origin, newBases);
  }
  
  static Set<Edge> addBasisEdge(Edge edge, Edge basis) {
    Set<Edge> newBases;
    if (edge.bases.isEmpty()) {
      newBases = Collections.singleton(basis);
    } else {
      newBases = new LinkedHashSet<>(edge.bases);
      newBases.add(basis);
    } 
    return newBases;
  }
  
  public DottedRule getDottedRule() {
    return this.dottedRule;
  }
  
  public int getOrigin() {
    return this.origin;
  }
  
  public Set<Edge> getBases() {
    return this.bases;
  }
  
  public boolean isPassive() {
    return (this.dottedRule.activeCategory == null);
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Edge) {
      Edge oe = (Edge)obj;
      return (this.origin == oe.origin && 
        this.dottedRule.equals(oe.dottedRule) && 
        this.bases.equals(oe.bases));
    } 
    return false;
  }
  
  public int hashCode() {
    return (37 + this.origin) * this.dottedRule.hashCode() * (
      1 + this.bases.hashCode());
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder(Integer.toString(this.origin));
    sb.append('[');
    sb.append(this.dottedRule.toString());
    sb.append(']');
    return sb.toString();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\Edge.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */