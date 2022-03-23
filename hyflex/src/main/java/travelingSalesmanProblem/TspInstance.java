package travelingSalesmanProblem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;


public class TspInstance {
	public int numbCities; 
	double[][] coordinates;
	String name; //instance name 
	
	/* The following three are for efficiency purposes, keep the 
	 * information regarding the N nearest cities to each particular
	 * city. */
	int N = 8; 	//DO NOT CHANGE
	/* 
	 * In the following two i= 1,..., numbCities and 
	 * j = 1,...,N
	 * */
	int[][] nearestCities; // [i][j] is the j-th nearest city to city i
	double[][] D; 		   // [i][j] distance between cities i and j
	
	String[] instanceNames = {"pr299",   "pr439",   "rat575",   "u724",	
							  "rat783",   "pcb1173", "d1291",   "u2152",							  
							  "usa13509", "d18512",};
							         

	public static void saveNearest(int[][] nearest, String fileName){
		
		StringBuilder strb = new StringBuilder();
		int N = nearest.length;
		int n = nearest[0].length;
		
		for(int i = 0; i < N; i++) 
		{
			for(int j = 0; j < n - 1; j++){
				strb.append(nearest[i][j]+" ");
			}
			if(i < N-1)
				strb.append(nearest[i][n-1]+"\n");
			else
				strb.append(nearest[i][n-1]);
		}
		save(fileName, strb.toString());
	}
	
