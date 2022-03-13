package hfu.heuristics;

import hfu.BenchmarkInfo;
import hfu.Parameters;

abstract public class Heuristic<P extends BenchmarkInfo>{

	protected Parameters params;

	public abstract void init(P instance);
	
	public void init(P instance, Parameters params){
		this.params = params;
		init(instance);
	}

}
