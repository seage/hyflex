package hfu.heuristics.selector;

import hfu.BasicSolution;
import hfu.BenchmarkInfo;
import hfu.heuristics.modifiers.Modifier;
import hfu.heuristics.modifiers.nh.IterableNH;
import hfu.heuristics.modifiers.nh.IteratorNH;
import hfu.heuristics.modifiers.nh.NeighbourHood;
import hfu.heuristics.selector.eval.EvaluationFunction;
import hfu.heuristics.selector.eval.ObjectiveFunction;

public class SelectBest<T extends BasicSolution<P>, P extends BenchmarkInfo, N extends NeighbourHood<P> & IterableNH> extends Selector<T, P, N> {
  EvaluationFunction<T, P> evalf;
  
  boolean strict;
  
  public SelectBest(boolean strict) {
    this.strict = strict;
    this.evalf = (EvaluationFunction<T, P>)new ObjectiveFunction();
  }
  
  public SelectBest(boolean strict, EvaluationFunction<T, P> evalf) {
    this.strict = strict;
    this.evalf = evalf;
  }
  
  public void init(P instance) {
    this.evalf.init((BenchmarkInfo)instance);
  }
  
  public Proposal<T, P> select(T c, Modifier<T, P, N> modifier, int max) {
    Proposal<T, P> proposal = new Proposal<>();
    if (modifier.isApplicable((BasicSolution)c)) {
      int dos = 1;
      Result r = searchBest(c, modifier, dos);
      proposal.c_proposed = r.c;
      proposal.nModifications = dos - r.depth + 1;
    } 
    return proposal;
  }
  
  private Result searchBest(T c, Modifier<T, P, N> modifier, int dos) {
    NeighbourHood neighbourHood = modifier.getNeightbourhood((BasicSolution)c);
    IteratorNH it = ((IterableNH)neighbourHood).getIterator();
    Result best = new Result(dos);
    while (it.hasNext()) {
      BasicSolution basicSolution = modifier.apply(c.deepCopy(), (int[])it.next());
      if (dos > 1 && modifier.isApplicable(basicSolution)) {
        Result r = searchBest(c, modifier, dos - 1);
        if (!this.strict) {
          double d = this.evalf.evaluate(basicSolution);
          if (r.e > d && !basicSolution.isEqualTo((BasicSolution)c)) {
            r.c = (T)basicSolution;
            r.e = d;
            r.depth = dos;
          } 
        } 
        if (r.e < best.e)
          best = r; 
        continue;
      } 
      double e_new = this.evalf.evaluate(basicSolution);
      if (e_new < best.e && !basicSolution.isEqualTo((BasicSolution)c))
        best = new Result((T)basicSolution, e_new, dos); 
    } 
    return best;
  }
  
  class Result {
    T c;
    
    double e;
    
    int depth;
    
    Result(int depth) {
      this.c = null;
      this.e = Double.MAX_VALUE;
      this.depth = depth;
    }
    
    Result(T c, double e, int depth) {
      this.c = c;
      this.e = e;
      this.depth = depth;
    }
  }
  
  public boolean usesDepthOfSearch() {
    return false;
  }
  
  public boolean usesIntensityOfMutation() {
    return false;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\selector\SelectBest.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */