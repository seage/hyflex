package hfu.heuristics.selector.eval;

public interface EvaluationFunction<T extends hfu.BasicSolution<P>, P extends hfu.BenchmarkInfo> {
  void init(P paramP);
  
  double evaluate(T paramT);
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\heuristics\selector\eval\EvaluationFunction.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */