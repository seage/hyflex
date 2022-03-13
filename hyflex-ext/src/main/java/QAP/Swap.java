package QAP;

import QAP.SolutionQAP.SwapNH;
import hfu.heuristics.modifiers.PerturbativeModifier;

public class Swap extends PerturbativeModifier<SolutionQAP,InfoQAP,SwapNH>{
	
	public Swap(){

	}

	@Override
	public SwapNH getNeightbourhood(SolutionQAP c) {
		return new SwapNH(instance);
	}

	@Override
	public SolutionQAP apply(SolutionQAP c, int... param) {
		c.swap(param[0],param[1]);
		return c;
	}

	@Override
	public int interpretIOM(double iom, SolutionQAP c) {
		return Math.max((int) Math.ceil(iom*5),1); // 0.2 = 2
	}
	
	public int interpretDOS(double dos, SolutionQAP c) {
		int threshold = 1000000000;
		int n = instance.getN();
		int swaps = n*(n-1)/2;
		int k = 1;
		while(Math.pow(swaps, k) < threshold){
			k++;
		}
		k -= 1;
		return Math.max(1, k);
	}

}
