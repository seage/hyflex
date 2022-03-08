package travelingSalesmanProblem;


import java.util.Arrays;

import AbstractClasses.ProblemDomain;

public class TSP extends ProblemDomain {

	/* MEMBERS */
	
	public TspInstance instance;
	private TspSolution[] memory = new TspSolution[2];
	private TspSolution bestSoFar;
	//public TspInstance probInstance;	
	public TspBasicAlgorithms algorithms;
	
	public TSP(long seed) {
		super(seed);
	}

	@Override
	public double applyHeuristic(int llhID, int solutionSourceIndex,
			int solutionDestinationIndex) {
		
		long startTime = System.currentTimeMillis();
		
		this.heuristicCallRecord[llhID]++;
		switch (llhID) {
		case 0:
			randomReinsertion(solutionSourceIndex, solutionDestinationIndex);
			break;
		case 1:
			swapTwo(solutionSourceIndex, solutionDestinationIndex);
			break;
		case 2:
			shuffle(solutionSourceIndex, solutionDestinationIndex);
			break;
		case 3:
			shuffleSubSequence(solutionSourceIndex, solutionDestinationIndex);
			break;
		case 4:
			nOptMove(solutionSourceIndex, solutionDestinationIndex);
			break;
		case 5:
			iteratedGreedy(solutionSourceIndex, solutionDestinationIndex);
			break;
		case 6:
			twoOptLocalSearch(solutionSourceIndex, solutionDestinationIndex);
			break;
		case 7:
			bestImpTwoOptLocalSearch(solutionSourceIndex, solutionDestinationIndex);
			break;
		case 8:
			threeOptLocalSearch(solutionSourceIndex, solutionDestinationIndex);
			break;
		case 9:
			ox(solutionSourceIndex, solutionSourceIndex, solutionDestinationIndex);
			break;
		case 10:
			pmx(solutionSourceIndex, solutionSourceIndex, solutionDestinationIndex);
			break;
		case 11:
			ppx(solutionSourceIndex, solutionSourceIndex, solutionDestinationIndex);
			break;
		case 12:
			oneX(solutionSourceIndex, solutionSourceIndex, solutionDestinationIndex);
			break;
		}
		
		this.heuristicCallTimeRecord[llhID]+= (System.currentTimeMillis() - startTime);
		
		this.verifyBestSolution(memory[solutionDestinationIndex]);
		
		assert algorithms.verifyPermutation(memory[solutionDestinationIndex].permutation, instance.numbCities);
		
		return memory[solutionDestinationIndex].Cost;
	}

	@Override
	public double applyHeuristic(int llhID, int solutionSourceIndex1,
			int solutionSourceIndex2, int solutionDestinationIndex) {
		
		long startTime = System.currentTimeMillis();
		this.heuristicCallRecord[llhID]++;
		switch (llhID) {
		case 0:
			randomReinsertion(solutionSourceIndex1, solutionDestinationIndex);
			break;
		case 1:
			swapTwo(solutionSourceIndex1, solutionDestinationIndex);
			break;
		case 2:
			shuffle(solutionSourceIndex1, solutionDestinationIndex);
			break;
		case 3:
			shuffleSubSequence(solutionSourceIndex1, solutionDestinationIndex);
			break;
		case 4:
			nOptMove(solutionSourceIndex1, solutionDestinationIndex);
			break;
		case 5:
			iteratedGreedy(solutionSourceIndex1, solutionDestinationIndex);
			break;
		case 6:
			twoOptLocalSearch(solutionSourceIndex1, solutionDestinationIndex);
			break;
		case 7:
			bestImpTwoOptLocalSearch(solutionSourceIndex1, solutionDestinationIndex);
			break;
		case 8:
			threeOptLocalSearch(solutionSourceIndex1, solutionDestinationIndex);
			break;
		case 9:
			ox(solutionSourceIndex1, solutionSourceIndex2, solutionDestinationIndex);
			break;
		case 10:
			pmx(solutionSourceIndex1, solutionSourceIndex2, solutionDestinationIndex);
			break;
		case 11:
			ppx(solutionSourceIndex1, solutionSourceIndex2, solutionDestinationIndex);
			break;
		case 12:
			oneX(solutionSourceIndex1, solutionSourceIndex2, solutionDestinationIndex);
			break;
		}
		
		this.heuristicCallTimeRecord[llhID] += (System.currentTimeMillis() - startTime);
		
		this.verifyBestSolution(memory[solutionDestinationIndex]);
		
		assert algorithms.verifyPermutation(memory[solutionDestinationIndex].permutation, instance.numbCities);

		return memory[solutionDestinationIndex].Cost;
	}

	@Override
	public String bestSolutionToString() {
		return bestSoFar.toString();
	}

	@Override
	public boolean compareSolutions(int solutionIndex1, int solutionIndex2) {
		TspSolution s1 = memory[solutionIndex1];
		TspSolution s2 = memory[solutionIndex2];
		for(int i = 0; i < instance.numbCities; i++){
			if(s1.permutation[i] != s2.permutation[i])
				return false;
		}
		return true;
	}

	@Override
	public void copySolution(int solutionSourceIndex,
			int solutionDestinationIndex) {
		memory[solutionDestinationIndex] = memory[solutionSourceIndex].clone();

	}

	@Override
	public double getBestSolutionValue() {
		return bestSoFar.Cost;
	}

	@Override
	public double getFunctionValue(int solutionIndex) {
		return memory[solutionIndex].Cost;
	}

	@Override
	public int[] getHeuristicsOfType(HeuristicType heuristicType) {
		if (heuristicType == ProblemDomain.HeuristicType.MUTATION)
			return new int[] { 0, 1, 2, 3, 4 };
		if (heuristicType == ProblemDomain.HeuristicType.RUIN_RECREATE)
			return new int[] { 5 };
		if (heuristicType == ProblemDomain.HeuristicType.LOCAL_SEARCH)
			return new int[] { 6, 7, 8 };
		if (heuristicType == ProblemDomain.HeuristicType.CROSSOVER)
			return new int[] { 9, 10, 11, 12};
		return null;
	}

	@Override
	public int[] getHeuristicsThatUseDepthOfSearch() {
		return new int[]{6, 7, 8};
	}

	@Override
	public int[] getHeuristicsThatUseIntensityOfMutation() {
		return new int[]{3, 4, 5};
	}

	@Override
	public int getNumberOfHeuristics() {
		return 13;
	}

	@Override
	public int getNumberOfInstances() {
		return 10;
	}

	@Override
	public void initialiseSolution(int i) {
		int startCity = rng.nextInt(instance.numbCities);
		int[] initialSolution = algorithms.greedyHeuristic(startCity);
		double cost = algorithms.computeCost(initialSolution);
		memory[i] = new TspSolution(initialSolution, cost);
		verifyBestSolution(memory[i]);
	}

	@Override
	public void loadInstance(int instanceID) {
		this.instance = new TspInstance(instanceID);
		this.algorithms = new TspBasicAlgorithms(instance);	
	}

	@Override
	public void setMemorySize(int size) {
		TspSolution[] tempMemory = new TspSolution[size];
		if (memory != null) {
			if (tempMemory.length <= memory.length)
				for (int i = 0; i < tempMemory.length; i++)
					tempMemory[i] = memory[i];
			else
				for (int i = 0; i < memory.length; i++)
					tempMemory[i] = memory[i];
		}
		memory = tempMemory;
	}

	@Override
	public String solutionToString(int solutionIndex) {
		return memory[solutionIndex].toString();
	}

	@Override
	public String toString() {
		return this.instance.toString();
	}
	
	/* HEURISTICS */	
	
	// MUTATION HEURISTICS	
	/**
	 * Makes a copy of the solution in sourceIndex, modifies it by removing a
	 * randomly selected element in the permutation and reinserting it in
	 * another randomly selected place. The rest of the cities are shifted as
	 * required. The new solution is placed in memory in targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void randomReinsertion(int sourceIndex, int targetIndex) {
		int[] array = memory[sourceIndex].permutation.clone();
		int i1 = rng.nextInt(array.length);
		int i2;
		
		while ((i2 = rng.nextInt(array.length)) == i1);
		
		//double cost = algorithms.incrementalInsertionCost(array, memory[sourceIndex].Cost, i1, i2);
		
		int[] newArray = new int[array.length];
		newArray[i2] = array[i1];
		if (i1 < i2) {
			for (int i = 0, count = 0; count < array.length; i++, count++) {
				if (i == i1)
					count++;
				if (i == i2) {
					count--;
					continue;
				}
				newArray[i] = array[count];
			}
		} else {
			for (int i = 0, count = 0; count < array.length; i++, count++) {
				if (i == i2) {
					count--;
					continue;
				}
				newArray[i] = array[count];
				if (i == i1)
					count++;
			}
		}
		double cost = algorithms.computeCost(newArray);
		memory[targetIndex] = new TspSolution(newArray, cost);
	}

	/**
	 * Makes a copy of the solution in sourceIndex, modifies it by swapping two
	 * of its elements (randomly selected). The new solution is placed in memory
	 * in targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void swapTwo(int sourceIndex, int targetIndex) {
		int[] array = memory[sourceIndex].permutation.clone();
		int i1 = rng.nextInt(array.length);
		int i2 = rng.nextInt(array.length);		
		array[i1] = memory[sourceIndex].permutation[i2];
		array[i2] = memory[sourceIndex].permutation[i1];
		double cost = algorithms.computeCost(array);		
		memory[targetIndex] = new TspSolution(array, cost);
	}

	/**
	 * Creates a new random solution and places it in memory in targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void shuffle(int sourceIndex, int targetIndex) {
		int[] array = memory[sourceIndex].permutation.clone();
		algorithms.shufflePermutation(array, rng);
		double cost = algorithms.computeCost(array);
		memory[targetIndex] = new TspSolution(array, cost);
	}

	/**
	 * Makes a copy of the solution in sourceIndex, modifies it by shuffling the
	 * order of a number of randomly selected elements in the permutation. The
	 * new solution is placed in memory in targetIndex. The number of elements
	 * to shuffle is dictated by the intensity of mutation parameter.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void shuffleSubSequence(int sourceIndex, int targetIndex) {
		int numbToShuffle = 2 + (int) (this.getIntensityOfMutation() * (instance.numbCities - 2));
		int[] AvailableIndices = new int[instance.numbCities];
		Arrays.fill(AvailableIndices, 1);
		int count = instance.numbCities;
		int[] IndicesToShuffle = new int[numbToShuffle];
		int[] jobIndices = new int[numbToShuffle];
		int[] permutation = memory[sourceIndex].permutation.clone();
		int randomInteger;
		int count2 = 0;
		for (int i = 0; i < numbToShuffle; i++) {
			randomInteger = rng.nextInt(count);
			count--;
			count2 = 0;
			for (int j = 0; j < permutation.length; j++) {
				if (AvailableIndices[j] == 1) {
					if (count2 == randomInteger) {
						IndicesToShuffle[i] = j;
						jobIndices[i] = permutation[j];
						AvailableIndices[j] = -1;
						break;
					}
					count2++;
				}
			}
		}
		algorithms.shufflePermutation(IndicesToShuffle, rng);
		for (int i = 0; i < numbToShuffle; i++) {
			permutation[IndicesToShuffle[i]] = jobIndices[i];
		}
		double cost = algorithms.computeCost(permutation);
		memory[targetIndex] = new TspSolution(permutation, cost);
	}

	/**
	 * N-Opt move, selects N arches and substitutes them with new randomly selected ones.
	 * The value of N depends on the intensityOfMutation parameter:
	 * N = 2 if        intensityOfMutation <= 0.25
	 * N = 3 if 0.25 < intensityOfMutation <= 0.50
	 * N = 4 if 0.50 < intensityOfMutation <= 0.75
	 * N = 5 if 0.75 < intensityOfMutation <= 1.00
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void nOptMove(int sourceIndex, int targetIndex){
		
		int N = 2;
		if(this.intensityOfMutation >= 0.25)
			N = 3;
		if(this.intensityOfMutation >= 0.5)
			N = 4;
		if(this.intensityOfMutation >= 0.75)
			N = 5;
		
		int[] tour = memory[sourceIndex].permutation;		
		int[] newTour = tour.clone();
		int city1, city2;
		for(int i = 0; i < N-1; i++){
			city1 = rng.nextInt(instance.numbCities);
			while( (city2=rng.nextInt(instance.numbCities)) == city1);		
			newTour = TspBasicAlgorithms.flip(newTour, city1, city2);
		}
		double cost = algorithms.computeCost(newTour);
		memory[targetIndex] = new TspSolution(newTour, cost);
		return;
	}
	
	
	// RUIN RECREATE HEURISTICS
	/**
	 * Makes a copy of the solution in sourceIndex. This is improved using an
	 * iterated procedure in which a number of elements of the permutation are
	 * removed and placed back using a re-insertion procedure. The new
	 * solution is placed in memory in targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void iteratedGreedy(int sourceIndex, int targetIndex) {
		
		int numbCities = instance.numbCities;
		
		int numbRemove = ((int) (this.getIntensityOfMutation() * (numbCities - 1))) + 1;
		int[] partialTour = new int[numbCities - numbRemove];
		int[] citiesToInsert = new int[numbRemove];
		int[] list = memory[sourceIndex].permutation.clone();
		int id;
		for (int i = 0; i < numbRemove; i++) {
			id = rng.nextInt(numbCities);
			while (list[id] < 0)
				id = rng.nextInt(numbCities);
			citiesToInsert[i] = list[id];
			list[id] = -1;
		}
		for (int i = 0, j = 0; i < numbCities; i++, j++) {
			try {
				if (list[i] > -1) {
					partialTour[j] = list[i];
				} else
					j--;
			} catch (Exception ex) {
				System.out.println(" " + partialTour.length + " "
						+ list.length + " j " + j + " i " + i);
				System.exit(0);
			}

		}
		double cost = algorithms.computeCost(partialTour);
		int[] permutation = algorithms.greedyInsertion(partialTour, citiesToInsert, cost);
		cost = algorithms.computeCost(permutation);
		
		memory[targetIndex] = new TspSolution(permutation, cost);
	}


	// LOCAL SEARCH HEURISTICS
	/**
	 * two-opt local search that accepts first improvements.
	 * The method is optimised by using specialised data structures to
	 * perform the flips. 
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void twoOptLocalSearch(int sourceIndex, int targetIndex) {
		int maxiterations = 10;
		if (this.depthOfSearch < 0.2) {
			maxiterations = 10;
		} else if (this.depthOfSearch < 0.4) {
			maxiterations = 20;
		} else if (this.depthOfSearch < 0.6) {
			maxiterations = 30;
		} else if (this.depthOfSearch < 0.8) {
			maxiterations = 40;
		} else {
			maxiterations = 50;
		}
		int[] improvedTour = algorithms.twoOptFirstImprovement(memory[sourceIndex].permutation, instance, maxiterations);
		double cost = algorithms.computeCost(improvedTour);
		memory[targetIndex] = new TspSolution(improvedTour, cost);
	}

	/**
	 * best improvement two-opt local search
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void bestImpTwoOptLocalSearch(int sourceIndex, int targetIndex) {
		int maxiterations = 10;
		if (this.depthOfSearch < 0.2) {
			maxiterations = 10;
		} else if (this.depthOfSearch < 0.4) {
			maxiterations = 20;
		} else if (this.depthOfSearch < 0.6) {
			maxiterations = 30;
		} else if (this.depthOfSearch < 0.8) {
			maxiterations = 40;
		} else {
			maxiterations = 50;
		}
		int[] improvedTour = algorithms.twoOptBestImprovement(memory[sourceIndex].permutation, instance, maxiterations);
		double cost = algorithms.computeCost(improvedTour);
		memory[targetIndex] = new TspSolution(improvedTour, cost);
	}

	/**
	 * three-opt local search (accepts first improvements)
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void threeOptLocalSearch(int sourceIndex, int targetIndex) {
		int maxiterations = 10;
		if (this.depthOfSearch < 0.2) {
			maxiterations = 10;
		} else if (this.depthOfSearch < 0.4) {
			maxiterations = 20;
		} else if (this.depthOfSearch < 0.6) {
			maxiterations = 30;
		} else if (this.depthOfSearch < 0.8) {
			maxiterations = 40;
		} else {
			maxiterations = 50;
		}
		int[] improvedTour = algorithms.threeOpt(memory[sourceIndex].permutation, instance, maxiterations);
		double cost = algorithms.computeCost(improvedTour);
		memory[targetIndex] = new TspSolution(improvedTour, cost);
	}


	// CROSSOVER HEURISTICS
	/**
	 * Order Crossover
	 * 
	 * @param sourceIndex1
	 * @param sourceIndex2
	 * @param targetIndex
	 */
	private void ox(int sourceIndex1, int sourceIndex2, int targetIndex) {
		int[] p1 = memory[sourceIndex1].permutation;
		int[] p22 = memory[sourceIndex2].permutation;
		if (p1.length <= 1 || p22.length <= 1 || p1.length != p22.length) {
			System.out
					.println("Error in ox (order crossover) input permutation are not of the same length or one of them is of size <= 1");
			System.exit(0);
		}
		int[] p2 = p22.clone();
		// STEP 1: selecting two crossing points
		int n = p1.length;
		int point1 = rng.nextInt(n);
		int point2 = rng.nextInt(n);
		while (point2 == point1) {
			point2 = rng.nextInt(n);
		}
		if (point2 < point1) {
			int temp = point1;
			point1 = point2;
			point2 = temp;
		}
		int pointsToCopy = point2 - point1;

		// STEP 2: remove elements from p1 that lie between pont1 and point2
		int[] inverseP2 = algorithms.inversePermutation(p2);
		int job, job_index;
		for (int i = 0; i < pointsToCopy; i++) {
			job = p1[point1 + i];
			job_index = inverseP2[job];
			p2[job_index] = -1; // erased points get -1
		}

		// STEP 3: copy elements between point1 and point2 from p1 into the
		// receiver.
		int insertionPoint = inverseP2[p1[point1]];
		int[] receiver = new int[n + pointsToCopy];
		int counter = 0;
		for (int i = 0; i <= n; i++) {
			if (i < insertionPoint) {
				receiver[i] = p2[i];
			}
			if (i == insertionPoint) {
				for (int j = 0; j < pointsToCopy; j++) {
					receiver[i + j] = p1[point1 + j];
				}
			}
			if (i > insertionPoint) {
				receiver[i - 1 + pointsToCopy] = p2[i - 1];
			}
		}

		// STEP 4: remove repeated elements from receiver
		int[] p3 = new int[n]; // the new individual
		counter = 0;
		for (int i = 0; i < n; i++) {
			job = receiver[counter];
			if (job == -1) {
				counter++;
				i--;
				continue;
			}
			p3[i] = job;
			counter++;
		}
		double cost = algorithms.computeCost(p3);
		memory[targetIndex] = new TspSolution(p3, cost);
	}

