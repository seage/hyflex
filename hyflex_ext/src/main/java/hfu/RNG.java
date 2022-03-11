package hfu;

import java.util.Random;

public class RNG {
	private static Random rng = new Random();
	
	static void setSeed(long seed){
		rng = new Random(seed);
	}
	
	static public Random get(){
		return rng;
	}
	
}
