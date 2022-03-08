package SAT;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

import AbstractClasses.ProblemDomain;


/**
 * This class implements the SAT problem domain.
 * @author Matthew Hyde. mvh@cs.nott.ac.uk
 */
public class SAT extends ProblemDomain {
	private static int defaultmemorysize = 2;
	private final int[] mutations = new int[]{0,1,2,3,4,5};
	private final int[] localSearches = new int[]{7,8};
	private final int[] ruin_recreate = new int[]{6};
	private final int[] crossovers = new int[]{9,10};

	private int numberOfClauses;
	private int numberOfVariables;
	private Clause[] clauses;

	private Solution[] solutionMemory;
	private Solution bestEverSolution;
	private double bestEverObjectiveFunction = Double.POSITIVE_INFINITY;

	private int lrepeats;
	private int mrepeats;

	/**
	 * Constructs a new SAT object with a seed for the random number generator.
	 * @param seed
	 */
	public SAT(long seed) {
		super(seed);
	}

	public void setDepthOfSearch(double depthOfSearch)
	{
		super.setDepthOfSearch(depthOfSearch);
		if (depthOfSearch <= 0.2) {
			lrepeats = 10;
		} else if (depthOfSearch <= 0.4) {
			lrepeats = 12;
		} else if (depthOfSearch <= 0.6) {
			lrepeats = 14;
		} else if (depthOfSearch <= 0.8) {
			lrepeats = 17;
		} else {//its 0.8<X<1.0
			lrepeats = 20;
		}
	}

	public void setIntensityOfMutation(double intensityOfMutation)
	{
		super.setIntensityOfMutation(intensityOfMutation);
		if (intensityOfMutation <= 0.2) {
			mrepeats = 1;
		} else if (intensityOfMutation <= 0.4) {
			mrepeats = 2;
		} else if (intensityOfMutation <= 0.6) {
			mrepeats = 3;
		} else if (intensityOfMutation <= 0.8) {
			mrepeats = 4;
		} else {//its 0.8<X<1.0
			mrepeats = 5;
		}
	}

	private LinkedList<Integer> getVariablesWithHighestNetGain(Solution tempSolution) {
		int[] numbersofbrokenclauses = new int[numberOfVariables];//the number of broken clauses after each variable is flipped
		for (int x = 0; x < numberOfVariables; x++) {
			numbersofbrokenclauses[x] = tempSolution.testFlipForBrokenClauses(x);
		}//end looping variables
		//find the miminum value in the array:
		int minimum = numbersofbrokenclauses[0];//start with the first value
		//System.out.println("min " + minimum);
		for (int i = 0; i < numbersofbrokenclauses.length; i++) {
			if (numbersofbrokenclauses[i] < minimum) {
				minimum = numbersofbrokenclauses[i];// new minimum
			}
		}
		//System.out.println("min " + minimum);
		//there may be more than one value with the minimum, so put them all in a list and pick between them
		LinkedList<Integer> jointminimums = new LinkedList<Integer>();
		for (int i = 0; i < numberOfVariables; i++) {
			if (numbersofbrokenclauses[i] == minimum) {
				//System.out.print(i + " ");
				jointminimums.add(new Integer(i));
			}
		}//System.out.println();
		return jointminimums;
	}//end method getVariablesWithHighestNetGain

	private void applyHeuristic0(Solution tempSolution) {//GSAT
		for (int re = 0; re < mrepeats; re++) {
			//System.out.println();
			//System.out.println("heuristic 0");
			LinkedList<Integer> highestNetGains = getVariablesWithHighestNetGain(tempSolution);
			Integer i = highestNetGains.get(rng.nextInt(highestNetGains.size()));//select one variable at random from those that resulted in the minimum
			tempSolution.variables[i.intValue()].permanentflip();
			//System.out.println("variable " + i.intValue() + "flipped");
		}
	}//end method applyHeuristic0

	private void applyHeuristic1(Solution tempSolution) {//HSAT
		for (int re = 0; re < mrepeats; re++) {
			//same as GSAT but breaks ties in favour of the variable with the highest age
			LinkedList<Integer> jointminimums = getVariablesWithHighestNetGain(tempSolution);
			Variable largestage = tempSolution.variables[jointminimums.getFirst().intValue()];//start with the first one
			for (int x = 0; x < jointminimums.size(); x++) {
				Variable contender = tempSolution.variables[jointminimums.get(x).intValue()];
				//System.out.print(contender.number + ":" + contender.age + " ");
				if (contender.age > largestage.age) {//then its older so save it
					largestage = contender;
				}//end if its older
			}//end for looping variables
			//System.out.println();
			//System.out.println("best: " + largestage.number + ":" + largestage.age);
			largestage.permanentflip();
		}
	}//end method applyHeuristic1

	private Clause getRandomBrokenClause(Solution tempSolution) {
		Vector<Clause> brokenClauses = new Vector<Clause>();
		for (int x = 0; x < numberOfClauses; x++) {
			if (!(clauses[x].evaluate(tempSolution.variables))) {//if its broken
				brokenClauses.add(clauses[x]);
			}//end if
		}//end looping the clauses
		if (brokenClauses.isEmpty()) {//sometimes, there are no broken clauses, for a solved solution
			return null;
		} else {
			return brokenClauses.get(rng.nextInt(brokenClauses.size()));//return a random clause that is broken
		}
	}//end method getbrokenclauses

	private void flipRandomVariableInClause(Solution tempSolution, Clause c) {
		//select a random variable in it
		int variable = rng.nextInt(c.numberOfVariables());
		int specificVariableNumber = c.variablenumbers[variable];
		(tempSolution.variables[specificVariableNumber]).permanentflip();
	}//end method flipRandomVariableInClause

	private void flipRandomVariableInRandomBrokenClause(Solution tempSolution) {
		//get the broken clauses and select one
		Clause randomBrokenClause = getRandomBrokenClause(tempSolution);
		if (randomBrokenClause == null) {
			return;
		} else {
			flipRandomVariableInClause(tempSolution, randomBrokenClause);
		}
	}//end method flipRandomVariableInRandomBrokenClause

	private int getNegativeGain(Solution tempSolution, int variableToFlip) {
		//this works out the number of clauses that are satisfied before the flip of variable variableToFlip but unsatified after
		int numberNotNowSatisfied = tempSolution.testFlipForNegGain(variableToFlip);
		return numberNotNowSatisfied;
	}//end method getNegativeGain

	private void applyHeuristic2(Solution tempSolution) {//WalkSAT modified with no random walk
		for (int re = 0; re < mrepeats; re++) {
			//choose a broken clause, if flipping any of the variables doesnt break any other clauses, then flip it
			//if none are available then flip the one with best neg gain
			//get a broken clause
			Clause randomBrokenClause = getRandomBrokenClause(tempSolution);
			if (randomBrokenClause == null) {
				break;//if the instance is solved, there is no point in continuing
			}
			/*System.out.println("clause chosen: " + randomBrokenClause.clauseToString(tempSolution.variables));
			for (int c = 0; c < randomBrokenClause.numberOfVariables(); c++) {
				System.out.print(getNegativeGain(tempSolution, randomBrokenClause.variablenumbers[c]) + " ");
			}System.out.println();*/
			int[] negativeGains = new int[randomBrokenClause.numberOfVariables()];
			//loop the variables in this clause, and save the ones with negative gain 0
			Vector<Integer> variablesWithNegativeGain0 = new Vector<Integer>(randomBrokenClause.numberOfVariables());
			for (int x = 0; x < randomBrokenClause.numberOfVariables(); x++) {
				negativeGains[x] = getNegativeGain(tempSolution, randomBrokenClause.variablenumbers[x]);
				if (negativeGains[x] == 0) {
					variablesWithNegativeGain0.add(new Integer(randomBrokenClause.variablenumbers[x]));//save it
				}
			}
			if (!variablesWithNegativeGain0.isEmpty()) {//if any variables have negative gain 0, then flip one of those, chosen randomly
				int r = rng.nextInt(variablesWithNegativeGain0.size());
				int varnumber = variablesWithNegativeGain0.get(r).intValue();
				//System.out.println("flipped because of zero negative gain: " + varnumber);
				tempSolution.variables[varnumber].permanentflip();
			} else {//flip with minimal negative gain
				int minimum = negativeGains[0];//the negative gain of the first variable in this clause
				for (int x = 1; x < randomBrokenClause.numberOfVariables(); x++) {//loop the variables in the clause and get the one with the minimal negative gain
					if (negativeGains[x] < minimum) {//this variable has a better negative gain than the best so far
						minimum = negativeGains[x];
					}
				}//end for looping the variables in the clause to find the one with best negative gain
				//now we have the minimum we can get all of the variables with the minimum
				Vector<Integer> jointminimums = new Vector<Integer>(randomBrokenClause.numberOfVariables());//the numbers of the variables that have the best negative gain
				for (int i = 0; i < randomBrokenClause.numberOfVariables(); i++) {
					if (negativeGains[i] == minimum) {//if the negative gain of this variable in the clause is equal to the minimum we found before
						jointminimums.add(new Integer(randomBrokenClause.variablenumbers[i]));//store the number of the variable
					}
				}//end looping the variables in the clause
				tempSolution.variables[jointminimums.get(rng.nextInt(jointminimums.size()))].permanentflip();
			}//end if else any variables have a 0 neg gain
		}
	}//end method applyheuristic3

	private void applyHeuristic3(Solution tempSolution) {//flip random variable in a broken clause
		for (int r = 0; r < mrepeats; r++) {
			flipRandomVariableInRandomBrokenClause(tempSolution);
		}
	}//end method applyheuristic4

	private void applyHeuristic4(Solution tempSolution) {//flip a completely random variable
		for (int r = 0; r < mrepeats; r++) {
			(tempSolution.variables[rng.nextInt(numberOfVariables)]).permanentflip();
		}
	}

	private void applyHeuristic5(Solution tempSolution) {
		for (int re = 0; re < mrepeats; re++) {
			Clause randomBrokenClause = getRandomBrokenClause(tempSolution);
			if (randomBrokenClause == null) {
				break;//if the instance is solved, there is no point in continuing
			}
			applyNovelty(tempSolution, randomBrokenClause);
		}
	}//end method applyHeuristic6

	private void applyNovelty(Solution tempSolution, Clause randomBrokenClause) {
		double p = 0.7;//probability of flipping the minimal age variable
		int[] numbersofbrokenclauses = new int[randomBrokenClause.numberOfVariables()];//the number of broken clauses after each variable is flipped
		//loop the variables in the broken clause
		int minimalage = Integer.MAX_VALUE;
		//System.out.println("loop the variables in the clause and get their ages");
		for (int x = 0; x < randomBrokenClause.numberOfVariables(); x++) {
			numbersofbrokenclauses[x] = tempSolution.testFlipForBrokenClauses(randomBrokenClause.variablenumbers[x]);
			if (tempSolution.variables[randomBrokenClause.variablenumbers[x]].age < minimalage) {//keep track of the variable with minimal age
				minimalage = tempSolution.variables[randomBrokenClause.variablenumbers[x]].age;
			}
		}
		//get the maximal net gain. i.e the minimal new number of broken clauses
		//System.out.println("find the variable with the least broken clauses after flipping");
		int minimum = Integer.MAX_VALUE;//start with the first value
		int secondminimum = Integer.MAX_VALUE;//need secondmin later in method
		//System.out.println("min " + minimum);
		for (int i = 0; i < randomBrokenClause.numberOfVariables(); i++) {
			if (numbersofbrokenclauses[i] < minimum) {
				secondminimum = minimum;
				minimum = numbersofbrokenclauses[i];// new minimum
			} else if (numbersofbrokenclauses[i] < secondminimum) {
				secondminimum = numbersofbrokenclauses[i];
			}
		}
		//System.out.println("min " + minimum + " secondmin " + secondminimum);
		//there may be more than one value with the minimum, so put them all in a list and pick between them
		Vector<Integer> jointminimums = new Vector<Integer>(randomBrokenClause.numberOfVariables());
		for (int i = 0; i < randomBrokenClause.numberOfVariables(); i++) {
			if (numbersofbrokenclauses[i] == minimum) {
				jointminimums.add(new Integer(i));
			}
		}
		//System.out.print("Tied for the lead: ");
		/*for (int y = 0; y < jointminimums.size(); y++) {
	    	System.out.print(jointminimums.get(y).intValue());
	    }System.out.println();*/
		Integer i = jointminimums.get(rng.nextInt(jointminimums.size()));//select one variable at random from those that resulted in the minimum
		if (tempSolution.variables[randomBrokenClause.variablenumbers[i.intValue()]].age == minimalage) {//if its got the minimal age
			//System.out.println("This variable has the minimal age of " + minimalage);
			if (p < rng.nextDouble()) {//flip it with probability 1-p
				//System.out.println("it has been flipped");
				tempSolution.variables[randomBrokenClause.variablenumbers[i.intValue()]].permanentflip();
			} else {//flip the variable with the second highest net gain
				if (randomBrokenClause.numberOfVariables() == 1) {//there is no second highest
					tempSolution.variables[randomBrokenClause.variablenumbers[i.intValue()]].permanentflip();//flip the only one
				} else {
					//System.out.println("flip variable with second highest net gain");
					if (minimum == secondminimum) {//then the variable picked could be the same as the one with the largest age
						//System.out.println("there are two variables with the same highest net gain");
						while (true) {//keep picking random numbers until it picks a different one from before, because the one before had the largest age
							//System.out.println("choosing another");
							Integer q = jointminimums.get(rng.nextInt(jointminimums.size()));
							if (q.intValue() != i.intValue()) {//if it has picked a different one from those with the highest net gain
								tempSolution.variables[randomBrokenClause.variablenumbers[q.intValue()]].permanentflip(); break;
							}
						}//end while
					} else {
						jointminimums = new Vector<Integer>(randomBrokenClause.numberOfVariables());
						for (int q = 0; q < randomBrokenClause.numberOfVariables(); q++) {
							if (numbersofbrokenclauses[q] == secondminimum) {
								jointminimums.add(new Integer(q));}
						}
						if (jointminimums.isEmpty()) {
							System.out.println(minimum + " " + secondminimum + " " + randomBrokenClause.numberOfVariables());
						}
						//System.out.print("Tied for the second highest net gain: ");
						/*for (int y = 0; y < jointminimums.size(); y++) {
		    	    	System.out.print(jointminimums.get(y).intValue());
		    	    }System.out.println();*/
						Integer q = jointminimums.get(rng.nextInt(jointminimums.size()));
						//System.out.println("chosen second highest variable: " + q.intValue());
						tempSolution.variables[randomBrokenClause.variablenumbers[q.intValue()]].permanentflip();
					}//end if else
				}
			}
		} else {//if it hasnt got the minimal age in the clause then flip it
			tempSolution.variables[randomBrokenClause.variablenumbers[i.intValue()]].permanentflip();
		}
	}//end method applynovelty

	private void applyHeuristic6(Solution tempSolution) {//ruin and recreate
		double prop;
		if (intensityOfMutation <= 0.25) {
			prop = 1.0/5.0;
		} else if (intensityOfMutation <= 0.49) {
			prop = 2.0/5.0;
		} else if (intensityOfMutation <= 0.75) {
			prop = 3.0/5.0;
		} else {//its 0.75<X<1.0
			prop = 4.0/5.0;
		}
		int numofvariables = (int)((double)this.numberOfVariables * prop);
		//	System.out.println("prop " + propofvariables);
		//		for (Variable v : tempSolution.variables) {
		//			System.out.print(v.number + ":" + v.state + " ");
		//		}System.out.println();
		int[] variables_to_reinitialise = new int[numofvariables];
		int count = 0;
		while (count < numofvariables) {
			int chosen = rng.nextInt(this.numberOfVariables);
			//check it's not in the array already
			boolean alreadychosen = false;
			for (int y = 0; y < count; y++) {
				if (variables_to_reinitialise[y] == chosen) {
					alreadychosen = true;
					break;
				}
			}
			if (!alreadychosen) {
				variables_to_reinitialise[count] = chosen;
				count++;
			}
		}
		//now we have an array of unique variables

		for(int y : variables_to_reinitialise) {
			if (rng.nextBoolean()) {
				tempSolution.variables[y].permanentflip();
			}
		}
	}//end method applyHeuristic6

	private void applyHeuristic7(Solution tempSolution) {
		double currentres = evaluateObjectiveFunction(tempSolution);
		double res;
		for (int r = 0; r < lrepeats; r++) {
			Clause randomBrokenClause = getRandomBrokenClause(tempSolution);
			if (randomBrokenClause == null) {
				break;//because the solution is already perfect
			}
			int variable = rng.nextInt(randomBrokenClause.numberOfVariables());
			int specificVariableNumber = randomBrokenClause.variablenumbers[variable];
			(tempSolution.variables[specificVariableNumber]).testflip();
			res = evaluateObjectiveFunction(tempSolution);
			(tempSolution.variables[specificVariableNumber]).testflip();
			if (res <= currentres) {//found better solution
				(tempSolution.variables[specificVariableNumber]).permanentflip();
				currentres = res;
			}
		}
	}//end method applyheuristic7

	private void applyHeuristic8(Solution tempSolution) {//flip a completely random variable
		double currentres = evaluateObjectiveFunction(tempSolution);
		double res;
		for (int r = 0; r < lrepeats; r++) {
			int vtoflip = rng.nextInt(numberOfVariables);
			(tempSolution.variables[vtoflip]).testflip();
			res = evaluateObjectiveFunction(tempSolution);
			(tempSolution.variables[vtoflip]).testflip();
			if (res <= currentres) {//found better solution
				(tempSolution.variables[vtoflip]).permanentflip();
				currentres = res;
			}
		}
	}//end method applyheuristic8
	
	private void applyHeuristic9(Solution tempSolution1, Solution tempSolution2) {
		//two point crossover, one child
		int crossoverpoint1 = rng.nextInt(tempSolution1.variables.length);
		int crossoverpoint2 = rng.nextInt(tempSolution1.variables.length);
		//order smallest first
		if (crossoverpoint1 > crossoverpoint2) {int temp = crossoverpoint1; crossoverpoint1 = crossoverpoint2; crossoverpoint2 = temp;}
		for (int x = crossoverpoint1; x < crossoverpoint2; x++) {
			tempSolution1.variables[x] = tempSolution2.variables[x];
		}
	}//end method applyHeuristic9

	private void applyHeuristic10(Solution tempSolution1, Solution tempSolution2) {
		//one point crossover, one child
		int crossoverpoint1 = rng.nextInt(tempSolution1.variables.length);
		for (int x = crossoverpoint1; x < tempSolution1.variables.length; x++) {
			tempSolution1.variables[x] = tempSolution2.variables[x];
		}
	}//end method applyHeuristic10

	public int getNumberOfHeuristics() {
		return 11;
	}

	public double applyHeuristic(int heuristicID, int solutionSourceIndex, int solutionDestinationIndex) {
		long startTime = System.currentTimeMillis();
		Solution temporarysolution = deepCopyTheSolution(solutionMemory[solutionSourceIndex]);
		//check if its a crossover heuristic
		boolean isCrossover = false;
		int[] crossovers = getHeuristicsOfType(HeuristicType.CROSSOVER);
		if (!(crossovers == null)) {
			for (int x = 0; x < crossovers.length; x++) {
				if (crossovers[x] == heuristicID) {
					isCrossover = true;
					break;}
			}//end for looping the crossover heuristics
		}//end if
		if (isCrossover) {
			//nothing happens, because the crossovers will result in the same solution if the 2 inputs are the same
		} else {
			if (temporarysolution.numberOfBrokenClauses() == 0) {//the solution is perfect
				//nothing happens
			} else {
				if (heuristicID == 0) {
					applyHeuristic0(temporarysolution);//GSAT
				} else if (heuristicID == 1) {
					applyHeuristic1(temporarysolution);//HSAT
				} else if (heuristicID == 2) {
					applyHeuristic2(temporarysolution);//WALKSAT modified with no random walk
				} else if (heuristicID == 3) {
					applyHeuristic3(temporarysolution);//flip random variable from a broken clause
				} else if (heuristicID == 4) {
					applyHeuristic4(temporarysolution);//flip a completely random variable
				} else if (heuristicID == 5) {
					applyHeuristic5(temporarysolution);//novelty
				} else if (heuristicID == 6) {
					applyHeuristic6(temporarysolution);//ruin and recreate - reinitialise a proportion of the variables
				} else if (heuristicID == 7) {
					applyHeuristic7(temporarysolution);//LS - iterate, flip random variable from a broken clause
				} else if (heuristicID == 8) {
					applyHeuristic8(temporarysolution);//LS - iterate, flip a completely random variable
				} else {
					System.err.println("Heuristic " + heuristicID + "does not exist");
					System.exit(0);
				}
				heuristicCallRecord[heuristicID]++;
				heuristicCallTimeRecord[heuristicID] += (int)(System.currentTimeMillis() - startTime);
			}
		}//end checking if its a crossover heuristic

		double newobjectiveFunctionValue = evaluateObjectiveFunction(temporarysolution);

		if (newobjectiveFunctionValue < bestEverObjectiveFunction) {
			bestEverObjectiveFunction = newobjectiveFunctionValue;
			bestEverSolution = deepCopyTheSolution(temporarysolution);
		}//end if the new solution is the best found so far

		//copy the solution to the destination index
		if (solutionMemory[solutionDestinationIndex] == null) {// if the destination index does not contain an initialised solution then initialise it
			solutionMemory[solutionDestinationIndex] = new Solution();
		}
		solutionMemory[solutionDestinationIndex] = deepCopyTheSolution(temporarysolution);
		solutionMemory[solutionDestinationIndex].incrementAge();

		return newobjectiveFunctionValue;
	}//end method applyheuristic

	public double applyHeuristic(int heuristicID, int solutionSourceIndex1, int solutionSourceIndex2, int solutionDestinationIndex) {
		long startTime = System.currentTimeMillis();
		Solution temporarysolution = deepCopyTheSolution(solutionMemory[solutionSourceIndex1]);
		Solution temporarysolution2 = deepCopyTheSolution(solutionMemory[solutionSourceIndex2]);
		//check if its a crossover heuristic
		boolean isCrossover = false;
		int[] crossovers = getHeuristicsOfType(HeuristicType.CROSSOVER);
		if (!(crossovers == null)) {
			for (int x = 0; x < crossovers.length; x++) {
				if (crossovers[x] == heuristicID) {
					isCrossover = true;
					break;}
			}//end for looping the crossover heuristics
		}//end if
		if (isCrossover) {
			if (heuristicID == 9) {//twopoint
				applyHeuristic9(temporarysolution, temporarysolution2);
			} else if (heuristicID == 10) {//onepoint
				applyHeuristic10(temporarysolution, temporarysolution2);
			} else {
				System.err.println("Heuristic " + heuristicID + " is not a crossover operator");
				System.exit(0);
			}
		} else {
			if (temporarysolution.numberOfBrokenClauses() == 0) {//the solution is perfect
				//nothing happens
			} else {
				if (heuristicID == 0) {
					applyHeuristic0(temporarysolution);//GSAT
				} else if (heuristicID == 1) {
					applyHeuristic1(temporarysolution);//HSAT
				} else if (heuristicID == 2) {
					applyHeuristic2(temporarysolution);//WALKSAT modified with no random walk
				} else if (heuristicID == 3) {
					applyHeuristic3(temporarysolution);//flip random variable from a broken clause
				} else if (heuristicID == 4) {
					applyHeuristic4(temporarysolution);//flip a completely random variable
				} else if (heuristicID == 5) {
					applyHeuristic5(temporarysolution);//novelty
				} else if (heuristicID == 6) {
					applyHeuristic6(temporarysolution);//ruin and recreate - reinitialise a proportion of the variables
				} else if (heuristicID == 7) {
					applyHeuristic7(temporarysolution);//LS - iterate, flip random variable from a broken clause
				} else if (heuristicID == 8) {
					applyHeuristic8(temporarysolution);//LS - iterate, flip a completely random variable
				} else {
					System.err.println("Heuristic " + heuristicID + "does not exist");
					System.exit(0);
				}
			}
		}//end checking if its a crossover heuristic
		heuristicCallRecord[heuristicID]++;
		heuristicCallTimeRecord[heuristicID] += (int)(System.currentTimeMillis() - startTime);

		double newobjectiveFunctionValue = evaluateObjectiveFunction(temporarysolution);

		if (newobjectiveFunctionValue < bestEverObjectiveFunction) {
			bestEverObjectiveFunction = newobjectiveFunctionValue;
			bestEverSolution = deepCopyTheSolution(temporarysolution);
		}//end if the new solution is the best found so far

		//copy the solution to the destination index
		if (solutionMemory[solutionDestinationIndex] == null) {// if the destination index does not contain an initialised solution then initialise it
			solutionMemory[solutionDestinationIndex] = new Solution();
		}
		solutionMemory[solutionDestinationIndex] = deepCopyTheSolution(temporarysolution);
		solutionMemory[solutionDestinationIndex].incrementAge();

		return newobjectiveFunctionValue;
	}//end method applyheuristic

	public String bestSolutionToString() {
		String solutionstring = "";
		solutionstring += "Best solution Found\nObjective function value: " + getBestSolutionValue() + "\n";
		solutionstring += "Variables:";
		for (int y = 0; y < numberOfVariables; y++) {
			if ((y % 5) == 0) {
				solutionstring += "\n";
			}
			solutionstring += bestEverSolution.variables[y].number + ":" + bestEverSolution.variables[y].state + "  \t";
		}
		solutionstring += "\nClauses:";
		for (int x = 0; x < numberOfClauses; x++) {
			if ((x % 3) == 0) {
				solutionstring += "\n";
			}
			solutionstring += clauses[x].clauseToString(bestEverSolution.variables);
		}
		return solutionstring;
	}//end method bestSolutonToString

	private Solution deepCopyTheSolution(Solution solutionToCopy) {
		Solution newsolution = new Solution();
		for (int x = 0; x < solutionToCopy.variables.length; x++) {
			newsolution.variables[x] = solutionToCopy.variables[x].clone();
		}
		return newsolution;
	}

	public void copySolution(int source, int destination) {
		Solution tempvariables = deepCopyTheSolution(solutionMemory[source]);
		solutionMemory[destination] = tempvariables;
	}

	public double getBestSolutionValue() {
		return bestEverObjectiveFunction;
	}

	private double evaluateObjectiveFunction(Solution solution) {
		return solution.numberOfBrokenClauses();
	}

	public double getFunctionValue(int solutionIndex) {
		return evaluateObjectiveFunction(solutionMemory[solutionIndex]);
	}

	public int[] getHeuristicsOfType(HeuristicType hType) {
		switch (hType)
		{
		case LOCAL_SEARCH : return localSearches;
		case MUTATION : return mutations;
		case RUIN_RECREATE : return ruin_recreate;
		case CROSSOVER : return crossovers;
		default: return null;
		}
	}

	public int[] getHeuristicsThatUseDepthOfSearch() {
		return localSearches;
	}

	public int[] getHeuristicsThatUseIntensityOfMutation() {
		int[] newint = new int[mutations.length + ruin_recreate.length];
		int count = 0;
		for (int x = 0; x < mutations.length; x++) {
			newint[count] = mutations[x];count++;
		}
		for (int x = 0; x < ruin_recreate.length; x++) {
			newint[count] = ruin_recreate[x];count++;
		}
		return newint;
	}

	public int getNumberOfInstances() {
		return 10;
	}

	public void initialiseSolution(int index) {
		solutionMemory[index] = new Solution();
		//with random boolean values
		for (int y = 0; y < numberOfVariables; y++) {
			(solutionMemory[index].variables[y]).state = rng.nextBoolean();
		}
		double i = getFunctionValue(index);
		if (i < bestEverObjectiveFunction) {
			bestEverObjectiveFunction = i;
		}
	}//end initialisesolution

	private void readInInstance(BufferedReader buffread) {
		try {
			String readline = "";
			boolean carryon = true;
			while (carryon) {
				readline = buffread.readLine();
				//System.out.println(readline);
				if (readline.startsWith("p")) {
					carryon = false;
				}
			}//end while
			numberOfVariables = Integer.parseInt(readline.split(" ")[2]);
			if (readline.split(" ").length == 5) {
				numberOfClauses = Integer.parseInt(readline.split(" ")[4]);
			} else if (readline.split(" ").length == 4) {
				numberOfClauses = Integer.parseInt(readline.split(" ")[3]);
			} else {
				System.out.println("file format incorrect");
				System.exit(0);
			}

			//System.out.println(numberOfVariables + " " + numberOfClauses);

			clauses = new Clause[numberOfClauses];

			for (int clause = 0; clause < numberOfClauses; clause++) {
				//System.out.print("clause " + clause + " ") ;
				readline = buffread.readLine();
				readline = readline.trim();
				String[] variables = readline.split(" ");
				Clause C = new Clause(variables.length - 1, clause);
				for (int v = 0; v < variables.length -1; v++) {
					//System.out.println("variable " + v);
					C.addVariable(Integer.parseInt(variables[v]));
				}//end looping the variables on the line
				clauses[clause] = C;
			}//end looping the clauses
		} catch (IOException b) {
			System.err.println(b.getMessage());
			System.exit(0);
		}
	}

