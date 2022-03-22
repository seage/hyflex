/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package iridia.hyperheuristics.specific;

import iridia.hyperheuristics.TunableHyperHeuristic;

/**
 * TunableHyperHeuristic tuned for the BinPacking problem.
 */
public class TunedSAT extends TunableHyperHeuristic {

    /**
     * Constructor of the Tuned_BinPacking.
     *
     * @param posInitialSolution position in the memory of the initial solution
     * @param posCurrentSolution position in the memory of the current solution
     * @param posCurrentSolution position in the memory of a temp location used internally
     * @param posBackupSolution position in the memory of a backup location used internally
     */
    public TunedSAT(int posInitialSolution, int posCurrentSolution, int posTempSolution, int posBackupSolution) {
        super(posInitialSolution, posCurrentSolution, posTempSolution, posBackupSolution);
        
        // previous tuning
        num_ruin_recreate = 1;
        intensity_of_muatation_ruin_recreate = 0.2;
        probability_acc_worsening_ruin_recreate = 0.4966;
        num_local_search = 50;
        depth_of_search_local_search = 0.6;
        probability_acc_worsening_local_search = 0.9265;
        probability_acc_worsening = 0.2341;
        probability_mutation = 0.09586;
        intensity_of_mutation = 0.10630;
        probability_restart = 0.0007728;
        
        // latest tuning
//        num_ruin_recreate = 1;
//        intensity_of_muatation_ruin_recreate = 0.23;
//        probability_acc_worsening_ruin_recreate = 0.34;
//        num_local_search = 54;
//        depth_of_search_local_search = 0.65;
//        probability_acc_worsening_local_search = 0.88;
//        probability_acc_worsening = 0.35;
//        probability_mutation = 0.14;
//        intensity_of_mutation = 0.085;
//        probability_restart = 0.011;

        // final tuning
//        num_ruin_recreate = 1;
//        intensity_of_muatation_ruin_recreate = 0.11;
//        probability_acc_worsening_ruin_recreate = 0.53;
//        num_local_search = 52;
//        depth_of_search_local_search = 0.53;
//        probability_acc_worsening_local_search = 0.9;
//        probability_acc_worsening = 0.14;
//        probability_mutation = 0.12;
//        intensity_of_mutation = 0.28;
//        probability_restart = 0.008;      
    }

    /**
     * Returns the short name of the algorithm.
     *
     * @return name of the algorithm
     */
    @Override
    public String toString() {
        return "TunedSAT";
    }     
}
