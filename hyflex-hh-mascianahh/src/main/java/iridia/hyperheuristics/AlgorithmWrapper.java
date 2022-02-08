package iridia.hyperheuristics;

import iridia.Shared;

/**
 * After tuning the different algorithms some common settings are tuned along 
 * the specific ones (e.g. iom and dos). The parameter are specified in the 
 * constructor and not through the configuration class.
 * Note that this is not always required, for example ILS heuristics read the
 * conf and store it locally, therefore it suffices to set the conf before
 * the constructor. IOM and DOS are different because it is necessary to call 
 * the framework and the heuristics don't do it for not wasting time.
 * If wrapping a TunableHyperHeuristics the parameters are ignored.
 * 
 * The wrapper resets these values before executing a steps of an algorithm, 
 * this is useful when racing the algorithms in parallel.
 */
public class AlgorithmWrapper {
    private Algorithm algo;
    private double intensityOfMutation;
    private double depthOfSearch;
            
    /**
     * Constructor.
     * 
     * @param algo the algorithm to be wrapped
     * @param intensityOfMutation intensity of mutation for mutation and ruin and recreate
     * @param depthOfSearch depth of search for local search low level heuristics
     */
    public AlgorithmWrapper(Algorithm algo, double intensityOfMutation, double depthOfSearch) {
        this.algo = algo;
        this.intensityOfMutation = intensityOfMutation;
        this.depthOfSearch = depthOfSearch;
    }
            
    /**
     * Restores the values specified in the constructor and executes a step of the wrapped algorithm.
     * 
     * @return current solution quality
     */
    public double step() {
        Shared.problem.setDepthOfSearch(depthOfSearch);
        Shared.problem.setIntensityOfMutation(intensityOfMutation);

        return algo.step();
    }

    /**
     * Restores the values specified in the constructor and executes a step of the wrapped algorithm.
     * 
     * @param maxTime maximum time for the step
     * @return current solution quality
     */
    public double stepLimited(long maxTime) {
        Shared.problem.setDepthOfSearch(depthOfSearch);
        Shared.problem.setIntensityOfMutation(intensityOfMutation);

        return algo.stepLimited(maxTime);        
    }
        
    
    /**
     * Returns the wrapped algorithm, to be used for the rest of the search after the racing.
     */
    public Algorithm getAlgo() {
        return algo;
    }


    /**
     * Returns the wrapped algorithm name.
     * 
     * @return algorithm name
     */
    public String toString() {
        return algo.toString();
    }
        
}
