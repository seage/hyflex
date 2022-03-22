package hsiao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

/**
 * 
 * @author Hsiao Ping-Che
 *	Tournament_v7
 */
public class HsiaoCHeSCHyperheuristic extends HyperHeuristic{
	
	public HsiaoCHeSCHyperheuristic(long seed){
		super(seed);
	}
//	String folder = "T7_T5/";
//	DecimalFormat df=(DecimalFormat)NumberFormat.getInstance(); 
//	DecimalFormat fp=(DecimalFormat)NumberFormat.getInstance(); 
//	PrintStream outOriginal, outView, outLocal, outHeuristic;
	HeuristicCluster heuristicsL, heuristicsRM;
	double[] currentValue;
	ProblemDomain problem;
	int initialPop, populationSize, memorySize, selectionPressure, currentPopulation;
	int heuristicToApply;
	long startTime, elapsedTime;
	int maxLS, minLS, currentLS, adaptiveInterval;
	int[] countLS, countSuc_01;
	double searchDepth, mutationIntensity, maxSD, maxMI;
	
	@Override
	protected void solve(ProblemDomain prob) {
		problem = prob;
		//----setting printing information----//
		
//		SimpleDateFormat sdFormat = new SimpleDateFormat("MMdd_HHmm_ss");
//		Date date = new Date();
//		outOriginal = System.out;
//		try {
//			outView = new PrintStream(new BufferedOutputStream(new
//			        FileOutputStream("C:/- HyFlex/"+folder+sdFormat.format(date)+"_View.txt", false)));
//			outLocal = new PrintStream(new BufferedOutputStream(new
//					FileOutputStream("C:/- HyFlex/"+folder+sdFormat.format(date)+"_Local.txt",false)));
//			outHeuristic = new PrintStream(new BufferedOutputStream(new
//					FileOutputStream("C:/- HyFlex/"+folder+sdFormat.format(date)+"_Heuristic.txt",false)));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		df.setMaximumFractionDigits(5);
//		fp.applyPattern("##.#%");
		
		//----Parameters----//
		initialPop = 6;
		populationSize = initialPop;
		selectionPressure = 2;
		currentPopulation = 0;
		memorySize = 3 + populationSize;
		searchDepth = problem.getDepthOfSearch();
		maxSD = 0.6;
		mutationIntensity = problem.getIntensityOfMutation();
		maxMI = 0.8;
		currentLS = 25;
		maxLS = 25;
		minLS = 1;
		countLS = new int[maxLS];
		countSuc_01 = new int[12];
//		countSuc_02 = new int[12];
//		countSuc_03 = new int[12];
		Arrays.fill(countLS, 0);
		Arrays.fill(countSuc_01, 0);
//		Arrays.fill(countSuc_02, 0);
//		Arrays.fill(countSuc_03, 0);
		//----load and categorize low-level heuristics----//
		
		List<Integer> listL = new ArrayList<Integer>();
		List<Integer> listRM = new ArrayList<Integer>();
		for(Integer i : problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH))
			listL.add(i);
		for(int i : problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION))
			listRM.add(i);
		for(int i : problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE))
			listRM.add(i);
		
		heuristicsL = new HeuristicCluster(listL, rng);
		heuristicsRM = new HeuristicCluster(listRM, rng);

		
		/**
		 * Initialize Object
		 */
		problem.setMemorySize(memorySize);
		problem.setDepthOfSearch(searchDepth);
		problem.setIntensityOfMutation(mutationIntensity);
		currentValue = new double[memorySize];
		
		
		//Multi-Start
		for(int i = offset; i < populationSize + offset; i ++){
			problem.initialiseSolution(i);
			currentValue[i] = problem.getFunctionValue(i);
			currentPopulation++;
			if(this.getElapsedTime() > 10000)
				break;
		}
		
//		System.setOut(outView);
//		System.out.println("Initialize\t" + this.getElapsedTime() +"ms");
		
		/**
		 * Initialize Heuristics
		 * 
		 */
		
		//------heuristicsL------//
		//Test if heuristicsL are stochastic
		startTime = System.currentTimeMillis();
		if(this.getElapsedTime() < 10000){
			int i = offset;
			for(int k : heuristicsL.keyset){
				currentValue[0] = problem.applyHeuristic(k, i, 0);
				for(int j = 0; j < 3; j++){
					currentValue[2] = problem.applyHeuristic(k, i, 2);
					if(!problem.compareSolutions(0, 2)){
						if(currentValue[2] < currentValue[0]){
							problem.copySolution(2, 0);
							currentValue[0] = currentValue[2];
						}
						heuristicsL.setTypeToStochastic(k);
						break;
					}
				}
				problem.copySolution(0, i);
				currentValue[i] = currentValue[0];
				i = (i - offset + 1) % populationSize + offset;
			}
		}
		else{
			for(int k : heuristicsL.keyset)
				heuristicsL.setTypeToStochastic(k);
		}
		
		elapsedTime = System.currentTimeMillis() - startTime;
//		System.setOut(outView);
//		System.out.print("heuristicsL\t");
//		heuristicsL.printType();
//		System.out.println(elapsedTime + "ms/" + this.getElapsedTime() + "ms");
		
		//-----heuristicsRM-----//
		problem.copySolution(offset, 0);
		currentValue[0] = currentValue[offset];
		for(int k : heuristicsRM.keyset){
			startTime = System.currentTimeMillis();
			currentValue[2] = problem.applyHeuristic(k, offset, 2);
			elapsedTime = System.currentTimeMillis() - startTime;
			heuristicsRM.setTime(k, elapsedTime);
			heuristicsRM.setValue(k, currentValue[2]);
			if(currentValue[2] < currentValue[0]){
				problem.copySolution(2, 0);
				currentValue[0] = currentValue[2];
			}
		}
		heuristicsRM.sortByValue();
		for(int i = 0; i < heuristicsRM.size(); i++)
			heuristicsRM.unbiased(i);
		
		problem.copySolution(0, offset);
		currentValue[offset] = currentValue[0];
		problem.copySolution(offset, 1);
		currentValue[1] = currentValue[offset];
		
		hasTimeExpired();
//		System.setOut(outView);
//		System.out.println("heuristicsRM\t" + this.getElapsedTime() + "ms BestSolution=" + this.getBestSolutionValue());
		
		double percentage = (double)this.getElapsedTime() / (double)this.getTimeLimit();
		while(timePeriod < percentage)
			timePeriod += 0.1;
		
		//----other parameters----//
		int heuristicToApply = heuristicsRM.getHeuristicByWheel();
		int k = 0;
		tournament();
		/**
		 * Main Loop
		 */
		while(!hasTimeExpired()){
			/**
			 * Basic VNS
			 */
//			long startTime2, elapsedTime2;
//			int tmpK = k;
//			startTime2 = System.currentTimeMillis();
			bestSolutionValue = this.getBestSolutionValue();
			
			//Shaking: apply Ruin-Recreate + Mutation heuristics
			heuristicToApply = heuristicsRM.getHeuristic(k);
			currentValue[2] = problem.applyHeuristic(heuristicToApply, 1, 2);
//			System.setOut(outView);
//			System.out.print(df.format(currentValue[1]) + "->" + df.format(currentValue[2]) + "->");
//			System.setOut(outLocal);
//			System.out.print(df.format(currentValue[1])  + "->" + df.format(currentValue[2])+"(" +k + "): ");
			
			//Local Search
			localSearch(2);
//			System.setOut(outView);
//			System.out.print(df.format(currentValue[2]) + "->");
			
			//Acceptance
			k = moveOrNot(heuristicsRM, 1, k);
			tournament();
			
			//----Printing----//
//			elapsedTime2 = System.currentTimeMillis() - startTime2;
//			System.setOut(outView);
//			System.out.print(tmpK +"->"+ k + ": " + elapsedTime2 + "ms/" + this.getElapsedTime() + "ms/" 
//					+ fp.format(timePeriod) +"/"+ currentLS + "LS ");
//			System.out.print(heuristicsRM.toTabuString() + "population[");
//			for(int i = offset; i < populationSize + offset; i++)
//				System.out.print(currentValue[i] + ",");
//			System.out.println("]");
//			System.setOut(outOriginal);
//			System.out.println(this.getBestSolutionValue());
			
			//Adaptive adjust LS parameters
			periodAdjust();
		}
