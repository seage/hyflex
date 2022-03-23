package AbstractClasses;
import java.util.Random;

/**
 * Problem domain is an abstract class containing methods
 * for applying heuristics and managing solutions. These methods are used by
 * a hyper-heuristic in order to progress the search.
 * 
 * Sub-classes of ProblemDomain provide the representations of the various problem
 * domains for the competition.
 * @author Antonio Vázquez, Matthew Hyde, Gabriela Ochoa, Tim Curtois. [jav,mvh,gxo,tec]@cs.nott.ac.uk
 */
public abstract class ProblemDomain {

	/**
	 * The random number generator used by the problem domain object. It is initialised in the constructor.
	 */
	protected Random rng;
	
	/**
	 * A parameter specifying the extent to which a local search heuristic will
	 * modify the solution. It could mean the size of the neighbourhood that is sampled by the heuristic, or
	 * the length of time that a low level heuristic runs for in order to improve the solution.
	 * The meaning of this variable is intentionally vaguely stated, as it depends 
	 * on the heuristic in question, and the problem domain in question.
	 */
	protected double depthOfSearch = 0.2;
	
	/**
	 * A parameter specifying the extent to which a mutation or ruin-recreate low level heuristic
	 * will mutate the solution. For a mutation heuristic, this could mean the range of new values that
	 * a variable can take, in relation to its current value. It could mean how many variables are changed by 
	 * one call to the heuristic. For a ruin-recreate heuristic, it could mean the percentage of the solution 
	 * that is destroyed and rebuilt. The meaning of this variable is intentionally vaguely stated, as it depends 
	 * on the heuristic in question, and the problem domain in question.
	 */
	protected double intensityOfMutation = 0.2;
	
	/**
	 * A record of the number of times that each low level heuristic has been called. Can be retrieved by a HyperHeuristic
	 * object by calling the getHeuristicCallRecord() method of this class.
	 */
	protected int[] heuristicCallRecord;
	
	/**
	 * A record of the length of time that each low level heuristic has been running for. Can be retrieved by a HyperHeuristic
	 * object by calling the getHeuristicCallTimeRecord() method of this class.
	 */
	protected int[] heuristicCallTimeRecord;
	
	/**
	 * An enumeration of the different types of low-level heuristics implemented in 
	 * the software. The set of all heuristics of a certain type can be obtained through
	 * methods in the ProblemDomain class.
	 */
	public enum HeuristicType
	{
		/**
		 * A heuristic which randomly mutates a solution.
		 */
		MUTATION,

		/**
		 * A heuristic which combines two solutions to produce a one solution.
		 */
		CROSSOVER,

		/**
		 * A perturbation type heuristic which ruins part of
		 * a solution and then attempts to recreate/repair it.
		 */
		RUIN_RECREATE,

		/**
		 * A local search heuristic. The resulting solution
		 * after applying a heuristic of this type will have an
		 * objective function value the same as or better than
		 * the value of the initial solution (that is, it will not
		 * be worse).
		 */
		LOCAL_SEARCH,

		/**
		 * A heuristic which does not fall into any of the other
		 * categories.
		 */
		OTHER
	}

	/**
	 * Creates a new problem domain and creates
	 * a new random number generator using the seed provided. If the seed
	 * takes the value -1, the seed is generated taking the current
	 * System time. The random number generator is used for all stochastic operations,
	 * so the problem will be initialised in the same way if the seed is the same.
	 *
	 * Sets the solution memory size to 2.
	 * @param seed a random seed
	 */
	public ProblemDomain(long seed)
	{
		heuristicCallRecord = new int[this.getNumberOfHeuristics()];
		heuristicCallTimeRecord = new int[this.getNumberOfHeuristics()];
		if(seed == -1) {
			this.rng = new Random();
		} else {
			this.rng = new Random(seed);
		}
		this.setMemorySize(2);
		setDepthOfSearch(this.depthOfSearch);
		setIntensityOfMutation(this.intensityOfMutation);
	}

