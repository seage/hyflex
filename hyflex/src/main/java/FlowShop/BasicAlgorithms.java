package FlowShop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * program: NEH , implements NEH heuristic and many other methods based on it.
 * Implements the speed up techniques explained in Taillard 1993
 * 
 * @author Jose Antonio Vazquez-Rodriguez
 */

class BasicAlgorithms {
	
	private int Cmax;
	
	BasicAlgorithms() {}

	Solution neh(Instance instance) {
		int[] initialPermutation = generateLPTSequence(instance);
		int[] partialSchedule = { initialPermutation[0] };
		for (int j = 1; j < instance.n; j++) {
			partialSchedule = insert(partialSchedule, initialPermutation[j],
					instance);
		}
		return new Solution(partialSchedule, this.Cmax);
	}
	
	Solution neh(Instance instance, int[] ordering) {
		int[] partialSchedule = { ordering[0] };
		for (int j = 1; j < instance.n; j++) {
			partialSchedule = insert(partialSchedule, ordering[j], instance);
		}
		return new Solution(partialSchedule, this.Cmax);
	}
	
	int nehReturnCmax(Instance instance) {
		neh(instance);
		return this.Cmax;
	}

	Solution nehBT(Instance instance, int backTrackLevel) {
		int[] initialPermutation = generateLPTSequence(instance);
		int[] partialSchedule = { initialPermutation[0] };
		for (int j = 1; j < backTrackLevel; j++) {
			partialSchedule = insert(partialSchedule, initialPermutation[j],
					instance);
		}
		int[] jobsToInsert = new int[instance.n - backTrackLevel];
		for (int i = backTrackLevel; i < initialPermutation.length; i++) {
			jobsToInsert[i - backTrackLevel] = initialPermutation[i];
		}
		int[] schedule = nehPartScheduleBT(instance, partialSchedule,
				jobsToInsert, 3);
		return new Solution(schedule, Cmax);
	}

	int[] nehPartialSchedule(Instance instance, int[] partialSchedule,
			int[] jobsToInsert) {
		int[] newSchedule = partialSchedule;
		for (int i = 0; i < jobsToInsert.length; i++) {
			newSchedule = insert(newSchedule, jobsToInsert[i], instance);
		}
		return newSchedule;
	}

	int[] nehPartScheduleBT(Instance problem, int[] partialSchedule,
			int[] jobsToInsert, int depthOfSearch) {
		// inserting first job
		ArrayList<PartialSchedule> initialPartialSchedules = insert(
				partialSchedule, jobsToInsert[0], problem, depthOfSearch);
		ArrayList<PartialSchedule> newPartialSchedules;
		for (int i = 1; i < jobsToInsert.length; i++) {
			newPartialSchedules = new ArrayList<PartialSchedule>(depthOfSearch
					* depthOfSearch);
			for (PartialSchedule schedule : initialPartialSchedules)
				schedule.insertJob();
			int jobToInsert = jobsToInsert[i];
			for (int j = 0; j < depthOfSearch; j++) {
				newPartialSchedules.addAll(insert(initialPartialSchedules
						.get(j).partialSchedule, jobToInsert, problem,
						depthOfSearch));
			}
			Collections.sort(newPartialSchedules);
			initialPartialSchedules.clear();
			for (int k = 0; k < depthOfSearch; k++) {
				initialPartialSchedules.add(newPartialSchedules.get(k));
			}
		}
		initialPartialSchedules.get(0).insertJob();
		return initialPartialSchedules.get(0).partialSchedule;
	}

	int[] fImpLocalSearch(Instance problem, int[] initialSchedule,
			int initialCmax) {
		int bestCmax = initialCmax;
		int newCmax = initialCmax;
		boolean improvement = true;
		int[] newSchedule = initialSchedule;
		int[] bestSchedule = initialSchedule;
		while (improvement) {
			improvement = false;
			for (int i = 0; i < initialSchedule.length; i++) {
				newSchedule = fImpLocalSearchPass(problem, bestSchedule, i,
						bestCmax);
				newCmax = evaluatePermutation(newSchedule, problem);
				if (newCmax < bestCmax) {
					bestSchedule = newSchedule;
					bestCmax = newCmax;
					improvement = true;
					break;
				}
			}
		}
		return bestSchedule;
	}

