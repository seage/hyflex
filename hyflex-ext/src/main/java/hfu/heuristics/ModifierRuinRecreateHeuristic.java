package hfu.heuristics;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.ConstructiveModifier;
import hfu.heuristics.modifiers.DestructiveModifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.Proposal;
import hfu.heuristics.selector.Selector;

public class ModifierRuinRecreateHeuristic<T extends BasicSolution<P>,P extends BenchmarkInfo, CN extends NeighbourHood<P>, DN extends NeighbourHood<P>> extends RuinRecreateHeuristic<T,P> {

	ConstructiveModifier<T,P,CN> cmodifier;
	DestructiveModifier<T,P,DN> dmodifier;
	Selector<T,P,CN> csel;
	Selector<T,P,DN> dsel;
	
	
	public ModifierRuinRecreateHeuristic(Selector<T,P,CN> csel, ConstructiveModifier<T,P,CN> cmodifier, Selector<T,P,DN> dsel, DestructiveModifier<T,P,DN> dmodifier){
		this.cmodifier = cmodifier;
		this.dmodifier = dmodifier;
		this.csel = csel;
		this.dsel = dsel;
	}
	
	@Override
	public void init(P instance) {
		cmodifier.init(instance);
		dmodifier.init(instance);
		csel.init(instance,params);
		dsel.init(instance,params);
	}
	
	@Override
	public T apply(T c) {
		//ruin
		int iom = dmodifier.interpretIOM(params.getIOM(this),c);
		while(iom > 0){
			Proposal<T,P> proposal = dsel.select(c,dmodifier,iom);
			iom -= proposal.nModifications;
			c = proposal.c_proposed;
		}
		//recreate
		while(c.isPartial()){
			c = csel.select(c, cmodifier);
		}
		return c;
	}
	
	@Override
	public boolean usesDepthOfSearch() {
		return csel.usesDepthOfSearch() || dsel.usesDepthOfSearch();
	}

	@Override
	public boolean usesIntensityOfMutation() {
		return true;
	}
}
