package hfu.heuristics.selector;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.ParameterUsage;
import hfu.Parameters;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;

abstract public class Selector<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P>>  implements ParameterUsage {

	protected Parameters params;
	
	public void init(P instance, Parameters params){
		this.params = params;
		init(instance);
	}
	
	abstract void init(P instance);
	
	abstract public Proposal<T,P> select(T c, Modifier<T,P,N> modifier, int max);
	
	public T select(T c, Modifier<T,P,N> modifier){
		return select(c,modifier,Integer.MAX_VALUE).c_proposed;
	}

}
