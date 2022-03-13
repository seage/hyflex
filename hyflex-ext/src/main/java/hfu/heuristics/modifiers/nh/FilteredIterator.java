package hfu.heuristics.modifiers.nh;

import hfu.heuristics.modifiers.nh.filter.Filter;

public class FilteredIterator extends IteratorNH{
	
	IteratorNH it;
	Filter filter;
	int[] current;
	boolean done;
	
	public FilteredIterator(IteratorNH it, Filter filter){
		this.it = it;
		this.filter = filter;
		done = true;
		while(it.hasNext()){
			current = it.next();
			if(filter.include(current)){
				done = false;
				break;
			}
		}
	}

	@Override
	public boolean hasNext() {
		return !done;
	}

	@Override
	public int[] next() {
		int[] result = null;
		if(current != null){
			result = new int[current.length];
			System.arraycopy(current, 0, result, 0, current.length);
			done = true;
			while(it.hasNext()){
				current = it.next();
				if(filter.include(current)){
					done = false;
					break;
				}
			}
		}
		return result;
	}

}
