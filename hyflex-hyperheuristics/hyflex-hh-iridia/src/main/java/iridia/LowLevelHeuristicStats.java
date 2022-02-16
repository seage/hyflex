package iridia;

import AbstractClasses.ProblemDomain;

/**
 * This is a helper class to call low level heuristics.
 */
public class LowLevelHeuristicStats extends LowLevelHeuristic {

    private Stats timesStats;
    private Stats solutionQualityStats;

    /**
     * Prepares the data structures for the low level heuristic.
     *
     * @param id identifier of the heuristic used in ProblemDomain applyHeurisitcs
     * @param pos incremental counter of heuristics of this type
     * @param posTempSolution solution used by low level heuristics (LS have to be applied until local minima is found)
     * @param type type of the heuristic
     */
    public LowLevelHeuristicStats(int id, int pos, int posTempSolution, ProblemDomain.HeuristicType type) {
        super(id, pos, posTempSolution, type);

        timesStats = new Stats<Integer>(Integer.MAX_VALUE);
        solutionQualityStats = new Stats<Double>(Double.POSITIVE_INFINITY);
    }

    /**
     * Executes the low level heuristics and stores solution quality and 
     * elapsed time for computing the statistics. LOCAL_SEARCH heuristics are
     * applied until a local minima is found (no improvement in quality).
     * If a single step of the LOCAL_SEARCH heuristics takes up more than 10
     * seconds then the function returns immediately without reaching the 
     * optimum. This function is used in fact in the preliminary phases of the
     * search where the main goal is to collect statistics and also incidentally
     * to progress with the search to avoid wasting time in the measures.
     * If the hyper-heuristics on the contrary wants to make a quick assessment 
     * of the performances it should use executeSingleStep where all heuristics
     * are run exactly once.
     *
     * @param src position of the source solution in the memory
     * @param dst position of the destination solution in the memory
     * @return solution quality
     */
    @Override
    public double execute(int src, int dst) {
        double qual = Double.POSITIVE_INFINITY;
        if (type == ProblemDomain.HeuristicType.LOCAL_SEARCH) {
            Shared.problem.copySolution(src, posTempSolution);
            double b_qual;
            long time = Shared.hyperHeuristic.getElapsedTime();
            do {
                b_qual = qual;
                qual = Shared.problem.applyHeuristic(id, posTempSolution, dst);
                Shared.problem.copySolution(dst, posTempSolution);
                if (Shared.hyperHeuristic.getElapsedTime() - time > 10000) {
                    break;
                }
            } while (!Shared.hyperHeuristic.hasTimeExpired() && b_qual - qual > confPrecision);
            solutionQualityStats.addMeasure(qual);
            timesStats.addMeasure(Shared.hyperHeuristic.getElapsedTime() - time);
            return qual;
        } else {
            qual = executeSingleStep(src, dst);
        }
        return qual;
    }

    /**
     * Executes the low level heuristics and stores solution quality and 
     * elapsed time for computing the statistics. A single step of LOCAL_SEARCH
     * is executed, no guarantees to reach a local minima.
     *
     * @param src position of the source solution in the memory
     * @param dst position of the destination solution in the memory
     * @return solution quality
     */
    public double executeSingleStep(int src, int dst) {
        long time = Shared.hyperHeuristic.getElapsedTime();
        double qual = Shared.problem.applyHeuristic(id, src, dst);
        solutionQualityStats.addMeasure(qual);
        timesStats.addMeasure(Shared.hyperHeuristic.getElapsedTime() - time);
        return qual;
    }

    /**
     * Updates the statistics for this low level heuristic.
     */
    public void updateStats() {
        timesStats.updateStats();
        solutionQualityStats.updateStats();
    }

    /**
     * Returns the median time. Method updateStats has to be called before to
     * compute the aggregate statistics of the measures collected by
     * executeWithStats.
     *
     * @return median time for a single run of the low level heuristic
     */
    public int getMedianTime() {
        return timesStats.getMedian().intValue();
    }

    /**
     * Returns the median quality. Method updateStats has to be called before to
     * compute the aggregate statistics of the measures collected by
     * executeWithStats.
     *
     * @return median solution quality for a single run of the low level heuristic
     */
    public double getMedianQuality() {
        return solutionQualityStats.getMedian().doubleValue();
    }

    /**
     * Returns the number of time and quality measures stored for this low level
     * heuristics.
     *
     * @return number of measures stored
     */
    public int getNumberOfMeasures() {
        // timeStats and solutionQualityStats have the same size
        return timesStats.getNumberOfMeasures();
    }

    /**
     * Returns a string with a low level heuristic identifier plus quality and
     * time statistics.
     *
     * @return heuristic type, internal numbering, median, lower and upper quartiles of solution quality and time
     */
    @Override
    public String toString() {
        return String.format("%15s", type + "" + indexInHeuristicsOfType) + " | quality: " + 
               String.format("%12e %12e %12e", solutionQualityStats.getMedian().doubleValue(), solutionQualityStats.getQ1().doubleValue(), solutionQualityStats.getQ3().doubleValue()) + " | seconds: " + 
               String.format("%12e %12e %12e", timesStats.getMedian().intValue() / 1000.0, timesStats.getQ1().intValue() / 1000.0, timesStats.getQ3().intValue() / 1000.0) + " | [collected in " + timesStats.getNumberOfMeasures() + " measure(s)]";
    }
}
