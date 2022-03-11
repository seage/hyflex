package hfu.datastructures;

import java.util.ArrayList;


public class AdjecencyList implements Graph{
	ArrayList<ArrayList<Neighbor>> G;
	int nedges;
	
	public AdjecencyList(int n_vertices){
		G = new ArrayList<ArrayList<Neighbor>>(n_vertices);
		for(int i = 0; i < n_vertices;i++){
			G.add(new ArrayList<Neighbor>(1));
		}
		nedges = 0;
	}
	
	public void addEdge(int v1, int v2, int w){
		G.get(v1-1).add(new Neighbor(v2-1,w));
		G.get(v2-1).add(new Neighbor(v1-1,w));
		nedges++;
	}
	
	public int getNedges(){
		return nedges;
	}
	
	public int getNvertices(){
		return G.size();
	}
	
	public ArrayList<Neighbor> getNeighbors(int v){
		return G.get(v);
	}
	
	public class Neighbor{
		int w;
		int id;
		
		Neighbor(int id, int w){
			this.w = w;
			this.id = id;
		}
		
		public int getW(){
			return w;
		}
		
		public int getID(){
			return id;
		}
	}
}
