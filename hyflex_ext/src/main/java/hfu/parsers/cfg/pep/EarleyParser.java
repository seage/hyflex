package hfu.parsers.cfg.pep;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class EarleyParser {
  Grammar grammar;
  
  ParserListener listener;
  
  Map<ParserOption, Boolean> options;
  
  private boolean predictPreterm;
  
  private boolean ignoreCase;
  
  public EarleyParser(Grammar grammar) {
    this(grammar, null);
  }
  
  public EarleyParser(Grammar grammar, ParserListener listener) {
    setGrammar(grammar);
    setListener(listener);
  }
  
  public Grammar getGrammar() {
    return this.grammar;
  }
  
  public void setGrammar(Grammar grammar) {
    if (grammar == null)
      throw new IllegalArgumentException("null grammar"); 
    Grammar grammar2 = new Grammar(grammar.name);
    for (Rule r : grammar.getAllRules()) {
      Category[] right_cs = r.getRight();
      if (right_cs.length > 1 && r.isPreterminal())
        for (int i = 0; i < right_cs.length; i++) {
          if (right_cs[i].isTerminal()) {
            Category dummy = new Category("§§" + right_cs[i].getName() + "§§");
            grammar2.addRule(new Rule(dummy, new Category[] { right_cs[i] }));
            right_cs[i] = dummy;
          } 
        }  
      grammar2.addRule(new Rule(r.getLeft(), right_cs));
    } 
    this.grammar = grammar2;
  }
  
  public ParserListener getListener() {
    return this.listener;
  }
  
  public void setListener(ParserListener listener) {
    this.listener = listener;
  }
  
  public boolean containsOption(ParserOption optionName) {
    return (this.options != null && this.options.containsKey(optionName));
  }
  
  public Boolean getOption(ParserOption optionName) {
    if (this.options == null)
      return optionName.defaultValue; 
    Boolean o = this.options.get(optionName);
    return (o == null) ? optionName.defaultValue : o;
  }
  
  public Boolean setOption(ParserOption optionName, Boolean value) {
    if (optionName == null)
      throw new IllegalArgumentException("null option name"); 
    if (value == null)
      throw new IllegalArgumentException("null value"); 
    if (this.options == null)
      this.options = new EnumMap<>(
          ParserOption.class); 
    Boolean oldValue = this.options.put(optionName, value);
    if (oldValue == null)
      oldValue = optionName.defaultValue; 
    if (!value.equals(oldValue))
      fireOptionSet(optionName, value); 
    return oldValue;
  }
  
  public Status recognize(String tokens, Category seed) throws PepException {
    return recognize(Arrays.asList(tokens.split(" ")), seed);
  }
  
  public Status recognize(String tokens, String separator, Category seed) throws PepException {
    return recognize(Arrays.asList(tokens.split(separator)), seed);
  }
  
  public Status recognize(Iterable<String> tokens, Category seed) throws PepException {
    return parse(tokens, seed).getStatus();
  }
  
  public Parse parse(String tokens, Category seed) throws PepException {
    return parse(Arrays.asList(tokens.split(" ")), seed);
  }
  
  public Parse parse(String tokens, String separator, Category seed) throws PepException {
    return parse(Arrays.asList(tokens.split(separator)), seed);
  }
  
  public Parse parse(Iterable<String> tokens, Category seed) throws PepException {
    Chart chart = new Chart();
    Integer index = new Integer(0);
    Parse parse = new Parse(seed, chart);
    if (seed == null) {
      fireParseError(parse, index, "invalid seed category: " + seed);
    } else if (tokens == null || !tokens.iterator().hasNext()) {
      fireParseError(parse, index, "null or empty tokens");
    } else {
      this.predictPreterm = getOption(ParserOption.PREDICT_FOR_PRETERMINALS).booleanValue();
      this.ignoreCase = getOption(ParserOption.IGNORE_TERMINAL_CASE).booleanValue();
      if (!this.predictPreterm)
        for (Rule r : this.grammar.getAllRules()) {
          if (r.isPreterminal() && r.right.length > 1) {
            this.predictPreterm = true;
            fireParseMessage(parse, "setting " + 
                ParserOption.PREDICT_FOR_PRETERMINALS.name() + " to true;" + 
                " grammar contains incompatible rule: " + r);
            break;
          } 
        }  
      Iterator<String> tokenIterator = tokens.iterator();
      Edge seedEdge = new Edge(DottedRule.startRule(seed), index.intValue());
      chart.addEdge(index, seedEdge);
      fireParserSeeded(index, seedEdge);
      while (tokenIterator.hasNext()) {
        try {
          if (index.intValue() % 1000 == 0)
            System.out.println(index); 
          predict(chart, index);
          String token = tokenIterator.next();
          parse.tokens.add(token);
          index = Integer.valueOf(index.intValue() + 1);
          scan(chart, index, token);
          complete(chart, index);
          predict(chart, index);
          complete(chart, index);
        } catch (PepException pe) {
          fireParseError(parse, index, pe);
        } 
      } 
    } 
    fireParseComplete(parse);
    return parse;
  }
  
  void predict(Chart chart, Integer index) {
    if (chart.containsEdges(index)) {
      Set<Edge> edges = chart.getEdges(index);
      byte b;
      int i;
      Edge[] arrayOfEdge;
      for (i = (arrayOfEdge = edges.<Edge>toArray(new Edge[edges.size()])).length, b = 0; b < i; ) {
        Edge edge = arrayOfEdge[b];
        predictForEdge(chart, edge, index);
        b++;
      } 
    } 
  }
  
  void predictForEdge(Chart chart, Edge edge, Integer index) {
    Category active = edge.dottedRule.activeCategory;
    if (active != null && this.grammar.containsRules(active)) {
      if (this.grammar.isNullable(active)) {
        Edge newEdge = new Edge(new DottedRule(new Rule(edge.dottedRule.left, edge.dottedRule.right), edge.dottedRule.getPosition() + 1), edge.origin);
        if (chart.addEdge(index, newEdge)) {
          fireEdgePredicted(index, newEdge);
          predictForEdge(chart, newEdge, index);
        } 
      } 
      for (Rule rule : this.grammar.getRules(active)) {
        if (!this.predictPreterm && rule.isPreterminal())
          continue; 
        Edge newEdge = Edge.predictFor(rule, index.intValue());
        if (chart.addEdge(index, newEdge)) {
          fireEdgePredicted(index, newEdge);
          predictForEdge(chart, newEdge, index);
        } 
      } 
    } 
  }
  
  void scan(Chart chart, Integer index, String token) throws PepException {
    if (token == null)
      throw new PepException("null token at index " + index); 
    if (chart.containsEdges(index)) {
      Set<Edge> edges = chart.getEdges(index);
      if (!this.predictPreterm) {
        byte b1;
        int j;
        Edge[] arrayOfEdge1;
        for (j = (arrayOfEdge1 = edges.<Edge>toArray(new Edge[edges.size()])).length, b1 = 0; b1 < j; ) {
          Edge edge = arrayOfEdge1[b1];
          if (!edge.isPassive()) {
            Rule r = this.grammar.getSingletonPreterminal(
                edge.dottedRule.activeCategory, token, this.ignoreCase);
            if (r != null) {
              Edge pt = Edge.predictFor(r, index.intValue());
              if (chart.addEdge(index, pt))
                fireEdgePredicted(index, pt); 
            } 
          } 
          b1++;
        } 
      } 
      byte b;
      int i;
      Edge[] arrayOfEdge;
      for (i = (arrayOfEdge = edges.<Edge>toArray(new Edge[edges.size()])).length, b = 0; b < i; ) {
        Edge edge = arrayOfEdge[b];
        if (!edge.isPassive()) {
          DottedRule dr = edge.dottedRule;
          if (dr.activeCategory.terminal && extended_equals(dr.activeCategory.name, token)) {
            Edge newEdge = Edge.scan(edge, token);
            Integer successor = 
              new Integer(index.intValue() + 1);
            if (chart.addEdge(successor, newEdge))
              fireEdgeScanned(successor, newEdge); 
          } 
        } 
        b++;
      } 
    } 
  }
  
  private boolean extended_equals(String terminal, String token) {
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
        return true; 
      System.out.println("ERROR: Unknown type: " + split[1]);
      return false;
    } 
    return !(!terminal.equals(token) && (!this.ignoreCase || !terminal.equalsIgnoreCase(token)));
  }
  
  void complete(Chart chart, Integer index) {
    if (chart.containsEdges(index)) {
      Set<Edge> edges = chart.getEdges(index);
      byte b;
      int i;
      Edge[] arrayOfEdge;
      for (i = (arrayOfEdge = edges.<Edge>toArray(new Edge[edges.size()])).length, b = 0; b < i; ) {
        Edge edge = arrayOfEdge[b];
        completeForEdge(chart, edge, index);
        b++;
      } 
    } 
  }
  
  void completeForEdge(Chart chart, Edge edge, Integer index) {
    Integer eo = new Integer(edge.origin);
    if (edge.isPassive() && chart.containsEdges(eo))
      for (Edge originEdge : chart.getEdges(eo)) {
        if (!originEdge.isPassive() && 
          originEdge.dottedRule.activeCategory.equals(
            edge.dottedRule.left)) {
          Edge newEdge = Edge.complete(originEdge, edge);
          if (chart.addEdge(index, newEdge)) {
            fireEdgeCompleted(index, newEdge);
            completeForEdge(chart, newEdge, index);
          } 
        } 
      }  
  }
  
  public String toString() {
    return "[" + getClass().getSimpleName() + ": grammar " + 
      this.grammar.name + "]";
  }
  
  private void fireOptionSet(ParserOption option, Boolean value) {
    if (this.listener != null)
      this.listener.optionSet(
          new ParserOptionEvent(this, option, value)); 
  }
  
  private void fireParserSeeded(Integer index, Edge edge) {
    if (this.listener != null)
      this.listener.parserSeeded(new EdgeEvent(this, index, edge)); 
  }
  
  private void fireEdgePredicted(Integer index, Edge edge) {
    if (this.listener != null)
      this.listener.edgePredicted(new EdgeEvent(this, index, edge)); 
  }
  
  private void fireEdgeScanned(Integer index, Edge edge) {
    if (this.listener != null)
      this.listener.edgeScanned(new EdgeEvent(this, index, edge)); 
  }
  
  private void fireEdgeCompleted(Integer index, Edge edge) {
    if (this.listener != null)
      this.listener.edgeCompleted(new EdgeEvent(this, index, edge)); 
  }
  
  private void fireParseComplete(Parse parse) {
    if (this.listener != null)
      this.listener.parseComplete(new ParseEvent(this, parse)); 
  }
  
  private void fireParseMessage(Parse parse, String message) {
    if (this.listener != null)
      this.listener.parseMessage(new ParseEvent(this, parse), message); 
  }
  
  private void fireParseError(Parse parse, Integer index, String message) throws PepException {
    fireParseError(parse, index, new PepException(message));
  }
  
  private void fireParseError(Parse parse, Integer index, PepException cause) throws PepException {
    parse.error = true;
    if (this.listener == null)
      throw cause; 
    this.listener.parseError(new ParseErrorEvent(this, index, parse, cause));
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\EarleyParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */