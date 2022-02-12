package hsiao;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;


public class HeuristicCluster{

	Random random;
	private Map<Integer, Integer> mapIndex; // <heuristic, index>
	private List<Double> value;
	private List<Integer> time;
	private List<Double> improvement;
	private List<Double> amplitude;
	public List<Integer> keyset; // [heuristic1, heuristic2, ... ,heuristicn]
	private Queue<Integer> tabuList; //record tabu heuristics in "index"
	private List<Integer> nonTabuList;
	private int tabuLength, maxTabuLength;
	private List<HeuristicType> type;
	private List<Integer> indexOfStochastic;
	private double maxValue;
	
	public HeuristicCluster(List<Integer> indices, Random rng){
		random = rng;
		mapIndex = new HashMap<Integer, Integer>();
		value = new ArrayList<Double>();
		time = new ArrayList<Integer>();
		improvement = new ArrayList<Double>();
		amplitude = new ArrayList<Double>();
		tabuList = new LinkedList<Integer>();
		nonTabuList = new LinkedList<Integer>();
		type = new ArrayList<HeuristicType>();
		indexOfStochastic = new ArrayList<Integer>();
		int j = 0;
		for(int i : indices){
			mapIndex.put(i, j);
			j++;
		}
		for(int i = 0; i < mapIndex.size(); i++){
			value.add(1.0);
			time.add(1);
			improvement.add(0.0);
			amplitude.add(0.0);
			type.add(HeuristicType.DETERMINISTIC);
			nonTabuList.add(i);
		}
		keyset = new ArrayList<Integer>(indices);
		
		//-----tabu part-----//
		//note: # of heuristics: 7, 5, 4, 7
		tabuLength = mapIndex.size() / 2;
		if(tabuLength < 1)
			tabuLength = 1;
		maxTabuLength = mapIndex.size() - 1;
		
		//-----Statistic part-----//
		
		successCount = new int[mapIndex.size()];
		failureCount = new int[mapIndex.size()];
		unbiasedCount = new int[mapIndex.size()];
		useCount = new int[mapIndex.size()];
		Arrays.fill(successCount, 0);
		Arrays.fill(failureCount, 0);
		Arrays.fill(unbiasedCount, 0);
		Arrays.fill(useCount, 0);
		totalSuccess = 0;
		totalFailure = 0;
		totalUnbiased = 0;
		totalUsed = 0;
	}
	public enum HeuristicType{
	   DETERMINISTIC(1),STOCHASTIC(0);
	   int type;
	   HeuristicType(int type){
	       this.type = type;
	   }
	}
	public void setTypeToStochastic(int heuristic){
		int index = mapIndex.get(heuristic);
		type.set(index, HeuristicType.STOCHASTIC);
		if(!indexOfStochastic.contains(index))
			indexOfStochastic.add(index);
	}
	public void setRank(int heuristic, double r){
		int index = mapIndex.get(heuristic);
		if(r > 0)
			value.set(index, 1.0);
		else if (r == 0){
			if(type.get(index) == HeuristicType.DETERMINISTIC)
				value.set(index, -1.0);
			else if(type.get(index) == HeuristicType.STOCHASTIC)
				value.set(index, 0.0);
			else
				System.err.println("HeuristicCluster.setRank error!!"); 
		}
		else{
			value.set(index, -1.0);
//			System.err.println("HeuristicCluster.setRank error! index:" 
//					+ index + " heuristic:" + heuristic);
		}
	}
	public void setValue(int heuristic, double v){
		int index = mapIndex.get(heuristic);
		value.set(index, v);
	}
	public void resetValue(){
		Collections.fill(value, 1.0);
	}
	public void setImprovement(int heuristic, double i){
		int index = mapIndex.get(heuristic);
		if(i > improvement.get(index))
			improvement.set(index, i);
	}
	public void resetImprovement(){
		amplitude.removeAll(amplitude);
		amplitude.addAll(improvement);
		Collections.fill(improvement, 0.0);
	}
	public void setTime(int heuristic, long t){
		int index = mapIndex.get(heuristic);
		if(t == 0)
			time.set(index, 1);
		else
			time.set(index, (int)t);
	}
	
	/**
	 * 
	 * @param i
	 * @return the ith key
	 */
	public int getHeuristic(int index){
		return keyset.get(index);
	}
	public int getIndex(int heuristic){
		return mapIndex.get(heuristic);
	}
	public double getValue(int heuristic){
		return value.get(mapIndex.get(heuristic));
	}
	public long getTime(int heuristic){
		return time.get(mapIndex.get(heuristic));
	}
	public double getMaxValue(){
		return maxValue;
	}
	public int getHeuristicOfMaxValue(){
		List<Integer> maxList = new LinkedList<Integer>();
		maxValue = value.get(0);
		maxList.add(0);
		for(int i = 1; i < mapIndex.size(); i++){
			if(value.get(i) > maxValue){
				maxValue = value.get(i);
				maxList.removeAll(maxList);
				maxList.add(i);
			}
			else if(value.get(i) == maxValue){
				maxList.add(i);
			}
		}
		int listIndex = random.nextInt(maxList.size());
		int maxIndex = maxList.get(listIndex);
		return getHeuristic(maxIndex);
	}
	public int getHeuristicOfMaxValue2(){
		List<Integer> maxList = new LinkedList<Integer>();
		maxValue = value.get(0);
		maxList.add(0);
		for(int i = 1; i < mapIndex.size(); i++){
			if(value.get(i) > maxValue){
				maxValue = value.get(i);
				maxList.removeAll(maxList);
				maxList.add(i);
			}
			else if(value.get(i) == maxValue){
				maxList.add(i);
			}
		}
		if(maxValue < 0)
			return -1;
		
		int maxIndex = maxList.get(random.nextInt(maxList.size()));
		return getHeuristic(maxIndex);
	}
	public int getHeuristicByRank(){
		List<Integer> maxList = new LinkedList<Integer>();
		maxValue = value.get(0);
		maxList.add(0);
		for(int i = 1; i < mapIndex.size(); i++){
			if(value.get(i) > maxValue){
				maxValue = value.get(i);
				maxList.removeAll(maxList);
				maxList.add(i);
			}
			else if(value.get(i) == maxValue){
				maxList.add(i);
			}
		}
		if(maxValue < 0)
			return -1;
		
		//int maxIndex = getIndexOfMaxAmplitude(maxList);
		int maxIndex = maxList.get(random.nextInt(maxList.size()));
		return getHeuristic(maxIndex);
	}
	public int getIndexOfMaxAmplitude(List<Integer> maxList){
		List<Integer> maxAmpList = new LinkedList<Integer>();
		double maxAmplitude = amplitude.get(maxList.get(0));
		maxAmpList.add(0);
		for(int i = 1; i < maxList.size(); i++){
			if(amplitude.get(maxList.get(i)) > maxAmplitude){
				maxAmplitude = amplitude.get(maxList.get(i));
				maxAmpList.removeAll(maxAmpList);
				maxAmpList.add(i);
			}
			else if(amplitude.get(maxList.get(i)) == maxAmplitude)
				maxAmpList.add(i);
		}
		return maxAmpList.get(random.nextInt(maxAmpList.size()));
	}
	public int getHeuristicByWheel(){
		double pointer = random.nextDouble();
		double totalValue = 0.0, pointerValue = 0.0;
		int index = nonTabuList.get(0);
		for(int i : nonTabuList)
			totalValue += value.get(i);
		for(int i : nonTabuList){
			pointerValue += value.get(i);
			index = i;
			if(pointer < pointerValue / totalValue)
				break;
		}
		return getHeuristic(index);
	}
	