	/**
	 * Partially Mapped Crossover
	 * 
	 * @param sourceIndex1
	 * @param sourceIndex2
	 * @param targetIndex
	 */
	private void pmx(int sourceIndex1, int sourceIndex2, int targetIndex) {
		int[] p1 = memory[sourceIndex1].permutation;
		int[] p2 = memory[sourceIndex2].permutation;

		if (p1.length <= 1 || p2.length <= 1 || p1.length != p2.length) {
			System.out
					.println("Error in ox (order crossover) input permutation are not of the same length or one of them is of size <= 1");
			System.exit(0);
		}
		// STEP 1: selecting two crossing points
		int n = p1.length;
		int point1 = rng.nextInt(n);
		int point2 = rng.nextInt(n);
		while (point2 == point1) {
			point2 = rng.nextInt(n);
		}
		if (point2 < point1) {
			int temp = point1;
			point1 = point2;
			point2 = temp;
		}
		int pointsToCopy = point2 - point1;

		// STEP 2: copy elements between point1 and point2 from p1. These are
		// placed in p3 starting at point1
		int[] p3 = new int[n]; // the new individual
		Arrays.fill(p3, -1);
		int[] jobsTaken = new int[n]; // keeps record of jobs that have been
		// copied. Job j has been copied if
		// jobsTaken[j] == -1
		int job = p1[point1];
		for (int i = 0; i < pointsToCopy; i++) {
			job = p1[point1 + i];
			p3[point1 + i] = job;
			jobsTaken[job] = -1;
		}
		// STEP 3: copy the rest of the elements in the order given in p2
		int counter = 0;
		for (int i = 0; i < n; i++) {
			if (p3[i] != -1)
				continue;
			job = p2[counter];
			while (jobsTaken[job] == -1) {
				counter++;
				job = p2[counter];
			}
			counter++;
			p3[i] = job;
		}
		double cost = algorithms.computeCost(p3);
		memory[targetIndex] = new TspSolution(p3, cost);
	}

