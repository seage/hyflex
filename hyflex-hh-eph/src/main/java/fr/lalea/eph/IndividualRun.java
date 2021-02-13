/**
 * Copyright 2011-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package fr.lalea.eph;

import AbstractClasses.ProblemDomain;

/**
 * This class allows to run EPH on a specific instance.
 * 
 * @author David Meignan
 */
public class IndividualRun implements Runnable {
	
	/**
	 * Creates an object for a run of EPH.
	 * 
	 * @param problemInstanceRef Reference of the problem instance in the form
	 * PROBLEM.INSTANCE.
	 * @param timeLimitSeconds the time limit in seconds.
	 * @param seed the seed value for random number generator.
	 */
	public IndividualRun(String problemInstanceRef, int timeLimitSeconds,
			int seed) {
		// Copy parameters
		this.timeLimitSeconds = timeLimitSeconds;
		this.seedValue = seed;
		this.problemInstanceRef = problemInstanceRef;
	}

	/**
	 * The reference of the problem (in form of PROBLEM.INSTANCE).
	 */
	private String problemInstanceRef;
	
	/**
	 * The time limit in second for the run.
	 */
	private int timeLimitSeconds;
	
	/**
	 * The seed value for the random number generator.
	 */
	private int seedValue;

	/**
	 * State of the run.
	 */
	private RUN_STATE state = RUN_STATE.PENDING;
	private enum RUN_STATE {
		PENDING, STARTED, COMPLETE, FAIL
	}
	
	/**
	 * EPH
	 */
	private EPH eph;
	
	/**
	 * Starting time of the run.
	 */
	private long runStartingTimeMillis;
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		runStartingTimeMillis = System.currentTimeMillis();
		setState(RUN_STATE.STARTED);
		// Load problem instance
		ProblemDomain problem = ProblemDomainFactory.loadProblemInstance(
				problemInstanceRef, seedValue);
		// Instantiate EPH
		eph = new EPH(seedValue);
		eph.setTimeLimit(timeLimitSeconds*1000);
		eph.loadProblemDomain(problem);
		// Run EPH
		eph.run();
		// Finished
		setState(RUN_STATE.COMPLETE);
	}

	/**
	 * Set the current state of the run.
	 * 
	 * @param newState the new state of the run.
	 */
	private void setState(RUN_STATE newState) {
		state = newState;
	}
	
	/**
	 * Returns the progress of the run.
	 * 
	 * @return the progress of the run.
	 */
	public synchronized double getProgress() {
		if(state == RUN_STATE.PENDING)
			return 0.;
		if (state == RUN_STATE.COMPLETE 
				|| state == RUN_STATE.FAIL)
			return 100.;
		double elapsedTimeMillis = System.currentTimeMillis()
				-runStartingTimeMillis;
		double completion = ((elapsedTimeMillis/(timeLimitSeconds*1000.))
				*100.);
		if (completion<=0)
			return 0.;
		if (completion>=100)
			return 100.;
		return completion;
	}

	/**
	 * Returns the best found value.
	 * 
	 * @return the best found value.
	 */
	public synchronized double getBestFoundValue() {
		if(state == RUN_STATE.PENDING || eph == null)
			return Double.NaN;
		return eph.getBestSolutionValue();
	}

	/**
	 * Returns the name of the problem.
	 * 
	 * @return the name of the problem.
	 */
	public String getProblemName() {
		return ProblemDomainFactory.getProblemName(problemInstanceRef);
	}

	/**
	 * Returns the ID of the instance.
	 * 
	 * @return the ID of the instance.
	 */
	public int getInstanceID() {
		return ProblemDomainFactory.getInstanceID(problemInstanceRef);
	}

}
