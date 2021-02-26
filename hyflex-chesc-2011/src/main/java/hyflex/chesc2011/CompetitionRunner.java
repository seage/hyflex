package hyflex.chesc2011;

// import java.io.FileWriter;
// import java.io.IOException;
// import java.io.PrintWriter;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import BinPacking.BinPacking;

import Examples.ExampleHyperHeuristic1;

import FlowShop.FlowShop;
import PersonnelScheduling.PersonnelScheduling;
import SAT.SAT;
import VRP.VRP;

import be.kuleuven.kahosl.acceptance.AcceptanceCriterionType;
import be.kuleuven.kahosl.hyperheuristic.GIHH;
import be.kuleuven.kahosl.selection.SelectionMethodType;
import fr.lalea.eph.EPH;

import java.util.ArrayList;
import java.util.Random;

import kubalik.EvoCOPHyperHeuristic;
import leangihh.LeanGIHH;
import pearlhunter.PearlHunter;
import travelingSalesmanProblem.TSP;


/**
 * This class replicates the experimental setup for the CHeSC competition 2011.
 * Please refer to the comments in the code to find the parameters which are modifiable.
 * 
 * <p>Your hyper-heuristic should be added into the loadHyperHeuristic class, 
 * replacing the example hyper-heuristic.
 * 
 * <p>To allow the program to print the output files, 
 * please create the following file structure, with 6 subdirectories:
 * 
 * <p>results/
 *    |- SAT/
 *    |- BinPacking/
 *    |- PersonnelScheduling/
 *    |- FlowShop/
 *    |- TSP/
 *    |- VRP/
 *
 * <p>Please report any bugs or issues to Dr. Matthew Hyde at mvh@cs.nott.ac.uk.
 * 
 * @author Dr. Matthew Hyde (mvh@cs.nott.ac.uk), School of Computer Science. 
 *      University of Nottingham, U.K.
 * 
 */ 

public class CompetitionRunner extends Thread {
  /* These are parameters which can be changed.
  * time - set to ten minutes, but this may need to change depending on your machine spec. Refer to http://www.asap.cs.nott.ac.uk/chesc2011/benchmarking.html
  * numberofhyperheuristics - the number you wish to test in the same run
  * problem - the selected domain
  * instance - the selected instance of the problem domain. This should be between 0-4 inclusive
  * rng - select a random seed
  */
  private static long time;
  //private static final int numberofhyperheuristics = 7;
  private static int problem = 0;
  private static int instance = 0;//This should be between 0-4 inclusive.
  private static int algorithm = 0;//This represents hh
  private static Random rng = new Random(123456789);
  //These are parameters were used for the competition, 
  //so if they are changed then the results may not be comparable to those of the competition
  private static int numberofruns = 31;
  private static final int domains = 6;
  private static final int instances = 5;

  private static long instanceseed;
  private static String resultsfolder;
  private static long[][][] instanceseeds;

  ArrayList<Double> results = new ArrayList<Double>();

  /**
  * Class constructor with problemID and algorithmID translator.
  * @param problemID   ID of the problem domain
  * @param instanceID  ID of the problem instance
 */
  public CompetitionRunner(
      String algorithmID,
      String problemID,
      int instanceID,
      long runTime,
      int algRuns
  ) {
    if (instanceID > instances || instanceID < 0) {
      System.err.println("wrong input for the problem domain");
      System.exit(-1);
    }
    instance      = instanceID;
    time          = runTime;
    numberofruns  = algRuns;

    switch (algorithmID) {
      case "ExampleHyperHeuristic1": 
        algorithm = 0;
        break;
      case "EPH": 
        algorithm = 1;
        break;
      case "LeanGIHH": 
        algorithm = 2;
        break;
      case "PearlHunter": 
        algorithm = 3;
        break;
      case "GIHH": 
        algorithm = 4;
        break;
      case "ISEA": 
        algorithm = 5;
        break;
      default: System.err.println("wrong input for the problem domain");
        System.exit(-1);
    }

    switch (problemID) {
      case "SAT": 
        problem = 0;
        resultsfolder = "SAT";
        break;
      case "BinPacking": 
        problem = 1;
        resultsfolder = "BinPacking";
        break;
      case "PersonnelScheduling": 
        problem = 2;
        resultsfolder = "PersonnelScheduling";
        break;
      case "FlowShop": 
        problem = 3;
        resultsfolder = "FlowShop";
        break;
      case "TSP": 
        problem = 4;
        resultsfolder = "TSP";
        break;
      case "VRP": 
        problem = 5;
        resultsfolder = "VRP";
        break;
      default: System.err.println("wrong input for the problem domain");
        System.exit(-1);
    }

    setInstanceseeds();
  }

  
  /**
   * Class constructor with problemID translator.
   * @param algorithmID number representing algorithm from range 0-5
   * @param problemID   number representing problem from range 0-(domains-1)
   * @param instanceID  number representing the instance of problem domain, range 0-(instances-1)
   * @param runTime     run time of given algorithm on instance
   * @param algRuns     number of runs
   */
  public CompetitionRunner(
      int algorithmID, 
      int problemID, 
      int instanceID, 
      long runTime, 
      int algRuns
  ) {
    problem       = problemID;
    instance      = instanceID;
    algorithm     = algorithmID;
    time          = runTime;
    numberofruns  = algRuns;

    switch (problem) {
      case 0:
        resultsfolder = "SAT";
        break;
      case 1:
        resultsfolder = "BinPacking";
        break;
      case 2:
        resultsfolder = "PersonnelScheduling";
        break;
      case 3:
        resultsfolder = "FlowShop";
        break;
      case 4:
        resultsfolder = "TSP";
        break;
      case 5:
        resultsfolder = "VRP";
        break;
      default: System.err.println("wrong input for the problem domain");
        System.exit(-1);
    }

    setInstanceseeds();
  }

  public ArrayList<Double> getResults() {
    return results;
  }

  private void setInstanceseeds() {
    instanceseeds = new long[domains][instances][numberofruns];

    for (int x = 0; x < domains; x++) {
      for (int y = 0; y < instances; y++) {
        for (int r = 0; r < numberofruns; r++) {
          instanceseeds[x][y][r] = rng.nextLong();
        }
      }
    }
  }

  /**
   * Method creates new HyperHeuristic object with given parameters and returs it.
   * @param number    number representing the hh
   * @param timeLimit time limit for hh
   * @param rng       random number
   * @return          HyperHeuristic object
   */
  public static HyperHeuristic loadHyperHeuristic(int number, long timeLimit, Random rng) {
    HyperHeuristic h;
    switch (number) {
      case 0: 
        h = new ExampleHyperHeuristic1(rng.nextLong()); 
        h.setTimeLimit(timeLimit); 
        break;
      case 1: 
        h = new EPH(rng.nextLong()); 
        h.setTimeLimit(timeLimit); 
        break;
      case 2: 
        h = new LeanGIHH(rng.nextLong()); 
        h.setTimeLimit(timeLimit); 
        break;
      case 3: 
        h = new PearlHunter(rng.nextLong()); 
        h.setTimeLimit(timeLimit); 
        break;
      case 4: 
        h = new GIHH(
          rng.nextLong(),
          loadProblemDomain(problem).getNumberOfHeuristics(), 
          time,
          "gihh",
          SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD, 
          AcceptanceCriterionType.AdaptiveIterationLimitedListBasedTA
        ); 
        h.setTimeLimit(timeLimit); 
        break;
      case 5: 
        h = new EvoCOPHyperHeuristic(rng.nextLong()); 
        h.setTimeLimit(timeLimit); 
        break;
      default: System.err.println("there is no hyper heuristic with this index");
        h = null;
        System.exit(0);
    }
    return h;
  }

  /**
   * Method creates new ProblemDomain object with given parameters and returns it.
   * @param number  number of problem
   * @return        ProblemDomain object
   */
  public static ProblemDomain loadProblemDomain(int number) {
    ProblemDomain p;
    switch (number) {
      case 0: 
        p = new SAT(instanceseed); 
        break;
      case 1: 
        p = new BinPacking(instanceseed); 
        break;
      case 2: 
        p = new PersonnelScheduling(instanceseed); 
        break;
      case 3: 
        p = new FlowShop(instanceseed); 
        break;
      case 4: 
        p = new TSP(instanceseed); 
        break;
      case 5: 
        p = new VRP(instanceseed); 
        break;
      default: System.err.println("there is no problem domain with this index");
        p = new BinPacking(rng.nextLong());
        System.exit(0);
    }
    return p;
  }

  /**
   * Method starts the computing of hh on given instance of given problem domain.
   */
  public void run() {
    int[][] instancesToUse = new int[domains][];
    /*
     * These instances are generated by CompetitionInstanceSelector.java
     * Ten instances are included for each problem domain, 
     * but these are the instances selected for use in the competition.
     * The last two instances of the first four domains were hidden instances.
     */
    final int[] sat = {3,5,4,10,11};
    final int[] bp  = {7,1,9,10,11};
    final int[] ps  = {5,9,8,10,11};
    final int[] fs  = {1,8,3,10,11};
    final int[] tsp = {0,8,2,7,6};
    final int[] vrp = {6,2,5,1,9};

    instancesToUse[0] = sat;
    instancesToUse[1] = bp;
    instancesToUse[2] = ps;
    instancesToUse[3] = fs;
    instancesToUse[4] = tsp;
    instancesToUse[5] = vrp;

    System.out.println("PROBLEM DOMAIN " + resultsfolder);
    int instancetouse = instancesToUse[problem][instance];
    System.out.println("  instance " + instancetouse + " ");

    for (int run = 0; run < numberofruns; run++) {
      instanceseed = instanceseeds[problem][instance][run];
      System.out.println("    RUN " + run + " " + instanceseed);

      ProblemDomain p = loadProblemDomain(problem);
      HyperHeuristic h = loadHyperHeuristic(algorithm, time, rng);
      System.out.print("      HYPER HEURISTIC " + h.toString());
      p.loadInstance(instancetouse);
      h.loadProblemDomain(p);

      long initialTime2 = System.currentTimeMillis();
      h.run();

      int[] i = p.getHeuristicCallRecord();
      int counter = 0;
      for (int y : i) {
        counter += y;
      }
      System.out.println(
          "\t" + h.getBestSolutionValue() + "\t"
          + (h.getElapsedTime() / 1000.0) + "\t"
          + (System.currentTimeMillis() - initialTime2) / 1000.0 + "\t" + counter
      );

      results.add(h.getBestSolutionValue());
    }
  }

  // public static void main(String[] args) {  
  //   CompetitionRunner r = new CompetitionRunner(problem, instance, algorithm, 5000);
  //   r.start();
  //   try {
  //     r.join();
  //   } catch (InterruptedException e) {
  //     System.out.println();
  //     System.exit(0);
  //   }
  // }
}