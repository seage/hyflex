package hfu;

import java.util.Random;

public class RNG {
  private static Random rng = new Random();
  
  static void setSeed(long seed) {
    rng = new Random(seed);
  }
  
  public static Random get() {
    return rng;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\RNG.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */