package iridia.hyperheuristics;

import AbstractClasses.ProblemDomain;
import iridia.Shared;

/**
 * This class implements an Iterated Local Search as described on paper:
 *
 * E. K. Burke, T. Curtois, M. Hyde, G. Kendall, G. Ochoa, S. Petrovic,
 * J. A. Vazquez-Rodriguez and M. Gendreau (2010) Iterated Local Search vs.
 * Hyper-heuristics: Towards General-purpose Search Algorithms,
 * IEEE Congress on Evolutionary Computation (CEC 2010), IEEE PRess, pp. 3073-3080.
 *
 * This class assumes that the Local Search low level heuristics are sorted
 * by increasing median time.
 */
public class ASAP_IteratedLocalSearch extends IteratedLocalSearch {
    private int numLSheuristics;

    /**
     * Constructor of the Iterated local search heuristic.
     * Starting from an initial solution it modifies it performing a LS.
     *
     * @param posInitialSolution position in the memory of the initial solution for ILS
     * @param posCurrentSolution position in the memory of the current solution for ILS
     * @param posCurrentSolution position in the memory of a temp location used internally by ILS
     * @param posBackupSolution position in the memory of the backup solution of ILS
     */
    public ASAP_IteratedLocalSearch(int posInitialSolution, int posCurrentSolution, int posTempSolution, int posBackupSolution) {
        super(posInitialSolution, posCurrentSolution, posTempSolution, posBackupSolution);
        numLSheuristics = (Shared.availableHeuristics.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH)).size();

    }
    
    /**
     * Applies a low level mutational or ruin and recreate heuristic.
     */
    @Override
    protected void perturbation() {
        if (Shared.rng.nextBoolean()) {
            qualCurrentSolution = (Shared.availableHeuristics.getRandomHeuristicOfType(ProblemDomain.HeuristicType.MUTATION)).execute(posCurrentSolution, posTempSolution);
        } else {
            qualCurrentSolution = (Shared.availableHeuristics.getRandomHeuristicOfType(ProblemDomain.HeuristicType.RUIN_RECREATE)).execute(posCurrentSolution, posTempSolution);
        }
        Shared.problem.copySolution(posTempSolution, posCurrentSolution);
    }

    /**
     * Applies all local search heuristics in increasing time.
     */
    @Override
    protected void subsidiaryLocalSearch() {
        for (int i = 0; i < numLSheuristics; i++) {
            qualCurrentSolution = (Shared.availableHeuristics.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH).get(i)).execute(posCurrentSolution, posTempSolution);
            Shared.problem.copySolution(posTempSolution, posCurrentSolution);
        }
    }

    /**
     * Accepts only improving solutions.
     *
     * @return if the solution has to be kept and backup at the next step
     */
    @Override
    protected boolean acceptance() {
        return (qualBackupSolution - qualCurrentSolution > confPrecision);
    }

    /**
     * Returns the short name of the algorithm.
     *
     * @return name of the algorithm
     */
    @Override
    public String toString() {
        return "ASAP_IteratedLocalSearch";
    }
}
