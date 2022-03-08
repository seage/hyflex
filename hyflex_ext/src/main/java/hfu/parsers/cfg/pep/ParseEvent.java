package hfu.parsers.cfg.pep;

public class ParseEvent extends ParserEvent {
  private static final long serialVersionUID = 1L;
  
  Parse parse;
  
  Integer index;
  
  ParseEvent(EarleyParser earleyParser, Parse parse) {
    this(earleyParser, Integer.valueOf(parse.tokens.size()), parse);
  }
  
  protected ParseEvent(EarleyParser earleyParser, Integer index, Parse parse) {
    super(earleyParser);
    this.parse = parse;
    this.index = index;
  }
  
  public Parse getParse() {
    return this.parse;
  }
  
  public Integer getIndex() {
    return this.index;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\ParseEvent.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */