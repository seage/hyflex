package iridia.hyperheuristics;

import AbstractClasses.ProblemDomain;
import iridia.Configuration;
import iridia.Shared;

/**
 * This class implements a TunableHyperHeuristic. The main difference between 
 * this HyperHeuristic and the other ones is that it does not call the low level
 * heuristics through the LowLevelHeuristic class but it calls them directly.
 * The local searches are not applied repeatedly until a local optimum is reached.
 * 
 * This heuristic performs restarts inside its step method. Subclasses redefine
 * the best tuned values for a specific problem.
 */
public class TunableHyperHeuristic implements Algorithm {

    protected int num_ruin_recreate = 10;
    protected double intensity_of_muatation_ruin_recreate = 0.9;
    protected double probability_acc_worsening_ruin_recreate = 0.4;
    protected int num_local_search = 10;
    protected double depth_of_search_local_search = 0.9;
    protected double probability_acc_worsening_local_search = 0.5;
    protected double probability_acc_worsening = 0.8;
    protected double probability_mutation = 0.2;
    protected double intensity_of_mutation = 0.9;
    protected double probability_restart = 0.005;
    private double currentObjFunctionValue = Double.POSITIVE_INFINITY;
    private double currentLocalObjFunctionValue = Double.POSITIVE_INFINITY;
    private double newObjFunctionValue;
    private int posInitialSolution;
    private int posCurrentSolution;
    private int posTempSolution;
    private int posBackupSolution;
    private double confPrecision;
    private double bestEver;

    /**
     * Constructor of the TunableHyperHeuristic.
     *
     * @param posInitialSolution position in the memory of the initial solution
     * @param posCurrentSolution position in the memory of the current solution
     * @param posCurrentSolution position in the memory of a temp location used internally
     * @param posBackupSolution position in the memory of a backup location used internally
     */
    public TunableHyperHeuristic(int posInitialSolution, int posCurrentSolution, int posTempSolution, int posBackupSolution) {
        this.posInitialSolution = posInitialSolution;
        this.posCurrentSolution = posCurrentSolution;
        this.posTempSolution = posTempSolution;
        this.posBackupSolution = posBackupSolution;

        bestEver = Double.POSITIVE_INFINITY;

        readConfiguration();
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
    }

    /**
     * Resets the algorithm.
     * Useful in case of restarts.
     */
    public void reset() {
        initialisation();
    }

    /**
     * Common operation for constructor and reset.
     */
    private void initialisation() {
        Shared.problem.copySolution(posInitialSolution, posTempSolution);
    }

    /**
     * Performs a step of the Simulated Annealing heuristic.
     *
     * @return current solution quality
     */
    public double step() {
        // ruin and recreate
        Shared.problem.setIntensityOfMutation(intensity_of_muatation_ruin_recreate);
        for (int i = 0; i < num_ruin_recreate; i++) {
            int[] hl = Shared.problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
            if (hl.length == 0) {
                break;
            }
            newObjFunctionValue = Shared.problem.applyHeuristic(hl[Shared.rng.nextInt(hl.length)], posTempSolution, posCurrentSolution);
            Shared.hyperHeuristic.hasTimeExpired(); // save solution
            if (currentLocalObjFunctionValue - newObjFunctionValue > confPrecision || Shared.rng.nextDouble() < probability_acc_worsening_ruin_recreate) {
                Shared.problem.copySolution(posCurrentSolution, posTempSolution);
                currentLocalObjFunctionValue = newObjFunctionValue;
            }
        }

        // local search
        Shared.problem.setDepthOfSearch(depth_of_search_local_search);
        for (int i = 0; i < num_local_search; i++) {
            int[] hl = Shared.problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
            if (hl.length == 0) {
                break;
            }
            newObjFunctionValue = Shared.problem.applyHeuristic(hl[Shared.rng.nextInt(hl.length)], posTempSolution, posCurrentSolution);
            Shared.hyperHeuristic.hasTimeExpired(); // save solution
            if (currentLocalObjFunctionValue - newObjFunctionValue > confPrecision || Shared.rng.nextDouble() < probability_acc_worsening_local_search) {
                Shared.problem.copySolution(posCurrentSolution, posTempSolution);
                currentLocalObjFunctionValue = newObjFunctionValue;
            }
        }

        // save 
        if (currentObjFunctionValue - currentLocalObjFunctionValue > confPrecision || Shared.rng.nextDouble() < probability_acc_worsening) {
            Shared.problem.copySolution(posTempSolution, posBackupSolution);
            currentObjFunctionValue = currentLocalObjFunctionValue;
        } else {
            Shared.problem.copySolution(posBackupSolution, posTempSolution);
        }

        // mutation
        if (Shared.rng.nextDouble() < probability_mutation) {
            Shared.problem.setIntensityOfMutation(intensity_of_mutation);
            int[] hl = Shared.problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
            if (hl.length != 0) {
                newObjFunctionValue = Shared.problem.applyHeuristic(hl[Shared.rng.nextInt(hl.length)], posTempSolution, posCurrentSolution);
                Shared.hyperHeuristic.hasTimeExpired(); // save solution            
                Shared.problem.copySolution(posCurrentSolution, posTempSolution);
            }
        }

        // restart
        if (Shared.rng.nextDouble() < probability_restart) {
            Shared.problem.initialiseSolution(posTempSolution);
        }

        return currentObjFunctionValue;
    }

