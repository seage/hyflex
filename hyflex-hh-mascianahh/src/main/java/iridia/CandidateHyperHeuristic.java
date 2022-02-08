package iridia;

import AbstractClasses.ProblemDomain;
import iridia.hyperheuristics.Algorithm;
import iridia.hyperheuristics.AlgorithmWrapper;
import iridia.hyperheuristics.specific.IteratedGreedyFlowShop;
import iridia.hyperheuristics.specific.TunedBinPacking;
import iridia.hyperheuristics.specific.TunedPersonnelScheduling;
import iridia.hyperheuristics.specific.TunedSAT;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class implements the candidate HyperHeuristic for the competition.
 */
public class CandidateHyperHeuristic extends MyHyperHeuristic {

    private double confPrecision;

    /**
     * Constructor passing the random seed to the parent class.
     *
     * @param seed seed of the random number generator
     * @param algo algorithm to be executed
     */
    public CandidateHyperHeuristic(long seed) {
        super(seed);

        Configuration conf = Configuration.getInstance();
        confPrecision = conf.getDouble("precision");
    }

    /**
     * Measures the performances of the low level hyper heuristics for a
     * given amount of milliseconds.
     *
     * @param time available in milliseconds
     * @param maxNumberOfMeasures how many tests to be performed
     */
    private void preliminaryRuns(long allocatedTime, int maxNumberOfMeasures, int posBestSolution) {
        long startTime = getElapsedTime();
        long time;
        double obj_function_value;

//        System.out.println("");
//        System.out.println("Preliminary runs for max " + allocatedTime / 1000 + " seconds.");
//        System.out.println("[LL_IntensityOfMutation=0.1 LL_DepthOfSearch=0.1 no impact on ranking]");
//        System.out.println("");

        Shared.problem.setDepthOfSearch(0.1);
        Shared.problem.setIntensityOfMutation(0.1);

        Shared.problem.initialiseSolution(0); // it seems we cannot call hasTimeExpired without having a solution quality for the FlowShop problem

        // we start every time with a new inital solution and we try to make
        // the highest number of measures in the given time we make at least one
        // measure per low level heuristic
        double bestSofar = Double.POSITIVE_INFINITY;
        while (getElapsedTime() - startTime < allocatedTime && !hasTimeExpired()) {
            Shared.problem.initialiseSolution(0);
            for (ProblemDomain.HeuristicType ht : ProblemDomain.HeuristicType.values()) {
                // we actually don't care for crossover mutation etc
                if (ht != ProblemDomain.HeuristicType.LOCAL_SEARCH && ht != ProblemDomain.HeuristicType.RUIN_RECREATE) {
                    continue;
                }
                for (LowLevelHeuristic ll : Shared.availableHeuristics.getHeuristicsOfType(ht)) {
                    if (((LowLevelHeuristicStats) ll).getNumberOfMeasures() < maxNumberOfMeasures) {
//                        double qual = ((LowLevelHeuristicStats)ll).executeSingleStep(0, 1);
                        double qual = ((LowLevelHeuristicStats) ll).execute(0, 1);
                        hasTimeExpired(); // save the best
                        if (bestSofar - qual > confPrecision) {
                            bestSofar = qual;
                            Shared.problem.copySolution(1, posBestSolution);
                        }
                    } else {
                        // if one reaches maxNumberOfMeasures tests we are happy with all of them
                        return;
                    }
                }
            }
        }
    }

    /**
     * Computes the statistics on the low level heuristics.
     * 
     * @param time available in milliseconds
     * @param maxNumberOfMeasures how many tests to be performed
     */
    private void computeStatsOnLowLevelHeuristics(long allocatedTime, int maxNumberOfMeasures, int posBestSolution) {
        preliminaryRuns(allocatedTime, maxNumberOfMeasures, posBestSolution);
        // computeStats and print them
        for (ProblemDomain.HeuristicType ht : ProblemDomain.HeuristicType.values()) {
            for (LowLevelHeuristic ll : Shared.availableHeuristics.getHeuristicsOfType(ht)) {
                ((LowLevelHeuristicStats) ll).updateStats();
//                System.out.println(ll);
            }
        }
//        System.out.println("");
    }

