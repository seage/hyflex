//--- Configuration D

package kubalik;
import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import BinPacking.BinPacking;
import FlowShop.FlowShop;
import PersonnelScheduling.PersonnelScheduling;
import SAT.SAT;
import travelingSalesmanProblem.TSP;
import VRP.VRP;

/**
 * This class shows how to run a selected hyper-heuristic on a selected problem domain.
 * It shows the minimum that must be done to test a hyper heuristic on a problem domain, and it is 
 * intended to be read before the ExampleRun2 class, which provides an example of a more complex set-up
 */
public class EvoCOPRun {

	public static void main(String[] args) {

            System.out.println("EvoCOP-IteratedLocalSearch");

            //--- SAT
//            int[] instanceSAT = {3, 3, 3, 5, 5, 5, 4, 10, 11};
            int[] instanceSAT = {};
            for(int i=0; i<instanceSAT.length; i++){
//                System.out.println("\n\n===========\nSAT " + instanceSAT[i] );
                ProblemDomain problem = new SAT(1234);
                HyperHeuristic hyper_heuristic_object = new EvoCOPHyperHeuristic(1); //--- seed
                problem.loadInstance(instanceSAT[i]);           //--- instance
                hyper_heuristic_object.setTimeLimit(650000);    //--- cas
                hyper_heuristic_object.loadProblemDomain(problem);
                hyper_heuristic_object.run();
                System.out.println("SAT " + instanceSAT[i] + " final: " + hyper_heuristic_object.getBestSolutionValue());
            }
            System.out.println("");

            //--- PersonnelScheduling
            int[] instancePS = {5,5,5,5,5};
            for(int i=0; i<instancePS.length; i++){
//                System.out.println("\n\n===========\nPersonnelScheduling " + instancePS[i] );
                ProblemDomain problem = new PersonnelScheduling(1234);
                HyperHeuristic hyper_heuristic_object = new EvoCOPHyperHeuristic(1); //--- seed
                problem.loadInstance(instancePS[i]);            //--- instance
                hyper_heuristic_object.setTimeLimit(650000);    //--- cas
                hyper_heuristic_object.loadProblemDomain(problem);
                hyper_heuristic_object.run();
                System.out.println("PS " + instancePS[i] + " final: " + hyper_heuristic_object.getBestSolutionValue());
            }
            System.out.println("");

            //--- FlowShop
            int[] instanceFS = {};
//            int[] instanceFS = {1,8,3,10,11};
            for(int i=0; i<instanceFS.length; i++){
//                System.out.println("\n\n===========\nFlowShop " + instanceFS[i] );
                ProblemDomain problem = new FlowShop(1234);
                HyperHeuristic hyper_heuristic_object = new EvoCOPHyperHeuristic(1); //--- seed
                problem.loadInstance(instanceFS[i]);           //--- instance
                hyper_heuristic_object.setTimeLimit(560000);    //--- cas
                hyper_heuristic_object.loadProblemDomain(problem);
                hyper_heuristic_object.run();
                System.out.println("FS " + instanceFS[i] + " final: " + hyper_heuristic_object.getBestSolutionValue());
            }
            System.out.println("");
//
            //--- BinPacking
            int[] instanceBP = {};
//            int[] instanceBP = {7,1,9,10,11};
            for(int i=0; i<instanceBP.length; i++){
                ProblemDomain problem = new BinPacking(1234);
                HyperHeuristic hyper_heuristic_object = new EvoCOPHyperHeuristic(1); //--- seed
                problem.loadInstance(instanceBP[i]);           //--- instance
                hyper_heuristic_object.setTimeLimit(560000);    //--- cas
                hyper_heuristic_object.loadProblemDomain(problem);
                hyper_heuristic_object.run();
                System.out.println("BP " + instanceBP[i] + " final: " + hyper_heuristic_object.getBestSolutionValue());
            }

            //--- TSP
            int[] instanceTSP = {};
            for(int i=0; i<instanceTSP.length; i++){
//                System.out.println("\n\n===========\nBinPacking " + instanceBP[i] );
                ProblemDomain problem = new TSP(1234);
                HyperHeuristic hyper_heuristic_object = new EvoCOPHyperHeuristic(1); //--- seed
                problem.loadInstance(instanceTSP[i]);           //--- instance
                hyper_heuristic_object.setTimeLimit(560000);    //--- cas
                hyper_heuristic_object.loadProblemDomain(problem);
                hyper_heuristic_object.run();
                System.out.println("TSP " + instanceTSP[i] + " final: " + hyper_heuristic_object.getBestSolutionValue());
            }

            //--- VRP
            int[] instanceVRP = {};
            for(int i=0; i<instanceVRP.length; i++){
//                System.out.println("\n\n===========\nBinPacking " + instanceBP[i] );
                ProblemDomain problem = new VRP(1234);
                HyperHeuristic hyper_heuristic_object = new EvoCOPHyperHeuristic(1); //--- seed
                problem.loadInstance(instanceVRP[i]);           //--- instance
                hyper_heuristic_object.setTimeLimit(560000);    //--- cas
                hyper_heuristic_object.loadProblemDomain(problem);
                hyper_heuristic_object.run();
                System.out.println("VRP " + instanceVRP[i] + " final: " + hyper_heuristic_object.getBestSolutionValue());
            }
        }
}
