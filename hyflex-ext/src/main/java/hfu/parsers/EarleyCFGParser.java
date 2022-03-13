package hfu.parsers;

import java.util.Set;

import hfu.BenchmarkInfo;
import hfu.Parser;
import hfu.parsers.cfg.EBNF;
import hfu.parsers.cfg.MyParseTree;
import hfu.parsers.cfg.MyTokenizer;
import hfu.parsers.cfg.pep.Category;
import hfu.parsers.cfg.pep.EarleyParser;
import hfu.parsers.cfg.pep.Grammar;
import hfu.parsers.cfg.pep.LLParser;
import hfu.parsers.cfg.pep.Parse;
import hfu.parsers.cfg.pep.ParseTree;
import hfu.parsers.cfg.pep.PepException;
import hfu.parsers.cfg.pep.Rule;

abstract public class EarleyCFGParser<P extends BenchmarkInfo> implements CFGParser<P>{

	public P parse(String file) {
		//get EBNF description of the file to parse
		EBNF ebnf = getEBNF();
		//create a token stream for the input file
		MyTokenizer tokenizer = new MyTokenizer(file);
		//actual parsing using the Early Parser (PEP library)...
		EarleyParser parser = new EarleyParser(ebnf.getGrammar());
		Parse result = null;
		try {
			result = parser.parse(tokenizer, new Category("S"));
		} catch (PepException e) {
			e.printStackTrace();
		}
		System.out.println(result.getStatus());
		Set<ParseTree> forest = result.getParseTrees();
		return interpret(new MyParseTree(forest.iterator().next(), result.getTokens().toArray(new String[0])));
	}


}
