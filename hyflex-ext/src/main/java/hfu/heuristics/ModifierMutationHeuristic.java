package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Proposal;
import hfu.heuristics.selector.Selector;

public class ModifierMutationHeuristic<T extends BasicSolution<P>,P extends BenchmarkInfo, N extends NeighbourHood<P>> extends MutationHeuristic<T,P> {

	PerturbativeModifier<T,P,N> modifier;
	Selector<T,P,N> sel;
	
	public ModifierMutationHeuristic(Selector<T,P,N> sel, PerturbativeModifier<T,P,N> modifier){
		this.modifier = modifier;
		this.sel = sel;
	}
	
	@Override
	public void init(P instance) {
		sel.init(instance,params);
		modifier.init(instance);
	}

	@Override
	public T apply(T c) {
		int iom = modifier.interpretIOM(params.getIOM(this),c);
		while(iom > 0){
			Proposal<T,P> proposal = sel.select(c,modifier,iom);
			iom -= proposal.nModifications;
			if(proposal.c_proposed != null){
				c = proposal.c_proposed;
			}else{
				break;
			}
		}
		return c;
	}


	@Override
	public boolean usesIntensityOfMutation() {
		return true;
	}

	
	

}
