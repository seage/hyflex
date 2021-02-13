/**
 * 
 * An example of how to use Lean-GIHH to solve an instance of the HyFlex benchmark.
 * 
 * @author Steven Adriaensen
 * 
 */
package hyflex.chesc2011;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import fr.lalea.eph.EPH;
import kubalik.EvoCOPHyperHeuristic;
import leangihh.LeanGIHH;
import pearlhunter.PearlHunter;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import SAT.SAT;
import travelingSalesmanProblem.TSP;


public class HHRun {
	/**
	 * @param args: This example takes no command line arguments
	 */
	public static void main(String args[]) {
		if (args.length < 3){
			System.out.println("ERROR, not enough arguments, " + "("+args.length+"/3)");
			return;
		}
		if (args.length > 3){
			System.out.println("ERROR, too many arguments, " + "("+args.length+"/3)");
			return;
		}
		if (args[2].matches("\\d+") == false){
			System.out.println("ERROR, wrong number format");
			return;
		}
		//seed for random generator
		long seed = new Date().getTime();
		/**
		 * Hyper heuristic to use
		 * ----------------------
		 * LeanGIHH
		 * PearlHunter
		 * EPH
		 */
		final String AlgorithmName = args[0];
		
		/**
		 * Problems
		 * -----------------
		 * SAT
		 * TSP
		 */
		final String ProblemName = args[1];
		
		int[] sat = {3,5,4,10,11};
		int[] tsp = {0,8,2,7,6};
		HashMap<String, int[]> instances = new HashMap<>();
		instances.put("SAT", sat);
		instances.put("TSP", tsp);

		//time we're allowed to optimize (600000ms = 10min)
		final long T_ALLOWED = Integer.parseInt(args[2])*1000;
		
		List <Double> output = new ArrayList<Double>();

		for (int i = 0; i < instances.get(ProblemName).length; i++){
			ProblemDomain problem = HHRun.createProblem(ProblemName, seed);
			HyperHeuristic algorithm = HHRun.createAlgorithm(AlgorithmName, seed);
			
			int instanceIx = instances.get(ProblemName)[i];

			algorithm.setTimeLimit(T_ALLOWED);
			algorithm.loadProblemDomain(problem);
			problem.loadInstance(instanceIx);			

			//start optimizing
			System.out.println("Testing "+algorithm+" for "+T_ALLOWED+" ms on "+problem.getClass().getSimpleName()+"["+instanceIx+"]...");
			algorithm.run();

			//print out quality of best solution found
			System.out.println(algorithm.getBestSolutionValue());
			output.add(algorithm.getBestSolutionValue());
		}
		System.out.println(output);
	}

	private static HyperHeuristic createAlgorithm(String AlgorithmName, long seed) {
		switch(AlgorithmName) {
			case "LeanGIHH":
				return new LeanGIHH(seed);
			case "PearlHunter":
				return new PearlHunter(seed);
			case "EPH":
				return new EPH(seed);
      case "ISEA":
				return new EvoCOPHyperHeuristic(seed);
			default:
				System.out.println("ERROR, " + AlgorithmName + " INVALID INPUT");
				return null;
		}
	}

	private static ProblemDomain createProblem(String ProblemName, long seed) {
		switch(ProblemName) {
			case "SAT":
				return new SAT(seed);
			case "TSP":
				return new TSP(seed);
			default:
				System.out.println("ERROR, " + ProblemName + " INVALID INPUT");
				return null;
		}
	}

}