	private void loadInstance(String filename) {
		BufferedReader buffread;
		try {
			FileReader read = new FileReader(filename);
			buffread = new BufferedReader(read);
			readInInstance(buffread);
		} catch (FileNotFoundException a) {
			try {
				InputStream fis = this.getClass().getClassLoader().getResourceAsStream(filename); 
				buffread = new BufferedReader(new InputStreamReader(fis));
				readInInstance(buffread);
			} catch(NullPointerException n) {
				System.err.println("cannot find file " + filename);
				System.exit(-1);
			}
		}//end catch
	}

	public void loadInstance(int instanceID) {
		solutionMemory = new Solution[defaultmemorysize];//set solution memory size
		String folder = "invalid instance selected: " + instanceID;
		if (instanceID < 1) {
			folder = "sat07/crafted/Difficult/contest-02-03-04/contest02-Mat26.sat05-457.reshuffled-07.txt";
		} else if (instanceID < 2) {
			folder = "sat07/crafted/Hard/contest03/looksrandom/hidden-k3-s0-r5-n700-01-S2069048075.sat05-488.reshuffled-07.txt";
		} else if (instanceID < 3) {
			folder = "sat07/crafted/Hard/contest03/looksrandom/hidden-k3-s0-r5-n700-02-S350203913.sat05-486.reshuffled-07.txt";
		} else if (instanceID < 4) {
			folder = "sat09/crafted/parity-games/instance_n3_i3_pp.txt";
		} else if (instanceID < 5) {
			folder = "sat09/crafted/parity-games/instance_n3_i3_pp_ci_ce.txt";
		} else if (instanceID < 6) {
			folder = "sat09/crafted/parity-games/instance_n3_i4_pp_ci_ce.txt";
		} else if (instanceID < 7) {
			folder = "ms_random/highgirth/3SAT/HG-3SAT-V250-C1000-1.txt";
		} else if (instanceID < 8) {
			folder = "ms_random/highgirth/3SAT/HG-3SAT-V250-C1000-2.txt";
		} else if (instanceID < 9) {
			folder = "ms_random/highgirth/3SAT/HG-3SAT-V300-C1200-2.txt";
		} else if (instanceID <10) {
			folder = "ms_crafted/MAXCUT/SPINGLASS/t7pm3-9999.spn.txt";
		} else {
			System.err.println("instance does not exist " + instanceID);
			System.exit(-1);
		}
		//System.out.println("Loading Instance " + "instances/sat/" + folder + "/uf" + folder + "-0" + instance + ".txt");
		loadInstance("data/sat/" + folder);
		//printclauses(0);
		//System.out.println(solutionToString(0));
	}//end loadinstance method

	public void setMemorySize(int size) {
		Solution[] newSolutionMemory = new Solution[size];
		if (solutionMemory != null) {
			for (int x = 0; x < solutionMemory.length; x++) {//copy each solution into the new memory
				if (x < size) {//checks that we do not try to go beyond the length of the new solution memory
					newSolutionMemory[x] = solutionMemory[x];
				}//end if
			}//end looping the current solutionmemory
		}
		solutionMemory = newSolutionMemory;
	}//end setmemorysize

	private String solutionToString(Solution s) {
		String solutionstring = "";
		for (int y = 0; y < numberOfVariables; y++) {
			solutionstring += s.variables[y].number + ":" + s.variables[y].state + " ";
		}
		return solutionstring;
	}

	public String solutionToString(int solutionIndex) {
		return solutionToString(solutionMemory[solutionIndex]);
	}//end method solutiontostring

//	private void printclauses(int solutionIndex) {
//		String solutionstring = "";
//		for (int x = 0; x < numberOfClauses; x++) {
//			Clause c = clauses[x];
//			System.out.println(x + " " + c.numberOfVariables());
//			solutionstring += "(";
//			for (int y = 0; y < c.numberOfVariables(); y++) {
//				int v = c.getVariableNumber(y);
//				solutionstring += v + ":";
//				solutionstring += Boolean.toString(c.getVariableSign(y));
//				solutionstring += " ";
//			}
//			solutionstring += ")\n";
//		}
//		//System.out.println(solutionstring);
//	}//end printclauses

	public String toString() {
		return "SAT";
	}

	public boolean compareSolutions(int solutionIndex1, int solutionIndex2) {
		Solution s1 = solutionMemory[solutionIndex1];
		Solution s2 = solutionMemory[solutionIndex2];
		for (int i = 0; i < numberOfVariables; i++) {
			if (s1.variables[i].state != s2.variables[i].state) {
				return false;
			}
		}
		return true;
	}

	class Variable {
		private boolean state;
		private int number;
		private int age;
		public Variable(int n) {
			number = n;
			state = false;
			age = 0;
		}
		public void permanentflip() {
			if (state) {
				state = false;
			} else {
				state = true;
			}
			this.age = 0;
		}
		public void testflip() {
			if (state) {
				state = false;
			} else {
				state = true;
			}
		}
		public void incrementAge() {
			this.age++;
		}
		public Variable clone() {
			Variable v = new Variable(this.number);
			v.state = this.state;
			v.age = this.age;
			return v;
		}
	}//end class variable

	class Clause {
		private int number;
		private int[] variablenumbers;
		private boolean[] variablesigns;
		private int clausefill;
		public Clause(int numberofvariables, int num) {
			if (numberofvariables == 0) {
				System.out.println("zero variables in this clause");
				System.exit(0);
			}
			number = num;
			variablenumbers = new int[numberofvariables];
			variablesigns = new boolean[numberofvariables];
			clausefill = 0;
		}
		public void addVariable(int n) {//when we add a variable, we must subtract 1 from its number because it relates to a position in an array in the solution object
			if (n > 0) {//a non negated variable
				variablenumbers[clausefill] = n - 1;
				variablesigns[clausefill] = true;
			} else {//a negated variable
				variablenumbers[clausefill] = (n * -1) - 1;
				variablesigns[clausefill] = false;
			}
			clausefill++;
		}
		public int numberOfVariables() {
			return variablenumbers.length; 
		}
		public boolean getVariableSign(int index) {//if the variable is normal, or the negation of it
			return variablesigns[index];
		}
		public int getVariableNumber(int index) {
			return variablenumbers[index];
		}
		public String clauseToString(Variable[] variables) {
			String s = "";
			s += "( ";
			for (int x = 0; x < variablenumbers.length; x++) {
				if (!variablesigns[x]) {
					s += "-";
				}
				s += variablenumbers[x] + ":" + (variablesigns[x] == variables[variablenumbers[x]].state) + " ";
			}s += ")";
			return s;
		}
		public void printclause(Variable[] variables) {
			System.out.print("( ");
			for (int x = 0; x < variablenumbers.length; x++) {
				System.out.print((variablesigns[x] == variables[variablenumbers[x]].state) + " ");
			}System.out.println(")");
		}
		public boolean evaluate(Variable[] variables) {//determine if the clause is true or not by the given solution
			for (int x = 0; x < variablenumbers.length; x++) {
				if (variablesigns[x] == variables[variablenumbers[x]].state) {//if the variable evaluates to true
					return true; //because one variable evaluates to true, and they are all linked with an OR in the clause
				}
			}//end looking along the clause variables for one that is true
			return false;//because no true variable has been found in the clause
		}//end method evaluate
		public boolean equals(Clause c) {
			if (c.number == this.number) {
				return true;
			} else {
				return false;
			}
		}
	}//end class clause

	class Solution {
		private Variable[] variables;
		public Solution() {
			variables = new Variable[numberOfVariables];
			for (int x = 0; x < numberOfVariables; x++) {
				variables[x] = new Variable(x);
			}
		}
		public int numberOfBrokenClauses() {
			int numberbroken = 0;
			for (int x = 0; x < numberOfClauses; x++) {
				if (!(clauses[x].evaluate(this.variables))) {//evaluates the clause with this set of variables
					numberbroken++;//if it evaluates to false then increment the count
				}
			}
			return numberbroken;
		}
		public void incrementAge() {
			for (int x = 0; x < numberOfVariables; x++) {
				variables[x].incrementAge();
			}
		}
		public int testFlipForBrokenClauses(int variableToFlip) {
			variables[variableToFlip].testflip();
			int numberbroken = 0;
			for (int x = 0; x < numberOfClauses; x++) {
				if (!(clauses[x].evaluate(this.variables))) {//evaluates the clause with this set of variables
					numberbroken++;//if it evaluates to false then increment the count
				}
			}
			variables[variableToFlip].testflip();
			return numberbroken;
		}
		public int testFlipForNegGain(int variableToFlip) {
			ArrayList<Clause> satisfiedClauses = new ArrayList<Clause>(numberOfClauses);
			for (int x = 0; x < numberOfClauses; x++) {
				if (clauses[x].evaluate(this.variables)) {//evaluates the clause with the original set of variables
					satisfiedClauses.add(clauses[x]);//add it to the list of those satisfied
				}
			}
			variables[variableToFlip].testflip();
			ArrayList<Clause> newbrokenClauses = new ArrayList<Clause>(50);
			for (int x = 0; x < numberOfClauses; x++) {
				if (!(clauses[x].evaluate(this.variables))) {//evaluates the clause with the variable flipped
					newbrokenClauses.add(clauses[x]);//add it to the list of those broken
				}
			}
			//check to find the number of clauses in satisfiedClauses AND in newbrokenClauses
			int numberNotNowSatisfied = 0;
			for (int x = 0; x < satisfiedClauses.size(); x++) {
				if (newbrokenClauses.contains(satisfiedClauses.get(x))) {//if it's in newbrokenclauses
					numberNotNowSatisfied++;
				}
			}
			variables[variableToFlip].testflip();
			return numberNotNowSatisfied;
		}

	}//end class solution

}//end class
