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

package be.kuleuven.kahosl.analysis;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to monitor the performance of the heuristics in terms of improvement capabilities and speed.
 */
public class PerformanceElements implements Serializable {
	
	private static final Logger log = LoggerFactory.getLogger(PerformanceElements.class);

	/** The number of heuristics **/
	private int numberOfHeuristics;	/* number of heuristics	*/
	
	/** An array of values showing the number of moves performed by each heuristic **/
	protected long[] numberOfMoves;	
	
	/** An array of values showing the number of new best solutions found by each heuristic **/
	protected long[] numberOfImprovingBestMoves;
	
	/** An array of values showing the number of improvement solutions found by each heuristic **/
	protected long[] numberOfImprovingMoves;
	
	/** An array of values showing the number of equal quality solutions found by each heuristic **/
	protected long[] numberOfEqualMoves;
	
	/** An array of values showing the number of worsening solutions found by each heuristic **/
	protected long[] numberOfWorseningMoves;
	
	/** An array of the spent execution time by each heuristic **/
	protected double[] spentExecutionTime;
	
	/** An array of values showing the amount of fitness improvement provided by each heuristic **/
	protected double[] totalHeurImprovement;
	
	/** An array of values showing the amount of fitness worsening caused by each heuristic **/
	protected double[] totalHeurWorsening;
	
	/** An array of the lowest and highest improvement provided by each heuristic**/
	protected double[][] improvementRange; 
	
	/** An array of values showing the number of moves over time by each heuristic **/
	protected double[] numberOfMovesPerTime;
	
	/** The number of new best solutions found by the relay hybridisation **/
	protected long FCNumberOfImprovingBestMoves;

	
	/**
	 * Constructor of PerformanceElements
	 * 
	 * @param numberOfHeuristics	the number of low-level heuristics
	 */
	public PerformanceElements(int numberOfHeuristics){
		this. numberOfHeuristics = numberOfHeuristics;
		
		numberOfMoves = new long[numberOfHeuristics];
		numberOfImprovingBestMoves = new long[numberOfHeuristics];
		numberOfImprovingMoves = new long[numberOfHeuristics];
		numberOfEqualMoves = new long[numberOfHeuristics];
		numberOfWorseningMoves = new long[numberOfHeuristics];
		spentExecutionTime = new double[numberOfHeuristics];
		
		totalHeurImprovement = new double[numberOfHeuristics];
		totalHeurWorsening = new double[numberOfHeuristics];
		
		improvementRange = new double[numberOfHeuristics][2];
		
		numberOfMovesPerTime = new double[numberOfHeuristics];
		
		FCNumberOfImprovingBestMoves = 0;
	}
	
	/**
	 * Reset the performance elements
	 */
	public void resetElements(){

		for(int i = 0; i < numberOfHeuristics; i++){
			numberOfMoves[i] = 0;
			numberOfImprovingBestMoves[i] = 0;
			numberOfImprovingMoves[i] = 0;
			numberOfEqualMoves[i] = 0;
			numberOfWorseningMoves[i] = 0;
			spentExecutionTime[i] = 0;
			
			improvementRange[i][0] = 0;
			improvementRange[i][1] = 0;
			
			numberOfMovesPerTime[i] = 0;
		}

		FCNumberOfImprovingBestMoves = 0;
	}
	
	/**
	 * Get copy of a performance element
	 * 
	 * @param perf	a performance element to be copied
	 */
	public void getCopyOf(PerformanceElements perf){
		System.arraycopy(perf.numberOfMoves, 0, numberOfMoves, 0, numberOfHeuristics);
		System.arraycopy(perf.numberOfImprovingBestMoves, 0, numberOfImprovingBestMoves, 0, numberOfHeuristics);
		System.arraycopy(perf.numberOfImprovingMoves, 0, numberOfImprovingMoves, 0, numberOfHeuristics);
		System.arraycopy(perf.numberOfEqualMoves, 0, numberOfEqualMoves, 0, numberOfHeuristics);
		System.arraycopy(perf.numberOfWorseningMoves, 0, numberOfWorseningMoves, 0, numberOfHeuristics);
		System.arraycopy(perf.spentExecutionTime, 0, spentExecutionTime, 0, numberOfHeuristics);
		System.arraycopy(perf.totalHeurImprovement, 0, totalHeurImprovement, 0, numberOfHeuristics);
		System.arraycopy(perf.totalHeurWorsening, 0, totalHeurWorsening, 0, numberOfHeuristics);
		
		System.arraycopy(perf.numberOfMovesPerTime, 0, numberOfMovesPerTime, 0, numberOfHeuristics);
		
		FCNumberOfImprovingBestMoves = perf.getFCNumberOfImprovingBestMoves();
		
		for(int i = 0; i < numberOfHeuristics; i++){
			improvementRange[i][0] = perf.getImprovementRange()[i][0];
			improvementRange[i][1] = perf.getImprovementRange()[i][1];
		}
	}
	
	/**
	 * Add long-type values to a given location in an array
	 * 
	 * @param arr	a long-type array
	 * @param inx	an index value of the array
	 * @param val	a value to set to the given index value
	 */
	public void add(long[] arr, int inx, long val){
		arr[inx] += val;
	}
	
	/**
	 * Add double-type values to a given location in an array
	 * 
	 * @param arr	a double-type array
	 * @param inx	an index value of the array
	 * @param val	a value to set to the given index value
	 */
	public void add(double[] arr, int inx, double val){
		arr[inx] += val;
	}
	
	
	/**
	 * Update the performance elements of a heuristic
	 * 
	 * @param heuristicIndex		index number of the heuristic
	 * @param fitnessBefore			quality of the solution before the corresponding heuristic is applied
	 * @param fitnessAfter			quality of the solution after the corresponding heuristic is applied
	 * @param bestFitness			fitness of the current best solution
	 * @param heursiticStartTime	starting time of applying the heuristic
	 * @param heursiticEndTime		finishing time of applying the heuristic
	 */
	public void updatePerformanceElements(int heuristicIndex, double fitnessBefore, double fitnessAfter,
			                              double bestFitness, long heursiticStartTime, long heursiticEndTime){
		
		/** number of moves **/
		numberOfMoves[heuristicIndex]++; 
		
		
		/** improvement range **/
		double diff = Math.abs(fitnessAfter-fitnessBefore);
		if(diff != 0){ //@26052011
			//if(improvementRange[heuristicIndex][0] == 0 || diff < improvementRange[heuristicIndex][0]){
			if(diff < improvementRange[heuristicIndex][0]){
				improvementRange[heuristicIndex][0] = diff;
			}
			//if(improvementRange[heuristicIndex][1] == 0 || diff > improvementRange[heuristicIndex][1]){
			if(diff > improvementRange[heuristicIndex][1]){
				improvementRange[heuristicIndex][1] = diff;
			}
		}
		
		
		/* update based on fitness comparison */
		if(fitnessAfter > fitnessBefore){ //Worsening move
			numberOfWorseningMoves[heuristicIndex]++;
			totalHeurWorsening[heuristicIndex] += (fitnessAfter-fitnessBefore);
			

        }else if(fitnessAfter < fitnessBefore){ //Improving move
        	numberOfImprovingMoves[heuristicIndex]++;
        	totalHeurImprovement[heuristicIndex] += (fitnessBefore-fitnessAfter);
        	
	        if(fitnessAfter < bestFitness){	
	        	numberOfImprovingBestMoves[heuristicIndex]++;
	        }
        }else{
        	numberOfEqualMoves[heuristicIndex]++;
        }
		

		/** spent execution time **/
		spentExecutionTime[heuristicIndex] += (heursiticEndTime-heursiticStartTime);
	}
	
	
	/**
	 * Get number of moves performed by each heuristic
	 * 
	 * @return	the number of moves performed by each heuristic in a string
	 */
	public String getNumberOfMovesAsStr() {
		String str = "";
		
		for(int i = 0; i < numberOfHeuristics; i++){
			str += numberOfMoves[i];
			if(i != numberOfHeuristics-1){
				str += ";";
			}
		}
		
		return str;
	}

	
	/**
	 * Get number of heuristics
	 * 
	 * @return	number of heuristics
	 */
	public int getNumberOfHeuristics() {
		return numberOfHeuristics;
	}

	/**
	 * Get number of moves performed by each heuristic
	 * 
	 * @return	an array where each element refers to the number of moves performed by one heuristic
	 */
	public long[] getNumberOfMoves() {
		return numberOfMoves;
	}

	/**
	 * Get number of new best solutions found
	 * 
	 * @return	an array where each element refers to the number of new best solutions found by one heuristic
	 */
	public long[] getNumberOfImprovingBestMoves() {
		return numberOfImprovingBestMoves;
	}

	/**
	 * Get number of improving solutions found
	 * 
	 * @return	an array where each element refers to the number of improving solutions found by one heuristic
	 */
	public long[] getNumberOfImprovingMoves() {
		return numberOfImprovingMoves;
	}

	/**
	 * Get number of equal quality solutions found
	 * 
	 * @return	an array where each element refers to the number of equal quality solutions found by one heuristic
	 */
	public long[] getNumberOfEqualMoves() {
		return numberOfEqualMoves;
	}

	/**
	 * Get number of worsening solutions found
	 * 
	 * @return	an array where each element refers to the number of worsening solutions found by one heuristic
	 */
	public long[] getNumberOfWorseningMoves() {
		return numberOfWorseningMoves;
	}

	/**
	 * Get spent execution time by each heuristic
	 * 
	 * @return	an array where each element refers to the spent execution time by one heuristic
	 */
	public double[] getSpentExecutionTime() {
		return spentExecutionTime;
	}

	/**
	 * Get the total improvement over the solutions' quality
	 * 
	 * @return	an array where each element refers to the total fitness improvement provided by one heuristic
	 */
	public double[] getTotalHeurImprovement() {
		return totalHeurImprovement;
	}

	/**
	 * Get the total worsening over the solutions' quality
	 * 
	 * @return	an array where each element refers to the total fitness worsening caused by one heuristic
	 */
	public double[] getTotalHeurWorsening() {
		return totalHeurWorsening;
	}


	/**
	 * Get the number of new best solutions found the by the relay hybridisation
	 * 
	 * @return	the number of new best solutions found the by the relay hybridisation 
	 */
	public long getFCNumberOfImprovingBestMoves() {
		return FCNumberOfImprovingBestMoves;
	}
	
	/**
	 * Set the number of new best solutions found the by the relay hybridisation 
	 * 
	 * @param FCNumberOfImprovingBestMoves	the number of new best solutions found the by the relay hybridisation
	 */
	public void setFCNumberOfImprovingBestMoves(long FCNumberOfImprovingBestMoves) { //@30052011
		this.FCNumberOfImprovingBestMoves = FCNumberOfImprovingBestMoves;
	}

	/**
	 * Get the lowest and highest fitness improvement provided by each heuristic
	 * 
	 * @return	an array of the lowest and highest fitness improvement provided by each heuristic
	 */
	public double[][] getImprovementRange() {
		return improvementRange;
	}

	/**
	 * Get the number of improving solutions found over time by each heuristic
	 * 
	 * @return	an array of the number of improving solutions found over time by each heuristic
	 */
	public double[] getNumberOfMovesPerTime() {
		return numberOfMovesPerTime;
	}

}
