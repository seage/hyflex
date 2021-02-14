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

package be.kuleuven.kahosl.hyperheuristic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.kahosl.acceptance.AcceptanceCriterion;
import be.kuleuven.kahosl.acceptance.AcceptanceCriterionType;
import be.kuleuven.kahosl.acceptance.AdaptiveIterationLimitedListBasedTA;
import be.kuleuven.kahosl.analysis.PerformanceElements;
import be.kuleuven.kahosl.analysis.StatFiles;
import be.kuleuven.kahosl.analysis.StatFiles.WriteFileType;
import be.kuleuven.kahosl.selection.AdaptiveLimitedLAassistedDHSMentorSTD;
import be.kuleuven.kahosl.selection.SelectionMethod;
import be.kuleuven.kahosl.selection.SelectionMethodType;
import be.kuleuven.kahosl.util.Print;
import be.kuleuven.kahosl.util.Vars;
import be.kuleuven.kahosl.util.WriteInfo;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import AbstractClasses.ProblemDomain.HeuristicType;


public class GIHH extends HyperHeuristic implements Serializable {
	
	/** A logger to write various running information/comments/errors **/
	private static final Logger log = LoggerFactory.getLogger(GIHH.class);

	/** The common part of the files' name including the hyper-heuristic information **/
	private String resultFileName = "";	

	/** The number of low-level heuristics **/
	private int numberOfHeuristics;		
	
	/** The index of the most recently called heuristic **/
	private int lastCalledHeuristic;	
	
	/** The number of iterations spent **/
	private long numberOfIterations;	
	
	/** The total execution time allowed (stopping condition) **/
	private long totalExecTime;			
	
	/** The quality of the current solution **/
	private double currentFitness;		
	
	/** The quality of the new solution **/
	private double newFitness;			
	
	/** The quality of the current best solution **/
	private double bestFitness;			
	
	/** The heuristic selection mechanism **/
	private SelectionMethod selection;	
	
	/** The move acceptance mechanism **/
	private AcceptanceCriterion acceptance;	
	
	/** The heuristic selection mechanism type **/
	private SelectionMethodType selectionType;
	
	/** The move acceptance mechanism type **/
	private AcceptanceCriterionType acceptanceType;	
	
	/** The object to manage statistic files **/
	private StatFiles statFiles;		
	
	/** The number of iterations spent during the current phase **/
	private int phaseIterCounter = 1;	

	/** The performance element of the current phase **/
	private PerformanceElements currPerformance;	
	
	/** The performance element of the preceding phase **/
	private PerformanceElements prevPerformance;	
	
	/** The execution starting time **/
	private long startTime;				
	
	/** The number of new best solutions by the relay hybridisation **/
	private int numberOfFCNewBest = 0;	
	
	/** The number of new best solutions by the relay hybridisation 
	 * (including the solutions found/visited where the first heuristic does not make no change)  
	 **/
	private int numberOfFCNewBestNotActual = 0;	
	
	/** A vector of second heuristic lists for the relay hybridisation **/
	private Vector<List<Integer>> applyAfterHeurList;
	
	/* 
	 * Parameters for the relay hybridisation 
	 */
	/** Whether the relay hybridisation is enabled or disabled **/
	private boolean fcMode;				
	/** The number of phases left to enable the relay hybridisation **/
	private int fcModeTabuDuration;		
	/** The number of phase for disable relay hybridisation **/
	private int fcModeTabuDurationSize;	
	/** Whether the relay hybridisation is disabled during the preceeding phase **/
	private boolean fcModeIsPrevTabu; 	
	/***********************************************************************************************************/
	
	/** Heuristic types in HyFlex **********/
	/** An array of integers denoting the local search heuristics' index values **/
	private int[] local_search_heuristics;
	/** An array of integers denoting the mutation operators' index values **/
	private int[] mutation_heuristics;
	/** An array of integers denoting the crossover operators' index values **/
	private int[] crossover_heuristics;
	/** An array of integers denoting the ruin-and-recreate heuristics' index values **/
	private int[] ruin_recreate_heuristics;
	/***************************************/

    /** Index of the fastest non-tabu heuristic */
    private int baseHeuristicIndex = -1;
    /** The number of moves performed by the fastest non-tabu heuristic over time **/
    private double baseRatioForLRate = -1;
    
    /** Whether the current phase check to measure the heuristics' performance is the first check **/
	private boolean firstPhaseCheck = true;
	
	/* 
	 * Re-initialisation parameters 
	 */
	/** The number of re-initialisations performed **/
	private int numberOfRestarts;				
	/** The number of re-initialisations performed without new best solutions **/
	private int numberOfRestartsWithoutNewBest;	
	/** The shortest time spent between re-initialisations **/
	private long shortestRestartTime;			
	/** The most recent time when the solution was re-initialised **/
	private long lastRestartTimePoint;			
	
	/** The heuristic type list based on HyFlex **/
	private HeuristicType[] heuristicTypeList;	
	
	/** The heuristic type list with generic heuristic types **/
	private HeuristicClassType[] heuristicClassTypeList;	
	
	/** An array of units showing the speed of the heuristics with respect to the fastest non-tabu heuristic **/
	public double[] learningRateMultiplierList;				
	
	/** The number of iterations spent by the relay hybridisation **/
	private long spentIterForRelay;			
	/** The number of iterations spent by the relay hybridisation (both a first heuristic and a second heuristic are used) **/
	private long spentIterForValidRelay;	
	/** The spent execution time by the relay hybridisation **/
	private long spentTimeForRelay;			
	/** The number of new best solutions found by the relay hybridisation **/
	private int numberOfNewBestForAllFC;	

	/** The number of phases spent **/
	private int numberOfPhasesPassed;	    
	
	/** Is the new best solution found by the relay hybridisation **/
	private boolean newBestByRelay;			
	
	/** The total number of new best solutions found **/
	private int totalNumOfNewBestFound;		
	
	/** A ratio used to determine to choose single heuristics or relay hybridisation (heuristic pairs) **/
	private double fcSelectPow;				
	
	
	/**
	 * GIHH constructor
	 * 
	 * @param seed					seed for the random number generator
	 * @param numberOfHeuristics	the number of heuristics
	 * @param totalExecTime			the total execution time given (stopping criterion)
	 * @param resultFileName		the common part of the files' name including the hyper-heuristic's details
	 * @param selectionType			the heuristic selection mechanism type
	 * @param acceptanceType		the move acceptance mechanism type
	 */
	public GIHH(long seed, int numberOfHeuristics, long totalExecTime, String resultFileName,
			          SelectionMethodType selectionType, AcceptanceCriterionType acceptanceType) {
		super(seed);

		this.numberOfHeuristics = numberOfHeuristics;
		
		this.totalExecTime = totalExecTime;
		Vars.totalExecutionTime = totalExecTime;
		
		this.resultFileName = resultFileName;
		
		this.selectionType = selectionType;
		this.acceptanceType = acceptanceType;
		
		this.fcMode = true; //the relay hybridisation is enabled
		
		
		currPerformance = new PerformanceElements(numberOfHeuristics);
		prevPerformance = new PerformanceElements(numberOfHeuristics);
		
        numberOfIterations = 1;
        
        fcModeTabuDuration = 0;
        fcModeTabuDurationSize = Vars.tabuDurationLBForFC;
        fcModeIsPrevTabu = false;
        
        applyAfterHeurList = new Vector<List<Integer>>();
        for(int i = 0; i < numberOfHeuristics; i++){
        	applyAfterHeurList.add(new ArrayList<Integer>());
        }
        
        numberOfRestarts = 0;
        shortestRestartTime = 0;
        
        heuristicTypeList = new HeuristicType[numberOfHeuristics];
        
        heuristicClassTypeList = new HeuristicClassType[numberOfHeuristics];
        
        learningRateMultiplierList = new double[numberOfHeuristics];
        for(int i = 0; i < numberOfHeuristics; i++){
        	learningRateMultiplierList[i] = 1.0;
        }
        
        spentIterForRelay = 0;
        spentIterForValidRelay = 0;
        spentTimeForRelay = 0;
        
        numberOfNewBestForAllFC = 0;
        
    	numberOfPhasesPassed = 0;
    	
    	numberOfRestartsWithoutNewBest = 0;
        
    	numberOfFCNewBestNotActual = 0;
    	
    	newBestByRelay = false;
    	
    	totalNumOfNewBestFound = 0;
    	
    	fcSelectPow = 0;
    	
        statFiles = new StatFiles(numberOfHeuristics, selectionType, acceptanceType);
        
        initializeHH();
	}
	
	
	/** 
	 * Create stat files. Initialise the hyper-heuristic's sub-mechanisms
	 */
	private void initializeHH(){
		statFiles.statFileInitialisation(resultFileName); //also create new files for the next run
		
		initializeHeuristicSelection();
		initializeMoveAcceptance();
	}
	
	
	/** 
	 * Initialise the heuristic selection mechanism
	 */
 	private void initializeHeuristicSelection() {
		
		if (selectionType == SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD){
			Vars.calculateDHSParams(numberOfHeuristics, Vars.PLFactor);
			selection = new AdaptiveLimitedLAassistedDHSMentorSTD(numberOfHeuristics, Vars.tabuDuration, rng);	
		}else{
			System.out.println(" >>> Unrecognized heuristic selection: "+selectionType.toString()+" !");
			System.exit(1);
		}
	}
	
	/**
	 * Initialise the move acceptance mechanism
	 */
	private void initializeMoveAcceptance() {
			
		if (acceptanceType == AcceptanceCriterionType.AdaptiveIterationLimitedListBasedTA){
			acceptance = new AdaptiveIterationLimitedListBasedTA(Vars.aillaListSize, Double.MAX_VALUE, 
					                                             Vars.aillaAdaptationLimitMultiplier, totalExecTime, rng);
		}else{
			log.error(" >>> Unrecognized move acceptance: "+acceptanceType.toString()+" !");
			System.exit(1);
		}
	}
	
	
	
	/**
	 * Set the heuristic types. These type information is used only while applying heuristics.
	 * For GIHH, each heuristic is initially classified as ImprovingMoreOrEqual. This feature
	 * makes GIHH generic for different heuristic sets
	 * 
	 * @param problem	a target problem
	 */
	private void setHeuristicTypes(ProblemDomain problem){
        local_search_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
    	mutation_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
    	ruin_recreate_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
        crossover_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
        
        for(int i = 0; i < local_search_heuristics.length; i++){
        	heuristicTypeList[local_search_heuristics[i]] = HeuristicType.LOCAL_SEARCH;
        	heuristicClassTypeList[local_search_heuristics[i]] = HeuristicClassType.ImprovingMoreOrEqual;
        }
        for(int i = 0; i < mutation_heuristics.length; i++){
        	heuristicTypeList[mutation_heuristics[i]] = HeuristicType.MUTATION;
        	heuristicClassTypeList[mutation_heuristics[i]] = HeuristicClassType.ImprovingMoreOrEqual;
        }
        for(int i = 0; i < ruin_recreate_heuristics.length; i++){
        	heuristicTypeList[ruin_recreate_heuristics[i]] = HeuristicType.RUIN_RECREATE;
        	heuristicClassTypeList[ruin_recreate_heuristics[i]] = HeuristicClassType.ImprovingMoreOrEqual;
        }
        for(int i = 0; i < crossover_heuristics.length; i++){
        	heuristicTypeList[crossover_heuristics[i]] = HeuristicType.CROSSOVER;
        	heuristicClassTypeList[crossover_heuristics[i]] = HeuristicClassType.ImprovingMoreOrEqual;
        }
	}
	
	/** 
	 * Is the given heuristic crossover
	 * 
	 * @param heurIndex	the heuristic index
	 * @return			whether the heuristic for the given heurIndex is crossover
	 */
	private boolean isCrossover(int heurIndex){
		boolean isCrossover = false;
		for(int cr = 0; cr < crossover_heuristics.length; cr++){
			if(crossover_heuristics[cr] == heurIndex){
				isCrossover = true;
				break;
			}
		}
		
		return isCrossover;
	}
	
	/**
	 * Main hyper-heuristic solver function
	 */
	@Override
	protected void solve(ProblemDomain problem) {
		
		setHeuristicTypes(problem);	/* Set the heuristic types */
		problem.setMemorySize(12); 	/* Create a memory to save solutions */
		

		
		String bestSlnFoundAsStr = ""; /* The best solution found in a string */

		Vars.restartSearch = false; //Reset value for the next run (the variable is static)
		Vars.useOverallBestSln = false; //Reset 
		Vars.noMoreRestart = false;
	
		long startHeur, endHeur;
		
		/* Create an inital solution */
		problem.initialiseSolution(0);
		/* Copy the initial solution to the best solution location in the solution memory */
		problem.copySolution(0, 10); 
		/* Set initial fitness parameters */
		bestFitness = currentFitness = newFitness = problem.getFunctionValue(0);
		/* Set the initial solution as the best solution in a string */
		bestSlnFoundAsStr = problem.solutionToString(0); 
	
		/* Reset the threshold list values of the acceptance mechanism */
		acceptance.resetAcceptanceList(bestFitness); 

		/* 
		 * Take a number of copies of the initial solution for using 
		 * the crossover operators (or any operator requiring two solutions) 
		 * */
		if(crossover_heuristics != null){
			for(int mInx = 5; mInx < 10; mInx++){
				problem.copySolution(0, mInx); //for crossovers
			}
		}
	
		if(Print.hyperheuristic){
			log.info("----------------------------------------------------------------\n" +
			         " >> HH Started ! (#LLH="+numberOfHeuristics+")"+
			         "\n----------------------------------------------------------------");
		}
	
		
		startTime = System.currentTimeMillis(); /* Set the starting execution time in ms */
		
		lastRestartTimePoint = startTime;		/* Set the start time as the most recent time point where the solution re-initialised */
			
		/* Write some initial information to the corresponding stat files */
		statFiles.writeIntoFile(WriteFileType.BFFile, getCurrentStateForBestAsStr(-1));
		statFiles.writeIntoFile(WriteFileType.HCntFile, getHeuristicCounterDetailsAsStr());
		statFiles.writeIntoFile(WriteFileType.ITFile, getIterationLimitDetailsAsStr());
			
    	if(Print.hyperheuristic){
    		printCurrentState();
    	}
    	
	    /* Repeat until the given execution time limit is reached */
		while (!hasTimeExpired()) {
		//while ((System.currentTimeMillis()-startTime) <= totalExecTime) {
			
			/* Decide whether to disable re-initialisation */
			if(!Vars.useOverallBestSln && numberOfRestarts > 0 && (System.currentTimeMillis()-startTime) > (1.1*(totalExecTime/2.0)) && 
			   bestFitness > problem.getFunctionValue(11)){
				
				Vars.restartSearch = false;
				Vars.useOverallBestSln = true;
			}
			/**********************************************************************************/
				
			/* Additional check to disable re-initialisation */
			if(Vars.restartSearch){
				
				if(numberOfRestarts > 0){
					if((System.currentTimeMillis()-lastRestartTimePoint)+shortestRestartTime > (1.1*(totalExecTime/2.0))){
						Vars.restartSearch = false;
						Vars.useOverallBestSln = true;
						
						if(Print.hyperheuristic){
							System.out.println("\n\n >> RE-INITIALIZATION CANCELLED !! \n");
						}
						
						Vars.noMoreRestart = true;
					}else if(numberOfRestartsWithoutNewBest >= 5 && (double)(System.currentTimeMillis()-startTime) > ((double)totalExecTime/5)){ //@13062011
						
						Vars.restartSearch = false;
						Vars.useOverallBestSln = true;

						if(Print.hyperheuristic){
							log.info("RE-INITIALIZATION CANCELLED (numberOfRestartsWithoutNewBest limit reached to "+
									  numberOfRestartsWithoutNewBest+")");
						}
						
						Vars.noMoreRestart = true;
					} 
				}else{
					if((System.currentTimeMillis()-startTime) > (totalExecTime/5.0)){
						Vars.restartSearch = false;
						Vars.useOverallBestSln = true;
						
						if(Print.hyperheuristic){
							log.info("FIRST RE-INITIALIZATION CANCELLED (INACTIVE) !!");
						}
						
						Vars.noMoreRestart = true;
					}
				}
			}

				
			/* if the move acceptance mechanism requests re-initialisation */
			if(Vars.restartSearch){
				
				Vars.isAtStuck = false; /* Search is not at stuck */ 
				Vars.restartSearch = false;	/* Do not re-initialise the solution */
				
				/* Calculate the elapsed time after the earlier restart */ 
				long tempElapsedTimeForRestart = System.currentTimeMillis()-lastRestartTimePoint;
				/* Update the restart time */
				lastRestartTimePoint = System.currentTimeMillis();
				

				/* Set the current best solution (index=10 in the solution memory) as the overall best solution (Index=11) */
				if(numberOfRestarts > 0){
					if(bestFitness < problem.getFunctionValue(11)){
						problem.copySolution(10, 11); 
						
						numberOfRestartsWithoutNewBest = 0;
					}else{
						numberOfRestartsWithoutNewBest++;
					}
					
					/* Keep the shortest execution time spent after re-initialisations */
					if(tempElapsedTimeForRestart < shortestRestartTime){
						shortestRestartTime = tempElapsedTimeForRestart;
					}
				}else{
					problem.copySolution(10, 11); 
					
					/* Keep the shortest execution time spent after re-initialisations */
					shortestRestartTime = tempElapsedTimeForRestart;
				}
				
				/* Re-initialise the solution */
				problem.initialiseSolution(0);
				/* Update fitness values */
				bestFitness = currentFitness = newFitness = problem.getFunctionValue(0);
				
				/* Take copies of the new initial solution to use for crossover (or any heuristic requiring two solutions) */
				if(crossover_heuristics != null){
					for(int mInx = 5; mInx < 10; mInx++){
						problem.copySolution(0, mInx); //for crossovers
					}
				}
				
				/* Update the threshold list of the acceptance mechanism */
				acceptance.resetAcceptanceList(currentFitness);
				/* Increment the number of restarts counter */
				numberOfRestarts++;
				
				if(Print.hyperheuristic){
					log.info("RE-INITIALIZATION !!");
				}
			}else if(Vars.useOverallBestSln && numberOfRestarts > 0){ //If the search was restarted before and re-initialisation is disabled
				/*
				 * Start using the overall best solution found after all re-initialisations.
				 * Also, use the threshold list when the overall best solution found, in the acceptance mechanism
				 */
				
				Vars.useOverallBestSln = false;
				/* Disable re-initialisation */
				Vars.noMoreRestart = true;

				/* Update the overall best solution if the most recent solution is better than the current overall best solution */
				if(bestFitness > problem.getFunctionValue(11)){
					if(Print.hyperheuristic){
						log.info("USE OVERALL BEST SOLUTION !!");
					}
					
					problem.copySolution(11, 0);
				
					/* Set fitness values to the fitness of the overall best solution */ 
					bestFitness = currentFitness = newFitness = problem.getFunctionValue(0); 
					
					/* Change the threshold list of the acceptance mechanism with the threshold list when the overall best solution found */
					acceptance.useNoMoreRestartCaseList();
					
					/* Write overall best solution information after re-initalisation to the BF file */
					statFiles.writeIntoFile(WriteFileType.BFFile, getCurrentStateForBestAsStr(-1));
				}
			}
			
			/* Select a heuristic from the heuristic set */
			lastCalledHeuristic = selection.selectHeuristic();
			
			double tempProb = rng.nextDouble();
			
			boolean isRelay = false;
			
			/* Reset the parameters related to the relay hybridisation */
			double relayFirstFitness = -1.0;
			double relaySecondFitness = -1.0;
			int relayFirstHeurIndex = -1;
			int relaySecondHeurIndex = -1;
			long relayStartFirstHeur = -1, relayEndFirstHeur = -1;
			long relayStartSecondHeur = -1, relayEndSecondHeur = -1;
			long relayPhaseStartTime = -1, relayPhaseEndTime = -1;

			newBestByRelay = false; 

			/* Calculate the selection ratio between single heuristics and the relay hybridisation*/
			fcSelectPow = (double)(totalNumOfNewBestFound-numberOfFCNewBest+1.0)/(numberOfFCNewBest+1.0);
			/* Keep the selection ratio between single heuristics and the relay hybridisation within its bounds (0.02, 50) */
			if(fcSelectPow > 50){ 
				fcSelectPow = 50; 
			}else if(fcSelectPow < 0.02){ 
				fcSelectPow = 0.02;
			}

			/* Randomly decide whether to use the relay hybridisation */
			double fcSelectProb = Math.pow((double)(phaseIterCounter%Vars.phaseLength)/Vars.phaseLength,fcSelectPow);
			if(fcMode && tempProb <= fcSelectProb){
				
				/* Keep the starting time of the relay hybridisation */
				relayPhaseStartTime = System.nanoTime();
				
				/* Select the first heuristic for the relay hybridisation */
				int wrsHeur = selection.selectTheFirstHeuristicForRelay();
				relayFirstHeurIndex = wrsHeur;
				
				
				/* Set the heuristics' parameter values */
				if(selectionType == SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD){
					problem.setIntensityOfMutation(selection.getLevelOfChangeList()[wrsHeur]);
					problem.setDepthOfSearch(selection.getLevelOfChangeList()[wrsHeur]);
				}
				
				
				/* 
				 * Apply the first heuristic for the relay hybridisation 
				 */
				/* Keep the starting time information for the execution of the first heuristic */
				relayStartFirstHeur = System.nanoTime();
				if(isCrossover(wrsHeur)){ /* If the selected heuristic is crossover */
					int slnInxForCrossover = rng.nextInt(5)+5;
					problem.applyHeuristic(wrsHeur, 0, slnInxForCrossover, 1);
				}else{
					problem.applyHeuristic(wrsHeur, 0, 1);
				}
				/* Keep the ending time information for the execution of the first heuristic */
				relayEndFirstHeur = System.nanoTime();

				/* Get the fitness value (quality) of the solution after the first heuristic applied by the relay hybridisation */
				relayFirstFitness = problem.getFunctionValue(1);
				
				/* 
				 * If the solution generated/visited after the first heuristic applied is the same solution before it was applied,
				 * decrease its probability to be selected
				 */
				boolean punisProbListForRelay = false;
				if(problem.compareSolutions(0, 1)){
					punisProbListForRelay = true; 
				}
				
				
				/*
				 * If the first heuristic applied by the relay hybridisation finds a new best solution,
				 * do not apply the second heuristic (cancel the relay hybridisation). 
				 * Otherwise, if the solution is a worsening solution, then apply a second heuristic
				 */
				if(relayFirstFitness >= bestFitness){ 
					
					isRelay = true;
				
					/* Choose a second heuristic for the relay hybridisation */
					int tempSize = applyAfterHeurList.get(wrsHeur).size();
					if(tempSize > 0 && rng.nextDouble() <= 0.25){ 
						lastCalledHeuristic = (Integer)applyAfterHeurList.get(wrsHeur).get(rng.nextInt(tempSize)); 
					}else{ 
						lastCalledHeuristic = selection.selectAnImprovementHeuristic(heuristicClassTypeList);
					}
					
					/* Set the heuristics' parameter values */
					if(selectionType == SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD){
						problem.setIntensityOfMutation(selection.getLevelOfChangeList()[lastCalledHeuristic]);
						problem.setDepthOfSearch(selection.getLevelOfChangeList()[lastCalledHeuristic]);
					}
					
					/* Set the second heuristic's index */
					relaySecondHeurIndex = lastCalledHeuristic;
					
					/* 
					 * Apply the first heuristic for the relay hybridisation 
					 */
					/* Keep the starting time information for the execution of the first heuristic */
					relayStartSecondHeur = startHeur = System.nanoTime();
					if(isCrossover(lastCalledHeuristic)){
						int slnInxForCrossover = rng.nextInt(5)+5;
						newFitness = problem.applyHeuristic(lastCalledHeuristic, 1, slnInxForCrossover, 1);
					}else{
						newFitness = problem.applyHeuristic(lastCalledHeuristic, 1, 1);
					}
					/* Keep the ending time information for the execution of the first heuristic */
					relayEndSecondHeur = endHeur = System.nanoTime();
					
					/* Get the fitness value (quality) of the solution after the second heuristic applied by the relay hybridisation */
					relaySecondFitness = problem.getFunctionValue(1);
					
					/* If a new best solution is found */
					if(newFitness < bestFitness){
						/*
						 * Update the selection probabilities of the first heuristics in the relay hybridisation 
						 */
						if(!punisProbListForRelay){ /* if the generated solution by the first heuristic is not the same as the solution before the relay hybridisation */
							newBestByRelay = true; 
							selection.updateProbListForRelay(wrsHeur, relaySecondHeurIndex, 1, learningRateMultiplierList); //@12052011
						}else{ /* Punish the first heuristic since it generated the exact same solution */
							selection.updateProbListForRelay(wrsHeur, relaySecondHeurIndex, 0, learningRateMultiplierList); //@12052011
						}
												
						/* The number of new best solutions by the relay hybridisation */
						numberOfFCNewBestNotActual++; 
						
						/* 
						 * Add the second heuristic to the first heuristic's second heuristic list for the relay hybridisation,
						 * since a new best solution is found 
						 */
						applyAfterHeurList.get(relayFirstHeurIndex).add(relaySecondHeurIndex);
						if(applyAfterHeurList.get(relayFirstHeurIndex).size() > 10){
							applyAfterHeurList.get(relayFirstHeurIndex).remove(0);
						}
						
						
						statFiles.writeIntoFile(WriteFileType.RHFile, getRelayDetailsAsStr(relayFirstHeurIndex, relaySecondHeurIndex,
								relayFirstFitness, relaySecondFitness));
						
						
						
						/* If the solution generated/visited after the first heuristic applied is the same solution before it was applied */
						if(!punisProbListForRelay){ 
							numberOfFCNewBest++;
							currPerformance.setFCNumberOfImprovingBestMoves(currPerformance.getFCNumberOfImprovingBestMoves()+1); 
						}
					
					}else if(newFitness < currentFitness){ /* If the solution is an improving solution */
						if(!punisProbListForRelay){
							selection.updateProbListForRelay(wrsHeur, relaySecondHeurIndex, 2, learningRateMultiplierList);  
						}else{ /* Punish the first heuristic since it generated/visited the same solution before it applied */
							selection.updateProbListForRelay(wrsHeur, relaySecondHeurIndex, 0, learningRateMultiplierList); 
						}
					}else{ /* If the solution found by the relay hybridisation is a worsening solution */
						selection.updateProbListForRelay(wrsHeur, relaySecondHeurIndex, 0, learningRateMultiplierList); 
					}
				}else{ /* The second heuristic is not applied. Consider only the first applied heuristic */
					lastCalledHeuristic = wrsHeur;
					newFitness = relayFirstFitness;

					startHeur = relayStartFirstHeur;
					endHeur = relayEndFirstHeur;
				}
				
				/* additional info for RHFile */
				relayPhaseEndTime = System.nanoTime();
				spentTimeForRelay += (relayPhaseEndTime-relayPhaseStartTime);
				spentIterForRelay++;
				if(punisProbListForRelay){
					spentIterForValidRelay++;
				}
				
				if(newFitness < bestFitness){
					numberOfNewBestForAllFC++;
				}
			}else{ /* The regular single heuristic application */
				/* Update/Set heuristics' parameters */
				if(selectionType == SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD){
					problem.setIntensityOfMutation(selection.getLevelOfChangeList()[lastCalledHeuristic]);
					problem.setDepthOfSearch(selection.getLevelOfChangeList()[lastCalledHeuristic]);
				}
				
				/* 
				 * Apply the selected heuristic 
				 */
				/* Keep the starting time information for the execution of the selected heuristic */
				startHeur = System.nanoTime();
				if(isCrossover(lastCalledHeuristic)){
					int slnInxForCrossover = rng.nextInt(5)+5;
					newFitness = problem.applyHeuristic(lastCalledHeuristic, 0, slnInxForCrossover, 1);
				}else{
					newFitness = problem.applyHeuristic(lastCalledHeuristic, 0, 1);
				}
				/* Keep the ending time information for the execution of the selected heuristic */
				endHeur = System.nanoTime();
			}
			
			/*
			 * Perform the required updates for the heuristic selection process
			 */
			if(!isRelay){
				currPerformance.updatePerformanceElements(lastCalledHeuristic, currentFitness, newFitness, bestFitness,
						                                  startHeur, endHeur);
				
				if(Vars.timeBasedLA){
					updateLearningRateMultiplier(lastCalledHeuristic);	
				}
				
				selection.updateSelectionElements(lastCalledHeuristic, heuristicClassTypeList[lastCalledHeuristic], 
												  currentFitness, newFitness, 
						                          bestFitness, startHeur, endHeur, 
						                          learningRateMultiplierList, currPerformance);
			}else{ /* Relay hybridisation */
				/** First heuristic **/
				currPerformance.updatePerformanceElements(relayFirstHeurIndex, currentFitness, relayFirstFitness, bestFitness,
                        								  relayStartFirstHeur, relayEndFirstHeur);

				if(Vars.timeBasedLA){
					updateLearningRateMultiplier(relayFirstHeurIndex);	
				}
				
				selection.updateSelectionElements(relayFirstHeurIndex, heuristicClassTypeList[relayFirstHeurIndex], 
												  currentFitness, relayFirstFitness, 
							                      bestFitness, relayStartFirstHeur, relayEndFirstHeur, 
							                      learningRateMultiplierList, currPerformance);
				/*********************************************************************************************************************/	
				
				
				/** Second heuristic **/
				currPerformance.updatePerformanceElements(relaySecondHeurIndex, relayFirstFitness, relaySecondFitness, bestFitness,
						  							      relayStartSecondHeur, relayEndSecondHeur);

				if(Vars.timeBasedLA){
					updateLearningRateMultiplier(relaySecondHeurIndex);	
				}
				
				selection.updateSelectionElements(relaySecondHeurIndex, heuristicClassTypeList[relaySecondHeurIndex], 
												  relayFirstFitness, relaySecondFitness, 
											      bestFitness, relayStartSecondHeur, relayEndSecondHeur, 
											      learningRateMultiplierList, currPerformance);
				/*********************************************************************************************************************/
			}
	      
			
			/* Ask the acceptance mechanism whether to accept or reject the new solution */
			if(acceptance.accept(newFitness, currentFitness, bestFitness)){
				/* Copy the accepted solution to the memory as the current solution */
				problem.copySolution(1, 0);
				
				/* If the solution is a new best solution */
				if(newFitness < bestFitness){
					
					/* Keep the current new best solution in a string */
					bestSlnFoundAsStr = problem.solutionToString(0); 

					/* Increment the total number of new best solutions found counter */
					totalNumOfNewBestFound++;
					
					/* Update the best fitness value */
					bestFitness = newFitness;
					
					/* Change randomly one of the solution used for crossovers (or any heuristic requiring two solutions) */
					int randMemIndex = rng.nextInt(5)+5;
					problem.copySolution(1, randMemIndex);
					
					/* Copy the new best solution to the solution memory */
					problem.copySolution(0, 10);

					/* Append the new search state information to the corresponding stat files */
					statFiles.writeIntoFile(WriteFileType.BFFile, getCurrentStateForBestAsStr(lastCalledHeuristic));
					statFiles.writeIntoFile(WriteFileType.HCntFile, getHeuristicCounterDetailsAsStr());
					statFiles.writeIntoFile(WriteFileType.ITFile, getIterationLimitDetailsAsStr());
					statFiles.writeIntoFile(WriteFileType.LOCFile, getCurrentStateForLOCAsStr());
					statFiles.writeIntoFile(WriteFileType.LRMFile, getCurrentStateForLRMAsStr());
					statFiles.writeIntoFile(WriteFileType.IMPRFile, getCurrentStateForIMPRAsStr());
					
					statFiles.writeIntoFile(WriteFileType.RHFile, getRelayDetailsAsStrJustInfo());


                	if(Print.hyperheuristic){
                		printCurrentState();
                	}
				}

				/* Append the new search state information to ACFile */
				statFiles.writeIntoFile(WriteFileType.ACFile, getAcceptanceDetailsAsStr());
				
				/* Update the current fitness */
				currentFitness = newFitness;
			}
			
			/* 
			 * End of the current phase for the selection mechanism. 
			 * Perform the required changes 
			 */
			if(selectionType == SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD && phaseIterCounter == Vars.phaseLength){
				/* Check the performance changes of the heuristics and update the related elements */
				performanceCheckForDHS();
				/* Reset the phase iteration counter */
				phaseIterCounter = 0;
				/* Increment the number phases passed counter */
				numberOfPhasesPassed++;
			}

			/* Print the current status of the search */
			if(Print.hyperheuristic && numberOfIterations%Print.iterationNum == 0){
				printCurrentState();
			}
			
			/* Write the current status of the search to the corresponding stat files */
			if(numberOfIterations%WriteInfo.periodicWriteIteration == 0){
				statFiles.writeIntoFile(WriteFileType.BFFile, getCurrentStateForBestAsStr(-1));
				statFiles.writeIntoFile(WriteFileType.HCntFile, getHeuristicCounterDetailsAsStr());
				statFiles.writeIntoFile(WriteFileType.ITFile, getIterationLimitDetailsAsStr());
				statFiles.writeIntoFile(WriteFileType.LOCFile, getCurrentStateForLOCAsStr());
				statFiles.writeIntoFile(WriteFileType.LRMFile, getCurrentStateForLRMAsStr());
				statFiles.writeIntoFile(WriteFileType.IMPRFile, getCurrentStateForIMPRAsStr());
			}
			
			/* Increment the number of iterations */
			numberOfIterations++;
			
			/* Increment the phase iteration counter */
			phaseIterCounter++;
		}
		
		
		/* The final writing to the stat files at the end of a run */
		statFiles.writeIntoFile(WriteFileType.BFFile, getCurrentStateForBestAsStr(-1));
		statFiles.writeIntoFile(WriteFileType.HCntFile, getHeuristicCounterDetailsAsStr());
		statFiles.writeIntoFile(WriteFileType.ITFile, getIterationLimitDetailsAsStr());
		statFiles.writeIntoFile(WriteFileType.LOCFile, getCurrentStateForLOCAsStr());
		statFiles.writeIntoFile(WriteFileType.LRMFile, getCurrentStateForLRMAsStr());
		statFiles.writeIntoFile(WriteFileType.IMPRFile, getCurrentStateForIMPRAsStr());
		
		/* Close BufferedWriters at the end of a run*/
		statFiles.closeBufferedWriters();
		
		/* Write the best solution found after a search to a file */
		writeTheBestSlnToAFile(problem, bestFitness, bestSlnFoundAsStr); 
		
	}
	
	
	/**
	 * Write the best solution found at the end of a run to a file
	 * 
	 * @param problem			target problem used to solve
	 * @param bestFitness		the best fitness value found
	 * @param bestSlnFoundAsStr	the best solution in a string
	 */
	public void writeTheBestSlnToAFile(ProblemDomain problem, double bestFitness, String bestSlnFoundAsStr){
		BufferedWriter outBestSln = null;
		try {
			outBestSln = new BufferedWriter(
				    		new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+resultFileName+"_BESTslnFOUND.txt", true));
			
			outBestSln.write("BestFitness: "+bestFitness+"\n");
			outBestSln.write(bestSlnFoundAsStr);
		} catch (IOException e) {
			log.error(""+e);
		} finally {
			try {
				outBestSln.close();
			} catch (IOException e) {
				log.error(""+e);
			}
		}
	}
	
	
	/**
	 *  Check the performance of the heuristics and make the required changes 
	 */
	private void performanceCheckForDHS(){
		
		if(Vars.timeBasedLA){
        	Vars.learningRateMultiplier = 1;
        	baseRatioForLRate = -1;
        	baseHeuristicIndex = -1;
    	}
    	
		/*  */
		updatePerformanceMetricForSelection();
		
		Vars.phaseLength = Vars.calculatePhaseLength(numberOfHeuristics-selection.getNumberOfTabuHeuristics());
		
    	if(Vars.timeBasedPhaseLength){
    		double avgIterTime = getAvgSubsetExecTime();
    		
    		if(avgIterTime == 0){
    			Vars.phaseLength = Vars.calculatePhaseLength(numberOfHeuristics-selection.getNumberOfTabuHeuristics());
    		}else{
    			int tempPhaseLength = (int)(Vars.phaseDuration/(avgIterTime*1000));
    			
    			if(tempPhaseLength < (numberOfHeuristics-selection.getNumberOfTabuHeuristics())*Vars.minPhaseFactor){
    				Vars.phaseLength = (numberOfHeuristics-selection.getNumberOfTabuHeuristics())*Vars.minPhaseFactor;
    			}else if(tempPhaseLength > Vars.phaseLength){
    				/* no change: keep the same phase length */
    			}else{
    				Vars.phaseLength = tempPhaseLength;
    			}
    		}
    	}
    	
    	/* Update the corresponding files at the end of a phase */
    	statFiles.writeIntoFile(WriteFileType.BFFile, getCurrentStateForBestAsStr(-1));
    	statFiles.writeIntoFile(WriteFileType.QIFile, selection.getRankListAsStr());
    	statFiles.writeIntoFile(WriteFileType.TDFile, selection.getTabuDurationListAsStr());
	}
	
	
	/**
	 * Calculate the average execution time spent by the non-tabu heuristics
	 * 
	 * @return average execution time 
	 */
	public double getAvgSubsetExecTime(){
		double total = 0.0;
		
		double tempExecTime;
    	double nano = 1000000000.0;
		double tempTime = nano*Vars.execTimeSensitivity;
		
		for(int i = 0; i < numberOfHeuristics; i++){
			if(selection.getTabuDurationList()[i] == 0){
				tempExecTime = currPerformance.getSpentExecutionTime()[i];
				
				if(tempExecTime <= tempTime){
					tempExecTime = Vars.execTimeSensitivity;
				}else{
					tempExecTime /= nano;
				}
				
				total += (tempExecTime/currPerformance.getNumberOfMoves()[i]);
			}
		}
		
		//return total/Vars.phaseLength;
		return total/(numberOfHeuristics-selection.getNumberOfTabuHeuristics());
	}

	
	/**
	 * Update the units showing the speed of the heuristics with respect to the fastest non-tabu heuristic
	 * 
	 * @param selectedHeuristicIndex
	 */
    private void updateLearningRateMultiplier(int selectedHeuristicIndex){
    	
    	double tempExecTime = currPerformance.getSpentExecutionTime()[selectedHeuristicIndex];
    	double nano = 1000000000.0;
		double tempTime = nano*Vars.execTimeSensitivity;
		
		if(tempExecTime <= tempTime){
			tempExecTime = Vars.execTimeSensitivity;
		}else{
			tempExecTime /= nano;
		}
		
		
    	currPerformance.getNumberOfMovesPerTime()[selectedHeuristicIndex] = currPerformance.getNumberOfMoves()[selectedHeuristicIndex]/tempExecTime;
    	if(baseRatioForLRate == -1.0 && tempExecTime != 0.0){
    		baseRatioForLRate = currPerformance.getNumberOfMovesPerTime()[selectedHeuristicIndex];
    		baseHeuristicIndex = selectedHeuristicIndex;
    	}else{
    		
    		if(selectedHeuristicIndex == baseHeuristicIndex){
    			baseRatioForLRate = currPerformance.getNumberOfMovesPerTime()[selectedHeuristicIndex];
    		}
    		
			for(int i = 0; i < numberOfHeuristics; i++){
    			if(currPerformance.getNumberOfMovesPerTime()[i] > baseRatioForLRate){
    				baseRatioForLRate = currPerformance.getNumberOfMovesPerTime()[i];
    				baseHeuristicIndex = i;
    			}
    		}
    	}

		if(currPerformance.getNumberOfMovesPerTime()[selectedHeuristicIndex] == 0.0){
			Vars.learningRateMultiplier = 1;
		}else{
			Vars.learningRateMultiplier = (int)(baseRatioForLRate/currPerformance.getNumberOfMovesPerTime()[selectedHeuristicIndex]);
		}
		
		
		learningRateMultiplierList[selectedHeuristicIndex] = Vars.learningRateMultiplier;
     }
   
	
    /**
     * Calculate the performance of the heuristics at the end of a phase based on a performance metric
     */
	private void updatePerformanceMetricForSelection(){
		
		double tempTotalExecTimeForFirstPhase = 0.0;
		
    	double[] performanceRate = new double[numberOfHeuristics];
    	double[] worseningRate = new double[numberOfHeuristics];
    	
    	double tempExecTime;
    	double tempSpentExecTime;
    	String execTimeStr = "";
    	
    	double[] timeExecList = new double[numberOfHeuristics];
    	
    	/** This is required to ignore the first part (about the new best solutions) of the performance metric **/
    	int tempNumOfNewBestSlnFoundInTheLastPhase = 0;
    	for(int i = 0; i < numberOfHeuristics; i++){
    		if(currPerformance.getNumberOfImprovingBestMoves()[i] != prevPerformance.getNumberOfImprovingBestMoves()[i]){
    			tempNumOfNewBestSlnFoundInTheLastPhase += (currPerformance.getNumberOfImprovingBestMoves()[i]-prevPerformance.getNumberOfImprovingBestMoves()[i]);
    		}
    	}
    	/******************************************************************************************************************/

    	
		double timeRemaining = (totalExecTime-(System.currentTimeMillis()-this.startTime))/1000F;
		if(timeRemaining < 0){
			timeRemaining = 0;
		}
		
    	for(int i = 0; i < numberOfHeuristics; i++){
    		tempExecTime = (currPerformance.getSpentExecutionTime()[i]-prevPerformance.getSpentExecutionTime()[i]);
    		tempSpentExecTime = currPerformance.getSpentExecutionTime()[i];
    		
    		if(firstPhaseCheck){
    			tempTotalExecTimeForFirstPhase += tempExecTime;
    		}
    		
    		if(tempExecTime != 0.0 && selection.getTabuDurationList()[i] == 0){
    			double nano = 1000000000.0;
    			double tempTime = nano*Vars.execTimeSensitivity;
    			/////////////////// For 1 Second - Sensitivity///////////////
        		if(tempExecTime <= tempTime){
        			tempExecTime = Vars.execTimeSensitivity;
        		}else{
        			tempExecTime /= nano;
        		}

        		if(tempSpentExecTime <= tempTime){
        			tempSpentExecTime = Vars.execTimeSensitivity;
        		}else{
        			tempSpentExecTime /= nano;
        		}
        		

        		performanceRate[i] = 0;
        		
        		/** This is required to ignore the first part (about the new best solutions) of the performance metric **/
        		if(tempNumOfNewBestSlnFoundInTheLastPhase > 0){
        			performanceRate[i] = (Math.pow(1.0+(currPerformance.getNumberOfImprovingBestMoves()[i]-prevPerformance.getNumberOfImprovingBestMoves()[i]),Vars.powerOfForNewBestSolutionsPerfM0)*(timeRemaining/tempExecTime)*100000000);
        		}
        		
        		performanceRate[i] += (((currPerformance.getTotalHeurImprovement()[i]-prevPerformance.getTotalHeurImprovement()[i])/tempExecTime)*100000)-
				                      (((currPerformance.getTotalHeurWorsening()[i]-prevPerformance.getTotalHeurWorsening()[i])/tempExecTime)*0.0001)+
				                      ((currPerformance.getTotalHeurImprovement()[i]/tempSpentExecTime)*0.000001)-
				                      ((currPerformance.getTotalHeurWorsening()[i]/tempSpentExecTime)*0.000000001);
	        		
        		
	    		worseningRate[i] = (((currPerformance.getTotalHeurWorsening()[i]-prevPerformance.getTotalHeurWorsening()[i])/tempExecTime));
    			
    			timeExecList[i] = tempExecTime/(currPerformance.getNumberOfMoves()[i]-prevPerformance.getNumberOfMoves()[i]);
    		}else{
    			if(tempSpentExecTime != 0){
    				worseningRate[i] += ((currPerformance.getTotalHeurWorsening()[i]/tempSpentExecTime)*0.000001);
    			}else{
    				worseningRate[i] = 0.0;
    			}
    			
    			performanceRate[i] = 0.0;
    			timeExecList[i] = 0.0;
    		}
    		
    		execTimeStr += "["+i+"]="+tempExecTime+", ";

    		setHeuristicClassType(currPerformance,i);
    	}
    	
    	
    	firstPhaseCheck = false;
    	/********************************************************************************/
    	
    	if(fcModeTabuDuration > 0){
    		--fcModeTabuDuration;
    		
    		if(fcModeTabuDuration == 0){
    			fcModeIsPrevTabu = true;
    			fcMode = true;
    		}
    	}else if((currPerformance.getFCNumberOfImprovingBestMoves()-prevPerformance.getFCNumberOfImprovingBestMoves()) == 0){
    		if(fcModeIsPrevTabu){
    			++fcModeTabuDurationSize;
    			
    			if(fcModeTabuDurationSize > Vars.tabuDurationUBForFC){
    				fcModeTabuDurationSize = Vars.tabuDurationUBForFC;
    			}
    		}else{
    			fcModeTabuDurationSize = Vars.tabuDurationLBForFC;
    		}
    		
    		fcMode = false;
    		fcModeTabuDuration = fcModeTabuDurationSize;
    	}else{
    		fcModeIsPrevTabu = false;
    		fcModeTabuDurationSize = Vars.tabuDurationLBForFC; //@30052011
    	}
    	
    	selection.update(performanceRate, worseningRate, timeExecList, currPerformance, prevPerformance, learningRateMultiplierList); //new method @12052011
    	
    	prevPerformance.getCopyOf(currPerformance);
	}
	
	
	/**
	 * Set/Update the heuristics' class type (generic heuristic types)
	 * 
	 * @param performance		the performance element showing the current performance of the heuristics in terms of improvement capabilities
	 * @param heuristicIndex	the heuristic index
	 */
	private void setHeuristicClassType(PerformanceElements performance, int heuristicIndex){
		if(performance.getTotalHeurImprovement()[heuristicIndex] > 0){
			if(performance.getTotalHeurWorsening()[heuristicIndex] == 0){ 
				heuristicClassTypeList[heuristicIndex] = HeuristicClassType.OnlyImproving;
			}else if(performance.getTotalHeurImprovement()[heuristicIndex] >= performance.getTotalHeurWorsening()[heuristicIndex]){
				heuristicClassTypeList[heuristicIndex] = HeuristicClassType.ImprovingMoreOrEqual;
			}else if(performance.getTotalHeurImprovement()[heuristicIndex] < performance.getTotalHeurWorsening()[heuristicIndex]){
				heuristicClassTypeList[heuristicIndex] = HeuristicClassType.WorseningMore;
			}
		}else{
			if(performance.getTotalHeurWorsening()[heuristicIndex] != 0){ 
				heuristicClassTypeList[heuristicIndex] = HeuristicClassType.OnlyWorsening;
			}else{
				heuristicClassTypeList[heuristicIndex] = HeuristicClassType.OnlyEqual;
			}
		}
	}

	
	
	/**
	 * Print current state of the search
	 */
	private void printCurrentState(){
		log.info("  >> iter="+ numberOfIterations+ " (rst="+numberOfRestarts+") new=" +newFitness+ 
                " curr=" + currentFitness +  " best=" + bestFitness +
                " :: ("+(System.currentTimeMillis()-startTime)/1000F+" | "+totalExecTime/1000F+
                ") @ phL="+Vars.phaseLength+" (nonTabuHr#="+(numberOfHeuristics-selection.getNumberOfTabuHeuristics())+" outOf "+numberOfHeuristics+") "+
                " #OfNBestAll="+totalNumOfNewBestFound+" [selPow="+fcSelectPow+"] "+
                " => FC="+numberOfFCNewBest+" (NotActual="+numberOfFCNewBestNotActual+") TbSize="+fcModeTabuDurationSize+" [fcMod="+fcMode+
                " : TD="+fcModeTabuDuration);
	}
	
	
	/**
	 * Get current state of the search in a string (for writing to the corresponding .csv file)
	 * 
	 * @param heurIndex	the heuristic index
	 */
	private String getCurrentStateForBestAsStr(int heurIndex){
		return numberOfIterations+";"+
		       (""+((System.currentTimeMillis()-startTime)/1000F)).replace(".", ",")+";"+
		       (""+bestFitness).replace(".", ",")+";"+
		       heurIndex+";"+
		       (heurIndex != -1 ? (newBestByRelay ? 1 : 0) : -1)+";"+
		       currPerformance.getNumberOfMovesAsStr()+";"+
		       selection.getProbabilityListAsString().replace(".", ",");
	}
	
	/**
	 * Get the parameter values of the heuristics in a string (for writing to the corresponding .csv file)
	 * 
	 * @return	the heuristics' parameter values
	 */
	private String getCurrentStateForLOCAsStr(){
		return numberOfIterations+";"+
		       (""+((System.currentTimeMillis()-startTime)/1000F)).replace(".", ",")+";"+
		       (""+bestFitness).replace(".", ",")+";"+
		       selection.getLevelOfChangeListAsString().replace(".", ",")+";"+
		       getHeuristicClassTypesAsString();
	}
	
	/**
	 * Get the speed ratios of the heuristics in a string (for writing to the corresponding .csv file)
	 * 
	 * @return	the speed rations of the heuristics
	 */
	private String getCurrentStateForLRMAsStr(){
		String str = numberOfIterations+";"+
			         (""+((System.currentTimeMillis()-startTime)/1000F)).replace(".", ",")+";"+
			         (""+bestFitness).replace(".", ",")+";";
		
		for(int i = 0; i < numberOfHeuristics; i++){
			str += (""+learningRateMultiplierList[i]).replace(".", ",");
			
			if(i != numberOfHeuristics-1){
				str += ";";
			}
		}
		
		return str;
	}
	
	
	/**
	 * Get the improvement range (lowest improvement-higher improvement) of the heuristics in a string (for writing to the corresponding .csv file)
	 * 
	 * @return	the improvement range of each heuristic
	 */
	private String getCurrentStateForIMPRAsStr(){
		String str = numberOfIterations+";"+
			         (""+((System.currentTimeMillis()-startTime)/1000F)).replace(".", ",")+";"+
			         (""+bestFitness).replace(".", ",")+";";
		
		for(int i = 0; i < numberOfHeuristics; i++){
			str += (""+currPerformance.getImprovementRange()[i][0]).replace(".", ",")+";";
			str += (""+currPerformance.getImprovementRange()[i][1]).replace(".", ",");
			
			if(i != numberOfHeuristics-1){
				str += ";";
			}
		}
		
		return str;
	}
	
	
	/**
	 * Get the heuristics' performance details in a string (for writing to the corresponding .csv file)
	 * 
	 * @return the heuristics' performance details
	 */
	private String getHeuristicCounterDetailsAsStr(){
		String str = "";

		str = numberOfIterations+";"+
			  (""+((System.currentTimeMillis()-this.startTime)/1000F)).replace(".", ",")+";"+
			  (""+bestFitness).replace(".", ",")+";";
		for(int i = 0; i < numberOfHeuristics; i++){
			str += currPerformance.getNumberOfMoves()[i]+";"+
				   currPerformance.getNumberOfImprovingBestMoves()[i]+";"+
				   currPerformance.getNumberOfImprovingMoves()[i]+";"+
				   currPerformance.getNumberOfEqualMoves()[i]+";"+
				   currPerformance.getNumberOfWorseningMoves()[i]+";"+
				   (""+(currPerformance.getSpentExecutionTime()[i]/1000000000L)).replace(".", ",")+";";
		}
		
		return str;
	}
	
	
	/**
	 * Get the current iteration limit and the threshold values of the acceptance mechanism in a string (for writing to the corresponding .csv file)
	 * 
	 * @return the details about the current status of the acceptance mechanism
	 */
	private String getIterationLimitDetailsAsStr(){
		return numberOfIterations+";"+
		       (""+((System.currentTimeMillis()-this.startTime)/1000F)).replace(".", ",")+";"+
		       (""+bestFitness).replace(".", ",")+";"+
	            acceptance.getIterationLimitWithThresholdValuesDetailsAsStr();
	}
	
	/**
	 * Get the current status of the acceptance mechanism (for writing to the corresponding .csv file)
	 * 
	 * @return the details about the current status of the acceptance mechanism
	 */
	private String getAcceptanceDetailsAsStr(){
		return numberOfIterations+";"+
			   (""+((System.currentTimeMillis()-startTime)/1000F)).replace(".", ",")+";"+
			   (""+bestFitness).replace(".", ",")+";"+
			   (""+currentFitness).replace(".", ",")+";"+
			   (""+acceptance.getCurrentThreshold()).replace(".", ",");
	}  
	
	
	/**
	 * Get the current status of the relay hybridisation (for writing to the corresponding .csv file)
	 * 
	 * @return	the details about the current status of the relay hybridisation
	 */
	private String getRelayDetailsAsStr(int relayFirstHeurIndex, int relaySecondHeurIndex, 
			                            double relayFirstFitness, double relaySecondFitness){
		return numberOfIterations+";"+
		      (""+((System.currentTimeMillis()-this.startTime)/1000F)).replace(".", ",")+";"+
		      (""+newFitness).replace(".", ",")+";"+
		      (""+currentFitness).replace(".", ",")+";"+
		      (""+relayFirstFitness).replace(".", ",")+";"+
		      (""+relaySecondFitness).replace(".", ",")+";"+
		      relayFirstHeurIndex+";"+
		      relaySecondHeurIndex+";"+
		      (""+selection.getLevelOfChangeList()[relayFirstHeurIndex]).replace(".", ",")+";"+
		      (""+selection.getLevelOfChangeList()[relaySecondHeurIndex]).replace(".", ",")+";"+
		      numberOfNewBestForAllFC+";"+
		      numberOfFCNewBest+";"+
		      spentIterForRelay+";"+
		      spentIterForValidRelay+";"+
		      (""+(spentTimeForRelay/1000000000F)).replace(".", ",")+";"+
		      getAfterListForRelayAsString()+";"+
		      selection.getProbListForRelayAsString().replace(".", ",");
	}
	
	
	/**
	 * Get the current status of the relay hybridisation in case of no new best solution is found (for writing to the corresponding .csv file)
	 * 
	 * @return	the details about the current status of the relay hybridisation
	 */
	private String getRelayDetailsAsStrJustInfo(){
		return numberOfIterations+";"+
		      (""+((System.currentTimeMillis()-this.startTime)/1000F)).replace(".", ",")+";"+
		      "-1;-1;-1;-1;-1;-1;-1;-1;"+
		      numberOfNewBestForAllFC+";"+
		      numberOfFCNewBest+";"+
		      spentIterForRelay+";"+
		      spentIterForValidRelay+";"+
		      (""+(spentTimeForRelay/1000000000F)).replace(".", ",")+";"+
		      getAfterListForRelayAsString()+";"+
		      selection.getProbListForRelayAsString().replace(".", ",");
	}
	
	
	/**
	 * Get the list of second heuristic for each heuristic in a string form  (for writing to the corresponding .csv file)
	 * 
	 * @return the list of second heuristics for each heuristic
	 */
	private String getAfterListForRelayAsString(){
		String str = "";
		
		for(int i = 0; i < numberOfHeuristics; i++){
			for(int al = 0; al < applyAfterHeurList.get(i).size(); al++){
				str += ""+applyAfterHeurList.get(i).get(al);
					
				if(al < applyAfterHeurList.get(i).size()-1){
					str +="::";
				}
			}
			
			if(i < numberOfHeuristics-1){
				str +=";";
			}
		}
		
		return str;
	}
	
	/**
	 * The current types of the heuristics (for writing to the corresponding .csv file)
	 * 
	 * @return the current types of the heuristics
	 */
	private String getHeuristicClassTypesAsString(){
		String str = "";
		
		for(int i = 0; i < numberOfHeuristics; i++){
			str += heuristicClassTypeList[i].toString();
			
			if(i != numberOfHeuristics-1){
				str += ";";
			}
		}
		
		return str;
	}
	
	/**
	 * Name of the hyper-heuristic
	 * 
	 * @return name of the hyper-heuristic
	 */
	@Override
	public String toString() {
		return "GIHH";
	}

}
