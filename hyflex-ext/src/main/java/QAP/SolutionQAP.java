package QAP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import hfu.BasicSolution;
import hfu.RNG;
import hfu.heuristics.modifiers.nh.CombinationNH;
import hfu.heuristics.modifiers.nh.RangeNH;

public class SolutionQAP extends BasicSolution<InfoQAP>{
	
	private int[] fac2loc; //facility to location (-1 = unassigned)
	int assigned;

	public SolutionQAP(InfoQAP instance) {
		super(instance);
		fac2loc = new int[instance.getN()];
		for(int i = 0; i < fac2loc.length;i++){
			fac2loc[i] = -1;
		}
		assigned = 0;
	}
	
	protected SolutionQAP(SolutionQAP other) {
		super(other);
		assigned = other.assigned;
		fac2loc = new int[instance.getN()];
		System.arraycopy(other.fac2loc, 0, fac2loc, 0, fac2loc.length);
	}

	@Override
	public boolean isEqualTo(BasicSolution<InfoQAP> other) {
		if(other instanceof SolutionQAP){
			SolutionQAP other_qap = (SolutionQAP) other;
			if(assigned == other_qap.assigned && getFunctionValue() == other_qap.getFunctionValue()){
				return Arrays.equals(fac2loc, other_qap.fac2loc);
			}
		}
		return false;
	}

	@Override
	public BasicSolution<InfoQAP> deepCopy() {
		return new SolutionQAP(this);
	}

	@Override
	public String toText() {
		String result = "";
		for(int i = 0; i < fac2loc.length;i++){
			result += "f"+i+" -> l"+fac2loc[i]+System.lineSeparator();
		}
		return result+System.lineSeparator()+" ("+getFunctionValue()+")";
	}

	@Override
	protected double evaluateFunctionValue() {
		double e = 0;
		for(int i = 0; i < fac2loc.length;i++){
			for(int j = 0; j < fac2loc.length;j++){
				e += instance.getFlow(i,j)*instance.getDistance(fac2loc[i], fac2loc[j]);
			}
		}
		return e;
	}

	@Override
	public boolean isPartial() {
		return assigned < instance.getN();
	}

	@Override
	public boolean isEmpty() {
		return assigned == 0;
	}
	
	private static void shuffleArray(int[] ar)
	  {
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = RNG.get().nextInt(i + 1);
	      // Simple swap
	      int a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }
	
	public void randomInit(){
		for(int i = 0; i < fac2loc.length;i++){
			fac2loc[i] = i;
		}
		shuffleArray(fac2loc);
		assigned = fac2loc.length;
		setFunctionValue(-1);
	}
	
	public void swap(int fi, int fj){
		double e = getFunctionValue();
		e -= evaluateFacility(fi);
		e -= evaluateFacility(fj);
		int temp_loc = fac2loc[fi];
		fac2loc[fi] = fac2loc[fj];
		fac2loc[fj] = temp_loc;
		e += evaluateFacility(fi);
		e += evaluateFacility(fj);
		setFunctionValue(e);
	}
	
	public void reAssignRandomFraction(double frac){
		int n_ua = (int) Math.ceil(frac*fac2loc.length);
		int[] ua_locs = new int[n_ua];
		int[] fs = new int[fac2loc.length];
		for(int i = 0; i < fac2loc.length;i++){
			fs[i] = i;
		}
		removeRandom(ua_locs,fs);
		//re-assign randomly
		shuffleArray(ua_locs);
		for(int i = 0; i < n_ua;i++){
			fac2loc[fs[assigned]] = ua_locs[i];
			assigned++;
		}
		setFunctionValue(-1);
	}
	
