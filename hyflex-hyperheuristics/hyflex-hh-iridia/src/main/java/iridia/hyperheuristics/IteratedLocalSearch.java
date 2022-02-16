package iridia.hyperheuristics;

import AbstractClasses.ProblemDomain;
import iridia.Configuration;
import iridia.Shared;

/**
 * This class implements a ILS procedure:
 *
 * determine initial candidate solution initialSolution
 * perform subsidiary local search on initialSolution
 * while termination criterion is not satisfied
 *   backupSolution = currentSolution
 *   perform perturbation on currentSolution
 *   perform subsidiary local search on currentSolution
 *   based on acceptance criterion, keep currentSolution or revert to backupSolution
 * done
 *
 * Heuristics are chosen randomly.
 */
public class IteratedLocalSearch implements Algorithm {

    protected int posInitialSolution;
    protected int posCurrentSolution;
    protected int posTempSolution;
    protected int posBackupSolution;
    protected double qualBackupSolution;
    protected double qualCurrentSolution;

    protected double confPrecision;
    protected double confProbabilityAcceptingWorsening;
    protected double confPerturbationSize;

    /**
     * Constructor of the Iterated local search heuristic.
     * Starting from an initial solution it modifies it performing a LS.
     *
     * @param posInitialSolution position in the memory of the initial solution
     * @param posCurrentSolution position in the memory of the current solution
     * @param posCurrentSolution position in the memory of a temp location used internally
     * @param posBackupSolution position in the memory of the backup solution
     */
    public IteratedLocalSearch(int posInitialSolution, int posCurrentSolution, int posTempSolution, int posBackupSolution) {
        qualBackupSolution = Double.POSITIVE_INFINITY;
        this.posInitialSolution = posInitialSolution;
        this.posCurrentSolution = posCurrentSolution;
        this.posTempSolution = posTempSolution;
        this.posBackupSolution = posBackupSolution;

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
        confProbabilityAcceptingWorsening = conf.getDouble("ILS_ProbabilityAcceptingWorsening");
        confPerturbationSize = conf.getDouble("ILS_PerturbationSize");
    }

    /**
     * Common operation for constructor and reset.
     * We apply a random LS to get to the first minima.
     */
    private void initialisation() {
        qualCurrentSolution = (Shared.availableHeuristics.getRandomHeuristicOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH)).execute(posInitialSolution, posCurrentSolution);
    }

    /**
     * Performs a backup of the current solution, a perturbation and a subsidiary
     * local search. If the acceptance criterion is not met the backup solution
     * is restored.
     *
     * @return current solution quality
     */
    public double step() {
        backup();
        perturbation();
        subsidiaryLocalSearch();
        if (!acceptance()) {
            Shared.problem.copySolution(posBackupSolution, posCurrentSolution);
            qualCurrentSolution = qualBackupSolution;
        }
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
     * Backups current solution.
     */
    private void backup() {
        Shared.problem.copySolution(posCurrentSolution, posBackupSolution);
        qualBackupSolution = qualCurrentSolution;
    }

    /**
     * Applies a low level mutational heuristic. The solution quality does not
     * necessarily worsen.
     */
    protected void perturbation() {
        for (int i = 0; i < confPerturbationSize; i++) {
            if (Shared.hyperHeuristic.hasTimeExpired()) {
                return;
            }
            qualCurrentSolution = (Shared.availableHeuristics.getRandomHeuristicOfType(ProblemDomain.HeuristicType.MUTATION)).execute(posCurrentSolution, posTempSolution);
            Shared.problem.copySolution(posTempSolution, posCurrentSolution);
        }
    }

    /**
     * Applies a random subsidiary local search
     */
    protected void subsidiaryLocalSearch() {
        qualCurrentSolution = (Shared.availableHeuristics.getRandomHeuristicOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH)).execute(posCurrentSolution, posTempSolution);
        Shared.problem.copySolution(posTempSolution, posCurrentSolution);
    }

    /**
     * Always accepts an improving solution, or a worsening solution with
     * probability probabilityAcceptingWorsening.
     *
     * @return if the solution has to be kept and backup at the next step
     */
    protected boolean acceptance() {
        return (qualBackupSolution - qualCurrentSolution > confPrecision || Shared.rng.nextFloat() < confProbabilityAcceptingWorsening);
    }

    /**
     * Returns the short name of the algorithm.
     *
     * @return name of the algorithm
     */
    @Override
    public String toString() {
        return "IteratedLocalSearch";
    }
}
