package KP;

import hfu.BenchmarkInfo;

public class InfoKP implements BenchmarkInfo {
    int capacity;
	int[] profits;
	int[] weights;
	long total_profit = -1;
	
	public InfoKP(int capacity, int[] profits, int[] weights){
		this.capacity = capacity;
		this.profits = profits;
		this.weights = weights;
	}
	
	public int getWeight(int i){
		return weights[i];
	}
	
	public int getProfit(int i){
		return profits[i];
	}
	
	public int getPPW(int i){
		return profits[i]/weights[i];
	}
	
	public int getNitems(){
		return profits.length;
	}
	
	public long getTotalProfit(){
		if(total_profit == -1){
			total_profit = 0;
			for(int i = 0; i < profits.length;i++){
				total_profit += profits[i];
			}
		}
		return total_profit;
	}
	
	public int getCapacity(){
		return capacity;
	}

}
