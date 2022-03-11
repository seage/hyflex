package KP.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.ModifierFullLocalSearchHeuristic;
import hfu.heuristics.ModifierMutationHeuristic;
import hfu.heuristics.RuinRecreateHeuristic;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.IterableNH;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Selector;

//ruin-recreate in solution space, corresponds to a perturbation, followed by local-search
public class PerturbativeRuinRecreate<T extends BasicSolution<P>,P extends BenchmarkInfo, CN extends NeighbourHood<P> & IterableNH, DN extends NeighbourHood<P>> extends RuinRecreateHeuristic<T,P> {

	ModifierMutationHeuristic<T,P,DN> dh;
	ModifierFullLocalSearchHeuristic<T,P,CN> ch;
	
	public PerturbativeRuinRecreate(Selector<T,P,CN> csel, PerturbativeModifier<T,P,CN> cmodifier, Selector<T,P,DN> dsel, PerturbativeModifier<T,P,DN> dmodifier){
		dh = new ModifierMutationHeuristic<T,P,DN>(dsel,dmodifier);
		ch = new ModifierFullLocalSearchHeuristic<T,P,CN>(csel,cmodifier);
	}
	
	@Override
	public void init(P instance) {
		dh.init(instance,params);
		ch.init(instance,params);
	}
	
	@Override
	public T apply(T c) {
		//ruin
		c = dh.apply(c);
		//recreate
		c = ch.apply(c);
		return c;
	}
	
	@Override
	public boolean usesDepthOfSearch() {
		return ch.usesDepthOfSearch();
	}

	@Override
	public boolean usesIntensityOfMutation() {
		return true;
	}
}
