package Examples;


import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

/**
 * This class shows an example hyper-heuristic which demonstrates that each ProblemDomain object has a modifiable solution memory. 
 * it is intended to be read after the ExampleHyperHeuristic1 class has been understood.
 * <p>
 * this hyper-heuristic maintains a memory of ten solutions. A randomly chosen low level heuristic is applied to the
 * best solution in the memory, and the solution is immediately accepted. the new solution overwrites the old solution
 * at the same memory index. If the solution is worse, then the best solution in the memory will most likely be different now.
 * therefore, the memory is searched for the current best solution, so that it can be used in the next iteration.
 */

public class ExampleHyperHeuristic2 extends HyperHeuristic {
	
	/**
	 * creates a new ExampleHyperHeuristic object with a random seed
	 */
	public ExampleHyperHeuristic2(long seed) {
		super(seed);
	}
	
	/**
	 * This method defines the strategy of the hyper-heuristic
	 * @param problem the problem domain to be solved
	 */
	public void solve(ProblemDomain problem) {
		
		//in this example, we will implement a methodology that employs ten solutions in memory
		int solutionmemorysize = 10;
		
		//we set the memory size in the problem domain object to ten
		problem.setMemorySize(solutionmemorysize);
		
		//it is often a good idea to record the number of low level heuristics, as this changes depending on the problem domain
		int hs = problem.getNumberOfHeuristics();

		//initialise the variable which keeps track of the current objective function value
		double[] current_obj_function_values = new double[solutionmemorysize];

		//we keep track of the best solution in the memory, and the objective function value of this solution
		int best_solution_index = 0;
		double best_solution_value = Double.POSITIVE_INFINITY;

		//we initialise the hyper-heuristic by checking the objective function values of the initial solutions at each index of the memory
		for (int x = 0; x < solutionmemorysize; x++) {
			
			//randomly initialise the solution at this index
			problem.initialiseSolution(x);
			
			//save the objective function value of the solution at this index
			current_obj_function_values[x] = problem.getFunctionValue(x);
			//if this solution is the new best, then save it as such
			if (current_obj_function_values[x] < best_solution_value) {
				best_solution_value = current_obj_function_values[x];
				best_solution_index = x;
			}
		}
		//the main loop of any hyper-heuristic, which checks if the time limit has been reached
		while (!hasTimeExpired()) {

			//this hyper-heuristic chooses a random low level heuristic to apply
			int heuristic_to_apply = rng.nextInt(hs);

			//apply the randomly chosen heuristic to the current best solution in the memory
			//we accept every move, so the new solution can be immediately written to the same index in memory
			current_obj_function_values[best_solution_index] = problem.applyHeuristic(heuristic_to_apply, best_solution_index, best_solution_index);

			//if the solution has got worse, we must find the new best solution in the memory
			if (current_obj_function_values[best_solution_index] > best_solution_value) {
				best_solution_value = Double.POSITIVE_INFINITY;
				for (int x = 0; x < solutionmemorysize; x++) {
					if (current_obj_function_values[x] < best_solution_value) {
						best_solution_value = current_obj_function_values[x];
						best_solution_index = x;
					}
				}
			}
			//one iteration has been completed, so we return to the start of the main loop and check if the time has expired 
		}
	}
	
	/**
	 * this method must be implemented, to provide a different name for each hyper-heuristic
	 * @return a string representing the name of the hyper-heuristic
	 */
	public String toString() {
		return "Example Hyper Heuristic Two";
	}
}