	public static void save(String fileName, String data) {
		try {
			FileWriter writer = new FileWriter(fileName);
			writer.write(data);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/**
	 * Calculate matrices with the N closest cities to each city
	 * and their corresponding distances.
	 * @param N
	 */
	public void calculateNearest(int N){
		nearestCities = new int[numbCities][N];	
		D = new double[numbCities][N];
		
		for(int i = 0; i < numbCities; i++){
			Arrays.fill(D[i], Integer.MAX_VALUE);
			for(int j = 0; j < numbCities; j++){
				if(i == j) continue;
				double cost = this.getDistance(i, j);
				int max = getMax(D[i]); 
				if(cost < D[i][max]){
					D[i][max] = cost;
					nearestCities[i][max] = j;
				}
			}
		}
	}
	
	public int getMax(double[] array){
		double max = - Double.MAX_VALUE;
		int maxIndex = 0;
		for(int i = 0; i < array.length; i++){
			if(array[i] > max){
				max = array[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	public boolean isNearest(int cityIndex, int candidate){
		for(int i = 0; i < nearestCities.length; i++){
			if(nearestCities[cityIndex][i] == candidate)
				return true;
		}
		return false;
	}
	
	public double getDistanceToNearest(int cityIndex, int nthNearest){
		return D[cityIndex][nthNearest];		
	}
	
	public TspInstance(int number) {	
		this.name = instanceNames[number];
		String fileName = "data\\tsp\\"+instanceNames[number]+".tsp";	
		try{
			FileReader fr = new FileReader(fileName);
			BufferedReader bfr = new BufferedReader(fr);
			loadData(bfr);
		}catch(Exception ex1){
			try{
			fileName = "data/tsp/"+instanceNames[number]+".tsp";	
			BufferedReader bfr = new BufferedReader(new InputStreamReader(this
					.getClass().getClassLoader().getResourceAsStream(fileName)));
			loadData(bfr);
			}catch(Exception ex2){
				ex1.printStackTrace();
				ex2.printStackTrace();
				System.err.print("problem when opening file "+fileName);				
			}
		}
		
//		this.name = instanceNames[number];
//		String fileName = "data/tsp/"+instanceNames[number]+".tsp";	
//		BufferedReader bfr;
//		try{
//			FileReader fr = new FileReader(fileName);
//			bfr = new BufferedReader(fr);
//			loadData(bfr);
//		}catch(Exception ex1){
//			try{
//			bfr = new BufferedReader(new InputStreamReader(this
//					.getClass().getClassLoader().getResourceAsStream(fileName)));
//			loadData(bfr);
//			}catch(Exception ex2) {
//				ex1.printStackTrace();
//				ex2.printStackTrace();
//				System.err.print("problem when opening file "+fileName);				
//			}
//		}
//		
		
		this.loadNearestCities();
	}
	
	public void loadNearestCities() {
		

//		String fileName = "data/tsp/"+name+"NearestCities.txt";
//		BufferedReader bfr;
//		try{
//			FileReader fr = new FileReader(fileName);
//			bfr = new BufferedReader(fr);
//			readTable(bfr);
//		}catch(Exception ex1){
//			try {
//			bfr = new BufferedReader(new InputStreamReader(this
//					.getClass().getClassLoader().getResourceAsStream(fileName)));
//			readTable(bfr);
//			}catch(Exception ex2) {
//				ex1.printStackTrace();
//				ex2.printStackTrace();
//				System.err.print("problem when opening file "+fileName);				
//			}
//		}
		
		String fileName = "data\\tsp\\"+name+"NearestCities.txt";	
		try{
			FileReader fr = new FileReader(fileName);
			BufferedReader bfr = new BufferedReader(fr);
			readTable(bfr);
		}catch(Exception ex1){
			try{
			fileName = "data/tsp/"+name+"NearestCities.txt";	
			BufferedReader bfr = new BufferedReader(new InputStreamReader(this
					.getClass().getClassLoader().getResourceAsStream(fileName)));
			readTable(bfr);
			}catch(Exception ex2){
				ex1.printStackTrace();
				ex2.printStackTrace();
				System.err.print("problem when opening file "+fileName);				
			}
		}

		
	}
	
	private void readTable(BufferedReader bfr)throws Exception{
		this.nearestCities = new int[numbCities][N];
		String line;
		for(int i = 0; i < this.numbCities; i++){
			line = bfr.readLine();
			StringTokenizer tok = new StringTokenizer(line);
			for(int j = 0; j < N; j++){
				nearestCities[i][j] = Integer.parseInt( tok.nextToken() );
			}
		}
		return;
	}
	
	
	/* Receives reader for data file and extracts data */
	private void loadData(BufferedReader bfr) throws Exception{
		String line;		
		for(int i = 0; i < 3; i++) // ignore first three lines
			bfr.readLine();	
		
		//fourth line gives the number of cities
		line = bfr.readLine(); 
		StringTokenizer tok = new StringTokenizer(line);
		tok.nextToken(); tok.nextToken();
		this.numbCities = Integer.parseInt(tok.nextToken());
		
		line = bfr.readLine(); //ignore lines 5 to ...
		while( !line.equals("NODE_COORD_SECTION")){ //ignore next lines
			line = bfr.readLine(); 	
			//System.out.println("here");
		}
		
		//retrieve coordinates
		this.coordinates = new double[numbCities][2];
		for(int i = 0; i < numbCities; i++){
			tok = new StringTokenizer(bfr.readLine());
			tok.nextToken();
			coordinates[i][0] = Double.parseDouble(tok.nextToken());
			coordinates[i][1] = Double.parseDouble(tok.nextToken());
		}				
	}

	public String toString() {
		StringBuilder build = new StringBuilder();		
		for(int i = 0; i < numbCities; i++){
			build.append(this.coordinates[i][0]+" "+this.coordinates[i][1]+"\n");
		}	
		return build.toString();
	}

	int getNumbCities() {
		return this.numbCities;
	}

	double[][] getCoordinates(){
		return this.coordinates;
	}
	
	public double getDistance(int city1, int city2){
		double x1 = coordinates[city1][0];
		double x2 = coordinates[city2][0];
		double y1 = coordinates[city1][1];
		double y2 = coordinates[city2][1];
		return StrictMath.sqrt( (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));	
	}
}

