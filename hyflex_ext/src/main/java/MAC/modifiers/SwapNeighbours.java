package MAC.modifiers;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import MAC.modifiers.nhs.SwapNeighboursNH;
import hfu.heuristics.modifiers.PerturbativeModifier;

public class SwapNeighbours extends PerturbativeModifier<SolutionMAC,InfoMAC,SwapNeighboursNH>{
	
	public SwapNeighbours(){

	}

	@Override
	public SwapNeighboursNH getNeightbourhood(SolutionMAC c) {
		return new SwapNeighboursNH(instance);
	}

	@Override
	public SolutionMAC apply(SolutionMAC c, int... param) {
		//System.out.println(c.toText());
		//System.out.println("swap "+ (param[0]+1)+" & "+ (param[1]+1));
		c.swapNeighbours(param[0],param[1]);
		//System.out.println(c.toText());
		return c;
	}

	@Override
	public int interpretIOM(double iom, SolutionMAC c) {
		return Math.max((int) Math.ceil(iom*5),1); // 0.2 = 1
	}

}
