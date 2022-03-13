package hfu;

public interface Parser<P extends BenchmarkInfo> {
	public P parse(String file); 
}
