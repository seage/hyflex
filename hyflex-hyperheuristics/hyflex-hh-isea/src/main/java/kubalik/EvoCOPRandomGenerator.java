package kubalik;

import java.util.Random;

public final class EvoCOPRandomGenerator {
	private static EvoCOPRandomGenerator instance = null;
	private Random generator = null;
	
	/**
	 * @return the generator
	 */
	public Random getGenerator() {
		return generator;
	}

	private EvoCOPRandomGenerator(){
		generator = new Random();
	}
	
	public static synchronized EvoCOPRandomGenerator getInstance() {
	  	if(instance == null) {
	  		instance = new EvoCOPRandomGenerator();
	 	}
	  	return instance;
	}
	
	public void setSeed(long seed){
		generator.setSeed(seed);
	}
}