//		System.setOut(outHeuristic);
//		heuristicsRM.printStat();
//		System.setOut(outView);
//		System.out.print("searchDepth=" + searchDepth + " mutationIntensity=" + mutationIntensity);
//		System.out.println(" currentLS=" + currentLS + " Iteration=" + heuristicsRM.getIterationCount());
//		System.out.println("Suc_01:" + Arrays.toString(countSuc_01));
//		System.out.println("Suc_02:" + Arrays.toString(countSuc_02));
//		System.out.println("Suc_03:" + Arrays.toString(countSuc_03));
//		outHeuristic.close();
//		outLocal.close();
//		outView.close();
	}
	public void localSearch(int index){
//		System.setOut(outLocal);
		heuristicToApply = heuristicsL.getHeuristicByRank();
		boolean improveFlag = false;
		for(int i = 0; i < currentLS; i++){
			
			currentValue[0] = problem.applyHeuristic(heuristicToApply, index, 0);
			heuristicsL.setRank(heuristicToApply, currentValue[index] - currentValue[0]);
			
			if(problem.compareSolutions(index, 0)){
//				System.out.print("!");
				heuristicsL.setValue(heuristicToApply, -1.0);
			}
//			System.out.print(df.format(currentValue[0]) + "(" + heuristicToApply +") ");
			
			if(currentValue[0] < currentValue[index]){
				problem.copySolution(0, index);
				currentValue[index] = currentValue[0];
				countLS[i]++;
				i = -1;
				improveFlag = true;
				//continue;
			}
			else if (currentValue[0] == currentValue[index]){
				problem.copySolution(0, index);
				currentValue[index] = currentValue[0];
			}
			
			heuristicToApply = heuristicsL.getHeuristicByRank();
			if(improveFlag && heuristicToApply == -1){
				heuristicsL.resetValue();
				improveFlag = false;
				heuristicToApply = heuristicsL.getHeuristicByRank();
			}
			else if(heuristicToApply == -1){
//				System.out.print("N/A");
				break;
			}
		}
		heuristicsL.resetValue();
//		System.out.println();
	}
	double bestSolutionValue;
	public int moveOrNot(HeuristicCluster cluster, int index, int k){ //index = 1
//		System.setOut(outView);
		if(currentValue[2] < currentValue[index]){
			if(currentValue[index] != bestSolutionValue){
				int tmp = (int)(timePeriod * 10.0 + 0.5);
				if(currentValue[2] < bestSolutionValue){
//					System.out.print("Suc_01\t");
					countSuc_01[tmp]++;
				}
				else if(currentValue[2] == bestSolutionValue){
//					System.out.print("Suc_02\t");
//					countSuc_02[tmp]++;
				}
				else{
//					System.out.print("Suc_03\t");
//					countSuc_03[tmp]++;
				}
			}
//			else
//				System.out.print("Succeed\t");
			problem.copySolution(2, index);
			currentValue[index] = currentValue[2];
			cluster.success(k);
			addToPopulation(index);
			k = 0;
			while(heuristicsRM.isTabu(k))
				k = (k + 1) % heuristicsRM.size();
			return k;
		}
		else if(currentValue[2] == currentValue[index]){
			if(problem.compareSolutions(2, index)){
//				System.out.print("Identical\t");
				cluster.addTabu(k);
			}
			else{
//				System.out.print("Equal\t");
				problem.copySolution(2, index);
				currentValue[index] = currentValue[2];
				if(rng.nextDouble() < 0.2)
					cluster.addTabu(k);
				addToPopulation(index);
			}
			cluster.unbiased(k);
			
		}
		else{
			cluster.addTabu(k);
//			System.out.print("\t\t");
			cluster.failure(k);
			addToPopulation(2);
		}
		k = (k + 1) % heuristicsRM.size();
		while(heuristicsRM.isTabu(k)){
			k = (k + 1) % heuristicsRM.size();
		}
		return k;
	}

	double timePeriod = 0.1;
	double lastValue;
	public void periodAdjust(){
//		unimprovedTime = System.currentTimeMillis() - lastSuccessTime;
//		unimprovedRatio = (double)unimprovedTime / (double)this.getTimeLimit();
		
		double percentage = (double)this.getElapsedTime() / (double)this.getTimeLimit();
		if(percentage < timePeriod)
			return;
		
//		System.setOut(outView);
//		System.out.println("//==========" + timePeriod + "==========//");
		while(timePeriod < percentage)
			timePeriod += 0.1;
		
		//-----adjust depth of search-----//
		if(lastValue == problem.getBestSolutionValue()){
			if(true){
				searchDepth += 0.2;
				if(searchDepth > maxSD)
					searchDepth = maxSD;
				problem.setDepthOfSearch(searchDepth);
//				System.out.print("searchDepth=" + searchDepth +" ");
			}
		}
		lastValue = problem.getBestSolutionValue();
		
		//-----adjust currentLS-----//
		int maxSuccessLS = 1;
		for(int i = currentLS - 1; i > 0; i--){
			if(countLS[i] > 0){
				if(i > 0 && countLS[i-1] == 0) continue;
				if(i > 0 && countLS[i-1] > countLS[i]*10) continue;
				maxSuccessLS = i + 1;
				break;
			}
		}
		if(maxSuccessLS != currentLS){
			currentLS = maxSuccessLS;
//			System.out.print("currentLS=" + currentLS +" ");
		}
		
		//-----adjust tabu length-----//
		heuristicsRM.adjustTabuLength();
		
		//-----adjust population size-----//
		adjustPopulationSize();
		
		//-----print-----//
		for(int i = 0; i < maxLS; i++){
//			System.out.print((i+1) + "/" + countLS[i] + " "); 
			countLS[i] = 0;
		}
//		System.out.println();
//		System.out.println("SR=" + fp.format(heuristicsRM.getSuccessRatio()) + " FR=" + fp.format(heuristicsRM.getFailureRatio())
//				+ " SF=" + heuristicsRM.getSuccessFreq() +" FF=" + heuristicsRM.getFailureFreq() + " population=" + populationSize
//				+ " Iteration=" + heuristicsRM.getIterationCount());
	}
	public void localSearchForInitialization(){
		double initialValue = Double.MAX_VALUE;
		for(int i = 0; i < heuristicsL.size(); i++){
			heuristicToApply = heuristicsL.getHeuristic(i);
			initialValue = currentValue[1];
			currentValue[1] = problem.applyHeuristic(heuristicToApply, 1, 1);
			while(currentValue[1] < initialValue){
				initialValue = currentValue[1];
				currentValue[1] = problem.applyHeuristic(heuristicToApply, 1, 1);
			}
		}
	}
	
	int offset = 3;
	int tIndex;
	double tValue = 0.0;
	public void tournament(){
		if(currentPopulation < populationSize || populationSize <= 2){
			return;
		}
		int[] randomArray = new int[populationSize];
		int[] base = new int[populationSize];
		int r;
		for(int i = 0; i < populationSize; i++)
			base[i] = i;
		for(int i = 0; i < populationSize; i++){
			r = rng.nextInt(populationSize - i);
			randomArray[i] = base[r];
			base[r] = base[populationSize - 1]; 
		}
		double minFitness = Double.MAX_VALUE;
		int challenger;
		for(int i = 0; i < selectionPressure; i++){
			challenger = randomArray[i];
			if(currentValue[challenger+offset] < minFitness){
				minFitness = currentValue[challenger+offset];
				tIndex = challenger + offset;
			}
		}
		problem.copySolution(tIndex, 1);
		currentValue[1] = currentValue[tIndex];
		tValue = currentValue[tIndex];
		
	}
	public void addToPopulation(int index){
		if(populationSize <= 2)
			return;
		else if(currentPopulation < populationSize){
			problem.copySolution(index, currentPopulation + offset);
			currentValue[currentPopulation + offset] = currentValue[index];
			currentPopulation++;
			return;
		}
		else if(currentValue[index] == tValue){
			problem.copySolution(index, tIndex);
			return;
		}
		int[] randomArray = new int[populationSize];
		int[] base = new int[populationSize];
		int r;
		for(int i = 0; i < populationSize; i++)
			base[i] = i;
		for(int i = 0; i < populationSize; i++){
			r = rng.nextInt(populationSize - i);
			randomArray[i] = base[r];
			base[r] = base[populationSize - 1]; 
		}
		double maxFitness = -1;
		int maxIndex = -1;
		int challenger;
		for(int i = 0; i < selectionPressure; i++){
			challenger = randomArray[i];
			if(currentValue[challenger+offset] > maxFitness){
				maxFitness = currentValue[challenger+offset];
				maxIndex = challenger + offset;
			}
		}
		problem.copySolution(index, maxIndex);
		currentValue[maxIndex] = currentValue[index];
	}
	public void adjustPopulationSize(){
		int tmp = (int)(timePeriod * 10 + 0.5) - 1;
		if((countSuc_01[tmp] <= 1 || tmp >= 5) && populationSize != 1){
			double minValue = currentValue[offset];
			int minIndex = offset;
			for(int i = offset+1; i < populationSize+offset; i++){
				if(currentValue[i] < minValue){
					minValue = currentValue[i];
					minIndex = i;
				}
			}
			problem.copySolution(minIndex, offset);
			currentValue[offset] = currentValue[minIndex];
			populationSize = 1;
		}
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "HsiaoCHeSCHyperheuristic";
	}
}