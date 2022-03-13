package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

abstract public class MutationHeuristic<T extends BasicSolution<P>,P extends BenchmarkInfo> extends PerturbationHeuristic<T,P> {

	@Override
	public boolean usesDepthOfSearch() {
		return false;
	}

}
