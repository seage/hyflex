/*  
    Adapted from ExampleRun1.java (chesc.jar @ http://www.asap.cs.nott.ac.uk/external/chesc2011/hyflex_download.html)
*/

package be.kuleuven.kahosl.hyperheuristic;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import be.kuleuven.kahosl.acceptance.AcceptanceCriterionType;
import be.kuleuven.kahosl.selection.SelectionMethodType;
import be.kuleuven.kahosl.util.WriteInfo;
import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import SAT.SAT;

/**
 * This class show hows to run GIHH
 */
public class ExampleRun {	
	
	public static void main(String[] args) {
		
		long seed = 1234;
		long totalExecutionTime = 10000;
		
		SelectionMethodType selectionType = SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD;
		AcceptanceCriterionType acceptanceType = AcceptanceCriterionType.AdaptiveIterationLimitedListBasedTA;
		
		String resultFileName = "GIHH_";
		
		Date today = new Date();
		Format dateFormatter = new SimpleDateFormat("ddMMyyyyHHmmss");
		WriteInfo.resultSubFolderName = dateFormatter.format(today);

		//create a ProblemDomain object with a seed for the random number generator
		ProblemDomain problem = new SAT(seed);

		//creates an HyperHeuristic object with a seed for the random number generator
		HyperHeuristic hyper_heuristic_object = new GIHH(seed, problem.getNumberOfHeuristics(),totalExecutionTime,
				                                               resultFileName, selectionType, acceptanceType);

		//we must load an instance within the problem domain, in this case we choose instance 2
		problem.loadInstance(2);
		
		//we must set the time limit for the hyper-heuristic in milliseconds
		hyper_heuristic_object.setTimeLimit(totalExecutionTime);

		//a key step is to assign the ProblemDomain object to the HyperHeuristic object. 
		//However, this should be done after the instance has been loaded, and after the time limit has been set
		hyper_heuristic_object.loadProblemDomain(problem);

		//now that all of the parameters have been loaded, the run method can be called.
		//this method starts the timer, and then calls the solve() method of the hyper_heuristic_object.
		hyper_heuristic_object.run();

		//obtain the best solution found within the time limit
		System.out.println("\n\n BEST SLN FOUND: "+hyper_heuristic_object.getBestSolutionValue());
	}
}
