package KP.evalfs;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.heuristics.selector.eval.EvaluationFunction;

public class MaxProfitPerPound implements EvaluationFunction<SolutionKP,InfoKP>{

	@Override
	public void init(InfoKP instance) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double evaluate(SolutionKP c) {
		int weight = c.getPackedWeight();
		double profit = c.getPackedProfit();
		return weight == 0? Double.MAX_VALUE : -profit/weight;
	}



}
