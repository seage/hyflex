package KP.modifiers;

import KP.InfoKP;
import KP.SolutionKP;
import KP.SolutionKP.UnpackedNH;
import hfu.heuristics.modifiers.PerturbativeModifier;


public class Insert extends PerturbativeModifier<SolutionKP,InfoKP,UnpackedNH>{

	@Override
	public UnpackedNH getNeightbourhood(SolutionKP c) {
		return new UnpackedNH(instance,c);
	}

	@Override
	public SolutionKP apply(SolutionKP c, int... param) {
		//System.out.println(c.toText());
		//System.out.println("insert "+ (param[0]+1));
		c.insert(param[0]);
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

}
