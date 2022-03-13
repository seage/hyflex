package hfu.heuristics.modifiers.nh;

import java.util.Arrays;

import hfu.BenchmarkInfo;
import hfu.RNG;
import hfu.heuristics.modifiers.nh.filter.Filter;

public class CombinationNH<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH{

	RangeNH<P> nh;
	int lo;
	int hi;
	Filter filter;
	int k;
	
	
	public CombinationNH(RangeNH<P> nh, int k, P instance) {
		super(instance);
		this.nh = nh;
		this.k = k;
		this.lo = nh.getLow();
		this.hi = nh.getHigh();
	}
	
	public CombinationNH(RangeNH<P> nh, int k, Filter filter, P instance) {
		this(nh,k,instance);
		this.filter = filter;
	}
	
	private int[] sample_unfiltered(){
		int[] sample = new int[k];
		for(int i = 0; i < k; i++){
			sample[i] = lo+RNG.get().nextInt(hi-lo-i);
			//make sure it is distinct
			for(int j = 0; j < i;j++){
				if(sample[i] >= sample[j]){
					sample[i]++;
				}
			}
		}
		return sample;
	}

	@Override
	public int[] sample() {
		int[] result = sample_unfiltered();
		while(filter != null && !filter.include(result)){
			result = sample_unfiltered();
		}
		return result;
	}

	@Override
	public int getDimensionality() {
		return k;
	}

	@Override
	public IteratorNH getIterator() {
		return filter == null? new CombinationIterator() : new FilteredIterator(new CombinationIterator(),filter);
	}

	@Override
	public IteratorNH getRandomIterator() {
		int[] init = sample_unfiltered();
		Arrays.sort(init);
		return filter == null? new CombinationIterator(init) : new FilteredIterator(new CombinationIterator(init),filter);
	}
	
	class CombinationIterator extends IteratorNH{

		int[] current;
		int[] init;
		boolean done;
		
		CombinationIterator(){
			init = new int[k];
			current = new int[k];
			for(int i = 0; i < k;i++){
				current[i] = lo+i;
				init[i] = lo+i;
			}
			if(k > hi-lo){
				done = true;
			}
		}
		
		CombinationIterator(int[] init){
			current = init;
			this.init = new int[k];
			System.arraycopy(init, 0, this.init, 0, k);
		}
		
		@Override
		public boolean hasNext() {
			return !done;
		}

		@Override
		public int[] next() {
			int[] result = new int[k];
			System.arraycopy(current, 0, result, 0, k);
			//increment 
			for(int i = k-1; i >= 0;i--){
				if(current[i] < hi-k+i){
					current[i]++;
					/*
					if(i == k-1){
						System.out.println(current[i]);
					}
					*/
					for(int j = i+1; j < k;j++){
						current[j] = current[i]+j-i;
					}
					break;
				}else if(i == 0){
					//wrap around
					for(int j = 0; j < k;j++){
						current[j] = lo+j;
					}
				}
			}
			
			if(Arrays.equals(current, init)){
				done = true;
			}
			return result;
		}
		
	}
}
