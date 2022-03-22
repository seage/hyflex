package iridia.hyperheuristics.specific;

import AbstractClasses.ProblemDomain;
import iridia.Configuration;
import iridia.LowLevelHeuristic;
import iridia.Shared;
import iridia.hyperheuristics.Algorithm;
//import com.sun.org.apache.xml.internal.utils.MutableAttrListImpl;
import java.util.ArrayList;

/**
 * edited by Dave Omrai
 */

/**
 * This class implements an IG procedure:
 *
 * s = generate solution
 * repeat
 *   sp = DestructionPhase(s)
 *   s' = ConstructionPhase(sp)
 *   s = AcceptanceCriterion(s, s')
 * until termination criterion is met
 *
 * Selects randomly a RUIN_AND_RECREATE and uses it for destruction and construction.
 */
public class IteratedGreedyFlowShop implements Algorithm {

    private int posInitialSolution;
    private int posCurrentSolution;
    private int posTempSolution;
    private int posTemp2Solution;
    private double qualTempSolution;
    private double qualCurrentSolution;
    private double confPrecision;
    
    private double confTemperature;
    private int confNumberOfRRmin;
    private int confNumberOfRRspread;
    private int confNumberOfLS;
    
    private ArrayList<LowLevelHeuristic> ls;
    private ArrayList<LowLevelHeuristic> mt;
    private ArrayList<LowLevelHeuristic> rr;
    private ArrayList<LowLevelHeuristic> div;
    
    /**
     * Constructor of the Iterated Greedy heuristic.
     *
     * @param posInitialSolution position in the memory of the initial solution
     * @param posCurrentSolution position in the memory of the current solution
     * @param posTempSolution position in the memory of a temp location used internally
     * @param posTemp2Solution position in the memory of a temp location used internally
     */
    public IteratedGreedyFlowShop(int posInitialSolution, int posCurrentSolution, int posTempSolution, int posTemp2Solution) {
        this.posInitialSolution = posInitialSolution;
        this.posCurrentSolution = posCurrentSolution;
        this.posTempSolution = posTempSolution;
        this.posTemp2Solution = posTemp2Solution;

        readConfiguration();
        initialisation();
    }

    /**
     * Resets the algorithm.
     * Useful in case of restarts.
     */
    public void reset() {
        initialisation();
    }

    /**
     * Reads the configuration and stores the values locally. If you change the
     * parameters after the constructor make sure you call this method.
     */
    public void updateConfiguration() {
        readConfiguration();
    }

    /**
     * Reads the configuration and stores the values locally. If you change the
     * parameters after the constructor make sure you call updateConfiguration().
     */
    private void readConfiguration() {
        Configuration conf = Configuration.getInstance();
        confPrecision = conf.getDouble("precision");
        confTemperature = conf.getDouble("IGFS_TemperatureAcceptingWorsening");
        confNumberOfRRmin = conf.getInteger("IGFS_numberOfRRmin");
        confNumberOfRRspread = conf.getInteger("IGFS_numberOfRRspread");        
        confNumberOfLS = conf.getInteger("IGFS_numberLS");
    }

    /**
     * Common operation for constructor and reset.
     * We copy the initial solution and set its quality to +inf so that the
     * first time the ruin and recreate heuristic is executed.
     */
    private void initialisation() {
        Shared.problem.copySolution(posInitialSolution, posTempSolution);
        qualTempSolution = Double.POSITIVE_INFINITY;        
        ls = Shared.availableHeuristics.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
        mt = Shared.availableHeuristics.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
        rr = Shared.availableHeuristics.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
    }

    /**
     * Performs a step of the Iterated Greedy heuristic.
     *
     * @return current solution quality
     */
    public double step() {        
        // from 3 to 6 runs
        int numb_of_rr = Shared.rng.nextInt(confNumberOfRRspread) + confNumberOfRRmin;
        Shared.problem.copySolution(posTempSolution, posTemp2Solution);            
        if (rr.size() > 0) {
            for (int i = 0; i < numb_of_rr; i++) {
                qualCurrentSolution = rr.get(Shared.rng.nextInt(rr.size())).execute(posTemp2Solution, posCurrentSolution);
                Shared.problem.copySolution(posCurrentSolution, posTemp2Solution);
            }
        } else if (mt.size() > 0) {
            for (int i = 0; i < numb_of_rr; i++) {
                qualCurrentSolution = mt.get(Shared.rng.nextInt(mt.size())).execute(posTemp2Solution, posCurrentSolution);
                Shared.problem.copySolution(posCurrentSolution, posTemp2Solution);
            }            
        }
        int max = confNumberOfLS;
        if (confNumberOfLS > ls.size()) {
            max = ls.size();
        }
        for (int i = 0; i < max; i++) {
            qualCurrentSolution = ls.get(i).execute(posTemp2Solution, posCurrentSolution);
            Shared.problem.copySolution(posCurrentSolution, posTemp2Solution);            
        }
                       
        if (acceptance()) {
            Shared.problem.copySolution(posCurrentSolution, posTempSolution);
            qualTempSolution = qualCurrentSolution;
        }
//        System.err.println(Shared.problem.getBestSolutionValue() + " " + qualCurrentSolution);
        return qualCurrentSolution;
    }

    /**
     * Performs a single step of the heuristic for a maximum time.
     *
     * @param if step is complex and can be stopped earlier stop after maxTime, 
     *        most algorithms call directly step, this is used mostly 
     *        by TunableHeuristics
     * @return current solution quality
     */
    public double stepLimited(long maxTime) {
        return step();
    }       
    
    /**
     * Always accepts an improving solution, or a worsening solution with
     * probability probabilityAcceptingWorsening.
     *
     * @return if the solution has to be kept and backup at the next step
     */
    private boolean acceptance() {
        if (qualTempSolution - qualCurrentSolution > confPrecision) {
            return true;
        } else {
            double ratio = Math.exp(-((qualCurrentSolution - qualTempSolution) / confTemperature));
            float val = Shared.rng.nextFloat();
            boolean accepted = val < ratio;
//            if (accepted) {
//                System.err.println(qualTempSolution + "<" + qualCurrentSolution + " but " + val + " < " + ratio);
//            }
            return accepted;
        }
    }

    /**
     * Returns the short name of the algorithm.
     *
     * @return name of the algorithm
     */
    @Override
    public String toString() {
        return "IteratedGreedyFlowShop";
    }
}
