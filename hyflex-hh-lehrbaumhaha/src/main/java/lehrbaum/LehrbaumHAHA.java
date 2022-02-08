package lehrbaum;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.TreeMap;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import AbstractClasses.ProblemDomain.HeuristicType;

public class LehrbaumHAHA extends HyperHeuristic {
	double initialFitness;
	ProblemDomain problem;
	SolutionBuffer buffer;
	Random rng;
	
	int[] lsHeuristics;
	int[] muHeuristics;
	int[] coHeuristics;
	
	final int ringBufferSize = 50;
	final int numMutants = 7;
	final int numSlots = 1 + numMutants + ringBufferSize;
	double[] fitness = new double[numSlots + 2];

	TreeMap<Double, Integer> lsQualities = new TreeMap<Double, Integer>();
	TreeMap<Double, Integer> muQualities = new TreeMap<Double, Integer>();
	
	int[] lsRuntimeSum;
	double[] lsGainSum;
	int[] lsUsageCount;
	int[] lsSuccessCount;
	
	int[] muRuntimeSum;
	double[] muGainSum;
	int[] muUsageCount;

	int[] workingHeuristicIdx = new int[numSlots];
	int[] muHeuristicIndices = new int[numSlots];
	
	boolean realValuedFitness = false;
	
	long curTimestamp, prevTimestamp;	
	
	int qualityUpdateInterval;
	long lastQualityUpdate;
	
	long timeLimit;
		
	public LehrbaumHAHA(long seed) {
		super(seed);
		rng = new Random(seed);
	}
	
	private void initialiseDatastructures() {
		lsHeuristics = problem.getHeuristicsOfType(HeuristicType.LOCAL_SEARCH);
		muHeuristics = Arrays.copyOf(
										problem.getHeuristicsOfType(HeuristicType.MUTATION),
										problem.getHeuristicsOfType(HeuristicType.MUTATION).length +
										problem.getHeuristicsOfType(HeuristicType.RUIN_RECREATE).length
									);
		System.arraycopy(
						  problem.getHeuristicsOfType(HeuristicType.RUIN_RECREATE),
						  0,
						  muHeuristics,
						  problem.getHeuristicsOfType(HeuristicType.MUTATION).length,
						  problem.getHeuristicsOfType(HeuristicType.RUIN_RECREATE).length
						);

		coHeuristics = problem.getHeuristicsOfType(HeuristicType.CROSSOVER);

		lsQualities = new TreeMap<Double, Integer>();
		muQualities = new TreeMap<Double, Integer>();

		lsRuntimeSum = new int[lsHeuristics.length];
		lsGainSum = new double[lsHeuristics.length];
		lsUsageCount = new int[lsHeuristics.length];
		lsSuccessCount = new int[lsHeuristics.length];

		muRuntimeSum = new int[muHeuristics.length];
		muGainSum = new double[muHeuristics.length];
		for (int i = 0; i < muHeuristics.length; i++) {
			muGainSum[i] = 1.0;
		}
		muUsageCount = new int[muHeuristics.length];

		workingHeuristicIdx = new int[numSlots];
		
		for (int i = 0; i < fitness.length; i++) {
			fitness[i] = Double.POSITIVE_INFINITY;
		}
		
		qualityUpdateInterval = 5000;
		timeLimit = getTimeLimit();
	}
	
	private void initialiseQualities() {
		long startTime;
		int bestFitnessIdx = 0;
		double bestFitness = Double.POSITIVE_INFINITY;

		problem.setDepthOfSearch(1.0);
		
		for (int i = 0; i < lsHeuristics.length; i++) {
			startTime = getElapsedTime();
			fitness[i+1] = problem.applyHeuristic(lsHeuristics[i], 0, i + 1);
			if (fitness[i+1] != Math.floor(fitness[i+1]))
				realValuedFitness = true;
			lsRuntimeSum[i] += (int)(getElapsedTime() - startTime + 1);
			lsGainSum[i] += (fitness[0] - fitness[i+1]);
			lsUsageCount[i]++;
			
			double quality = lsGainSum[i] / (double)(lsRuntimeSum[i]);
			
			while (lsQualities.containsKey(quality)) {
				quality = nextAfter(quality, Double.NEGATIVE_INFINITY);
			}
			lsQualities.put(quality, i);
			
			if (fitness[i+1] < fitness[numSlots]) {
				lsSuccessCount[i]++;
				
				if (fitness[i+1] < bestFitness) {
					bestFitness = fitness[i+1];
					bestFitnessIdx = i + 1;
				}
			}
		}
		
		if (bestFitnessIdx != 0) {
			problem.copySolution(bestFitnessIdx, 0);
			fitness[0] = bestFitness;
			problem.copySolution(bestFitnessIdx, numSlots + 1);
			fitness[numSlots + 1] = bestFitness;
		}
	}
	
