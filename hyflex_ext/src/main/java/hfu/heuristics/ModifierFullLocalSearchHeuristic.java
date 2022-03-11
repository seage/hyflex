package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.modifiers.nh.IterableNH;
import hfu.heuristics.selector.Selector;

public class ModifierFullLocalSearchHeuristic<T extends BasicSolution<P>,P extends BenchmarkInfo, N extends NeighbourHood<P> & IterableNH> extends LocalSearchHeuristic<T,P> {

	PerturbativeModifier<T,P,N> modifier;
	Selector<T,P,N> sel;
	boolean limited_depth;
	
	public ModifierFullLocalSearchHeuristic(Selector<T,P,N> sel, PerturbativeModifier<T,P,N> modifier){
		this.modifier = modifier;
		this.sel = sel;
		this.limited_depth = false;
	}
	
	public ModifierFullLocalSearchHeuristic(Selector<T,P,N> sel, PerturbativeModifier<T,P,N> modifier, boolean limited_depth){
		this.modifier = modifier;
		this.sel = sel;
		this.limited_depth = limited_depth;
	}
	
	@Override
	public void init(P instance) {
		modifier.init(instance);
		sel.init(instance,params);
	}

	@Override
	public boolean usesDepthOfSearch() {
		return limited_depth || sel.usesDepthOfSearch();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T improve(T c) {
		int max_depth = Integer.MAX_VALUE;
		if(limited_depth){
			double dos = params.getDOS(this);
			max_depth = Math.max((int) Math.ceil(100*dos),1);
		}
		T c_prev = (T) c.deepCopy();
		for(int i = 0; i < max_depth;i++){
			c = sel.select(c, modifier);
			if(c == null || c.getFunctionValue() >= c_prev.getFunctionValue()){
				break;
			}
			c_prev = (T) c.deepCopy();
		}
		return c_prev;
	}

}
