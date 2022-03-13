package hfu.heuristics.modifiers;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.nh.NeighbourHood;

abstract public class Modifier<T extends BasicSolution<P>, P extends BenchmarkInfo,N extends NeighbourHood<P>> {

	protected P instance;
	
	//boolean isSymmetric();
	public void init(P instance){
		this.instance = instance;
	}
	abstract public boolean isApplicable(T c);
	abstract public N getNeightbourhood(T c);
	abstract public T apply(T c, int... param);
	abstract public int interpretIOM(double iom, T c);

	public int interpretDOS(double dos, T c) {
		return (int) Math.ceil(5*dos);
	}
}
