package hfu.parsers.cfg.pep;

import java.util.EventObject;

public abstract class ParserEvent extends EventObject {
  protected ParserEvent(EarleyParser earleyParser) {
    super(earleyParser);
  }
  
  public EarleyParser getEarleyParser() {
    return (EarleyParser)getSource();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\ParserEvent.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */