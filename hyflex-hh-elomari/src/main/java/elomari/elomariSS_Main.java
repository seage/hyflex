package Examples;

import java.util.Random;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

import BinPacking.BinPacking;
import FlowShop.FlowShop;
import PersonnelScheduling.PersonnelScheduling;
import SAT.SAT;

public class elomariSS_Main 
{
	static long seed = System.currentTimeMillis();
	//create an object of jawadHH(seed)... 
	static HyperHeuristic LLH_object = new elomariSS(seed);
	
	//create an object of each problem domain
	private static ProblemDomain loadProblemDomain(short index, long instanceSeed)
	{
		ProblemDomain p = null;
		
		switch (index) 
		{
			case 0: p = new SAT(instanceSeed); break; 
			case 1: p = new BinPacking(instanceSeed); break;
			case 2: p = new PersonnelScheduling(instanceSeed); break;
			case 3: p = new FlowShop(instanceSeed); break;
			default: System.err.println("No problem domain with this index!");
			System.exit(1);
		}
		return p;
	}
	
	//main block
	public static void main(String[] args) 
	{
		//specify the instances to use
		short[][] instances_to_use = new short[4][];
		short[] sat = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		short[] bp =  {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		short[] ps =  {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		short[] fs =  {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		instances_to_use[0] = sat;
		instances_to_use[1] = bp;
		instances_to_use[2] = ps;
		instances_to_use[3] = fs;
	
		//declarations and initializations 
		Random random_number_generator = new Random(seed);//change system.nanoTime() to a const. to control the randomness
		long instance_seed = 0;
		short problem_domain_index = 0;
		short instance = 0;
		short number_of_replications = 5;
		short replication = 0;
		
		//print headers
		System.out.println("ProblemDomain" + "\t" + "Instance" + "\t" + "Replication" + "\t" + "Restart" + "\t" + "Generation" + "\t" + "BestSoFar" + "\t" + "ElapsedTime");
		
		//loop through the problem domains and apply all the LLH
		for (problem_domain_index = 0; problem_domain_index < 4; problem_domain_index++) 
		{			
			//loop through the ten instances in the current problem domain
			for (instance = 0; instance < 10; instance++) 
			{	
				for (replication = 0; replication < number_of_replications; replication++)
				{
					//to ensure that all LLH begin from the same initial solution, we set a seed for each instance
					instance_seed = random_number_generator.nextInt();
					
					//create a problem domain object and an instance seed
					ProblemDomain problem_domain_object = loadProblemDomain(problem_domain_index, instance_seed);
					
					//load instanceToUse to the problem_domain_object
					problem_domain_object.loadInstance(instances_to_use[problem_domain_index][instance]);
					
					//set the time limit for learningPhaseHeuristic
					LLH_object.setTimeLimit(600000);
									
					//load problem_domain_object to LLH_object
					LLH_object.loadProblemDomain(problem_domain_object);
					
					//run the hyper-heuristic
					LLH_object.run();
				}
			}
		}
	}
}
