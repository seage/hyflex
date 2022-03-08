package travelingSalesmanProblem;

class TspSolution implements Cloneable{
	int[] permutation;
	double Cost;
	
	TspSolution(int[] permutation, double Cost){
		this.permutation = permutation;
		this.Cost = Cost;
	}
	public TspSolution clone(){
		int[] newPermutation = permutation.clone();
		return new TspSolution(newPermutation, Cost);
	}
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("Cost = "+this.Cost+"\n");
		for(int i = 0; i < permutation.length; i++){
			builder.append(" "+permutation[i]);
		}
		return builder.toString();
	}
}