package hfu.parsers;

import hfu.BenchmarkInfo;
import hfu.parsers.cfg.EBNF;
import hfu.parsers.cfg.MyParseTree;
import hfu.parsers.cfg.pep.ParseTree;

public class MyCFGParser extends LLCFGParser{

	@Override
	public EBNF getEBNF() {
		EBNF ebnf = new EBNF();
		ebnf.addRule("[S]", "[F]");
		ebnf.addRule("[S]","( [S] + [F] ) \n");
		ebnf.addRule("[F]","<a:int>");
		return ebnf;
	}

	@Override
	public BenchmarkInfo interpret(MyParseTree ptree) {
		return null;
	}

}
