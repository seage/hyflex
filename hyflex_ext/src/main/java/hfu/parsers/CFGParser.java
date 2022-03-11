package hfu.parsers;

import hfu.BenchmarkInfo;
import hfu.Parser;
import hfu.parsers.cfg.EBNF;
import hfu.parsers.cfg.MyParseTree;

abstract public interface CFGParser<P extends BenchmarkInfo> extends Parser<P>{
	public EBNF getEBNF();
	public P interpret(MyParseTree ptree);

}
