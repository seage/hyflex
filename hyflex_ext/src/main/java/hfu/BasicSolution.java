package hfu;

public abstract class BasicSolution<P extends BenchmarkInfo> {
  private double e;
  
  protected P instance;
  
  public abstract boolean isEqualTo(BasicSolution<P> paramBasicSolution);
  
  public abstract BasicSolution<P> deepCopy();
  
  public abstract String toText();
  
  protected abstract double evaluateFunctionValue();
  
  public abstract boolean isPartial();
  
  public abstract boolean isEmpty();
  
  public String toString() {
    return toText();
  }
  
  protected BasicSolution(P instance) {
    this.e = -1.0D;
    this.instance = instance;
  }
  
  protected BasicSolution(BasicSolution<P> other) {
    this.e = other.e;
    this.instance = other.instance;
  }
  
  public double getFunctionValue() {
    if (this.e == -1.0D)
      this.e = evaluateFunctionValue(); 
    return this.e;
  }
  
  protected void setFunctionValue(double e) {
    this.e = e;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\BasicSolution.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */