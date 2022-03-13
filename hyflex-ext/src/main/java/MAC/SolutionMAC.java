package MAC;

import java.util.ArrayList;
import java.util.Arrays;

import hfu.BasicSolution;
import hfu.RNG;
import hfu.datastructures.AdjecencyList.Neighbor;
import hfu.datastructures.Graph;
import hfu.heuristics.modifiers.nh.CompositeNH;
import hfu.heuristics.modifiers.nh.IteratorNH;
import hfu.heuristics.modifiers.nh.RangeNH;
import hfu.heuristics.modifiers.nh.filter.Filter;

public class SolutionMAC extends BasicSolution<InfoMAC>{
	
	private int[] assignment; //0 not assigned, 1 partition 1, 2 partition 2
	private int assigned1;
	private int assigned2;

	public SolutionMAC(InfoMAC instance) {
		super(instance);
		assigned1 = 0;
		assigned2 = 0;
		assignment = new int[instance.getNvertices()];
	}
	
	protected SolutionMAC(SolutionMAC other) {
		super(other);
		assigned1 = other.assigned1;
		assigned2 = other.assigned2;
		assignment = new int[instance.getNvertices()];
		System.arraycopy(other.assignment, 0, assignment, 0, assignment.length);
	}

	@Override
	public boolean isEqualTo(BasicSolution<InfoMAC> other) {
		SolutionMAC o = ((SolutionMAC) other);
		return assigned1 == o.assigned1 && assigned2 == o.assigned2 && Arrays.equals(assignment, o.assignment); 
	}

	@Override
	public BasicSolution<InfoMAC> deepCopy() {
		return new SolutionMAC(this);
	}

	@Override
	public String toText() {
		String result = "partition 1: ";
		for(int i = 0; i < assignment.length;i++){
			if(assignment[i] == 1){
				result += (i+1)+" ";
			}
		}
		result += System.lineSeparator()+"partition 2: ";
		for(int i = 0; i < assignment.length;i++){
			if(assignment[i] == 2){
				result += (i+1)+" ";
			}
		}
		return result+System.lineSeparator()+" ("+getFunctionValue()+")";
	}

	@Override
	protected double evaluateFunctionValue() {
		Graph G = instance.getGraph();
		int e = 0;
		//for all vertices in cut (1)
		for(int v1 = 0; v1 < assignment.length;v1++){
			if(assignment[v1] == 1){
				e += evaluateVertice(v1, G);
			}
		}
		return e;
	}
	
	private int evaluateVertice(int v, Graph G){
		//sum weights of neighbors in the other partition
		int e = 0;
		if(assignment[v] != 0){
			int other = 3-assignment[v];
			ArrayList<Neighbor> neighbors = G.getNeighbors(v);
			for(int vi = 0; vi < neighbors.size();vi++){
				Neighbor nb = neighbors.get(vi);
				if(assignment[nb.getID()] == other){
					e += nb.getW();
				}
			}
		}
		return -e;
	}

	@Override
	public boolean isPartial() {
		return assigned1 + assigned2 < instance.getNvertices();
	}

	@Override
	public boolean isEmpty() {
		return (assigned1 + assigned2) == 0 ;
	}
	
	public void insert(int v, int p){
		//insert vertice v in partition p
		double e = getFunctionValue();
		assignment[v] = p;
		if(p == 1){
			assigned1++;
		}else{
			assigned2++;
		}
		e += evaluateVertice(v,instance.getGraph());
		/*
		double teste = evaluateFunctionValue();
		if(teste != e){
			System.out.println(toText());
		}
		*/
		setFunctionValue(e);
	}
	
	public void swap(int v){
		//change partition 
		double e = getFunctionValue();
		if(assignment[v] == 1){
			assigned1--;
			assigned2++;
		}else{
			assigned2--;
			assigned1++;
		}
		e -= evaluateVertice(v,instance.getGraph());
		assignment[v] = 3-assignment[v];
		e += evaluateVertice(v,instance.getGraph());
		/*
		double teste = evaluateFunctionValue();
		if(teste != e){
			System.out.println(toText());
		}
		*/
		setFunctionValue(e);
	}
	
	public void remove(int v){
		//remove vertice v
		double e = getFunctionValue();
		if(assignment[v] == 0){
			return;
		}else if(assignment[v] == 1){
			assigned1--;
		}else{
			assigned2--;
		}
		e -= evaluateVertice(v,instance.getGraph());
		assignment[v] = 0;
		/*
		double teste = evaluateFunctionValue();
		if(teste != e){
			System.out.println(toText());
		}
		*/
		setFunctionValue(e);
	}
	
	public void removeRadial(int v){
		//remove vertice v
		remove(v);
		// and all neighbors
		ArrayList<Neighbor> neighbors = instance.getGraph().getNeighbors(v);
		for(int i = 0; i < neighbors.size();i++){
			remove(neighbors.get(i).getID());
		}
	}
	
	public void swapNeighbours(int vi, int vj){
		//swap vertice vi
		swap(vi);
		//and neighbor vj
		swap(vj);
	}
	
	private void compute_assigned(){
		assigned1 = 0;
		assigned2 = 0;
		for(int i = 0; i < assignment.length;i++){
			if(assignment[i] == 1){
				assigned1++;
			}else if(assignment[i] == 2){
				assigned2++;
			}
		}
	}
	
	private int nmatch(SolutionMAC c2){
		int matches = 0;
		for(int i = 0; i < assignment.length;i++){
			if(assignment[i] == c2.assignment[i]){
				matches++;
			}
		}
		return matches;
	}
	
	private void swap_labels(){
		int temp = assigned1;
		assigned1 = assigned2;
		assigned2 = temp;
		for(int i = 0; i < assignment.length;i++){
			assignment[i] = 3-assignment[i];
		}
	}
	
	public void one_point_crossover(int pivot, SolutionMAC c2){
		if(2*nmatch(c2) < assignment.length){
			swap_labels();
		}
		//actual crossover
		System.arraycopy(c2.assignment, pivot, assignment, pivot, assignment.length-pivot);
		//recompute assigned
		compute_assigned();
		setFunctionValue(-1);
	}
	
	public void mp_crossover(SolutionMAC c2){
		//multi-parent crossover as proposed in "A Memetic Approach for the Max-Cut Problem" 
		assigned1 = 0;
		assigned2 = 0;
		int[] new_assignment = new int[assignment.length];
		//- determine cardinality of intersections of S11-S21, S11-S22, S12-S21, S12-s22
		int[][] c = new int[2][2];
		for(int i = 0; i < assignment.length;i++){
			c[assignment[i]-1][c2.assignment[i]-1]++;
		}
	    //- take pair with maximum cardinality
		int v1 = -1;
		int v2 = -1;
		int bestc = -1;
		for(int i = 0; i < 2;i++){
			for(int j = 0; j < 2;j++){
				if(c[i][j] > bestc){
					v1 = i+1;
					v2 = j+1;
					bestc = c[i][j];
				}
			}
		}
	    //- first set is the intersection of these 2 sets
		for(int i = 0; i < assignment.length;i++){
			if(assignment[i] == v1 && c2.assignment[i] == v2){
				new_assignment[i] = 1;
				assigned1++;
			}
		}
	    //- again determine cardinality of intersections of S11-S21, S11-S22, S12-S21, S12-s22, but this time ignoring those already assigned
		c = new int[2][2];
		for(int i = 0; i < assignment.length;i++){
			if(new_assignment[i] == 0){
				c[assignment[i]-1][c2.assignment[i]-1]++;
			}
		}
	    //- take pair with maximum cardinality
		v1 = -1;
		v2 = -1;
		bestc = -1;
		for(int i = 0; i < 2;i++){
			for(int j = 0; j < 2;j++){
				if(c[i][j] > bestc){
					v1 = i+1;
					v2 = j+1;
					bestc = c[i][j];
				}
			}
		}
	    //- second set is the intersection of these 2 sets
		for(int i = 0; i < assignment.length;i++){
			if(assignment[i] == v1 && c2.assignment[i] == v2){
				new_assignment[i] = 2;
				assigned2++;
			}
		}
	    //- unassigned vertices are inserted randomly
		for(int i = 0; i < assignment.length;i++){
			if(new_assignment[i] == 0){
				if(RNG.get().nextBoolean()){
					new_assignment[i] = 1;
					assigned1++;
				}else{
					new_assignment[i] = 2;
					assigned2++;
				}
			}
		}
		assignment = new_assignment;
		setFunctionValue(-1);
	}
	
	public int getNassigned(){
		return assigned1 + assigned2;
	}
	
	public boolean isAssigned(int v){
		return assignment[v] != 0;
	}
	
	static public class SwapNH extends RangeNH<InfoMAC>{

		public SwapNH(InfoMAC instance) {
			super(0, instance.getNvertices(), instance);
		}
		
	}
	
	static public class RemoveNH extends RangeNH<InfoMAC>{

		public RemoveNH(InfoMAC instance, SolutionMAC c) {
			super(0, instance.getNvertices(), new Assigned(c), instance);
		}
		
		static class Assigned implements Filter{
			SolutionMAC c;
			
			Assigned(SolutionMAC c){
				this.c = c;
			}
			
			@Override
			public boolean include(int[] param) {
				return c.isAssigned(param[0]);
			}
			
		}
	}
	
	
	static public class InsertNH extends CompositeNH<InfoMAC>{
		
		SolutionMAC c;
		
		public InsertNH(InfoMAC instance, SolutionMAC c, int v) {
			super(instance, new RangeNH<InfoMAC>(v,v+1,instance),new RangeNH<InfoMAC>(1,3,instance));
			this.c = c;
		}
		
		public InsertNH(InfoMAC instance, SolutionMAC c) {
			super(instance, 
				new RangeNH<InfoMAC>(0,instance.getNvertices(),new UnAssigned(c),instance),
				new RangeNH<InfoMAC>(1,3,instance)
			);
			this.c = c;
		}
		
		@Override
		public int[] sample(){
			IteratorNH it = getIterator();
			ArrayList<int[]> included = new ArrayList<int[]>(instance.getNvertices());
			while(it.hasNext()){
				int[] p = it.next();
				if(!c.isAssigned(p[0])){
					included.add(p);
				}
			}
			//sample
			return included.get(RNG.get().nextInt(included.size()));
		}
		
		static class UnAssigned implements Filter{
			SolutionMAC c;
			
			UnAssigned(SolutionMAC c){
				this.c = c;
			}
			
			@Override
			public boolean include(int[] param) {
				return !c.isAssigned(param[0]);
			}
			
		}
		

	}

}
