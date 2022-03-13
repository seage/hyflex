package hfu.heuristics.modifiers.nh;

import hfu.BenchmarkInfo;
import hfu.RNG;
import hfu.heuristics.modifiers.nh.filter.Filter;

public class RangeNH<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH{

	int lo;
	int hi;
	Filter filter;
	
	public RangeNH(int lo, int hi, P instance) {
		super(instance);
		this.lo = lo;
		this.hi = hi;
	}
	
	public RangeNH(int lo, int hi, Filter filter, P instance) {
		this(lo,hi,instance);
		this.filter = filter;
	}

	@Override
	public int[] sample() {
		int[] result;
		if(hi <= lo){
			result = null;
		}else if(filter != null){
			//create an array of range
			int[] range = new int[hi-lo];
			for(int i = 0;i < hi-lo;i++){
				range[i] = i;
			}
			int max = hi-lo;
			int pick = RNG.get().nextInt(max);
			result = new int[]{lo+range[pick]};
			while(!filter.include(result) && max > 1){
				//exclude excluded
				max--;
				int temp = range[max];
				range[max] = range[pick];
				range[pick] = temp;
				pick = RNG.get().nextInt(max);
				result[0] = lo+range[pick];
			}
			if(max == 1 && !filter.include(result)){
				result = null;
			}
			/*
			result = new int[]{lo+RNG.get().nextInt(hi-lo)};
			while(!filter.include(result)){
				result[0]++;
				if(result[0] == hi){
					result[0] = lo;
				}
			}
			*/
		}else{
			result = new int[]{lo+RNG.get().nextInt(hi-lo)};
		}
		return result;
	}

	@Override
	public int getDimensionality() {
		return 1;
	}
	
	public int getUnFilteredSize() {
		return hi-lo;
	}

	@Override
	public IteratorNH getIterator() {
		return filter == null? new RangeIterator() : new FilteredIterator(new RangeIterator(),filter);
	}

	public IteratorNH getIterator(int init) {
		return filter == null? new RangeIterator(init) : new FilteredIterator(new RangeIterator(init),filter);
	}
	
	@Override
	public IteratorNH getRandomIterator() {
		int init = lo+RNG.get().nextInt(hi-lo);
		return getIterator(init);
	}
	
	public int getHigh(){
		return hi;
	}
	
	public int getLow(){
		return lo;
	}
	
	
	class RangeIterator extends IteratorNH{

		int init;
		int current;
		boolean first = true;
		
		RangeIterator(){
			init = lo;
			current = lo;
		}
		
		RangeIterator(int init){
			this.init = init;
			current = init;	
		}
		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return first || current != init;
		}

		@Override
		public int[] next() {
			first = false;
			int result = current;
			current++;
			if(current == hi){
				//wrap around
				current = lo;
			}
			return new int[]{result};
		}
		
	}

}
