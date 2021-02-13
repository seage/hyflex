/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kubalik;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author 
 */
public class EvoCOPHyFlexManager {

    //-------
    public static ProblemDomain problem;        //--- KUBA: tohle bude delat vsechny operace s heuiristikami a se solution memory
    //---
    public static double Temperature;   //--- 0.0005 pravdepodobnost, ze prijmu i horsi reseni
    public static int solutionMemorySize;
    public static int operationalLength = 4;    //--- pocet pracovnich reseni v solutionMemory
    public static int tabuListLength = 10;      //--- delka tabu listu
    public static int currTabuId = 0;           //--- aktualni pozice pro pridani prvku do tabuListu
                                                //--- relativni pozice vzhledem k cele solutionMemory

    //---
    public static int[] local_search_heuristics;
    public static int[] mutation_heuristics;
    public static int[] crossover_heuristics;
    public static int[] ruin_recreate_heuristics;
    //initialise the variable which keeps track of the current objective function value
    public static double[] current_obj_function_values;
    public static int best_solution_index;
    public static double best_solution_value;

    //---
    public static long lastRestartTime;         //--- okamzik posledniho restartu
    public static long startTime, endTime;      //--- zacatek celeho vypoctu, mezicas
    public static long minRestartRate;          //--- minimalni frekvence restartu
                                                //--- timeLimit / minRestartRate
    public static long nonImprovementCount;     //--- pocet nezlepsujicich ohodnoceni
    public static long adjustProbRate;          //--- frequence upravy pravdepodobnosti
//    public static long expectedRestartRate;     //--- ocekavana doba mezi restarty
    public static long timeLimit;               //--- cas na cely vypocet

    //---
    public static int nbActiveLLH;      //--- aktualni pocet aktivnich LLh v prototypu HH
    public static int maxActiveLLH;     //--- maximalni pocet aktivnich LLH v prototypu
    //--- Parametry pro minimalni meze DOS a IOM
    public static double addValueLocalLH;
    public static double addValueMutationLH;
    public static double addValueRuinLH;
    public static double changeValueAction;
    //---
    public static double startMinLocalLLH;
    public static double startMinMutationLLH;
    public static double startMinRuinLLH;
    //--- restarts parameters
    public static double startRecreate;
    public static double addRecreate;
    public static double startMutation;
    public static double addMutation;
    public static int ruinRate;
    public static int mutationRate;
    //---
    public static double minLocalLLH;
    public static double minMutationLLH;
    public static double minRuinLLH;
    //---
    public static double maxLocalLLH;
    public static double maxMutationLLH;
    public static double maxRuinLLH;

    //-------
    private EvoCOPRandomGenerator rand = EvoCOPRandomGenerator.getInstance();
    private Random generator = rand.getGenerator();

    public EvoCOPHyFlexManager(ProblemDomain problem) {
        
        // inicializuj problem
        this.problem = problem;
//        this.maxActiveLLH = 10;     //--- maximalni velikost HH
//        this.nbActiveLLH = 2;       //--- zacina s 5 LLH v prototypu HH

        //obtain arrays of the indices of the low level heuristics which correspond to the different types.
        //the arrays will be set to 'null' if there are no low level heuristics of that type
        local_search_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
        mutation_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
        crossover_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
        ruin_recreate_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);

        //this code is from examplehyperheuristic2, and is explained fully in that example
        solutionMemorySize = operationalLength + tabuListLength;
        currTabuId = 1;     //--- aktualni pozice pro pridani noveho reseni do solutionMemory
        problem.setMemorySize(solutionMemorySize);
        current_obj_function_values = new double[solutionMemorySize];
        best_solution_index = 0;
        best_solution_value = Double.POSITIVE_INFINITY;
        for (int x = 0; x < solutionMemorySize; x++) {
                problem.initialiseSolution(x);
                current_obj_function_values[x] = problem.getFunctionValue(x);
                if (current_obj_function_values[x] < best_solution_value) {
                        best_solution_value = current_obj_function_values[x];
                        best_solution_index = x;
                        //---
                        problem.copySolution(x, 0);
                        current_obj_function_values[0] = current_obj_function_values[x];
                        problem.copySolution(x, 1);
                        current_obj_function_values[1] = current_obj_function_values[x];
                        problem.copySolution(x, 2);
                        current_obj_function_values[2] = current_obj_function_values[x];
                        problem.copySolution(x, 3);
                        current_obj_function_values[3] = current_obj_function_values[x];
                }
        }
    }

    /*
     * Returns random number in given interval
     */
    public double randomNumber(double min, double max) {
        return generator.nextDouble() * (max - min) + min;
    }
}
