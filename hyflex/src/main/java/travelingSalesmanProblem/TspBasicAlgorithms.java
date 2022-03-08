package travelingSalesmanProblem;

import java.util.Arrays;
import java.util.Random;

/**
 * @author Antonio
 *
 */
public class TspBasicAlgorithms {

	private TspInstance instance;
	TspDataStructure ds;

	public TspBasicAlgorithms(TspInstance instance){
		this.instance = instance;
	}

	/**
	 * Generates a random permutation with n elements, starting at 0
	 * @param n
	 * @return
	 */
	public int[] generateRandomPermutation(int n, Random rng) {
		int[] randomPermutation = new int[n];
		for (int i = 0; i < n; i++)
			randomPermutation[i] = i;
		shufflePermutation(randomPermutation, rng);
		return randomPermutation;
	}

	public void shufflePermutation(int[] array, Random rng) {
		int n = array.length; // The number of items left to shuffle (loop
		// invariant).
		while (n > 1) {
			int k = rng.nextInt(n); // 0 <= k < n.
			--n; // n is now the last pertinent index;
			int temp = array[n]; // swap array[n] with array[k].
			array[n] = array[k];
			array[k] = temp;
		}
	}

	/**
	 * Returns the cost of the tour represented by the permutation. 
	 * Note that the tour may be partial, i.e. not including all cities. 
	 * @param permutation
	 * @return cost of the tour represented by the given permutation
	 */
	public double computeCost(int[] permutation){	
		double sum = 0;
		for(int i = 1; i < permutation.length; i++)
			sum += instance.getDistance(permutation[i-1], permutation[i]);	
		sum += instance.getDistance(permutation[0], permutation[permutation.length-1]);
		return sum;
	}

	public double incrementalInsertionCost(int[] tour, double initialCost, int index1, int index2){
		return initialCost + reinsertionCost(tour, index1, index2);
	}
	public double incrementalCostFlip(int[] tour, double initialCost, int index1, int index2){
		return initialCost + flipCost(tour, index1, index2);
	}	
	public double incrementalCostSwap(int[] tour, double initialCost, int index1, int index2){
		return initialCost + swapCost(tour, index1, index2);
	}	
	public double reinsertionCost(int[] tour, int index1, int index2){
		int city1 = tour[index1]; int city2 = tour[index2];		
		int prev1, next1, prev2;

		if(index1 == 0)		prev1 = tour[instance.numbCities - 1];
		else				prev1 = tour[index1 - 1];
		if(index1 == instance.numbCities - 1)		next1 = tour[0];
		else										next1 = tour[index1 + 1];		
		if(index2 == 0)		prev2 = tour[instance.numbCities - 1];
		else				prev2 = tour[index2 - 1];

		double currentCost = instance.getDistance(city1, prev1) +
		instance.getDistance(city1, next1) +
		instance.getDistance(city2, prev2);
		double newCost 	   = instance.getDistance(prev2, city1) +
		instance.getDistance(city1, city2) +
		instance.getDistance(prev1, next1);

		return newCost - currentCost;

	}	
	public double insertionCost(int[] tour, int city1, int index){
		if(index == 0 || index == tour.length)
			return instance.getDistance(city1, tour[0]) + 
			instance.getDistance(city1, tour[tour.length - 1]);

		return instance.getDistance(city1, tour[index - 1])+
		instance.getDistance(city1, tour[index]);
	}		
	public double flipCost(int[] tour, int index1, int index2){
		int city1, city2, prev1, next2;

		city1 = tour[index1]; city2 = tour[index2];		
		if(index1 == 0)		prev1 = instance.numbCities - 1;
		else 				prev1 = tour[index1 - 1];	
		if(index2 == instance.numbCities - 1)	next2 = 0;
		else									next2 = tour[index2 + 1];

		if(prev1 == city2 || next2 == city1) return 0;

		double currentCost = instance.getDistance(city1, prev1) + 
		instance.getDistance(city2, next2);		
		double newCost     = instance.getDistance(city1, next2) +
		instance.getDistance(city2, prev1);

		return newCost - currentCost;		
	}
	public double flipCost(TspDataStructure ds, int city1, int city2){
		int prev1 = ds.prev(city1);
		int next2 = ds.next(city2);
		if(prev1 == city2 || next2 == city1) return 0;

		double currentCost = instance.getDistance(city1, prev1) + 
		instance.getDistance(city2, next2);

		double newCost     = instance.getDistance(city1, next2) +
		instance.getDistance(city2, prev1);

		return newCost - currentCost;
	}	
	public double swapCost(int[] tour, int index1, int index2){
		int city1, city2, prev1, prev2, next1, next2;		
		city1 = tour[index1]; city2 = tour[index2];

		if(index1 == 0) 			prev1 = instance.numbCities - 1;
		else            			prev1 = tour[index1 - 1];	
		if(index2 == 0)				prev2 = instance.numbCities - 1;
		else     					prev2 = tour[index2 - 1];
		if(index1 == instance.numbCities - 1)	next1 = 0;
		else									next1 = tour[index1 + 1];
		if(index2 == instance.numbCities - 1)	next2 = 0;
		else 									next2 = tour[index2 + 1];

		double currentCost = instance.getDistance(prev1, city1) +
		instance.getDistance(city1, next1) +
		instance.getDistance(prev2, city2) +
		instance.getDistance(city2, next2);

		double newCost     = instance.getDistance(prev2, city1) +
		instance.getDistance(city1, next2) +
		instance.getDistance(prev1, city2) +
		instance.getDistance(city2, next1);

		return newCost - currentCost;
	}		

