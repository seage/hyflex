package hfu.parsers.cfg.pep;

public class EdgeEvent extends ParserEvent {
  private static final long serialVersionUID = 1L;
  
  Edge edge;
  
  Integer index;
  
  EdgeEvent(EarleyParser earleyParser, Integer index, Edge edge) {
    super(earleyParser);
    this.edge = edge;
    this.index = index;
  }
  
  public Edge getEdge() {
    return this.edge;
  }
  
  public Integer getIndex() {
    return this.index;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\EdgeEvent.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */