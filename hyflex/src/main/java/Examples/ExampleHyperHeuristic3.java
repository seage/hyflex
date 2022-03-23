package Examples;


import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

/**
 * This class presents an example hyper-heuristic which demonstrates how to use the different types of low level heuristic 
 * in each Problem Domain. it is intended to be read after the ExampleHyperHeuristic2 class has been understood.
 * There are four types of low level heuristic within each domain of the competition software:
 * <br>
 * 1) Local Search<br>
 * 2) Mutation<br>
 * 3) Ruin-Recreate<br>
 * 4) Crossover<br>
 * <p>
 * the methods to retrieve the indices of the heuristics of each type are given in the example below.
 * if there are no heuristics of a certain type, then the method returns null. Using the information about the type of the heuristic 
 * is potentially powerful, but it is important to check that the problem domain contains heuristics of that type, by checking 
 * for equality with null. Examples of such checks can be seen in this class.
 * <p>
 * to apply a crossover heuristic, a different method must be used, which supplies two input solutions instead of one.
 * it is possible to supply the index of a crossover heuristic to the method which applies a non-crossover heuristic.
 * if this happens, the method will return the input solution unmodified. Therefore, the hyper-heuristic
 * strategy implemented in ExampleHyperHeuristic1.java occasionally chooses a crossover heuristic which does nothing, as it
 * is applied with only one input solution.
 * 
 */

public class ExampleHyperHeuristic3 extends HyperHeuristic {

	/**
	 * creates a new ExampleHyperHeuristic object with a random seed
	 */
	public ExampleHyperHeuristic3(long seed) {
		super(seed);
	}

	/**
	 * This method defines the strategy of the hyper-heuristic
	 * @param problem the problem domain to be solved
	 */
	public void solve(ProblemDomain problem) {
		//obtain arrays of the indices of the ow level heuristics heuristics which correspond to the different types.
		//the arrays will be set to 'null' if there are no low level heuristics of that type
		int[] local_search_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
		int[] mutation_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
		int[] crossover_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
		//in this example, we do not use the ruin-recreate heuristics, but this is how you would obtain them:
		//int[] ruin_recreate_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);

		//this code is from examplehyperheuristic2, and is explained fully in that example
		int solutionmemorysize = 10;
		problem.setMemorySize(solutionmemorysize);
		double[] current_obj_function_values = new double[solutionmemorysize];
		int best_solution_index = 0;
		double best_solution_value = Double.POSITIVE_INFINITY;
		for (int x = 0; x < solutionmemorysize; x++) {
			problem.initialiseSolution(x);
			current_obj_function_values[x] = problem.getFunctionValue(x);
			if (current_obj_function_values[x] < best_solution_value) {
				best_solution_value = current_obj_function_values[x];
				best_solution_index = x;
			}
		}

		//the main loop of any hyper-heuristic, which checks if the time limit has been reached
		while (!hasTimeExpired()) {

			//this hyper-heuristic first randomly chooses a mutation heuristic to apply
			int heuristic_to_apply = 0;
			//we must check that there are some mutational heuristics in this problem domain
			if (mutation_heuristics != null) {
				heuristic_to_apply = mutation_heuristics[rng.nextInt(mutation_heuristics.length)];
			} else {//we apply a randomly selected heuristic if there are no mutational heuristics
				heuristic_to_apply = rng.nextInt(problem.getNumberOfHeuristics());
			}

			//apply the randomly chosen heuristic to the current best solution in the memory
			//we accept every move, so the new solution can be immediately written to the same index in memory
			problem.applyHeuristic(heuristic_to_apply, best_solution_index, best_solution_index);

			//secondly, this hyper-heuristic applies a randomly chosen local search heuristic to the new solution
			heuristic_to_apply = 0;
			//we must check that there are some local search heuristics in this problem domain
			if (local_search_heuristics != null) {
				heuristic_to_apply = local_search_heuristics[rng.nextInt(local_search_heuristics.length)];
			} else {//we apply a randomly selected heuristic if there are no local searchers
				heuristic_to_apply = rng.nextInt(problem.getNumberOfHeuristics());
			}
			
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

			//it is important to check first that there are some crossover heuristics to use
			if (crossover_heuristics != null) {

				//we now perform a crossover heuristic on two randomly selected solutions
				int solution_index_1 = rng.nextInt(solutionmemorysize);
				int solution_index_2 = rng.nextInt(solutionmemorysize);
				//if the solutions are the same, choose a different one
				while (true) {
					if (solution_index_1 == solution_index_2) {
						solution_index_2 = rng.nextInt(solutionmemorysize);
					} else {break;}}

				//we select a random crossover heuristic to use
				heuristic_to_apply = crossover_heuristics[rng.nextInt(crossover_heuristics.length)];

				//the method to apply crossover heuristics involves specifying two indices of solutions in the memory, and an index into which to put the result of the crossover.
				//in this example we give the randomly selected indices as input, and we overwrite the first parent with the resulting solution
				current_obj_function_values[solution_index_1] = problem.applyHeuristic(heuristic_to_apply, solution_index_1, solution_index_2, solution_index_1);

				
				//if we have overwritten the best solution with the result of the crossover, we must check for the new best solution
				if (solution_index_1 == best_solution_index) {
					//we only need to check for a new best solution if the new solution that replaced the best solution is worse
					if (current_obj_function_values[solution_index_1] > best_solution_value) {
						best_solution_value = Double.POSITIVE_INFINITY;
						for (int x = 0; x < solutionmemorysize; x++) {
							if (current_obj_function_values[x] < best_solution_value) {
								best_solution_value = current_obj_function_values[x];
								best_solution_index = x;
							}
						}
					}
				} 
				//if the result of the crossover is better than the best solution, then update the best solution record
				else if (current_obj_function_values[solution_index_1] < best_solution_value) {
					best_solution_value = current_obj_function_values[solution_index_1];
					best_solution_index = solution_index_1;
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
		return "Example Hyper Heuristic Three";
	}
}
