package QAP;

import java.util.ArrayList;
import java.util.Collections;

import hfu.BenchmarkInfo;
import hfu.datastructures.AdjecencyList;
import hfu.datastructures.Graph;

public class InfoQAP implements BenchmarkInfo {

	private int n;
	private int[][] distances;
	private int[][] flows;
	private int[] sum_flows;
	private int[] sum_dists;
	private int[] f_ordered;
	private int[] l_ordered;
	
	public InfoQAP(int[][] distances, int[][] flows){
		this.n = distances.length;
		this.distances = distances;
		this.flows = flows;
		sum_flows = new int[n];
		for(int f = 0; f < n; f++){
			for(int fo = 0; fo < n; fo++){
				sum_flows[f] += flows[f][fo]+flows[fo][f];
			}
		}
		sum_dists = new int[n];
		for(int l = 0; l < n; l++){
			for(int lo = 0; lo < n; lo++){
				sum_dists[l] += distances[l][lo]+distances[lo][l];
			}
		}
		ArrayList<KeyValuePair> pairs = new ArrayList<KeyValuePair>(n);
		for(int i = 0; i < n;i++){
			pairs.add(new KeyValuePair(i,sum_flows[i]));
		}
		Collections.sort(pairs);
		f_ordered = new int[n];
		for(int i = 0; i < n;i++){
			f_ordered[i] = pairs.get(i).k;
		}
		pairs.clear();
		for(int i = 0; i < n;i++){
			pairs.add(new KeyValuePair(i,sum_dists[i]));
		}
		Collections.sort(pairs);
		l_ordered = new int[n];
		for(int i = 0; i < n;i++){
			l_ordered[i] = pairs.get(i).k;
		}
		
	}
	
	public int getN(){
		return n;
	}
	
	public int getDistance(int li, int lj){
		return distances[li][lj];
	}
	
	public int getFlow(int fi, int fj){
		return flows[fi][fj];
	}
	
	public int getSumOfFlow(int f){
		return sum_flows[f];
	}
	
	public int getSumOfDist(int l){
		return sum_dists[l];
	}
	
	public int[] getWeightedFacilities(){
		return f_ordered;
	}
	
	public int[] getWeightedLocations(){
		return l_ordered;
	}
	
	class KeyValuePair implements Comparable<KeyValuePair>{
		int k;
		double v;
		
		KeyValuePair(int k, double v){
			this.k = k;
			this.v = v;
		}

		@Override
		public int compareTo(KeyValuePair o) {
			if(v < o.v){
				return 1;
			}else if(v > o.v){
				return -1;
			}
			return 0;
		}
		
		
	}

}
