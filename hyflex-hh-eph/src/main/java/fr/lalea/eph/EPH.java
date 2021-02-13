package fr.lalea.eph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeMap;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import AbstractClasses.ProblemDomain.HeuristicType;

/**
 * Evolutionary Programming Hyper-heuristic
 * with Co-evolution for CHeSC'11
 * 
 * name: EPH
 * 
 * @author David Meignan
 */
public class EPH extends HyperHeuristic {

	//=====================================================
	//      PROBLEM
	private ProblemDomain problem;
	
	//=====================================================
	//      HEURISTICS TYPE AND INDEXES
	
	// Type of heuristic from heuristic index
	private TreeMap<Integer, HeuristicType> heuristicTypes;
	
	// Indexes of heuristics by type
	private ArrayList<Integer> localSearchHeuristicIndexes;
	private ArrayList<Integer> perturbationHeuristicIndexes;
	private ArrayList<Integer> mutationHeuristicIndexes;
	private ArrayList<Integer> crossoverHeuristicIndexes;
	private ArrayList<Integer> ruinRecreateHeuristicIndexes;
	private ArrayList<Integer> otherHeuristicIndexes;
	
	//=====================================================
	//      SOLUTION POOL
	private int startPopIndex;
	private int endPopIndex;
	private int temporarySolutionIndex1;
	private int temporarySolutionIndex2;
	private int temporarySolutionIndex3;
	private ArrayList<Double> solutionValues;
	
	//=====================================================
	//      HEURISTICS STAT.
	
	// Average execution times (Generation index = -1)
	private TreeMap<Integer, Double> preliminaryExecutionTimes;
	private double averageOperatorIterations;
	private double vndVsOneRowFailureRate;
	private double averageVndIterations;
	protected double averageVndTime;
	
	//=====================================================
	//      LOCAL DESCENT PARAMETERS
	
	private int localOptimRepetitionsIncrement = 5;
	private int localOptimMaxRepetitions = 100;
	private TreeMap<Integer, Integer> localOptimumRepetitions;
	private double relativeVNDTimeLimit = 0.002;
	private LOCAL_SEARCH_TYPE localSearchType = LOCAL_SEARCH_TYPE.VND;
	private boolean additionalIntensification = true;
	
	public enum LOCAL_SEARCH_TYPE {
		ONE_ROW,
		VND;
	}
	
	//=====================================================
	//      SEARCH PATTERN
	
	private LinkedList<HeuristicSearchPattern> searchPatternPopulation;
	private String strategyDesc = "";
	private int nbTournaments = 1;
	private boolean orderedLsOperators = false;
	
	/**
	 * Sets the initial solution's population size.
	 */
	private void setInitialPopulationSize(int popSize) {
		problem.setMemorySize(popSize+3);
		startPopIndex = 0;
		endPopIndex = popSize-1;
		temporarySolutionIndex1 = popSize;
		temporarySolutionIndex2 = popSize+1;
		temporarySolutionIndex3 = popSize+2;
		if (solutionValues == null) {
			solutionValues = new ArrayList<Double>();
		}
		while(solutionValues.size() < popSize+3) {
			solutionValues.add(Double.MAX_VALUE);
		}
	}
	
	/**
	 * Reduces the size of the population and keeps the best solutions.
	 */
	private void reducePopulationSize(int popSize) {
		for (int i=popSize; i<=endPopIndex; i++) {
			int insertIndex = -1;
			for (int j=0; j<popSize; j++) {
				if (solutionValues.get(i).doubleValue() 
						< solutionValues.get(j).doubleValue()) {
					if (insertIndex == -1) {
						insertIndex = j;
					} else if (solutionValues.get(insertIndex).doubleValue() 
							< solutionValues.get(j).doubleValue()) {
						insertIndex = j;
					}
				}
			}
			if (insertIndex != -1) {
				problem.copySolution(i, insertIndex);
				solutionValues.set(insertIndex, solutionValues.get(i).doubleValue());
			}
		}
		startPopIndex = 0;
		endPopIndex = popSize-1;
		temporarySolutionIndex1 = popSize;
		temporarySolutionIndex2 = popSize+1;
		temporarySolutionIndex3 = popSize+2;
		while (solutionValues.size()>popSize+3) {
			solutionValues.remove(solutionValues.size()-1);
		}
		solutionValues.set(popSize, Double.MAX_VALUE);
		solutionValues.set(popSize+1, Double.MAX_VALUE);
		solutionValues.set(popSize+2, Double.MAX_VALUE);
	}
	
	/**
	 * Initialize empty solutions in the population. 
	 */
	private void completePopulation() {
		// Generate initial solutions
		for (int i=startPopIndex; i<=endPopIndex; i++) {
			if (solutionValues.get(i).doubleValue() == Double.MAX_VALUE) {
				problem.initialiseSolution(i);
				solutionValues.set(i, problem.getFunctionValue(i));
			}
		}
	}
	
