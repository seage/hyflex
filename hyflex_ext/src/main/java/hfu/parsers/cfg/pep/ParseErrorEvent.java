package hfu.parsers.cfg.pep;

public class ParseErrorEvent extends ParseEvent {
  private static final long serialVersionUID = 1L;
  
  PepException cause;
  
  ParseErrorEvent(EarleyParser earleyParser, Integer index, Parse parse, PepException cause) {
    super(earleyParser, index, parse);
    this.cause = cause;
  }
  
  public PepException getCause() {
    return this.cause;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\ParseErrorEvent.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */