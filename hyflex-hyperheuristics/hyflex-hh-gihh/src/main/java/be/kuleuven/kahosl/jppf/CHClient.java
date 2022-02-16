/*
    Copyright (c) 2012  Mustafa MISIR, KU Leuven - KAHO Sint-Lieven, Belgium
 	
 	This file is part of GIHH v1.0.
 	
    GIHH is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    	
*/

package be.kuleuven.kahosl.jppf;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jppf.client.JPPFClient;
import org.jppf.server.protocol.JPPFTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.kahosl.acceptance.AcceptanceCriterionType;
import be.kuleuven.kahosl.problem.ProblemName;
import be.kuleuven.kahosl.selection.SelectionMethodType;
import be.kuleuven.kahosl.util.Vars;
import be.kuleuven.kahosl.util.WriteInfo;

/**
 * This class is responsible for submitting search/optimisation tasks for JPPF
 * (Java Parallel Programming Framework - www.jppf.org)
 */
public class CHClient {
	
	/** A logger to write various running information/comments/errors **/
	private static final Logger log = LoggerFactory.getLogger(CHClient.class);
	
	private static long seed = 1234;

	/** Heuristic selection mechanisms **/
	private static SelectionMethodType[] selectionList = { SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD };

	/** Move acceptance mechanisms **/
	private static AcceptanceCriterionType[] acceptanceList = { AcceptanceCriterionType.AdaptiveIterationLimitedListBasedTA };
	
	/** Problems to solve **/
	private static ProblemName[] problemList = {ProblemName.MaxSAT,
												//ProblemName.FlowShop,
												//ProblemName.BinPacking,
												//ProblemName.PersonelScheduling,
												//ProblemName.TravellingSalesman,
												//ProblemName.VehicleRouting
										        }; 	
		
	/** Number of instances for each problem **/
	//private static int[] instanceNumber = {12,12,1s2,12,10,10};
	private static int[] instanceNumber = {2};

	
	/**
	 * Main function
	 * 
	 * @param args	arguments for the main function
	 */
	public static void main(String[] args) {
		
		Vars.totalExecutionTime = 10000; /* Execution time in milliseconds */
		Vars.numberOfTrials = 2; /* Number of trials */
		
		String resultFileName;
		
		Date today = new Date();
		Format formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
		String todayDate = formatter.format(today);
		
		WriteInfo.resultSubFolderName = todayDate;
		

		System.out.println("Hello...");
		// make a new JPPFClient, this client will connect to the JPPF-system
		JPPFClient client=new JPPFClient();
		// make a new List, it will contain all tasks that have to be executed
		List<JPPFTask> taskList = new ArrayList<JPPFTask>();		
		System.out.println("Creating the tasks...");
		// we will now create the tasks we want to execute
		// and add them to the list of tasks that have to be executed
		
		/* For each problem */
		for(int pr = 0; pr < problemList.length; pr++){
			/* For each problem instance */
			for(int ins = 0; ins < instanceNumber[pr]; ins++){ 
				/*For each heuristic selection mechanism */
				for(int hs = 0; hs < selectionList.length; hs++){
					/*For each move acceptance mechanism */
					for(int ac = 0; ac < acceptanceList.length; ac++){
						/* For each trial */
						for(int tr = 0; tr < Vars.numberOfTrials; tr++){

							resultFileName = selectionList[hs].toString()+"_"+acceptanceList[ac].toString()+"_"+ 
											 problemList[pr].toString().replace(" ", "")+"_INST"+ins+
											 "_TM"+(int)(Vars.totalExecutionTime/1000.0)+
											 "_TTR"+Vars.numberOfTrials+"_TR"+(tr+1)+"_";
							
							System.out.println(" @@ "+resultFileName);
							
							CHTask b = new CHTask(selectionList[hs],
									              Vars.phaseLength,
									              acceptanceList[ac],
									              problemList[pr], ins, 
									              Vars.totalExecutionTime, 
									              WriteInfo.resultSubFolderName, resultFileName,
									              seed);
							taskList.add(b);	
							System.err.println("Now have "+taskList.size()+" tasks  ::  "+resultFileName);
						}
					}
				}
			}
		}
				
		

		log.info("Tasks created!"+taskList.size());
		log.info("Submitting tasks...");
		
		boolean succes = false;
		while(!succes){
			try {
				long stop, start=System.currentTimeMillis();
				client.submit(taskList, null);
				stop = System.currentTimeMillis();
			
				log.info("Execution of all tasks took: "+(stop-start)+" ms");
				succes=true;
			} catch (Exception e) {
				log.error("Connection failed..."+e);
			}
		}
		log.info("All tasks are completed...");
	}
}
