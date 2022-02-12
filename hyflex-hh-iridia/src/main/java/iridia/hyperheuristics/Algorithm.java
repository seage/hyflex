package iridia.hyperheuristics;

/**
 * Abstract class defining the required operations of the algorithms.
 */
public interface Algorithm {
    
    /**
     * Resets the algorithm.
     * Useful in case of restarts.
     */
    public void reset();

    /**
     * Reads the configuration and stores the values locally. If you change the
     * parameters after the constructor make sure you call this method.
     */
    public void updateConfiguration();

    /**
     * Performs a single step of the heuristic.
     *
     * @return current solution quality
     */
    public double step();

    /**
     * Performs a single step of the heuristic for a maximum time.
     *
     * @param if step is complex and can be stopped earlier stop after maxTime, 
     *        most algorithms call directly step, this is used mostly 
     *        by TunableHeuristics
     * @return current solution quality
     */
    public double stepLimited(long maxTime);
    
    /**
     * Returns the short name of the algorithm.
     *
     * @return name of the algorithm
     */
    @Override
    public String toString();
}
