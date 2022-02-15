package iridia;

import AbstractClasses.ProblemDomain;
import java.util.ArrayList;

/**
 * This is a helper class to call low level heuristics and compute performance
 * statistics.
 */
public class LowLevelHeuristic {

    protected ProblemDomain.HeuristicType type;
    protected int id;
    protected int indexInHeuristicsOfType;
    protected int posTempSolution;
    protected boolean acceptsParameter;
    protected double parameter;

    protected double confPrecision;
    
    /**
     * Prepares the data structures for the low level heuristic.
     *
     * @param id identifier of the heuristic used in ProblemDomain applyHeurisitcs
     * @param pos incremental counter of heuristics of this type
     * @param posTempSolution solution used by low level heuristics (LS have to be applied until local minima is found)
     * @param type type of the heuristic
     */
    public LowLevelHeuristic(int id, int pos, int posTempSolution, ProblemDomain.HeuristicType type) {
        Configuration conf = Configuration.getInstance();
        confPrecision = conf.getDouble("precision");

        this.id = id;
        this.indexInHeuristicsOfType = pos;
        this.type = type;
        this.posTempSolution = posTempSolution;
        acceptsParameter = false;

        if (type == ProblemDomain.HeuristicType.LOCAL_SEARCH) {
            ArrayList<Integer> dos = new ArrayList<Integer>();
            for (int i : Shared.problem.getHeuristicsThatUseDepthOfSearch()) {
                dos.add(i);
            }
            acceptsParameter = dos.contains(id);
        } else if (type == ProblemDomain.HeuristicType.RUIN_RECREATE) {

            ArrayList<Integer> iom = new ArrayList<Integer>();
            for (int i : Shared.problem.getHeuristicsThatUseIntensityOfMutation()) {
                iom.add(i);
            }
            acceptsParameter = iom.contains(id);
        }
        
        parameter = 0.1;
    }

    /**
     * Executes the low level heuristic with a specific parameter.
     *
     * @param src position of the source solution in the memory
     * @param dst position of the destination solution in the memory
     * @param parameter parameter to be set (i.e. depthOfSearch or intensityOfMutation)
     * @throws UnsupportedOperationException if the local search does not support the setting of parameters
     * @return solution quality
     */
    public double executeWithParameter(int src, int dst, double parameter) {
        if (!acceptsParameter) {
//            throw new UnsupportedOperationException("Parameter setting not supported for this low level heuristic.");
            return doExecute(src, dst);
        }

        double backup = this.parameter;
        if (type == ProblemDomain.HeuristicType.LOCAL_SEARCH) {
            backup = Shared.problem.getDepthOfSearch();
            Shared.problem.setDepthOfSearch(parameter);
        } else if (type == ProblemDomain.HeuristicType.RUIN_RECREATE) {
            backup = Shared.problem.getIntensityOfMutation();
            Shared.problem.setIntensityOfMutation(parameter);
        }

        double solutionQuality = doExecute(src, dst);

        if (type == ProblemDomain.HeuristicType.LOCAL_SEARCH) {
            Shared.problem.setDepthOfSearch(backup);
        } else if (type == ProblemDomain.HeuristicType.RUIN_RECREATE) {
            Shared.problem.setIntensityOfMutation(backup);
        }

        return solutionQuality;
    }

    /**
     * Executes the low level heuristics. LOCAL_SEARCH heuristics are applied
     * until a local minima is found (no improvement in quality);
     *
     * @param src position of the source solution in the memory
     * @param dst position of the destination solution in the memory
     * @return solution quality
     */
    public double execute(int src, int dst) {
        return executeWithParameter(src, dst, parameter);
    }
    
    /**
     * Executes the low level heuristics. LOCAL_SEARCH heuristics are applied
     * until a local minima is found (no improvement in quality);
     *
     * @param src position of the source solution in the memory
     * @param dst position of the destination solution in the memory
     * @return solution quality
     */
    private double doExecute(int src, int dst) {
        if (type == ProblemDomain.HeuristicType.LOCAL_SEARCH) {
            Shared.problem.copySolution(src, posTempSolution);
            double qual = Double.POSITIVE_INFINITY;
            double b_qual;
            do {
                b_qual = qual;
                qual = Shared.problem.applyHeuristic(id, posTempSolution, dst);
                Shared.problem.copySolution(dst, posTempSolution);
            } while (!Shared.hyperHeuristic.hasTimeExpired() && b_qual - qual > confPrecision);
            return qual;
        } else {
            return Shared.problem.applyHeuristic(id, src, dst);
        }
    }

    /**
     * Returns whether this low level heuristic accepts a parameter.
     * 
     * @return if the heuristic accepts a parameter
     */
    public boolean getAcceptsParameter() {
        return acceptsParameter;
    }

    /**
     * Sets the parameter for all the calls of this low level heuristic.
     * 
     * @return if the heuristic accepts a parameter
     */
    public void setParameter(double parameter) {
        if (acceptsParameter) {
            this.parameter = parameter;
        }
    }
    
    /**
     * Returns the heuristic id.
     *
     * @return id
     */
    public int getId() {
       return id; 
    }
    
    /**
     * Returns the type of heuristic.
     * 
     * @return type of heuristic
     */
    public ProblemDomain.HeuristicType getType() {
        return type;
    }
    
    /**
     * Returns a string with a low level heuristic identifier plus quality and
     * time statistics.
     *
     * @return heuristic type, internal numbering, median, lower and upper quartiles of solution quality and time
     */
    @Override
    public String toString() {
        return type + "" + indexInHeuristicsOfType;
    }
}
