package hfu;


abstract public class BasicSolution<P extends BenchmarkInfo> {
	private double e;
	protected P instance;
	
	abstract public boolean isEqualTo(BasicSolution<P> other);
    abstract public BasicSolution<P> deepCopy();
    abstract public String toText();
    abstract protected double evaluateFunctionValue();
    abstract public boolean isPartial();
    abstract public boolean isEmpty();
    
    @Override
    public String toString(){
    	return toText();
    }
    
    protected BasicSolution(P instance){
    	e = -1;
    	this.instance = instance;
    }
    
    protected BasicSolution(BasicSolution<P> other){
    	e = other.e;
    	instance = other.instance;
    }
    
	public double getFunctionValue(){
		if(e == -1){
			//not yet computed (by a previous call or delta-evaluation) => compute & store
			e = evaluateFunctionValue();
		}
		return e;
	}
	
	protected void setFunctionValue(double e){
		this.e = e;
	}
	
}
