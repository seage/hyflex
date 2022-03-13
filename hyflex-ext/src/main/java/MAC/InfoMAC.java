package MAC;

import hfu.BenchmarkInfo;
import hfu.datastructures.AdjecencyList;
import hfu.datastructures.Graph;

public class InfoMAC implements BenchmarkInfo {

	private AdjecencyList G;
	
	public InfoMAC(AdjecencyList G){
		this.G = G;
	}
	
	public int getNvertices(){
		return G.getNvertices();
	}
	
	public int getNedges(){
		return G.getNvertices();
	}
	
	public Graph getGraph(){
		return G;
	}
	

}