	/**
	 * Precedence Preservative Crossover
	 * 
	 * @param sourceIndex1
	 * @param sourceIndex2
	 * @param targetIndex
	 */
	private void ppx(int sourceIndex1, int sourceIndex2, int targetIndex) {
		int[] p12 = memory[sourceIndex1].permutation;
		int[] p22 = memory[sourceIndex2].permutation;
		int[] p1 = p12.clone(); 
		int[] p2 = p22.clone();
		int n = p1.length;
		int[] inverseP1 = this.algorithms.inversePermutation(p1); // inverse of p1
		int[] inverseP2 = this.algorithms.inversePermutation(p2); // inverse of p2

		// STEP 1
		int counterP1 = 0;
		int counterP2 = 0;
		int[] p3 = new int[n];
		int randNumb;
		int job;
		for (int i = 0; i < n; i++) { // for each element of p3
			randNumb = rng.nextInt(2);
			if (randNumb == 0) { // copy next gene from p1
				job = p1[counterP1];
				while (job == -1) {
					counterP1++;
					job = p1[counterP1];
				}
				p3[i] = job;
				p1[counterP1] = -1; // remove job from p1 (not really necessary
				// since there is counter1
				p2[inverseP2[job]] = -1; // remove job from p2
				counterP1++;
			} else { // copy from p2
				job = p2[counterP2];
				while (job == -1) {
					counterP2++;
					job = p2[counterP2];
				}
				p3[i] = job;
				p2[counterP2] = -1;
				p1[inverseP1[job]] = -1;
				counterP2++;
			}
		}
		double Cost = algorithms.computeCost(p3);
		memory[targetIndex] = new TspSolution(p3, Cost);
	}

