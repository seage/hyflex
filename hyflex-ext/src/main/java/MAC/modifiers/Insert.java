package MAC.modifiers;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import MAC.SolutionMAC.InsertNH;
import hfu.heuristics.modifiers.ConstructiveModifier;

public class Insert extends ConstructiveModifier<SolutionMAC,InfoMAC,InsertNH>{
	int v;
	
	public Insert(){
		v = -1;
	}
	
	public Insert(int v){
		this.v = v;
	}

	@Override
	public InsertNH getNeightbourhood(SolutionMAC c) {
		return v == -1? new InsertNH(instance,c) : new InsertNH(instance,c,v) ;
	}

	@Override
	public SolutionMAC apply(SolutionMAC c, int... param) {
		//System.out.println(c.toText());
		//System.out.println("insert "+ (param[0]+1)+ " in partition "+param[1]);
		c.insert(param[0],param[1]);
		//System.out.println(c.toText());
		return c;
	}

	@Override
	public int interpretDOS(double dos, SolutionMAC c) {
		return (int) Math.ceil(5*dos);
	}

}
