package hyperHeuristics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import AbstractClasses.ProblemDomain.HeuristicType;

/**
 *	AVEG_NeptuneHyperHeuristic: an hyper-heuristic based on a number of autonomous agents using reinforcement learning to learn low-level heuristic preferences 
 *  (on per-state basis), and an epsilon-greedy action selection policy. The state is represented by the "recent relative reward" received by each of. Agents
 *  breed together upon the selection of a CROSSOVER action. Details on the paper.
 */
public class Urli_AVEG_NeptuneHyperHeuristic extends HyperHeuristic {

	/**
	 * Constructor. Accepts a seed to initialize a Pseudo-Random Number Generator.
	 * @param seed seed for the PSRNG.
	 */
	public Urli_AVEG_NeptuneHyperHeuristic(long seed) {
		super(seed);
		rng.setSeed(seed);
	}

	// Learning rate at which action values are updated
	double learningRate = 0.1;
	
	// Reactivity to new reward when updating state
	double reactivity = 0.1;	
	
	// Probability of choosing a suboptimal action
	double epsilon = 0.1;
	
	// Possible actions to take
	Map<Urli_AVEG_Action, Double> actions = new HashMap<Urli_AVEG_Action, Double>();

	// Agents
	int agents = 3;
	
	// Value of the current solution of an agent
	double[] functionValue = null;
	
	// State of an agent
	int[] state = null;
	
	// Recent low reward gained by an agent
	double[] recentLowReward = null;
	
	// Recent reference reward gained by an agent
	double[] recentReferenceReward = null;
	
	// Action values, for each agent
	Map<Integer, Map<Integer,Map<Urli_AVEG_Action, Double>>> actionValues = new HashMap<Integer, Map<Integer, Map<Urli_AVEG_Action,Double>>>();

	@Override
	protected void solve(ProblemDomain problemDomain) {
		
		// Initialize actions
		HeuristicType[] heuristicTypes = HeuristicType.values();
		double[] quantities = new double[] {0.2, 0.4, 0.6, 0.8, 1.0};
		for(int h = 0; h < heuristicTypes.length; h++)
			if ( problemDomain.getHeuristicsOfType(heuristicTypes[h]) != null )
				for(int q = 0; q < quantities.length; q++)
					actions.put(new Urli_AVEG_Action(heuristicTypes[h], quantities[q]),  0.0);

		// Initialize solutions, solution values and states for each agent
		problemDomain.setMemorySize(agents);
		functionValue = new double[agents];
		state = new int[agents];
		recentLowReward = new double[agents];
		recentReferenceReward = new double[agents];
		
		for( int agent = 0; agent < agents; agent++) {
			problemDomain.initialiseSolution(agent);
			functionValue[agent] = problemDomain.getFunctionValue(agent);
			state[agent] = 0;
			recentLowReward[agent] = 1.0;
			recentReferenceReward[agent] = 1.0;
			actionValues.put(agent, new HashMap<Integer, Map<Urli_AVEG_Action,Double>>());
		}
						
		// If there's more time to find a solution
		while(!hasTimeExpired()) {
			
			// Perform a search step for each agent
			for(int agent = 0; agent < agents; agent++) {
				
				// If we are in a new state, initialize action values for that state
				if ( !actionValues.get(agent).containsKey(state[agent]) )
					initializeActionValues(agent, state[agent]);
				
				// Find heuristic to apply according to policy
				Urli_AVEG_Action action = chooseAction(agent);
				double reward = functionValue[agent];

				int[] heuristics = problemDomain.getHeuristicsOfType(action.type);
				int heuristicIndex = heuristics[rng.nextInt(heuristics.length)];
					
				// Setup low-level heuristic
				problemDomain.setDepthOfSearch(action.intensity);
				problemDomain.setIntensityOfMutation(action.intensity);
					
				// Apply low level heuristic
				functionValue[agent] = problemDomain.applyHeuristic( heuristicIndex, agent, chooseBestPartner(agent), agent );

				// Compute reward as delta
		 		reward -= functionValue[agent];
					
				// Update reference reward
				recentReferenceReward[agent] = recentReferenceReward[agent] + reactivity * ( reward - recentReferenceReward[agent] );
					
				// If reward is "low" wrt. the reference reward, use it to update the concept of "low" reward
				if (Math.abs(reward) < Math.abs(recentReferenceReward[agent]) && Math.abs(reward) != 0.0f)
					recentLowReward[agent] = recentLowReward[agent] + reactivity * ( Math.abs(reward)- recentLowReward[agent] );

				// Calculate relative improvement wrt. the recent low reward
				double relativeReward = reward / recentLowReward[agent];

				// Update agent's state
				int oldState = state[agent];
				state[agent] = (int) Math.floor( state[agent] + reactivity * ( relativeReward -  state[agent]));

				// Get old action value
				double oldValue = actionValues.get(agent).get(oldState).get(action);

				// Update action value in previous state using the formula newValue = oldValue + stepSize [target - oldValue], where stepSize = learning rate
				actionValues.get(agent).get(oldState).put(action, (oldValue + learningRate * ( relativeReward - oldValue )));
			}
		}
	}

	/**
	 * Choose action based on action values.
	 * @param agent index of the agent.
	 */
	private Urli_AVEG_Action chooseAction(int agent) {
		
		// With probability epsilon choose randomly
		if ( rng.nextDouble() <= epsilon )
			return (Urli_AVEG_Action) actions.keySet().toArray()[rng.nextInt(actions.size())];
	
	    // Otherwise choose according to action values
		return chooseActionGreedily(agent);		
	}
	
	/**
	 * Initialize action values once arrived into a new state.
	 * @param agent index of itself.
	 * @param state current state.
	 */
	private void initializeActionValues(int agent, int state) {
		
		actionValues.get(agent).put(state, new HashMap<Urli_AVEG_Action, Double>());
		Iterator<Urli_AVEG_Action> actionIterator = actions.keySet().iterator();
		
		// Initialize action values randomly
		while(actionIterator.hasNext())
			actionValues.get(agent).get(state).put(actionIterator.next(), 0.0);
		
	}
	
	/**
	 * Choose the best action to apply, greedily.
	 * @param agent index of itself.
	 */
	private Urli_AVEG_Action chooseActionGreedily(int agent) {
		
		Map<Urli_AVEG_Action, Double> currentActionValues = actionValues.get(agent).get(state[agent]);
		Iterator<Urli_AVEG_Action> actionValueIterator = currentActionValues.keySet().iterator();
		
		Urli_AVEG_Action bestAction = actionValueIterator.next();
		
		// Choose best
		while (actionValueIterator.hasNext()) {
			Urli_AVEG_Action action = actionValueIterator.next();
			if( currentActionValues.get(action) > currentActionValues.get(bestAction))
				bestAction = action;
		}
		
		return bestAction;
	}
	
	/**
	 * Choose the best among the set of agents (except itself).
	 * @param agent index of itself.
	 */
	private int chooseBestPartner(int agent) {
		int bestPartner = 0;
		for(int partner = 0; partner < agents ; partner++)
			if (agent != partner && functionValue[bestPartner] > functionValue[partner])
				bestPartner = partner;
		
		return bestPartner;
	}

	@Override
	public String toString() {
		return "AVEG_NeptuneHyperHeuristic";
	}

	
	
}
