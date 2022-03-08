package hfu.parsers.cfg.pep;

public enum ParserOption {
  IGNORE_TERMINAL_CASE(
    
    Boolean.FALSE),
  PREDICT_FOR_PRETERMINALS(
    
    Boolean.FALSE);
  
  final Boolean defaultValue;
  
  ParserOption(Boolean defaultValue) {
    this.defaultValue = defaultValue;
  }
  
  public Boolean getDefaultValue() {
    return this.defaultValue;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder(name());
    sb.append('=');
    sb.append(this.defaultValue.toString());
    return sb.toString();
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\ParserOption.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */