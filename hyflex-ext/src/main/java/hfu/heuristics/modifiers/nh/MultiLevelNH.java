package hfu.heuristics.modifiers.nh;

import hfu.BenchmarkInfo;
import hfu.RNG;

abstract public class MultiLevelNH<P extends BenchmarkInfo> extends NeighbourHood<P> implements RandomIterable, SamplableNH{

	int nlevels;
	
	public MultiLevelNH(int nlevels, P instance) {
		super(instance);
		this.nlevels = nlevels;
	}
	
	abstract public RangeNH<P> getNeighbourhood(int[] p, int level);
	
	@Override
	public int[] sample() {
		int[] result = new int[nlevels];
		for(int i = 0; i < nlevels;i++){
			result[i] = getNeighbourhood(result,i).sample()[0];
		}
		return result;
	}
	
	@Override
	public IteratorNH getIterator() {
		return new MultiLevelIterator();
	}

	
	@Override
	public IteratorNH getRandomIterator() {
		return new MultiLevelIterator(sample());
	}

	@Override
	public int getDimensionality() {
		return nlevels;
	}
	
	class MultiLevelIterator extends IteratorNH{
		IteratorNH[] its;
		int[] current;
		boolean done = false;

		MultiLevelIterator(){
			its = new IteratorNH[nlevels];
			current = new int[nlevels];
			for(int i = 0; i < nlevels;i++){
				RangeNH<P> nh = getNeighbourhood(current,i);
				its[i] = nh.getIterator();
				if(its[i].hasNext()){
					current[i] = its[i].next()[0];
				}else if(i == 0){
					done = true;
					break;
				}else{
					//backtrack
					i = i-2;
				}
			}
		}
		
		MultiLevelIterator(int[] init){
			its = new IteratorNH[nlevels];
			current = new int[nlevels];
			for(int i = 0; i < nlevels;i++){
				RangeNH<P> nh = getNeighbourhood(current,i);
				its[i] = nh.getIterator(init[i]);
				if(its[i].hasNext()){
					current[i] = its[i].next()[0];
				}else{
					done = true;
					break;
				}
			}
		}
		
		public boolean hasNext() {
			return !done;
		}

		@Override
		public int[] next() {
			int[] result = new int[nlevels];
			System.arraycopy(current, 0, result, 0, nlevels);
			for(int i = nlevels-1; i >= 0;i--){
				if(its[i].hasNext()){
					boolean next = true;
					current[i] = its[i].next()[0];
					for(int j = i+1;j < nlevels;j++){
						its[j] = getNeighbourhood(current,j).getIterator();
						if(its[j].hasNext()){
							current[j] = its[j].next()[0];
						}else if(j == i+1){
							//backtrack to previous level
							i++;
							next = false;
							break;
						}else{
							//backtrack in current level
							j = j-2;
						}
					}
					if(next){
						break;
					}
				}else if(i == 0){
					done = true;
				}
			}
			return result;
		}
		
	}

}
