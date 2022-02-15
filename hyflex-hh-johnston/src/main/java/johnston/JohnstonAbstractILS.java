package johnston;

/**
 * edited by Dave Omrai
 */

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

public abstract class JohnstonAbstractILS extends HyperHeuristic {

	protected ProblemDomain problem;
	protected int[] localSearchHeuristics;
	protected int[] perturbationHeuristics;
	protected double currentObjectiveFunctionValue;
	protected double newObjectiveFunctionValue;
	protected double delta;
	protected int numPerturbationStrengths;
	protected boolean accepted;

	public JohnstonAbstractILS() {
		super();
		numPerturbationStrengths = 20;
	}

	public JohnstonAbstractILS(long seed) {
		this(seed, 20);
	}
	
	public JohnstonAbstractILS(long seed, int nPerturbationStrengths) {
		super(seed);
		numPerturbationStrengths = nPerturbationStrengths;
	}

	private void getHeuristics() {
		//obtain arrays of the indices of the low level heuristics which correspond to the different types.
		//the arrays will be set to 'null' if there are no low level heuristics of that type
		localSearchHeuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
		if (localSearchHeuristics == null)
			localSearchHeuristics = new int[0];
		
		int[] mutationHeuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
		if (mutationHeuristics == null)
			mutationHeuristics = new int[0];
		int[] ruinRecreateHeuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
		if (ruinRecreateHeuristics == null)
			ruinRecreateHeuristics = new int[0];
		perturbationHeuristics = new int[mutationHeuristics.length+ruinRecreateHeuristics.length];
		System.arraycopy(    mutationHeuristics, 0, perturbationHeuristics,                         0,     mutationHeuristics.length);
		System.arraycopy(ruinRecreateHeuristics, 0, perturbationHeuristics, mutationHeuristics.length, ruinRecreateHeuristics.length);
	}

	protected void doLocalSearch(int index) {
		for (int i = 0; i < localSearchHeuristics.length; i++) {
			problem.applyHeuristic(localSearchHeuristics[i], index, index);
		}
	}

	@Override
	protected void solve(ProblemDomain prob) {
		problem = prob;
		
		getHeuristics();
	
		//initialise the variable which keeps track of the current objective function value
		currentObjectiveFunctionValue = Double.POSITIVE_INFINITY;
	
		//initialise the solution at index 0 in the solution memory array
		problem.initialiseSolution(0);
		doLocalSearch(0);
			
		//initialise the variable which keeps track of the current objective function value
		currentObjectiveFunctionValue = problem.getFunctionValue(0);

		if (hasTimeExpired()) { return; }
			
		initialiseData();
	
		//the main loop of any hyper-heuristic, which checks if the time limit has been reached
		while (!hasTimeExpired()) {
	
			int heuristicToApply = selectPerturbation();
			int perturbationStrength = selectedPerturbationStrength();
			
			kickAndClimb(heuristicToApply, perturbationStrength);
			
			newObjectiveFunctionValue = problem.getFunctionValue(1);
				
			//calculate the change in fitness from the current solution to the new solution
			delta = newObjectiveFunctionValue - currentObjectiveFunctionValue;
	
			//all of the problem domains are implemented as minimisation problems. A lower fitness means a better solution.
			accepted = delta < 0;
			if (accepted) {
				//if there is an improvement then we 'accept' the solution by copying the new solution into memory index 0
				problem.copySolution(1, 0);
				//we also set the current objective function value to the new function value, as the new solution is now the current solution
				currentObjectiveFunctionValue = newObjectiveFunctionValue;
			}
	//		else {
	//			//if there is not an improvement in solution quality then we accept the solution with a 50% probability
	//			if (rng.nextBoolean()) {
	//				//the process for 'accepting' a solution is the same as above
	//				problem.copySolution(1, 0);
	//				currentObjectiveFunctionValue = newObjectiveFunctionValue;
	//			}
	//		}
				
			updateData();
				
			//one iteration has been completed, so we return to the start of the main loop and check if the time has expired 
		}
			
	}
	
	protected void kickAndClimb(int heuristicToApply, int perturbationStrength) {
		if (perturbationStrength > -1)
			problem.setIntensityOfMutation(((double) (perturbationStrength+1)) / numPerturbationStrengths);

		//apply the chosen heuristic to the solution at index 0 in the memory
		//the new solution is then stored at index 1 of the solution memory while we decide whether to accept it
		problem.applyHeuristic(heuristicToApply, 0, 1);

		// do a local search
		doLocalSearch(1);
	}
	
	abstract protected int selectPerturbation();
	
	abstract protected int selectedPerturbationStrength();
	
	abstract protected void initialiseData();
		// what to do to initialise stored information
	
	abstract protected void updateData();
		// what to do to update stored information

}
