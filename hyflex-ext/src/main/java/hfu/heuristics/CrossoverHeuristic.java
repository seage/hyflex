package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

abstract public class CrossoverHeuristic<T extends BasicSolution<P>,P extends BenchmarkInfo> extends PerturbationHeuristic<T,P>{
	abstract public T apply(T c1, T c2);
	

	@SuppressWarnings("unchecked")
	@Override
	public T apply(T c) {
		return apply(c,(T) c.deepCopy());
	}

}