	//ALGORITHMS

	public static int[] flip(int[] permutation, int a, int b){	
		if(a > b){
			int temp = a;
			a = b + 1;
			b = temp - 1;
		}				
		if(a == b)
			return (int[])permutation.clone();		
		int[] newPermutation = new int[permutation.length];
		System.arraycopy(permutation, 0, newPermutation, 0, a);		
		int count = a;
		for(int i = b; i >= a; i--, count++){
			newPermutation[count] = permutation[i];
		}
		System.arraycopy(permutation, b + 1, newPermutation, b+1, permutation.length - b - 1);
		return newPermutation;
	}	
	public int[] greedyInsertion(Double cost){
		int[] tour = {0, 1};
		int[] toInsert = new int[instance.numbCities - 2];
		for(int i = 2; i < instance.numbCities; i++)
			toInsert[i-2] = i;
		cost += 2*instance.getDistance(0, 1);
		return greedyInsertion(tour, toInsert, cost);
	}	
	public int[] greedyInsertion(int[] tour, int[] toInsert, Double cost){		
		double min = Double.MAX_VALUE;
		double temp;
		int index = 0;
		for(int i = 0; i < toInsert.length; i++){
			min = insertionCost(tour, toInsert[i], 0);
			index = 0;
			for(int j = 1; j <= tour.length; j++){
				temp = insertionCost(tour, toInsert[i], j);
				if(temp < min){
					min = temp;
					index = j;
				}	
			}	
			cost += min;
			tour = insert(tour, toInsert[i], index);
		}
		return tour;
	}
	public int[] greedyHeuristic(int startCity){
		int numbCities = instance.numbCities;
		int[] tour = new int[numbCities];
		boolean[] included = new boolean[numbCities];
		Arrays.fill(included, false);
		tour[0] = startCity;
		included[startCity] = true;
		double min;
		double temp;
		int city = 0;
		for(int i = 1; i < numbCities; i++){
			min = Double.MAX_VALUE;
			for(int j = 0; j < numbCities; j++){
				if(included[j]) continue;
				temp = instance.getDistance(tour[i-1], j);
				if(temp < min){
					min = temp;
					city = j;
				}
			}
			tour[i] = city;
			included[city] = true;
		}
		return tour;
	}	