	/**
	 * Shows how many times each low level heuristic has been called.
	 * @return an int[] which contains an integer value for each low level heuristic,
	 * representing the number of times that heuristic has been called by the HyperHeuristic object.
	 */
	public int[] getHeuristicCallRecord() {
		return heuristicCallRecord;
	}

	/**
	 * Shows the total time that each low level heuristic has been operating on the problem.
	 * @return an int[] which contains an integer value representing the total number of 
	 * milliseconds used by each low level heuristic.
	 */
	public int[] getheuristicCallTimeRecord() {
		return heuristicCallTimeRecord;
	}

	/** 
	 * Sets the parameter specifying the extent to which a local search heuristic will
	 * modify the solution. This parameter is related to the number of improving steps to be completed by the local search heuristics.
	 * 
	 * @param depthOfSearch must be in the range 0 to 1. The initial value of 0.1 represents
	 * the default operation of the low level heuristic.
	 */
	public void setDepthOfSearch(double depthOfSearch)
	{
		this.depthOfSearch = depthOfSearch;
		if (this.depthOfSearch < 0)
			this.depthOfSearch = 0;
		else if (this.depthOfSearch > 1)
			this.depthOfSearch = 1;
	}

	/** 
	 * Sets the parameter specifying the extent to which a mutation or ruin-recreate low level heuristic
	 * will mutate the solution. For a mutation heuristic, this could mean the range of new values that
	 * a variable can take, in relation to its current value. It could mean how many variables are changed by 
	 * one call to the heuristic. For a ruin-recreate heuristic, it could mean the percentage of the solution 
	 * that is destroyed and rebuilt. For example, a value of 0.5 may indicate that
	 * half the solution will be rebuilt by a RUIN_RECREATE heuristic. The meaning of this variable is intentionally vaguely stated, as it depends 
	 * on the heuristic in question, and the problem domain in question.
	 * 
	 * @param intensityOfMutation must be in the range 0 to 1. The initial value of 0.1 represents
	 * the default operation of the low level heuristic.
	 */
	public void setIntensityOfMutation(double intensityOfMutation)
	{
		this.intensityOfMutation = intensityOfMutation;
		if (this.intensityOfMutation < 0)
			this.intensityOfMutation = 0;
		else if (this.intensityOfMutation > 1)
			this.intensityOfMutation = 1;
	}

	/**
	 * Gets the current value of the "depth of search" parameter.
	 * @return The current value of the depth of search parameter.
	 */
	public double getDepthOfSearch()
	{
		return depthOfSearch;
	}

	/**
	 * Gets the current intensity of mutation parameter.
	 * @return the current value of the intensity of mutation parameter.
	 */
	public double getIntensityOfMutation()
	{
		return intensityOfMutation;
	}

	/**
	 * Gets an array of heuristicIDs of the type specified by heuristicType.
	 * @param heuristicType the heuristic type.
	 * @return An array containing the indices of the heuristics
	 * of the type specified. If there are no heuristics of this type
	 * it returns null.
	 */
	public abstract int[] getHeuristicsOfType(HeuristicType heuristicType);
	/**
	 * Gets an array of heuristicIDs that use the intensityOfMutation parameter
	 * @return An array containing the indexes of the heuristics
	 * that use the intensityOfMutation parameter, or null if there are no
	 * heuristics of this type.
	 */
	public abstract int[] getHeuristicsThatUseIntensityOfMutation();
	/**
	 * Gets an array of heuristicIDs that use the depthOfSearch parameter
	 * @return An array containing the indexes of the heuristics
	 * that use the depthOfSearch parameter, or null if there are no
	 * heuristics of this type.
	 */    
	public abstract int[] getHeuristicsThatUseDepthOfSearch();
	 /**
	  * Loads the instance specified by instanceID.
	  * @param instanceID Specifies the instance to load. The ID's
	  * start at zero.
	  */
	 public abstract void loadInstance(int instanceID);

	 /**
	  * Sets the size of the array where the solutions are stored. The default size is 2.
	  * @param size The new size of the solution array.
	  */
	 public abstract void setMemorySize(int size);