	/**
	 * Populate heuristics indexes.
	 */
	private void populateHeuristicIndexes() {
		heuristicTypes = new TreeMap<Integer, HeuristicType>();
		
		mutationHeuristicIndexes = new ArrayList<Integer>();
		crossoverHeuristicIndexes = new ArrayList<Integer>();
		ruinRecreateHeuristicIndexes = new ArrayList<Integer>();
		otherHeuristicIndexes = new ArrayList<Integer>();
		localSearchHeuristicIndexes = new ArrayList<Integer>();
		
		int heuristics[];
		
		heuristics = problem.getHeuristicsOfType(HeuristicType.OTHER);
		if (heuristics != null) {
			for (int h:heuristics) {
				heuristicTypes.put(h, HeuristicType.OTHER);
				otherHeuristicIndexes.add(h);
			}
		}
		heuristics = problem.getHeuristicsOfType(HeuristicType.MUTATION);
		if (heuristics != null) {
			for (int h:heuristics) {
				heuristicTypes.put(h, HeuristicType.MUTATION);
				mutationHeuristicIndexes.add(h);
			}
		}
		heuristics = problem.getHeuristicsOfType(HeuristicType.CROSSOVER);
		if (heuristics != null) {
			for (int h:heuristics) {
				heuristicTypes.put(h, HeuristicType.CROSSOVER);
				crossoverHeuristicIndexes.add(h);
			}
		}
		heuristics = problem.getHeuristicsOfType(HeuristicType.RUIN_RECREATE);
		if (heuristics != null) {
			for (int h:heuristics) {
				heuristicTypes.put(h, HeuristicType.RUIN_RECREATE);
				ruinRecreateHeuristicIndexes.add(h);
			}
		}
		heuristics = problem.getHeuristicsOfType(HeuristicType.LOCAL_SEARCH);
		if (heuristics != null) {
			for (int h:heuristics) {
				heuristicTypes.put(h, HeuristicType.LOCAL_SEARCH);
				localSearchHeuristicIndexes.add(h);
			}
		}
		perturbationHeuristicIndexes = new ArrayList<Integer>();
		perturbationHeuristicIndexes.addAll(crossoverHeuristicIndexes);
		perturbationHeuristicIndexes.addAll(ruinRecreateHeuristicIndexes);
		perturbationHeuristicIndexes.addAll(mutationHeuristicIndexes);
		perturbationHeuristicIndexes.addAll(otherHeuristicIndexes);
	}
	
	/**
	 * Initializes the values of local optimum repetitions for local descent.
	 */
	private void initLocalOptimumRepetitions() {
		localOptimumRepetitions = new TreeMap<Integer, Integer>();
		for (int op:localSearchHeuristicIndexes) {
			localOptimumRepetitions.put(op, localOptimRepetitionsIncrement);
		}
	}
	
	/**
	 * Disable the repetition increment and remove last repetitions (used
	 * for increment).
	 */
	private void disableLocalOptimumRepetitionsIncrement() {
		// Remove the last repetitions
		int currentRepetitions;
		for (int op:localSearchHeuristicIndexes) {
			currentRepetitions = localOptimumRepetitions.get(op).intValue();
			if (currentRepetitions >= localOptimMaxRepetitions) {
				currentRepetitions = localOptimMaxRepetitions;
			} else {
				currentRepetitions -= localOptimRepetitionsIncrement;
				if (currentRepetitions < 0) {
					currentRepetitions = 0;
				}
			}
			localOptimumRepetitions.put(op, currentRepetitions);
		}
		localOptimRepetitionsIncrement = 0;
	}
	
	/**
	 * Computes the average execution times from two iterations
	 * of each heuristic.
	 */
	private void computePreliminaryExecutionTimes() {
		if (preliminaryExecutionTimes == null) {
			preliminaryExecutionTimes = new TreeMap<Integer, Double>();
		}
		double cumulatedTime = 0.;
		double nbExec = 0.;
		Double execTime = getAverageExecutionTime(-1, 2);
		cumulatedTime += execTime;
		nbExec += 2.;
		preliminaryExecutionTimes.put(-1, execTime);
		
		for (int op:mutationHeuristicIndexes) {
			execTime = getAverageExecutionTime(op, 2);
			cumulatedTime += execTime;
			nbExec += 2.;
			preliminaryExecutionTimes.put(op, execTime);
		}
		for (int op:crossoverHeuristicIndexes) {
			execTime = getAverageExecutionTime(op, 2);
			cumulatedTime += execTime;
			nbExec += 2.;
			preliminaryExecutionTimes.put(op, execTime);
		}
		for (int op:ruinRecreateHeuristicIndexes) {
			execTime = getAverageExecutionTime(op, 2);
			cumulatedTime += execTime;
			nbExec += 2.;
			preliminaryExecutionTimes.put(op, execTime);
		}
		for (int op:otherHeuristicIndexes) {
			execTime = getAverageExecutionTime(op, 2);
			cumulatedTime += execTime;
			nbExec += 2.;
			preliminaryExecutionTimes.put(op, execTime);
		}
		for (int op:localSearchHeuristicIndexes) {
			execTime = getAverageExecutionTime(op, 2);
			cumulatedTime += execTime;
			nbExec += 2.;
			preliminaryExecutionTimes.put(op, execTime);
		}
		// Compute average operator iterations
		if (cumulatedTime == 0.) {
			averageOperatorIterations = 
				((double)getTimeLimit())/(1./nbExec);
		} else {
			averageOperatorIterations = 
				((double)getTimeLimit())/(cumulatedTime/nbExec);
			
		}
	}
	

