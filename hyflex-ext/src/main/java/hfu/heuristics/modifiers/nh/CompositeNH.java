package hfu.heuristics.modifiers.nh;

import java.util.Arrays;

import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.nh.filter.Filter;

public class CompositeNH<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH{

	RangeNH<P>[] nhs;
	//int lo[];
	//int hi[];
	Filter filter;
	int k;
	
	
	@SafeVarargs
	public CompositeNH(P instance,RangeNH<P>... nhs) {
		super(instance);
		this.nhs = nhs;
		this.k = nhs.length;
		/*
		lo = new int[nhs.length];
		hi = new int[nhs.length];
		for(int i = 0; i < nhs.length;i++){
			lo[i] = nhs[i].getLow();
			hi[i] = nhs[i].getHigh();
		}
		*/
	}
	
	@SafeVarargs
	public CompositeNH(P instance, Filter filter, RangeNH<P>... nhs) {
		this(instance,nhs);
		this.filter = filter;
	}
	
	private int[] sample_unfiltered(){
		int[] sample = new int[nhs.length];
		for(int i = 0; i < nhs.length; i++){
			int[] param = nhs[i].sample();
			if(param != null){
				sample[i] = param[0];
			}else{
				return null;
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
		return nhs.length;
	}

	@Override
	public IteratorNH getIterator() {
		return filter == null? new CompositeIterator() : new FilteredIterator(new CompositeIterator(),filter);
	}

	@Override
	public IteratorNH getRandomIterator() {
		int[] init = sample_unfiltered();
		return filter == null? new CompositeIterator(init) : new FilteredIterator(new CompositeIterator(init),filter);
	}
	
	class CompositeIterator extends IteratorNH{
		IteratorNH[] its;
		int[] current;
		int[] init;
		boolean done;
		
		CompositeIterator(){
			current = new int[k];
			init = new int[k];
			its = new IteratorNH[k];
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
			if(init != null){
				current = new int[k];
				this.init = new int[k];
				its = new IteratorNH[k];
				for(int i = 0; i < nhs.length;i++){
					its[i] = nhs[i].getIterator(init[i]);
					if(its[i].hasNext()){
						this.init[i] = its[i].next()[0];
						current[i] = this.init[i];
					}else{
						done = true;
					}
				}
			}else{
				done = true;
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
				if(its[i].hasNext()){
					current[i] = its[i].next()[0];
					break;
				}else{
					its[i] = nhs[i].getIterator();
					current[i] = its[i].next()[0];
				}
			}
			if(Arrays.equals(current, init)){
				done = true;
			}
			return result;
		}
		
	}
}
