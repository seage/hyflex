package hfu;

public class BenchmarkInstance<P extends BenchmarkInfo>{
	String file;
	Parser<P> parser;
	
	public BenchmarkInstance(String file, Parser<P> parser){
		this.file = file;
		this.parser = parser;
	}
	
	P load(){
		return parser.parse(file);
	}
	
}