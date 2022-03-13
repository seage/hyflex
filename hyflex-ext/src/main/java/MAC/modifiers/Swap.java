package MAC.modifiers;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import MAC.SolutionMAC.InsertNH;
import MAC.SolutionMAC.SwapNH;
import hfu.heuristics.modifiers.PerturbativeModifier;

public class Swap extends PerturbativeModifier<SolutionMAC,InfoMAC,SwapNH>{
	
	public Swap(){

	}

	@Override
	public SwapNH getNeightbourhood(SolutionMAC c) {
		return new SwapNH(instance);
	}

	@Override
	public SolutionMAC apply(SolutionMAC c, int... param) {
		//System.out.println(c.toText());
		//System.out.println("swap "+ (param[0]+1));
		c.swap(param[0]);
		//System.out.println(c.toText());
		return c;
	}

	@Override
	public int interpretIOM(double iom, SolutionMAC c) {
		return Math.max((int) Math.ceil(iom*10),1); // 0.2 = 2
	}

}