	/**
	 * One Point Crossover 
	 * 
	 * @param sourceIndex1
	 * @param sourceIndex2
	 * @param targetIndex
	 */
	private void oneX(int sourceIndex1, int sourceIndex2, int targetIndex) {
		int[] p1 = memory[sourceIndex1].permutation;
		int[] p2 = memory[sourceIndex2].permutation;

		// STEP 1: Select a crossing point.
		int n = p1.length;
		int xPoint = rng.nextInt(n);

		int[] inv = new int[n]; // inv[i] = 0, if job i has not been copied to
		// new individual
		// = 1, otherwise

		// STEP 2: Copy elements from parent 1 from, 0 to xPoint, into new
		// solution.
		int[] p3 = new int[n];
		for (int i = 0; i < xPoint; i++) {
			p3[i] = p1[i];
			inv[p1[i]] = 1;
		}

		// STEP 3: Copy rest of elements from parent 2.
		int count = xPoint;
		for (int i = 0; i < n; i++) {
			if (inv[p2[i]] != 1) {
				p3[count] = p2[i];
				count++;
			}
		}

		// STEP 4: Calculate Cmax
		double cost = algorithms.computeCost(p3);
		memory[targetIndex] = new TspSolution(p3, cost);

	}	

	private void verifyBestSolution(TspSolution solution) {
		if (bestSoFar == null || solution.Cost < bestSoFar.Cost)
			bestSoFar = solution;
	}
	
}