	// LOCAL SEARCH	
	public int[] twoOptBestImprovement(int[] tour, TspInstance instance, int maxiterations){
		TspDataStructure ds = TspDataStructure.create(tour);
		int numbCities = instance.numbCities;
		double flipCost = 0.0;
		boolean improvement = true;

		int[][] nearest = instance.nearestCities; 
		int N = nearest[0].length;
		int completediterations = 0;
		while(improvement && (completediterations++ < maxiterations)){
			improvement = false;
			for(int i = 0; i < numbCities; i++){
				int bestMoveSoFar = -1;
				double bestCostSoFar = 0.0;				
				for(int j = 0; j < N; j++){
					flipCost = flipCost(ds, i, nearest[i][j]);
					if(flipCost < bestCostSoFar && flipCost < 0.0){
						bestMoveSoFar = nearest[i][j];
						bestCostSoFar = flipCost;
					}
				}
				if(bestMoveSoFar != -1){
					ds.flip(i,bestMoveSoFar);
					improvement = true;
				}
			}
		}
		return ds.returnTour(tour[0]);		
	}
	public int[] twoOptFirstImprovement(int[] tour, TspInstance instance, int maxiterations){
		TspDataStructure ds = TspDataStructure.create(tour);
		int numbCities = instance.numbCities;
		double flipCost = 0.0;
		boolean improvement = true;

		int[][] nearest = instance.nearestCities; 
		int N = nearest[0].length;
		
		int completediterations = 0;
		while(improvement && (completediterations++ < maxiterations)){
			improvement = false;
			for(int i = 0; i < numbCities; i++){
				for(int j = 0; j < N; j++){
					flipCost = flipCost(ds, i, nearest[i][j]);
					if(flipCost < 0.0){
						ds.flip(i,nearest[i][j]);		
						improvement = true;
					}
				}
			}
		}
		return ds.returnTour(tour[0]);		
	}
	public int[] threeOpt(int[] tour, TspInstance instance, int maxiterations){		
		int numbCities = instance.numbCities;
		TspDataStructure ds = TspDataStructure.create(tour);
		int[][] nearest = instance.nearestCities;
		int N = nearest[0].length;
		double cost;
		boolean localImprovment = false;
		boolean globalImprovement = true;
		int completediterations = 0;
		while(globalImprovement == true && (completediterations++ < maxiterations)){
			globalImprovement = false;
			for(int i = 0; i < numbCities; i++){//iterate all of the cities
				//System.out.println("city " + i);
				cost = 0;
				localImprovment = false;
				int iPrev = ds.prev(i);//the city previous to i

				for(int j2 = 0; j2 < N; j2++){//iterate nearest cities to i
					//System.out.println("nearest " + j2);
					int j = nearest[i][j2];

					if(j == iPrev || j == ds.prev(iPrev)) //if the nearest is one of the two preceeding ones
						continue;

					cost = flipCost(ds, i, j);
					ds.flip(i, j);//flip and get cost

					int jNext = ds.next(j);

					//loop the 8 cities nearest to jNext, swap them if better cost
					for(int k = 0; k < N; k++){
						//System.out.println("nearest next1" + k);
						int nextCity = nearest[jNext][k];
						if(ds.sequence(iPrev, nextCity, jNext) || nextCity == iPrev )
							continue;

						if(cost + flipCost(ds, jNext, nextCity) < 0){
							ds.flip(jNext, nextCity);
							localImprovment = true;
							globalImprovement = true;
							break;														
						}
					}
					if(localImprovment)
						break;

					//loop the 8 cities nearest to iPrev, swap them if better cost
					for(int k = 0; k < N; k++){
						//System.out.println("nearest next2" + k);
						int prevCity = nearest[iPrev][k];
						if(ds.sequence(iPrev, prevCity, jNext) || prevCity == jNext)
							continue;

						if(cost + flipCost(ds, iPrev, prevCity) < 0){
							ds.flip(iPrev, prevCity);
							localImprovment = true;
							globalImprovement = true;
							break;														
						}
					}

					if(localImprovment)
						break;

					ds.flip(j,i);
				}

			}
		}
		return ds.returnTour(tour[0]);				
	}


	//UTILITIES


	public int[] inversePermutation(int[] permutation){
		int[] inv = new int[permutation.length];
		for(int i = 0; i < permutation.length; i++){
			inv[permutation[i]] = i;
		}
		return inv;
	}
	public int[] insert(int[] initialTour, int x, int index){
		int[] newTour = new int[initialTour.length + 1];
		System.arraycopy(initialTour, 0, newTour, 0, index);
		newTour[index] = x;
		if(index < initialTour.length)
			System.arraycopy(initialTour, index, newTour, index + 1, initialTour.length - index);
		return newTour;
	}
	public boolean verifyPermutation(int[] p, int n){
		if(p.length != n) return false;
		boolean[] included = new boolean[n];
		Arrays.fill(included, false);
		for(int i = 0; i < n; i++){
			if(p[i] < 0 || p[i] >= n)
				return false;
			if(included[p[i]])
				return false;
			included[p[i]] = true;
		}
		for(int i = 0; i < n; i++){
			if(!included[i])
				return false;
		}		


		return true;
	}	
	public String tourToString(int[] tour){
		StringBuilder stb = new StringBuilder();
		for(int i = 0; i < tour.length; i++){
			stb.append(tour[i]+" ");
		}
		return stb.toString();
	}	


	//Some testing	
	//	public static void main (String[] args){		
	//		//int[] tour = {0,1,2,3,4,5,6,7,8,9,10};
	//	//	int[] newTour = TspBasicAlgorithms.flip(tour, 5, 4);
	//	//	System.out.println(Arrays.toString(newTour));
	//
	//		int instanceNumber = 28;
	//		TspInstance instance = new TspInstance(instanceNumber);
	//		//instance.calculateNearest(25);
	//		TspBasicAlgorithms algorithms = new TspBasicAlgorithms(instance);
	//		Double cost = 0.0;
	//		int[] greedytour = algorithms.greedyInsertion(cost);
	//	//	System.out.println(algorithms.tourToString(greedytour));
	//		System.out.println(algorithms.computeCost(greedytour));
	//		
	//		int[] permutation = algorithms.generateRandomPermutation(instance.numbCities, new Random());
	//		System.out.println(algorithms.computeCost(permutation));
	//		System.out.println(algorithms.verifyPermutation(permutation, instance.numbCities));
	//		
	//		int[] tour2 = algorithms.greedyHeuristic(0);
	//		int[] tour3 = algorithms.threeOpt(permutation, instance);
	//		System.out.println(algorithms.computeCost(tour3));
	//				
	//		
	//	}





}
