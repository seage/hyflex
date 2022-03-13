package QAP.heuristics;

import java.util.HashSet;
import java.util.Set;

import QAP.InfoQAP;
import QAP.SolutionQAP;

import hfu.heuristics.MutationHeuristic;

public class BestSwap extends MutationHeuristic<SolutionQAP,InfoQAP>{

	@Override
	public boolean usesDepthOfSearch() {
		return false;
	}

	@Override
	public boolean usesIntensityOfMutation() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public SolutionQAP apply(SolutionQAP c) {
		SolutionQAP c_res = (SolutionQAP) c.deepCopy();
		double iom = params.getIOM(this);
		int repeated = (int) Math.ceil(1000*iom*iom*iom);
		Set<Integer> tabu = new HashSet<Integer>();
		for(int i = 0; i < repeated;i++){
			tabu.add(c_res.swapBest(tabu));
			if(c_res.getFunctionValue() < c.getFunctionValue()){
				return c_res;
			}
		}
		return c_res;
	}
	

	@Override
	public void init(InfoQAP instance) {
		// TODO Auto-generated method stub
		
	}


}