	int[] fImpLocalSearchPass(Instance problem, int[] initialSchedule,
			int indexToReinsert, int initialCmax) {
		// boolean foundImprovement = false;
		int jobToInsert = initialSchedule[indexToReinsert];
		int[] partialSchedule = removeIndex(initialSchedule, indexToReinsert);
		int partialScheduleLength = partialSchedule.length;
		int[][] e = calculate_e(partialScheduleLength, problem.m,
				partialSchedule, problem.processingTimes); // completion times
															// of jobs in
															// partialSchedule
		int[][] q = calculate_q(partialScheduleLength, problem.m,
				partialSchedule, problem.processingTimes); // tails of jobs in
															// partialSchedule
		int[][] f = calculate_f(jobToInsert, partialScheduleLength,
				partialSchedule, e, problem.m, problem.processingTimes);
		int newCmax = initialCmax;
		// int position = 0;
		for (int i = 0; i <= partialScheduleLength; i++) {
			if (i == partialSchedule.length)
				newCmax = calculatePartialCmax(i, problem.m, q, f,
						partialScheduleLength);
			else
				newCmax = calculatePartialCmax(i, problem.m, q, f,
						partialScheduleLength);
			if (newCmax < initialCmax)
				return insertJob(partialSchedule, jobToInsert, i);
		}
		return initialSchedule;
	}

	int[] localSearch(Instance problem, int[] initialSchedule, int initialCmax) {
		int bestCmax = initialCmax;
		int newCmax = initialCmax;
		boolean improvement = true;
		int[] newSchedule = initialSchedule;
		int[] bestSchedule = initialSchedule;
		while (improvement) {
			improvement = false;
			for (int i = 0; i < initialSchedule.length; i++) {
				newSchedule = localSearchPass(problem, bestSchedule, i,
						bestCmax);
				newCmax = evaluatePermutation(newSchedule, problem);
				if (newCmax < bestCmax) {
					bestSchedule = newSchedule;
					bestCmax = newCmax;
					improvement = true;
					break;
				}
			}
		}
		return bestSchedule;
	}

	int[] localSearchPass(Instance problem, int[] initialSchedule,
			int indexToReinsert, int initialCmax) {
		boolean foundImprovement = false;
		int jobToInsert = initialSchedule[indexToReinsert];
		int[] partialSchedule = removeIndex(initialSchedule, indexToReinsert);
		int partialScheduleLength = partialSchedule.length;
		int[][] e = calculate_e(partialScheduleLength, problem.m,
				partialSchedule, problem.processingTimes); // completion times
															// of jobs in
															// partialSchedule
		int[][] q = calculate_q(partialScheduleLength, problem.m,
				partialSchedule, problem.processingTimes); // tails of jobs in
															// partialSchedule
		int[][] f = calculate_f(jobToInsert, partialScheduleLength,
				partialSchedule, e, problem.m, problem.processingTimes);
		int minCmax = initialCmax;
		int newCmax = initialCmax;
		int position = 0;
		for (int i = 0; i <= partialScheduleLength; i++) {
			if (i == partialSchedule.length)
				newCmax = calculatePartialCmax(i, problem.m, q, f,
						partialScheduleLength);
			else
				newCmax = calculatePartialCmax(i, problem.m, q, f,
						partialScheduleLength);
			if (newCmax < minCmax) {
				minCmax = newCmax;
				position = i;
				foundImprovement = true;
			}
		}
		if (foundImprovement)
			return insertJob(partialSchedule, jobToInsert, position);
		return initialSchedule;
	}

	int[] randomFImpLocalSearch(Instance problem, int[] initialSchedule,
			int initialCmax, int[] jobIndices) {
		int bestCmax = initialCmax;
		int newCmax = initialCmax;
		int[] newSchedule = initialSchedule;
		int[] bestSchedule = initialSchedule;
		for (int i = 0; i < jobIndices.length; i++) {
			newSchedule = fImpLocalSearchPass(problem, bestSchedule,
					jobIndices[i], bestCmax);
			newCmax = evaluatePermutation(newSchedule, problem);
			if (newCmax <= bestCmax) {
				bestSchedule = newSchedule;
				bestCmax = newCmax;
			}
		}
		return bestSchedule;
	}

