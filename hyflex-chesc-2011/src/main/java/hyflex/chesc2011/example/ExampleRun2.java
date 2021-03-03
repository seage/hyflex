package hyflex.chesc2011.example;


import java.util.Random;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import BinPacking.BinPacking;
import FlowShop.FlowShop;
import PersonnelScheduling.PersonnelScheduling;
import SAT.SAT;

/**
 * This class shows an example of how to test a number of hyper heuristics on a number of problem domains. 
 * this class is similar to that which will be used to test the hyper-heuristics in the final competition.
 * it is intended to be read after studying ExampleRun1, because it is more complex, and so it is easier to 
 * understand if the commands in ExampleRun1 are understood first.
 * <p>
 * we suggest that the reader goes through the example code of this class, and then refers to the 
 * notes below, which provide further clarification.
 * <p>
 * For each run, a new object is created for the ProblemDomain, and a new object is created for the HyperHeuristic.
 * This is important because it ensures that the object (particularly the solution memory in the ProblemDomain object) 
 * is completely initialised before the next test. It is not sufficient to simply load a new problem instance into the
 * same object as this is not guaranteed to completely initialise the object for a new run.
 * <p>
 * The elapsed time printed at the end of each hyper-heuristic run may be slightly longer than the time limit for the 
 * hyper-heuristic, as the last low level heuristic to be called overruns the time limit. However, the 
 * hyper_heuristic_object.getBestSolutionValue() method disregards any solution found after the hyper-heuristic 
 * exceeds its time limit. So the best solution that is printed at the end has been found within the time limit.
 */
public class ExampleRun2 {

	/**
	 * This method creates the relevant HyperHeuristic object from the index given as a parameter.
	 * after the HyperHeuristic object is created, its time limit is set.
	 */
	private static HyperHeuristic loadHyperHeuristic(int index, long timeLimit, Random rng) {
		HyperHeuristic h = null;
		switch (index) {
		case 0: h = new ExampleHyperHeuristic1(rng.nextLong()); h.setTimeLimit(timeLimit); break;
		case 1: h = new ExampleHyperHeuristic2(rng.nextLong()); h.setTimeLimit(timeLimit); break;
		case 2: h = new ExampleHyperHeuristic3(rng.nextLong()); h.setTimeLimit(timeLimit); break;
		default: System.err.println("there is no hyper heuristic with this index");
		System.exit(1);
		}
		return h;
	}
	
	/**
	 * this method creates the relevant ProblemDomain object from the index given as a parameter.
	 * for each instance, the ProblemDomain is initialised with an identical seed for each hyper-heuristic.
	 * this is so that each hyper-heuristic starts its search from the same initial solution.
	 */
	private static ProblemDomain loadProblemDomain(int index, long instanceseed) {
		ProblemDomain p = null;
		switch (index) {
		case 0: p = new SAT(instanceseed); break;
		case 1: p = new BinPacking(instanceseed); break;
		case 2: p = new PersonnelScheduling(instanceseed); break;
		case 3: p = new FlowShop(instanceseed); break;
		default: System.err.println("there is no problem domain with this index");
		System.exit(1);
		}//end switch
		return p;
	}

	public static void main(String[] args) {
		//we first initialise the random number generator for this class
		//it is useful to generate all of the random numbers from one seed, so that the experiments can be easily replicated
		Random random_number_generator = new Random(12345);
		
		//we set the number of hyper-heuristics involved in this example experiment, 
		//and a time limit of 60 seconds for each hyper-heuristic run
		int number_of_hyperheuristics = 3;
		long time_limit = 10000;
		
		//this array is initialised with the indices of the instances that we wish to test the hyper-heuristics on
		//for this example we arbitrarily choose the indices of five instances from each problem domain
		int[][] instances_to_use = new int[4][];
		int[] sat = {0, 1, 2, 3, 4};
		int[] bp = {2, 3, 4, 5, 6};
		int[] ps = {4, 5, 6, 7, 8};
		int[] fs = {9, 0, 4, 2, 3};
		instances_to_use[0] = sat;
		instances_to_use[1] = bp;
		instances_to_use[2] = ps;
		instances_to_use[3] = fs;
		
		//loop through all four problem domains
		for (int problem_domain_index = 0; problem_domain_index < 4; problem_domain_index++) {
			
			//to ensure that all hyperheuristics begin from the same initial solution, we set a seed for each problem domain
			long problem_domain_seed = random_number_generator.nextInt();
			
			//loop through the five instances in the current problem domain
			for (int instance = 0; instance < 5; instance++) {
				
				//we retrieve the exact index of the instance we wish to use
				int instancetouse = instances_to_use[problem_domain_index][instance];
				
				System.out.println("Problem Domain " + problem_domain_index);
				System.out.println("	Instance " + instancetouse);
				
				//to ensure that all hyperheuristics begin from the same initial solution, we set a seed for each instance
				long instance_seed = problem_domain_seed*(instance+1);
				
				//loop through the hyper-heuristics that we will test in this experiment
				for (int hyper_heuristic_index = 0; hyper_heuristic_index < number_of_hyperheuristics; hyper_heuristic_index++) {

					//we create the problem domain object. we give the problem domain index and the unique 
					//seed for this instance, so that the problem domain object is initialised in the same way,
					//and each hyper-heuristic will begin from the same initial solution.
					ProblemDomain problem_domain_object = loadProblemDomain(problem_domain_index, instance_seed);
					
					//we create the hyper-heuristic object from the hyperheuristic index. we provide the time limit, 
					//which is set after the object is created in the loadHyperHeuristic method
					HyperHeuristic hyper_heuristic_object = loadHyperHeuristic(hyper_heuristic_index, time_limit, random_number_generator);
					
					//the required instance is loaded in the ProblemDomain object
					problem_domain_object.loadInstance(instancetouse);
					
					//critically, the ProblemDomain object is provided to the HyperHeuristic object, so that it knows which problem to solve
					hyper_heuristic_object.loadProblemDomain(problem_domain_object);

					//now that all objects have been initialised, the current hyper-heuristic is run on the current instance to produce a solution
					hyper_heuristic_object.run();

					//for this example, we use the record within each problem domain of the number of times each low level heuristic was called.
					//we sum the results to obtain the total number of times that a low level heuristic was called
					int[] i = problem_domain_object.getHeuristicCallRecord();
					int counter = 0;
					for (int y : i) { counter += y; }
					
					//we print the results of this hyper-heuristic on this problem instance
					//print the name of this hyper-heuristic
					System.out.print("\tHYPER HEURISTIC " + hyper_heuristic_object.toString());
					//print the best solution found within the time limit
					System.out.print("\t" + hyper_heuristic_object.getBestSolutionValue());
					//print the elapsed time in seconds
					System.out.print("\t" + (hyper_heuristic_object.getElapsedTime()/1000.0));
					//print the number of calls to any low level heuristic
					System.out.println("\t" + counter);
					
					
					double[] fitnesstrace = hyper_heuristic_object.getFitnessTrace();
					for (double f : fitnesstrace) {
						System.out.print(f + " ");
					}System.out.println();
				}
			}
		}
	}
}
