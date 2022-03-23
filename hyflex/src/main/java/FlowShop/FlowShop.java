package FlowShop;



import java.util.Arrays;

import AbstractClasses.ProblemDomain;


/**
 * Implements the permutation flow shop problem domain.
 * @author Jose Antonio Vazquez-Rodriguez, email jav@cs.nott.ac.uk
 */
public class FlowShop extends ProblemDomain {
	private Solution[] memory = new Solution[2];
	private Solution bestSoFar;
	private Instance probInstance;
	private BasicAlgorithms heuristics = new BasicAlgorithms();

	/**
	 * Creates a flow shop domain and creates a new random number generator
	 * using the seed provided. Sets the memory size to 2.
	 * 
	 * @param seed
	 *            a random seed
	 */
	public FlowShop(long seed) {
		super(seed);
	}

	public void loadInstance(int instanceID) {
		this.probInstance = new Instance(instanceID);
	}

	public void initialiseSolution(int targetIndex) {
		memory[targetIndex] = heuristics.neh(probInstance, this
				.generateRandomPermutation(this.probInstance.n));
		verifyBestSolution(memory[targetIndex]);
	} 

	public void setMemorySize(int size) {
		Solution[] tempMemory = new Solution[size];
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

	public int[] getHeuristicsOfType(HeuristicType heuristicType) {
		if (heuristicType == ProblemDomain.HeuristicType.MUTATION)
			return new int[] { 0, 1, 2, 3, 4 };
		if (heuristicType == ProblemDomain.HeuristicType.RUIN_RECREATE)
			return new int[] { 5, 6 };
		if (heuristicType == ProblemDomain.HeuristicType.LOCAL_SEARCH)
			return new int[] { 7, 8, 9, 10 };
		if (heuristicType == ProblemDomain.HeuristicType.CROSSOVER)
			return new int[] { 11, 12, 13, 14};
		return null;
	}

	@Override
	public int[] getHeuristicsThatUseDepthOfSearch() {
		return new int[] { 6, 9, 10 };
	}

	@Override
	public int[] getHeuristicsThatUseIntensityOfMutation() {
		return new int[] { 3, 5, 6 };
	}

	public double getBestSolutionValue() {
		return bestSoFar.Cmax;
	}

	public double getFunctionValue(int index) {
		return memory[index].Cmax;
	}

	public double applyHeuristic(int llhID, int solutionSourceIndex,
			int solutionTargetIndex) {
		long startTime = System.currentTimeMillis();
		
		boolean isCrossover = false;
		int[] crossovers = getHeuristicsOfType(HeuristicType.CROSSOVER);
		if (!(crossovers == null)) {
			for (int x = 0; x < crossovers.length; x++) {
				if (crossovers[x] == llhID) {
					isCrossover = true;
					break;}
			}//end for looping the crossover heuristics
		}//end if
		if (isCrossover) {//copy over the solution
			copySolution(solutionSourceIndex, solutionTargetIndex);
		} else {
			switch (llhID) {
			case 0:
				randomReinsertion(solutionSourceIndex, solutionTargetIndex);
				break;
			case 1:
				swapTwo(solutionSourceIndex, solutionTargetIndex);
				break;
			case 2:
				shuffle(solutionSourceIndex, solutionTargetIndex);
				break;
			case 3:
				shuffleSubSequence(solutionSourceIndex, solutionTargetIndex);
				break;
			case 4:
				useNEH(solutionSourceIndex, solutionTargetIndex);
				break;
			case 5:
				iteratedGreedy(solutionSourceIndex, solutionTargetIndex);
				break;
			case 6:
				deepIteratedGreedy(solutionSourceIndex, solutionTargetIndex);
				break;
			case 7:
				localSearch(solutionSourceIndex, solutionTargetIndex);
				break;
			case 8:
				fImpLocalSearch(solutionSourceIndex, solutionTargetIndex);
				break;
			case 9:
				randomLocalSearch(solutionSourceIndex, solutionTargetIndex);
				break;
			case 10:
				randomFImpLocalSearch(solutionSourceIndex, solutionTargetIndex);
				break;
			default: 
				System.err.println("heuristic does not exist, or the crossover index array is not set up correctly");
				System.exit(-1);
			}
		}
		
		this.heuristicCallRecord[llhID]++;
		heuristicCallTimeRecord[llhID] += (int)(System.currentTimeMillis() - startTime);
		
		this.verifyBestSolution(memory[solutionTargetIndex]);
		return memory[solutionTargetIndex].Cmax;
	}

	public double applyHeuristic(int llhID, int solutionSourceIndex1,
			int solutionSourceIndex2, int solutionTargetIndex) {
		long startTime = System.currentTimeMillis();
		
		switch (llhID) {
		case 0:
			randomReinsertion(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 1:
			swapTwo(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 2:
			shuffle(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 3:
			shuffleSubSequence(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 4:
			useNEH(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 5:
			iteratedGreedy(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 6:
			deepIteratedGreedy(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 7:
			localSearch(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 8:
			fImpLocalSearch(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 9:
			randomLocalSearch(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 10:
			fImpLocalSearch(solutionSourceIndex1, solutionTargetIndex);
			break;
		case 11:
			ox(solutionSourceIndex1, solutionSourceIndex2, solutionTargetIndex);
			break;
		case 12:
			ppx(solutionSourceIndex1, solutionSourceIndex2, solutionTargetIndex);
			break;
		case 13:
			pmx(solutionSourceIndex1, solutionSourceIndex2, solutionTargetIndex);
			break;
		case 14:
			oneX(solutionSourceIndex1, solutionSourceIndex2,
					solutionTargetIndex);
			break;
		default: 
			System.err.println("heuristic does not exist, or the crossover index array is not set up correctly");
			System.exit(-1);
		}
		
		this.heuristicCallRecord[llhID]++;
		heuristicCallTimeRecord[llhID] += (int)(System.currentTimeMillis() - startTime);
		
		this.verifyBestSolution(memory[solutionTargetIndex]);
		return memory[solutionTargetIndex].Cmax;
	}

	public void copySolution(int sourceIndex, int targetIndex) {
		memory[targetIndex] = memory[sourceIndex].clone();
	}

	public int getNumberOfHeuristics() {
		return 15;
	}

	public String solutionToString(int solutionIndex) {
		return memory[solutionIndex].toString();
	}

	public String toString() {
		//return this.probInstance.toString();
		return "FlowShop";
	}

	public String bestSolutionToString() {
		return bestSoFar.toString();
	}

	public int getNumberOfInstances() {
		return 12;
	}

	public Object getProblemData(String args) {
		if (args == "N")
			return probInstance.n;
		if (args == "M")
			return probInstance.m;
		if (args == "SUM_P")
			return probInstance.getSumP();

		// if none of the above
		return probInstance.processingTimes;
	}

	public boolean compareSolutions(int solutionIndex1, int solutionIndex2) {
		if (memory[solutionIndex1].Cmax != memory[solutionIndex2].Cmax)
			return false;
		int[] p1 = memory[solutionIndex1].permutation;
		int[] p2 = memory[solutionIndex2].permutation;
		int n = this.probInstance.n;
		for (int i = 0; i < n; i++) {
			if (p1[i] != p2[i])
				return false;
		}
		return true;
	}

	// MUTATION HEURISTICS
	/**
	 * Makes a copy of the solution in sourceIndex, modifies it by removing a
	 * randomly selected element in the permutation and reinserting it in
	 * another randomly selected place. The rest of the jobs are shifted as
	 * required. The new solution is placed in memory in targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void randomReinsertion(int sourceIndex, int targetIndex) {
		int[] array = memory[sourceIndex].permutation.clone();

		int i1 = rng.nextInt(array.length);
		int i2;
		while ((i2 = rng.nextInt(array.length)) == i1)
			;

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
		double Cmax = evaluatePermutation(newArray, probInstance);
		memory[targetIndex] = new Solution(newArray, Cmax);
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
		double Cmax = evaluatePermutation(array, probInstance);
		memory[targetIndex] = new Solution(array, Cmax);
	}

	/**
	 * Creates a new random solution and places it in memory in targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void shuffle(int sourceIndex, int targetIndex) {
		int[] array = memory[sourceIndex].permutation.clone();
		this.shufflePermutation(array);
		double Cmax = evaluatePermutation(array, probInstance);
		memory[targetIndex] = new Solution(array, Cmax);
	}

	/**
	 * Makes a copy of the solution in sourceIndex, the permutation is then used
	 * to seed the NEH procedure. The resultant solution is placed in memory in
	 * targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void useNEH(int sourceIndex, int targetIndex) {
		memory[targetIndex] = heuristics.neh(this.probInstance,
				memory[sourceIndex].permutation);
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
		int numbToShuffle = 2 + (int) (this.getIntensityOfMutation() * (this.probInstance.n - 2));
		int[] AvailableIndices = new int[probInstance.n];
		Arrays.fill(AvailableIndices, 1);
		int count = probInstance.n;
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
		this.shufflePermutation(IndicesToShuffle);
		for (int i = 0; i < numbToShuffle; i++) {
			permutation[IndicesToShuffle[i]] = jobIndices[i];
		}
		double Cmax = evaluatePermutation(permutation, probInstance);
		memory[targetIndex] = new Solution(permutation, Cmax);
	}

	// RUIN RECREATE HEURISTICS
	/**
	 * Makes a copy of the solution in sourceIndex. This is improved using an
	 * iterated procedure in which a number of elements of the permutation are
	 * removed and placed back using the re-insertion procedure of NEH. 
The new
	 * solution is placed in memory in targetIndex.
	 *
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void iteratedGreedy(int sourceIndex, int targetIndex) {
		int numbRemove = ((int) (this.getIntensityOfMutation() * (this.probInstance.n - 1)*0.5)) + 1;
		int[] partialSequence = new int[probInstance.n - numbRemove];
		int[] jobsToInsert = new int[numbRemove];
		int[] list = memory[sourceIndex].permutation.clone();
		int id;
		for (int i = 0; i < numbRemove; i++) {
			id = rng.nextInt(probInstance.n);
			while (list[id] < 0)
				id = rng.nextInt(probInstance.n);
			jobsToInsert[i] = list[id];
			list[id] = -1;
		}
		for (int i = 0, j = 0; i < probInstance.n; i++, j++) {
			try {
				if (list[i] > -1) {
					partialSequence[j] = list[i];
				} else
					j--;
			} catch (Exception ex) {
				System.out.println(" " + partialSequence.length + " "
						+ list.length + " j " + j + " i " + i);
				System.exit(0);
			}
		}
		int[] permutation = heuristics.nehPartialSchedule(probInstance,
				partialSequence, jobsToInsert);
		double Cmax = evaluatePermutation(permutation, probInstance);
		memory[targetIndex] = new Solution(permutation, Cmax);
	}

	/**
	 * Makes a copy of the solution in sourceIndex. The copy is improved using
	 * an iterated procedure in which a number of elements of the permutation
	 * are removed and placed back using the re-insertion procedure of NEH. For
	 * each of the removed elements, the procedures keeps record of a certain
	 * best possible positions for the element. The next element is considered
	 * in all possible positions on all of the best partial solutions. 
The new
	 * solution is placed in memory in targetIndex.
	 *
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void deepIteratedGreedy(int sourceIndex, int targetIndex) {

		int numbRemove = ((int) (this.getIntensityOfMutation() * (this.probInstance.n - 1)*0.5)) + 1;
		int depthOfSearch = ((int) (this.getDepthOfSearch() * (numbRemove - 1))) + 1;

		int[] partialSequence = new int[probInstance.n - numbRemove];
		int[] jobsToInsert = new int[numbRemove];
		int[] list = memory[sourceIndex].permutation.clone();
		int id;
		for (int i = 0; i < numbRemove; i++) {
			id = rng.nextInt(probInstance.n);
			while (list[id] < 0)
				id = rng.nextInt(probInstance.n);
			jobsToInsert[i] = list[id];
			list[id] = -1;
		}
		for (int i = 0, j = 0; i < probInstance.n; i++, j++) {
			if (list[i] > -1)
				partialSequence[j] = list[i];
			else
				j--;
		}
		int[] permutation = heuristics.nehPartScheduleBT(probInstance,
				partialSequence, jobsToInsert, depthOfSearch);
		double Cmax = evaluatePermutation(permutation, probInstance);
		memory[targetIndex] = new Solution(permutation, Cmax);
	}


	// LOCAL SEARCH HEURISTICS
	/**
	 * Makes a copy of the solution in sourceIndex. The copy is improved by
	 * removing each job from its position and placing it in all other
	 * positions. The job is fixed where it leads to the best schedule. This is
	 * repeated for all jobs for a number of times as long as there is
	 * improvement. The final solution is placed in memory in targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void localSearch(int sourceIndex, int targetIndex) {
		int[] improvedSchedule = this.heuristics
		.localSearch(probInstance, memory[sourceIndex].permutation,
				(int) memory[sourceIndex].Cmax);
		double Cmax = evaluatePermutation(improvedSchedule, probInstance);
		memory[targetIndex] = new Solution(improvedSchedule, Cmax);
	}

	/**
	 * Makes a copy of the solution in sourceIndex. The copy is improved by
	 * removing each job from its position and placing it the other positions.
	 * Once an improving move is identified, this is performed and the search
	 * moves to the next job. This is repeated for all jobs for a number of
	 * times as long as there is improvement. The final solution is placed in
	 * memory in targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void fImpLocalSearch(int sourceIndex, int targetIndex) {
		int[] improvedSchedule = this.heuristics
		.fImpLocalSearch(probInstance, memory[sourceIndex].permutation,
				(int) memory[sourceIndex].Cmax);
		double Cmax = evaluatePermutation(improvedSchedule, probInstance);
		memory[targetIndex] = new Solution(improvedSchedule, Cmax);
	}

	/**
	 * Makes a copy of the solution in sourceIndex. The copy is improved by
	 * removing a number of randomly selected jobs from their position (one at a
	 * time) and placing them in all the other positions fixing them to the best
	 * place. This is repeated for all jobs for as long as there is improvement.
	 * The final solution is placed in memory in targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void randomLocalSearch(int sourceIndex, int targetIndex) {
		int numbLocalSearchPasses = ((int) (this.getDepthOfSearch() * (this.probInstance.n - 1))) + 1;
		int[] randomPermutation = generateRandomPermutation(probInstance.n);
		int[] reducedPermutation = new int[numbLocalSearchPasses];
		for (int i = 0; i < numbLocalSearchPasses; i++)
			reducedPermutation[i] = randomPermutation[i];
		int[] improvedSchedule = this.heuristics.randomLocalSearch(
				probInstance, memory[sourceIndex].permutation,
				(int) memory[sourceIndex].Cmax, reducedPermutation);
		double Cmax = evaluatePermutation(improvedSchedule, probInstance);
		memory[targetIndex] = new Solution(improvedSchedule, Cmax);
	}

	/**
	 * Makes a copy of the solution in sourceIndex. The copy is improved by
	 * removing a number of randomly selected jobs from their position (one at a
	 * time) and placing them in the other positions. Once an improving move is
	 * identified, this is performed and the search moves to the next job. This
	 * is repeated only once and for the randomly selected jobs only. The final
	 * solution is placed in memory in targetIndex.
	 * 
	 * @param sourceIndex
	 * @param targetIndex
	 */
	private void randomFImpLocalSearch(int sourceIndex, int targetIndex) {
		int numbLocalSearchPasses = ((int) (this.getDepthOfSearch() * (this.probInstance.n - 1))) + 1;
		int[] randomPermutation = generateRandomPermutation(probInstance.n);
		int[] reducedPermutation = new int[numbLocalSearchPasses];
		for (int i = 0; i < numbLocalSearchPasses; i++)
			reducedPermutation[i] = randomPermutation[i];
		int[] improvedSchedule = this.heuristics.randomFImpLocalSearch(
				probInstance, memory[sourceIndex].permutation,
				(int) memory[sourceIndex].Cmax, reducedPermutation);
		double Cmax = evaluatePermutation(improvedSchedule, probInstance);
		memory[targetIndex] = new Solution(improvedSchedule, Cmax);
	}

	// CROSSOVER HEURISTICS
	/**
	 * Creates a new solution by applying Order Crossover to the two parents.
	 * The new solution is stored in memory in targetIndex.
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
		int[] inverseP2 = returnInversePermutation(p2);
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
		double Cmax = evaluatePermutation(p3, probInstance);
		memory[targetIndex] = new Solution(p3, Cmax);
	}

	/**
	 * Creates a new solution by applying Partially Mapped Crossover to the two
	 * parents. The new solution is stored in memory in targetIndex.
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
		double Cmax = evaluatePermutation(p3, probInstance);
		memory[targetIndex] = new Solution(p3, Cmax);
	}

	/**
	 * Creates a new solution by applying Precedence Preservative Crossover to
	 * the two parents. The new solution is stored in memory in targetIndex.
	 * 
	 * @param sourceIndex1
	 * @param sourceIndex2
	 * @param targetIndex
	 */
	private void ppx(int sourceIndex1, int sourceIndex2, int targetIndex) {
		int[] p12 = memory[sourceIndex1].permutation;
		int[] p22 = memory[sourceIndex2].permutation;
		int[] p1 = p12.clone(); // to be modified, already assigned jobs are
		// marked as -1
		int[] p2 = p22.clone(); // to be modified, already assigned jobs are
		// marked as -1
		int n = p1.length;
		int[] inverseP1 = returnInversePermutation(p1); // inverse of p1
		int[] inverseP2 = returnInversePermutation(p2); // inverse of p2

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
		double Cmax = evaluatePermutation(p3, probInstance);
		memory[targetIndex] = new Solution(p3, Cmax);
	}

	/**
	 * Creates a new solution by applying One Point Crossover to the two
	 * parents. The new solution is stored in memory in targetIndex.
	 * 
	 * The new solution is created by copying all of the elements from parent 1, up to the crossover point.
	 * Then the remaining elements are copied from parent 2, in the order that they appear.
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
		double Cmax = evaluatePermutation(p3, probInstance);
		memory[targetIndex] = new Solution(p3, Cmax);

	}

	//	// PROTOTYPE HEURISTICS
	//	/**
	//	 * Creates a new solution by applying Local Search Guided Crossover to the
	//	 * two parents. The new solution is stored in memory in targetIndex.
	//	 * 
	//	 * @param sourceIndex1
	//	 * @param sourceIndex2
	//	 * @param targetIndex
	//	 */
	//	private void lsXOver(int sourceIndex1, int sourceIndex2, int targetIndex) {
	//		int[] p2 = memory[sourceIndex2].permutation;
	//		int[] inverse = new int[p2.length];
	//		for (int i = 0; i < p2.length; i++) {
	//			inverse[p2[i]] = i;
	//		}
	//		int inv;
	//		int[] p1 = memory[sourceIndex1].permutation;
	//		int[] newArray;
	//		double Xo = memory[sourceIndex1].Cmax;
	//		double X1;
	//		for (int i = 0; i < p1.length; i++) {
	//			inv = inverse[p1[i]];
	//			if (i != inv) {
	//				newArray = shift(p1, i, inv);
	//				X1 = evaluatePermutation(newArray, probInstance);
	//				if (X1 < Xo || rng.nextDouble() < this.intensityOfMutation) {
	//					p1 = newArray;
	//					Xo = X1;
	//					if (i < inv) {
	//						i--;
	//					} else {
	//						i++;
	//					}
	//				}
	//			}
	//		}
	//		memory[targetIndex] = new Solution(p1, Xo);
	//	}

	// UTILITY METHODS
	private void shufflePermutation(int[] array) {
		int n = array.length; // The number of items left to shuffle (loop
		// invariant).
		while (n > 1) {
			int k = rng.nextInt(n); // 0 <= k < n.
			--n; // n is now the last pertinent index;
			int temp = array[n]; // swap array[n] with array[k].
			array[n] = array[k];
			array[k] = temp;
		}
	}

	private int[] generateRandomPermutation(int n) {
		int[] randomPermutation = new int[n];
		for (int i = 0; i < n; i++)
			randomPermutation[i] = i;
		shufflePermutation(randomPermutation);
		return randomPermutation;
	}

	private void verifyBestSolution(Solution solution) {
		if (bestSoFar == null || solution.Cmax < bestSoFar.Cmax)
			bestSoFar = solution;
	}

	private int evaluatePermutation(int[] permutation, Instance instance) {
		int[][] processingTimes = instance.processingTimes;
		int n = instance.n;
		int m = instance.m;

		int[] releaseTimes = new int[n];
		int time, jobIndex, releaseTime;

		// scheduling machine 1
		time = 0;
		for (int j = 0; j < n; j++) {
			jobIndex = permutation[j];
			time += processingTimes[jobIndex][0];
			releaseTimes[jobIndex] = time;
		}

		// scheduling machines 2 ... m
		for (int i = 1; i < m; i++) {
			time = 0;
			for (int j = 0; j < n; j++) {
				jobIndex = permutation[j];
				releaseTime = releaseTimes[jobIndex];
				time = (releaseTime >= time ? releaseTime : time)
				+ processingTimes[jobIndex][i];
				releaseTimes[jobIndex] = time;
			}
		}
		return time;
	}

//	private int[] shift(int[] array, int i1, int i2) {
//		int[] newArray = new int[array.length];
//		newArray[i2] = array[i1];
//		if (i1 < i2) {
//			for (int i = 0, count = 0; count < array.length; i++, count++) {
//				if (i == i1)
//					count++;
//				if (i == i2) {
//					count--;
//					continue;
//				}
//				newArray[i] = array[count];
//			}
//		} else {
//			for (int i = 0, count = 0; count < array.length; i++, count++) {
//				if (i == i2) {
//					count--;
//					continue;
//				}
//				newArray[i] = array[count];
//				if (i == i1)
//					count++;
//			}
//		}
//		return newArray;
//	}

	private int[] returnInversePermutation(int[] p) {
		int[] inv = new int[p.length];
		int n = p.length;
		for (int i = 0; i < n; i++)
			inv[p[i]] = i;
		return inv;
	}

}
