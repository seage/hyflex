package KP.heuristics;

import KP.InfoKP;
import KP.SolutionKP;
import hfu.heuristics.CrossoverHeuristic;

public class IntersectCrossover extends CrossoverHeuristic<SolutionKP,InfoKP>{

	@Override
	public void init(InfoKP instance) {

	}
	
	@Override
	public boolean usesDepthOfSearch() {
		return false;
	}

	@Override
	public boolean usesIntensityOfMutation() {
		return false;
	}

	@Override
	public SolutionKP apply(SolutionKP c1, SolutionKP c2) {
		//System.out.println("c1:"+c1.toText());
		//System.out.println("c2:"+c2.toText());
		SolutionKP c_res = ((SolutionKP) c1.deepCopy());
		c_res.intersect(c2);
		//System.out.println("c_res:"+c_res.toText());
		return c_res;
	}

}
