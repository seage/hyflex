/**
 * Copyright 2011-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package fr.lalea.eph;

import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Command line parameters for running EPH on instances of the
 * CHeSC benchmark.
 * 
 * @author David Meignan
 */
public class BenchmarkParameters {

	/**
	 * Maximum time limit allowed.
	 */
	protected static final int MAX_TIME_LIMIT_SECONDS = 86400;
	
	/**
	 * Maximum number of runs allowed.
	 */
	protected static final int MAX_RUNS = 5000;
	
	/**
	 * The time limit of a run in seconds.
	 */
	@Parameter(names = { "-t", "-timeLimit"}, description = "Time limit in seconds for each run.")
	private Integer timeLimitSeconds = 600;
	
	/**
	 * The number of runs to perform for each problem instance.
	 */
	@Parameter(names = { "-r", "-runs"}, description = "The number of runs to perform for each problem instance.")
	private Integer runs = 1;
	
	/**
	 * The file in which the results have to be recorded.
	 */
	@Parameter(names = { "-o", "-outputFile"}, description = "The file in which the results have to be recorded.",
			required = true)
	private String outputFileName;
	
	/**
	 * Disable the dynamic display of the progress.
	 */
	@Parameter(names = { "-dp", "-disableProgressOutput"}, description = "Disable the dynamic display of the progress.")
	private Boolean disableProgressOutput = false;
	
	/**
	 * The seed value for the random number generator. This value will be
	 * incremented by 1 for each run when mutiple runs are specified.
	 */
	@Parameter(names = { "-s", "-randomSeed"}, description = "The seed value for the random number generator. This value will be " +
			"incremented by 1 for each run when mutiple runs are specified.")
	private Integer randomSeedStartingValue = 0;

	/**
	 * The problem instances to run. The syntax is: 
	 * PROBLEM.INSTANCE [PROBLEM.INSTANCE...]. Valid problem names are: 
	 * SAT, BinPacking, PersonnelScheduling, FlowShop, TSP, VRP. Instances
	 * are numbers between 0 and 11 for the first four problem domains, and 
	 * between 0 and 4 for TSP and VRP.
	 * For instance the IDs of instances 0 and 2 of the SAT problem are "SAT.0" and "SAT.2".
	 */
	@Parameter(names = { "-p", "-problems"}, description = "The problem instances to run. The syntax is: " +
			"PROBLEM.INSTANCE [PROBLEM.INSTANCE...]. Valid problem names are: SAT, BinPacking, PersonnelScheduling, " +
			"FlowShop, TSP, VRP. Instances are numbers between 0 and 11 for the first four problem domains, and between 0 and 9 " +
			"for TSP and VRP. For instance to run instances 0 and 2 of the SAT problem: -p SAT.0 SAT.2",
			required = true, variableArity = true)
	private List<String> problemInstanceRefs = new ArrayList<String>();

	/**
	 * Validates the parameters and throw an exception if one of the
	 * parameter is not valid.
	 * 
	 * @throws ParameterException if a parameter value is not valid.
	 */
	public void validate() throws ParameterException {
		// Time limit in seconds
		if (timeLimitSeconds <= 0) {
			throw new ParameterException("Parameter value for -timeLimit"
					+ " must be greater than 0 (found "
					+ timeLimitSeconds + ")");
		}
		if (timeLimitSeconds > BenchmarkParameters.MAX_TIME_LIMIT_SECONDS) {
			throw new ParameterException("Parameter value for -timeLimit"
					+ " must be less than " + BenchmarkParameters.MAX_TIME_LIMIT_SECONDS
					+ " (found "
					+ timeLimitSeconds + ")");
		}
		// Runs
		if (runs <= 0) {
			throw new ParameterException("Parameter value for -runs"
					+ " must be greater than 0 (found "
					+ timeLimitSeconds + ")");
		}
		if (runs > BenchmarkParameters.MAX_TIME_LIMIT_SECONDS) {
			throw new ParameterException("Parameter value for -runs"
					+ " must be less than " + BenchmarkParameters.MAX_RUNS
					+ " (found "
					+ runs + ")");
		}
		// Output file
		try {
			File output = new File(outputFileName);
			if (output.isDirectory()) {
				throw new ParameterException("Parameter -outputFile"
						+ " is not a valid file name (found "
						+ outputFileName + ")");
			}
		} catch (Exception e) {
			throw new ParameterException("Parameter -outputFile"
					+ " is not a valid file name (found "
					+ outputFileName + ")");
		}
		// Problem list
		for (String ref: problemInstanceRefs) {
			if (ProblemDomainFactory.getInstanceID(ref) == null)
				throw new ParameterException("Value for -problems"
						+ " is not a valid problem instance identifier (found "
						+ ref + ")");
		}
	}
	
	/**
	 * Returns the time limit in seconds.
	 * 
	 * @return the time limit in seconds.
	 */
	public int getTimeLimitSeconds() {
		return timeLimitSeconds;
	}
	
	/**
	 * Returns the number of runs of EPH to perform for each instance.
	 * 
	 * @return the number of runs of EPH to perform for each instance.
	 */
	public int getRuns() {
		return runs;
	}
	
	/**
	 * Returns the file name for the output.
	 * 
	 * @return the file name for the output.
	 */
	public String getOutputFileName() {
		return outputFileName;
	}
	
	/**
	 * Returns <code>true</code> if the progress of the optimization must
	 * not be displayed, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the progress of the optimization must
	 * not be displayed, <code>false</code> otherwise.
	 */
	public boolean isProgressDisplayDisable() {
		return disableProgressOutput;
	}
	
	/**
	 * Returns the seed value for random number generator.
	 * 
	 * @return the seed value for random number generator.
	 */
	public int getSeedStartingValue() {
		return randomSeedStartingValue;
	}
	
	/**
	 * Returns a collection view of the references of problems to solve.
	 * 
	 * @return a collection view of the references of problems to solve.
	 */
	public List<String> problemInstanceRefs() {
		return new ProblemRefCollection();
	}
	
	/**
	 * Collection view of problem references.
	 */
	private class ProblemRefCollection extends AbstractList<String> {

		/* (non-Javadoc)
		 * @see java.util.AbstractList#get(int)
		 */
		@Override
		public String get(int idx) {
			return problemInstanceRefs.get(idx);
		}

		/* (non-Javadoc)
		 * @see java.util.AbstractCollection#size()
		 */
		@Override
		public int size() {
			return problemInstanceRefs.size();
		}
		
	}
	
}
