package hfu.parsers.cfg.pep;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parse {
  List<String> tokens;
  
  Category seed;
  
  Chart chart;
  
  boolean error;
  
  private Set<ParseTree> parseTrees;
  
  Parse(Category seed, Chart chart) {
    this(seed, chart, false);
  }
  
  Parse(Category seed, Chart chart, boolean error) {
    this.seed = seed;
    this.chart = chart;
    this.error = error;
    this.tokens = new ArrayList<>();
  }
  
  public List<String> getTokens() {
    return Collections.unmodifiableList(this.tokens);
  }
  
  public Category getSeed() {
    return this.seed;
  }
  
  public Chart getChart() {
    return this.chart;
  }
  
  Set<Edge> getCompletedEdges(Category category, int origin, int index) {
    Set<Edge> edges = this.chart.edgeSets.get(Integer.valueOf(index));
    if (edges == null || edges.isEmpty())
      return Collections.emptySet(); 
    Set<Edge> es = new HashSet<>();
    for (Edge e : edges) {
      if (e.origin == origin && e.isPassive() && 
        e.dottedRule.left.equals(category))
        es.add(e); 
    } 
    return es;
  }
  
  public Status getStatus() {
    return this.error ? 
      Status.ERROR : (getCompletedEdges(Category.START, 0, this.tokens.size()).isEmpty() ? 
      Status.REJECT : Status.ACCEPT);
  }
  
  public Set<ParseTree> getParseTrees() {
    if (this.parseTrees == null)
      if (this.error) {
        this.parseTrees = Collections.emptySet();
      } else {
        this.parseTrees = getParseTreesFor(Category.START, 0, this.tokens.size());
      }  
    return this.parseTrees;
  }
  
  public ParseTree getParseTreeFor(Edge edge) {
    if (edge == null)
      throw new NullPointerException("edge is null"); 
    if (!this.chart.contains(edge))
      return null; 
    return ParseTree.newParseTree(edge);
  }
  
  public Set<ParseTree> getParseTreesFor(Category category, int origin, int index) {
    if (category == null)
      throw new NullPointerException("null category"); 
    Set<ParseTree> trees = new HashSet<>();
    for (Edge e : getCompletedEdges(category, origin, index))
      trees.add(ParseTree.newParseTree(e)); 
    return trees;
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Parse) {
      Parse op = (Parse)obj;
      return (this.error == op.error && this.tokens.equals(op.tokens) && 
        this.seed.equals(op.seed) && this.chart.equals(op.chart));
    } 
    return false;
  }
  
  public int hashCode() {
    return 31 * this.tokens.hashCode() * this.seed.hashCode() * this.chart.hashCode();
  }
  
  public String toString() {
    Status status = getStatus();
    StringBuilder sb = new StringBuilder(status.toString());
    sb.append(": ");
    sb.append(this.seed);
    sb.append(" -> ");
    sb.append(this.tokens);
    if (status.equals(Status.ACCEPT)) {
      sb.append(" (");
      sb.append(getParseTrees().size());
      sb.append(')');
    } 
    return sb.toString();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\Parse.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */