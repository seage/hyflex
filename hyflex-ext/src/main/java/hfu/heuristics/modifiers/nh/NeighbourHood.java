package hfu.heuristics.modifiers.nh;

import hfu.BenchmarkInfo;

abstract public class NeighbourHood<P extends BenchmarkInfo> {

	protected P instance;
	
	public NeighbourHood(P instance){
		this.instance = instance;
	}
	
	abstract public int getDimensionality();
	
}
