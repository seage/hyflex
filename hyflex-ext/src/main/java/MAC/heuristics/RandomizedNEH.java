package MAC.heuristics;

import MAC.modifiers.Insert;
import MAC.InfoMAC;
import MAC.SolutionMAC;
import MAC.SolutionMAC.InsertNH;
import hfu.RNG;
import hfu.heuristics.ConstructionHeuristic;
import hfu.heuristics.selector.SelectBest;
import hfu.heuristics.selector.Selector;

public class RandomizedNEH extends ConstructionHeuristic<SolutionMAC,InfoMAC>{

	@Override 
	public void init(InfoMAC instance){
		super.init(instance);
		
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
	
	@Override
	public SolutionMAC apply() {
		//generate a random order of jobs
		int[] queue = new int[instance.getNvertices()];
		for(int i = 0; i < queue.length;i++){
			queue[i] = i;
		}
		shuffleArray(queue);
		//insert each job, using greedy insertion
		SolutionMAC c = new SolutionMAC(instance);
		Selector<SolutionMAC,InfoMAC,InsertNH> greedy = new SelectBest<SolutionMAC,InfoMAC,InsertNH>(false);
		greedy.init(instance, params);
		for(int i = 0; i < queue.length;i++){
			Insert insertor = new Insert(queue[i]);
			insertor.init(instance);
			c = greedy.select(c, insertor,1).c_proposed;
		}
		return c;
	}

}
