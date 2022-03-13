package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.ParameterUsage;

abstract public class PerturbationHeuristic<T extends BasicSolution<P>,P extends BenchmarkInfo> extends Heuristic<P> implements ParameterUsage{
	abstract public T apply(T c);
}