    /**
     * Removes the dominated heuristics from the pool of available ones.
     */
    private void removeDominated(int keepBest) {
//        System.out.print("Checking for dominated solutions (among same type)...");
        ArrayList<LowLevelHeuristic> dominated = new ArrayList<LowLevelHeuristic>();
        ArrayList<LowLevelHeuristic> best = new ArrayList<LowLevelHeuristic>();
//        boolean printed = false;
        for (ProblemDomain.HeuristicType ht : ProblemDomain.HeuristicType.values()) {
            if (ht != ProblemDomain.HeuristicType.LOCAL_SEARCH && ht != ProblemDomain.HeuristicType.RUIN_RECREATE) {
                continue;
            }
            // check for dominated among the heuristics of this type
            dominated.clear();
            for (LowLevelHeuristic ll1 : Shared.availableHeuristics.getHeuristicsOfType(ht)) {
                for (LowLevelHeuristic ll2 : Shared.availableHeuristics.getHeuristicsOfType(ht)) {
                    if (ll1.getId() != ll2.getId() && ((LowLevelHeuristicStats) ll1).getMedianTime() >= ((LowLevelHeuristicStats) ll2).getMedianTime() && ((LowLevelHeuristicStats) ll1).getMedianQuality() >= ((LowLevelHeuristicStats) ll2).getMedianQuality() && !dominated.contains(ll2)) {
                        dominated.add(ll1);
                        break;
                    }
                }
            }
//            if (!dominated.isEmpty() && !printed) {
//                System.out.println("");
//                printed = true;
//            }
            // finding the two best performing
            best.clear();
            Shared.availableHeuristics.sortHeuristicsByQuality(ht);
            int max = Shared.availableHeuristics.getHeuristicsOfType(ht).size();
            if (max > keepBest) {
                max = keepBest;
            }
            for (int i = 0; i < max; i++) {
                best.add(Shared.availableHeuristics.getHeuristicsOfType(ht).get(i));
            }
            // remove the dominated among this type keeping "keepBest" of the best always
            for (LowLevelHeuristic ll : dominated) {
                if (!best.contains(ll)) {
//                    System.out.println("\t Removing " + ((LowLevelHeuristicStats) ll) + ".");
                    Shared.availableHeuristics.getHeuristicsOfType(ht).remove(ll);
                } else {
//                    System.out.println("\t Keeping " + ((LowLevelHeuristicStats) ll) + " (among the " + keepBest + " best).");
                }
            }
        }
//        System.out.println("done.\n");

        // print the list of heuristics again
//        for (ProblemDomain.HeuristicType ht : ProblemDomain.HeuristicType.values()) {
//            for (LowLevelHeuristic ll : Shared.availableHeuristics.getHeuristicsOfType(ht)) {
//                System.out.println(((LowLevelHeuristicStats) ll));
//            }
//        }
//        System.out.println("");
    }

    /**
     * This method defines the strategy of the hyper-heuristic depending on the
     * algorithm specified in the constructor.
     *
     * @param problem the problem domain to be solved
     */
    @Override
    public void solve(ProblemDomain problem) {
        // we set some common variables used by almost all instances
        Shared.problem = problem;
        Shared.rng = rng;
        Shared.hyperHeuristic = this;

        // MEMORY:
        // 0 - initial solution (never toucherd by hyper heuristics)
        // 1, 2, ..., n - 3 - used internally by hyper heuristics
        // n - 2 - used by localsearch to be called repeatedly until local minima
        // n - 1 - used to store best so far especially in the initial tuning
        int memorySize = 30; // should be enough also for interleaved
        Shared.problem.setMemorySize(memorySize);
        Shared.availableHeuristics = new AvailableHeuristics(memorySize - 2, true);
        for (ProblemDomain.HeuristicType ht : ProblemDomain.HeuristicType.values()) {
            if (problem.getHeuristicsOfType(ht) != null) {
                for (int i : Shared.problem.getHeuristicsOfType(ht)) {
                    Shared.availableHeuristics.add(i, ht);
                }
            }
        }
        Configuration conf = Configuration.getInstance();

        // max LLSTATS_MaxFractionOfTime of initial time spent in preliminary measures of low level heuristics
        computeStatsOnLowLevelHeuristics((long) (getTimeLimit() * conf.getDouble("LLSTATS_MaxFractionOfTime")), conf.getInteger("LLSTATS_MaxIterations"), memorySize - 1);

        // remove dominated low level heuristics
        removeDominated(conf.getInteger("RD_KeepBest"));
        Shared.availableHeuristics.sortHeuristicsByTime(ProblemDomain.HeuristicType.LOCAL_SEARCH);
        Shared.availableHeuristics.sortHeuristicsByTime(ProblemDomain.HeuristicType.RUIN_RECREATE);

        // prepare the heuristics without stats
        AvailableHeuristics temp = new AvailableHeuristics(memorySize - 2, false);
        for (ProblemDomain.HeuristicType ht : ProblemDomain.HeuristicType.values()) {
            if (problem.getHeuristicsOfType(ht) != null) {
                // it iterates in the right order so we keep the sorted values
                for (LowLevelHeuristic ll : Shared.availableHeuristics.getHeuristicsOfType(ht)) {
                    if (ht == ProblemDomain.HeuristicType.LOCAL_SEARCH && ((LowLevelHeuristicStats) ll).getMedianTime() > conf.getInteger("LL_MaxTimeToSetDepthOfSearch")) {
                        ll.setParameter(conf.getDouble("LL_DepthOfSearch"));
                    }
                    temp.add(ll.getId(), ht);
                }
            }
        }
        Shared.availableHeuristics = temp;
        Shared.problem.setIntensityOfMutation(conf.getDouble("LL_IntensityOfMutation"));
        Shared.problem.setDepthOfSearch(conf.getDouble("LL_DepthOfSearch"));

        // we start from the best solution so far
        Shared.problem.copySolution(memorySize - 1, 0);

        // construct the algorithm
        Algorithm hh = null;

        long remainingTimeForActualSearch = getTimeLimit() - getElapsedTime();
        long beginTime = getElapsedTime();

//        System.out.println("Already used " + getElapsedTime() / 1000 + " seconds, remaining " + remainingTimeForActualSearch / 1000 + " seconds for the actual search.\n");
//        System.out.println("Starting from best so far: " + getBestSolutionValue() + ".\n");


        HashMap<AlgorithmWrapper, Double> interleaved = new HashMap<AlgorithmWrapper, Double>(5);
        // best candidate for sat
        interleaved.put(new AlgorithmWrapper(new TunedSAT(0, 1, 2, 3), -1, -1), Double.POSITIVE_INFINITY);

        // best candidate for flowshop
        interleaved.put(new AlgorithmWrapper(new IteratedGreedyFlowShop(0, 4, 5, 6), 0.25, 0.098), Double.POSITIVE_INFINITY);

        // best candidate for personnel scheduling
        interleaved.put(new AlgorithmWrapper(new TunedPersonnelScheduling(0, 7, 8, 9), -1, -1), Double.POSITIVE_INFINITY);

        // best candidate for bin packing
        interleaved.put(new AlgorithmWrapper(new TunedBinPacking(0, 10, 11, 12), -1, -1), Double.POSITIVE_INFINITY);

        // execute the algorithm
        double valueBest = Double.POSITIVE_INFINITY;
        AlgorithmWrapper algoBest = null;
        HashSet<AlgorithmWrapper> toRemove = new HashSet<AlgorithmWrapper>();
        long allocatedTime = (long) (remainingTimeForActualSearch * conf.getDouble("INTERLEAVED_FractionORTBeforeDropping"));
        long newAllocatedTime = allocatedTime;
        long initialTime = getElapsedTime();
        double initialBest = getBestSolutionValue();
        while (!hasTimeExpired() && interleaved.keySet().size() > 1) {
            ArrayList<AlgorithmWrapper> availAlgos = new ArrayList<AlgorithmWrapper>(interleaved.keySet());
            Collections.shuffle(availAlgos);
            for (AlgorithmWrapper ia : availAlgos) {
                double perf = 0;
                long startTime = getElapsedTime();
                long timeSpent = 0;
                // we exec one of the candidates
                while (!hasTimeExpired() && timeSpent < allocatedTime) {
                    perf = ia.stepLimited(allocatedTime);
                    timeSpent = getElapsedTime() - startTime;
                }
                // we adapt to the slowest so that all have roughly the same time
                if (timeSpent > newAllocatedTime) {
                    newAllocatedTime = timeSpent;
                }
                // we save the performance
                if (perf < interleaved.get(ia)) {
                    interleaved.put(ia, perf);
                    if (perf < valueBest) {
                        algoBest = ia;
                        valueBest = perf;
                    }
                } else {
                    // could just be the first, anyway... 
                    if (ia != algoBest) {
                        toRemove.add(ia);
                    }
                }
//                System.out.println(ia + " " + interleaved.get(ia) + " " + (timeSpent / 1000) + "s.");
            }
            allocatedTime += (long) ((newAllocatedTime - allocatedTime) * 0.90);

            // if the time elapsed is 10 times INTERLEAVED_FractionORTBeforeDropping we just keep the best
            if (getElapsedTime() - initialTime > 10 * (remainingTimeForActualSearch * conf.getDouble("INTERLEAVED_FractionORTBeforeDropping"))) {
                break;
            }

            // removing algorithms that perform twice as bad as the best one
            for (AlgorithmWrapper ia : interleaved.keySet()) {
                if (interleaved.get(ia) - valueBest > valueBest) {
                    toRemove.add(ia);
                }
            }

            // removing algorithms that perform worse than the starting one
            for (AlgorithmWrapper ia : interleaved.keySet()) {
                if (interleaved.get(ia) - initialBest > confPrecision) {
                    toRemove.add(ia);
                }
            }

            // if we did not drop any we drop in any case one
            if (toRemove.isEmpty()) {
                AlgorithmWrapper worst = null;
                double worstValue = 0;
                for (AlgorithmWrapper ia : interleaved.keySet()) {
                    if (interleaved.get(ia) > worstValue) {
                        worstValue = interleaved.get(ia);
                        worst = ia;
                    }
                }
                toRemove.add(worst);
            }
            for (AlgorithmWrapper ia : toRemove) {
//                System.out.println("- Dropping " + ia + " " + interleaved.get(ia) + ".");
                interleaved.remove(ia);
            }
            toRemove.clear();
//            System.out.println("");
        }

        // continuing with the winning one and recompute the time limits
        remainingTimeForActualSearch = getTimeLimit() - getElapsedTime();
        beginTime = getElapsedTime();
        hh = algoBest.getAlgo();
//        System.out.println("Continuing with " + hh + " for the remaining " + remainingTimeForActualSearch / 1000 + " seconds.\n");
        while (!hasTimeExpired()) {
            hh.step();
        }

//        System.out.println("done. [AT " + (getElapsedTime() - beginTime) / 1000 + "s]");
    }

    /**
     * The name of this current hyper heuristic.
     * 
     * @return a string representing the name of the hyper-heuristic
     */
    @Override
    public String toString() {
        return "Test Hyper Heuristic (iridia.ulb.ac.be)";
    }
}
