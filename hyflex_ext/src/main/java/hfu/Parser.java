package hfu;

import java.io.InputStream;

public interface Parser<P extends BenchmarkInfo> {
  P parse(InputStream paramInputStream);
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\Parser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */