package hyflex.chesc2011.metrics.metadata;

import AbstractClasses.ProblemDomain;
import FlowShop.FlowShop;
import java.util.Arrays;

public class MetadataGenerator {
  private static final int NUM_TRIALS = 1000;
  private static final String[] FLOWSHOP_INSTANCES = {
    "tai100_20_1",
    "tai100_20_2",
    "tai100_20_3",
    "tai100_20_4",
    "tai100_20_5",
    "tai200_10_2",
    "tai200_10_3",
    "tai500_20_1",
    "tai500_20_2",
    "tai500_20_4",
    "tai200_20_1",
    "tai500_20_3",
  };
  private static final String[] FLOWSHOP_INSTANCE_OPTIMUM = {
    "5851",
    "6099",
    "6099",
    "6072",
    "6009",
    "10422",
    "10886",
    "25922",
    "26353",
    "26424",
    "10979",
    "26320",
  };

  /**. */
  public static void main(String[] args) {
    ProblemDomain problem = new FlowShop(123456);
    for (int instanceIx = 0; instanceIx <= 11; instanceIx++) {
      problem.loadInstance(instanceIx);
      
      double[] objValues = new double[NUM_TRIALS];
      for (int i = 0;i < NUM_TRIALS;i++) {
        problem.initialiseSolution(0);
        objValues[i] = problem.getFunctionValue(0);
      }
      Arrays.sort(objValues);
      double middle = objValues[NUM_TRIALS / 2];
      
      System.out.println(
          FLOWSHOP_INSTANCES[instanceIx]
          + ": " 
          + FLOWSHOP_INSTANCE_OPTIMUM[instanceIx]
          + " - "
          + middle
      );
    }
  }
}
