package hfu.heuristics.selector;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

public class Proposal<T extends BasicSolution<P>, P extends BenchmarkInfo> {
	public T c_proposed;
	public int nModifications;
}