	int[] randomLocalSearch(Instance problem, int[] initialSchedule,
			int initialCmax, int[] jobIndices) {
		int bestCmax = initialCmax;
		int newCmax = initialCmax;
		int[] newSchedule = initialSchedule;
		int[] bestSchedule = initialSchedule;
		for (int i = 0; i < jobIndices.length; i++) {
			newSchedule = localSearchPass(problem, bestSchedule, jobIndices[i],
					bestCmax);
			newCmax = evaluatePermutation(newSchedule, problem);
			if (newCmax <= bestCmax) {
				bestSchedule = newSchedule;
				bestCmax = newCmax;
			}
		}
		return bestSchedule;
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
	
	private int[][] calculate_e(final int partialScheduleLength, final int m,
			final int[] partialSchedule, int[][] processingTimes) {
		int[][] e = new int[partialSchedule.length][m];
		int jobIndex;
		for (int j = 0; j < partialScheduleLength; j++) { // for every job in
															// partialSchudule
			jobIndex = partialSchedule[j];
			for (int k = 0; k < m; k++) { // for every stage
				if (j == 0 && k == 0) { // first job in first stage
					e[j][k] = processingTimes[jobIndex][k];
					continue;
				}
				if (j == 0) { // if first job
					e[j][k] = e[j][k - 1] + processingTimes[jobIndex][k];
					continue;
				}
				if (k == 0) { // if first stage
					e[j][k] = e[j - 1][k] + processingTimes[jobIndex][k];
					continue;
				}
				e[j][k] = Math.max(e[j][k - 1], e[j - 1][k])
						+ processingTimes[jobIndex][k];
			}
		}
		return e;
	}

	private int[][] calculate_f(final int jobIndex,
			final int partialScheduleLength, final int[] partialSchedule,
			final int[][] e, final int m, int[][] processingTimes) {
		int[][] f = new int[partialSchedule.length + 1][m];
		for (int j = 0; j <= partialScheduleLength; j++) {
			for (int k = 0; k < m; k++) {
				if (j == 0 && k == 0) { // if inserted in the first machine in
										// the first position
					f[j][k] = processingTimes[jobIndex][k];
					continue;
				}
				if (j == 0) { // if first position in any stage
					f[j][k] = f[j][k - 1] + processingTimes[jobIndex][k];
					continue;
				}
				if (k == 0) { // if stage 1 in any position
					f[j][k] = e[j - 1][k] + processingTimes[jobIndex][k];
					continue;
				}
				f[j][k] = Math.max(f[j][k - 1], e[j - 1][k])
						+ processingTimes[jobIndex][k];
			}
		}
		return f;
	}

	private int[][] calculate_q(final int partialScheduleLength, int m,
			final int[] partialSchedule, int[][] processingTimes) {
		int[][] q = new int[partialScheduleLength][m];
		int jobIndex;
		for (int j = partialScheduleLength - 1; j >= 0; j--) { // for every job
																// in partial
																// schedule
			jobIndex = partialSchedule[j];
			for (int k = m - 1; k >= 0; k--) { // for every stage
				if (j == partialScheduleLength - 1 && k == m - 1) { // if last
																	// job in
																	// last
																	// machine
					q[j][k] = processingTimes[jobIndex][k];
					continue;
				}
				if (j == partialScheduleLength - 1) { // if last job in any
														// machine
					q[j][k] = q[j][k + 1] + processingTimes[jobIndex][k];
					continue;
				}
				if (k == m - 1) { // if last machine and any job
					q[j][k] = q[j + 1][k] + processingTimes[jobIndex][k];
					continue;
				}
				q[j][k] = Math.max(q[j + 1][k], q[j][k + 1])
						+ processingTimes[jobIndex][k];
			}
		}
		return q;
	}

	private int calculatePartialCmax(int position, int m, int[][] q, int[][] f,
			int partialScheduleLength) {
		int M = 0;
		if (position == partialScheduleLength) {
			M = f[position][m - 1];
		} else {
			for (int k = 0; k < m; k++) {
				M = Math.max(M, f[position][k] + q[position][k]);
			}
		}
		return M;
	}

	private int[] generateLPTSequence(Instance instance) {
		int m = instance.m;
		int n = instance.n;
		int[][] processingTimes = instance.processingTimes;

		int[] sumProcTimes = new int[n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				sumProcTimes[i] = sumProcTimes[i] + processingTimes[i][j];
			}
		}
		Job[] jobs = new Job[n];
		for (int i = 0; i < n; i++) {
			jobs[i] = new Job(i, sumProcTimes[i]);
		}
		Arrays.sort(jobs);
		int[] permutation = new int[n];
		for (int i = 0; i < n; i++) {
			permutation[i] = jobs[i].index;
		}
		return permutation;
	}

