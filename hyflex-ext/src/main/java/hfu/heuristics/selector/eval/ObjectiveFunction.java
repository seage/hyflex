package hfu.heuristics.selector.eval;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;

public class ObjectiveFunction<T extends BasicSolution<P>, P extends BenchmarkInfo> implements EvaluationFunction<T,P>{

	@Override
	public void init(P instance) {
		
	}

	@Override
	public double evaluate(T c) {
		return c.getFunctionValue();
	}

}
