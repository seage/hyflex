package QAP;

import QAP.SolutionQAP.SwapNH;
import QAP.heuristics.BestSwap;
import QAP.heuristics.OrderedXO;
import QAP.heuristics.PartiallyMatchedXO;
import QAP.heuristics.RandomInit;
import QAP.heuristics.ReAssignGreedyF;
import QAP.heuristics.ReAssignGreedyL;
import QAP.heuristics.ReAssignRandom;
import QAP.parsers.CFGParserQAP;
import hfu.BasicProblemDomain;
import hfu.BenchmarkInstance;
import hfu.Parser;
import hfu.heuristics.ConstructionHeuristic;
import hfu.heuristics.CrossoverHeuristic;
import hfu.heuristics.LocalSearchHeuristic;
import hfu.heuristics.ModifierFullLocalSearchHeuristic;
import hfu.heuristics.ModifierMutationHeuristic;
import hfu.heuristics.MutationHeuristic;
import hfu.heuristics.RuinRecreateHeuristic;
import hfu.heuristics.selector.SelectBest;
import hfu.heuristics.selector.SelectBestDepth;
import hfu.heuristics.selector.SelectFirst;
import hfu.heuristics.selector.SelectRandom;

public class QAP extends BasicProblemDomain<SolutionQAP,InfoQAP>{

	public QAP(long seed) {
		super(seed);
	}

	@SuppressWarnings("unchecked")
	@Override
	public BenchmarkInstance<InfoQAP>[] getBenchmarkInstances() {
		Parser<InfoQAP> parser = new CFGParserQAP();
		BenchmarkInstance<InfoQAP>[] benchmarks = new BenchmarkInstance[]{
				new BenchmarkInstance<InfoQAP>("instances/qap/sko100a.flp",parser),
				new BenchmarkInstance<InfoQAP>("instances/qap/sko100b.flp",parser),
				new BenchmarkInstance<InfoQAP>("instances/qap/sko100c.flp",parser),
				new BenchmarkInstance<InfoQAP>("instances/qap/sko100d.flp",parser),
				new BenchmarkInstance<InfoQAP>("instances/qap/tai100a.flp",parser),
				new BenchmarkInstance<InfoQAP>("instances/qap/tai100b.flp",parser),
				new BenchmarkInstance<InfoQAP>("instances/qap/tai150b.flp",parser),
				new BenchmarkInstance<InfoQAP>("instances/qap/tai256c.flp",parser),
				new BenchmarkInstance<InfoQAP>("instances/qap/tho150.flp",parser),
				new BenchmarkInstance<InfoQAP>("instances/qap/wil100.flp",parser),
		};
		return benchmarks;
	}

	@Override
	public ConstructionHeuristic<SolutionQAP, InfoQAP> getConstructionHeuristic() {
		return new RandomInit();
	}

	@SuppressWarnings("unchecked")
	@Override
	public LocalSearchHeuristic<SolutionQAP, InfoQAP>[] getLocalSearchHeuristics() {
		LocalSearchHeuristic<SolutionQAP, InfoQAP>[] llhs_ls = new LocalSearchHeuristic[]{
				new ModifierFullLocalSearchHeuristic<SolutionQAP,InfoQAP,SwapNH>(new SelectFirst<SolutionQAP,InfoQAP,SwapNH>(), new Swap(), false), //random order first-improvement hill-climbing (terminated in local optima or after x iterations)
				new ModifierFullLocalSearchHeuristic<SolutionQAP,InfoQAP,SwapNH>(new SelectBest<SolutionQAP,InfoQAP,SwapNH>(false), new Swap(), false), //best-improvement hill-climbing (terminated in local optima or after x iterations)
		};
		return llhs_ls;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MutationHeuristic<SolutionQAP, InfoQAP>[] getMutationHeuristics() {
		MutationHeuristic<SolutionQAP, InfoQAP>[] llhs_mut = new MutationHeuristic[]{
				new ModifierMutationHeuristic<SolutionQAP,InfoQAP,SwapNH>(new SelectRandom<SolutionQAP,InfoQAP,SwapNH>(), new Swap()), //swap x pairs of randomly selected facilities
				new BestSwap(), //performs the best swap (even when not improving, maintain tabu-list)
		};
		return llhs_mut;
	}

	@SuppressWarnings("unchecked")
	@Override
	public RuinRecreateHeuristic<SolutionQAP, InfoQAP>[] getRuinRecreateHeuristics() {
		RuinRecreateHeuristic<SolutionQAP, InfoQAP>[] llhs_rr = new RuinRecreateHeuristic[]{
				new ReAssignRandom(),
				new ReAssignGreedyF(),
				new ReAssignGreedyL()
		};
		return llhs_rr;
	}

	@Override
	public String toString() {
		return "Quadratic Assignment Problem";
	}

	@SuppressWarnings("unchecked")
	@Override
	public CrossoverHeuristic<SolutionQAP, InfoQAP>[] getCrossoverHeuristics() {
		CrossoverHeuristic<SolutionQAP, InfoQAP>[] llhs_xo = new CrossoverHeuristic[]{
				new PartiallyMatchedXO(),
				new OrderedXO()
		};
		return llhs_xo;
	}

}