	private int[] insert(int[] partialSchedule, int jobToInsert,
			Instance instance) {
		int m = instance.m;
		int[][] processingTimes = instance.processingTimes;
		int partialScheduleLength = partialSchedule.length;
		int[][] e = calculate_e(partialScheduleLength, m, partialSchedule,
				processingTimes); // completion times of jobs in partialSchedule
		int[][] q = calculate_q(partialScheduleLength, m, partialSchedule,
				processingTimes); // tails of jobs in partialSchedule
		int[][] f = calculate_f(jobToInsert, partialScheduleLength,
				partialSchedule, e, m, processingTimes);
		int minCmax = calculatePartialCmax(0, m, q, f, partialScheduleLength);
		int position = 0;
		int newCmax;
		for (int i = 1; i <= partialSchedule.length; i++) {
			if (i == partialSchedule.length)
				newCmax = calculatePartialCmax(i, m, q, f,
						partialScheduleLength);
			else
				newCmax = calculatePartialCmax(i, m, q, f,
						partialScheduleLength);

			if (newCmax < minCmax) {
				minCmax = newCmax;
				position = i;
			}
		}
		this.Cmax = minCmax;
		int[] newPermutation = new int[partialSchedule.length + 1];
		int counter = 0;
		newPermutation[position] = jobToInsert;
		for (int i = 0; i < newPermutation.length; i++, counter++) {
			if (i == position) {
				counter--;
				continue;
			}
			newPermutation[i] = partialSchedule[counter];
		}
		return newPermutation;
	}

	private ArrayList<PartialSchedule> insert(int[] partialSchedule,
			int jobToInsert, Instance problem, int depthOfSearch) {
		ArrayList<PartialSchedule> partialSchedules = new ArrayList<PartialSchedule>(
				partialSchedule.length + 1);
		int partialScheduleLength = partialSchedule.length;
		int[][] e = calculate_e(partialScheduleLength, problem.m,
				partialSchedule, problem.processingTimes); // completion times
															// of jobs in
															// partialSchedule
		int[][] q = calculate_q(partialScheduleLength, problem.m,
				partialSchedule, problem.processingTimes); // tails of jobs in
															// partialSchedule
		int[][] f = calculate_f(jobToInsert, partialScheduleLength,
				partialSchedule, e, problem.m, problem.processingTimes);
		int newCmax;
		for (int i = 0; i <= partialScheduleLength; i++) {
			if (i == partialSchedule.length)
				newCmax = calculatePartialCmax(i, problem.m, q, f,
						partialScheduleLength); // the 0 was chosen arbitrarily
												// it is not taken inteo account
												// in method
			else
				newCmax = calculatePartialCmax(i, problem.m, q, f,
						partialScheduleLength);
			partialSchedules.add(new PartialSchedule(partialSchedule,
					jobToInsert, i, newCmax));
		}
		Collections.sort(partialSchedules);
		ArrayList<PartialSchedule> schedulesToReturn = new ArrayList<PartialSchedule>(
				depthOfSearch);
		Iterator<PartialSchedule> iterator = partialSchedules.iterator();
		for (int i = 0; i < depthOfSearch; i++) {
			schedulesToReturn.add(iterator.next());
		}
		return schedulesToReturn;
	}


	private int[] insertJob(int[] initialSequence, int jobToInsert,
			int placeToInsert) {
		int[] newSequence = new int[initialSequence.length + 1];
		int counter = 0;
		newSequence[placeToInsert] = jobToInsert;
		for (int i = 0; i < newSequence.length; i++, counter++) {
			if (i == placeToInsert) {
				counter--;
				continue;
			}
			newSequence[i] = initialSequence[counter];
		}
		return newSequence;
	}

	private int[] removeIndex(int[] initialSequence, int indexToRemove) {
		int[] newSequence = new int[initialSequence.length - 1];
		int counter = 0;
		for (int i = 0; i < initialSequence.length; i++) {
			if (i != indexToRemove) {
				newSequence[counter] = initialSequence[i];
				counter++;
			}
		}
		return newSequence;
	}

	private class Job implements Comparable<Job> {
		int index;
		int sumproctimes;

		Job(int index, int sumproctimes) {
			this.index = index;
			this.sumproctimes = sumproctimes;
		}

		public int compareTo(Job o) {
			return (-this.sumproctimes + o.sumproctimes);
		}
	}

	private class PartialSchedule implements Comparable<PartialSchedule> {
		int[] partialSchedule;
		int jobToInsert;
		int placeToInsert;
		int Cmax;

		PartialSchedule(int[] partialSchedule, int jobToInsert,
				int placeToInsert, int Cmax) {
			this.partialSchedule = partialSchedule;
			this.jobToInsert = jobToInsert;
			this.placeToInsert = placeToInsert;
			this.Cmax = Cmax;
		}

		public int compareTo(PartialSchedule ps2) {
			return this.Cmax - ps2.Cmax;
		}

		void insertJob() {
			int[] newSchedule = new int[partialSchedule.length + 1];
			int counter = 0;
			newSchedule[placeToInsert] = jobToInsert;
			for (int i = 0; i < newSchedule.length; i++, counter++) {
				if (i == placeToInsert) {
					counter--;
					continue;
				}
				newSchedule[i] = partialSchedule[counter];
			}
			this.partialSchedule = newSchedule;
		}
	}

}