	public void reAssignGreedyFFraction(double frac){
		int n_ua = (int) Math.ceil(frac*fac2loc.length);
		int[] ua_locs = new int[n_ua];
		int[] fs = new int[fac2loc.length];
		for(int i = 0; i < fac2loc.length;i++){
			fs[i] = i;
		}
		removeRandom(ua_locs,fs);
		//re-assign greedily, assign facilities to locations in order of summed flow
		int[] f_ordered = instance.getWeightedFacilities();
		for(int i = 0; i < fac2loc.length;i++){
			if(!isPartial()){
				break;
			}
			//check whether f_ordered[i] is unassigned
			if(fac2loc[f_ordered[i]] == -1){
				//collect all unassigned with equal sum_of_flows
				double sof = instance.getSumOfFlow(f_ordered[i]);
				ArrayList<Integer> candidates = new ArrayList<Integer>(1);
				int j = i;
				while(j < fac2loc.length && instance.getSumOfFlow(f_ordered[j]) == sof){
					if(fac2loc[f_ordered[j]] == -1){
						candidates.add(j);
					}
					j++;
				}
				//select a random candidate
				int pivot = candidates.get(RNG.get().nextInt(candidates.size()));
				int f = f_ordered[pivot];
				//swap to make sure f_ordered[i] is not skipped
				f_ordered[pivot] = f_ordered[i];
				f_ordered[i] = f;
				//insert it greedily (minimizing its contribution)
				double best = Double.MAX_VALUE;
				ArrayList<Integer> bestLoc = new ArrayList<Integer>(1);
				for(int k = 0; k < n_ua; k++){
					if(ua_locs[k] >= 0){
						double e = evaluateFacilityAtLocation(f,ua_locs[k]);
						if(e <= best){
							if(e < best){
								bestLoc.clear();
								best = e;
							}
							bestLoc.add(k);
						}
					}
				}
				//select randomly amongst the best locations
				pivot = bestLoc.get(RNG.get().nextInt(bestLoc.size()));
				fac2loc[f] = ua_locs[pivot];
				ua_locs[pivot] = -1;
				assigned++;
			}
		}
		setFunctionValue(-1);
	}
	
	public void reAssignGreedyLFraction(double frac){
		int n_ua = (int) Math.ceil(frac*fac2loc.length);
		int[] ua_locs = new int[n_ua];
		int[] fs = new int[fac2loc.length];
		for(int i = 0; i < fac2loc.length;i++){
			fs[i] = i;
		}
		removeRandom(ua_locs,fs);
		//re-assign greedily, assign locations to facilities in order of summed distance
		int[] l_ordered = instance.getWeightedLocations();
		for(int i = 0; i < fac2loc.length;i++){
			if(!isPartial()){
				break;
			}
			//check whether l_ordered[i] is unassigned
			boolean l_ua = false;
			for(int m = 0; m < n_ua;m++){
				if(l_ordered[i] == ua_locs[m]){
					l_ua = true;
					break;
				}
			}
			if(l_ua){
				//collect all unassigned with equal sum_of_flows
				double sod = instance.getSumOfDist(l_ordered[i]);
				ArrayList<Integer> candidates = new ArrayList<Integer>(1);
				int j = i;
				while(j < fac2loc.length && instance.getSumOfDist(l_ordered[j]) == sod){
					l_ua = false;
					for(int m = 0; m < n_ua;m++){
						if(l_ordered[j] == ua_locs[m]){
							l_ua = true;
							break;
						}
					}
					if(l_ua){
						candidates.add(j);
					}
					j++;
				}
				//select a random candidate
				int pivot = candidates.get(RNG.get().nextInt(candidates.size()));
				int l = l_ordered[pivot];
				//swap to make sure f_ordered[i] is not skipped
				l_ordered[pivot] = l_ordered[i];
				l_ordered[i] = l;
				//insert it greedily (minimizing its contribution)
				double best = Double.MAX_VALUE;
				ArrayList<Integer> bestFac = new ArrayList<Integer>(1);
				for(int k = 0; k < n_ua; k++){
					int index = fs.length+k-n_ua;
					if(fs[index] >= 0){
						double e = evaluateFacilityAtLocation(fs[index],l);
						if(e <= best){
							if(e < best){
								bestFac.clear();
								best = e;
							}
							bestFac.add(index);
						}
					}
				}
				//select randomly amongst the best locations
				pivot = bestFac.get(RNG.get().nextInt(bestFac.size()));
				fac2loc[fs[pivot]] = l;
				fs[pivot] = -1;
				assigned++;
			}
		}
		setFunctionValue(-1);
	}
	
	private double evaluateFacilityAtLocation(int f, int l){
		fac2loc[f] = l;
		double ef = 0;
		for(int i = 0; i < fac2loc.length; i++){
			if(fac2loc[i] >= 0){
				ef += instance.getFlow(f, i)*instance.getDistance(l, fac2loc[i]);
				ef += instance.getFlow(i, f)*instance.getDistance(fac2loc[i], l);
			}
		}
		fac2loc[f] = -1;
		return ef;
	}
	
	private void removeRandom(int[] ua_locs, int[] fs){
		//unassign n_ua
		for(int i = 0; i < ua_locs.length;i++){
			int pivot = RNG.get().nextInt(assigned);
			ua_locs[i] = fac2loc[fs[pivot]];
			fac2loc[fs[pivot]] = -1;
			assigned--;
			int temp_fac = fs[pivot];
			fs[pivot] = fs[assigned];
			fs[assigned] = temp_fac;
		}
	}
	
