package MAC.modifiers;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import MAC.SolutionMAC.InsertNH;
import MAC.SolutionMAC.RemoveNH;
import hfu.heuristics.modifiers.ConstructiveModifier;
import hfu.heuristics.modifiers.DestructiveModifier;

public class Remove extends DestructiveModifier<SolutionMAC,InfoMAC,RemoveNH>{

	@Override
	public RemoveNH getNeightbourhood(SolutionMAC c) {
		return new RemoveNH(instance,c);
	}

	@Override
	public SolutionMAC apply(SolutionMAC c, int... param) {
		//System.out.println(c.toText());
		//System.out.println("remove "+ (param[0]+1));
		c.remove(param[0]);
		//System.out.println(c.toText());
		return c;
	}

	@Override
	public int interpretIOM(double iom, SolutionMAC c) {
		return Math.max((int) Math.ceil(iom*50),1); // 0.2 = 10
	}

}
