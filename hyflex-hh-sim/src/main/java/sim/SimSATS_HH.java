package sim;
import java.util.ArrayList;
import java.util.HashMap;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import AbstractClasses.ProblemDomain.HeuristicType;

/**
 * 
 * @author Kevin Sim
 * @email k.sim@napier.ac.uk
 * @version 1.00 June, 2011
 * @description This class extends the abstract class {@link HyperHeuristic} and
 *              was developed for entry to the CHeSC competition hosted by the
 *              Automated Scheduling, Optimisation and Planning (ASAP) group at
 *              the University of Nottingham.
 *              <p>
 *              The implementation uses a self adjusting Simulated Annealing
 *              approach with features of tabu-search and reinforcement learning
 *              incorporated.
 *              <p>
 *              The hyper heuristic conducts a single point search by selecting
 *              a heuristic and then deciding whether to keep the resultant
 *              solution.
 *              <p>
 *              Heuristics are selected from those not in the tabu list using a
 *              competition between two random participants with the winner
 *              determined by the heuristics fitness.
 *              <p>
 *              The fitness of a heuristic is incremented each time that it
 *              produces a non deteriorating solution and decremented otherwise.
 *              Heuristics that do not produce better solutions are added to a
 *              tabu list for 7 generations.
 *              <p>
 *              Better or equally good solutions are accepted always with worse
 *              solutions accepted with a probability that reduces over time
 *              using the SA paradigm.
 *              <p>
 *              In order for the approach to work across different domains the
 *              change in fitness value used to determine the probability of
 *              accepting a worse solution is taken as the percentage change
 *              from the previous solution.
 *              <p>
 *              The cooling schedule is calculated to allow it to fall to 0
 *              within the time limit for each problem instance. The algorithm
 *              estimates the number of possible iterations within the time
 *              limit and if no improvement is achieved within a percentage of
 *              this estimate the algorithm's parameters are reset to allow for
 *              a broader search.
 */
public class SimSATS_HH extends HyperHeuristic {

	/**
	 * creates a new HyperHeuristic object with the specified seed
	 * 
	 * @param seed
	 *            The seed used for the random number generator.
	 */
	public SimSATS_HH(long seed) {
		super(seed);
	}

	/**
	 * 
	 * @param problem
	 *            the problem domain to be solved
	 */

	public void solve(ProblemDomain problem) {

		// a random number in (0..1)
		double r = 0;
		// The change in fitness value
		double c = 0;
		// The temperature
		double t = 0;
		// The result of the SA equation to determine the acceptance probability
		// (accepted if > r)
		double probability = 0;

		// get the number of heuristics in the domain
		int numHeuristics = problem.getNumberOfHeuristics();

		// stores the current best value.
		double best_solution_value;

		// stores the potential new solution's fitness
		double newSolutionValue;

		// initialise the problem memory and obtain the initial fitness
		problem.initialiseSolution(0);
		best_solution_value = problem.getFunctionValue(0);

		// monitors the number of items used to calculate avg
		int count = 0;

		// the average percentage change in fitness value of proposed solutions
		double avg = 0;

		// factor and cooling combined provide the cooling (could change to one
		// variable). These are set after n iterations have taken place (15)
		double factor = 1;
		double cooling = 0.99;

		// amount of iterations since an improvement was seen
		int iterNoImprovement = 0;

		double startTime = System.currentTimeMillis();

		// used in calculating the average time taken for an iteration
		double iterTime = 0;

		// a ceiling on the number of iterations allowed without improvement.
		// This is recalculated as for the cooling after n iterations.
		int iterNoImprovementLimit = 1000;

		// the current iteration
		int iter = 0;

		// ignore crossover heuristics as only a single point search implemented
		ArrayList<Integer> crossoverHeuristics = new ArrayList<Integer>();
		int[] temp = problem.getHeuristicsOfType(HeuristicType.CROSSOVER);
		for (int e : temp) {
			crossoverHeuristics.add(e);
		}

		// The allowed set of heuristics to select from. At the start this is
		// all but the crossover heuristics but is updated by removing any
		// heuristics added or adding any released from the tabu list.
		ArrayList<Integer> allowedHeuristics = new ArrayList<Integer>();
		for (int i = 0; i < numHeuristics; i++) {
			if (!crossoverHeuristics.contains(i)) {
				allowedHeuristics.add(i);
			}
		}

		// the list of tabu heuristics
		ArrayList<Integer> tabuHeuristics = new ArrayList<Integer>();

		// records the number of iterations heuristic has been tabu
		ArrayList<Integer> tabuCount = new ArrayList<Integer>();

		// Used to release heuristics back into the allowed set of heuristics
		ArrayList<Integer> releasedHeuristics = new ArrayList<Integer>();

		// Maps heuristics to their fitness
		HashMap<Integer, Integer> heuristicFitnesses = new HashMap<Integer, Integer>();
		for (int i = 0; i < allowedHeuristics.size(); i++) {
			heuristicFitnesses.put(allowedHeuristics.get(i), 10);
		}

		/**
		 * The main algorithm loop
		 */
		while (!hasTimeExpired()) {

			numHeuristics = allowedHeuristics.size();
			if (numHeuristics > 0) {
				// perform a competition between two random allowed heuristics.
				int contender1Idx = rng.nextInt(numHeuristics);
				int contender2Idx = -1;
				if (numHeuristics > 1) {
					contender2Idx = contender1Idx;
				}
				while (contender2Idx == contender1Idx) {
					contender2Idx = rng.nextInt(numHeuristics);
				}
				int heuristicToApplyIdx = contender1Idx;
				if (contender2Idx != -1
						&& heuristicFitnesses.get(allowedHeuristics
								.get(contender2Idx)) > heuristicFitnesses
								.get(allowedHeuristics.get(contender1Idx))) {
					heuristicToApplyIdx = contender2Idx;
				}

				int heuristic_to_apply = allowedHeuristics
						.get(heuristicToApplyIdx);

				// apply the heuristic chosen
				problem.applyHeuristic(heuristic_to_apply, 0, 1);
				newSolutionValue = problem.getFunctionValue(1);

				// calculate the change in fitness
				c = newSolutionValue - best_solution_value;

				// if the new solution is equal or better
				if (c <= 0) {

					// sideways moves are allowed but the heuristic is added to
					// the tabu list
					if (c == 0) {
						tabuHeuristics.add(heuristic_to_apply);
						allowedHeuristics.remove(heuristicToApplyIdx);
						tabuCount.add(0);
					} else {
						// only when improving reset iterNoImprovement
						iterNoImprovement = 0;
					}

					// increment the heuristics fitness;
					int heuristicFitness = heuristicFitnesses
							.get(heuristic_to_apply);
					heuristicFitness++;
					if (heuristicFitness > 100) {
						heuristicFitness = 100;
					}
					heuristicFitnesses
							.put(heuristic_to_apply, heuristicFitness);

					// store the improved (or equal) solution value
					best_solution_value = newSolutionValue;
					problem.copySolution(1, 0);
				} else {
					// use the SA strategy to determine if the solution should
					// be accepted

					// lower the heuristic fitness
					int heuristicFitness = heuristicFitnesses
							.get(heuristic_to_apply);
					heuristicFitness--;
					if (heuristicFitness < 10) {
						heuristicFitness = 10;
					}
					heuristicFitnesses
							.put(heuristic_to_apply, heuristicFitness);

					// add the heuristic to the tabu list
					tabuHeuristics.add(heuristic_to_apply);
					allowedHeuristics.remove(heuristicToApplyIdx);

					// set the number of iterations the heuristic has been in
					// the tabulist to 0
					tabuCount.add(0);
					iterNoImprovement++;

					// decide if to select worse solution. Changing c to a
					// percentage hopefully allows different domains with
					// variable scale fitness measures to be implemented
					// similarly.
					c = (c / best_solution_value) * 100;

					// resets the average every n iterations
					if (count % iterNoImprovementLimit == 0) {
						count = 0;
						avg = 0;
					}
					count++;
					// calculate the average % change in worse solutions
					// encountered
					avg = ((avg * (count - 1)) + c) / count;

					// factor -= cooling;
					factor *= cooling;
					t = avg * factor;

					// ensures that t doesn't go negative and that large
					if (t < 0) {
						t = 0;
					}

					probability = Math.exp(-c / t);
					r = rng.nextDouble();

					// accept the solution
					if (probability > r) {
						best_solution_value = newSolutionValue;
						problem.copySolution(1, 0);
					}
				}
			}

			// resets the algorithm if no improvement for iterNoImprovementLimit
			// iterations
			if (iterNoImprovement >= iterNoImprovementLimit) {
				factor = 0.5;
				avg = c;
				count = 1;
				iterNoImprovement = 0;
			}

			// increment the number of iterations the tabu heuristics are in the
			// list and release any that have been there for >= 7 iterations
			releasedHeuristics.clear();
			for (int i = 0; i < tabuCount.size(); i++) {
				int tCount = tabuCount.get(i);
				tCount++;
				if (tCount > 6) {
					releasedHeuristics.add(i);
				} else {
					tabuCount.set(i, tCount);
				}
			}
			for (int e : releasedHeuristics) {
				tabuCount.remove(e);
				allowedHeuristics.add(tabuHeuristics.get(e));
				tabuHeuristics.remove(e);
			}

			iter++;

			// set the cooling schedule and iteration with no improvement limit
			// The figure here has a profound effect on the algorithm. n = 30
			// was best for SAT but poor on FS & PS. 15 was found to be a good
			// average
			if (iter == 15) {
				iterTime = System.currentTimeMillis() - startTime;
				iterNoImprovementLimit = (int) (getTimeLimit() / (1 * iterTime));
				// impose a limit on the number of iterations without improvemnt
				if (iterNoImprovementLimit > 5000) {
					iterNoImprovementLimit = 5000;
				}

				// determine a cooling schedule
				cooling = 1 - ((double) 1 / iterNoImprovementLimit);
			}
		}// end the main loop
	}

	/**
	 * this method must be implemented, to provide a different name for each
	 * hyper-heuristic
	 * 
	 * @return a string representing the name of the hyper-heuristic
	 */
	public String toString() {
		return getName();
	}

	public static String getName() {
		return SimSATS_HH.class.getSimpleName();
	}
}
