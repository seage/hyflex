package KP.heuristics;

import KP.InfoKP;
import KP.SolutionKP;
import KP.SolutionKP.UnionNH;
import hfu.heuristics.CrossoverHeuristic;
import hfu.heuristics.ModifierFullLocalSearchHeuristic;
import hfu.heuristics.modifiers.PerturbativeModifier;
import hfu.heuristics.selector.SelectFirst;
import hfu.heuristics.selector.Selector;

public class UnionCrossover extends CrossoverHeuristic<SolutionKP,InfoKP>{
	InfoKP instance;
	Selector<SolutionKP,InfoKP,UnionNH> sel;

	public UnionCrossover(Selector<SolutionKP, InfoKP, UnionNH> selectFirst){
		this.sel = selectFirst;
	}

	@Override
	public void init(InfoKP instance) {
		this.instance = instance;
	}
	
	@Override
	public boolean usesDepthOfSearch() {
		return sel.usesDepthOfSearch();
	}

	@Override
	public boolean usesIntensityOfMutation() {
		return sel.usesIntensityOfMutation();
	}

	@Override
	public SolutionKP apply(SolutionKP c1, SolutionKP c2) {
		//System.out.println("c1: "+c1.toText());
		//System.out.println("c2: "+c2.toText());
		InsertUnion modifier = new InsertUnion(c1,c2);
		ModifierFullLocalSearchHeuristic<SolutionKP,InfoKP,UnionNH> ls = 
				new ModifierFullLocalSearchHeuristic<SolutionKP,InfoKP,UnionNH>(sel, modifier);
		ls.init(instance,params);
		SolutionKP c = new SolutionKP(instance);
		c = ls.apply(c);
		return c;
	}
	
	public class InsertUnion extends PerturbativeModifier<SolutionKP,InfoKP,UnionNH>{
		SolutionKP c1;
		SolutionKP c2;
		
		InsertUnion(SolutionKP c1,SolutionKP c2){
			this.c1 = c1;
			this.c2 = c2;
		}
		
		@Override
		public UnionNH getNeightbourhood(SolutionKP c) {
			return new UnionNH(instance,c,c1,c2);
		}

		@Override
		public SolutionKP apply(SolutionKP c, int... param) {
			//System.out.println(c.toText());
			//System.out.println("insert "+ (param[0]+1));
			c.insert(param[0]);
			//System.out.println(c.toText());
			return c;
		}

		@Override
		public int interpretDOS(double iom, SolutionKP c) {
			return (int) 1;
		}

		@Override
		public int interpretIOM(double iom, SolutionKP c) {
			return 0;
		}

	}

}
