package KP;

import KP.SolutionKP.KnapSackNH;
import KP.SolutionKP.SwapNH;
import KP.SolutionKP.UnionNH;
import KP.SolutionKP.UnpackedNH;
import KP.evalfs.MaxProfitPerPound;
import KP.evalfs.MinWeight;
import KP.heuristics.ConstructEmpty;
import KP.heuristics.IntersectCrossover;
import KP.heuristics.PerturbativeRuinRecreate;
import KP.heuristics.UnionCrossover;
import KP.modifiers.Insert;
import KP.modifiers.Remove;
import KP.modifiers.Swap;
import KP.parsers.CFGParserKP;
import QAP.SolutionQAP;
import hfu.BasicProblemDomain;
import hfu.BenchmarkInstance;
import hfu.Parser;
import hfu.heuristics.ConstructionHeuristic;
import hfu.heuristics.CrossoverHeuristic;
import hfu.heuristics.LocalSearchHeuristic;
import hfu.heuristics.ModifierFullLocalSearchHeuristic;
import hfu.heuristics.ModifierLocalSearchHeuristic;
import hfu.heuristics.ModifierMutationHeuristic;
import hfu.heuristics.MutationHeuristic;
import hfu.heuristics.RuinRecreateHeuristic;
import hfu.heuristics.selector.SelectBest;
import hfu.heuristics.selector.SelectFirst;
import hfu.heuristics.selector.SelectRandom;

public class KnapsackProblem extends BasicProblemDomain<SolutionKP,InfoKP>{

	public KnapsackProblem(long seed) {
		super(seed);
	}

	@SuppressWarnings("unchecked")
	@Override
	public BenchmarkInstance<InfoKP>[] getBenchmarkInstances() {
		Parser<InfoKP> parser = new CFGParserKP();
		BenchmarkInstance<InfoKP>[] benchmarks = new BenchmarkInstance[]{
				new BenchmarkInstance<InfoKP>("instances/kp/0.kp",parser), //9445
				new BenchmarkInstance<InfoKP>("instances/kp/1.kp",parser), //5024
				new BenchmarkInstance<InfoKP>("instances/kp/2.kp",parser), //2193
				new BenchmarkInstance<InfoKP>("instances/kp/3.kp",parser), //29
				new BenchmarkInstance<InfoKP>("instances/kp/4.kp",parser), //12830
				new BenchmarkInstance<InfoKP>("instances/kp/5.kp",parser), //17836
				new BenchmarkInstance<InfoKP>("instances/kp/6.kp",parser), //7489
				new BenchmarkInstance<InfoKP>("instances/kp/7.kp",parser), //4167
				new BenchmarkInstance<InfoKP>("instances/kp/8.kp",parser), //42001
				new BenchmarkInstance<InfoKP>("instances/kp/9.kp",parser), //2103
		};
		return benchmarks;
	}

	@Override
	public ConstructionHeuristic<SolutionKP, InfoKP> getConstructionHeuristic() {
		return new ConstructEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public LocalSearchHeuristic<SolutionKP, InfoKP>[] getLocalSearchHeuristics() {
		LocalSearchHeuristic<SolutionKP, InfoKP>[] llhs_ls = new LocalSearchHeuristic[]{
				new ModifierLocalSearchHeuristic<SolutionKP,InfoKP,UnpackedNH>(new SelectFirst<SolutionKP,InfoKP,UnpackedNH>(), new Insert()), //packs randomly fitting piece (1 move)
				//new ModifierFullLocalSearchHeuristic<SolutionKS,InfoKS,UnpackedNH>(new SelectFirst<SolutionKS,InfoKS,UnpackedNH>(), new Insert()), //packs randomly fitting piece (till no more fit)
				new ModifierLocalSearchHeuristic<SolutionKP,InfoKP,UnpackedNH>(new SelectBest<SolutionKP,InfoKP,UnpackedNH>(false), new Insert()), //packs most expensive fitting piece (1 move)
				//new ModifierFullLocalSearchHeuristic<SolutionKS,InfoKS,UnpackedNH>(new SelectBest<SolutionKS,InfoKS,UnpackedNH>(false), new Insert()), //packs most expensive fitting piece (till no more fit)
				//new ModifierLocalSearchHeuristic<SolutionKS,InfoKS,UnpackedNH>(new SelectFirst<SolutionKS,InfoKS,UnpackedNH>(new MaxProfitPerPound()), new Insert()), //packs randomly a fitting piece increasing Profit per Pound (1 move)
				new ModifierFullLocalSearchHeuristic<SolutionKP,InfoKP,UnpackedNH>(new SelectFirst<SolutionKP,InfoKP,UnpackedNH>(new MaxProfitPerPound()), new Insert()), //packs randomly a fitting piece increasing Profit per Pound (till no more fit)
				//new ModifierLocalSearchHeuristic<SolutionKS,InfoKS,UnpackedNH>(new SelectBest<SolutionKS,InfoKS,UnpackedNH>(false,new MaxProfitPerPound()), new Insert()), //packs fitting piece increasing Profit per Pound most (1 move)
				//new ModifierFullLocalSearchHeuristic<SolutionKS,InfoKS,UnpackedNH>(new SelectBest<SolutionKS,InfoKS,UnpackedNH>(false,new MaxProfitPerPound()), new Insert()), //packs fitting piece increasing Profit per Pound most (till no more fit)
				new ModifierLocalSearchHeuristic<SolutionKP,InfoKP,UnpackedNH>(new SelectBest<SolutionKP,InfoKP,UnpackedNH>(false,new MinWeight()), new Insert()), //packs piece with the smallest weight (1 move)
				//new ModifierFullLocalSearchHeuristic<SolutionKS,InfoKS,UnpackedNH>(new SelectBest<SolutionKS,InfoKS,UnpackedNH>(false,new MinWeight()), new Insert()), //packs piece with the smallest weight (till no more fit)
				new ModifierLocalSearchHeuristic<SolutionKP,InfoKP,SwapNH>(new SelectFirst<SolutionKP,InfoKP,SwapNH>(), new Swap()), //swaps a random cheaper with a fitting more expensive piece (1 move)
				//new ModifierFullLocalSearchHeuristic<SolutionKS,InfoKS,SwapNH>(new SelectFirst<SolutionKS,InfoKS,SwapNH>(), new Swap()), //swaps a random cheaper with a fitting more expensive piece (till lo)
				new ModifierLocalSearchHeuristic<SolutionKP,InfoKP,SwapNH>(new SelectBest<SolutionKP,InfoKP,SwapNH>(false), new Swap()), //swap pieces increasing packed profit most (1 move)
				//new ModifierFullLocalSearchHeuristic<SolutionKS,InfoKS,SwapNH>(new SelectBest<SolutionKS,InfoKS,SwapNH>(false), new Swap()), //swap pieces increasing packed profit most (till lo)
				
		};
		return llhs_ls;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MutationHeuristic<SolutionKP, InfoKP>[] getMutationHeuristics() {
		MutationHeuristic<SolutionKP, InfoKP>[] llhs_mut = new MutationHeuristic[]{
				new ModifierMutationHeuristic<SolutionKP,InfoKP,SwapNH>(new SelectRandom<SolutionKP,InfoKP,SwapNH>(), new Swap()), //swap random pieces
				new ModifierMutationHeuristic<SolutionKP,InfoKP,SwapNH>(new SelectFirst<SolutionKP,InfoKP,SwapNH>(new MaxProfitPerPound()), new Swap()), //swap random pieces improving profit/pound
				//new ModifierMutationHeuristic<SolutionKS,InfoKS,SwapNH>(new SelectBest<SolutionKS,InfoKS,SwapNH>(false,new MaxProfitPerPound()), new Swap()), //swap pieces improving profit/pound most
				//new ModifierMutationHeuristic<SolutionKS,InfoKS,SwapNH>(new SelectFirst<SolutionKS,InfoKS,SwapNH>(new MinWeight()), new Swap()), //swap random pieces reducing weight
				//new ModifierMutationHeuristic<SolutionKS,InfoKS,SwapNH>(new SelectBest<SolutionKS,InfoKS,SwapNH>(false,new MinWeight()), new Swap()), //swap pieces to reduce weight most
				new ModifierMutationHeuristic<SolutionKP,InfoKP,KnapSackNH>(new SelectRandom<SolutionKP,InfoKP,KnapSackNH>(), new Remove()), //remove random pieces
				//new ModifierMutationHeuristic<SolutionKS,InfoKS,KnapSackNH>(new SelectFirst<SolutionKS,InfoKS,KnapSackNH>(new MaxProfitPerPound()), new Remove()), //remove random pieces improving profit/pound
				//new ModifierMutationHeuristic<SolutionKS,InfoKS,KnapSackNH>(new SelectBest<SolutionKS,InfoKS,KnapSackNH>(false,new MaxProfitPerPound()), new Remove()), //remove pieces to improve profit/pound most
				new ModifierMutationHeuristic<SolutionKP,InfoKP,KnapSackNH>(new SelectBest<SolutionKP,InfoKP,KnapSackNH>(false), new Remove()), //remove least expensive pieces
				new ModifierMutationHeuristic<SolutionKP,InfoKP,KnapSackNH>(new SelectBest<SolutionKP,InfoKP,KnapSackNH>(false,new MinWeight()), new Remove()), //remove heaviest pieces
		};
		return llhs_mut;
	}
	
	//random destruction, greedy packing (greedily packing based on profit, profit/pound)

	@SuppressWarnings("unchecked")
	@Override
	public RuinRecreateHeuristic<SolutionKP, InfoKP>[] getRuinRecreateHeuristics() {
		RuinRecreateHeuristic<SolutionKP, InfoKP>[] llhs_rr = new RuinRecreateHeuristic[]{
				new PerturbativeRuinRecreate<SolutionKP,InfoKP,UnpackedNH,KnapSackNH>(new SelectBest<SolutionKP,InfoKP,UnpackedNH>(false), new Insert(), new SelectRandom<SolutionKP,InfoKP,KnapSackNH>(), new Remove()),
				new PerturbativeRuinRecreate<SolutionKP,InfoKP,UnpackedNH,KnapSackNH>(new SelectBest<SolutionKP,InfoKP,UnpackedNH>(true,new MaxProfitPerPound()), new Insert(), new SelectRandom<SolutionKP,InfoKP,KnapSackNH>(), new Remove()),
		};
		return llhs_rr;
	}

	@Override
	public String toString() {
		return "0-1 Knapsack Problem";
	}

	@SuppressWarnings("unchecked")
	@Override
	public CrossoverHeuristic<SolutionKP, InfoKP>[] getCrossoverHeuristics() {
		CrossoverHeuristic<SolutionKP, InfoKP>[] llhs_xo = new CrossoverHeuristic[]{
				new IntersectCrossover(),
				new UnionCrossover(new SelectFirst<SolutionKP,InfoKP,UnionNH>()),
				new UnionCrossover(new SelectBest<SolutionKP,InfoKP,UnionNH>(false))
		};
		return llhs_xo;
	}
	
	public int solveExact(){
		// Input:  => instance
		// Number of distinct items (n)
		int n = instance.getNitems();
		// Values (stored in array v)
		int[] v = new int[n];
		for(int i = 0; i < n;i++){
			v[i] = instance.getProfit(i);
			if(v[i] < 1){
				System.out.println("error");
			}
		}
		// Weights (stored in array w)
		int[] w = new int[n];
		for(int i = 0; i < n;i++){
			w[i] = instance.getWeight(i);
			if(w[i] < 1){
				System.out.println("error");
			}
		}
		// Knapsack capacity (W)
		int W = instance.getCapacity();
		instance = null;
		// m
		/*
		int[][] m = new int[n+1][W+1];
		
		//for j from 0 to W do
		//  m[0, j] := 0
		//end for
		for(int j = 0; j < W; j++){
			m[0][j] = 0;
		}
		//for i from 1 to n do
		//	  for j from 0 to W do
		//	    if w[i] <= j then
		//	      m[i, j] := max(m[i-1, j], m[i-1, j-w[i]] + v[i])
		//	    else
		//	      m[i, j] := m[i-1, j]
		//	    end if
		//	  end for
		//	end for
		for(int i = 1; i <= n;i++){
			for(int j = 0; j <= W; j++){
				if(w[i-1] <= j){
					m[i][j] = Math.max(m[i-1][j], m[i-1][j-w[i-1]] + v[i-1]);
				}else{
					m[i][j] = m[i-1][j];
				}
			}
		}
		return m[n][W];
		*/
		int[] m = new int[W+1];
		for(int i = 1; i <= n;i++){
			for(int j = W; j >= 0; j--){
				if(w[i-1] <= j){
					m[j] = Math.max(m[j], m[j-w[i-1]] + v[i-1]);
				}
			}
		}
		return m[W];
	}

}
