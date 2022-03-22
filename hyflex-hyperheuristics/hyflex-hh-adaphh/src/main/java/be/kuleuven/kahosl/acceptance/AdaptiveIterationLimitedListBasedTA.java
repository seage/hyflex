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

package be.kuleuven.kahosl.acceptance;

import java.util.Random;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.kahosl.util.Print;
import be.kuleuven.kahosl.util.Vars;

/**
 * This class provides the implementation of the acceptance mechanism.
 */
public class AdaptiveIterationLimitedListBasedTA extends AcceptanceCriterion {
	
	/** A logger to write various running information/comments/errors **/
	private static final Logger log = LoggerFactory.getLogger(AdaptiveIterationLimitedListBasedTA.class);

	/** The iteration limit (k) **/
	private int iterationLimit;	

	/** A counter to check the iteration limit (k) **/
	private int limitCounter;	
	
	/** The iteration limit (K) to increase the threshold level **/
	private int iterationLimitForThresholdChange;	
	
	/** A counter to check the iteration limit (K) **/
	private int limitCounterForThresholdChange;		

	/** The threshold level **/
	private double threshold;	
	
	/** The index value showing the threshold level in the threshold level list **/
	private int thresholdIndex;	

	/** The iteration number when the most recent best solution is found **/
	private int lastBestFoundIter; 
	
	/** A multiplier used to determine iterationLimitForThresholdChange **/
	private int adaptationLimitMultiplier;	
	
	/** Execution start time **/
	private long startTime;		
	
	/** The total execution time limit **/
	private long totalExecTime;	
	
	/** The threshold list in a vector format **/
	private Vector<Double> bestFitnessList;	
	
	/** The length of the threshold list **/
	private int listLength;					
	
	/** An iteration related value to update the iteration limit (k) **/
	private int movingAvgImpBestIteration;	
	
	/** A counter indicating the number of worsening solutions accepted consecutively **/
	private int consecWorseMoveAcceptanceCounter; 
	
	/** A time related value used to update the iteration limit **/
	public double tempTimeFactor;	
	
	/** The list length used while calculating new iteration limit (k) value **/
	private int iterAdaptationListLength; 
	
	/** A threshold list to keep the best threshold list when the re-initialisation is active **/
	private Vector<Double> bestFitnessListForNoMoreRestartCase = new Vector<Double>();
	
	
	/**
	 * Constructor of AdaptiveIterationLimitedListBasedTA
	 * 
	 * @param	listLength					length of the threshold list
	 * @param	initialFitness				fitness value of the initial solution
	 * @param	adaptationLimitMultiplier	a multiplier to decide about when to increase the threshold level
	 * @param	totalExecTime				the total execution time allowed
	 */
	public AdaptiveIterationLimitedListBasedTA(int listLength, double initialFitness, int adaptationLimitMultiplier, long totalExecTime, Random r){
		super(r);
		this.adaptationLimitMultiplier = adaptationLimitMultiplier;
		
		this.totalExecTime = totalExecTime; 
		startTime = System.currentTimeMillis();
		
		this.listLength = listLength;
		this.adaptationLimitMultiplier = adaptationLimitMultiplier;
		
		limitCounter = 0;
		limitCounterForThresholdChange = 0;
		
    	numberOfIterations = 0;
    	
    	threshold = initialFitness;
    	
    	thresholdIndex = 1;
    	
    	lastBestFoundIter = 0;
    	
    	bestFitnessList = new Vector<Double>();
    	for(int i = 0; i < listLength; i++){
    		bestFitnessList.add(initialFitness);
    	}
    	
    	
    	movingAvgImpBestIteration = 0;
    	consecWorseMoveAcceptanceCounter = 0;
    	
    	iterationLimit = Vars.iterationLimitLB; /* Set initially the iteration limit to its lower bound */
    	movingAvgImpBestIteration = iterationLimit;	/* */
    	/* Set the iteration limit (K) which is used to increase the threshold level */
    	iterationLimitForThresholdChange = adaptationLimitMultiplier*iterationLimit; 
    	
    	tempTimeFactor = 1.0; /* Initialiase the time factor (linearly decreases to zero (0)) */
    	
    	iterAdaptationListLength = Vars.aillaIterAdaptationListLength;
	}
	

	/**
	 * Decision on whether to accept or reject a new solution
	 * 
	 * @param	newFitness		fitness value of the new solution
	 * @param	currentFitness	fitness value of the current solution
	 * @param	bestFitness		fitness value of the current best solution
	 * @return					the acceptance decision (accept: true - reject: false)
	 */
	@Override
	public boolean accept(double newFitness, double currentFitness, double bestFitness) {
		
		if(Vars.enableUpdateListLength){
			updateListLength(); 
		}
		
    	boolean acp = false;
		
    	//If the new solution is better than the current solution 
    	if(newFitness < currentFitness){
    		acp = true;
    		limitCounter = 0;
    		
    		if(newFitness < bestFitness){	
    			
    			/* Set isAtStuck status to false */
    			Vars.isAtStuck = false;
				
    			/* Add the fitness value of the new best solution to the threshold list */
				bestFitnessList.add(0,newFitness);
				/* Remove the oldest element in the threshold list */
				bestFitnessList.remove(bestFitnessList.size()-1);
				/* Reset the the counter used to check the threshold level increase */
				limitCounterForThresholdChange = 0;
				/* Set the thresholdIndex to the lowest value (preceding value to the new best solution's fitness) */
				thresholdIndex = 1; 
				/* Update the threshold level */
				threshold = bestFitnessList.get(thresholdIndex);

				/* Update the time factor */
				tempTimeFactor = 1.0*(totalExecTime-(System.currentTimeMillis()-startTime))/totalExecTime;
				if(tempTimeFactor < 0){
					tempTimeFactor = 0;
				}
				
				/* 
				 * Calculate the number of iterations passed to find the new best solution 
				 * starting when the earlier best solution was found 
				 */
				double passedIter = (numberOfIterations-lastBestFoundIter);

				/* Calculate the new iteration limit (k) */
				if(passedIter <= iterationLimit){
					movingAvgImpBestIteration = (int)Math.ceil((double)(((iterAdaptationListLength-1)*iterationLimit+passedIter)/iterAdaptationListLength));
				}else{
					double tempNewBestReqIter = 0.0;
					
					/* 
					 * if a worsening solution was not found even if passedIter >> iterationLimit
					 */
					consecWorseMoveAcceptanceCounter = (int)(passedIter/iterationLimit); 
					
					for(int i  = 0; i < consecWorseMoveAcceptanceCounter; i++){
						if(Vars.aiilaV6WithTimeFactor && i != 0){
							tempNewBestReqIter += iterationLimit*Math.pow(Vars.aillaV6IterLimitAdaptationBase, i)*tempTimeFactor;
						}else{
							tempNewBestReqIter += iterationLimit*Math.pow(Vars.aillaV6IterLimitAdaptationBase, i);
						}
					}
					
					/*  */
					movingAvgImpBestIteration = (int)Math.ceil((double)(((iterAdaptationListLength-1)*iterationLimit+tempNewBestReqIter)/iterAdaptationListLength));

					if(Print.acceptance){
						log.info("iterationLimit="+iterationLimit+"  ... tempNewBestReqIter="+tempNewBestReqIter+
								" ... tempTimeFactor="+tempTimeFactor+" :: consecWorseMoveAcceptanceCounter="+consecWorseMoveAcceptanceCounter+
								" :: movingAvgImpBestIteration="+movingAvgImpBestIteration+" :: passedIter="+passedIter);
					}
				}
				
				/* Set new iteration limit */
				iterationLimit = (int)movingAvgImpBestIteration;			
				
				/* Set iteration limit (k) to its predetermined lower bound if required */
				if(iterationLimit < Vars.iterationLimitLB)
					iterationLimit = Vars.iterationLimitLB;

				/* Set the iteration limit for threshold change (K) */
				iterationLimitForThresholdChange = adaptationLimitMultiplier*iterationLimit;
				 
				if(Print.acceptance){
					log.info("IterLimit updated = "+iterationLimit+
							 " :: iterLimitThresholdChange updated = "+iterationLimitForThresholdChange);
				}
				
				/* Set the current iteration number as the most recent iteration number when a new best solution was found */
				lastBestFoundIter = numberOfIterations;

				/* Reset the consecutive worsening moves counter */
				consecWorseMoveAcceptanceCounter = 0;
			}
    	}else if(newFitness == currentFitness){ //If the qualities of the new solution and current solution are the same
    		acp = true;
    	}else if(limitCounter >= iterationLimit){ //If the new solution is worse than the current solution

    		/* Accept a worsening solution for diversification */
    		if(newFitness != currentFitness && newFitness != bestFitness && newFitness <= threshold){
				acp = true;
				/* Reset the iteration limit counter */
				limitCounter = 0;

				if(Print.acceptance){
					log.info("A Worsening Move Accepted: "+newFitness+
					         " :: THRESHOLD = ("+threshold+") - iterLimit="+iterationLimit+
					         " (threshInx="+thresholdIndex+" : ListLength="+listLength+" )");
				}
				
				/* Increment the consecutive worsening move counter */ 
				consecWorseMoveAcceptanceCounter++;
    		}
    		
    		/* Increment the counter for increasing the threshold level */
    		limitCounterForThresholdChange++;
		}else{
			/* Increment the counter to check the iteration limit (k) */
			limitCounter++;
			/* Increment the counter to check the iteration limit (K) */
			limitCounterForThresholdChange++;
		}
    	
    	
    	if((numberOfIterations%Print.iterationNum) == 0 && Print.acceptance){
    		log.info("THRESHOLD = ("+threshold+") - iterationLimit="+iterationLimit);
    	}
    	
    	/* Increment the number of iterations */
		numberOfIterations++;

		/* Increase the threshold level if the iteration counter reaches to its limit */
		if(limitCounterForThresholdChange >= iterationLimitForThresholdChange){
			
			/* Incremenent threshold level index */
			thresholdIndex = thresholdIndex+1;
			
			if(thresholdIndex <= bestFitnessList.size()-1){
				if(Print.acceptance){
					log.info("Threshold value is CHANGED FROM "+threshold+"  (thresholdIndex="+thresholdIndex+")");
				}
				
				/* Set the threshold level */
				threshold = bestFitnessList.get(thresholdIndex);
				
				if(Print.acceptance){
					log.info(" TO "+threshold);
				}
			}else{ /* If the threshold level is reached to its maximum level */
				/* Consider the search is at stuck */
				Vars.isAtStuck = true;

				if(Print.acceptance){
					log.info("Search is at stuck (explore better level of change values for the heuristics' parameters) !");
				}
				
				/* Decide about whether to restart or not */
				if(!Vars.useOverallBestSln && !Vars.noMoreRestart){ 
					/* If there is enough time to re-initialise */
					if(2*(System.currentTimeMillis()-startTime) < totalExecTime){
						/* Active re-initialisation */
						Vars.restartSearch = true;
						/*  */
						updateAcceptanceListForNoMoreRestartCase(bestFitnessList);
					}else if(!Vars.noMoreRestart){ //RESTART is impossible during further iterations
						Vars.noMoreRestart = true;
						Vars.useOverallBestSln = true;
					}
				}else{ //If restart is deactivated at somewhere else
					Vars.noMoreRestart = true;
				}
			}
			
			/* Reset the iteration limit counter for threshold level change */
			limitCounterForThresholdChange = 0;
		}
		
		return acp;
	}
	

	/**
	 * Reset the threshold list in the case of re-initialisation
	 * 
	 * @param newFitness	the quality of the new solution
	 */
	public void resetAcceptanceList(double newFitness){
		/* Reset the threshold list using the given fitness value */
		bestFitnessList.removeAllElements();
		for(int i = 0; i < listLength; i++){
    		bestFitnessList.add(newFitness);
    	}
		
		/* Set the threshold index to the lowest threshold value's index (0: is the index of the new best solution) */
		thresholdIndex = 1;
		/* Set the threshold level */
		threshold = newFitness;

		/* Reset the iteration limit counter */
		limitCounter = 0;
		/* Reset the iteration limit counter for threshold level change */
		limitCounterForThresholdChange = 0;
	}
	
 
	/**
	 * Update the best threshold list if a new best solution is found during a current re-initialisation 
	 * 
	 * @param bestFitnessList	the list of recently found best solutions (threshold list)
	 */
	private void updateAcceptanceListForNoMoreRestartCase(Vector<Double> bestFitnessList){
		if(bestFitnessListForNoMoreRestartCase.size() == 0){
			for(double fitness : bestFitnessList){
				bestFitnessListForNoMoreRestartCase.add(fitness);
			}
		}else if(bestFitnessListForNoMoreRestartCase.get(0) > bestFitnessList.get(0)){ 
			//If the new found best solution is better than the one came from the previous restart
			
			bestFitnessListForNoMoreRestartCase.removeAllElements();
			for(double fitness : bestFitnessList){
				bestFitnessListForNoMoreRestartCase.add(fitness);
			}
		}
	}
	
	
	/**
	 * Set the threshold list with the values from the case where the best solution found among all restarts
	 */
	public void useNoMoreRestartCaseList(){
		bestFitnessList.removeAllElements();
		for(double fitness : bestFitnessListForNoMoreRestartCase){
			bestFitnessList.add(fitness);
		}
		
		thresholdIndex = 1;
		threshold = bestFitnessList.get(thresholdIndex);
		
		/** change for testing @27042011 **/
		limitCounter = 0;
		limitCounterForThresholdChange = 0;
	}
	
	
	/**
	 * Update the list length of the threshold list  
	 */
	private void updateListLength(){
		double timeFactor = (double)(Vars.totalExecutionTime-(System.currentTimeMillis()-startTime))/Vars.totalExecutionTime;
		
		listLength = (int)(Vars.aillaListSize/2)+(int)(((int)(Vars.aillaListSize/2)+1)*(timeFactor*timeFactor*timeFactor));
		
		if (listLength < 1) return;

		int tempBestListSize = bestFitnessList.size();
		if(listLength < tempBestListSize){
			for(int i = tempBestListSize; i > listLength; i--){
				bestFitnessList.remove(bestFitnessList.size()-1);
			}
		}
		
		if(thresholdIndex > listLength-1){
			thresholdIndex = listLength-1;
			threshold = bestFitnessList.get(thresholdIndex); 
		}
	}

	
	/**
	 * Get the current iteration limit together with threshold list values 
	 * 
	 * @return str	the mentioned information in a string form
	 */
	public String getIterationLimitWithThresholdValuesDetailsAsStr(){
		String str = ""+iterationLimit+";";
		
		for(int i = 0; i < bestFitnessList.size(); i++){
			str += (""+bestFitnessList.get(i)).replace(".", ",");
			
			if(i < bestFitnessList.size()-1){
				str += ";";
			}
		}
		
		return str;
	}
	
	/**
	 * Get the current threshold value for the adaptive iteration limited list-based threshold accepting criterion
	 */
	public double getCurrentThreshold(){
		return threshold;
	}
	
	private void printListValues(){
		System.out.println("--------------------------------------------");
		for(int i = 0; i < bestFitnessList.size(); i++){
			System.out.print(bestFitnessList.get(i)+" : ");
		}
		
		System.out.println("\n--------------------------------------------");
	}
	
}
