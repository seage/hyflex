package QAP.parsers;

import QAP.InfoQAP;
import hfu.datastructures.AdjecencyList;
import hfu.parsers.LLCFGParser;
import hfu.parsers.cfg.EBNF;
import hfu.parsers.cfg.MyParseTree;

public class CFGParserQAP extends LLCFGParser<InfoQAP>{

	@Override
	public EBNF getEBNF() {
		EBNF ebnf = new EBNF();
		ebnf.addRule("[S]", "[Name] [Comments] [N] [Flow] [Distance]");
		ebnf.addRule("[Name]", "NAME : <name:string> \n");
		ebnf.addRule("[Comments]", "");
		ebnf.addRule("[Comments]", "COMMENT : [Text] \n [Comments]");
		ebnf.addRule("[N]", "N : <n:int> \n");
		ebnf.addRule("[Flow]", "FLOWS_BETWEEN_FACILITIES \n [Flows]");
		ebnf.addRule("[Flows]", "");
		ebnf.addRule("[Flows]", "[Flowl] \n [Flows]");
		ebnf.addRule("[Flowl]", "");
		ebnf.addRule("[Flowl]", "<f:int> [Flowl]");
		ebnf.addRule("[Distance]", "DISTANCES_BETWEEN_LOCATIONS \n [Dists]");
		ebnf.addRule("[Dists]", "EOF \n");
		ebnf.addRule("[Dists]", "[Distl] \n [Dists]");
		ebnf.addRule("[Distl]", "");
		ebnf.addRule("[Distl]", "<d:int> [Distl]");
		ebnf.addRule("[Text]","");
		ebnf.addRule("[Text]","<word:string> [Text]");
		return ebnf;
	}

	@Override
	public InfoQAP interpret(MyParseTree ptree) {
		int n = ptree.get("N").get("n").asInteger();
		int[][] flow = new int[n][n];
		MyParseTree tflows = ptree.get("Flow").get("Flows");
		int k = 0;
		while(tflows.get("Flowl") != null){
			MyParseTree tflowl = tflows.get("Flowl");
			while(tflowl.get("f") != null){
				flow[k/n][k%n] = tflowl.get("f").asInteger();
				k++;
				tflowl = tflowl.rNext();
			}
			tflows = tflows.rNext();
		}
		int[][] dist = new int[n][n];
		MyParseTree tdists = ptree.get("Distance").get("Dists");
		k = 0;
		while(tdists.get("Distl") != null){
			MyParseTree tdistl = tdists.get("Distl");
			while(tdistl.get("d") != null){
				dist[k/n][k%n] = tdistl.get("d").asInteger();
				k++;
				tdistl = tdistl.rNext();
			}
			tdists = tdists.rNext();
		}
		return new InfoQAP(dist,flow);
	}

}
