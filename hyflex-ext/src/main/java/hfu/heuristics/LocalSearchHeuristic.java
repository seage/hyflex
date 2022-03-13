package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

abstract public class LocalSearchHeuristic<T extends BasicSolution<P>,P extends BenchmarkInfo> extends PerturbationHeuristic<T,P> {
	
	
	abstract public T improve(T c);
	
	@Override
	public boolean usesIntensityOfMutation() {
		return false;
	}


	@Override
	public T apply(T c) {
		//guarantee improvement
		T c_proposed = improve(c);  
		return c_proposed != null && c.getFunctionValue() > c_proposed.getFunctionValue()? c_proposed : c;
	}

}
