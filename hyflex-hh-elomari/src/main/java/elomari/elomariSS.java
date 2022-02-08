package Examples;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

import java.util.Arrays;
import java.util.Comparator;

public class elomariSS extends HyperHeuristic
{
	public elomariSS(long seed)
	{ super(seed); }

	//declaration and initialization
	
	////algorithm parameters////
	double alpha = 0.5; //adaptation rate 
	double minSelectionProbability = 0; // 1/2k
	double minOperatorApplication = 300; 
	double lowIntsOfMutation = 0.2;
	double highIntsOfMutation = 0.8;
	double lowDepthOfSearch = 0.2;
	double highDepthOfSearch = 0.8;
	int populationSize = 5;
	
	////constants////
	int memorySize = 3*populationSize;
	int rep = 0; //replication counter
	int inst = 0; //instance counter
	int operatorAtt = 9;
	int decimalPlaces = 6;
	int noImprovementCounter = 0;
	int restart = 0;
	
	////counters////
	int c, i, j, k = 0;
	double generation = 0;
	
	////variables////
	int heuristic_to_apply, heuristic_to_apply_index, minSameSolution_index, minNoImprovement_index, minImpRate_index = 0;
	double noImprovementRunLength = 0;
	double rndSum, rndNumber = 0; //used for probabilistic selection from calculationsData
	double startTime, totalTime, generationStartTime, avNoOfGenerations, avTimePerGenSum = 0;
	double minSameSolution, minNoImprovement, minImpRate = Double.POSITIVE_INFINITY;
	double noImprovementRunLengthFactor = 0.05;

	////arrays////
	double[] obj_function_values_current = new double[populationSize];
	double[] obj_function_values_new = new double[populationSize];
	double[][] obj_function_values_temp = new double[2*populationSize][2];
	
	//indices
	int operatorID_index = 0;
	int operatorType_index = 1;
	int time_index = 2;
	int quality_index = 3;
	int noOfCalls_index = 4;
	int lastApplied_index = 5;
	int sameSolution_index = 6;
	int noImprovement_index = 7;
	int selectionProbAP_index = 8;
	
