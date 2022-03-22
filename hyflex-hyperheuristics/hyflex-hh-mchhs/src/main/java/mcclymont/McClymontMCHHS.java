package mcclymont;

/**
 * edited by Dave Omrai
 */

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

public class McClymontMCHHS extends HyperHeuristic {

    int numberOfSolutions = 5;

    public McClymontMCHHS(long seed) {
        super(seed);
    }

    public void solve(ProblemDomain problem) {
        int i, h = 0, a, b, c, prev = 0;
        double maxDelta = 0, obj;
        boolean flag;
        long maxTime = 0, time;
        //initialise the population
        double[] objectives = new double[this.numberOfSolutions * 2];
        int[] sinceLastAccept = new int[this.numberOfSolutions];
        boolean[] primaryActive = new boolean[this.numberOfSolutions];

        //it is often a good idea to record the number of low level
        //heuristics, as this changes depending on the problem domain
        int number_of_heuristics = problem.getNumberOfHeuristics();
        boolean[] crossover = new boolean[number_of_heuristics];
        boolean[] localSearch = new boolean[number_of_heuristics];
        int[] crossoverIndicies = problem.getHeuristicsOfType(
                ProblemDomain.HeuristicType.CROSSOVER);
        int[] localSearchIndicies = problem.getHeuristicsOfType(
                ProblemDomain.HeuristicType.LOCAL_SEARCH);
        for (i = 0; i < crossoverIndicies.length; i++) {
            crossover[crossoverIndicies[i]] = true;
        }
        for (i = 0; i < localSearchIndicies.length; i++) {
            localSearch[localSearchIndicies[i]] = true;
        }
        double[][] weights = new double[number_of_heuristics][number_of_heuristics];

        //initialise the variable which keeps track of the current
        //objective function value
        problem.setMemorySize(1 + this.numberOfSolutions * numberOfSolutions);

        for (i = 0; i < this.numberOfSolutions; i++) {

            //randomly initialise the solution at this index
            problem.initialiseSolution(i);

            //save the objective function value of the solution at this index
            objectives[i] = problem.getFunctionValue(i);

            //randomly initialise the solution at this index
            problem.initialiseSolution(i + this.numberOfSolutions);

            //save the objective function value of the solution at this index
            objectives[i + this.numberOfSolutions] = problem.getFunctionValue(i + 
this.numberOfSolutions);
        }

        //the main loop of any hyper-heuristic, which checks if the
        //time limit has been reached
        i = 0;
        while (!hasTimeExpired()) {
            sinceLastAccept[i]++;
            prev = h;
            h = select(h, weights);
            a = primaryActive[i] ? 0 : this.numberOfSolutions;
            b = primaryActive[i] ? this.numberOfSolutions : 0;
            time = System.nanoTime();
            if (crossover[h]) {
                do {
                    c = rng.nextInt(this.numberOfSolutions);
                } while (c == i);
                c += primaryActive[c] ? 0 : this.numberOfSolutions;
                objectives[i + b] = problem.applyHeuristic(h, i + a, c, i + b);
            } else {
                objectives[i + b] = problem.applyHeuristic(h, i + a, i + b);
            }
            if(!hasTimeExpired())
            {
                time = System.nanoTime() - time;
                if (maxTime < time) {
                    maxTime = time;
                }
                obj = objectives[i + a] - objectives[i + b];
                if (Math.abs(obj) > maxDelta) {
                    maxDelta = Math.abs(obj);
                }
                obj /= maxDelta;
                update(weights, prev, h, obj * (1 - (time / (double) maxTime)));
                if (a == 0) {
                    if (objectives[i] > objectives[i + this.numberOfSolutions] || rng.nextBoolean()) 
{
                        if(primaryActive[i]) sinceLastAccept[i] = 0;
                        primaryActive[i] = false;
                    } else {
                        if (localSearchIndicies.length > 0 && rng.nextDouble() > 0.35) {
                            time = System.nanoTime();
                            h = localSearchIndicies[rng.nextInt(localSearchIndicies.length)];
                            problem.applyHeuristic(h, i + b, i + b);
                            flag = primaryActive[i];
                            primaryActive[i] = objectives[i] >
                                    objectives[i + this.numberOfSolutions];
                            if(flag != primaryActive[i]){
                                sinceLastAccept[i] = 0;
                            } else sinceLastAccept[i]++;
                            time = System.nanoTime() - time;
                            if (maxTime < time) {
                                maxTime = time;
                            }
                            obj = objectives[i + a] - objectives[i + b];
                            if (Math.abs(obj) > maxDelta) {
                                maxDelta = Math.abs(obj);
                            }
                            obj /= maxDelta;
                            update(weights, prev, h, obj * (1 - (time / (double) maxTime)));
                        } else {
                            if(!primaryActive[i]) sinceLastAccept[i] = 0;
                            primaryActive[i] = true;
                        }
                    }
                } else {
                    if (objectives[i] < objectives[i + this.numberOfSolutions] || rng.nextDouble() > 
0.35) {
                        if(!primaryActive[i]) sinceLastAccept[i] = 0;
                        primaryActive[i] = true;
                    } else {
                        if (localSearchIndicies.length > 0 && rng.nextBoolean()) {
                            time = System.nanoTime();
                            h = localSearchIndicies[rng.nextInt(localSearchIndicies.length)];
                            problem.applyHeuristic(h, i + b, i + b);
                            flag = primaryActive[i];
                            primaryActive[i] = objectives[i] >
                                    objectives[i + this.numberOfSolutions];
                            if(flag != primaryActive[i]){
                                sinceLastAccept[i] = 0;
                            } else sinceLastAccept[i]++;
                            time = System.nanoTime() - time;
                            if (maxTime < time) {
                                maxTime = time;
                            }
                            obj = objectives[i + a] - objectives[i + b];
                            if (Math.abs(obj) > maxDelta) {
                                maxDelta = Math.abs(obj);
                            }
                            obj /= maxDelta;
                            update(weights, prev, h, obj * (1 - (time / (double) maxTime)));
                            primaryActive[i] = objectives[i] >
                                    objectives[i + this.numberOfSolutions];
                        } else {
                            if(primaryActive[i]) sinceLastAccept[i] = 0;
                            primaryActive[i] = false;
                        }
                    }
                }

                if(rng.nextDouble() < sinceLastAccept[i] / 5d)
                {
                    primaryActive[i] = !primaryActive[i];
                    sinceLastAccept[i] = 0;
                }
            }

            if (++i >= this.numberOfSolutions) {
                i = 0;
            }
        }
    }

    public int select(int current, double[][] weights) {
        double total = 0;
        for (int i = 0; i < weights[current].length; i++) {
            total += weights[current][i] + 0.01;
        }
        total = rng.nextDouble() * total;
        for (int i = 0; i < weights[current].length && total >= 0; i++) {
            total -= (weights[current][i] + 0.01);
            if (total <= 0) {
                return i;
            }
        }
        return weights[current].length - 1;
    }

    /**
     * this method must be implemented, to provide a different name for each hyper-heuristic
     * @return a string representing the name of the hyper-heuristic
     */
    public String toString() {
        return "Single objective Markov chain Hyper-heuristic (MCHH-S)";
    }

    private void update(double[][] weights, int prev, int index, double performance) {
        weights[prev][index] = Math.max(0, Math.min(100,
                weights[prev][index] + performance));
    }
}