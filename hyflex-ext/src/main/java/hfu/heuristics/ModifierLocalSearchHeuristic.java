package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.modifiers.nh.IterableNH;
import hfu.heuristics.selector.Selector;

public class ModifierLocalSearchHeuristic<T extends BasicSolution<P>,P extends BenchmarkInfo, N extends NeighbourHood<P> & IterableNH> extends LocalSearchHeuristic<T,P> {

	PerturbativeModifier<T,P,N> modifier;
	Selector<T,P,N> sel;
	
	public ModifierLocalSearchHeuristic(Selector<T,P,N> sel, PerturbativeModifier<T,P,N> modifier){
		this.modifier = modifier;
		this.sel = sel;
	}
	
	@Override
	public void init(P instance) {
		modifier.init(instance);
		sel.init(instance,params);
	}

	@Override
	public boolean usesDepthOfSearch() {
		return sel.usesDepthOfSearch();
	}

	@Override
	public T improve(T c) {
		return sel.select(c, modifier);
	}

}
