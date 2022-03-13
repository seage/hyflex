package hfu.datastructures;

import hfu.datastructures.AdjecencyList.Neighbor;

import java.util.ArrayList;

public interface Graph {
	public void addEdge(int v1, int v2, int w);
	
	public int getNedges();
	
	public int getNvertices();
	
	public ArrayList<Neighbor> getNeighbors(int v);
}
