package MAC.parsers;

import MAC.InfoMAC;
import hfu.BenchmarkInfo;
import hfu.datastructures.AdjecencyList;
import hfu.parsers.LLCFGParser;
import hfu.parsers.cfg.EBNF;
import hfu.parsers.cfg.MyParseTree;

public class CFGParserMAC extends LLCFGParser<InfoMAC> {
  public EBNF getEBNF() {
    EBNF ebnf = new EBNF();
    ebnf.addRule("[S]", "[Header] [Edges]");
    ebnf.addRule("[Header]", "<nvertices:int> <nedges:int> \n");
    ebnf.addRule("[Edges]", "[Edge] [Edges]");
    ebnf.addRule("[Edges]", "");
    ebnf.addRule("[Edge]", "<v1:int> <v2:int> <w:int> \n");
    return ebnf;
  }
  
  public InfoMAC interpret(MyParseTree ptree) {
    int nvertices = ptree.get("Header").get("nvertices").asInteger();
    int nedges = ptree.get("Header").get("nedges").asInteger();
    AdjecencyList G = new AdjecencyList(nvertices);
    MyParseTree edges = ptree.get("Edges");
    for (int i = 0; i < nedges; i++) {
      MyParseTree edge = edges.get("Edge");
      G.addEdge(edge.get("v1").asInteger(), edge.get("v2").asInteger(), edge.get("w").asInteger());
      edges = edges.rNext();
    } 
    return new InfoMAC(G);
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\parsers\CFGParserMAC.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */