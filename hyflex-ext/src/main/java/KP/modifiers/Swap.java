package KP.modifiers;

import KP.InfoKP;
import KP.SolutionKP;
import KP.SolutionKP.SwapNH;
import hfu.heuristics.modifiers.PerturbativeModifier;

public class Swap extends PerturbativeModifier<SolutionKP,InfoKP,SwapNH>{

	@Override
	public SwapNH getNeightbourhood(SolutionKP c) {
		return new SwapNH(instance,c);
	}

	@Override
	public SolutionKP apply(SolutionKP c, int... param) {
		//System.out.println(c.toText());
		//System.out.println("substitute " + (param[0]+1) + " by " + (param[1]+1));
		c.swap(param[0],param[1]);
		//System.out.println(c.toText());
		return c;
	}

	@Override
	public int interpretIOM(double iom, SolutionKP c) {
		return (int) Math.ceil(5*iom);
	}
	
	@Override
	public int interpretDOS(double dos, SolutionKP c) {
		return 1;
	}
	
	@Override
	public boolean isApplicable(SolutionKP c) {
		return super.isApplicable(c) && c.getPacked() > 0;
	}

}
