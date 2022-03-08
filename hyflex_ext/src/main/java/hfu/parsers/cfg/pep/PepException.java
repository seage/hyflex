package hfu.parsers.cfg.pep;

public class PepException extends Exception {
  private static final long serialVersionUID = 1L;
  
  PepException(String message) {
    super(message);
  }
  
  PepException(Throwable cause) {
    super(cause);
  }
  
  PepException(String message, Throwable cause) {
    super(message, cause);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\cfg\pep\PepException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */