package QAP.heuristics;

import QAP.InfoQAP;
import QAP.SolutionQAP;
import hfu.heuristics.RuinRecreateHeuristic;

public class ReAssignGreedyL extends RuinRecreateHeuristic<SolutionQAP,InfoQAP>{

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
		c.reAssignGreedyLFraction(params.getIOM(this));
		return c;
	}

	@Override
	public void init(InfoQAP instance) {
		
	}



}