	public int size(){
		return mapIndex.size();
	}
	public void printToString(){
		for(int i = 0; i < mapIndex.size(); i++){
			System.out.print(value.get(i) + "(" + improvement.get(i) +"/"+ time.get(i) +  ")\t");
		}
		System.out.print("||\t");
	}
	public void sortByTime(){
		quickSortTime(0, mapIndex.size()-1);
	}
	public void quickSortTime(int left, int right){
		int i = left, j = right + 1;
		if(left < right){
			while(true){
				while(i < right && time.get(++i) < time.get(left));
				while(j > left && time.get(--j) > time.get(left));
				if(i >= j) break;
				swap(i, j);
			}
			swap(left, j);
			quickSortTime(left, j-1);
			quickSortTime(j+1, right);
		}
	}
	public void sortByValue(){
		quickSortValue(0, mapIndex.size()-1);
	}
	public void quickSortValue(int left, int right){
		int i = left, j = right +1;
		if(left < right){
			while(true){
				while(i < right && value.get(++i) < value.get(left));
				while(j > left && value.get(--j) > value.get(left));
				if(i >= j) break;
				swap(i,j);
			}
			swap(left, j);
			quickSortValue(left, j-1);
			quickSortValue(j+1, right);
		}
	}
	public void sortByFailureRatio(){
		
	}
	
	public void swap(int i, int j){
		int indexA = getHeuristic(i);
		int indexB = getHeuristic(j);
		mapIndex.put(indexA, j);
		mapIndex.put(indexB, i);
		keyset.set(i, indexB);
		keyset.set(j, indexA);
		
		double valueTmp = value.get(i);
		value.set(i, value.get(j));
		value.set(j, valueTmp);
		
		int timeTmp = time.get(i);
		time.set(i, time.get(j));
		time.set(j, timeTmp);
	}
	//=============================//
	//==========Tabu Part==========//
	//=============================//
	public boolean isTabu(int index){
		if(tabuList.contains(index))
			return true;
		return false;
	}
	public void addTabu(int index){
		Integer indexObject = index;
		tabuList.add(index);
		nonTabuList.remove(indexObject);
		while(tabuList.size() > tabuLength){
			indexObject = tabuList.poll();
			nonTabuList.add(indexObject);
		}
	}
	public void emptyTabu(){
		tabuList.clear();
	}
	public String toTabuString(){
		return "tabu:" + tabuList.toString() + "/" + nonTabuList.toString();
	}
	public void adjustTabuLength(){
		if(tabuLength == maxTabuLength)
			return;
		else if(getFailureRatio() > 0.4){
			tabuLength++;
//			System.out.println("tabuLength is set to " + tabuLength);
		}
	}
	
	//=============================================//
	//==========heuristics statistic part==========//
	//=============================================//

	int[] successCount;
	int[] failureCount;
	int[] unbiasedCount;
	int[] useCount;
	int totalSuccess, totalFailure, totalUnbiased, totalUsed;
	
	public void success(int index){
		successCount[index]++;
		useCount[index]++;
		totalSuccess++;
		totalUsed++;
	}
	public void failure(int index){
		failureCount[index]++;
		useCount[index]++;
		totalFailure++;
		totalUsed++;
	}
	public void unbiased(int index){
		unbiasedCount[index]++;
		useCount[index]++;
		totalUnbiased++;
		totalUsed++;
	}
	public int getSuccessCount(int index){
		 return successCount[index];
	}
	public int getUnbiasedCount(int index){
		return unbiasedCount[index];
	}
	public int getSuccessFreq(){
		return totalSuccess;
	}
	public int getFailureFreq(){
		return totalFailure;
	}
	public int getUnbiasedFreq(){
		return totalUnbiased;
	}
	public double getSuccessRatio(){
		return (double)totalSuccess / totalUsed;
	}
	public double getFailureRatio(){
		return (double)totalFailure / totalUsed;
	}
	public double getSuccessRatio(int index){
		return (double)successCount[index]/useCount[index];
	}
	public double getUnbiasedRatio(int index){
		return (double)unbiasedCount[index]/useCount[index];
	}
	public double getSumOfSuccessRatio(){
		double sum = 0.0;
		for(int i = 0; i < mapIndex.size(); i++){
			sum += getSuccessRatio(i);
		}
		return sum;
	}
	public double getSumOfUnbiasedRatio(){
		double sum = 0.0;
		for(int i = 0; i < mapIndex.size(); i++){
			sum += getUnbiasedRatio(i);
		}
		return sum;
	}
	public int getWorstHeuristic(){
		int worst = 0;
		double worstRate = (double)failureCount[0] / useCount[0];
		double tmp;
		for(int i = 1; i < mapIndex.size(); i++){
			tmp = (double)failureCount[i] / useCount[i];
			if(tmp > worstRate){
				worst = i;
				worstRate = tmp;
			}
		}
		return worst;
	}
	public int getIterationCount(){
		return totalUsed;
	}
	//==============================//
	//==========Print part==========//
	//==============================//
	public void printValue(){
		DecimalFormat df = (DecimalFormat)NumberFormat.getInstance();
		df.setMaximumFractionDigits(4);
		System.out.print("Value\t");
		for(int i = 0; i < mapIndex.size(); i++)
			System.out.print(df.format(value.get(i)) + "\t");
		System.out.println();
	}
	public void printStat(){
		DecimalFormat df=(DecimalFormat)NumberFormat.getInstance(); 
		df.applyPattern("##.#%");
		
		System.out.print("SuccessCount\t");
		for(int i : successCount){
			System.out.print(i + "\t");
		}
		System.out.println();
		System.out.print("FailureCount\t");
		for(int i : failureCount){
			System.out.print(i + "\t");
		}
		System.out.println();
		System.out.print("UnbiasedCount\t");
		for(int i : unbiasedCount){
			System.out.print(i + "\t");
		}
		System.out.println();
		System.out.print("TotalCount\t");
		for(int i : useCount){
			System.out.print(i + "\t");
		}
		System.out.println();
		System.out.print("SuccessRate\t");
		for(int i = 0; i < successCount.length; i ++){
			double rate = (double)successCount[i] / useCount[i];
			System.out.print(df.format(rate) + "\t");
		}
		System.out.println();
		System.out.print("FailureRate\t");
		for(int i = 0; i < failureCount.length; i++){
			double rate = (double)failureCount[i] / useCount[i];
			System.out.print(df.format(rate) + "\t");
		}
		System.out.println();
		System.out.print("UnbiasedRate\t");
		for(int i = 0; i < unbiasedCount.length; i++){
			double rate = (double)unbiasedCount[i] / useCount[i];
			System.out.print(df.format(rate) + "\t");
		}
		System.out.println();
		System.out.print("Frequency\t");
		for(int i = 0; i < useCount.length; i++){
			double rate = (double)useCount[i] / totalUsed;
			System.out.print(df.format(rate) + "\t");
		}
		System.out.println();
		this.printValue();
		System.out.println("TotalSuccessRate =\t" + df.format((double)totalSuccess / totalUsed) + "\ttotalSuccess\t" + totalSuccess);
		System.out.println("TotalFailureRate =\t" + df.format((double)totalFailure / totalUsed) + "\ttotalFailure\t" + totalFailure);
		System.out.println("TotalUnbiasedRate =\t" + df.format((double)totalUnbiased / totalUsed) + "\ttotalUnbiased\t" + totalUnbiased);
		System.out.println("Total Iteration =\t" + (totalUsed));
	}
	public void printType(){
		for(int i = 0; i < mapIndex.size(); i++){
			System.out.print(type.get(i) + "\t");
		}
	}
	public void printAmplitude(){
		System.out.println(amplitude);
	}
}