    /**
     * Performs a step of the Simulated Annealing heuristic.
     * Limit execution time (useful for racing).
     * 
     * @param maxTime maximum time allocated for the step
     * @return current solution quality
     */
    public double stepLimited(long maxTime) {
        long start = Shared.hyperHeuristic.getElapsedTime();
        // ruin and recreate
        Shared.problem.setIntensityOfMutation(intensity_of_muatation_ruin_recreate);
        for (int i = 0; i < num_ruin_recreate; i++) {
            if (Shared.hyperHeuristic.getElapsedTime() - start > maxTime) {
                break;
            }
            int[] hl = Shared.problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
            if (hl.length == 0) {
                break;
            }
            newObjFunctionValue = Shared.problem.applyHeuristic(hl[Shared.rng.nextInt(hl.length)], posTempSolution, posCurrentSolution);
            if (newObjFunctionValue < bestEver) {
                bestEver = newObjFunctionValue;
            }
            if (currentLocalObjFunctionValue - newObjFunctionValue > confPrecision || Shared.rng.nextDouble() < probability_acc_worsening_ruin_recreate) {
                Shared.problem.copySolution(posCurrentSolution, posTempSolution);
                currentLocalObjFunctionValue = newObjFunctionValue;
            }
        }

        // local search
        Shared.problem.setDepthOfSearch(depth_of_search_local_search);
        for (int i = 0; i < num_local_search; i++) {
            if (Shared.hyperHeuristic.getElapsedTime() - start > maxTime) {
                break;
            }
            int[] hl = Shared.problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
            if (hl.length == 0) {
                break;
            }
            newObjFunctionValue = Shared.problem.applyHeuristic(hl[Shared.rng.nextInt(hl.length)], posTempSolution, posCurrentSolution);
            if (newObjFunctionValue < bestEver) {
                bestEver = newObjFunctionValue;
            }
            if (currentLocalObjFunctionValue - newObjFunctionValue > confPrecision || Shared.rng.nextDouble() < probability_acc_worsening_local_search) {
                Shared.problem.copySolution(posCurrentSolution, posTempSolution);
                currentLocalObjFunctionValue = newObjFunctionValue;
            }
        }

        // save 
        if (currentObjFunctionValue - currentLocalObjFunctionValue > confPrecision || Shared.rng.nextDouble() < probability_acc_worsening) {
            Shared.problem.copySolution(posTempSolution, posBackupSolution);
            currentObjFunctionValue = currentLocalObjFunctionValue;
        } else {
            Shared.problem.copySolution(posBackupSolution, posTempSolution);
        }

        // mutation
        if (Shared.rng.nextDouble() < probability_mutation) {
            Shared.problem.setIntensityOfMutation(intensity_of_mutation);
            int[] hl = Shared.problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
            if (hl.length != 0) {
                newObjFunctionValue = Shared.problem.applyHeuristic(hl[Shared.rng.nextInt(hl.length)], posTempSolution, posCurrentSolution);
                if (newObjFunctionValue < bestEver) {
                    bestEver = newObjFunctionValue;
                }
                Shared.problem.copySolution(posCurrentSolution, posTempSolution);
            }
        }
        // restart
        if (Shared.rng.nextDouble() < probability_restart) {
            Shared.problem.initialiseSolution(posTempSolution);
        }

        return bestEver;
    }

    /**
     * Returns the short name of the algorithm.
     *
     * @return name of the algorithm
     */
    @Override
    public String toString() {
        return "TunableHyperHeuristic";
    }
}
