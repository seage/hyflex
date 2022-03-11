package hfu.heuristics.modifiers.nh;

import java.util.Iterator;

abstract public class IteratorNH implements Iterator<int[]>{

	@Override
	public void remove() {
		//NOT SUPPORTED		
	}
	
	public static IteratorNH fromIterator(Iterator<Integer> it){
		return new SimpleIterator(it);
	}
	
	static class SimpleIterator extends IteratorNH{
		Iterator<Integer> it;
		
		SimpleIterator(Iterator<Integer> it){
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int[] next() {
			return new int[]{it.next()};
		}
		
		
	}


}
