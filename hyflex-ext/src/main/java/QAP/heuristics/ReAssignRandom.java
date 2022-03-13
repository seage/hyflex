package QAP.heuristics;

import QAP.InfoQAP;
import QAP.SolutionQAP;
import hfu.heuristics.RuinRecreateHeuristic;

public class ReAssignRandom extends RuinRecreateHeuristic<SolutionQAP,InfoQAP>{

	@Override
	public boolean usesDepthOfSearch() {
		return false;
	}

	@Override
	public boolean usesIntensityOfMutation() {
		return true;
	}

	@Override
	public SolutionQAP apply(SolutionQAP c) {
		c = ((SolutionQAP) c.deepCopy());
		c.reAssignRandomFraction(params.getIOM(this)/2);
		return c;
	}

	@Override
	public void init(InfoQAP instance) {
		
	}



}
