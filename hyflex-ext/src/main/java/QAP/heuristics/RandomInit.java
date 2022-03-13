package QAP.heuristics;

import QAP.InfoQAP;
import QAP.SolutionQAP;
import hfu.RNG;
import hfu.heuristics.ConstructionHeuristic;
import hfu.heuristics.selector.SelectBest;
import hfu.heuristics.selector.Selector;

public class RandomInit extends ConstructionHeuristic<SolutionQAP,InfoQAP>{

	@Override 
	public void init(InfoQAP instance){
		super.init(instance);
		
	}
	
	@Override
	public SolutionQAP apply() {
		SolutionQAP c = new SolutionQAP(instance);
		c.randomInit();
		return c;
	}

}
