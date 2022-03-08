package MAC.heuristics;

import MAC.InfoMAC;
import MAC.SolutionMAC;
import MAC.modifiers.Insert;
import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.RNG;
import hfu.heuristics.ConstructionHeuristic;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.selector.SelectBest;

public class RandomizedNEH extends ConstructionHeuristic<SolutionMAC, InfoMAC> {
  public void init(InfoMAC instance) {
    super.init((BenchmarkInfo)instance);
  }
  
  private static void shuffleArray(int[] ar) {
    for (int i = ar.length - 1; i > 0; i--) {
      int index = RNG.get().nextInt(i + 1);
      int a = ar[index];
      ar[index] = ar[i];
      ar[i] = a;
    } 
  }
  
  public SolutionMAC apply() {
    int[] queue = new int[((InfoMAC)this.instance).getNvertices()];
    for (int i = 0; i < queue.length; i++)
      queue[i] = i; 
    shuffleArray(queue);
    SolutionMAC c = new SolutionMAC((InfoMAC)this.instance);
    SelectBest selectBest = new SelectBest(false);
    selectBest.init(this.instance, this.params);
    for (int j = 0; j < queue.length; j++) {
      Insert insertor = new Insert(queue[j]);
      insertor.init(this.instance);
      c = (SolutionMAC)(selectBest.select((BasicSolution)c, (Modifier)insertor, 1)).c_proposed;
    } 
    return c;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\MAC\heuristics\RandomizedNEH.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */