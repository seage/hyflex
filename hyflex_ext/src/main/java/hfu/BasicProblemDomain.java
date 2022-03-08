package hfu;

import AbstractClasses.ProblemDomain;
import hfu.heuristics.ConstructionHeuristic;
import hfu.heuristics.CrossoverHeuristic;
import hfu.heuristics.LocalSearchHeuristic;
import hfu.heuristics.MutationHeuristic;
import hfu.heuristics.PerturbationHeuristic;
import hfu.heuristics.RuinRecreateHeuristic;

public abstract class BasicProblemDomain<T extends BasicSolution<P>, P extends BenchmarkInfo> extends ProblemDomain {
  private final BenchmarkInstance<P>[] benchmarks;
  
  private final PerturbationHeuristic<T, P>[] llhs;
  
  private ConstructionHeuristic<T, P> init;
  
  private final int[] llhs_ls;
  
  private final int[] llhs_mut;
  
  private final int[] llhs_rc;
  
  private final int[] llhs_xo;
  
  private final int[] llhs_iom;
  
  private final int[] llhs_dof;
  
  protected P instance;
  
  private T[] c_memory;
  
  private T c_best;
  
  private Parameters params;
  
  public BasicProblemDomain(long seed) {
    super(seed);
    this.benchmarks = getBenchmarkInstances();
    this.init = getConstructionHeuristic();
    LocalSearchHeuristic[] ls = (LocalSearchHeuristic[])getLocalSearchHeuristics();
    MutationHeuristic[] mut = (MutationHeuristic[])getMutationHeuristics();
    RuinRecreateHeuristic[] rc = (RuinRecreateHeuristic[])getRuinRecreateHeuristics();
    CrossoverHeuristic[] xo = (CrossoverHeuristic[])getCrossoverHeuristics();
    this.llhs = (PerturbationHeuristic<T, P>[])new PerturbationHeuristic[ls.length + mut.length + rc.length + xo.length];
    this.llhs_ls = new int[ls.length];
    this.llhs_mut = new int[mut.length];
    this.llhs_rc = new int[rc.length];
    this.llhs_xo = new int[xo.length];
    int k = 0;
    int i;
    for (i = 0; i < ls.length; i++) {
      this.llhs[k] = (PerturbationHeuristic<T, P>)ls[i];
      this.llhs_ls[i] = k;
      k++;
    } 
    for (i = 0; i < mut.length; i++) {
      this.llhs[k] = (PerturbationHeuristic<T, P>)mut[i];
      this.llhs_mut[i] = k;
      k++;
    } 
    for (i = 0; i < rc.length; i++) {
      this.llhs[k] = (PerturbationHeuristic<T, P>)rc[i];
      this.llhs_rc[i] = k;
      k++;
    } 
    for (i = 0; i < xo.length; i++) {
      this.llhs[k] = (PerturbationHeuristic<T, P>)xo[i];
      this.llhs_xo[i] = k;
      k++;
    } 
    int n_iom = 0;
    int j;
    for (j = 0; j < this.llhs.length; j++)
      n_iom += this.llhs[j].usesIntensityOfMutation() ? 1 : 0; 
    this.llhs_iom = new int[n_iom];
    k = 0;
    for (j = 0; j < this.llhs.length; j++) {
      if (this.llhs[j].usesIntensityOfMutation()) {
        this.llhs_iom[k] = j;
        k++;
      } 
    } 
    int n_dof = 0;
    int m;
    for (m = 0; m < this.llhs.length; m++)
      n_dof += this.llhs[m].usesDepthOfSearch() ? 1 : 0; 
    this.llhs_dof = new int[n_dof];
    k = 0;
    for (m = 0; m < this.llhs.length; m++) {
      if (this.llhs[m].usesDepthOfSearch()) {
        this.llhs_dof[k] = m;
        k++;
      } 
    } 
  }
  
  public double applyHeuristic(int llh, int in, int out) {
    long startTime = System.currentTimeMillis();
    this.c_memory[out] = (T)this.llhs[llh].apply((BasicSolution)this.c_memory[in]);
    double e = this.c_memory[out].getFunctionValue();
    if (e < this.c_best.getFunctionValue())
      this.c_best = (T)this.c_memory[out].deepCopy(); 
    this.heuristicCallRecord[llh] = this.heuristicCallRecord[llh] + 1;
    this.heuristicCallTimeRecord[llh] = this.heuristicCallTimeRecord[llh] + (int)(System.currentTimeMillis() - startTime);
    return e;
  }
  
  public double applyHeuristic(int llh, int in1, int in2, int out) {
    long startTime = System.currentTimeMillis();
    if (llh < this.llhs.length - this.llhs_xo.length) {
      this.c_memory[out] = (T)this.llhs[llh].apply((BasicSolution)this.c_memory[in1]);
    } else {
      this.c_memory[out] = (T)((CrossoverHeuristic)this.llhs[llh]).apply((BasicSolution)this.c_memory[in1], (BasicSolution)this.c_memory[in2]);
    } 
    double e = this.c_memory[out].getFunctionValue();
    if (e < this.c_best.getFunctionValue())
      this.c_best = (T)this.c_memory[out].deepCopy(); 
    this.heuristicCallRecord[llh] = this.heuristicCallRecord[llh] + 1;
    this.heuristicCallTimeRecord[llh] = this.heuristicCallTimeRecord[llh] + (int)(System.currentTimeMillis() - startTime);
    return e;
  }
  
  public double getIntensityOfMutation() {
    return this.params.iom;
  }
  
  public double getDepthOfSearch() {
    return this.params.dos;
  }
  
  public void setDepthOfSearch(double dos) {
    if (this.params == null)
      this.params = new Parameters(); 
    this.params.setDOS(dos);
  }
  
  public void setIntensityOfMutation(double iom) {
    if (this.params == null)
      this.params = new Parameters(); 
    this.params.setIOM(iom);
  }
  
  public String bestSolutionToString() {
    return this.c_best.toText();
  }
  
  public boolean compareSolutions(int c1, int c2) {
    return this.c_memory[c1].isEqualTo((BasicSolution<BenchmarkInfo>)this.c_memory[c2]);
  }
  
  public void copySolution(int in, int out) {
    this.c_memory[out] = (T)this.c_memory[in].deepCopy();
  }
  
  public double getBestSolutionValue() {
    return (this.c_best == null) ? Double.MAX_VALUE : this.c_best.getFunctionValue();
  }
  
  public double getFunctionValue(int c) {
    return this.c_memory[c].getFunctionValue();
  }
  
  public int[] getHeuristicsOfType(ProblemDomain.HeuristicType t) {
    switch (t) {
      case LOCAL_SEARCH:
        return this.llhs_ls;
      case MUTATION:
        return this.llhs_mut;
      case RUIN_RECREATE:
        return this.llhs_rc;
      case null:
        return this.llhs_xo;
    } 
    return new int[0];
  }
  
  public int[] getHeuristicsThatUseDepthOfSearch() {
    return this.llhs_dof;
  }
  
  public int[] getHeuristicsThatUseIntensityOfMutation() {
    return this.llhs_iom;
  }
  
  public int getNumberOfHeuristics() {
    return (this.llhs == null) ? 20 : this.llhs.length;
  }
  
  public int getNumberOfInstances() {
    return this.benchmarks.length;
  }
  
  public void initialiseSolution(int out) {
    this.c_memory[out] = (T)this.init.apply();
    if (this.c_memory[out].getFunctionValue() < getBestSolutionValue())
      this.c_best = this.c_memory[out]; 
  }
  
  public void loadInstance(int pi) {
    this.heuristicCallRecord = new int[getNumberOfHeuristics()];
    this.heuristicCallTimeRecord = new int[getNumberOfHeuristics()];
    this.instance = this.benchmarks[pi].load();
    this.init.init((BenchmarkInfo)this.instance, this.params);
    for (int i = 0; i < this.llhs.length; i++)
      this.llhs[i].init((BenchmarkInfo)this.instance, this.params); 
  }
  
  public void setMemorySize(int n) {
    BasicSolution[] t_memory = new BasicSolution[n];
    int m = Math.min(n, (this.c_memory == null) ? 0 : this.c_memory.length);
    for (int i = 0; i < m; i++)
      t_memory[i] = (BasicSolution)this.c_memory[i]; 
    this.c_memory = (T[])t_memory;
  }
  
  public String solutionToString(int c) {
    return this.c_memory[c].toText();
  }
  
  public P getLoadedBenchmark() {
    return this.instance;
  }
  
  public abstract BenchmarkInstance<P>[] getBenchmarkInstances();
  
  public abstract ConstructionHeuristic<T, P> getConstructionHeuristic();
  
  public abstract LocalSearchHeuristic<T, P>[] getLocalSearchHeuristics();
  
  public abstract MutationHeuristic<T, P>[] getMutationHeuristics();
  
  public abstract RuinRecreateHeuristic<T, P>[] getRuinRecreateHeuristics();
  
  public abstract CrossoverHeuristic<T, P>[] getCrossoverHeuristics();
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\BasicProblemDomain.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */