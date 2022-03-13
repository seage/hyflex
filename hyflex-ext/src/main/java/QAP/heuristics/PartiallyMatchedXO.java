package QAP.heuristics;

import QAP.InfoQAP;
import QAP.SolutionQAP;
import hfu.RNG;
import hfu.heuristics.CrossoverHeuristic;

public class PartiallyMatchedXO extends CrossoverHeuristic<SolutionQAP,InfoQAP>{

	
	@Override
	public boolean usesDepthOfSearch() {
		return false;
	}

	@Override
	public boolean usesIntensityOfMutation() {
		return false;
	}

	@Override
	public SolutionQAP apply(SolutionQAP c1, SolutionQAP c2) {
		SolutionQAP c_res = (SolutionQAP) c1.deepCopy();
		c_res.pmx(c2);
		return c_res;
	}

	@Override
	public void init(InfoQAP instance) {
		// TODO Auto-generated method stub
		
	}

}
