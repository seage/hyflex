/*
 Copyright 2011 karim@computer.org

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package acuna;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

/**
 *
 * @author Karim
 * 
 * edited by Dave Omrai
 * 
 */
public class GISS extends HyperHeuristic {

    private static final double current_temp=25;
    
    public GISS(long seed) {
        super(seed);
    }

    protected boolean isCrossover(int heuristic, int[] crossover_heuristics) {
        for (int in : crossover_heuristics) {
            if (in == heuristic) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void solve(ProblemDomain problem) {

        int iterator = 0;

        problem.setMemorySize(3);

        int number_of_heuristics = problem.getNumberOfHeuristics();

        int[] mutation_array = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
        int[] ruin_array = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
        int[] crossover_array = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);

        Temperature tmp = new Temperature(current_temp);

        problem.initialiseSolution(0);
        problem.copySolution(0, 1);
        problem.initialiseSolution(2);

        int heuristic_to_apply = 0;

        double new_obj_function_value = 0.0;

        double current_obj_function_value = problem.getFunctionValue(0);

        boolean crossover_flag = false;

        double delta = 0.0;

        double variable_aleatorea = 0.0;

        double probability = 0.0;

        MersenneTwisterFast mt_random = new MersenneTwisterFast(1337);

        //0 : Last Best Value
        //1 : Current Best Value
        //2 : Temporal Solution


        while (!hasTimeExpired()) {
            heuristic_to_apply = mt_random.nextInt(number_of_heuristics);
            crossover_flag = isCrossover(heuristic_to_apply, crossover_array);
            if (crossover_flag) {
                new_obj_function_value = problem.applyHeuristic(heuristic_to_apply, 0, 1, 2);
            } else {
                new_obj_function_value = problem.applyHeuristic(heuristic_to_apply, 0, 2);
            }
            delta = current_obj_function_value - new_obj_function_value;
            if(delta==0){
                iterator++;
            }
            if (delta >= 0) {
                //(2) Best on iteration
                //(0) New Best
                //(1) Old Best
                problem.copySolution(0, 1);
                problem.copySolution(2, 0);
                current_obj_function_value = new_obj_function_value;
                if(delta==0)iterator++;else iterator=0;
            } else {
                variable_aleatorea = mt_random.nextDouble();
                probability = tmp.expDescend(new_obj_function_value, current_obj_function_value, super.getElapsedTime(), super.getTimeLimit());
                if (variable_aleatorea <= probability) {
                    problem.copySolution(0, 1);
                    problem.copySolution(2, 0);
                    current_obj_function_value = new_obj_function_value;
                    iterator=0;
                }
            }//fin if-else
            
            int limite_iteraciones = problem.getNumberOfHeuristics()*problem.getNumberOfHeuristics();
            //Relajacion de la condicion de termino
            if(this.getBestSolutionValue()*1.5>current_obj_function_value)
                limite_iteraciones = limite_iteraciones*2;
            
            if (iterator > limite_iteraciones) {
                iterator=0;
                tmp.setCurrentTemperature(current_temp);
                
                if(mt_random.nextBoolean(0.2)){
                    problem.setDepthOfSearch(problem.getDepthOfSearch()*1.1);
                    problem.setIntensityOfMutation(problem.getIntensityOfMutation()*1.1);
                }
                if (ruin_array.length > 0) {
                    current_obj_function_value = problem.applyHeuristic(ruin_array[mt_random.nextInt(ruin_array.length)], 0, 0);
                    current_obj_function_value = problem.applyHeuristic(mutation_array[mt_random.nextInt(mutation_array.length)], 0, 0);

                }else{
                    current_obj_function_value = problem.applyHeuristic(mutation_array[mt_random.nextInt(mutation_array.length)], 0, 0);
                }
                
            }//fin iterator
        }//fin-while
    }//fin solve

    @Override
    public String toString() {
        return "GISS1";
    }
}
