package hfu.heuristics.modifiers.nh;

import java.util.Arrays;

import hfu.BenchmarkInfo;
import hfu.RNG;
import hfu.heuristics.modifiers.nh.filter.Filter;

public class VariationNH<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH{

	RangeNH<P> nh;
	int lo;
	int hi;
	Filter filter;
	int k;
	
	
	public VariationNH(RangeNH<P> nh, int k, P instance) {
		super(instance);
		this.nh = nh;
		this.k = k;
		this.lo = nh.getLow();
		this.hi = nh.getHigh();
	}
	
	public VariationNH(RangeNH<P> nh, int k, Filter filter, P instance) {
		this(nh,k,instance);
		this.filter = filter;
	}
	
	private int[] sample_unfiltered(){
		int[] sample = new int[k];
		for(int i = 0; i < k; i++){
			sample[i] = lo+RNG.get().nextInt(hi-lo);
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
		return filter == null? new VariationIterator() : new FilteredIterator(new VariationIterator(),filter);
	}

	@Override
	public IteratorNH getRandomIterator() {
		int[] init = sample_unfiltered();
		return filter == null? new VariationIterator(init) : new FilteredIterator(new VariationIterator(init),filter);
	}
	
	class VariationIterator extends IteratorNH{
		int[] current;
		int[] init;
		boolean done;
		
		VariationIterator(){
			current = new int[k];
			init = new int[k];
			for(int i = 0; i < k;i++){
				init[i] = 0;
				current[i] = 0;
			}
		}
		
		VariationIterator(int[] init){
			this.init = init;
			current = new int[k];
			System.arraycopy(init, 0, current, 0, init.length);
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
			for(int i = 0; i < k;i++){
				if(current[i] < hi-1){
					current[i]++;
					break;
				}else{
					current[i] = lo;
				}
			}
			if(Arrays.equals(current, init)){
				done = true;
			}
			return result;
		}
		
	}

}
