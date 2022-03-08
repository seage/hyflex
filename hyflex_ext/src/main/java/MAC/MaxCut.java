package MAC;

import MAC.heuristics.MultiParentCrossover;
import MAC.heuristics.OnePointCrossover;
import MAC.heuristics.RandomizedNEH;
import MAC.modifiers.Insert;
import MAC.modifiers.Remove;
import MAC.modifiers.RemoveRadial;
import MAC.modifiers.Swap;
import MAC.modifiers.SwapNeighbours;
import MAC.parsers.CFGParserMAC;
import hfu.BasicProblemDomain;
import hfu.BenchmarkInstance;
import hfu.Parser;
import hfu.heuristics.ConstructionHeuristic;
import hfu.heuristics.CrossoverHeuristic;
import hfu.heuristics.LocalSearchHeuristic;
import hfu.heuristics.ModifierFullLocalSearchHeuristic;
import hfu.heuristics.ModifierLocalSearchHeuristic;
import hfu.heuristics.ModifierMutationHeuristic;
import hfu.heuristics.ModifierRuinRecreateHeuristic;
import hfu.heuristics.MutationHeuristic;
import hfu.heuristics.RuinRecreateHeuristic;
import hfu.heuristics.modifiers.ConstructiveModifier;
import hfu.heuristics.modifiers.DestructiveModifier;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.selector.SelectBest;
import hfu.heuristics.selector.SelectFirst;
import hfu.heuristics.selector.SelectRandom;
import hfu.heuristics.selector.Selector;

public class MaxCut extends BasicProblemDomain<SolutionMAC, InfoMAC> {
  public MaxCut(long seed) {
    super(seed);
  }
  
  public BenchmarkInstance<InfoMAC>[] getBenchmarkInstances() {
    CFGParserMAC cFGParserMAC = new CFGParserMAC();
    BenchmarkInstance[] benchmarks = { new BenchmarkInstance("instances/mac/g3-8.txt", (Parser)cFGParserMAC), 
        new BenchmarkInstance("instances/mac/g3-15.txt", (Parser)cFGParserMAC), 
        new BenchmarkInstance("instances/mac/g14.rud", (Parser)cFGParserMAC), 
        new BenchmarkInstance("instances/mac/g15.rud", (Parser)cFGParserMAC), 
        new BenchmarkInstance("instances/mac/g16.rud", (Parser)cFGParserMAC), 
        new BenchmarkInstance("instances/mac/g22.rud", (Parser)cFGParserMAC), 
        new BenchmarkInstance("instances/mac/g34.rud", (Parser)cFGParserMAC), 
        new BenchmarkInstance("instances/mac/g55.rud", (Parser)cFGParserMAC), 
        new BenchmarkInstance("instances/mac/pm3-8-50.txt", (Parser)cFGParserMAC), 
        new BenchmarkInstance("instances/mac/pm3-15-50.txt", (Parser)cFGParserMAC) };
    return (BenchmarkInstance<InfoMAC>[])benchmarks;
  }
  
  public ConstructionHeuristic<SolutionMAC, InfoMAC> getConstructionHeuristic() {
    return (ConstructionHeuristic<SolutionMAC, InfoMAC>)new RandomizedNEH();
  }
  
  public LocalSearchHeuristic<SolutionMAC, InfoMAC>[] getLocalSearchHeuristics() {
    LocalSearchHeuristic[] llhs_ls = { (LocalSearchHeuristic)new ModifierFullLocalSearchHeuristic((Selector)new SelectFirst(), (PerturbativeModifier)new Swap(), true), 
        (LocalSearchHeuristic)new ModifierFullLocalSearchHeuristic((Selector)new SelectBest(false), (PerturbativeModifier)new Swap(), true), 
        (LocalSearchHeuristic)new ModifierLocalSearchHeuristic((Selector)new SelectBest(false), (PerturbativeModifier)new SwapNeighbours()) };
    return (LocalSearchHeuristic<SolutionMAC, InfoMAC>[])llhs_ls;
  }
  
  public MutationHeuristic<SolutionMAC, InfoMAC>[] getMutationHeuristics() {
    MutationHeuristic[] llhs_mut = { (MutationHeuristic)new ModifierMutationHeuristic((Selector)new SelectRandom(), (PerturbativeModifier)new Swap()), 
        (MutationHeuristic)new ModifierMutationHeuristic((Selector)new SelectRandom(), (PerturbativeModifier)new SwapNeighbours()) };
    return (MutationHeuristic<SolutionMAC, InfoMAC>[])llhs_mut;
  }
  
  public RuinRecreateHeuristic<SolutionMAC, InfoMAC>[] getRuinRecreateHeuristics() {
    RuinRecreateHeuristic[] llhs_rr = { (RuinRecreateHeuristic)new ModifierRuinRecreateHeuristic((Selector)new SelectRandom(), (ConstructiveModifier)new Insert(), (Selector)new SelectRandom(), (DestructiveModifier)new Remove()), 
        
        (RuinRecreateHeuristic)new ModifierRuinRecreateHeuristic((Selector)new SelectBest(true), (ConstructiveModifier)new Insert(), (Selector)new SelectRandom(), (DestructiveModifier)new Remove()), 
        
        (RuinRecreateHeuristic)new ModifierRuinRecreateHeuristic((Selector)new SelectBest(true), (ConstructiveModifier)new Insert(), (Selector)new SelectRandom(), (DestructiveModifier)new RemoveRadial()) };
    return (RuinRecreateHeuristic<SolutionMAC, InfoMAC>[])llhs_rr;
  }
  
  public String toString() {
    return "Max-Cut Problem";
  }
  
  public CrossoverHeuristic<SolutionMAC, InfoMAC>[] getCrossoverHeuristics() {
    CrossoverHeuristic[] llhs_xo = { (CrossoverHeuristic)new OnePointCrossover(), 
        (CrossoverHeuristic)new MultiParentCrossover() };
    return (CrossoverHeuristic<SolutionMAC, InfoMAC>[])llhs_xo;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\MaxCut.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */