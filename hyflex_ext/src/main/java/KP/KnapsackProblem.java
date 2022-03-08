package KP;

import KP.evalfs.MaxProfitPerPound;
import KP.evalfs.MinWeight;
import KP.heuristics.ConstructEmpty;
import KP.heuristics.IntersectCrossover;
import KP.heuristics.PerturbativeRuinRecreate;
import KP.heuristics.UnionCrossover;
import KP.modifiers.Insert;
import KP.modifiers.Remove;
import KP.modifiers.Swap;
import KP.parsers.CFGParserKP;
import hfu.BasicProblemDomain;
import hfu.BenchmarkInstance;
import hfu.Parser;
import hfu.heuristics.ConstructionHeuristic;
import hfu.heuristics.CrossoverHeuristic;
import hfu.heuristics.LocalSearchHeuristic;
import hfu.heuristics.ModifierFullLocalSearchHeuristic;
import hfu.heuristics.ModifierLocalSearchHeuristic;
import hfu.heuristics.ModifierMutationHeuristic;
import hfu.heuristics.MutationHeuristic;
import hfu.heuristics.RuinRecreateHeuristic;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.selector.SelectBest;
import hfu.heuristics.selector.SelectFirst;
import hfu.heuristics.selector.SelectRandom;
import hfu.heuristics.selector.Selector;
import hfu.heuristics.selector.eval.EvaluationFunction;

public class KnapsackProblem extends BasicProblemDomain<SolutionKP, InfoKP> {
  public KnapsackProblem(long seed) {
    super(seed);
  }
  
  public BenchmarkInstance<InfoKP>[] getBenchmarkInstances() {
    CFGParserKP cFGParserKP = new CFGParserKP();
    BenchmarkInstance[] benchmarks = { new BenchmarkInstance("instances/kp/0.kp", (Parser)cFGParserKP), 
        new BenchmarkInstance("instances/kp/1.kp", (Parser)cFGParserKP), 
        new BenchmarkInstance("instances/kp/2.kp", (Parser)cFGParserKP), 
        new BenchmarkInstance("instances/kp/3.kp", (Parser)cFGParserKP), 
        new BenchmarkInstance("instances/kp/4.kp", (Parser)cFGParserKP), 
        new BenchmarkInstance("instances/kp/5.kp", (Parser)cFGParserKP), 
        new BenchmarkInstance("instances/kp/6.kp", (Parser)cFGParserKP), 
        new BenchmarkInstance("instances/kp/7.kp", (Parser)cFGParserKP), 
        new BenchmarkInstance("instances/kp/8.kp", (Parser)cFGParserKP), 
        new BenchmarkInstance("instances/kp/9.kp", (Parser)cFGParserKP) };
    return (BenchmarkInstance<InfoKP>[])benchmarks;
  }
  
  public ConstructionHeuristic<SolutionKP, InfoKP> getConstructionHeuristic() {
    return (ConstructionHeuristic<SolutionKP, InfoKP>)new ConstructEmpty();
  }
  
  public LocalSearchHeuristic<SolutionKP, InfoKP>[] getLocalSearchHeuristics() {
    LocalSearchHeuristic[] llhs_ls = { (LocalSearchHeuristic)new ModifierLocalSearchHeuristic((Selector)new SelectFirst(), (PerturbativeModifier)new Insert()), 
        
        (LocalSearchHeuristic)new ModifierLocalSearchHeuristic((Selector)new SelectBest(false), (PerturbativeModifier)new Insert()), 
        
        (LocalSearchHeuristic)new ModifierFullLocalSearchHeuristic((Selector)new SelectFirst((EvaluationFunction)new MaxProfitPerPound()), (PerturbativeModifier)new Insert()), 
        
        (LocalSearchHeuristic)new ModifierLocalSearchHeuristic((Selector)new SelectBest(false, (EvaluationFunction)new MinWeight()), (PerturbativeModifier)new Insert()), 
        
        (LocalSearchHeuristic)new ModifierLocalSearchHeuristic((Selector)new SelectFirst(), (PerturbativeModifier)new Swap()), 
        
        (LocalSearchHeuristic)new ModifierLocalSearchHeuristic((Selector)new SelectBest(false), (PerturbativeModifier)new Swap()) };
    return (LocalSearchHeuristic<SolutionKP, InfoKP>[])llhs_ls;
  }
  
  public MutationHeuristic<SolutionKP, InfoKP>[] getMutationHeuristics() {
    MutationHeuristic[] llhs_mut = { (MutationHeuristic)new ModifierMutationHeuristic((Selector)new SelectRandom(), (PerturbativeModifier)new Swap()), 
        (MutationHeuristic)new ModifierMutationHeuristic((Selector)new SelectFirst((EvaluationFunction)new MaxProfitPerPound()), (PerturbativeModifier)new Swap()), 
        
        (MutationHeuristic)new ModifierMutationHeuristic((Selector)new SelectRandom(), (PerturbativeModifier)new Remove()), 
        
        (MutationHeuristic)new ModifierMutationHeuristic((Selector)new SelectBest(false), (PerturbativeModifier)new Remove()), 
        (MutationHeuristic)new ModifierMutationHeuristic((Selector)new SelectBest(false, (EvaluationFunction)new MinWeight()), (PerturbativeModifier)new Remove()) };
    return (MutationHeuristic<SolutionKP, InfoKP>[])llhs_mut;
  }
  
  public RuinRecreateHeuristic<SolutionKP, InfoKP>[] getRuinRecreateHeuristics() {
    RuinRecreateHeuristic[] llhs_rr = { (RuinRecreateHeuristic)new PerturbativeRuinRecreate((Selector)new SelectBest(false), (PerturbativeModifier)new Insert(), (Selector)new SelectRandom(), (PerturbativeModifier)new Remove()), 
        (RuinRecreateHeuristic)new PerturbativeRuinRecreate((Selector)new SelectBest(true, (EvaluationFunction)new MaxProfitPerPound()), (PerturbativeModifier)new Insert(), (Selector)new SelectRandom(), (PerturbativeModifier)new Remove()) };
    return (RuinRecreateHeuristic<SolutionKP, InfoKP>[])llhs_rr;
  }
  
  public String toString() {
    return "0-1 Knapsack Problem";
  }
  
  public CrossoverHeuristic<SolutionKP, InfoKP>[] getCrossoverHeuristics() {
    CrossoverHeuristic[] llhs_xo = { (CrossoverHeuristic)new IntersectCrossover(), 
        (CrossoverHeuristic)new UnionCrossover((Selector)new SelectFirst()), 
        (CrossoverHeuristic)new UnionCrossover((Selector)new SelectBest(false)) };
    return (CrossoverHeuristic<SolutionKP, InfoKP>[])llhs_xo;
  }
  
  public int solveExact() {
    int n = ((InfoKP)this.instance).getNitems();
    int[] v = new int[n];
    for (int i = 0; i < n; i++) {
      v[i] = ((InfoKP)this.instance).getProfit(i);
      if (v[i] < 1)
        System.out.println("error"); 
    } 
    int[] w = new int[n];
    for (int j = 0; j < n; j++) {
      w[j] = ((InfoKP)this.instance).getWeight(j);
      if (w[j] < 1)
        System.out.println("error"); 
    } 
    int W = ((InfoKP)this.instance).getCapacity();
    this.instance = null;
    int[] m = new int[W + 1];
    for (int k = 1; k <= n; k++) {
      for (int i1 = W; i1 >= 0; i1--) {
        if (w[k - 1] <= i1)
          m[i1] = Math.max(m[i1], m[i1 - w[k - 1]] + v[k - 1]); 
      } 
    } 
    return m[W];
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\KP\KnapsackProblem.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */