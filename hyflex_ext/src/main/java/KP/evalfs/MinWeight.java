package KP.evalfs;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.heuristics.selector.eval.EvaluationFunction;

public class MinWeight implements EvaluationFunction<SolutionKP,InfoKP>{

	@Override
	public void init(InfoKP instance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double evaluate(SolutionKP c) {
		return c.getPackedWeight();
	}



}