	public void solve(ProblemDomain problem) {
		this.problem = problem;
		
		initialiseDatastructures();
		
		problem.setMemorySize(numSlots + 2);
		problem.initialiseSolution(0);
		fitness[0] = problem.getFunctionValue(0);
		
		initialiseQualities();
		
		if (realValuedFitness) {
			buffer = new HashSolutionBuffer(numSlots - ringBufferSize, ringBufferSize);
		} else {
			buffer = new LinearSolutionBuffer(numSlots - ringBufferSize, ringBufferSize);
		}

		int stallCounter = 0;
		long stalledSince = 0;
		
		while (!hasTimeExpired()) {
			localSearch(0);
			generateMutations();
			parallelSearch();
			selectWorkingSolution();
			
			if (fitness[0] > fitness[numSlots + 1]) {
				stallCounter++;
				if (stallCounter > 5 && (curTimestamp - stalledSince) > 6000) {
					problem.copySolution(numSlots + 1, 0);
					fitness[0] = fitness[numSlots + 1];
					stallCounter = 0;
					stalledSince = curTimestamp;
				}
			} else {
				stallCounter = 0;
				stalledSince = curTimestamp;
			}

		}
	}
	
	private void localSearch(int workingSlot) {
		buffer.add(workingSlot, fitness[workingSlot]);

		curTimestamp = getElapsedTime();
		for (int i = 0; i < lsHeuristics.length; i++) {
			problem.setDepthOfSearch(rng.nextDouble());
			int curHeuristicIdx = (Integer)lsQualities.values().toArray()[lsHeuristics.length - i - 1];
			
			fitness[numSlots] = problem.applyHeuristic(lsHeuristics[curHeuristicIdx], workingSlot, numSlots);
			prevTimestamp = curTimestamp;
			curTimestamp = getElapsedTime();
			if (hasTimeExpired()) return;
			
			lsRuntimeSum[curHeuristicIdx] += (int)(curTimestamp - prevTimestamp + 1);
			lsGainSum[curHeuristicIdx] += (fitness[workingSlot] - fitness[numSlots]);
			lsUsageCount[curHeuristicIdx]++;
			
			if (fitness[numSlots] < fitness[workingSlot] || (fitness[workingSlot] == fitness[numSlots] && !buffer.contains(numSlots, fitness[numSlots]))) {
				lsSuccessCount[curHeuristicIdx]++;
				problem.copySolution(numSlots, workingSlot);
				fitness[workingSlot] = fitness[numSlots];
				if (fitness[numSlots] < fitness[numSlots + 1]) {
					problem.copySolution(numSlots, numSlots + 1);
					fitness[numSlots + 1] = fitness[numSlots];
				}
				buffer.add(workingSlot, fitness[workingSlot]);
				i = 0;
			}
			
			if (curTimestamp > lastQualityUpdate + qualityUpdateInterval) {
				updateQualities();
			}
		}
	}
	
	private void updateQualities() {
		lastQualityUpdate = curTimestamp;
		
		lsQualities.clear();
		for (int i = 0; i < lsHeuristics.length; i++) {
			double quality = lsSuccessCount[i] / (double)lsRuntimeSum[i];
			while (lsQualities.containsKey(quality)) {
				quality = nextAfter(quality, Double.NEGATIVE_INFINITY);
			}
			lsQualities.put(quality, i);		
		}
	}
	
	private void generateMutations() {
		for (int i = 0; i < numMutants; i++) {
			int mutationHeuristicIdx = 0;
			double sum = 0.0;
			for (int j = 0; j < muHeuristics.length; j++) {
				sum += muGainSum[j];
			}
			
			double rouletteBall = rng.nextDouble() * sum;
			sum = 0.0;
			for (int j = 0; j < muHeuristics.length; j++) {
				sum += muGainSum[j];
				if (sum > rouletteBall) {
					mutationHeuristicIdx = j;
					break;
				}
			}
			problem.setIntensityOfMutation(rng.nextDouble() * (1.0 - (double)curTimestamp / (double)timeLimit));
			
			fitness[i] = problem.applyHeuristic(muHeuristics[mutationHeuristicIdx], 0, i);
							
			muHeuristicIndices[i] = mutationHeuristicIdx;
		}
	}
	
	private void parallelSearch() {
		for (int i = 0; i < numMutants; i++) {
			workingHeuristicIdx[i] = 0;
		}
		boolean candidatesLeft;

		do {
			candidatesLeft = false;
			curTimestamp = getElapsedTime();						
			for (int i = 0; i < numMutants; i++) {
				if (workingHeuristicIdx[i] < lsHeuristics.length) {
					int curHeuristicIdx = (Integer)lsQualities.values().toArray()[lsHeuristics.length - workingHeuristicIdx[i] - 1];

					problem.setDepthOfSearch(rng.nextDouble());
					
					fitness[numSlots] = problem.applyHeuristic(lsHeuristics[curHeuristicIdx], i, numSlots);
					prevTimestamp = curTimestamp;
					curTimestamp = getElapsedTime();						
					if (hasTimeExpired()) return;
					
					lsRuntimeSum[curHeuristicIdx] += (int)(curTimestamp - prevTimestamp + 1);
					lsGainSum[curHeuristicIdx] += (fitness[numSlots] - fitness[i]);
					lsUsageCount[curHeuristicIdx]++;

					if (curTimestamp > lastQualityUpdate + qualityUpdateInterval) {
						updateQualities();
					}
					
					if (fitness[numSlots] >= fitness[i]) {
						workingHeuristicIdx[i]++;
					} else if (fitness[numSlots] < fitness[0]) {
						lsSuccessCount[curHeuristicIdx]++;
						problem.copySolution(numSlots, 0);
						fitness[0] = fitness[numSlots];
						
						if (fitness[numSlots] < fitness[numSlots+1]) {
							problem.copySolution(numSlots, numSlots + 1);
							fitness[numSlots + 1] = fitness[numSlots];
						}
						
						if (i > 0) {
							muGainSum[muHeuristicIndices[i]] += (1.0 / fitness[i]);
							muGainSum[muHeuristicIndices[i]] *= 1.1;
						}
						candidatesLeft = false;
						break;
					} else {
						lsSuccessCount[curHeuristicIdx]++;
						problem.copySolution(numSlots, i);
						fitness[i] = fitness[numSlots];
						workingHeuristicIdx[i] = 0;
						candidatesLeft = true;
					}
				}
				if (hasTimeExpired()) return;
			}
		} while (candidatesLeft);
		for (int i = 1; i < numMutants; i++) {
			muGainSum[muHeuristicIndices[i]] += (1.0 / fitness[i]);
		}
	}
	
	private void selectWorkingSolution() {
		int selectedMutant = 0;
		TreeMap<Double, Integer> mutantCandidates = new TreeMap<Double, Integer>();
		for (int i = 1; i < numMutants; i++) {
			if (!buffer.contains(i, fitness[i])) {
				mutantCandidates.put(fitness[i], i);
			}
		}

		for (int i = 0; i < mutantCandidates.size(); i++) {
			if (rng.nextDouble() < 0.8) {
				selectedMutant = mutantCandidates.get(mutantCandidates.keySet().toArray()[i]);
				break;
			}
		}

		if (selectedMutant != 0) {
			problem.copySolution(selectedMutant, 0);
		} else {
			problem.setIntensityOfMutation(rng.nextDouble() * (1.0 - (double)getElapsedTime() / (double)getTimeLimit()));			
			problem.applyHeuristic(muHeuristics[rng.nextInt(muHeuristics.length)], 0, 0);
		}
	}
	
