package KP.parsers;

import KP.InfoKP;
import hfu.parsers.LLCFGParser;
import hfu.parsers.cfg.EBNF;
import hfu.parsers.cfg.MyParseTree;

public class CFGParserKP extends LLCFGParser<InfoKP>{

	@Override
	public EBNF getEBNF() {
		EBNF ebnf = new EBNF();
		ebnf.addRule("[S]", "[Header] [Items]");
		ebnf.addRule("[Header]", "npieces: <npieces:int> \n capacity: <capacity:int> \n");
		ebnf.addRule("[Items]", "");
		ebnf.addRule("[Items]","[Item] \n [Items]");
		ebnf.addRule("[Item]","<id:int> <profit:int> <weight:int>");
		return ebnf;
	}

	@Override
	public InfoKP interpret(MyParseTree ptree) {
		int nitems = ptree.get("Header").get("npieces").asInteger();
		int capacity = ptree.get("Header").get("capacity").asInteger();
		int[] profits = new int[nitems];
		int[] weights = new int[nitems];
		MyParseTree items = ptree.get("Items");
		for(int i = 0; i < nitems;i++){
			MyParseTree item = items.get("Item");
			profits[i] = item.get("profit").asInteger();
			weights[i] = item.get("weight").asInteger();
			items = items.rNext();
		}
		return new InfoKP(capacity,profits,weights);
	}

}
