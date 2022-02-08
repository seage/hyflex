package hyperHeuristics;

import AbstractClasses.ProblemDomain.HeuristicType;

/**
 * Simple class to represent an action.
 */
public class Urli_AVEG_Action {
	
	/**
	 * Heuristic family.
	 */
	public HeuristicType type = null;

	/**
	 * Intensity of application.
	 */
	public Double intensity = 0.0;
	
	/**
	 * Constructor.
	 * @param type heuristic family.
	 * @param intensity intensity of application;
	 */
	public Urli_AVEG_Action(HeuristicType type, Double intensity) {
		this.type = type;
		this.intensity = intensity;
	}
}
