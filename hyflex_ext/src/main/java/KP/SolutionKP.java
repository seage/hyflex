package KP;

import java.util.Arrays;

import hfu.BasicSolution;
import hfu.heuristics.modifiers.nh.CompositeNH;
import hfu.heuristics.modifiers.nh.RangeNH;
import hfu.heuristics.modifiers.nh.filter.Filter;

public class SolutionKP extends BasicSolution<InfoKP>{
	boolean[] knapsack;
	private int packed;
	private int packed_weight;
	private int packed_profit;

	public SolutionKP(InfoKP instance) {
		super(instance);
		knapsack = new boolean[instance.getNitems()];
		packed = 0;
		packed_weight = 0;
		packed_profit = 0;
	}
	
	protected SolutionKP(SolutionKP other) {
		super(other);
		knapsack = new boolean[other.knapsack.length];
		System.arraycopy(other.knapsack, 0, knapsack, 0, knapsack.length);
		packed = other.packed;
		packed_weight = other.packed_weight;
		packed_profit = other.packed_profit;
	}

	@Override
	public boolean isEqualTo(BasicSolution<InfoKP> other) {
		SolutionKP other_ks = (SolutionKP) other;
		if(packed == other_ks.packed && packed_weight == other_ks.packed_weight){
			return Arrays.equals(knapsack, other_ks.knapsack);
		}
		return false;
	}

	@Override
	public BasicSolution<InfoKP> deepCopy() {
		return new SolutionKP(this);
	}

	@Override
	public String toText() {
		String result = "knapsack: ";
		for(int i = 0; i < knapsack.length;i++){
			if(knapsack[i]){
				result += (i+1)+" ";
			}
		}
		return result + " ("+getFunctionValue()+")";
	}

	@Override
	protected double evaluateFunctionValue() {
		//value of unpacked items
		return /*instance.getTotalProfit()*/-packed_profit;
	}

	@Override
	public boolean isPartial() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
	
	public boolean insert(int i){
		//attempts to insert unpacked item i
		if(fits(i)){
			//insert
			knapsack[i] = true;
			packed++;
			packed_weight += instance.getWeight(i);
			packed_profit += instance.getProfit(i);
			setFunctionValue(evaluateFunctionValue());
			return true;
		}
		return false;
	}
	
	public boolean swap(int i, int j){
		//attempts to swap packed item i for unpacked item j
		int w_diff = instance.getWeight(j)-instance.getWeight(i);
		if(getRemainingCapacity() >= w_diff){
			//swap
			knapsack[i] = false;
			knapsack[j] = true;
			packed_weight += w_diff;
			packed_profit += instance.getProfit(j)-instance.getProfit(i);
			setFunctionValue(evaluateFunctionValue());
			return true;
		}
		return false;
	}
	
	public void remove(int i){
		//remove packed item i
		knapsack[i] = false;
		packed--;
		packed_weight -= instance.getWeight(i);
		packed_profit -= instance.getProfit(i);
		setFunctionValue(evaluateFunctionValue());
	}
	
	public void intersect(SolutionKP other){
		//take the intersection of both knapsacks
		boolean[] other_knapsack = other.knapsack;
		for(int i = 0; i < knapsack.length;i++){
			if(knapsack[i] && !other_knapsack[i]){
				//exclude the item
				remove(i);
			}
		}
	}
	
	public int getRemainingCapacity(){
		return instance.getCapacity()-packed_weight;
	}
	
	public boolean fits(int i){
		return instance.getWeight(i) <= getRemainingCapacity();
	}
	
	public int getPacked(){
		return packed;
	}
	
	public int getPackedWeight(){
		return packed_weight;
	}
	
	public int getPackedProfit(){
		return packed_profit;
	}
	
	public static class KnapSackNH extends RangeNH<InfoKP>{

		public KnapSackNH(InfoKP instance, SolutionKP c) {
			super(0, instance.getNitems(), new IsPacked(c), instance);
		}
		
		static class IsPacked implements Filter{

			SolutionKP c;
			
			IsPacked(SolutionKP c){
				this.c = c;
			}
			
			@Override
			public boolean include(int[] param) {
				return c.knapsack[param[0]];
			}
			
		}

		
	}
	
	public static class UnpackedNH extends RangeNH<InfoKP>{

		public UnpackedNH(InfoKP instance, SolutionKP c) {
			super(0, instance.getNitems(), new IsUnPacked(c), instance);
		}
		
		static class IsUnPacked implements Filter{

			SolutionKP c;
			
			IsUnPacked(SolutionKP c){
				this.c = c;
			}
			
			@Override
			public boolean include(int[] param) {
				return !c.knapsack[param[0]];
			}
			
		}
		
	}
	
	public static class UnionNH extends RangeNH<InfoKP>{

		public UnionNH(InfoKP instance, SolutionKP c, SolutionKP c1, SolutionKP c2) {
			super(0, instance.getNitems(), new IsUnPackedUnion(c,c1,c2), instance);
		}
		
		static class IsUnPackedUnion implements Filter{
			SolutionKP c;
			SolutionKP c1;
			SolutionKP c2;
			
			IsUnPackedUnion(SolutionKP c, SolutionKP c1, SolutionKP c2){
				this.c = c;
				this.c1 = c1;
				this.c2 = c2;
			}
			
			@Override
			public boolean include(int[] param) {
				return !c.knapsack[param[0]] && (c1.knapsack[param[0]] || c2.knapsack[param[0]]);
			}
			
		}
		
	}
	
	public static class SwapNH extends CompositeNH<InfoKP>{

		public SwapNH(InfoKP instance, SolutionKP c) {
			super(instance, new KnapSackNH(instance,c),new UnpackedNH(instance,c));
		}
		
	}

}
