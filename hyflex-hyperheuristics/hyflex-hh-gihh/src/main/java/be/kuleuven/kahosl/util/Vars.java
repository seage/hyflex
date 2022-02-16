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

package be.kuleuven.kahosl.util;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.kahosl.acceptance.DutyType;

/**
 * This class consists of a number of parameter used within GIHH.
 */
public class Vars implements Serializable {
	
	/** The total execution time allowed (stopping condition) **/
	public static long totalExecutionTime = 600000;
	/** The number of trials for solving each problem instance **/
	public static int numberOfTrials = 10; 
	
	/** Whether the heuristics' parameters are oscillated in case of getting stuck**/
	public final static boolean oscilateLOCValues = true; 
	
	/** **/
	public final static boolean enableLAUpdateForSelection = true;
	
	/** Whether the length of the threshold list of the acceptance mechanism can be updated **/
	public final static boolean enableUpdateListLength = true; 
	

	/*
	 * Parameters used to calculate 'phase' related values for the selection mechanism  
	 */
	/** A constant multipled used to calculate new phase length values **/
	public final static int numberOfPhasesRequestedMultiplier = 25;
	/** The number of phases requested during a whole search **/
	public static int numberOfPhasesRequested = 100;
	/** Duration of a phase for achieving the requested number of phases **/
	public static double phaseDuration = totalExecutionTime/numberOfPhasesRequested;  
	
	
	/*
	 * Re-initialisation related parameters
	 */
	/** Whether re-initialisation is disabled **/
	public static boolean noMoreRestart = false;
	/** Whether re-initialise a current solution **/
	public static boolean restartSearch = false; 
	/** Whether to use overall best solution found/visited after re-initialisations **/
	public static boolean useOverallBestSln = false;
	/*******************************************/
	
	/** Whether a search process is at stuck **/
	public static boolean isAtStuck = false;
	
	/** The lower bound for the tabu duration in the relay hybridisation **/
	public static int tabuDurationLBForFC = 1; 
	/** The upper bound for the tabu duration in the relay hybridisation **/
	public static int tabuDurationUBForFC = 10;
	
	/** The upper bound for the tabu duration (d) in the heuristic selection mechanism **/
	public static int tabuDurationUB;
	/** The tabu duration (d) multiplier to calculate the tabu duration upper bound in the heuristic selection mechanism **/
	public static int tabuDurationUBMultiplier = 2; 
	
	/*
	 * Phase length related parameters for the heuristic selection mechanism
	 */
	/** Phase length (pl) **/
	public static int phaseLength;
	/** A constant value used to calculate the phase length **/
	public static int PLFactor = 500;
	/** The default tabu duration (d) value **/
	public static int tabuDuration;
	/** Whether the phase length is calculated based on speed of the non-tabu heuristics **/
	public final static boolean timeBasedPhaseLength = true;
	/** Minimum phase execution time required per iteration **/
	public static double minPhaseTimePerIter = -1.0;
	/** A constant value used to calculate the phase length **/
	public final static int minPhaseFactor = 50; 
	
	/** Whether time based information is used to calculate heuristics' selection probabilities **/
	public final static boolean timeBasedLA = true;//true
	
	/** A multiplier showing the speed of a heuristic with respect to the fastest non-tabu heuristic **/
	public static int learningRateMultiplier = 1;
	
	/** A constant value used to calculate the performance of the heuristic at the end of each phase **/
	public static double powerOfForNewBestSolutionsPerfM0 = 2;
	
	/** Sensitivity level considered while using time information **/
	public static double execTimeSensitivity = 0.001;
	
	/** The lower bound on the iteration limit for the acceptance mechanism **/
	public final static int iterationLimitLB = 5; 
	
	
	/*
	 * Acceptance mechanism related parameters
	 */
	/** The list size of the thresolh list in the acceptance mechanism **/
	public static int aillaListSize = 10;
	/** The list length used while calculating new iteration limit (k) value **/
	public static int aillaIterAdaptationListLength = 5;
	/** A constant value used to calculate the iteration limit (K) for increasing the threshold level **/
	public static int aillaAdaptationLimitMultiplier = 5;
	
	/** A constant values used to calculate the iteration limit (k) **/
	public static double aillaV6IterLimitAdaptationBase = 0.5;
	/** Whether the iteration limit is determined based on time **/
	public static boolean aiilaV6WithTimeFactor = true;

	/** Upper bound for the learning rates **/
	public final static double learningRateLimit = 0.5;	
	
	
	/**
	 * Calculate new phase length (pl) and new tabu duration (d)
	 * 
	 * @param numberOfHeuristics	number of heuristics
	 * @param PLFactor				phase length factor
	 * @return						tabu duration (d)
	 */
	public static int calculateDHSParams(int numberOfHeuristics, int PLFactor){
		double tabuDuration = Math.sqrt(2*numberOfHeuristics); 
		Vars.tabuDuration = (int)tabuDuration;
		if(tabuDuration-(Math.floor(tabuDuration)) >= 0.5)
			Vars.tabuDuration += 1; 
		
		tabuDurationUB = Vars.tabuDuration*tabuDurationUBMultiplier;
		
		phaseLength = (int)(Vars.tabuDuration*PLFactor);
		
		if(phaseLength < (numberOfHeuristics*minPhaseFactor)){
			phaseLength = numberOfHeuristics*minPhaseFactor;
		}
		
		return Vars.tabuDuration;
	}
	
	/**
	 * Calculate new phase length referring to a number of iterations to capture the performance of the heuristics
	 * 
	 * @param numberOfHeuristics	number of heuristics
	 * @return						phase length
	 */
	public static int calculatePhaseLength(int numberOfHeuristics){
		double tabuDuration = Math.sqrt(2*numberOfHeuristics); 
		if(tabuDuration-(Math.floor(tabuDuration)) >= 0.5)
			tabuDuration += 1; 
		
		double tempPL = ((int)(tabuDuration)*PLFactor);
		
		if(tempPL < (numberOfHeuristics*minPhaseFactor)){
			tempPL = numberOfHeuristics*minPhaseFactor;
		}
		
		return (int)tempPL;
	}

}
