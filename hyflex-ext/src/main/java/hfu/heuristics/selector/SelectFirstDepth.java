package hfu.heuristics.selector;

import java.util.Arrays;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.nh.IteratorNH;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.modifiers.nh.RandomIterable;
import hfu.heuristics.selector.eval.EvaluationFunction;
import hfu.heuristics.selector.eval.ObjectiveFunction;

public class SelectFirstDepth<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P> & RandomIterable> extends Selector<T,P,N>{

	EvaluationFunction<T,P> evalf;
	
	public SelectFirstDepth(){
		evalf = new ObjectiveFunction<T,P>();
	}
	
	public SelectFirstDepth(EvaluationFunction<T,P> evalf){
		this.evalf = evalf;
	}
	
	@Override
	public void init(P instance) {
		evalf.init(instance);
	}
	
	@Override
	public Proposal<T,P> select(T c, Modifier<T, P, N> modifier, int max) {
		Proposal<T,P> proposal = new Proposal<T,P>();
		if(modifier.isApplicable(c)){
			int dos = Math.min(modifier.interpretDOS(params.getDOS(this),c),max);
			double target_e = evalf.evaluate(c);
			//apply iterative deepening
			int n = 0;
			while(proposal.c_proposed == null && n < dos){
				n++;
				proposal.c_proposed = searchFirst(c,c,target_e,modifier,n);
			}
			proposal.nModifications = proposal.c_proposed == null? 0 : n;
		}
		return proposal;
	}
	
	@SuppressWarnings("unchecked")
	private T searchFirst(T c, T c_original, double target_e, Modifier<T,P,N> modifier,int dos){
		N nh = modifier.getNeightbourhood(c);
		IteratorNH it = nh.getRandomIterator();
		while(it.hasNext()){
			T c_new = modifier.apply((T)c.deepCopy(), it.next());
			if(dos > 1 && modifier.isApplicable(c_new)){
				c_new = searchFirst(c_new,c,target_e,modifier,dos-1);
				if(c_new != null){
					return c_new;
				}
			}else if(evalf.evaluate(c_new) < target_e && !c_original.isEqualTo(c_new)){
				return c_new;
			}
		}
		return null;
	}

	@Override
	public boolean usesDepthOfSearch() {
		return true;
	}

	@Override
	public boolean usesIntensityOfMutation() {
		return false;
	}





	


	

	

}
