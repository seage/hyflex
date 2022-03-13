package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

abstract public class ConstructionHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo> extends Heuristic<P> {
	protected P instance;
	
	abstract public T apply();
	
	@Override
	public void init(P instance) {
		this.instance = instance;
	}

}
