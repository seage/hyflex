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

package be.kuleuven.kahosl.selection;

import java.io.Serializable;
import java.util.Random;

import be.kuleuven.kahosl.analysis.PerformanceElements;
import be.kuleuven.kahosl.hyperheuristic.HeuristicClassType;
import be.kuleuven.kahosl.selection.AdaptiveLimitedLAassistedDHSMentorSTD.LevelOfChangeType;


/**
 * This class is an abstract class to implement the selection mechanism.
 * It can be extended to implement new selection mechanisms.
 */
public abstract class SelectionMethod implements Serializable {

	/** Random number generator **/
	Random r;

	/** The number of heuristics **/
	protected int numberOfHeuristics;
	/** The number of moves performed by each heuristic **/
	protected int[] numCalled;
	/** The number of iterations spent **/
	protected int iterations;
	
	/**
	 * Constructor for the selection mechanism
	 * 
	 * @param numberOfHeuristics	number of heuristics		
	 * @param r						random number generator		
	 */
	public SelectionMethod(int numberOfHeuristics, Random r)
	{
		this.numberOfHeuristics = numberOfHeuristics;
		this.r = r;
		numCalled = new int[numberOfHeuristics];
		for (int i = 0; i < numCalled.length; i++) {
			numCalled[i] = 0;
		}
		iterations = 0;
	}
	
	/** 
	 * Heuristic selection method
	 * 
	 * @return	index of the selected heuristic
	 */
	public abstract int selectHeuristic();
	
	/**
	 * Get number of tabu (excluded) heuristics 
	 * 
	 * @return	number of tabu (excluded) heuristics 
	 */
	public abstract int getNumberOfTabuHeuristics();
	
	/**
	 * Get tabu duration of each heuristic
	 * 
	 * @return	an array of tabu durations
	 */
	public int[] getTabuDurationList(){ return null;};
	
	/**
	 * Get the probability list involving heuristics' selection probabilities 
	 * 
	 * @return	the probability list involving heuristics' selection probabilities in a string
	 */
	public String getProbabilityListAsString(){return "";}
	
	/**
	 * Get tabu duration of each heuristic
	 * 
	 * @return	tabu duration of each heuristic in a string
	 */
	public String getTabuDurationListAsStr() { return "";}	
	
	/**
	 * Get rank of each heuristic
	 * 
	 * @return heuristics' ranks in a string	
	 */
	public String getRankListAsStr(){return "";}
		
	/**
	 * Get the probability list involving first heuristics' selection probabilities in the relay hybridisation
	 *  
	 * @return	the probability list involving first heuristics' selection probabilities in the relay hybridisation in a string
	 */
	public String getProbListForRelayAsString(){ return ""; };
	
	/**
	 * Select the first heuristic for the relay hybridisation
	 * 
	 * @return	heuristic index of the selected heuristic
	 */
	public int selectTheFirstHeuristicForRelay() { return -1; }
	
	/** 
	 * Update the level of change values (heuristics' parameter values) for a given heuristic 
	 * 
	 * @param lcType		level of change type
	 * @param hClassType	heuristic class type
	 * @param heurIndex		index of the given heuristic 
	 */
	public void updateLevelOfChange(LevelOfChangeType lcType, HeuristicClassType hClassType, int heurIndex){ 
		System.out.println("@updateLevelOfChange => This method is not present for this object!"); System.exit(1);
	}
	
	/**
	 * Get the level of change list involving the heuristics' parameter values
	 * 
	 * @return an array of the heuristics' parameter values
	 */
	public double[] getLevelOfChangeList(){ 
		System.out.println("@getLevelOfChangeList => This method is not present for this object!"); System.exit(1); return null;
	}
	
	/**
	 * Get the level of change list for the heuristics' parameter values in a string
	 * 
	 * @return the heuristics' parameter values in a string
	 */
	public String getLevelOfChangeListAsString(){
		System.out.println("@printLevelOfChangeList => This method is not present for this object!"); System.exit(1); return null;
	}
	
	
	/**
	 * Update the heuristics' selection probabilities and level of change values 
	 * 
	 * @param heuristicIndex		index of a heuristic
	 * @param heuristicClassType	heuristic class type
	 * @param fitnessBefore			quality (fitness) of the solution before applying the corresponding heuristic
	 * @param fitnessAfter			quality (fitness) of the solution after applying the corresponding heuristic
	 * @param bestFitness			current best fitness value
	 * @param heursiticStartTime	execution starting time of the corresponding heuristic
	 * @param heursiticEndTime		execution ending time of the corresponding heuristic 
	 * @param learningMultRateList	an array of units showing the speed of the heuristics with respect to the fastest non-tabu heuristic
	 * @param currPerformance		current performance status of the heuristics (during current phase)
	 */
	public void updateSelectionElements(int heuristicIndex, HeuristicClassType heuristicClassType,
            double fitnessBefore, double fitnessAfter, double bestFitness, 
            long heursiticStartTime, long heursiticEndTime, double[] learningMultRateList, PerformanceElements currPerformance){
		System.out.println("@updateSelectionElements => This method is not present for this object!"); System.exit(1);
	}
	
	/**
	 * Update the ranks and selection probabilities of the heuristics
	 * 
	 * @param performanceRate		an array of performance rates of heuristics
	 * @param worseningRate			an array of worsening performance rate of heuristics
	 * @param execTimeList			an array of spent execution time list by heuristics
	 * @param currPerformance		current performance status of the heuristics (during current phase)
	 * @param prevPerformance		preceding performance status of the heuristics (during preceding phase)
	 * @param learningMultRateList	an array of units showing the speed of the heuristics with respect to the fastest non-tabu heuristic
	 */
	public void update(double[] performanceRate, double[] worseningRate, double[] execTimeList, 
	           		   PerformanceElements currPerformance, PerformanceElements prevPerformance, double[] learningMultRateList){
		System.out.println("@update (STD) => This method is not present for this object!"); System.exit(1);
	}
	
	/**
	 * Update the selection probabilities of the heuristic 
	 * 
	 * @param heuristicIndex		index of the first heuristic applied by the relay hybridisation
	 * @param secondHeurIndex		index of the second heuristic applied by the relay hybridisation
	 * @param updateType			update type (0: worsening move, 1: new best move, 2: improving move)
	 * @param learningRateMultList	
	 */
	public void updateProbListForRelay(int heuristicIndex, int secondHeurIndex, int updateType, double[] learningRateMultList){
		System.out.println("@updateProbListForRelay (STD) => This method is not present for this object!"); System.exit(1);
	}
	
	/**
	 * Select an improving heuristic (used for choosing the second heuristic in the relay hybridisation)
	 * 
	 * @param heurClassTypeList	heuristics class types (based on introduced generic heuristic types)
	 * @return					index of the selected heuristic 					
	 */	
	public int selectAnImprovementHeuristic(HeuristicClassType[] heurClassTypeList){
		System.out.println("@selectAnImprovementHeuristic => This method is not present for this object!"); System.exit(1); return -1;
	}
}
