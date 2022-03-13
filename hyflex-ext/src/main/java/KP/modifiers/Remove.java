package KP.modifiers;

import KP.InfoKP;
import KP.SolutionKP;
import KP.SolutionKP.KnapSackNH;
import hfu.heuristics.modifiers.PerturbativeModifier;

public class Remove extends PerturbativeModifier<SolutionKP,InfoKP,KnapSackNH>{

	@Override
	public KnapSackNH getNeightbourhood(SolutionKP c) {
		return new KnapSackNH(instance,c);
	}

	@Override
	public SolutionKP apply(SolutionKP c, int... param) {
//		System.out.println(c.toText());
	//	System.out.println("remove "+(param[0]+1));
		c.remove(param[0]);
		//System.out.println(c.toText());
		return c;
	}

	@Override
	public int interpretIOM(double iom, SolutionKP c) {
		return (int) Math.max(Math.ceil(iom*c.getPacked()),1);
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
