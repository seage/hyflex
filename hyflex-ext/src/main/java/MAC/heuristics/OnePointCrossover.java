package MAC.heuristics;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import hfu.RNG;
import hfu.heuristics.CrossoverHeuristic;

public class OnePointCrossover extends CrossoverHeuristic<SolutionMAC,InfoMAC>{

	InfoMAC instance;
	
	@Override
	public boolean usesDepthOfSearch() {
		return false;
	}

	@Override
	public boolean usesIntensityOfMutation() {
		return false;
	}

	@Override
	public SolutionMAC apply(SolutionMAC c1, SolutionMAC c2) {
		SolutionMAC c_res = (SolutionMAC) c1.deepCopy();
		c_res.one_point_crossover(RNG.get().nextInt(instance.getNvertices()), c2);
		return c_res;
	}

	@Override
	public void init(InfoMAC instance) {
		this.instance = instance;
	}

	

}
