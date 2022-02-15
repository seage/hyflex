package johnston;

/**
 * edited by Dave Omrai
 */

public class JohnstonBiasILS extends JohnstonAbstractILS {
	
	private int[][] perturbationStrengthWeights;
	
	private int numNonImprovingPerturbations;
	
	private int selectedPerturbation;
	private int selectedPerturbationStrength;
	
	public JohnstonBiasILS(long seed) {
		this(seed, 20);
	}
	
	public JohnstonBiasILS(long seed, int nPerturbationStrengths) {
		super(seed, nPerturbationStrengths);
	}
	
	protected int selectPerturbation() {
		int[][] normalisedWeights = new int[perturbationHeuristics.length][numPerturbationStrengths];
		int minWeight = Integer.MAX_VALUE;
		int maxWeight = Integer.MIN_VALUE;
		for (int i = 0; i < perturbationHeuristics.length; i++) {
			for (int j = 0; j < numPerturbationStrengths; j++) {
				if (perturbationStrengthWeights[i][j] < minWeight)
					minWeight = perturbationStrengthWeights[i][j];
				if (perturbationStrengthWeights[i][j] > maxWeight)
					maxWeight = perturbationStrengthWeights[i][j];
			}
		}
		int weightRange = maxWeight - minWeight;
		if (minWeight < 0)
			for (int i = 0; i < perturbationHeuristics.length; i++)
				for (int j = 0; j < numPerturbationStrengths; j++)
					normalisedWeights[i][j] = perturbationStrengthWeights[i][j] - minWeight;
		else
			for (int i = 0; i < perturbationHeuristics.length; i++)
				for (int j = 0; j < numPerturbationStrengths; j++)
					normalisedWeights[i][j] = perturbationStrengthWeights[i][j];
		int normalisationIncrement = weightRange / numPerturbationStrengths;
		if (normalisationIncrement < 1)
			normalisationIncrement = 1;
		int totalWeight = 0;
		for (int i = 0; i < perturbationHeuristics.length; i++) {
			for (int j = 0; j < numPerturbationStrengths; j++) {
				int nonImprovementBias = numNonImprovingPerturbations * j;
				normalisedWeights[i][j] += normalisationIncrement + nonImprovementBias;
				totalWeight += normalisedWeights[i][j];
			}
		}
		int rand = rng.nextInt(totalWeight);
		int cumulativeWeight = 0;
		for (int i = 0; i < perturbationHeuristics.length; i++) {
			for (int j = 0; j < numPerturbationStrengths; j++) {
				cumulativeWeight += normalisedWeights[i][j];
				if (rand < cumulativeWeight) {
					selectedPerturbation = i;
					selectedPerturbationStrength = j;
					return selectedPerturbation;
				}
			}
		}
		throw new RuntimeException("If we get here, selectedPerturbation and selectedPerturbationStrength have not been set");
	}
	
	protected int selectedPerturbationStrength() {
		return selectedPerturbationStrength;
	}
	
	protected void initialiseData() {
		perturbationStrengthWeights = new int[perturbationHeuristics.length][numPerturbationStrengths];
		
		// initially promote pertubationStrength 0.1 (the default)
		for (int i = 0; i < perturbationHeuristics.length; i++)
			perturbationStrengthWeights[i][numPerturbationStrengths/10 - 1] = numPerturbationStrengths;
		
		numNonImprovingPerturbations = 0;
	}
	
	protected void updateData() {
		double relativeImprovement = -delta / currentObjectiveFunctionValue;
		     // an improvement is a negative delta as problems are minimisation problems
		
		double weightUpdateFactor = 1000.0;
		perturbationStrengthWeights[selectedPerturbation][selectedPerturbationStrength] +=
			(int) (relativeImprovement * weightUpdateFactor);
		
		boolean identicalSolutions = problem.compareSolutions(0, 1);
		if (identicalSolutions) {
			// penalise all strengths less than or equal to the current one
			for (int j = 0; j <= selectedPerturbationStrength; j++)
				perturbationStrengthWeights[selectedPerturbation][j] -= 100;
		}
		
		if (accepted)
			numNonImprovingPerturbations = 0;
		else
			numNonImprovingPerturbations++;
	}

	@Override
	public String toString() {
		return "Dynamic Iterated Local Search With Non Improvement Bias";
	}

}