	 /**
	  * Create an initial solution at a specified position in the memory array. The method of initialising the solution depends on the 
	  * specific problem domain, but it is a random process, which will produce a different solution each time. The initialisation process
	  * may randomise all of the elements of the problem, or it may use a constructive heuristic with a randomised input.
	  * @param index The index of the memory array at which the solution should be initialised.
	  */
	 public abstract void initialiseSolution(int index);

	 /**
	  * Gets the number of heuristics available in this problem domain
	  * @return The number of heuristics available in this problem domain
	  */
	 public abstract int getNumberOfHeuristics();

	 /**
	  * Applies the heuristic specified by heuristicID to the solution at
	  * position solutionSourceIndex and places the resulting solution at
	  * position solutionDestinationIndex in the solution array. If the
	  * heuristic is a CROSSOVER type then the solution at solutionSourceIndex
	  * is just copied to solutionDestinationIndex.
	  * @param heuristicID The ID of the heuristic to apply (starts at zero)
	  * @param solutionSourceIndex The index of the solution in the memory array to which to apply the heuristic
	  * @param solutionDestinationIndex The index in the memory array at which 
	  * to store the resulting solution
	  * @return the objective function value of the solution created by
	  * applying the heuristic
	  */
	 public abstract double applyHeuristic(int heuristicID,
			 int solutionSourceIndex,
			 int solutionDestinationIndex);

	 /**
	  * Apply the heuristic specified by heuristicID to the solutions at
	  * position solutionSourceIndex1 and position solutionSourceIndex2 and put
	  * the resulting solution at position solutionDestinationIndex.
	  * The heuristic can be of any type (including CROSSOVER). If a non-CROSSOVER type
	  * heuristic is chosen, the heuristic is applied to the solution at solutionSourceIndex1
	  * only, and solutionSourceIndex2 is not used.
	  * @param heuristicID the heuristic to apply (starts at zero)
	  * @param solutionSourceIndex1
	  * @param solutionSourceIndex2
	  * @param solutionDestinationIndex the position to store the resulting
	  * solutions at
	  * @return the objective function value of the new solution created by
	  * applying the heuristic
	  */
	 public abstract double applyHeuristic(int heuristicID, int solutionSourceIndex1,
			 int solutionSourceIndex2, int solutionDestinationIndex);

	 /**
	  * Copies a solution from one position in the solution array to another
	  * @param solutionSourceIndex The position of the solution to copy
	  * @param solutionDestinationIndex The position in the array to copy the
	  * solution to.
	  */
	 public abstract void copySolution(int solutionSourceIndex,
			 int solutionDestinationIndex);

	 /**
	  * Gets the name of the problem domain. For example, "Bin Packing"
	  * @return the name of the ProblemDomain
	  */
	 public abstract String toString();

	 /**
	  * Gets the number of instances available in this problem domain
	  * @return the number of instances available
	  */
	 public abstract int getNumberOfInstances();

	 /**
	  * Gets a String representation of the best solution found so far by the HyperHeuristic. 
	  * This is useful if the solution needs to be printed to the screen, or saved to a file.
	  * @return A String representation of the best solution found
	  */
	 public abstract String bestSolutionToString();

	 /**
	  * Returns the objective function value of the best solution found so far by the HyperHeuristic.
	  * @return The objective function value of the best solution.
	  */
	 public abstract double getBestSolutionValue();

	 /**
	  * Gets a String representation of a given solution in memory
	  * @param solutionIndex The index of the solution of which a String representation is required
	  * @return A String representation of the solution at solutionIndex in the solution memory
	  */
	 public abstract String solutionToString(int solutionIndex);

	 /**
	  * Gets the objective function value of the solution at index solutionIndex
	  * @param solutionIndex The index of the solution from which the objective function is required
	  * @return A double value of the solution's objective function value.
	  */
	 public abstract double getFunctionValue(int solutionIndex);

	 /**
	  * Compares the two solutions for equality, based on their structure, not just their fitness. This is more time consuming, but is necessary, as if the solutions have the same fitness they are not necessarily the same solution. 
	  * @param solutionIndex1
	  * @param solutionIndex2
	  * @return true if the solutions are identical, false otherwise.
	  */
	 public abstract boolean compareSolutions(int solutionIndex1, int solutionIndex2);
} 