	public void solve(ProblemDomain problem) 
	{	
		////algorithm parameter////
		minSelectionProbability = (double)1/(2*problem.getNumberOfHeuristics());
	
		////available low level heuristics////
		int[] local_search_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
		int[] mutation_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
		int[] crossover_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
		int[] ruin_recreate_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
		
		////storage arrays//// the last four hold operator indicies, not operator IDs
		double[][] operatorData = new double [problem.getNumberOfHeuristics()][operatorAtt];
		int[] minSameSolutionsArray = new int [problem.getNumberOfHeuristics()+1];//the added one is just to avoid a bug
		int[] minImpRateArray = new int [problem.getNumberOfHeuristics()+1];//the added one is just to avoid a bug
		int[] candidatesArray = new int [problem.getNumberOfHeuristics()+1];//the added one is just to avoid a bug
			
		//reset variables, counters, and arrays
		c = 0;
		i = 0;
		j = 0;
		k = 0; 
		generation = 0;
		restart = 0;
		noImprovementCounter = 0;
		
		heuristic_to_apply = 0;
		heuristic_to_apply_index = 0;
		
		rndSum = 0; 
		rndNumber = 0;
		minNoImprovement = Double.POSITIVE_INFINITY;
		minSameSolution = Double.POSITIVE_INFINITY;
		minImpRate = Double.POSITIVE_INFINITY;
		
		startTime = 0;
		totalTime = 0;
		generationStartTime = 0;

		avTimePerGenSum = 0;
		avNoOfGenerations = 0;

		for(c = 0; c < 2*populationSize; c++)
			obj_function_values_temp[c][0] = 0;
		for(c = 0; c < populationSize; c++)
			obj_function_values_current[c] = 0;
		for(c = 0; c < populationSize; c++)
			obj_function_values_new[c] = 0;
		for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
			minSameSolutionsArray[c] = 1000;
		for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
			minImpRateArray[c] = 1000;
		for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
			candidatesArray[c] = 1000;
		for(i = 0; i < problem.getNumberOfHeuristics(); i++)
			for(j = 0; j < operatorAtt; j++)
				operatorData[i][j] = 0;
		
		//populate operatorData with operator IDs and types
		for(i = 0; i < local_search_heuristics.length; i++)
		{
			operatorData[i][0] = local_search_heuristics[i];
			operatorData[i][1] = 100;
		}
		for(i = 0; i < mutation_heuristics.length; i++)
		{
			operatorData[i + local_search_heuristics.length][0] = mutation_heuristics[i];
			operatorData[i + local_search_heuristics.length][1] = 200;
		}
		for(i = 0; i < crossover_heuristics.length; i++)
		{
			operatorData[i + local_search_heuristics.length + mutation_heuristics.length][0] = crossover_heuristics[i];
			operatorData[i + local_search_heuristics.length + mutation_heuristics.length][1] = 300;
		}
		for(i = 0; i < ruin_recreate_heuristics.length; i++)
		{
			operatorData[i + local_search_heuristics.length + mutation_heuristics.length + crossover_heuristics.length][0] = ruin_recreate_heuristics[i];
			operatorData[i + local_search_heuristics.length + mutation_heuristics.length + crossover_heuristics.length][1] = 400;
		}
	
		//initialize solutions
		problem.setMemorySize(memorySize);
		for(c = 0; c < populationSize; c++)
		{
			problem.initialiseSolution(c);
			obj_function_values_current[c] = problem.getFunctionValue(c);
		}
		
		//apply all operators in sequence and collect data for each... no need to apply an operator to the entire population
		for(i = 0; i < problem.getNumberOfHeuristics(); i++)
		{
			//resetting...
			totalTime = 0;
			
			//start timing
			generationStartTime = System.currentTimeMillis();
		
			//for crossover
			if(operatorData[i][operatorType_index] == 300) 
			{		
				//applying to one solution
				for(j = 0; j < 1; j++)
				{
					startTime = System.currentTimeMillis();
					
					obj_function_values_new[j] = problem.applyHeuristic((int) operatorData[i][operatorID_index], j, j + 1, j + populationSize);
					
					totalTime = totalTime + (System.currentTimeMillis() - startTime);
					
					//collect data about this application
					////Step2: quality variation per application
					operatorData[i][quality_index] = operatorData[i][quality_index] + (problem.getFunctionValue(j + populationSize) - Math.min(problem.getFunctionValue(j), problem.getFunctionValue(j+1)));
					
					////Step3: same solutions and no improvements
					if(problem.compareSolutions(j, j + populationSize) || problem.compareSolutions(j+1, j + populationSize))
					{
						operatorData[i][sameSolution_index] = operatorData[i][sameSolution_index] + 1;
						operatorData[i][noImprovement_index] = operatorData[i][noImprovement_index] + 1;	
					}
					else
					{
						if(problem.getFunctionValue(j + populationSize) >= problem.getFunctionValue(j) || problem.getFunctionValue(j + populationSize) >= problem.getFunctionValue(j+1))
							operatorData[i][noImprovement_index] = operatorData[i][noImprovement_index] + 1;
					}						
				}			
				
				////Step1: total time allocated to this operator
				operatorData[i][time_index] = operatorData[i][time_index] + totalTime;	
			}
			else //not 300
			{
				for(j = 0; j < 1; j++)
				{
					startTime = System.currentTimeMillis();
					
					obj_function_values_new[j] = problem.applyHeuristic((int) operatorData[i][operatorID_index], j, j + populationSize);
					
					totalTime = totalTime + (System.currentTimeMillis() - startTime);
					
					//collect data about this application
					////Step2: quality variation per application
					operatorData[i][quality_index] = operatorData[i][quality_index] + (problem.getFunctionValue(j + populationSize) - problem.getFunctionValue(j));
					
					////Step3: same solutions and no improvements
					if(problem.compareSolutions(j, j + populationSize))
					{
						operatorData[i][sameSolution_index] = operatorData[i][sameSolution_index] + 1;
						operatorData[i][noImprovement_index] = operatorData[i][noImprovement_index] + 1;	
					}
					else
					{
						if(problem.getFunctionValue(j + populationSize) >= problem.getFunctionValue(j))
							operatorData[i][noImprovement_index] = operatorData[i][noImprovement_index] + 1;
					}						
				}
				
				////Step1: total time allocated to this operator
				operatorData[i][time_index] = operatorData[i][time_index] + totalTime;	
			}
			
			//update number of calls, selection prob, and no of generations sum
			operatorData[i][noOfCalls_index] = operatorData[i][noOfCalls_index] + 1;
			operatorData[i][selectionProbAP_index] = (double)1/(problem.getNumberOfHeuristics());
			avTimePerGenSum = avTimePerGenSum + (System.currentTimeMillis() - generationStartTime);
		}
		
		//calculate the expected number of generations
		avNoOfGenerations = (elomariSS.this.getTimeLimit())/(avTimePerGenSum / problem.getNumberOfHeuristics())/((double)populationSize);
		
		if(avNoOfGenerations > minOperatorApplication)
		{ 
			////////^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^/////////////
			////////^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^EXPLORE^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^/////////////
			////////^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^/////////////
		
			avTimePerGenSum = 0;
			problem.setDepthOfSearch(lowDepthOfSearch);
			problem.setIntensityOfMutation(highIntsOfMutation);
			
			//find operators that do not produce same solutions
			for(k = 0; k < problem.getNumberOfHeuristics(); k++)
			{
				if(operatorData[k][sameSolution_index]/(operatorData[k][noOfCalls_index]*populationSize) < minSameSolution)
				{
					minSameSolution = operatorData[k][sameSolution_index]/(operatorData[k][noOfCalls_index]*populationSize);
					minSameSolution_index = k; 							
				}
			}
			
			////place the minSameSolution operator in minSameSolutionsArray
			c = 0;
			minSameSolutionsArray[c] = minSameSolution_index;
			
			////check if there are other operators with equal sameSolution%
			for(k = 0; k < problem.getNumberOfHeuristics(); k++)
			{
				if(operatorData[k][sameSolution_index]/(operatorData[k][noOfCalls_index]*populationSize) == minSameSolution && k != minSameSolution_index)
				{	
					minSameSolutionsArray[c+1] = k;
					c++;
				}
			}
			
			////check which of the minSameSoutionsArray has the minNoImprovement%
			for(k = 0; k < problem.getNumberOfHeuristics(); k++)
			{
				if(minSameSolutionsArray[k] == 1000)
					break;
				else
				{
					if(operatorData[minSameSolutionsArray[k]][noImprovement_index]/(operatorData[minSameSolutionsArray[k]][noOfCalls_index]*populationSize) < minNoImprovement)
					{
						minNoImprovement = operatorData[minSameSolutionsArray[k]][noImprovement_index]/(operatorData[minSameSolutionsArray[k]][noOfCalls_index]*populationSize);
						minNoImprovement_index = minSameSolutionsArray[k];
						heuristic_to_apply_index = minNoImprovement_index;
					} 								
				}
			}
			
			//update selection probabilities... AP
			for(k = 0; k < problem.getNumberOfHeuristics(); k++)
			{
				if(k != heuristic_to_apply_index)
					operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*(minSelectionProbability - operatorData[k][selectionProbAP_index]);
				else
					operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*((1-(problem.getNumberOfHeuristics() - 1)*minSelectionProbability) - (operatorData[k][selectionProbAP_index]));
			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////********************************************EXPLORE MAIN**********************************************************///////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			while(!hasTimeExpired())
			{
				generationStartTime = System.currentTimeMillis();
			
				//resetting... 
				totalTime = 0;
				minNoImprovement = Double.POSITIVE_INFINITY;
				minSameSolution = Double.POSITIVE_INFINITY;
				minImpRate = Double.POSITIVE_INFINITY;
				noImprovementRunLength = noImprovementRunLengthFactor*avNoOfGenerations;
				for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
					minSameSolutionsArray[c] = 1000;
				for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
					minImpRateArray[c] = 1000;
				for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
					candidatesArray[c] = 1000;

				//for crossover
				if(operatorData[heuristic_to_apply_index][operatorType_index] == 300) 
				{		
					for(j = 0; j < populationSize; j++)
					{
						startTime = System.currentTimeMillis();
						
						obj_function_values_new[j] = problem.applyHeuristic((int) operatorData[heuristic_to_apply_index][operatorID_index], j, j + 1, j + populationSize);
						
						totalTime = totalTime + (System.currentTimeMillis() - startTime);
						
						//collect data about this application
						////Step2: quality variation per application
						operatorData[heuristic_to_apply_index][quality_index] = operatorData[heuristic_to_apply_index][quality_index] + (problem.getFunctionValue(j + populationSize) - Math.min(problem.getFunctionValue(j), problem.getFunctionValue(j+1)));
						
						////Step3: same solutions and no improvements
						if(problem.compareSolutions(j, j + populationSize) || problem.compareSolutions(j+1, j + populationSize))
						{
							operatorData[heuristic_to_apply_index][sameSolution_index] = operatorData[heuristic_to_apply_index][sameSolution_index] + 1;
							operatorData[heuristic_to_apply_index][noImprovement_index] = operatorData[heuristic_to_apply_index][noImprovement_index] + 1;	
						}
						else
						{
							if(problem.getFunctionValue(j + populationSize) > problem.getFunctionValue(j))
								operatorData[heuristic_to_apply_index][noImprovement_index] = operatorData[heuristic_to_apply_index][noImprovement_index] + 1;
						}						
					}
					
					////Step1: total time allocated to this operator
					operatorData[heuristic_to_apply_index][time_index] = operatorData[heuristic_to_apply_index][time_index] + totalTime;	
				}
				else //not 300
				{
					for(j = 0; j < populationSize; j++)
					{
						startTime = System.currentTimeMillis();
						
						obj_function_values_new[j] = problem.applyHeuristic((int) operatorData[heuristic_to_apply_index][operatorID_index], j, j + populationSize);
											
						totalTime = totalTime + (System.currentTimeMillis() - startTime);
						
						//collect data about this application
						////Step2: quality variation per application
						operatorData[heuristic_to_apply_index][quality_index] = operatorData[heuristic_to_apply_index][quality_index] + (problem.getFunctionValue(j + populationSize) - problem.getFunctionValue(j));
						
						////Step3: same solutions and no improvements
						if(problem.compareSolutions(j, j + populationSize))
						{
							operatorData[heuristic_to_apply_index][sameSolution_index] = operatorData[heuristic_to_apply_index][sameSolution_index] + 1;
							operatorData[heuristic_to_apply_index][noImprovement_index] = operatorData[heuristic_to_apply_index][noImprovement_index] + 1;	
						}
						else
						{
							if(problem.getFunctionValue(j + populationSize) > problem.getFunctionValue(j))
								operatorData[heuristic_to_apply_index][noImprovement_index] = operatorData[heuristic_to_apply_index][noImprovement_index] + 1;
						}							
					}
					
					////Step1: total time allocated to this operator
					operatorData[heuristic_to_apply_index][time_index] = operatorData[heuristic_to_apply_index][time_index] + totalTime;	
				}

				//pool current and new in temp and their indices
				for(k = 0; k < 2*populationSize; k++)
				{
					obj_function_values_temp[k][0] = problem.getFunctionValue(k);
					obj_function_values_temp[k][1] = k;
				}
				
				//sort temp after the first two restarts
				if(restart >= 2)
				{
					Arrays.sort(obj_function_values_temp, new Comparator<double[]>() 
					{
			            @Override
			            public int compare(double[] a, double[] b) 
			            {
			            	return (int) (Math.pow(10, 6)*b[0] - Math.pow(10, 6)*a[0]);
			            }
					});	
				}
				
				//copy the best half into obj_function_values_new... with no duplicates
				problem.copySolution((int) obj_function_values_temp[2*populationSize - 1][1], 2*populationSize);
				obj_function_values_new[0] = obj_function_values_temp[2*populationSize - 1][0];
				
				for (i = 0; i < populationSize - 1; i++)
				{
					j = i;
					try
					{
						while(problem.compareSolutions((int)obj_function_values_temp[2*populationSize - 1 - j - 1][1], (int)obj_function_values_temp[2*populationSize - 1 - i][1]))
							j++;
					}
					//used when all solutions in obj_function_values_temp are the same. Just copy one of them.
					catch(Exception e)
					{j = j - 1;}
					
					problem.copySolution((int) obj_function_values_temp[2*populationSize - 1 - j - 1][1], 2*populationSize + i + 1);					
				}
				
				for(k = 0; k < populationSize; k++)
				{
					problem.copySolution(2*populationSize + k, k );
					obj_function_values_new[k] = problem.getFunctionValue(2*populationSize + k);
				}	
				
				//update the operator to pursue... 
				////find an operator with low sameSolution% and high improvement rate
				for(k = 0; k < problem.getNumberOfHeuristics(); k++)
				{
					if(operatorData[k][sameSolution_index]/(operatorData[k][noOfCalls_index]*populationSize) < minSameSolution)
					{
						minSameSolution = operatorData[k][sameSolution_index]/(operatorData[k][noOfCalls_index]*populationSize);
						minSameSolution_index = k; 							
					}
				}
				
				////place the minSameSolution operator in minSameSolutionsArray
				c = 0;
				minSameSolutionsArray[c] = minSameSolution_index;
				
				////check if there are other operators with equal sameSolution%
				for(k = 0; k < problem.getNumberOfHeuristics(); k++)
				{
					if(operatorData[k][sameSolution_index]/(operatorData[k][noOfCalls_index]*populationSize) == minSameSolution && k != minSameSolution_index)
					{	
						minSameSolutionsArray[c+1] = k;
						c++;
					}
				}
				
				//check which of the minSameSoutionsArray has the highest improvement rate
				for(k = 0; k < problem.getNumberOfHeuristics(); k++)
				{
					if(minSameSolutionsArray[k] == 1000)
						break;
					
					if((operatorData[minSameSolutionsArray[k]][quality_index]/(operatorData[minSameSolutionsArray[k]][noOfCalls_index]*populationSize))/ (operatorData[minSameSolutionsArray[k]][time_index]/(operatorData[minSameSolutionsArray[k]][noOfCalls_index]*populationSize)) < minImpRate)
					{
						minImpRate = (operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize));
						minImpRate_index = k;
						heuristic_to_apply_index = k;
					}
				}
			
				////place the highest improvement rate in candidatesArray
				i = 0;
				candidatesArray[i] = minImpRate_index;
				heuristic_to_apply_index = candidatesArray[0];
			
				//check if there are other operators with minImpRate <= 0
				for(k = 0; k < problem.getNumberOfHeuristics(); k++)
				{
					Arrays.sort(candidatesArray);
					if((operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize)) <= 0)
						if(Arrays.binarySearch(candidatesArray, k) < 0)
						{	
							candidatesArray[i+1] = k;
							i++;
						}
				}
			
				//update selection probs. AP
				for(k = 0; k < problem.getNumberOfHeuristics(); k++)
				{
					if(k != heuristic_to_apply_index)
						operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*(minSelectionProbability - operatorData[k][selectionProbAP_index]);
					else
						operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*((1-(problem.getNumberOfHeuristics() - 1)*minSelectionProbability) - (operatorData[k][selectionProbAP_index]));
				}
				
				////select an operator
				rndSum = 0;
				rndNumber = rng.nextDouble();
				for(k = 0; k < problem.getNumberOfHeuristics(); k++)
				{
					if(rndNumber <= operatorData[k][selectionProbAP_index] + rndSum)
					{
						heuristic_to_apply = (int) operatorData[k][operatorID_index];
						
						//update selection counter and lastApplied
						for(i = 0; i < problem.getNumberOfHeuristics(); i++)
							if(operatorData[i][operatorID_index] == heuristic_to_apply)
							{
								operatorData[i][noOfCalls_index] = operatorData[i][noOfCalls_index] + 1;
								operatorData[i][lastApplied_index] = generation;
								heuristic_to_apply_index = i;
								break;
							}
						break;
					}
					else
						rndSum = rndSum + operatorData[k][selectionProbAP_index];
				}
				
				/////////////////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////***************SELF SEARCH EXPLORE START***************////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				//check if the BSF hasn't changed 
				if(restart <= 2)
					if(obj_function_values_new[0] >= problem.getBestSolutionValue())
						noImprovementCounter++;
					else
						noImprovementCounter = 0;
				else
					if(obj_function_values_new[0] >= obj_function_values_current[0])
						noImprovementCounter++;
					else
						noImprovementCounter = 0;
				
				//copy new to current and calculate population average
				for(k = 0; k < populationSize; k++)
					obj_function_values_current[k] = obj_function_values_new[k];
				
				//if no improvement is seen over the past k generations, restart the search
				if(noImprovementCounter >= noImprovementRunLength)
				{
					noImprovementCounter = 0;
					restart++;
		
					//check how much time is left
	 				if((avNoOfGenerations - generation) > 0.5*avNoOfGenerations)
	 				{
		  				//focus on exploration operators
		 				problem.setIntensityOfMutation(highIntsOfMutation + (double)restart/10);
		 				problem.setDepthOfSearch(lowDepthOfSearch);
		 				
		 				//check if the intensity of mutation reached its limit
		 				if(problem.getIntensityOfMutation() == 1)
		 				{
		 					//apply ruin and recreate operators to the current solutions
		 					for(c = 0; c < populationSize; c++)
		 						obj_function_values_new[c] = problem.applyHeuristic(ruin_recreate_heuristics[rng.nextInt(ruin_recreate_heuristics.length)], c, c);		
						
		 					//reset operatorData counters
		 					for(j = 0; j < problem.getNumberOfHeuristics(); j++)
		 						for(k = time_index; k < selectionProbAP_index; k++)
		 							operatorData[j][k] = 0;	
		 				}
	 
	 					//apply the current operator to the current population except the best solution
	 					if(operatorData[heuristic_to_apply_index][operatorType_index] == 300)
	 					{	
	 						i = 0;
	 						
	 						for(j = 0; j < populationSize-1; j++)//since I'm not replacing the first element
		 					{
	 							//fail safe in case candidatesArray runs out of options
	 							if(heuristic_to_apply_index == 1000)
	 							{
	 								heuristic_to_apply_index = candidatesArray[0];
	 								break;
	 							}
	 							
		 						problem.applyHeuristic((int) operatorData[heuristic_to_apply_index][operatorID_index], j+1, j+2, j+1 + populationSize);
		 						if(problem.compareSolutions(j+1, j+1 + populationSize) || problem.compareSolutions(j+2, j+1 + populationSize))
		 						{		 							
		 							heuristic_to_apply_index = candidatesArray[i+1];
		 							i++;
		 							j--;
		 						}
		 						else
		 						{
		 							obj_function_values_new[j+1] = problem.getFunctionValue(j+1 + populationSize);
		 							problem.copySolution(j+1 + populationSize, j+1);
		 							i = 0;
		 						}
		 					}
	 					}
	 					else //operator not 300
	 					{
	 						i = 0;
	 						
	 						for(j = 0; j < populationSize-1; j++)//since I'm not replacing the first element
		 					{
	 							//fail safe in case candidatesArray runs out of options
	 							if(heuristic_to_apply_index == 1000)
	 							{
	 								heuristic_to_apply_index = candidatesArray[0];
	 								break;
	 							}
	 							
		 						problem.applyHeuristic((int) operatorData[heuristic_to_apply_index][operatorID_index], j+1, j+1 + populationSize);
		 						if(problem.compareSolutions(j+1, j+1 + populationSize))
		 						{		 							
		 							heuristic_to_apply_index = candidatesArray[i+1];
		 							j--;
		 							i++;
		 						}
		 						else
		 						{
		 							obj_function_values_new[j+1] = problem.getFunctionValue(j+1 + populationSize);
		 							problem.copySolution(j+1 + populationSize, j+1);
		 							i = 0;
		 						}
		 					}
	 					}
	 					
	 					//update selection probabilities... AP
	 					for(k = 0; k < problem.getNumberOfHeuristics(); k++)
	 					{
	 						if(k != heuristic_to_apply_index)
	 							operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*(minSelectionProbability - operatorData[k][selectionProbAP_index]);
	 						else
	 							operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*((1-(problem.getNumberOfHeuristics() - 1)*minSelectionProbability) - (operatorData[k][selectionProbAP_index]));
	 					}
	 				}
					else //exploit the current best operator(s)
					{
						alpha = 0.8;
	 					//resetting... 
	 					for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
	 						minSameSolutionsArray[c] = 1000;
	 					for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
	 						minImpRateArray[c] = 1000;
	 					for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
	 						candidatesArray[c] = 1000;
	 					minNoImprovement = Double.POSITIVE_INFINITY;
	 					minSameSolution = Double.POSITIVE_INFINITY;
	 					minImpRate = Double.POSITIVE_INFINITY;
	 					
						//find an operator with the highest improvement rate
	 					for(k = 0; k < problem.getNumberOfHeuristics(); k++)
	 					{
	 						if((operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize)) < minImpRate)
	 						{
	 							minImpRate = (operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize));
	 							minImpRate_index = k;
	 							heuristic_to_apply_index = k;
	 						}
	 					}
	 					
	 					//place the minImpRate operator in minImpRateArray
	 					i = 0;
	 					minImpRateArray[i] = minImpRate_index;
	 					
	 					//check if there are other operators with minImpRate <= 0
	 					for(k = 0; k < problem.getNumberOfHeuristics(); k++)
	 					{
	 						Arrays.sort(minImpRateArray);
	 						if((operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize)) <= 0)
	 							if(Arrays.binarySearch(minImpRateArray, k) < 0)
		 						{	
		 							minImpRateArray[i+1] = k;
		 							i++;
		 						}
	 					}
	 					
	 					//focus on exploitation operators
	 					problem.setIntensityOfMutation(lowIntsOfMutation);
	 					problem.setDepthOfSearch(highDepthOfSearch + ((double)restart/10));
	 					
		 				//check if the depth of search reached its limit
		 				if(problem.getDepthOfSearch() == 1)
		 				{
		 					//apply ruin and recreate operators to the current solutions
		 					for(c = 0; c < populationSize; c++)
		 						obj_function_values_new[c] = problem.applyHeuristic(ruin_recreate_heuristics[rng.nextInt(ruin_recreate_heuristics.length)], c, c);	
		 				
							//reset operatorData counters
		 					for(j = 0; j < problem.getNumberOfHeuristics(); j++)
		 						for(k = time_index; k < selectionProbAP_index; k++)
		 							operatorData[j][k] = 0;		 				
		 				}
	
	 					//apply this operator to the current population except the best solution
	 					if(operatorData[heuristic_to_apply_index][operatorType_index] == 300)
	 					{
	 						i = 0;
	 						
	 						for(j = 0; j < populationSize-1; j++)
		 					{
	 							//fail safe in case candidatesArray runs out of options
	 							if(heuristic_to_apply_index == 1000)
	 							{
	 								heuristic_to_apply_index = minImpRateArray[0];
	 								break;
	 							}
	 							
		 						problem.applyHeuristic((int) operatorData[heuristic_to_apply_index][operatorID_index], j+1, j+2, j+1 + populationSize);
		 						if(problem.compareSolutions(j+1, j+1 + populationSize) || problem.compareSolutions(j+2, j+1 + populationSize))
		 						{
		 							heuristic_to_apply_index = minImpRateArray[i+1];
		 							i++;
		 							j--;
		 						}
		 						else
		 						{
		 							obj_function_values_new[j+1] = problem.getFunctionValue(j+1 + populationSize);
		 							problem.copySolution(j+1 + populationSize, j+1);
		 							i = 0;
		 						}
		 					}
	 					}
	 					else //operator not 300
	 					{
	 						i = 0;
	 						
	 						for(j = 0; j < populationSize-1; j++)
		 					{
	 							//fail safe in case candidatesArray runs out of options
	 							if(heuristic_to_apply_index == 1000)
	 							{
	 								heuristic_to_apply_index = minImpRateArray[0];
	 								break;
	 							}
	 							
		 						problem.applyHeuristic((int) operatorData[heuristic_to_apply_index][operatorID_index], j+1, j+1 + populationSize);
		 						if(problem.compareSolutions(j+1, j+1 + populationSize))
		 						{
		 							heuristic_to_apply_index = minImpRateArray[i+1];
		 							i++;
		 							j--;
		 						}
		 						else
		 						{
		 							obj_function_values_new[j+1] = problem.getFunctionValue(j+1 + populationSize);
		 							problem.copySolution(j+1 + populationSize, j+1);
		 							i = 0;
		 						}
		 					}
	 					}	
	
	 					//update selection probabilities... AP
	 					for(k = 0; k < problem.getNumberOfHeuristics(); k++)
	 					{
	 						if(k != heuristic_to_apply_index)
	 							operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*(minSelectionProbability - operatorData[k][selectionProbAP_index]);
	 						else
	 							operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*((1-(problem.getNumberOfHeuristics() - 1)*minSelectionProbability) - (operatorData[k][selectionProbAP_index]));
	 					}
					}
				}				
				
				/////////////////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////***************SELF SEARCH EXPLORE END***************//////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				//update the expected number of generations
				avTimePerGenSum = avTimePerGenSum + (System.currentTimeMillis() - generationStartTime);
				avNoOfGenerations = (elomariSS.this.getTimeLimit())/(avTimePerGenSum / ((double)(generation+1)));
							
				//print output
				System.out.println(problem.toString() + "\t" + inst + "\t" + rep + "\t" + restart + "\t" + generation + "\t" + problem.getBestSolutionValue() + "\t" + elomariSS.this.getElapsedTime());
		
				generation++;
			}
		}
		
		else
		{
			////////^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^/////////////
			////////^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^EXPLOIT^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^/////////////
			////////^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^/////////////
			
			avTimePerGenSum = 0;
			noImprovementRunLengthFactor = 0.1;
			
			//focus on exploitation operators
			problem.setIntensityOfMutation(lowIntsOfMutation);
			problem.setDepthOfSearch(highDepthOfSearch);
			
			//resetting... 
			for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
				minSameSolutionsArray[c] = 1000;
			for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
				minImpRateArray[c] = 1000;
			for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
				candidatesArray[c] = 1000;
			minNoImprovement = Double.POSITIVE_INFINITY;
			minSameSolution = Double.POSITIVE_INFINITY;
			minImpRate = Double.POSITIVE_INFINITY;
		
			//find an operator with the highest improvement rate
			for(k = 0; k < problem.getNumberOfHeuristics(); k++)
			{
				if((operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize)) < minImpRate)
				{
					minImpRate = (operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize));
					minImpRate_index = k;
					heuristic_to_apply_index = k;
				}
			}
		
			//focus on exploitation operators
			problem.setIntensityOfMutation(lowIntsOfMutation);
			problem.setDepthOfSearch(highDepthOfSearch + ((double)restart/10));
							 			
			//update selection probabilities... AP
			for(k = 0; k < problem.getNumberOfHeuristics(); k++)
			{
				if(k != heuristic_to_apply_index)
					operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*(minSelectionProbability - operatorData[k][selectionProbAP_index]);
				else
					operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*((1-(problem.getNumberOfHeuristics() - 1)*minSelectionProbability) - (operatorData[k][selectionProbAP_index]));
			}
		
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////********************************************EXPLOIT MAIN**********************************************************//////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			while(!hasTimeExpired())
			{
				generationStartTime = System.currentTimeMillis();
			
				////select an operator
				rndSum = 0;
				rndNumber = rng.nextDouble();
				for(k = 0; k < problem.getNumberOfHeuristics(); k++)
				{
					if(rndNumber <= operatorData[k][selectionProbAP_index] + rndSum)
					{
						heuristic_to_apply = (int) operatorData[k][operatorID_index];
						
						//update selection counter and lastApplied
						for(i = 0; i < problem.getNumberOfHeuristics(); i++)
							if(operatorData[i][operatorID_index] == heuristic_to_apply)
							{
								operatorData[i][noOfCalls_index] = operatorData[i][noOfCalls_index] + 1;
								operatorData[i][lastApplied_index] = generation;
								heuristic_to_apply_index = i;
								break;
							}
						break;
					}
					else
						rndSum = rndSum + operatorData[k][selectionProbAP_index];
				}
						
				//resetting... 
				totalTime = 0;
				minNoImprovement = Double.POSITIVE_INFINITY;
				minSameSolution = Double.POSITIVE_INFINITY;
				minImpRate = Double.POSITIVE_INFINITY;
				noImprovementRunLength = noImprovementRunLengthFactor*avNoOfGenerations;
				for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
					minSameSolutionsArray[c] = 1000;
				for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
					minImpRateArray[c] = 1000;
				for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
					candidatesArray[c] = 1000;

				//for crossover
				if(operatorData[heuristic_to_apply_index][operatorType_index] == 300) 
				{		
					for(j = 0; j < populationSize; j++)
					{
						startTime = System.currentTimeMillis();
						
						obj_function_values_new[j] = problem.applyHeuristic((int) operatorData[heuristic_to_apply_index][operatorID_index], j, j + 1, j + populationSize);
						
						totalTime = totalTime + (System.currentTimeMillis() - startTime);
						
						//collect data about this application
						////Step2: quality variation per application
						operatorData[heuristic_to_apply_index][quality_index] = operatorData[heuristic_to_apply_index][quality_index] + (problem.getFunctionValue(j + populationSize) - Math.min(problem.getFunctionValue(j), problem.getFunctionValue(j+1)));
						
						////Step3: same solutions and no improvements
						if(problem.compareSolutions(j, j + populationSize) || problem.compareSolutions(j+1, j + populationSize))
						{
							operatorData[heuristic_to_apply_index][sameSolution_index] = operatorData[heuristic_to_apply_index][sameSolution_index] + 1;
							operatorData[heuristic_to_apply_index][noImprovement_index] = operatorData[heuristic_to_apply_index][noImprovement_index] + 1;	
						}
						else
						{
							if(problem.getFunctionValue(j + populationSize) > problem.getFunctionValue(j))
								operatorData[heuristic_to_apply_index][noImprovement_index] = operatorData[heuristic_to_apply_index][noImprovement_index] + 1;
						}						
					}
					
					////Step1: total time allocated to this operator
					operatorData[heuristic_to_apply_index][time_index] = operatorData[heuristic_to_apply_index][time_index] + totalTime;	
				}
				else //if not 300
				{
					for(j = 0; j < populationSize; j++)
					{
						startTime = System.currentTimeMillis();
						
						obj_function_values_new[j] = problem.applyHeuristic((int) operatorData[heuristic_to_apply_index][operatorID_index], j, j + populationSize);
											
						totalTime = totalTime + (System.currentTimeMillis() - startTime);
						
						//collect data about this application
						////Step2: quality variation per application
						operatorData[heuristic_to_apply_index][quality_index] = operatorData[heuristic_to_apply_index][quality_index] + (problem.getFunctionValue(j + populationSize) - problem.getFunctionValue(j));
						
						////Step3: same solutions and no improvements
						if(problem.compareSolutions(j, j + populationSize))
						{
							operatorData[heuristic_to_apply_index][sameSolution_index] = operatorData[heuristic_to_apply_index][sameSolution_index] + 1;
							operatorData[heuristic_to_apply_index][noImprovement_index] = operatorData[heuristic_to_apply_index][noImprovement_index] + 1;	
						}
						else
						{
							if(problem.getFunctionValue(j + populationSize) > problem.getFunctionValue(j))
								operatorData[heuristic_to_apply_index][noImprovement_index] = operatorData[heuristic_to_apply_index][noImprovement_index] + 1;
						}							
					}
					
					////Step1: total time allocated to this operator
					operatorData[heuristic_to_apply_index][time_index] = operatorData[heuristic_to_apply_index][time_index] + totalTime;	
				}

				//pool current and new in temp and their indices
				for(k = 0; k < 2*populationSize; k++)
				{
					obj_function_values_temp[k][0] = problem.getFunctionValue(k);
					obj_function_values_temp[k][1] = k;
				}
				
				//sort temp 
				Arrays.sort(obj_function_values_temp, new Comparator<double[]>() 
				{
		            @Override
		            public int compare(double[] a, double[] b) 
		            {
		            	if(restart >= 2)
		            		return (int) (Math.pow(10, 6)*b[0] - Math.pow(10, 6)*a[0]);
		            	else
		            		return (int) (b[0] - a[0]);
		            }
				});	
				
				//copy the best half into obj_function_values_new... with no duplicates
				problem.copySolution((int) obj_function_values_temp[2*populationSize - 1][1], 2*populationSize);
				obj_function_values_new[0] = obj_function_values_temp[2*populationSize - 1][0];
				
				for (i = 0; i < populationSize - 1; i++)
				{
					j = i;
					try
					{
						while(problem.compareSolutions((int)obj_function_values_temp[2*populationSize - 1 - j - 1][1], (int)obj_function_values_temp[2*populationSize - 1 - i][1]))
							j++;
					}
					//used when all solutions in obj_function_values_temp are the same. Just copy one of them.
					catch(Exception e)
					{j = j - 1;}
					
					problem.copySolution((int) obj_function_values_temp[2*populationSize - 1 - j - 1][1], 2*populationSize + i + 1);					
				}
				
				for(k = 0; k < populationSize; k++)
				{
					problem.copySolution(2*populationSize + k, k );
					obj_function_values_new[k] = problem.getFunctionValue(2*populationSize + k);
				}	
				
				//update the operator to pursue... 
				//find an operator with the highest improvement rate
				for(k = 0; k < problem.getNumberOfHeuristics(); k++)
				{
					if((operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize)) < minImpRate)
					{
						minImpRate = (operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize));
						minImpRate_index = k;
						heuristic_to_apply_index = k;
					}
				}
				
				//place the minImpRate operator in minImpRateArray
				i = 0;
				minImpRateArray[i] = minImpRate_index;
				
				//check if there are other operators with minImpRate <= 0
				for(k = 0; k < problem.getNumberOfHeuristics(); k++)
				{
					Arrays.sort(minImpRateArray);
					if((operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize)) <= 0)
						if(Arrays.binarySearch(minImpRateArray, k) < 0)
					{	
						minImpRateArray[i+1] = k;
						i++;
					}
				}				
					
				//update selection probabilities... AP
				for(k = 0; k < problem.getNumberOfHeuristics(); k++)
				{
					if(k != heuristic_to_apply_index)
						operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*(minSelectionProbability - operatorData[k][selectionProbAP_index]);
					else
						operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*((1-(problem.getNumberOfHeuristics() - 1)*minSelectionProbability) - (operatorData[k][selectionProbAP_index]));
				}
				
				/////////////////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////***************SELF SEARCH EXPLOIT START***************////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////
		
				//check if the BSF hasn't changed 
				if(restart <= 2)
					if(obj_function_values_new[0] >= problem.getBestSolutionValue())
						noImprovementCounter++;
					else
						noImprovementCounter = 0;
				else
					if(obj_function_values_new[0] >= obj_function_values_current[0])
						noImprovementCounter++;
					else
						noImprovementCounter = 0;
								
				//copy new to current and calculate population average
				for(k = 0; k < populationSize; k++)
					obj_function_values_current[k] = obj_function_values_new[k];
				
				//if no improvement is seen over the past k generations, restart the search
				if(noImprovementCounter >= noImprovementRunLength)
				{
					noImprovementCounter = 0;
					restart++;
					{
	 					//resetting... 
	 					for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
	 						minSameSolutionsArray[c] = 1000;
	 					for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
	 						minImpRateArray[c] = 1000;
	 					for(c = 0; c <= problem.getNumberOfHeuristics(); c++)
	 						candidatesArray[c] = 1000;
	 					minNoImprovement = Double.POSITIVE_INFINITY;
	 					minSameSolution = Double.POSITIVE_INFINITY;
	 					minImpRate = Double.POSITIVE_INFINITY;
						
	 					//find an operator with the highest improvement rate
	 					for(k = 0; k < problem.getNumberOfHeuristics(); k++)
	 					{
	 						if((operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize)) < minImpRate)
	 						{
	 							minImpRate = (operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize));
	 							minImpRate_index = k;
	 							heuristic_to_apply_index = k;
	 						}
	 					}
	 					
	 					//place the minImpRate operator in minImpRateArray
	 					i = 0;
	 					minImpRateArray[i] = minImpRate_index;
	 					
	 					//check if there are other operators with minImpRate <= 0
	 					for(k = 0; k < problem.getNumberOfHeuristics(); k++)
	 					{
	 						Arrays.sort(minImpRateArray);
	 						if((operatorData[k][quality_index] / operatorData[k][noOfCalls_index]*populationSize) / (operatorData[k][time_index]/(operatorData[k][noOfCalls_index]*populationSize)) <= 0)
	 							if(Arrays.binarySearch(minImpRateArray, k) < 0)
		 						{	
		 							minImpRateArray[i+1] = k;
		 							i++;
		 						}
	 					}
	 					
	 					//focus on exploitation operators
	 					problem.setIntensityOfMutation(lowIntsOfMutation);
	 					problem.setDepthOfSearch(highDepthOfSearch + ((double)restart/10));
		 				
	 					//check if the depth of search reached its limit
		 				if(problem.getDepthOfSearch() == 1)
		 				{
		 					//apply ruin and recreate operators to the current solutions
		 					for(c = 0; c < populationSize; c++)
		 						obj_function_values_new[c] = problem.applyHeuristic(ruin_recreate_heuristics[rng.nextInt(ruin_recreate_heuristics.length)], c, c);		
						
		 					//reset operatorData counters
		 					for(j = 0; j < problem.getNumberOfHeuristics(); j++)
		 						for(k = time_index; k < selectionProbAP_index; k++)
		 							operatorData[j][k] = 0;	
		 				}
	
	 					//apply this operator to the current population
	 					if(operatorData[heuristic_to_apply_index][operatorType_index] == 300)
	 					{
	 						i = 0;
	 						
	 						for(j = 0; j < populationSize-1; j++)
		 					{
	 							//fail safe in case candidatesArray runs out of options
	 							if(heuristic_to_apply_index == 1000)
	 							{
	 								heuristic_to_apply_index = minImpRate_index;
	 								break;
	 							}
	 							
		 						problem.applyHeuristic((int) operatorData[heuristic_to_apply_index][operatorID_index], j+1, j+2, j+1 + populationSize);
		 						if(problem.compareSolutions(j+1, j+1 + populationSize) || problem.compareSolutions(j+2, j+1 + populationSize))
		 						{
		 							heuristic_to_apply_index = minImpRateArray[i+1];
		 							i++;
		 							j--;
		 						}
		 						else
		 						{
		 							obj_function_values_new[j+1] = problem.getFunctionValue(j+1 + populationSize);
		 							problem.copySolution(j+1 + populationSize, j+1);
		 							i = 0;
		 						}
		 					}
	 					}
	 					else //not 300
	 					{
	 						i = 0;
	 						
	 						for(j = 0; j < populationSize-1; j++)
		 					{
	 							//fail safe in case candidatesArray runs out of options
	 							if(heuristic_to_apply_index == 1000)
	 							{
	 								heuristic_to_apply_index = minImpRate_index;
	 								break;
	 							}
	 							
		 						problem.applyHeuristic((int) operatorData[heuristic_to_apply_index][operatorID_index], j+1, j+1 + populationSize);
		 						if(problem.compareSolutions(j+1, j+1 + populationSize))
		 						{
		 							heuristic_to_apply_index = minImpRateArray[i+1];
		 							i++;
		 							j--;
		 						}
		 						else
		 						{
		 							obj_function_values_new[j+1] = problem.getFunctionValue(j+1 + populationSize);
		 							problem.copySolution(j+1 + populationSize, j+1);
		 							i = 0;
		 						}
		 					}
	 					}
	 					
	 					//update selection probabilities... AP
	 					for(k = 0; k < problem.getNumberOfHeuristics(); k++)
	 					{
	 						if(k != heuristic_to_apply_index)
	 							operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*(minSelectionProbability - operatorData[k][selectionProbAP_index]);
	 						else
	 							operatorData[k][selectionProbAP_index] = operatorData[k][selectionProbAP_index] + alpha*((1-(problem.getNumberOfHeuristics() - 1)*minSelectionProbability) - (operatorData[k][selectionProbAP_index]));
	 					}
					}					
				}
				
				/////////////////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////***************SELF SEARCH EXPLOIT END***************//////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				//update the expected number of generations
				avTimePerGenSum = avTimePerGenSum + (System.currentTimeMillis() - generationStartTime);
				avNoOfGenerations = (elomariSS.this.getTimeLimit())/(avTimePerGenSum / ((double)(generation+1)));
						
				//print output
				System.out.println(problem.toString() + "\t" + inst + "\t" + rep + "\t" + restart + "\t" + generation + "\t" + problem.getBestSolutionValue() + "\t" + elomariSS.this.getElapsedTime());
		
				generation++;
			}			
		}
		
		rep++;
		
		if(rep == 5)
		{
			rep = 0;
			inst++;			
		}
		
		if(inst > 9 )
		{ inst = 0; }			
	}

	public String toString() 
	{ return Integer.toString(heuristic_to_apply); }	
}