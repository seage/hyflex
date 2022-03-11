package hfu.heuristics.selector;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.modifiers.nh.SamplableNH;

public class SelectRandom<T extends BasicSolution<P>,P extends BenchmarkInfo,N extends NeighbourHood<P> & SamplableNH> extends Selector<T,P,N>{

	@Override
	public void init(P instance) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public Proposal<T,P> select(T c, Modifier<T, P, N> modifier, int max) {
		Proposal<T,P> proposal = new Proposal<T,P>();
		if(modifier.isApplicable(c)){
			proposal.c_proposed =  modifier.apply((T)c.deepCopy(),modifier.getNeightbourhood(c).sample());
			proposal.nModifications = 1;
		}
		return proposal;
	}

	@Override
	public boolean usesDepthOfSearch() {
		return false;
	}

	@Override
	public boolean usesIntensityOfMutation() {
		return false;
	}

}
