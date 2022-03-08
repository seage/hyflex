package FlowShop;

class Solution implements Cloneable{
	int[] permutation;
	double Cmax;
	Solution(int[] permutation, double Cmax){
		this.permutation = permutation;
		this.Cmax = Cmax;
	}
	public Solution clone(){
		int[] newPermutation = permutation.clone();
		return new Solution(newPermutation, Cmax);
	}
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("Cmax = "+this.Cmax+"\n");
		for(int i = 0; i < permutation.length; i++){
			builder.append(" "+permutation[i]);
		}
		return builder.toString();
	}
}