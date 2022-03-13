package hfu.heuristics.selector;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.nh.IterableNH;
import hfu.heuristics.modifiers.nh.IteratorNH;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.modifiers.nh.RandomIterable;
import hfu.heuristics.selector.eval.EvaluationFunction;
import hfu.heuristics.selector.eval.ObjectiveFunction;

public class SelectBest<T extends BasicSolution<P>,P extends BenchmarkInfo, N extends NeighbourHood<P> & IterableNH> extends Selector<T,P,N>{

	EvaluationFunction<T,P> evalf;
	boolean strict;
	
	public SelectBest(boolean strict){
		this.strict = strict;
		evalf = new ObjectiveFunction<T,P>();
	}
	
	public SelectBest(boolean strict,EvaluationFunction<T,P> evalf){
		this.strict = strict;
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
			int dos = 1;
			Result r = searchBest(c,modifier,dos);
			proposal.c_proposed = r.c;
			proposal.nModifications = dos-r.depth+1;
		}
		return proposal;
	}
	

	@SuppressWarnings("unchecked")
	private Result searchBest(T c, Modifier<T,P,N> modifier,int dos){
		N nh = modifier.getNeightbourhood(c);
		IteratorNH it = nh.getIterator();
		Result best = new Result(dos);
		while(it.hasNext()){
			T c_new = modifier.apply((T)c.deepCopy(), it.next());
			double e_new;
			if(dos > 1 && modifier.isApplicable(c_new)){
				Result r = searchBest(c,modifier,dos-1);
				if(!strict){
					e_new = evalf.evaluate(c_new);
					if(r.e > e_new && !c_new.isEqualTo(c)){
						r.c = c_new;
						r.e = e_new;
						r.depth = dos;
					}
				}
				if(r.e < best.e){
					best = r;
				}
			}else{
				e_new = evalf.evaluate(c_new);
				if(e_new < best.e && !c_new.isEqualTo(c)){
					best = new Result(c_new,e_new,dos);
				}
			}
		}
		return best;
	}

	class Result{
		T c;
		double e;
		int depth;
		
		Result(int depth){
			c = null;
			e = Double.MAX_VALUE;
			this.depth = depth;
		}
		
		Result(T c, double e, int depth){
			this.c = c;
			this.e = e;
			this.depth = depth;
		}
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
