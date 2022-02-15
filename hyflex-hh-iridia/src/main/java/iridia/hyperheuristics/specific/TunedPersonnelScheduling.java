package iridia.hyperheuristics.specific;

import iridia.hyperheuristics.TunableHyperHeuristic;

/**
 * TunableHyperHeuristic tuned for the BinPacking problem.
 */
public class TunedPersonnelScheduling extends TunableHyperHeuristic {

    /**
     * Constructor of the Tuned_BinPacking.
     *
     * @param posInitialSolution position in the memory of the initial solution
     * @param posCurrentSolution position in the memory of the current solution
     * @param posCurrentSolution position in the memory of a temp location used internally
     * @param posBackupSolution position in the memory of a backup location used internally
     */
    public TunedPersonnelScheduling(int posInitialSolution, int posCurrentSolution, int posTempSolution, int posBackupSolution) {
        super(posInitialSolution, posCurrentSolution, posTempSolution, posBackupSolution);

        // previous tuning
//        num_ruin_recreate = 20;
//        intensity_of_muatation_ruin_recreate = 0.2;
//        probability_acc_worsening_ruin_recreate = 0.5514;
//        num_local_search = 20;
//        depth_of_search_local_search = 1.0;
//        probability_acc_worsening_local_search = 0.6259;
//        probability_acc_worsening = 0.51470;
//        probability_mutation = 0.02821;
//        intensity_of_mutation = 0.6672;
//        probability_restart = 0.6598;

        // latest tuning
//        num_ruin_recreate = 16;
//        intensity_of_muatation_ruin_recreate = 0.29;
//        probability_acc_worsening_ruin_recreate = 0.46;
//        num_local_search = 12;
//        depth_of_search_local_search = 0.84;
//        probability_acc_worsening_local_search = 0.76;
//        probability_acc_worsening = 0.46;
//        probability_mutation = 0.16;
//        intensity_of_mutation = 0.57;
//        probability_restart = 0.61;    

        // final tuning
        num_ruin_recreate = 11;
        intensity_of_muatation_ruin_recreate = 0.21;
        probability_acc_worsening_ruin_recreate = 0.54;
        num_local_search = 15;
        depth_of_search_local_search = 0.85;
        probability_acc_worsening_local_search = 0.72;
        probability_acc_worsening = 0.54;
        probability_mutation = 0.2;
        intensity_of_mutation = 0.43;
        probability_restart = 0.67;    
    }

    /**
     * Returns the short name of the algorithm.
     *
     * @return name of the algorithm
     */
    @Override
    public String toString() {
        return "TunedPersonnelScheduling";
    }     
}
