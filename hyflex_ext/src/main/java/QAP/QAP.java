package QAP;

import QAP.heuristics.BestSwap;
import QAP.heuristics.OrderedXO;
import QAP.heuristics.PartiallyMatchedXO;
import QAP.heuristics.RandomInit;
import QAP.heuristics.ReAssignGreedyF;
import QAP.heuristics.ReAssignGreedyL;
import QAP.heuristics.ReAssignRandom;
import QAP.parsers.CFGParserQAP;
import hfu.BasicProblemDomain;
import hfu.BenchmarkInstance;
import hfu.Parser;
import hfu.heuristics.ConstructionHeuristic;
import hfu.heuristics.CrossoverHeuristic;
import hfu.heuristics.LocalSearchHeuristic;
import hfu.heuristics.ModifierFullLocalSearchHeuristic;
import hfu.heuristics.ModifierMutationHeuristic;
import hfu.heuristics.MutationHeuristic;
import hfu.heuristics.RuinRecreateHeuristic;
import hfu.heuristics.selector.SelectBest;
import hfu.heuristics.selector.SelectFirst;
import hfu.heuristics.selector.SelectRandom;
import hfu.heuristics.selector.Selector;

public class QAP extends BasicProblemDomain<SolutionQAP, InfoQAP> {
  public QAP(long seed) {
    super(seed);
  }
  
  public BenchmarkInstance<InfoQAP>[] getBenchmarkInstances() {
    CFGParserQAP cFGParserQAP = new CFGParserQAP();
    BenchmarkInstance[] benchmarks = { new BenchmarkInstance("instances/qap/sko100a.flp", (Parser)cFGParserQAP), 
        new BenchmarkInstance("instances/qap/sko100b.flp", (Parser)cFGParserQAP), 
        new BenchmarkInstance("instances/qap/sko100c.flp", (Parser)cFGParserQAP), 
        new BenchmarkInstance("instances/qap/sko100d.flp", (Parser)cFGParserQAP), 
        new BenchmarkInstance("instances/qap/tai100a.flp", (Parser)cFGParserQAP), 
        new BenchmarkInstance("instances/qap/tai100b.flp", (Parser)cFGParserQAP), 
        new BenchmarkInstance("instances/qap/tai150b.flp", (Parser)cFGParserQAP), 
        new BenchmarkInstance("instances/qap/tai256c.flp", (Parser)cFGParserQAP), 
        new BenchmarkInstance("instances/qap/tho150.flp", (Parser)cFGParserQAP), 
        new BenchmarkInstance("instances/qap/wil100.flp", (Parser)cFGParserQAP) };
    return (BenchmarkInstance<InfoQAP>[])benchmarks;
  }
  
  public ConstructionHeuristic<SolutionQAP, InfoQAP> getConstructionHeuristic() {
    return (ConstructionHeuristic<SolutionQAP, InfoQAP>)new RandomInit();
  }
  
  public LocalSearchHeuristic<SolutionQAP, InfoQAP>[] getLocalSearchHeuristics() {
    LocalSearchHeuristic[] llhs_ls = { (LocalSearchHeuristic)new ModifierFullLocalSearchHeuristic((Selector)new SelectFirst(), new Swap(), false), 
        (LocalSearchHeuristic)new ModifierFullLocalSearchHeuristic((Selector)new SelectBest(false), new Swap(), false) };
    return (LocalSearchHeuristic<SolutionQAP, InfoQAP>[])llhs_ls;
  }
  
  public MutationHeuristic<SolutionQAP, InfoQAP>[] getMutationHeuristics() {
    MutationHeuristic[] llhs_mut = { (MutationHeuristic)new ModifierMutationHeuristic((Selector)new SelectRandom(), new Swap()), 
        (MutationHeuristic)new BestSwap() };
    return (MutationHeuristic<SolutionQAP, InfoQAP>[])llhs_mut;
  }
  
  public RuinRecreateHeuristic<SolutionQAP, InfoQAP>[] getRuinRecreateHeuristics() {
    RuinRecreateHeuristic[] llhs_rr = { (RuinRecreateHeuristic)new ReAssignRandom(), 
        (RuinRecreateHeuristic)new ReAssignGreedyF(), 
        (RuinRecreateHeuristic)new ReAssignGreedyL() };
    return (RuinRecreateHeuristic<SolutionQAP, InfoQAP>[])llhs_rr;
  }
  
  public String toString() {
    return "Quadratic Assignment Problem";
  }
  
  public CrossoverHeuristic<SolutionQAP, InfoQAP>[] getCrossoverHeuristics() {
    CrossoverHeuristic[] llhs_xo = { (CrossoverHeuristic)new PartiallyMatchedXO(), 
        (CrossoverHeuristic)new OrderedXO() };
    return (CrossoverHeuristic<SolutionQAP, InfoQAP>[])llhs_xo;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\QAP\QAP.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */