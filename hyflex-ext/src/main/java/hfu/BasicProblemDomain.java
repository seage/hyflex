package hfu;

import hfu.heuristics.ConstructionHeuristic;
import hfu.heuristics.CrossoverHeuristic;
import hfu.heuristics.LocalSearchHeuristic;
import hfu.heuristics.MutationHeuristic;
import hfu.heuristics.PerturbationHeuristic;
import hfu.heuristics.RuinRecreateHeuristic;
import AbstractClasses.ProblemDomain;


abstract public class BasicProblemDomain<T extends BasicSolution<P>, P extends BenchmarkInfo> extends ProblemDomain{
	final private BenchmarkInstance<P>[] benchmarks;
	final private PerturbationHeuristic<T,P>[] llhs;
	private ConstructionHeuristic<T,P> init;
	final private int[] llhs_ls;
	final private int[] llhs_mut;
	final private int[] llhs_rc;
	final private int[] llhs_xo;
	final private int[] llhs_iom;
	final private int[] llhs_dof;
	
	
	
	protected P instance;
	private T[] c_memory;
	private T c_best;
	private Parameters params;
	
	abstract public BenchmarkInstance<P>[] getBenchmarkInstances();
	abstract public ConstructionHeuristic<T,P> getConstructionHeuristic();
	abstract public LocalSearchHeuristic<T,P>[] getLocalSearchHeuristics();
	abstract public MutationHeuristic<T,P>[] getMutationHeuristics();
	abstract public RuinRecreateHeuristic<T,P>[] getRuinRecreateHeuristics();
	abstract public CrossoverHeuristic<T,P>[] getCrossoverHeuristics();
	
	@SuppressWarnings("unchecked")
	public BasicProblemDomain(long seed) {
		super(seed);
		//initialize the benchmarks in this domain
		benchmarks = getBenchmarkInstances();
		//initialize the heuristics
		init = getConstructionHeuristic();
		//perturbation (low level) heuristics
		LocalSearchHeuristic<T,P>[] ls = getLocalSearchHeuristics();
		MutationHeuristic<T,P>[] mut = getMutationHeuristics();
		RuinRecreateHeuristic<T,P>[] rc = getRuinRecreateHeuristics();
		CrossoverHeuristic<T,P>[] xo = getCrossoverHeuristics();
		llhs = (PerturbationHeuristic<T,P>[]) new PerturbationHeuristic[ls.length+mut.length+rc.length+xo.length];
		llhs_ls = new int[ls.length];
		llhs_mut = new int[mut.length];
		llhs_rc = new int[rc.length];
		llhs_xo = new int[xo.length];
		int k = 0;
		//ls
		for(int i = 0; i < ls.length;i++){
			llhs[k] = ls[i];
			llhs_ls[i] = k;
			k++;
		}
		//mut
		for(int i = 0; i < mut.length;i++){
			llhs[k] = mut[i];
			llhs_mut[i] = k;
			k++;
		}
		//rc
		for(int i = 0; i < rc.length;i++){
			llhs[k] = rc[i];
			llhs_rc[i] = k;
			k++;
		}
		//xo
		for(int i = 0; i < xo.length;i++){
			llhs[k] = xo[i];
			llhs_xo[i] = k;
			k++;
		}
		//iom
		int n_iom = 0;
		for(int i = 0; i < llhs.length;i++){
			n_iom += llhs[i].usesIntensityOfMutation()? 1: 0;
		}
		llhs_iom = new int[n_iom];
		k = 0;
		for(int i = 0; i < llhs.length;i++){
			if(llhs[i].usesIntensityOfMutation()){
				llhs_iom[k] = i;
				k++;
			}
		}
		//dof
		int n_dof = 0;
		for(int i = 0; i < llhs.length;i++){
			n_dof += llhs[i].usesDepthOfSearch()? 1: 0;
		}
		llhs_dof = new int[n_dof];
		k = 0;
		for(int i = 0; i < llhs.length;i++){
			if(llhs[i].usesDepthOfSearch()){
				llhs_dof[k] = i;
				k++;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public double applyHeuristic(int llh, int in, int out) {
		long startTime = System.currentTimeMillis();
		c_memory [out] = llhs[llh].apply(c_memory[in]);
		double e = c_memory[out].getFunctionValue();
		if(e < c_best.getFunctionValue()){
			c_best = (T) c_memory[out].deepCopy();
			//System.out.println(c_best.getFunctionValue()+" (error: "+Math.abs(c_best.getFunctionValue()-c_best.evaluateFunctionValue())+") ");
		}
		this.heuristicCallRecord[llh] += 1;
	    this.heuristicCallTimeRecord[llh] += (int)(System.currentTimeMillis() - startTime);
		return e;
	}

	@SuppressWarnings("unchecked")
	@Override
	public double applyHeuristic(int llh, int in1, int in2, int out) {
		long startTime = System.currentTimeMillis();
		if(llh < llhs.length-llhs_xo.length){
			//apply to in1
			c_memory[out] = llhs[llh].apply(c_memory[in1]);
		}else{
			//apply as crossover
			c_memory[out] = ((CrossoverHeuristic<T,P>) llhs[llh]).apply(c_memory[in1],c_memory[in2]);
		}
		double e = c_memory[out].getFunctionValue();
		if(e < c_best.getFunctionValue()){
			c_best = (T) c_memory[out].deepCopy();
			//System.out.println(c_best.getFunctionValue()+" (error: "+Math.abs(c_best.getFunctionValue()-c_best.evaluateFunctionValue())+")");
		}
		this.heuristicCallRecord[llh] += 1;
	    this.heuristicCallTimeRecord[llh] += (int)(System.currentTimeMillis() - startTime);
		return e;
	}
	
	@Override
	public double getIntensityOfMutation(){                  
		return params.iom;
	}

	@Override
	public double getDepthOfSearch(){
		return params.dos;
	}
	
	@Override
	public void setDepthOfSearch(double dos){
		if(params == null){
			params = new Parameters();
		}
		params.setDOS(dos);
	}
	
	@Override
	public void setIntensityOfMutation(double iom){
		if(params == null){
			params = new Parameters();
		}
		params.setIOM(iom);
	}

	@Override
	public String bestSolutionToString() {
		return c_best.toText();
	}

	@Override
	public boolean compareSolutions(int c1, int c2) {
		return c_memory[c1].isEqualTo(c_memory[c2]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void copySolution(int in, int out) {
		c_memory[out] = (T) c_memory[in].deepCopy();
	}

	@Override
	public double getBestSolutionValue() {
		return c_best == null ? Double.MAX_VALUE : c_best.getFunctionValue();
	}

	@Override
	public double getFunctionValue(int c) {
		return c_memory[c].getFunctionValue();
	}

	@Override
	public int[] getHeuristicsOfType(HeuristicType t) {
		switch (t) {
			case LOCAL_SEARCH: return llhs_ls;
			case MUTATION: return llhs_mut;
			case RUIN_RECREATE: return llhs_rc;
			case CROSSOVER: return llhs_xo;
			default: return null;
		}
	}

	@Override
	public int[] getHeuristicsThatUseDepthOfSearch() {
		return llhs_dof;
	}

	@Override
	public int[] getHeuristicsThatUseIntensityOfMutation() {
		return llhs_iom;
	}

	@Override
	public int getNumberOfHeuristics() {
		return llhs == null ? 20 : llhs.length;
	}

	@Override
	public int getNumberOfInstances() {
		return benchmarks.length;
	}

	@Override
	public void initialiseSolution(int out) {
		c_memory[out] = init.apply();
		if(c_memory[out].getFunctionValue() < getBestSolutionValue()){
			c_best = c_memory[out];
			//System.out.println(c_best.toText());
			//System.out.println(c_best.getFunctionValue()+" (error: "+Math.abs(c_best.getFunctionValue()-c_best.evaluateFunctionValue())+")");
		}
	}

	@Override
	public void loadInstance(int pi) {
		//resize records
		heuristicCallRecord = new int[this.getNumberOfHeuristics()];
		heuristicCallTimeRecord = new int[this.getNumberOfHeuristics()];
		instance = benchmarks[pi].load();
		//re-initialize heuristics
		init.init(instance, params);
		for(int i = 0; i < llhs.length;i++){
			llhs[i].init(instance, params);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setMemorySize(int n) {
		T[] t_memory = (T[]) new BasicSolution[n];
		int m = Math.min(n, (c_memory == null ? 0 : c_memory.length));
		for(int i = 0; i < m;i++){
			t_memory[i] = c_memory[i];
		}
		c_memory = t_memory;
	}

	@Override
	public String solutionToString(int c) {
		return c_memory[c].toText();
	}
	
	public P getLoadedBenchmark(){
		return instance;
	}

}