	public double nextAfter(double d, double direction) {
	      if (Double.isInfinite(d) || Double.isNaN(d)) {
	    	  return d;
	      } else if (d == 0) {
	    	  return (direction < 0) ? -Double.MIN_VALUE : Double.MIN_VALUE;
	      }
	      long bits = Double.doubleToLongBits(d);
	      long sign = bits & 0x8000000000000000L;
	      long exponent = bits & 0x7ff0000000000000L;
	      long mantissa = bits & 0x000fffffffffffffL;

	      if (d * (direction - d) >= 0) {
	              if (mantissa == 0x000fffffffffffffL) {
	                      return Double.longBitsToDouble(sign |
	                                      (exponent + 0x0010000000000000L));
	              } else {
	                      return Double.longBitsToDouble(sign |
	                                      exponent | (mantissa + 1));
	              }
	      } else {
	              if (mantissa == 0L) {
	                      return Double.longBitsToDouble(sign |
	                                      (exponent - 0x0010000000000000L) |
	                                      0x000fffffffffffffL);
	              } else {
	                      return Double.longBitsToDouble(sign |
	                                      exponent | (mantissa - 1));
	              }
	      }

	  }
	
	public String toString() {
		return getClass().getSimpleName();
	}
	
	private interface SolutionBuffer {
		public void add(int idx, double fitness);
		public boolean contains(int idx, double fitness);
	}
	
	private class HashSolutionBuffer implements SolutionBuffer {
		final int bufferStart;
		final int bufferSize;
		boolean bufferFull = false;
		int bufferHead;
		HashMap<Double, HashSet<Integer>> bufferMap;
		HashSet<Integer>[] bufferHashsets;
		
		@SuppressWarnings("unchecked")
		public HashSolutionBuffer(int bufferStart, int bufferSize) {
			this.bufferStart = bufferStart;
			this.bufferSize = bufferSize;
			this.bufferHead = bufferStart;
			this.bufferMap = new HashMap<Double, HashSet<Integer>>();
			this.bufferHashsets = new HashSet[bufferSize];
		}
		
		public void add(int idx, double fitness) {
			problem.copySolution(idx, bufferHead);

			if (bufferFull) {
				bufferHashsets[bufferHead - bufferStart].remove(bufferHead - bufferStart);
			}

			HashSet<Integer> h = null;
			if (!bufferMap.containsKey(fitness)) {
				h = new LinkedHashSet<Integer>();
				bufferMap.put(fitness, h);
			} else {
				h = bufferMap.get(fitness);
			}
			h.add(idx);
			bufferHashsets[bufferHead - bufferStart] = h;
			
			bufferHead++;
			if (bufferHead == (bufferStart + bufferSize)) {
				bufferHead = bufferStart;
			}
		}
		
		public boolean contains(int idx, double fitness) {
			if (!bufferMap.containsKey(fitness))
				return false;
			for (Iterator<Integer> i = bufferMap.get(fitness).iterator(); i.hasNext(); ) {
				if (problem.compareSolutions(idx, i.next())) {
					return true;
				}
			}
			return false;
		}
	}
	
	private class LinearSolutionBuffer implements SolutionBuffer {
		final int bufferStart;
		final int bufferSize;
		boolean bufferFull = false;
		int bufferHead;
		
		public LinearSolutionBuffer(int bufferStart, int bufferSize) {
			this.bufferStart = bufferStart;
			this.bufferSize = bufferSize;
			this.bufferHead = bufferStart;
		}
		
		public void add(int idx, double fitness) {
			problem.copySolution(idx, bufferHead);
			bufferHead++;
			if (bufferHead == (bufferStart + bufferSize)) {
				bufferHead = bufferStart;
				bufferFull = true;
			}
		}
		
		public boolean contains(int idx, double fitness) {
			if (bufferFull) {
				for (int i = 0; i < bufferSize; i++) {
					int x = bufferHead - i - 1;
					if (x < bufferStart) {
						x += bufferSize;
					}
				
					if (problem.compareSolutions(idx, x)) {
						return true;
					}
				}
			} else if (bufferHead > bufferStart){
				for (int i = bufferHead - 1; i >= bufferStart; i--) {
					if (problem.compareSolutions(idx, i)) {
						return true;
					}
				}
			}
			return false; 
		}		
	}
}