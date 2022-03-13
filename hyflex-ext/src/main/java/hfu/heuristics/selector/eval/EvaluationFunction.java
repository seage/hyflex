package hfu.heuristics.selector.eval;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

public interface EvaluationFunction<T extends BasicSolution<P>, P extends BenchmarkInfo> {
	void init (P instance);
	double evaluate(T c);
}
