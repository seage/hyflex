package hfu.parsers;

import hfu.Parser;
import hfu.parsers.cfg.EBNF;
import hfu.parsers.cfg.MyParseTree;

public interface CFGParser<P extends hfu.BenchmarkInfo> extends Parser<P> {
  EBNF getEBNF();
  
  P interpret(MyParseTree paramMyParseTree);
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\CFGParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */