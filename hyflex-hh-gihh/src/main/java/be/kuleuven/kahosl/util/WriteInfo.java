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

/**
 * This class involves choices whether certain files maintaining certain statistics about a search process. 
 * A file is written if its corresponding parameter value is set to true.
 */
public class WriteInfo implements Serializable {

	/** Subfolder name where the generated files are saved **/
	public static String resultSubFolderName;
	
	/** Main folder where the subfolder which consists of solution and stat files, is placed **/
	public static String mainFolder = "results/";
	
	/** 
	 * A file with the information about accepted solutions. It consists of 
	 * the current iteration (Iteration), 
	 * the current time (Time), 
	 * the current best fitness (BestFitness), 
	 * the current fitness (CurrentFitness), 
	 * the current threshold level (Threshold) 
	 */
	public static boolean writeACFile = true; /** Maintains statistics about accepted solutions **/
	
	/** 
	 * A file with the information about best solutions found. It consists of 
	 * consists of the current iteration (Iteration), 
	 * the current time (Time), 
	 * the current best fitness (BestFitness), 
	 * the index of the called heuristic at the time the corresponding the information is captured (LastCalledHeur), 
	 * whether the new best solution is found by the relay hybridisation (IsRelay), 
	 * the number of moves performed by each heuristic (NumOfMoves-LLH i), 
	 * the selection probability of each heuristic (LAProb-LLH i)
	 */
	public static boolean writeBFFile = true;
	
	/** 
	 * A file with the information on the changes of the quality index (QI) values for the selection mechanism.
	 * It consists of 
	 * the current iteration (Iteration), 
	 * the current time (Time), 
	 * the quality index (QI) values of each heuristic (QI-i)
	 */
	public static boolean writeQIFile = true;
	
	/** 
	 * A file with the information on the changes of the tabu duration (d) values for the selection mechanism.
	 * It consists of 
	 * the current iteration (Iteration), 
	 * the current time (Time), 
	 * the tabu duration (d) values of each heuristic (TD-i)
	 */
	public static boolean writeTDFile = true;
	
	/**
	 * A file with the information on the changes of the iteration limit (k) values for the acceptanc mechanism.
	 * It consists of 
	 * the current iteration (Iteration), 
	 * the current time (Time), 
	 * the current best fitness (BestFitness), 
	 * the iteration limit (k) (IterLimit), 
	 * the threshold values in the current threshold list (Threshold-i)
	 */
	public static boolean writeITFile = true;
	
	/**
	 * A file with the information on the changes on the behaviour of each heuristic in terms of improvement capabilities and speed.	 
	 * It consists of 
	 * the current iteration (Iteration), 
	 * the current time (Time), 
	 * the current best fitness (BestFitness), 
	 * the number of moves performed by each heuristic (Call-i), 
	 * the number of new best solutions found by each heuristic (Best-i), 
	 * the number of improving solutions found by each heuristic (Imp-i), 
	 * the number of equal quality solutions found by each heuristic (Eq-i), 
	 * the number of worsening solutions found by each heuristic (Wrs-i), 
	 * the current spent execution time by each heuristic (Tm-i)
	 */
	public static boolean writeHCntFile = true; 
	
	/** 
	 * A file with the information on the effective heuristic pairs detected by the relay hybridisation.
	 * It consists of 
	 * the current iteration (Iteration), 
	 * the current time (Time), 
	 * the current best fitness (BestFitness), 
	 * the quality of the solution used to find the new best solution (PrevFitness), 
	 * the fitness value of the solution found after applying the first heuristic (FirstFitness), 
	 * the fitness value of the solution found after applying the second heuristic (SecondFitness),
	 * the level of change value (heuristic parameter value) of the first heuristic (1st-LOC),
	 * the level of change value (heuristic parameter value) of the second heuristic (2nd-LOC),
	 * the number of new best solutions found when relay hybridisation is preferred over single heuristics (NewBestCntAll),
	 * the number of new best solutions found by the relay hybridisation (NewBestCntValid),
	 * the number of iterations spent when relay hybridisation is preferred over single heuristics (SpentIter)
	 * the number of iterations spent by the relay hybridisation (ValidSpentIter),
	 * the execution time spent by the relay hybridisation (SpentTime),
	 * the second heuristic list of each heuristic (After-LLH i)
	 * the selection probability of each heuristic as first heuristic (RelayProb-LLH i)
	 */
	public static boolean writeRHFile = true;
	
	/** 
	 * A file with the information on the level of change referring to the heuristics parameter values.
	 * It consists of 
	 * the current iteration (Iteration), 
	 * the current time (Time), 
	 * the current best fitness (BestFitness), 
	 * the level of change value for each heuristic (LOC-LLH i),
	 * the heuristic (generic) class type for each heuristic (CT-LLH i)
	 * 
	 */
	public static boolean writeLOCFile = true;
	
	/** 
	 * A file with the information on the units showing the speed of the heuristics with respect to the fastest non-tabu heuristic.
	 * It consists of 
	 * the current iteration (Iteration), 
	 * the current time (Time), 
	 * the current best fitness (BestFitness), 
	 * the units showing the speed of the heuristics with respect to the fastest non-tabu heuristic (LRM-LLH i)
	 */
	public static boolean writeLRMFile = true;
	
	/**
	 * A file with the information about the lowest and highest fitness improvements provided by each heuristic.
	 * It consists of 
	 * the current iteration (Iteration), 
	 * the current time (Time), 
	 * the current best fitness (BestFitness),   
	 * the lowest fitness improvement provided by each heuristic (LB-H i)
	 * the highest fitness improvement provided by each heuristic (UB-H i)
	 */
	public static boolean writeIMPRFile = true;

	/** An iteration value to monitor and write the related information to the solution stat files **/
	public static int periodicWriteIteration = Print.iterationNum;
}
