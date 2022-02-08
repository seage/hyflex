package ShafiXCJ;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * eXplore-Climb-Jump (XCJ)
 * Kamran Shafi and Hussein A. Abbass
 * University of New South Wales
 * Australian Defence Force Academy
 * Canberra, Australia 
 */

public class ShafiXCJ extends HyperHeuristic {
	
	/**
	 * creates a new ExampleHyperHeuristic object with a random seed
	 */
	public ShafiXCJ(long seed){
		super(seed);
	}
	
	/**
	 * This method defines the strategy of the hyper-heuristic
	 * @param problem the problem domain to be solved
	 */
	public void solve(ProblemDomain problem) {
		
		int i, j;
		int improvement_steps = 10;
		int heuristic_to_apply, best_heuristic = 0;
		double curr_obj_value, prev_obj_value, best_obj_value;
		
		//initialize variables
		int number_of_heuristics = problem.getNumberOfHeuristics();
		int[] local_search_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
		int[] mutation_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
		int[] crossover_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
		int[] ruin_recreate_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
		int num_rr_hs = ruin_recreate_heuristics.length;
		int num_xo_hs = crossover_heuristics.length;
		int num_mut_hs = mutation_heuristics.length;
		int num_ls_hs = local_search_heuristics.length;
		int number_explore_heuristics = num_rr_hs + num_xo_hs;		
		ArrayList<Integer> explore_heuristic_indices = new ArrayList<Integer>(number_explore_heuristics);
		int number_exploit_heuristics = num_ls_hs + num_mut_hs;
		ArrayList<Integer> exploit_heuristic_indices =new ArrayList<Integer>(number_exploit_heuristics);

		//
		for (i=0; i<num_rr_hs; i++)
		{
			explore_heuristic_indices.add(ruin_recreate_heuristics[i]);
		}		
		for (i=0; i<num_xo_hs; i++)
		{
			explore_heuristic_indices.add(crossover_heuristics[i]);
		}		
	
		for (i=0; i<num_ls_hs; i++)
		{
			exploit_heuristic_indices.add(local_search_heuristics[i]);
		}
		for (i=0; i<num_mut_hs; i++)
		{
			exploit_heuristic_indices.add(mutation_heuristics[i]);
		}
		
		problem.setMemorySize(number_of_heuristics+1);		
		problem.initialiseSolution(0);
		curr_obj_value = problem.getFunctionValue(0);
		best_obj_value = curr_obj_value;
		for (i=0; i<number_explore_heuristics; i++)
		{
			int t = explore_heuristic_indices.get(i)+1;
			problem.initialiseSolution(t);			
			curr_obj_value = problem.getFunctionValue(t);
			best_obj_value = curr_obj_value;
			if (curr_obj_value < best_obj_value)
			{
				best_obj_value = curr_obj_value;
			}
		}
		while (!hasTimeExpired())
		{
			//generate a distinct solution using each explore heuristic
			for (i=0; i<number_explore_heuristics; i++)
			{
				heuristic_to_apply = explore_heuristic_indices.get(i);
				if (Arrays.binarySearch(crossover_heuristics, heuristic_to_apply) > -1)
				{
					curr_obj_value = problem.applyHeuristic(heuristic_to_apply, 0, heuristic_to_apply+1, heuristic_to_apply+1);					
				}
				else
				{
					curr_obj_value = problem.applyHeuristic(heuristic_to_apply, 0, heuristic_to_apply+1);
				}
			}			
			//apply multiple local search heuristics on each explored solution
			for (i=0; i<number_explore_heuristics; i++)
			{
				for (j=0; j<number_exploit_heuristics; j++)
				{
					heuristic_to_apply = exploit_heuristic_indices.get(j);
					prev_obj_value = problem.getFunctionValue(explore_heuristic_indices.get(i)+1);
					int improved = improvement_steps;
					while (!hasTimeExpired() && improved > 0)
					{
						curr_obj_value = problem.applyHeuristic(heuristic_to_apply, explore_heuristic_indices.get(i)+1, heuristic_to_apply+1);
						double delta = 0.0;
						if (curr_obj_value < best_obj_value)
						{
							best_obj_value = curr_obj_value;
							best_heuristic = heuristic_to_apply;
							improved = improvement_steps;
							problem.copySolution(best_heuristic+1, 0);
						}
						else if (curr_obj_value >= prev_obj_value)
						{
							improved--;
							delta = 1.0 - prev_obj_value/curr_obj_value;
						}
						else
						{
							improved = improvement_steps;
							prev_obj_value = curr_obj_value;
						}
						if (delta < 0.2)
						{
							problem.copySolution(heuristic_to_apply+1, explore_heuristic_indices.get(i)+1);
						}
					}
				}
			}
		}
	}

	/**
	 * this method must be implemented, to provide a different name for each hyper-heuristic
	 * @return a string representing the name of the hyper-heuristic
	 */
	public String toString() {
		return "ShafiXCJ";
	}
}

