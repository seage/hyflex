/**
 * @author Steven Adriaensen
 * 
 * The ANW-HH hyperheuristic used in
 * 
 * Adriaensen, Steven, Gabriela Ochoa, and Ann Nowé. 
 * "A Benchmark Set Extension and Comparative Study for the HyFlex Framework." 
 * Evolutionary Computation (CEC), 2015 IEEE Congress on. IEEE, 2015.
 */

package hh;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

public class AcceptNoWorseHH extends HyperHeuristic {
	
	public AcceptNoWorseHH(long r) {
		super(r); 
	}
	
	public void solve(ProblemDomain problem) {
		int hs = problem.getNumberOfHeuristics();
		double obj = Double.POSITIVE_INFINITY;
		problem.initialiseSolution(0);
		while (!hasTimeExpired()) {
			int v = rng.nextInt(hs);
			double newobjfunction = problem.applyHeuristic(v, 0, 1);
			double delta = obj - newobjfunction;
			if (delta > 0) {
				problem.copySolution(1, 0);
				obj = newobjfunction;
			}
		}
	}

	public String toString() {
		return "ANW-HH";
	}

}
