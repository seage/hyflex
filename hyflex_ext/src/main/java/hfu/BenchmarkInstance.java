package hfu;

public class BenchmarkInstance<P extends BenchmarkInfo> {
  String file;
  
  Parser<P> parser;
  
  public BenchmarkInstance(String file, Parser<P> parser) {
    this.file = file;
    this.parser = parser;
  }
  
  P load() {
    return this.parser.parse(BenchmarkInstance.class.getClassLoader().getResourceAsStream(this.file));
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\BenchmarkInstance.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */