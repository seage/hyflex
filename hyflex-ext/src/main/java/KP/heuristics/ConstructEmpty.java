package KP.heuristics;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.heuristics.ConstructionHeuristic;

public class ConstructEmpty extends ConstructionHeuristic<SolutionKP,InfoKP>{

	@Override
	public SolutionKP apply() {
		return new SolutionKP(instance);
	}

}