	/**
	 * Returns the average execution time of the operator in milliseconds.
	 */
	private double getAverageExecutionTime(
			int operatorIdx, int nbGeneration) {
		
		long cumulatedTime = 0;
		long startTime;
		long endTime;
		
		if (operatorIdx == -1) {
			// Execution time of generation
			for (int i=0; i<nbGeneration && !hasTimeExpired(); i++) {
				startTime = getElapsedTime();
				problem.initialiseSolution(temporarySolutionIndex1);
				endTime = getElapsedTime();
				cumulatedTime += endTime-startTime;
				solutionValues.set(
						temporarySolutionIndex1,
						problem.getFunctionValue(temporarySolutionIndex1));
				insertSolutionInPop(temporarySolutionIndex1);
			}
		} else if (heuristicTypes.get(operatorIdx) == HeuristicType.CROSSOVER) {
			// Execution time for crossover
			for (int i=0; i<nbGeneration && !hasTimeExpired(); i++) {
				// generate two new solutions
				problem.initialiseSolution(temporarySolutionIndex1);
				problem.initialiseSolution(temporarySolutionIndex2);
				startTime = getElapsedTime();
				problem.applyHeuristic(operatorIdx,
						temporarySolutionIndex1, temporarySolutionIndex2,
						temporarySolutionIndex1);
				endTime = getElapsedTime();
				cumulatedTime += endTime-startTime;
				solutionValues.set(
						temporarySolutionIndex1,
						problem.getFunctionValue(temporarySolutionIndex1));
				solutionValues.set(
						temporarySolutionIndex2,
						problem.getFunctionValue(temporarySolutionIndex2));
				insertSolutionInPop(temporarySolutionIndex1);
				insertSolutionInPop(temporarySolutionIndex2);
			}
		} else {
			// Execution time for standard operators
			for (int i=0; i<nbGeneration && !hasTimeExpired(); i++) {
				problem.initialiseSolution(temporarySolutionIndex1);
				startTime = getElapsedTime();
				problem.applyHeuristic(operatorIdx,
						temporarySolutionIndex1,
						temporarySolutionIndex1);
				endTime = getElapsedTime();
				cumulatedTime += endTime-startTime;
				solutionValues.set(
						temporarySolutionIndex1,
						problem.getFunctionValue(temporarySolutionIndex1));
				insertSolutionInPop(temporarySolutionIndex1);
			}
		}
		
		return ((double)cumulatedTime)/((double)nbGeneration);
	}
	
	/**
	 * Computes the local search failure rate.
	 */
	private void computeLSFailureRate() {
		
		ArrayList<Double> vndResultingValues = new ArrayList<Double>();
		ArrayList<Double> oneRowResultingValues = new ArrayList<Double>();
		ArrayList<Integer> lsIterations = new ArrayList<Integer>();
		ArrayList<Double> vndTimes = new ArrayList<Double>();
		double startTime;
		double endTime;
		
		// LS operators
		ArrayList<Integer> lsOperatorsIndexes = new ArrayList<Integer>(
				localSearchHeuristicIndexes);
		
		for (int i=0; i<5; i++) {
			// Randomly select a solution
			problem.initialiseSolution(temporarySolutionIndex1);
			solutionValues.set(temporarySolutionIndex1,
					problem.getFunctionValue(temporarySolutionIndex1));
			// Apply VNS with random ordered LS operators
			Collections.shuffle(lsOperatorsIndexes, rng);
			startTime = getElapsedTime();
			lsIterations.add(variableNeighborhoodDescent(
					temporarySolutionIndex1,
					temporarySolutionIndex1,
					temporarySolutionIndex2,
					lsOperatorsIndexes,
					null,
					(long) (relativeVNDTimeLimit*( (double)(getTimeLimit()) ))
			));
			endTime = getElapsedTime();
			// Get resulting solution value
			vndResultingValues.add(solutionValues.get(temporarySolutionIndex1));
			insertSolutionInPop(temporarySolutionIndex1);
			// Record VND time
			vndTimes.add(endTime-startTime);
		}
		
		// Compute average number of iterations
		averageVndIterations = 0.;
		for (int nbIt:lsIterations) {
			averageVndIterations += (double)nbIt;
		}
		averageVndIterations /= ((double)lsIterations.size());
		
		// Compute average VND time
		averageVndTime = 0.;
		for (double time:vndTimes) {
			averageVndTime += time;
		}
		averageVndTime /= ((double)vndTimes.size());
		
		// Compute one row results
		for (int i=0; i<5; i++) {
			// Random LS operators
			Collections.shuffle(lsOperatorsIndexes, rng);
			startTime = getElapsedTime();
			oneRowResultingValues.add(Double.MAX_VALUE);
			while(getElapsedTime()-startTime<vndTimes.get(i).doubleValue()) {
				// Generate a solution
				problem.initialiseSolution(temporarySolutionIndex1);
				solutionValues.set(temporarySolutionIndex1,
						problem.getFunctionValue(temporarySolutionIndex1));
				// Apply one row LS
				for (int opIdx: lsOperatorsIndexes) {
					problem.applyHeuristic(
							opIdx,
							temporarySolutionIndex1, temporarySolutionIndex1);
					solutionValues.set(temporarySolutionIndex1,
							problem.getFunctionValue(temporarySolutionIndex1));
				}
				// Insert in population
				insertSolutionInPop(temporarySolutionIndex1);
				// Record resulting solution value
				if (solutionValues.get(temporarySolutionIndex1).doubleValue()
						< oneRowResultingValues.get(i).doubleValue()) {
					oneRowResultingValues.set(i, solutionValues.get(temporarySolutionIndex1).doubleValue());
				}
			}
		}
		
		// Compute one row failure rate
		vndVsOneRowFailureRate = 0.;
		for (int i=0; i<vndResultingValues.size(); i++) {
			for (int j=0; j<oneRowResultingValues.size(); j++) {
				if (vndResultingValues.get(i).doubleValue() >
				oneRowResultingValues.get(j).doubleValue()) {
					vndVsOneRowFailureRate += 1.;
				}
			}
		}
		vndVsOneRowFailureRate = vndVsOneRowFailureRate/
		((double)(vndResultingValues.size()*oneRowResultingValues.size()));
	}
	

	/**
	 * Insert a solution at the specified index in the population of solutions.
	 * Solution is inserted if it is better than a previous one and cost is not
	 * equal to a previous cost. The worst solution is replaced by the inserted
	 * one. Returns true if the solution is inserted in the population, false 
	 * otherwise.
	 */
	private boolean insertSolutionInPop(int toInsertIdx) {
		int insertionPos = -1;
		for (int i=startPopIndex; i<=endPopIndex; i++) {
			if (solutionValues.get(i).doubleValue() 
					== solutionValues.get(toInsertIdx).doubleValue()) {
				insertionPos = -1;
				break;
			} else if (solutionValues.get(i).doubleValue()
					> solutionValues.get(toInsertIdx).doubleValue()) {
				if (insertionPos == -1) {
					insertionPos = i;
				} else {
					if (solutionValues.get(i).doubleValue()
							> solutionValues.get(insertionPos).doubleValue()) {
						insertionPos = i;
					}
				}
			}
		}
		if (insertionPos != -1) {
			problem.copySolution(toInsertIdx, insertionPos);
			solutionValues.set(insertionPos, solutionValues.get(toInsertIdx));
			hasTimeExpired();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Apply a variable neighborhood descent.
	 */
	private int variableNeighborhoodDescent(
			int initialSolutionIdx,
			int resultingSolutionIdx,
			int temporarySolutionIdx,
			ArrayList<Integer> localSearchOperators,
			ArrayList<Double> depthsOfSearch,
			long maxTime
	) {
		if (localSearchOperators == null || 
				localSearchOperators.size()==0) {
			problem.copySolution(initialSolutionIdx,
					resultingSolutionIdx);
			return 0;
		}
		
		long startTime = getElapsedTime();
		if (maxTime <= 0)
			maxTime = Long.MAX_VALUE;
		int nbIterations = 0;
		int neighborhoodIdx = 0;
		int repetition = 0;
		int maxRepetition = localOptimumRepetitions.get(
			localSearchOperators.get(neighborhoodIdx));
		
		// Copy the initial solution in final position
		problem.copySolution(initialSolutionIdx,
				resultingSolutionIdx);
		solutionValues.set(resultingSolutionIdx, 
				solutionValues.get(initialSolutionIdx));
		
	
		// Local descent with systematic move
		while (neighborhoodIdx < localSearchOperators.size() &&
				!hasTimeExpired() &&
				getElapsedTime()-startTime < maxTime) {
			// Set depth of search
			if (depthsOfSearch != null)
				problem.setDepthOfSearch(
						depthsOfSearch.get(neighborhoodIdx));
			// Apply operator
			problem.applyHeuristic(
					localSearchOperators.get(neighborhoodIdx),
					resultingSolutionIdx, temporarySolutionIdx);
			solutionValues.set(
					temporarySolutionIdx,
					problem.getFunctionValue(temporarySolutionIdx));
			// Verify result
			if (solutionValues.get(temporarySolutionIdx).doubleValue() < 
					solutionValues.get(resultingSolutionIdx).doubleValue()) {
				nbIterations++;
				problem.copySolution(
						temporarySolutionIdx, resultingSolutionIdx);
				solutionValues.set(
						resultingSolutionIdx,
						solutionValues.get(temporarySolutionIdx));
				// If find a better solution at the end of repetitions
				if (repetition > 0 && repetition >= maxRepetition - 
						localOptimRepetitionsIncrement &&
						maxRepetition < localOptimMaxRepetitions) {
					// Increase max repetition
					localOptimumRepetitions.put(
							localSearchOperators.get(neighborhoodIdx),
							localOptimumRepetitions.get(
							localSearchOperators.get(neighborhoodIdx)).intValue()+
							localOptimRepetitionsIncrement);
				}
				neighborhoodIdx = 0;
				maxRepetition = localOptimumRepetitions.get(
						localSearchOperators.get(neighborhoodIdx));						
				repetition = 0;
			} else {
				if (repetition >= maxRepetition) {
					neighborhoodIdx++;
					if (neighborhoodIdx < localSearchOperators.size()) {
						repetition = 0;
						maxRepetition = localOptimumRepetitions.get(
								localSearchOperators.get(neighborhoodIdx));
					}
				} else {
					repetition++;
				}
			}
		}
		
		// Return the number of iteration (improvement)
		return nbIterations;
	}
	

	/**
	 * Returns the list of LS operators ordered by average computation times.
	 */
	private ArrayList<Integer> getOrderedLSOperatorIndexes() {
		ArrayList<Integer> orderedLSOperatorIndexes = new ArrayList<Integer>();
		orderedLSOperatorIndexes.addAll(localSearchHeuristicIndexes);
		ArrayList<Double> orderedExecTimes = new ArrayList<Double>();
		if (this.preliminaryExecutionTimes != null) {
			for (int op:orderedLSOperatorIndexes) {
				orderedExecTimes.add(preliminaryExecutionTimes.get(op));
			}
		} else {
			return orderedLSOperatorIndexes;
		}
		
		int minIdx;
		int tempIdx;
		double tempValue;
		for (int i=0; i<orderedExecTimes.size()-1; i++) {
			minIdx = i;
			for (int j=i+1; j<orderedExecTimes.size(); j++) {
				if (orderedExecTimes.get(j).doubleValue()
						< orderedExecTimes.get(minIdx).doubleValue()) {
					minIdx = j;
				}
			}
			tempValue = orderedExecTimes.get(i);
			orderedExecTimes.set(i, orderedExecTimes.get(minIdx));
			orderedExecTimes.set(minIdx, tempValue);
			tempIdx = orderedLSOperatorIndexes.get(i);
			orderedLSOperatorIndexes.set(i, orderedLSOperatorIndexes.get(minIdx));
			orderedLSOperatorIndexes.set(minIdx, tempIdx);
		}
		return orderedLSOperatorIndexes;
	}
	
	/**
	 * Intensify the best solution in the population.
	 */
	private void intensifyBestSolution() {
		// Get best solution index
		int bestSolutionIndex = startPopIndex;
		for (int i=startPopIndex+1; i<=endPopIndex; i++) {
			if (solutionValues.get(i).doubleValue()
					< solutionValues.get(bestSolutionIndex).doubleValue()) {
				bestSolutionIndex = i;
			}
		}
		// Apply LS operators
		problem.setDepthOfSearch(1.);
		problem.copySolution(bestSolutionIndex, temporarySolutionIndex1);
		solutionValues.set(temporarySolutionIndex1,
				solutionValues.get(bestSolutionIndex));
		for (int opIdx: localSearchHeuristicIndexes) {
			problem.applyHeuristic(
					opIdx,
					temporarySolutionIndex1, temporarySolutionIndex1);
			if (hasTimeExpired())
				break;
		}
		solutionValues.set(temporarySolutionIndex1,
				problem.getFunctionValue(temporarySolutionIndex1));
		// Insert in population
		insertSolutionInPop(temporarySolutionIndex1);
	}
	
	/**
	 * Returns the relative elapsed time.
	 */
	private double getRelativeElapsedTime() {
		double timeLimit = (double) this.getTimeLimit();
		
		if (timeLimit > 0)
			return ((double)this.getElapsedTime())/timeLimit;
		else
			return 0.d;
	}
	
	/**
	 * Generate the initial population of search pattern.
	 */
	private void generateSearchPatterns() {
		
		searchPatternPopulation = new LinkedList<HeuristicSearchPattern>();
		int patternPopulationSize = heuristicTypes.size();
		ArrayList<Integer> orderedLSOperators = getOrderedLSOperatorIndexes();
		
		for (int i=0; i<patternPopulationSize; i++) {
			HeuristicSearchPattern currentPattern = new HeuristicSearchPattern();
			// Perturbation phase
			if (perturbationHeuristicIndexes.size()>0) {
				currentPattern.perturbationPhase.add(perturbationHeuristicIndexes.get(
						i%perturbationHeuristicIndexes.size() ));
				currentPattern.intensitiesOfMutation.add(rng.nextDouble());
				if (rng.nextBoolean() && perturbationHeuristicIndexes.size()>1) {
					int additionalPerturbationOp = perturbationHeuristicIndexes.get(
							rng.nextInt(perturbationHeuristicIndexes.size()));
					int j=0;
					while(j<5 && (heuristicTypes.get(additionalPerturbationOp)==heuristicTypes.get(
							currentPattern.perturbationPhase.get(0)))) {
						additionalPerturbationOp = perturbationHeuristicIndexes.get(
								rng.nextInt(perturbationHeuristicIndexes.size()));
						j++;
					}
					// Add second perturbation operator
					if (heuristicTypes.get(currentPattern.perturbationPhase.get(0))
							==HeuristicType.CROSSOVER) {
						// Crossover in first position
						currentPattern.perturbationPhase.add(additionalPerturbationOp);
						currentPattern.intensitiesOfMutation.add(rng.nextDouble());
					} else {
						currentPattern.perturbationPhase.add(0,additionalPerturbationOp);
						currentPattern.intensitiesOfMutation.add(0,rng.nextDouble());
					}
				}
			}
			// Local search phase
			if (orderedLsOperators && orderedLSOperators != null &&
					orderedLSOperators.size()>0) {
				currentPattern.localSearchPhase.addAll(orderedLSOperators);
				for (int j=0; j<currentPattern.localSearchPhase.size(); j++) {
					currentPattern.depthsOfSearch.add(1.);
				}
			} else {
				currentPattern.localSearchPhase.addAll(localSearchHeuristicIndexes);
				Collections.shuffle(currentPattern.localSearchPhase, rng);
				for (int j=0; j<currentPattern.localSearchPhase.size(); j++) {
					currentPattern.depthsOfSearch.add(rng.nextDouble());
				}				
			}
			// Add into the population
			searchPatternPopulation.add(currentPattern);
		}
	}
	
	/**
	 * Generates mutated search patterns.
	 */
	private void generateMutatedPatterns() {
		int popSize = searchPatternPopulation.size();
		for (int i=0; i<popSize; i++) {
			searchPatternPopulation.add(searchPatternPopulation.get(i).mutate());
		}
	}
	
	/**
	 * Prints the values of solutions in the population on the
	 * standard output.
	 */
	protected void printSolutionPopulation(){
		System.out.print("[ ");
		for (int i=0; i<solutionValues.size(); i++) {
			System.out.print(solutionValues.get(i));
			if (i==endPopIndex)
				System.out.print(" | ");
			else if (i==solutionValues.size()-1)
				System.out.print(" ]");
			else
				System.out.print(" , ");
		}
		System.out.print(" ("+problem.getBestSolutionValue()+")\n");
	}
	
	/**
	 * Prints a description of the search strategies on the
	 * standard output.
	 */
	protected void printStrategyPopulation() {
		System.out.println("[");
		for (int i=0; i<searchPatternPopulation.size(); i++) {
			System.out.println(searchPatternPopulation.get(i).toString());
		}
		System.out.println("]");
	}
	
	/**
	 * Constructor with seed for random number generation.
	 */
	public EPH(long seed) {
		super(seed);
	}
	
	@Override
	protected void solve(ProblemDomain p) {
		
		try {

			//================================================
			// Initialize hyper-heuristic
			
			problem = p;
			populateHeuristicIndexes();
			setInitialPopulationSize(50);
			problem.initialiseSolution(startPopIndex); // Just for HyFlex bug.
			initLocalOptimumRepetitions();
			computePreliminaryExecutionTimes();
			completePopulation();
			
			
			//================================================
			// Parameter setting

			additionalIntensification = true;
			nbTournaments = 3;
			
			if (averageOperatorIterations < 1500.) {
				// Strategy for limited iterations
				localSearchType = LOCAL_SEARCH_TYPE.ONE_ROW;
				reducePopulationSize(2);
				orderedLsOperators = true;
				strategyDesc = "Very limited iterations, pop. 2, one row LS," +
						" additional intensification, 3 rounds, ordered LS.";
			} else if (averageOperatorIterations < 9000.) {
				// Strategy for limited iterations
				localSearchType = LOCAL_SEARCH_TYPE.ONE_ROW;
				reducePopulationSize(2);
				orderedLsOperators = false;
				strategyDesc = "Limited iterations, pop. 2, one row LS," +
						" additional intensification, 3 rounds.";
			} else {
				// Compute VND performance
				computeLSFailureRate();
				if (vndVsOneRowFailureRate > 0.45) {
					// High VND failure rate
					if (averageVndIterations/((double)localSearchHeuristicIndexes.size()) < 5.0) {
						// Small number of iterations in VND
						reducePopulationSize(35);
						localSearchType = LOCAL_SEARCH_TYPE.ONE_ROW;
						orderedLsOperators = false;
						strategyDesc = "High VND failure rate and low iterations in VND, " +
								"one-row LS, pop. 35, additional intensification, 3 rounds.";
						
					} else {
						// High number of iterations in VND
						localSearchType = LOCAL_SEARCH_TYPE.ONE_ROW;
						orderedLsOperators = false;
						strategyDesc = "High VND failure rate and high iterations in VND, " +
								"one-row LS, pop. 50, additional intensification, 3 rounds.";
					}
				} else {
					// Low VND failure rate
					localSearchType = LOCAL_SEARCH_TYPE.VND;
					reducePopulationSize(2);
					orderedLsOperators = false;
					strategyDesc = "Low VND failure rate, VND LS, pop. 2, " +
							"additional intensification, 3 rounds.";
				}
			}
			
		} catch (Exception e) {
			System.err.println("Error during profiling phase. Apply emergency settings.");
			e.printStackTrace();
			startPopIndex = 0;
			endPopIndex = 1;
			temporarySolutionIndex1 = 2;
			temporarySolutionIndex2 = 3;
			temporarySolutionIndex3 = 4;
			solutionValues = new ArrayList<Double>();
			for (int i=0; i<5; i++) {
				solutionValues.add(Double.MAX_VALUE);
			}
			problem.initialiseSolution(0);
			problem.initialiseSolution(1);
			solutionValues.set(0, problem.getFunctionValue(0));
			solutionValues.set(1, problem.getFunctionValue(1));
			localSearchType = LOCAL_SEARCH_TYPE.ONE_ROW;
			orderedLsOperators = false;
			strategyDesc = "Emergency parameters.";
		}
		
		//================================================
		// Co-evolution
		
		// Initialize population of search pattern
		generateSearchPatterns();
				
		// Main loop
		int patternGeneration = 0;
		int solutionIdx = 0;
		int initialSolutionIdx1;
		int initialSolutionIdx2;
		int nbVictories1;
		int nbVictories2;
		boolean resultingSolution1Inserted;
		boolean resultingSolution2Inserted;
		double initialSolutionValue;
		
		
		while (!hasTimeExpired()) {
			
			patternGeneration++;
			
			// Mutate ILS individuals
			generateMutatedPatterns();
			Collections.shuffle(searchPatternPopulation, rng);
			
			// New population of strategies
			LinkedList<HeuristicSearchPattern> newPopulation = new LinkedList<HeuristicSearchPattern>();
			
			// Tournament selection with co-evolution
			while (searchPatternPopulation.size() >= 2 && !hasTimeExpired()) {
				
				// Select competing patterns
				HeuristicSearchPattern competingPattern1 = searchPatternPopulation.remove();
				HeuristicSearchPattern competingPattern2 = searchPatternPopulation.remove();
				
				nbVictories1 = 0;
				nbVictories2 = 0;
				
				for (int t=0; t<nbTournaments; t++) {
					// Select solutions
					initialSolutionIdx1 = (solutionIdx%(endPopIndex+1))+startPopIndex;
					initialSolutionValue = solutionValues.get(initialSolutionIdx1);
					initialSolutionIdx2 = ((solutionIdx+1)%(endPopIndex+1))+startPopIndex;
					solutionIdx++;
					
					// Apply patterns
					try {
						competingPattern1.apply(initialSolutionIdx1, initialSolutionIdx2,
								temporarySolutionIndex1, temporarySolutionIndex3);
						competingPattern2.apply(initialSolutionIdx1, initialSolutionIdx2,
								temporarySolutionIndex2, temporarySolutionIndex3);						
					} catch (Exception e) {
						System.err.println("Error during sequence application.");
						e.printStackTrace();
						problem.copySolution(initialSolutionIdx1, temporarySolutionIndex1);
						problem.copySolution(initialSolutionIdx1, temporarySolutionIndex2);
						solutionValues.set(temporarySolutionIndex1,
								solutionValues.get(initialSolutionIdx1).doubleValue());
						solutionValues.set(temporarySolutionIndex2,
								solutionValues.get(initialSolutionIdx2).doubleValue());
					}
					
					// Insert resulting solution in population
					resultingSolution1Inserted = insertSolutionInPop(temporarySolutionIndex1);
					if (resultingSolution1Inserted) {
						competingPattern1.lastPopulationUpdate = patternGeneration;
					}
					resultingSolution2Inserted = insertSolutionInPop(temporarySolutionIndex2);
					if (resultingSolution2Inserted) {
						competingPattern2.lastPopulationUpdate = patternGeneration;
					}
					
					// Selection based on insertion, then solution value
					if (resultingSolution1Inserted == resultingSolution2Inserted) {
						if (solutionValues.get(temporarySolutionIndex1).doubleValue() 
								< solutionValues.get(temporarySolutionIndex2).doubleValue()) {
							if (solutionValues.get(temporarySolutionIndex1).doubleValue() 
									!= initialSolutionValue) {
								nbVictories1++;
							} else {
								nbVictories2++;
							}
						} else if (solutionValues.get(temporarySolutionIndex1).doubleValue() 
								> solutionValues.get(temporarySolutionIndex2).doubleValue()) {
							if (solutionValues.get(temporarySolutionIndex2).doubleValue() 
									!= initialSolutionValue) {
								nbVictories2++;
							} else {
								nbVictories1++;
							}
						}
					} else if (resultingSolution1Inserted) {
						nbVictories1++;
					} else {
						nbVictories2++;
					}
					
				}
				
				// Result of the tournament
				if (nbVictories1 == nbVictories2) {
					if (competingPattern1.lastPopulationUpdate == 
						competingPattern2.lastPopulationUpdate) {
						if (rng.nextBoolean()) {
							newPopulation.add(competingPattern1);
						} else {
							newPopulation.add(competingPattern2);
						}
					} else if (competingPattern1.lastPopulationUpdate > 
						competingPattern2.lastPopulationUpdate){
						newPopulation.add(competingPattern1);
					} else {
						newPopulation.add(competingPattern2);
					}
				} else if (nbVictories1 > nbVictories2){
					newPopulation.add(competingPattern1);
				} else {
					newPopulation.add(competingPattern2);
				}
				
				// Additional intensification at 75% of the time
				if (getRelativeElapsedTime() > 0.75 && additionalIntensification) {
					intensifyBestSolution();
				}
				
				// Disable repetition increment after one generation
				if (localOptimRepetitionsIncrement != 0)
					disableLocalOptimumRepetitionsIncrement();
			}
			// Replace previous pattern population by new one
			searchPatternPopulation = newPopulation;
			
		}
		strategyDesc = strategyDesc+" (gen:"+patternGeneration+").";
		
	}

	@Override
	public String toString() {
		return "EPH by David M.";
	}
	
	/**
	 * Returns the description of the applied strategy parameters.
	 */
	public String getStrategyDescription() {
		return strategyDesc;
	}
	
	/**
	 * Heuristic search pattern.
	 */
	class HeuristicSearchPattern {
		
		// Operators for perturbation and local search phases
		ArrayList<Integer> perturbationPhase;
		ArrayList<Double> intensitiesOfMutation;
		ArrayList<Integer> localSearchPhase;
		ArrayList<Double> depthsOfSearch;
		
		// Last application duration
		long lastDuration;
		
		// Last population update
		int lastPopulationUpdate;
		
		/**
		 * Constructor.
		 */
		public HeuristicSearchPattern() {
			perturbationPhase = new ArrayList<Integer>();
			intensitiesOfMutation = new ArrayList<Double>();
			localSearchPhase = new ArrayList<Integer>();
			depthsOfSearch = new ArrayList<Double>();
			lastDuration = Long.MAX_VALUE;
			lastPopulationUpdate = -1;
		}
		
		/**
		 * Copy the search pattern.
		 */
		public HeuristicSearchPattern(HeuristicSearchPattern searchPattern) {
			perturbationPhase = new ArrayList<Integer>();
			intensitiesOfMutation = new ArrayList<Double>();
			localSearchPhase = new ArrayList<Integer>();
			depthsOfSearch = new ArrayList<Double>();
			lastDuration = Long.MAX_VALUE;
			lastPopulationUpdate = -1;
			for (int p:searchPattern.perturbationPhase) {
				perturbationPhase.add(p);
			}
			for (double i:searchPattern.intensitiesOfMutation) {
				intensitiesOfMutation.add(i);
			}
			for (int l:searchPattern.localSearchPhase) {
				localSearchPhase.add(l);
			}
			for (double d:searchPattern.depthsOfSearch) {
				depthsOfSearch.add(d);
			}
			lastDuration = searchPattern.lastDuration;
			lastPopulationUpdate = searchPattern.lastPopulationUpdate;
		}
		
		/**
		 * Applies one iteration of the search pattern.
		 */
		public void apply(int initialSolutionIdx1, int initialSolutionIdx2,
				int resultingSolutionIdx, int tempSolutionIdx) {
			long startTime = getElapsedTime();
			// Perturbation phase
			if (perturbationPhase.size() > 0) {
				if (heuristicTypes.get(perturbationPhase.get(0))==HeuristicType.CROSSOVER) {
					// Apply a crossover
					problem.applyHeuristic(perturbationPhase.get(0),
							initialSolutionIdx1, initialSolutionIdx2,
							resultingSolutionIdx);
				} else {
					// Apply standard perturbation operator
					problem.setIntensityOfMutation(intensitiesOfMutation.get(0));
					problem.applyHeuristic(perturbationPhase.get(0),
							initialSolutionIdx1,
							resultingSolutionIdx);
				}
				if (perturbationPhase.size() > 1) {
					// Second perturbation operator
					problem.setIntensityOfMutation(intensitiesOfMutation.get(1));
					problem.applyHeuristic(perturbationPhase.get(1),
							resultingSolutionIdx,
							resultingSolutionIdx);
				}
				solutionValues.set(resultingSolutionIdx,
						problem.getFunctionValue(resultingSolutionIdx));
			} else {
				problem.copySolution(initialSolutionIdx1, resultingSolutionIdx);
				solutionValues.set(resultingSolutionIdx,
						solutionValues.get(initialSolutionIdx1));
			}
			// Local search phase
			if (localSearchType == LOCAL_SEARCH_TYPE.VND) {
				// Variable neighborhood descent
				variableNeighborhoodDescent(
						resultingSolutionIdx,
						resultingSolutionIdx,
						tempSolutionIdx,
						localSearchPhase,
						depthsOfSearch,
						Long.MAX_VALUE);
			} else {
				// One row local search
				for (int i=0; i<localSearchPhase.size() && !hasTimeExpired(); i++) {
					problem.setDepthOfSearch(depthsOfSearch.get(i));
					problem.applyHeuristic(
							localSearchPhase.get(i),
							resultingSolutionIdx, resultingSolutionIdx);
				}
				solutionValues.set(resultingSolutionIdx,
						problem.getFunctionValue(resultingSolutionIdx));
			}
			// Record duration
			lastDuration = getElapsedTime()-startTime;
		}
		
		/**
		 * Generate a new pattern by mutation.
		 */
		public HeuristicSearchPattern mutate() {
			HeuristicSearchPattern mutated = new HeuristicSearchPattern(this);
			mutated.lastPopulationUpdate = -1;
			mutated.lastDuration = Long.MAX_VALUE;;
			// Mutation
			double r = rng.nextDouble();
			
			if (r < 0.25 && mutated.perturbationPhase.size() != 0) {
				// Modify perturbation intensity
				mutated.intensitiesOfMutation.clear();
				for (int i=0; i<mutated.perturbationPhase.size(); i++) {
					mutated.intensitiesOfMutation.add(rng.nextDouble());
				}
			} else if (r < 0.5 && mutated.perturbationPhase.size() != 0) {
				// Modify perturbation operator
				if (rng.nextBoolean()) {
					// Add or remove an operator
					if (mutated.perturbationPhase.size()==1) {
						// Additional operator
						int additionalPerturbationOp = perturbationHeuristicIndexes.get(
								rng.nextInt(perturbationHeuristicIndexes.size()));
						int j=0;
						while(j<5 && (heuristicTypes.get(additionalPerturbationOp)==heuristicTypes.get(
								mutated.perturbationPhase.get(0)))) {
							additionalPerturbationOp = perturbationHeuristicIndexes.get(
									rng.nextInt(perturbationHeuristicIndexes.size()));
							j++;
						}
						// Add second perturbation operator
						if (heuristicTypes.get(mutated.perturbationPhase.get(0))
								==HeuristicType.CROSSOVER) {
							// Crossover in first position
							mutated.perturbationPhase.add(additionalPerturbationOp);
							mutated.intensitiesOfMutation.add(rng.nextDouble());
						} else {
							mutated.perturbationPhase.add(0,additionalPerturbationOp);
							mutated.intensitiesOfMutation.add(0, rng.nextDouble());
						}
					} else {
						// Removal of an operator
						int pos = rng.nextInt(mutated.perturbationPhase.size());
						mutated.perturbationPhase.remove(pos);
						mutated.intensitiesOfMutation.remove(pos);
					}
				} else {
					// Change an existing operator
					// Removal of an operator
					int pos = rng.nextInt(mutated.perturbationPhase.size());
					mutated.perturbationPhase.remove(pos);
					mutated.intensitiesOfMutation.remove(pos);
					if (mutated.perturbationPhase.size()==1) {
						// Additional operator
						int additionalPerturbationOp = perturbationHeuristicIndexes.get(
								rng.nextInt(perturbationHeuristicIndexes.size()));
						int j=0;
						while(j<5 && (heuristicTypes.get(additionalPerturbationOp)==heuristicTypes.get(
								mutated.perturbationPhase.get(0)))) {
							additionalPerturbationOp = perturbationHeuristicIndexes.get(
									rng.nextInt(perturbationHeuristicIndexes.size()));
							j++;
						}
						// Add second perturbation operator
						if (heuristicTypes.get(mutated.perturbationPhase.get(0))
								==HeuristicType.CROSSOVER) {
							// Crossover in first position
							mutated.perturbationPhase.add(additionalPerturbationOp);
							mutated.intensitiesOfMutation.add(rng.nextDouble());
						} else {
							mutated.perturbationPhase.add(0,additionalPerturbationOp);
							mutated.intensitiesOfMutation.add(0, rng.nextDouble());
						}
					} else {
						// Add a new operator
						mutated.perturbationPhase.add(perturbationHeuristicIndexes.get(
								rng.nextInt(perturbationHeuristicIndexes.size())));
						mutated.intensitiesOfMutation.add(0, rng.nextDouble());
					}
				}
			} else if (r < 0.75) {
				// Modify depth of search
				mutated.depthsOfSearch.clear();
				for (int i=0; i<mutated.localSearchPhase.size(); i++) {
					mutated.depthsOfSearch.add(rng.nextDouble());
				}
			} else {
				// Modify LS operator order
				for (int i=0; i<mutated.localSearchPhase.size(); i++) {
					int n = rng.nextInt(mutated.localSearchPhase.size());
					// Swap position I and N
					if (i != n) {
						int t = mutated.localSearchPhase.get(n);
						mutated.localSearchPhase.set(n, mutated.localSearchPhase.get(i));
						mutated.localSearchPhase.set(i, t);
						double d = mutated.depthsOfSearch.get(n);
						mutated.depthsOfSearch.set(n, mutated.depthsOfSearch.get(i));
						mutated.depthsOfSearch.set(i, d);
					}
				}
			}
			return mutated;
		}
		
		/**
		 * Returns a description of the search pattern.
		 */
		public String toString() {
			DecimalFormat df = new DecimalFormat("0.0");
			String desc = "[";
			int opIdx;
			for (int i=0; i<perturbationPhase.size(); i++) {
				opIdx = perturbationPhase.get(i);
				if (heuristicTypes.get(opIdx)==HeuristicType.CROSSOVER) {
					desc += "C"+opIdx+" ("+
					df.format(intensitiesOfMutation.get(i))
					+") ,\t";
				} else if (heuristicTypes.get(opIdx)==HeuristicType.MUTATION) {
					desc += "M"+opIdx+" ("+
					df.format(intensitiesOfMutation.get(i))
					+") ,\t";
				} else if (heuristicTypes.get(opIdx)==HeuristicType.RUIN_RECREATE) {
					desc += "R"+opIdx+" ("+
					df.format(intensitiesOfMutation.get(i))
					+") ,\t";
				} else if (heuristicTypes.get(opIdx)==HeuristicType.OTHER) {
					desc += "O"+opIdx+" ("+
					df.format(intensitiesOfMutation.get(i))
					+") ,\t";
				}
			}
			for (int i=0; i<localSearchPhase.size(); i++) {
				desc += "L"+localSearchPhase.get(i)+" ("+
				df.format(depthsOfSearch.get(i))
				+") ";
				if (i < localSearchPhase.size()-1)
					desc += " ,\t";
				else {
					desc += "]";
				}
			}
			return desc;
		}
	}

}