	public void pmx(SolutionQAP o){
		//choose 2 cut points uniformly at random
		int i = 1+RNG.get().nextInt(fac2loc.length-1);
		int j = RNG.get().nextInt(fac2loc.length);
		if(i > j){
			int temp = i;
			i = j;
			j = temp;
		}
		//determine middle -> middle mapping (adapting int->int)
		Set<Integer> this_middle = new HashSet<Integer>();
		for(int k = i; k < j;k++){
			this_middle.add(fac2loc[k]);
		}
		Set<Integer> other_middle = new HashSet<Integer>();
		for(int k = i; k < j;k++){
			other_middle.add(o.fac2loc[k]);
		}
		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
		int key_k = i;
		int val_k = i;
		while(key_k < j && val_k < j){
			while(key_k < j && this_middle.contains(o.fac2loc[key_k])){
				key_k++;
			}
			while(val_k < j && other_middle.contains(fac2loc[val_k])){
				val_k++;
			}
			if(key_k < j && val_k < j){
				map.put(o.fac2loc[key_k],fac2loc[val_k]);
				val_k++;
				key_k++;
			}
		}
		//perform xo
		System.arraycopy(o.fac2loc, i, fac2loc, i, j-i);
		//restore applying mapping to first & last
		for(int k = 0; k < i;k++){
			if(map.containsKey(fac2loc[k])){
				fac2loc[k] = map.get(fac2loc[k]);
			}
		}
		for(int k = j; k < fac2loc.length;k++){
			if(map.containsKey(fac2loc[k])){
				fac2loc[k] = map.get(fac2loc[k]);
			}
		}
		setFunctionValue(-1);
	}
	
	public void ox(SolutionQAP o){
		//choose 2 cut points uniformly at random
		int i = 1+RNG.get().nextInt(fac2loc.length-1);
		int j = RNG.get().nextInt(fac2loc.length);
		if(i > j){
			int temp = i;
			i = j;
			j = temp;
		}
		//determine a set of already added genes
		Set<Integer> other_middle = new HashSet<Integer>();
		for(int k = i; k < j;k++){
			other_middle.add(o.fac2loc[k]);
		}
		//restore by sliding approach (preserving order)
		int g = j;
		for(int k = j; k < fac2loc.length;k++){
			if(!other_middle.contains(fac2loc[k])){
				fac2loc[g] = fac2loc[k];
				g++;
			}
		}
		for(int k = 0; k < j;k++){
			if(!other_middle.contains(fac2loc[k])){
				fac2loc[g%fac2loc.length] = fac2loc[k];
				g++;
			}
		}
		//perform xo
		System.arraycopy(o.fac2loc, i, fac2loc, i, j-i);
		setFunctionValue(-1);
	}
	
	public int swapBest(Set<Integer> tabu){
		int best_index = -1;
		double best_value = Double.MAX_VALUE;
		for(int i = 0; i < fac2loc.length;i++){
			for(int j = i+1; j < fac2loc.length;j++){
				int index = i*fac2loc.length+j;
				if(!tabu.contains(index)){
					double e_delta = 0;
					//evaluate swap
					e_delta -= evaluateFacility(i);
					e_delta -= evaluateFacility(j);
					int temp_loc = fac2loc[i];
					fac2loc[i] = fac2loc[j];
					fac2loc[j] = temp_loc;
					e_delta += evaluateFacility(i);
					e_delta += evaluateFacility(j);
					//restore
					temp_loc = fac2loc[j];
					fac2loc[j] = fac2loc[i];
					fac2loc[i] = temp_loc;
					if(e_delta < best_value){
						best_index = index;
						best_value = e_delta;
					}
				}
			}
		}
		if(best_index != -1){
			//perform swap
			int i = best_index/fac2loc.length;
			int j = best_index%fac2loc.length;
			int temp_loc = fac2loc[i];
			fac2loc[i] = fac2loc[j];
			fac2loc[j] = temp_loc;
			setFunctionValue(getFunctionValue()+best_value);
		}else{
			//do nothing (all are tabu)
		}
		return best_index;
	}
	
	private double evaluateFacility(int f){
		double ef = 0;
		int loc = fac2loc[f];
		for(int i = 0; i < fac2loc.length; i++){
			ef += instance.getFlow(f, i)*instance.getDistance(loc, fac2loc[i]);
			ef += instance.getFlow(i, f)*instance.getDistance(fac2loc[i], loc);
		}
		return ef;
	}
	
	static public class SwapNH extends CombinationNH<InfoQAP>{

		public SwapNH(InfoQAP instance) {
			super(new RangeNH<InfoQAP>(0, instance.getN(), instance),2,instance);
		}
		
	}

}
