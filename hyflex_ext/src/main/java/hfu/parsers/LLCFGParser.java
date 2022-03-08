package hfu.parsers;

import hfu.BenchmarkInfo;
import hfu.parsers.cfg.EBNF;
import hfu.parsers.cfg.MyParseTree;
import hfu.parsers.cfg.MyTokenizer;
import hfu.parsers.cfg.pep.Category;
import hfu.parsers.cfg.pep.LLParser;
import java.io.InputStream;

public abstract class LLCFGParser<P extends BenchmarkInfo> implements CFGParser<P> {
  public P parse(InputStream in) {
    EBNF ebnf = getEBNF();
    MyTokenizer tokenizer = new MyTokenizer(in);
    LLParser parser = new LLParser(ebnf.getGrammar());
    LLParser.ParseTree tree = parser.parse(tokenizer, new Category("S"));
    return interpret(MyParseTree.produce(tree));
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\parsers\LLCFGParser.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */