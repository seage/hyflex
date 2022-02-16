/**
 * 
 * An example of how to use Lean-GIHH to solve an instance of the HyFlex benchmark.
 * 
 * @author Steven Adriaensen
 * 
 */
package leangihh;

import java.util.Date;

import SAT.SAT;
import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;


public class ExampleRun {

	/**
	 * @param args: This example takes no command line arguments
	 */
	public static void main(String[] args) {
		long seed = new Date().getTime();
		
		//algorithm used (Lean-GIHH)
		HyperHeuristic algo = new LeanGIHH(seed);
		
		//benchmark instance solved (4th instance in the Maximum Satisfiability problem domain)
		ProblemDomain problem = new SAT(seed);
		int instance = 3;
		problem.loadInstance(instance);
		
		//time we're allowed to optimize (600000ms = 10min)
		long t_allowed = 600000;
		algo.setTimeLimit(t_allowed);

		algo.loadProblemDomain(problem);
		
		//start optimizing
		System.out.println("Testing "+algo+" for "+t_allowed+" ms on "+problem.getClass().getSimpleName()+"["+instance+"]...");
		algo.run();

		//print out quality of best solution found
		System.out.println(algo.getBestSolutionValue());
	}

}
