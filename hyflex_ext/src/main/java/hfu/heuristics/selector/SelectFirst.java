package hfu.heuristics.selector;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.nh.IteratorNH;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.modifiers.nh.RandomIterable;
import hfu.heuristics.selector.eval.EvaluationFunction;
import hfu.heuristics.selector.eval.ObjectiveFunction;

public class SelectFirst<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P> & RandomIterable> extends Selector<T, P, N> {
  EvaluationFunction<T, P> evalf;
  
  public SelectFirst() {
    this.evalf = (EvaluationFunction<T, P>)new ObjectiveFunction();
  }
  
  public SelectFirst(EvaluationFunction<T, P> evalf) {
    this.evalf = evalf;
  }
  
  public void init(P instance) {
    this.evalf.init((BenchmarkInfo)instance);
  }
  
  public Proposal<T, P> select(T c, Modifier<T, P, N> modifier, int max) {
    Proposal<T, P> proposal = new Proposal<>();
    if (modifier.isApplicable((BasicSolution)c)) {
      int dos = 1;
      double target_e = this.evalf.evaluate((BasicSolution)c);
      int n = 0;
      while (proposal.c_proposed == null && n < dos) {
        n++;
        proposal.c_proposed = searchFirst(c, c, target_e, modifier, n);
      } 
      proposal.nModifications = (proposal.c_proposed == null) ? 0 : n;
    } 
    return proposal;
  }
  
  private T searchFirst(T c, T c_original, double target_e, Modifier<T, P, N> modifier, int dos) {
    NeighbourHood neighbourHood = modifier.getNeightbourhood((BasicSolution)c);
    IteratorNH it = ((RandomIterable)neighbourHood).getRandomIterator();
    while (it.hasNext()) {
      BasicSolution basicSolution = modifier.apply(c.deepCopy(), (int[])it.next());
      if (dos > 1 && modifier.isApplicable(basicSolution)) {
        basicSolution = (BasicSolution)searchFirst((T)basicSolution, c, target_e, modifier, dos - 1);
        if (basicSolution != null)
          return (T)basicSolution; 
        continue;
      } 
      if (this.evalf.evaluate(basicSolution) < target_e && !c_original.isEqualTo(basicSolution))
        return (T)basicSolution; 
    } 
    return null;
  }
  
  public boolean usesDepthOfSearch() {
    return false;
  }
  
  public boolean usesIntensityOfMutation() {
    return false;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\selector\SelectFirst.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */