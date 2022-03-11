package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.ConstructiveModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Selector;

abstract public class ModifierConstructionHeuristic<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P>> extends ConstructionHeuristic<T,P> {

	ConstructiveModifier<T,P,N> modifier;
	Selector<T,P,N> sel;
	
	public abstract T getEmptySolution();
	
	public ModifierConstructionHeuristic(Selector<T,P,N> sel, ConstructiveModifier<T,P,N> modifier){
		this.modifier = modifier;
		this.sel = sel;
	}
	
	@Override
	public void init(P instance) {
		this.instance = instance;
		sel.init(instance, params);
		modifier.init(instance);
	}
	
	@Override
	public  T apply() {
		T c = getEmptySolution();
		while(c.isPartial()){
			c = sel.select(c, modifier);
		}
		return c;
	}

	

}