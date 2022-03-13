package hfu.heuristics.modifiers.nh;

import java.util.Arrays;

import hfu.BenchmarkInfo;
import hfu.RNG;
import hfu.heuristics.modifiers.nh.filter.Filter;

public class CompositeNH2<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH{

	RangeNH<P>[] nhs;
	int lo[];
	int hi[];
	Filter filter;
	int k;
	
	
	@SafeVarargs
	public CompositeNH2(P instance,RangeNH<P>... nhs) {
		super(instance);
		this.nhs = nhs;
		
		lo = new int[nhs.length];
		hi = new int[nhs.length];
		for(int i = 0; i < nhs.length;i++){
			lo[i] = nhs[i].getLow();
			hi[i] = nhs[i].getHigh();
		}
	}
	
	@SafeVarargs
	public CompositeNH2(P instance, Filter filter, RangeNH<P>... nhs) {
		this(instance,nhs);
		this.filter = filter;
	}
	
	private int[] sample_unfiltered(){
		int[] sample = new int[nhs.length];
		for(int i = 0; i < nhs.length; i++){
			sample[i] = nhs[i].sample()[0];
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
		return nhs.length;
	}

	@Override
	public IteratorNH getIterator() {
		return filter == null? new CompositeIterator() : new FilteredIterator(new CompositeIterator(),filter);
	}

	@Override
	public IteratorNH getRandomIterator() {
		int[] init = sample_unfiltered();
		Arrays.sort(init);
		return filter == null? new CompositeIterator(init) : new FilteredIterator(new CompositeIterator(init),filter);
	}
	
	class CompositeIterator extends IteratorNH{

		int[] current;
		int[] init;
		IteratorNH[] its;
		boolean done;
		
		CompositeIterator(){
			current = new int[k];
			init = new int[k];
			for(int i = 0; i < nhs.length;i++){
				its[i] = nhs[i].getIterator();
				if(its[i].hasNext()){
					init[i] = its[i].next()[0];
					current[i] = init[i];
				}else{
					done = true;
				}
			}
		}
		
		CompositeIterator(int[] init){
			current = new int[k];
			init = new int[k];
			for(int i = 0; i < nhs.length;i++){
				its[i] = nhs[i].getIterator(init[i]);
				if(its[i].hasNext()){
					this.init[i] = its[i].next()[0];
					current[i] = init[i];
				}else{
					done = true;
				}
			}
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
				if(current[i] < hi[i]-1){
					current[i]++;
					break;
				}else if(i < k-1){
					current[i] = lo[i];
				}else{
					System.arraycopy(lo, 0, current, 0, nhs.length);
				}
			}
			if(Arrays.equals(current, init)){
				done = true;
			}
			return result;
		}
		
	}
}
