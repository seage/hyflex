/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package iridia.hyperheuristics.specific;

import iridia.hyperheuristics.TunableHyperHeuristic;

/**
 * TunableHyperHeuristic tuned for the BinPacking problem.
 */
public class TunedBinPacking extends TunableHyperHeuristic {

    /**
     * Constructor of the Tuned_BinPacking.
     *
     * @param posInitialSolution position in the memory of the initial solution
     * @param posCurrentSolution position in the memory of the current solution
     * @param posCurrentSolution position in the memory of a temp location used internally
     * @param posBackupSolution position in the memory of a backup location used internally
     */
    public TunedBinPacking(int posInitialSolution, int posCurrentSolution, int posTempSolution, int posBackupSolution) {
        super(posInitialSolution, posCurrentSolution, posTempSolution, posBackupSolution);
        // previous tuning
//        num_ruin_recreate = 5;
//        intensity_of_muatation_ruin_recreate = 0.8;
//        probability_acc_worsening_ruin_recreate = 0.04593;
//        num_local_search = 5;
//        depth_of_search_local_search = 0.4;
//        probability_acc_worsening_local_search = 0.5242;
//        probability_acc_worsening = 0.006053;
//        probability_mutation = 0.2173;
//        intensity_of_mutation = 0.1012;
//        probability_restart = 0.07962;
        
        // last tuning
//        num_ruin_recreate = 4;
//        intensity_of_muatation_ruin_recreate = 0.69;
//        probability_acc_worsening_ruin_recreate = 0.057;
//        num_local_search = 4;
//        depth_of_search_local_search = 0.46;
//        probability_acc_worsening_local_search = 0.43;
//        probability_acc_worsening = 0.00045;
//        probability_mutation = 0.29;
//        intensity_of_mutation = 0.11;
//        probability_restart = 0.014;      
        
        // final tuning
        num_ruin_recreate = 4;
        intensity_of_muatation_ruin_recreate = 0.69;
        probability_acc_worsening_ruin_recreate = 0.057;
        num_local_search = 4;
        depth_of_search_local_search = 0.46;
        probability_acc_worsening_local_search = 0.43;
        probability_acc_worsening = 0.00045;
        probability_mutation = 0.29;
        intensity_of_mutation = 0.11;
        probability_restart = 0.014;                           
    }

    /**
     * Returns the short name of the algorithm.
     *
     * @return name of the algorithm
     */
    @Override
    public String toString() {
        return "TunedBinPacking";
    }     
}
