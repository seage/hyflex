package chesc.solutions;

import java.util.Vector;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

public class CSPUTGeneticHiveHyperHeuristic extends HyperHeuristic {

	public CSPUTGeneticHiveHyperHeuristic(long seed) {
		super(seed);
	}

	private int number_of_heuristics;
	private final int number_of_locations = 4;
	private final int population_size = 35;
	private final int agent_size = 7;
	private final double stay_fraction = 0.232273285445667;
	private final double children_fraction = 0.802697122884014;
	private final double mutation_probability = 0.145729164115365;

	public class HiveAgent {
		public Integer id;
		public Vector<Integer> code;
		public Integer location_index;
		public Double score;

		public HiveAgent(Integer id_src, Vector<Integer> code_src, Integer location_index_src) {
			id = id_src;
			code = code_src;
			location_index = location_index_src;
		}
	}

	private Vector<HiveAgent> allAgents;
	private Vector<HiveAgent> delegateAgents;

	private void evaluateAgents(Vector<HiveAgent> agents, ProblemDomain problem) {
		int new_location = number_of_locations;

		for (int i = 0; i < agents.size() && !hasTimeExpired(); i++) {
			int source = agents.get(i).location_index;
			Vector<Integer> code = agents.get(i).code;

			double r1 = problem.getFunctionValue(source);

			problem.applyHeuristic(code.get(0), source, new_location);

			for (int j = 1; j < code.size(); j++) {
				problem.applyHeuristic(code.get(j), new_location, new_location);
			}

			double r2 = problem.getFunctionValue(new_location);

			agents.get(i).location_index = new_location - number_of_locations;
			agents.get(i).score = (r1 - r2) / (r2 + 1); // relative score

			new_location++;
		}
		
		if (hasTimeExpired())
			return;

		for (int i = number_of_locations; i < 2 * number_of_locations; i++) {
			problem.copySolution(i, i - number_of_locations);
		}

		for (int i = 1; i < agents.size(); i++) {
			for (int j = i; j >= 1; j--) {
				if (agents.get(j).score > agents.get(j - 1).score) {
					HiveAgent tmp = agents.get(j);
					agents.set(j, agents.get(j - 1));
					agents.set(j - 1, tmp);
				}
			}
		}
	}

	private Vector<HiveAgent> selectStayAgents(Vector<HiveAgent> delegateAgents) {
		Vector<HiveAgent> result = new Vector<HiveAgent>();

		for (int k = 0; k < (stay_fraction * ((double) number_of_locations)); k++) {
			result.add(delegateAgents.get(k));
		}

		return result;
	}

	private void makePointMutations(Vector<Integer> v) {
		for (int i = 0; i < agent_size; i++) {
			int p = rng.nextInt(1000);
			if (p < mutation_probability * 1000)
				v.set(i, rng.nextInt(number_of_heuristics));
		}
	}

	private Vector<Vector<Integer>> makeCrossingOver(Vector<Integer> v1, Vector<Integer> v2) {
		int pos = rng.nextInt(agent_size);

		Vector<Integer> child1 = new Vector<Integer>();
		Vector<Integer> child2 = new Vector<Integer>();

		for (int i = 0; i <= pos; i++) {
			// we swap prefixes
			child1.add(i, v2.get(i));
			child2.add(i, v1.get(i));
		}

		for (int i = pos + 1; i < agent_size; i++) {
			child1.add(i, v1.get(i));
			child2.add(i, v2.get(i));
		}

		Vector<Vector<Integer>> result = new Vector<Vector<Integer>>();
		result.add(0, child1);
		result.add(1, child2);

		return result;
	}

	private int getIndexFromRoulette(Vector<Double> probabilities) {
		Double p = rng.nextDouble();
		Double sum = 0.0;

		for (int i = 0; i < probabilities.size(); i++) {
			if (p >= sum && p < sum + probabilities.get(i))
				return i;

			sum += probabilities.get(i);
		}

		return 0;
	}

	private Vector<Vector<Integer>> getChildren(Vector<Vector<Integer>> population, Vector<Double> population_scores, Integer set_size, Integer create_children) {
		
		
		Double min = population_scores.get(0);
		for (int i = 1; i < set_size; i++)
			if (population_scores.get(i) < min)
				min = population_scores.get(i);

		Double sum = 0.0;
		for (int i = 0; i < set_size; i++) {
			population_scores.set(i, population_scores.get(i) - min + 1.0 / set_size);
			sum += population_scores.get(i);
		}

		for (int i = 0; i < set_size; i++) {
			population_scores.set(i, population_scores.get(i) / sum);
		}

		Vector<Vector<Integer>> children = new Vector<Vector<Integer>>();

		for (int i = 1; i < set_size; i++) {
			for (int j = i; j >= 1; j--) {
				if (population_scores.get(j) > population_scores.get(j - 1)) {
					double tmp = population_scores.get(j);
					population_scores.set(j, population_scores.get(j - 1));
					population_scores.set(j - 1, tmp);

					Vector<Integer> tmpItem = population.get(j);
					population.set(j, population.get(j - 1));
					population.set(j - 1, tmpItem);
				}
			}
		}

		double sumAll = 0;
		for (int i = 0; i < set_size; i++) {
			population_scores.set(i, population_scores.get(i) * set_size - i);
			sumAll += population_scores.get(i);
		}

		for (int i = 0; i < set_size; i++) {
			population_scores.set(i, population_scores.get(i) / sum);
		}

		while (children.size() < create_children) {
			int index1 = getIndexFromRoulette(population_scores);
			int index2 = getIndexFromRoulette(population_scores);

			Vector<Vector<Integer>> children_pair = makeCrossingOver(population.get(index1), population.get(index2));
			index2++;
			makePointMutations(children_pair.get(0));
			makePointMutations(children_pair.get(1));

			Double r = rng.nextDouble();
			if (r >= 0.5)
				children.add(children_pair.get(0));
			else
				children.add(children_pair.get(1));
		}

		return children;
	}

	private Vector<HiveAgent> completeDelegateAgents(Vector<HiveAgent> allAgents, Vector<HiveAgent> delegateAgents, ProblemDomain problem) {
		Vector<HiveAgent> result = new Vector<HiveAgent>();
		for (int i = 0; i < delegateAgents.size(); i++)
			result.add(delegateAgents.get(i));
		Vector<Integer> locations = new Vector<Integer>();
		Vector<Double> probabilities = new Vector<Double>();
		double min = 0;
		double total = 0;

		Vector<HiveAgent> newAgents = new Vector<HiveAgent>();
		for (int i = 0; i < allAgents.size(); i++) {
			Boolean isDelegate = false;
			for (int j = 0; j < delegateAgents.size(); j++) {
				if (delegateAgents.get(j).id == allAgents.get(i).id) {
					isDelegate = true;
					break;
				}
			}

			if (!isDelegate)
				newAgents.add(allAgents.get(i));
		}

		Vector<Integer> selected_locations = new Vector<Integer>();

		for (int i = 0; i < delegateAgents.size(); i++) {
			HiveAgent agent = delegateAgents.get(i);
			double score = problem.getFunctionValue(agent.location_index);
			if (i == 0)
				min = score;

			Boolean in_set = false;
			for (int j = 0; j < locations.size(); j++)
				if (locations.get(j).equals(agent.location_index)) {
					in_set = true;
					break;
				}

			if (!in_set) {
				locations.add(agent.location_index);
				probabilities.add(score);
				if (score < min)
					min = score;

				total += score;
			}
		}

		Vector<Vector<Integer>> replaced = new Vector<Vector<Integer>>();
		Vector<Double> scores = new Vector<Double>();
		for (int i = 0; i < delegateAgents.size(); i++) {
			replaced.add(delegateAgents.get(i).code);
			scores.add(delegateAgents.get(i).score);
		}

		Vector<Vector<Integer>> children = getChildren(replaced, scores, replaced.size(), (int) (children_fraction * newAgents.size()));

		for (int i = 0; i < (int) (children_fraction * newAgents.size()); i++) {
			newAgents.get(i).code = children.get(i);
		}

		int size = probabilities.size();

		if (size * min < total) {
			for (int i = 0; i < size; i++) {
				probabilities.set(i, (probabilities.get(i) - min + 1) / (total - size * min + size));
			}
		} else {
			for (int i = 0; i < size; i++) {
				probabilities.set(i, 1.0 / (double) size);
			}
		}

		for (int i = 0; i < number_of_locations - delegateAgents.size(); i++) {
			double random = rng.nextDouble();
			double sum = 0.0;
			for (int j = 0; j < size; j++) {
				if (random >= sum && random < sum + probabilities.get(j)) {
					selected_locations.add(locations.get(j));
					break;
				}
				sum += probabilities.get(j);
			}
		}

		for (int i = 0; i < selected_locations.size(); i++) {
			int random = rng.nextInt(newAgents.size());
			HiveAgent tmp = newAgents.get(random);
			tmp.location_index = selected_locations.get(i);
			result.add(tmp);
			newAgents.remove(random);
		}

		return result;
	}

	public void solve(ProblemDomain problem) {

		number_of_heuristics = problem.getNumberOfHeuristics();
		problem.setMemorySize(number_of_locations * 2);

		allAgents = new Vector<HiveAgent>();
		delegateAgents = new Vector<HiveAgent>();

		for (int i = 0; i < population_size; i++) {
			Vector<Integer> newAgentCode = new Vector<Integer>();
			for (int j = 0; j < agent_size; j++)
				newAgentCode.add(rng.nextInt(number_of_heuristics));

			allAgents.add(new HiveAgent(i, newAgentCode, i % number_of_locations));
		}

		for (int i = 0; i < number_of_locations; i++) {
			delegateAgents.add(allAgents.get(i));
		}

		for (int i = 0; i < number_of_locations; i++) {
			problem.initialiseSolution(i);
		}

		while (!hasTimeExpired()) {
			evaluateAgents(delegateAgents, problem);

			if (hasTimeExpired())
				break;

			delegateAgents = selectStayAgents(delegateAgents);
			delegateAgents = completeDelegateAgents(allAgents, delegateAgents, problem);
		}
	}

	public String toString() {
		return "CS-PUT Genetic Hive Hyper Heuristic";
	}
}
