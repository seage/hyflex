package hfu.parsers.cfg.pep;

public class ParserOptionEvent extends ParserEvent {
  private static final long serialVersionUID = 1L;
  
  ParserOption option;
  
  Boolean value;
  
  ParserOptionEvent(EarleyParser earleyParser, ParserOption option, Boolean value) {
    super(earleyParser);
    this.option = option;
    this.value = value;
  }
  
  public ParserOption getOption() {
    return this.option;
  }
  
  public Boolean getValue() {
    return this.value;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\ParserOptionEvent.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */