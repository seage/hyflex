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

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.kahosl.analysis.PerformanceElements;
import be.kuleuven.kahosl.hyperheuristic.HeuristicClassType;
import be.kuleuven.kahosl.util.Print;
import be.kuleuven.kahosl.util.Vars;


public class AdaptiveLimitedLAassistedDHSMentorSTD extends SelectionMethod {
	
	/**
	 * Enumeration of level of change types for updating the heuristics' parameters
	 * 
	 */
	public enum LevelOfChangeType {
		/** a found/visited solution is a new best solution **/
		NewBest,
		/** a found/visited solution is an improving solution **/
		Improving,
		/** a found/visited solution is an equal quality solution **/
		Equal,
		/** a found/visited solution is a worsening solution **/
		Worsening,
	}

	/** A logger to write various running information/comments/errors **/
	private static final Logger log = LoggerFactory.getLogger(AdaptiveLimitedLAassistedDHSMentorSTD.class);
	
	/** an array of heuristics' ranks **/
	private int[] rankList;		
	
	/** average of the all ranks **/
	private int avgRank;		
	
	/** default tabu duration showing the number of iterations to exclude a heuristic*/
	private int tabuDuration;
	
	/** an array of tabu duration values involving adapted a tabu duration for each heuristic */
	private int[] adaptiveTabuDurationValueList;
	
	/** an array of boolean values showing the heuristics were tabu during the preceding phase **/
	private boolean[] isTabuPrevPhase; 
	
	/** an array of heuristics' tabu durations **/
	private int[] tabuDurationList;	
	
	private Vector<int[]> tabuListVector;
	
	/** an array of counters denoting how many times each heuristic is made tabu **/
	private int[] tabuCounter; 
	
	/** a probability list involving the single heuristics' selection probabilities **/
	private double[] probabilityList;
	
	/** execution starting time in milliseconds **/
	private long startTime;

	
	/* 
	 * Parameters for learning automata used in the relay hybridisation 
	 */
	/** an array of probabilities denoting the selection probabilities of the first heuristics for the relay hybridisation */
	private double[] probListForRelay;
	/** a learning rate used to update the selection probabilities of the first heuristics for the relay hybridisation **/
	private double alpha1ForRelay = 0.5;
	/** a learning rate used to update the selection probabilities of the first heuristics for the relay hybridisation **/
	private double alpha2ForRelay = 0; 
	/** a learning rate used to update the selection probabilities of the first heuristics for the relay hybridisation **/
	private double betaForRelay = 0; 

	
	/* 
	 * Level of change information for update the heuristics' parameters 
	 */
	private double[] levelOfChangeList;	
	private final static double incrementLOCNewBest = 0.01; 
	private final static double incrementLOCImproving = 0.001;
	private final static double decrementLOCWorsening = 0.0005; 
	private final static double decrementLOCEqual = 0.0001; 
	private final static double initialLOCValue = 0.2; 
	private final static double LOCLowerBound = 0.2; 
	private final static double LOCUpperBound = 1.0; 
	
	
	private int numberOfIterationsAtStuck;
	private boolean directionChangeForLowLOC = false;
	private boolean directionChangeForHighLOC = false;
	/**********************************************************/
	
	private boolean isFirstPhaseProcessed = false;
	
	private int[] consecutiveExclusionCounter;
	private boolean[] disableHeuristicForever;
	private boolean[] isTabuPrevPhaseBeforeFirstUpdate;
	private boolean[] isTurnedIntoNonTabuList; 
	
	
	/**
	 * Constructor of the heuristic selection mechanism
	 * 
	 * @param numberOfHeuristics	number of heuristics
	 * @param tabuDuration			default tabu duration showing the number of iterations to exclude a heuristic
	 * @param r						random number generator
	 */
	public AdaptiveLimitedLAassistedDHSMentorSTD(int numberOfHeuristics, int tabuDuration, Random r) {
		super(numberOfHeuristics, r);
		
		this.tabuDuration = tabuDuration;
		tabuDurationList = new int[numberOfHeuristics];
		rankList = new int[numberOfHeuristics];
		
		tabuCounter = new int[numberOfHeuristics];
		
		for(int i = 0; i < numberOfHeuristics; i++){
			rankList[i] = 1;
			tabuDurationList[i] = 0;
		}
		
		avgRank = 1;
		
		tabuListVector = new Vector<int[]>();
		
		/** LA for Relay **********************************/
		probListForRelay = new double[numberOfHeuristics];
		for(int i = 0; i < numberOfHeuristics; i++){
			probListForRelay[i] = 1.0/numberOfHeuristics;
		}
		/**************************************************/
		
		
		//LA/////////////////////////////////////////////
		probabilityList = new double[numberOfHeuristics];
		
		//Initialize the probability list
		for(int i = 0; i < numberOfHeuristics; i++){
			probabilityList[i] = 1.0/numberOfHeuristics;
		}

		
		//For adapting the tabu duration values for each heuristic by determining the ones which are prohibited consecutively
		adaptiveTabuDurationValueList = new int[numberOfHeuristics];
		isTabuPrevPhase = new boolean[numberOfHeuristics]; 
		for(int i = 0; i < numberOfHeuristics; i++){
			adaptiveTabuDurationValueList[i] = tabuDuration;
			isTabuPrevPhase[i] = false;
		}
		/////////////////////////////////////////////////////////////////
		
		/** level of change list @04052011 *****************/
		levelOfChangeList = new double[numberOfHeuristics];
		for(int i = 0; i < numberOfHeuristics; i++){
			levelOfChangeList[i] = initialLOCValue;
		}
		/****************************************************/
		
		numberOfIterationsAtStuck = 0; //@12052011
		
		isFirstPhaseProcessed = false;
		
		consecutiveExclusionCounter = new int[numberOfHeuristics];
		for(int i = 0; i < numberOfHeuristics; i++){
			consecutiveExclusionCounter[i] = 0;
		}
		 
		/** a heuristic is disabled or not @24052011 **************/
		disableHeuristicForever = new boolean[numberOfHeuristics];
		for(int i = 0; i < numberOfHeuristics; i++){
			disableHeuristicForever[i] = false;
		}
		/**********************************************************/
		
		isTabuPrevPhaseBeforeFirstUpdate = new boolean[numberOfHeuristics];
		for(int i = 0; i < numberOfHeuristics; i++){
			isTabuPrevPhaseBeforeFirstUpdate[i] = false;
		}
		
		isTurnedIntoNonTabuList = new boolean[numberOfHeuristics];
		for(int i = 0; i < numberOfHeuristics; i++){
			isTurnedIntoNonTabuList[i] = true;
		}
			
		startTime = System.currentTimeMillis();
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
		
		if(updateType != 0){		
			double alpha = 0.0;
			if(updateType == 1){ //new best
				alpha = alpha1ForRelay;
			}else if(updateType == 2){ //improvement
				alpha = alpha2ForRelay;
			}else{ //unrecognised
				System.err.println("Unrecognised update type for relay problist");
				System.exit(1);
			}
			
			alpha = alpha/learningRateMultList[secondHeurIndex];
			if(updateType == 1 && alpha < (alpha1ForRelay/25.0)){ //new best
				alpha = alpha1ForRelay/25.0;
			}else if(updateType == 2 && alpha < (alpha2ForRelay/25.0)){ //improvement
				alpha = alpha2ForRelay/25.0;
			}
			
			probListForRelay[heuristicIndex] += alpha*(1.0-probListForRelay[heuristicIndex]);
			for(int i = 0; i < numberOfHeuristics; i++){
				if(i != heuristicIndex)
					probListForRelay[i] -= alpha*probListForRelay[i];
			}
		}else{ //worsening
			
			double beta = betaForRelay*learningRateMultList[secondHeurIndex];
			
			if(beta > Vars.learningRateLimit){
				beta = Vars.learningRateLimit;
			}
			
			probListForRelay[heuristicIndex] -= beta*probListForRelay[heuristicIndex];
			for(int i = 0; i < numberOfHeuristics; i++){
				if(i != heuristicIndex)
					probListForRelay[i] += (beta/(numberOfHeuristics-1)) - beta*probListForRelay[i];
			} 
		}	
	}
	
	
	/*
	 * Print probability list for the selection probabilities of the single heuristics
	 */
	private void printProbabilityList(){
		double probTotal = 0.0;
		System.out.print("  @@ probabilityList => ");
		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] == 0){
				probTotal += probabilityList[i];
				System.out.print(probabilityList[i]+"("+i+")::");
			}
		}
	}
	
	/**
	 * Print probability list for the selection probabilities of the first heuristics in the relay hybridisation
	 */
	private void printProbListForRelay(){
		double probTotal = 0.0;
		System.out.print("  @@ probListForRelay => ");
		for(int i = 0; i < numberOfHeuristics; i++){
			probTotal += probListForRelay[i];
			System.out.print(probListForRelay[i]+"("+i+")::");
		}
		
		System.out.println(" probTotal="+probTotal);
		
	}

	/**
	 * Select a non-tabu heuristic from the elite heuristic subset
	 * 
	 * @return index of the selected heuristic
	 */
	@Override
	public int selectHeuristic() {
		int heurIndex = -1;
		
		double tempProb;
		double tempProbTotal = 0.0;
		
		int lastCheckedHeur = -1;
		tempProb = r.nextDouble();
		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] == 0){
				tempProbTotal += probabilityList[i];
				lastCheckedHeur = i;
				
				if(tempProb <= tempProbTotal){
					heurIndex = i;
					break;
				}
			}
		}
		
		//printProbabilityList();
		if(heurIndex == -1){
			heurIndex = lastCheckedHeur;
			//printProbabilityList();
		}

		numCalled[heurIndex]++;
		iterations++;
		
		return heurIndex;
	}
	

	/**
	 * Select an improving heuristic (used for choosing the second heuristic in the relay hybridisation)
	 * 
	 * @param heurClassTypeList	heuristics class types (based on introduced generic heuristic types)
	 * @return					index of the selected heuristic 					
	 */	
	public int selectAnImprovementHeuristic(HeuristicClassType[] heurClassTypeList){
	
		int heurIndex = -1;
		//Select an OnlyImproving Heuristic
		int cnt = 0;
		
		if(r.nextDouble() <= 0.5){
			do{
				heurIndex = r.nextInt(numberOfHeuristics);
				if(cnt == 10){
					break;
				}
				cnt++;
			}while(heurClassTypeList[heurIndex] != HeuristicClassType.OnlyImproving);
			
			if(cnt == 10){
				cnt = 0;
				do{
					heurIndex = r.nextInt(numberOfHeuristics);
					if(cnt == 10){
						break;
					}
					cnt++;
				}while(heurClassTypeList[heurIndex] != HeuristicClassType.ImprovingMoreOrEqual);
			}
			
		}else{
			do{
				heurIndex = r.nextInt(numberOfHeuristics);
				if(cnt == 10){
					break;
				}
				cnt++;
			}while(heurClassTypeList[heurIndex] != HeuristicClassType.ImprovingMoreOrEqual);
		}
		
		if(cnt == 10){ //Not found, select a heuristic using the regular selection function
			return selectHeuristic(); //iterations and numCalled are already increased
		}
		
		
		numCalled[heurIndex]++;
		iterations++;
		
		return heurIndex;
	}
	
	
	/**
	 * Select the first heuristic for the relay hybridisation
	 * 
	 * @return	heuristic index of the selected heuristic
	 */
	@Override
	public int selectTheFirstHeuristicForRelay() {
		int heurIndex = -1;
		double tempTotal = 0;
		
		double tempProb = r.nextDouble();
		
		for(int i = 0; i < numberOfHeuristics; i++){
			tempTotal += probListForRelay[i];
			if(probListForRelay[i] != 0 && tempProb <= tempTotal){
				heurIndex = i;
				break;
			}
			
			if(i == numberOfHeuristics-1 && tempTotal >= 0.9){
				heurIndex =  numberOfHeuristics-1;
			}
		}
		
		return heurIndex;
	}
	
	
	
	/**
	 * Get the number of tabu (excluded) heuristics
	 * 
	 * @return	the number of tabu heuristics
	 */
	@Override
	public int getNumberOfTabuHeuristics() {
		int cnt = 0;
		
		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] != 0){
				cnt++;
			}
		}
		
		return cnt;
	}
	
	
	/**
	 * Print the number of moves performed by each heuristic
	 */
	public void printHeurCallList(){		
		String str = "";
		for(int i = 0; i < numberOfHeuristics; i++){
			str += numCalled[i];
			if(i != numberOfHeuristics-1)
				str += ";";
		}
		
		//log.info("### HEURISTIC CALLS: "+str);
	}
	
	/**
	 * Print the heuristics' ranks 
	 */
	public void printRankList(){		
		String str = "";
		for(int i = 0; i < numberOfHeuristics; i++){
			str += rankList[i];
			if(i != numberOfHeuristics-1)
				str += ";";
		}
		
		//log.info("### RANK LIST: "+str);
	}
	
	/**
	 * Print the tabu duration list for the heuristics
	 */
	public void printTabuDurationList(){		
		String str = "";
		for(int i = 0; i < numberOfHeuristics; i++){
			str += tabuDurationList[i];
			if(i != numberOfHeuristics-1)
				str += ";";
		}
		
		//log.info("### TABU DURATION LIST: "+str);
	}
	
	/**
	 * Print a given tabu duration list
	 * 
	 * @param tabuDurationList	a list of tabu durations for each heuristic
	 */
	public void printTabuDurationList2(int[] tabuDurationList){		
		String str = "";
		for(int i = 0; i < numberOfHeuristics; i++){
			str += tabuDurationList[i];
			if(i != numberOfHeuristics-1)
				str += ";";
		}
		
		log.info(str);
	}

	/**
	 * Print the tabu list vector
	 */
	public void printTabuListVector(){
		System.out.println("  @@@ printTabuListVector: ");
		for(int i = 0; i < tabuListVector.size(); i++){
			printTabuDurationList2(tabuListVector.get(i));
		}
	}
	
	/**
	 * Print how many times each heuristic is excluded
	 */
	public void printTabuCounterList(){		
		String str = "";
		for(int i = 0; i < numberOfHeuristics; i++){
			str += tabuCounter[i];
			if(i != numberOfHeuristics-1)
				str += ";";
		}
		
		//log.info("### TABU COUNTER: "+str);
	}
	
	
	/**
	 * Update the heuristics' ranks showing their performance
	 * 
	 * @param performanceRate		an array of performance rates of heuristics	
	 * @param currPerformance		current performance status of the heuristics (during current phase)
	 * @param prevPerformance		preceding performance status of the heuristics (during preceding phase)
	 * @param learningMultRateList	an array of units showing the speed of the heuristics with respect to the fastest non-tabu heuristic
	 */
	public void updateRanks(double[] performanceRate, PerformanceElements currPerformance, 
			                PerformanceElements prevPerformance, double[] learningMultRateList){
		
		/** make tabu the disabled heuristics @24052011 **/
		for(int i = 0; i < numberOfHeuristics; i++){
			if(disableHeuristicForever[i]){
				tabuDurationList[i] = 100;
			}
		}
		/*************************************************/
		
		
		int tempRank;
		avgRank = 0;
		
		//System.out.println("----S---IR-WRSList----");
		for(int i = 0; i < numberOfHeuristics; i++){
			if(performanceRate[i] != 0.0 && tabuDurationList[i] == 0){
				tempRank = 1;
				for(int j = 0; j < numberOfHeuristics; j++){
					if(i != j && tabuDurationList[j] == 0){
						if(performanceRate[i] > performanceRate[j]){
							tempRank++;
						}
					}
				}	
				
				rankList[i] = tempRank;
			}else{
				rankList[i] = 1;
			}
			
			avgRank += rankList[i];
		}
	
		avgRank /= numberOfHeuristics;
		
	
		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] == 0){
				if(rankList[i] < avgRank){ //If the heuristic is not tabu
					
					if(isTabuPrevPhase[i]){
						if(adaptiveTabuDurationValueList[i] < Vars.tabuDurationUB){
							adaptiveTabuDurationValueList[i]++;
							
							consecutiveExclusionCounter[i]++; 
						}
					}else{
						adaptiveTabuDurationValueList[i] = tabuDuration;
						consecutiveExclusionCounter[i] = 0; 
					}
					
					tabuDurationList[i] = adaptiveTabuDurationValueList[i];
					tabuCounter[i]++;
				}else{
					if(isTabuPrevPhase[i]){
						isTabuPrevPhaseBeforeFirstUpdate[i] = true; 
					}else{
						isTabuPrevPhaseBeforeFirstUpdate[i] = false;
					}
					
					isTurnedIntoNonTabuList[i] = false;
				}
			}else if(tabuDurationList[i] > 0){
				tabuDurationList[i]--;
				
				if(tabuDurationList[i] == 0){
					isTabuPrevPhase[i] = true;
					isTurnedIntoNonTabuList[i] = true;
				}
				else{
					isTabuPrevPhase[i] = false;
				}
			}
		}
		
		
		
		/** @24052011 *********************************************************************************************************/ 
		boolean[] isHeurExcludedOverStdList = excludeHeuristicOverStd(currPerformance, prevPerformance, learningMultRateList);
		/**********************************************************************************************************************/
		for(int i = 0; i < numberOfHeuristics; i++){
			if(isHeurExcludedOverStdList[i]){
				
				if(isTabuPrevPhaseBeforeFirstUpdate[i]){
					if(adaptiveTabuDurationValueList[i] < Vars.tabuDurationUB){
						adaptiveTabuDurationValueList[i]++;
						
						consecutiveExclusionCounter[i]++; 
					}
				}
				
				
				tabuDurationList[i] = adaptiveTabuDurationValueList[i];

				tabuCounter[i]++;
				
			}else if(tabuDurationList[i] == 0){ 
				if(!isTabuPrevPhase[i]){
					adaptiveTabuDurationValueList[i] = tabuDuration;
					
					consecutiveExclusionCounter[i] = 0; 
				}else if(!isTurnedIntoNonTabuList[i]){
					adaptiveTabuDurationValueList[i] = tabuDuration;
					isTabuPrevPhase[i] = false;

					consecutiveExclusionCounter[i] = 0;
				}
				
			}
		}
		/**********************************************************************/
		

		
		/** Check whether the heuristic should be disabled forever @24052011 **/
		for(int i = 0; i < numberOfHeuristics; i++){
			if(adaptiveTabuDurationValueList[i] == Vars.tabuDurationUB && learningMultRateList[i] > 5){ //also check learningMultRateList[i] < 5 (do not exclude fast heuristics forever)
				disableHeuristicForever[i] = true;
			}
		}
		/**********************************************************************/
		
		
		int[] tempTabuDurList = new int[numberOfHeuristics];
		for(int i = 0; i < numberOfHeuristics; i++){
			tempTabuDurList[i] = tabuDurationList[i];
		}
		
		tabuListVector.add(tempTabuDurList);
	}
	
	
	/**
	 * Calculate the average and standard deviation of values in an array
	 * 
	 * @param learningMultRateList	an array of values indicating the speed ratios of non-tabu heuristics with respect to the fastest heuristic in the heuristic set
	 * @return						the average of standard deviation of the values for the given array 
	 */
	private double[] calculateAvgStd(double[] learningMultRateList){
		double[] avgStd = new double[2];
		
		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] == 0){
				avgStd[0] += learningMultRateList[i];
			}
		}
		
		avgStd[0] /= (numberOfHeuristics-getNumberOfTabuHeuristics());
		
		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] == 0){
				avgStd[1] += Math.pow(learningMultRateList[i]-avgStd[0], 2.0);
			}
		}
		
		avgStd[1] = Math.sqrt(avgStd[1]);
		
		return avgStd;
	}
	
	
	/**
	 * Return whether some of the low-level heuristics were excluded based on the extreme exclusion approach
	 * 
	 * @param currPerformance		a performance element concerning the current performance of the hyper-heuristic	
	 * @param prevPerformance		a performance element concerning the previous performance of the hyper-heuristic
	 * @param learningMultRateList	an array of units showing the speed of the heuristics with respect to the fastest non-tabu heuristic
	 * @return						a list of boolean values indicating the excluded heuristic for extreme exclusion
	 */
	private boolean[] excludeHeuristicOverStd(PerformanceElements currPerformance, PerformanceElements prevPerformance, double[] learningMultRateList){

		/* an array showing whether a heuristic is excluded based on the extreme exclusion approach */
		boolean[] isHeurExcludedOverStd = new boolean[numberOfHeuristics];
		
		double[] tempArr = calculateAvgStd(learningMultRateList);
		double avg = tempArr[0];
		double std = tempArr[1];
		
		double smallestNonTabuLRM = -1;
		ArrayList<Integer> newBestFoundHeurList = new ArrayList<Integer>();
		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] == 0){
				if(currPerformance.getNumberOfImprovingBestMoves()[i] > 0){
					newBestFoundHeurList.add(i);
				}
				
				if(i == 0){
					smallestNonTabuLRM = learningMultRateList[i];
				}else{
					if(learningMultRateList[i] < smallestNonTabuLRM){
						smallestNonTabuLRM = learningMultRateList[i];
					}
				}
			}
			
		}
		
		
		/* Exclude slow heuristics without new best solution during the current phase and */ 
		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] == 0 && 
			   !isTabuPrevPhase[i] && 
			   std != 0 && 
			   learningMultRateList[i] > (2.0*avg) && 
			   (currPerformance.getNumberOfImprovingBestMoves()[i]-prevPerformance.getNumberOfImprovingBestMoves()[i]) == 0 && 
			   newBestFoundHeurList.size() > 1 && 
			   std > 2.0 
			   ){
				
				for(int checkInx = 0; checkInx < newBestFoundHeurList.size(); checkInx++){
					if(newBestFoundHeurList.get(checkInx) == i){
						newBestFoundHeurList.remove(checkInx);
						break;
					}
				}
				
				isHeurExcludedOverStd[i] = true;

				if(Print.selection){
					log.info(" ###### STD Exclusion : heuristic="+i+" with LRM="+learningMultRateList[i]+
							 " (tabuDur="+tabuDurationList[i]+" - STD="+std+")\n\n");
				}
				
				tempArr = calculateAvgStd(learningMultRateList);
				avg = tempArr[0];
				std = tempArr[1];
				
			}else{
				if(tabuDurationList[i] == 0){
					isHeurExcludedOverStd[i] = false;
				}
			}
		}
		
		return isHeurExcludedOverStd;
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
	@Override
	public void update(double[] performanceRate, double[] worseningRate, double[] execTimeList, 
			           PerformanceElements currPerformance, PerformanceElements prevPerformance, double[] learningMultRateList){
		
		isFirstPhaseProcessed = true;
		
		updateRanks(performanceRate, currPerformance, prevPerformance, learningMultRateList);

		resetProbabilityListBasedNewBestPerSpeed(currPerformance); 
	}

	
	/** 
	 * Maintain a probability list for selecting single heuristics.
	 * Called at the end of a phase
	 *  
	 * @param currPerformance	current performance state of the heuristics
	 */
	private void resetProbabilityListBasedNewBestPerSpeed(PerformanceElements currPerformance){
		
		double tempTotal = 0.0;
		// set probability list
		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] == 0){
				
				double tempSpentExecTime = currPerformance.getSpentExecutionTime()[i];
				double nano = 1000000000.0;
    			double tempTime = nano*Vars.execTimeSensitivity;
    			/////////////////// For 1 Second - Sensitivity///////////////
        		if(tempSpentExecTime <= tempTime){
        			tempSpentExecTime = Vars.execTimeSensitivity;
        		}else{
        			tempSpentExecTime /= nano;
        		}
        		
        		
        		double timeFactor = (double)(Vars.totalExecutionTime-(System.currentTimeMillis()-startTime))/Vars.totalExecutionTime;
        		probabilityList[i] = Math.pow((1.0+currPerformance.getNumberOfImprovingBestMoves()[i])/tempSpentExecTime, 
        				                      1.0+(3.0*timeFactor*timeFactor*timeFactor)); 
		
		
				tempTotal += probabilityList[i];
			}
		}
		
		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] == 0){
				probabilityList[i] /= tempTotal;
			}
		}
		
		//printProbabilityList();
	}
	
	
	
	/**
	 * Get the probability list of single heuristic selection probabilities
	 * 
	 * @return probability list as a string
	 */
	public String getProbabilityListAsString(){
		String str = ""; 

		for(int i = 0; i < numberOfHeuristics; i++){
			if(tabuDurationList[i] == 0){
				str += probabilityList[i];
			}else{
				str += "0";
			}
			
			if(i != numberOfHeuristics-1){
				str += ";";
			}
		}
		
		return str;
	}
	
	/**
	 * Get the probability list involving the selection probabilities for the relay hybridisation
	 * 
	 * @return	probability list as a string
	 */
	@Override
	public String getProbListForRelayAsString(){
		String str = ""; 

		for(int i = 0; i < numberOfHeuristics; i++){
			str += probListForRelay[i];
			
			if(i != numberOfHeuristics-1){
				str += ";";
			}
		}
		
		return str;
	}
	
	
	/**
	 * Get rank list showing the performance of heuristics
	 * 
	 * @return heuristics' ranks in a string 	
	 */
	@Override
	public String getRankListAsStr(){		
		String str = iterations+";"+(""+(System.currentTimeMillis()-startTime)/1000F).replace(".", ",")+";";
		for(int i = 0; i < numberOfHeuristics; i++){
			str += rankList[i];
			if(i != numberOfHeuristics-1)
				str += ";";
		}
		
		return str;
	}	
	
	/**
	 * Get tabu duration list showing that a heuristic should stay out of the heuristic set for how many iterations
	 * 
	 * @return	heuristics' tabu durations in an array
	 */
	@Override
	public int[] getTabuDurationList(){
		return tabuDurationList;
	}
	
	/**
	 * Get tabu duration list showing that a heuristic should stay out of the heuristic set for how many iterations
	 * 
	 * @return	tabu durations in a string
	 */
	@Override
	public String getTabuDurationListAsStr(){		
		String str = iterations+";"+(""+(System.currentTimeMillis()-startTime)/1000F).replace(".", ",")+";";
		for(int i = 0; i < numberOfHeuristics; i++){
			str += tabuDurationList[i];
			if(i != numberOfHeuristics-1)
				str += ";";
		}
		
		return str;
	}
	
	
	/** 
	 * Update the level of change values (heuristics' parameter values) for a given heuristic 
	 * 
	 * @param lcType		level of change type
	 * @param hClassType	heuristic class type
	 * @param heurIndex		index of the given heuristic 
	 */
	@Override
	public void updateLevelOfChange(LevelOfChangeType lcType, HeuristicClassType hClassType, int heurIndex){
		
		double learningRateLevel = 1.0;
		     	
		/* 
		 * Oscillate the heuristics' level of change values 'at stuck' case 
		 */
		if(Vars.oscilateLOCValues && Vars.isAtStuck && !Vars.restartSearch){

			if(hClassType == HeuristicClassType.OnlyImproving){ // >= 0.5
				if(!directionChangeForHighLOC){
					levelOfChangeList[heurIndex] = 0.5+((LOCUpperBound-0.5)*(numberOfIterationsAtStuck/5000.0));
					
					if(levelOfChangeList[heurIndex] == LOCUpperBound){
						directionChangeForHighLOC = true;
					}
				}else{
					levelOfChangeList[heurIndex] = LOCUpperBound-((LOCUpperBound-0.5)*(numberOfIterationsAtStuck/5000.0));
					
					if(levelOfChangeList[heurIndex] == 0.5){
						directionChangeForHighLOC = false;
					}
				}
			}else{ // if(levelOfChangeList[heurIndex] < 0.5){
				if(!directionChangeForLowLOC){
					levelOfChangeList[heurIndex] = LOCLowerBound+((0.499999999-LOCLowerBound)*(numberOfIterationsAtStuck/5000.0));
					
					if(levelOfChangeList[heurIndex] == 0.499999999){
						directionChangeForLowLOC = true;
					}
				}else{
					levelOfChangeList[heurIndex] = 0.5-((0.5-LOCLowerBound)*(numberOfIterationsAtStuck/5000.0));
					
					if(levelOfChangeList[heurIndex] == LOCLowerBound){
						directionChangeForLowLOC = false;
					}
				}
			}
			
			numberOfIterationsAtStuck++;
			
			if(numberOfIterationsAtStuck == 5000){
				numberOfIterationsAtStuck = 1;
			}

		}else{
		
			double plusMinusZero = 1.0;
			
			if(lcType == LevelOfChangeType.NewBest){
				
				numberOfIterationsAtStuck = 0;
				
				if(hClassType == HeuristicClassType.OnlyImproving){
					double tempProb = r.nextDouble();
					if(tempProb <= 0.5){
						plusMinusZero = 1.0; //increase
					}else{
						plusMinusZero = 0.0; //no change
					}
				}else if(hClassType == HeuristicClassType.ImprovingMoreOrEqual){
					double tempProb = r.nextDouble();
					if(tempProb <= 0.5){ 
						plusMinusZero = 1.0;   //increase 
					}else if(tempProb <= 0.75){
						plusMinusZero = -1.0;  //decrease 
					}else{
						plusMinusZero = 0.0;   //no change
					}
				}
				
				else if(hClassType == HeuristicClassType.WorseningMore){
					double tempProb = r.nextDouble();
					if(tempProb <= 0.5){
						plusMinusZero = 1.0;
					}else{
						plusMinusZero = -1.0;
					}
				} //@16052011 gecici kapattim
				
				levelOfChangeList[heurIndex] += incrementLOCNewBest*learningRateLevel*plusMinusZero;
				
			}else if(lcType == LevelOfChangeType.Improving){
				if(hClassType == HeuristicClassType.OnlyImproving){
					double tempProb = r.nextDouble();
					if(tempProb <= 0.5){
						plusMinusZero = 1.0; //increase
					}else{
						plusMinusZero = 0.0; //no change
					}
				}else 
					
					if(hClassType == HeuristicClassType.ImprovingMoreOrEqual){
					double tempProb = r.nextDouble();
					if(tempProb <= 0.5){
						plusMinusZero = 1.0;  //increase
					}
					else if(tempProb <= 0.75){
						plusMinusZero = -1.0; //decrease
					} 
					
					else{
						plusMinusZero = 0.0; //no change
					}
				}else if(hClassType == HeuristicClassType.WorseningMore){
					double tempProb = r.nextDouble();
					if(tempProb <= 0.5){
						plusMinusZero = 1.0;
					}else{
						plusMinusZero = -1.0;
					}
				}
				
				levelOfChangeList[heurIndex] += incrementLOCImproving*learningRateLevel*plusMinusZero;
				
			}else if(lcType == LevelOfChangeType.Worsening){ //This is not possible for local search
				
				if(hClassType == HeuristicClassType.ImprovingMoreOrEqual){
					double tempProb = r.nextDouble();
					if(tempProb <= 0.5){
						plusMinusZero = 1.0; //decrease
					}else{
						plusMinusZero = 0.0; //no change
					}
				}else{ //WorseningMore or OnlyWorsening
					plusMinusZero = 1.0;
				}
				
				
				levelOfChangeList[heurIndex] -= decrementLOCWorsening*learningRateLevel*plusMinusZero;
				
			}else if(lcType == LevelOfChangeType.Equal){
		
				if(hClassType == HeuristicClassType.OnlyImproving){
					double tempProb = r.nextDouble();
					if(tempProb <= 0.5){
						plusMinusZero = 1.0; //decrease
					}else if(tempProb <= 0.75){
						plusMinusZero = -1.0; //increase
					}else{
						plusMinusZero = 0.0; //no change
					}
				}else if(hClassType == HeuristicClassType.ImprovingMoreOrEqual){
					double tempProb = r.nextDouble();
					if(tempProb <= 0.5){
						plusMinusZero = 1.0; //decrease
					}else{
						plusMinusZero = 0.0; //no change
					}
				}else{//WorseningMore or OnlyWorsening
					plusMinusZero = -1.0;
				}
				
				levelOfChangeList[heurIndex] -= decrementLOCEqual*learningRateLevel*plusMinusZero;
				
			}else{
				System.out.println("@updateLevelOfChange => Uncrecognised LevelOfChangeType : "+lcType.toString());
				System.exit(1);
			}
					
			//Keep the values within their predetermined bounds
			if(levelOfChangeList[heurIndex] < LOCLowerBound){
				levelOfChangeList[heurIndex] = LOCLowerBound;
			}else if(levelOfChangeList[heurIndex] > LOCUpperBound){
				levelOfChangeList[heurIndex] = LOCUpperBound;
			}
		}
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
	@Override
	public void updateSelectionElements(int heuristicIndex, HeuristicClassType heuristicClassType,
			                            double fitnessBefore, double fitnessAfter, double bestFitness, 
			                            long heursiticStartTime, long heursiticEndTime, double[] learningMultRateList, PerformanceElements currPerformance){
		
		
        if(fitnessAfter > fitnessBefore){ /* If the fitness value of the new solution is worse than the earlier solution */
        	if(Vars.enableLAUpdateForSelection){      		
        		resetProbabilityListBasedNewBestPerSpeed(currPerformance); 
	        }
        	
        	updateLevelOfChange(LevelOfChangeType.Worsening, heuristicClassType, heuristicIndex);
        }else if(fitnessAfter < fitnessBefore){ /* If the fitness value of the new solution is better than the earlier solution */
            
            if(fitnessAfter < bestFitness){ /* If the fitness value of the new solution is better than the current best solution */
            	if(Vars.enableLAUpdateForSelection){	            	
            		resetProbabilityListBasedNewBestPerSpeed(currPerformance);
	            }
            	
            	updateLevelOfChange(LevelOfChangeType.NewBest, heuristicClassType, heuristicIndex);
            }else{ /* If the fitness value of the new solution is not better than the current best solution */
            
            	if(Vars.enableLAUpdateForSelection){
            		resetProbabilityListBasedNewBestPerSpeed(currPerformance); 
            	}
            	
            	updateLevelOfChange(LevelOfChangeType.Improving, heuristicClassType, heuristicIndex);
            }
        }else{ /* If the fitness value of the new solution is equal to the earlier solution's fitness */
        	
        	if(Vars.enableLAUpdateForSelection){
        		resetProbabilityListBasedNewBestPerSpeed(currPerformance); 
        	}
        	
        	updateLevelOfChange(LevelOfChangeType.Equal, heuristicClassType, heuristicIndex);
        }
	}
	

	/**
	 * Get the level of change list involving the heuristics' parameter values
	 * 
	 * @return an array of the heuristics' parameter values
	 */
	public double[] getLevelOfChangeList(){
		return levelOfChangeList;
	}
	
	/**
	 * Print the level of change list involving the heuristics' parameter values
	 */
	public void printLevelOfChangeList(){
		System.out.print("  @@ levelOfChangeList => ");
		for(int i = 0; i < numberOfHeuristics; i++){
			System.out.print(levelOfChangeList[i]+" , ");
		}
	}	
	
	/**
	 * Get the level of change list for the heuristics' parameter values in a string
	 * 
	 * @return the heuristics' parameter values in a string
	 */
	public String getLevelOfChangeListAsString(){
		String str = ""; 

		for(int i = 0; i < numberOfHeuristics; i++){
			str += levelOfChangeList[i];
			
			if(i != numberOfHeuristics-1){
				str += ";";
			}
		}
		
		return str;
	}
}
