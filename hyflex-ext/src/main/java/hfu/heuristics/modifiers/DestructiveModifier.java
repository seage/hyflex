package hfu.heuristics.modifiers;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.nh.NeighbourHood;

abstract public class DestructiveModifier<T extends BasicSolution<P>,P extends BenchmarkInfo, N extends NeighbourHood<P>> extends Modifier<T,P,N> {
	@Override
	public
	boolean isApplicable(T c) {
		return !c.isEmpty();
	}

}
