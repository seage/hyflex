package MAC.modifiers.nhs;

import java.util.ArrayList;

import MAC.InfoMAC;

import hfu.RNG;
import hfu.datastructures.AdjecencyList.Neighbor;
import hfu.heuristics.modifiers.nh.IterableNH;
import hfu.heuristics.modifiers.nh.IteratorNH;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.modifiers.nh.RangeNH;
import hfu.heuristics.modifiers.nh.SamplableNH;

public class SwapNeighboursNH  extends NeighbourHood<InfoMAC> implements IterableNH, SamplableNH{
	
	RangeNH<InfoMAC> rnh;

	public SwapNeighboursNH(InfoMAC instance) {
		super(instance);
		rnh = new RangeNH<InfoMAC>(0,instance.getNvertices(),instance);
	}
	
	@Override
	public IteratorNH getIterator() {
		return new NeighboursIterator();
	}


	@Override
	public int[] sample() {
		ArrayList<Neighbor> neighbours = null;
		int vi = 0;
		while(neighbours == null || neighbours.size() == 0){
			vi = rnh.sample()[0];
			neighbours = instance.getGraph().getNeighbors(vi);
		}
		int vj = neighbours.get(RNG.get().nextInt(neighbours.size())).getID();
		return new int[]{vi,vj};
	}


	@Override
	public int getDimensionality() {
		return 2;
	}
	
	class NeighboursIterator extends IteratorNH{
		
		IteratorNH vis;
		ArrayList<Neighbor> neighbours;
		int vi;
		int vj;
		boolean done;
		
		NeighboursIterator(){
			done = false;
			vis = rnh.getIterator();
			while(neighbours == null || neighbours.size() < 1){
				if(vis.hasNext()){
					vi = vis.next()[0];
					neighbours = instance.getGraph().getNeighbors(vi);
					vj = 0;
				}else{
					done = true;
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
			int[] result = new int[]{vi,vj};
			vj++;
			if(vj == neighbours.size()){
				neighbours = null;
				while(neighbours == null || neighbours.size() < 1){
					if(vis.hasNext()){
						vi = vis.next()[0];
						neighbours = instance.getGraph().getNeighbors(vi);
						vj = 0;
					}else{
						done = true;
						break;
					}
				}
			}
			return result;
		}
		
	}

	

}

