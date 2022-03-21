package hyflex.chesc2011;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import BinPacking.BinPacking;

import Examples.ExampleHyperHeuristic1;

import FlowShop.FlowShop;
import PersonnelScheduling.PersonnelScheduling;
import QAP.QAP;
import SAT.SAT;
import VRP.VRP;

import be.kuleuven.kahosl.acceptance.AcceptanceCriterionType;
import be.kuleuven.kahosl.hyperheuristic.GIHH;
import be.kuleuven.kahosl.selection.SelectionMethodType;
import fr.lalea.eph.EPH;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;

import kubalik.EvoCOPHyperHeuristic;
import leangihh.LeanGIHH;
import pearlhunter.PearlHunter;
import travelingSalesmanProblem.TSP;

import acuna.GISS;
import bader.Clean;
import bader.Clean02;
import csput.CSPUTGeneticHiveHyperHeuristic;
import elomari.elomariSS;
import gomez.HaeaHH;
import hsiao.HsiaoCHeSCHyperheuristic;
import jiang.sa_ilsHyperHeuristic;
import johnston.JohnstonBiasILS;
import johnston.JohnstonDynamicILS;
import laroseml.LaroseML;
import lehrbaum.LehrbaumHAHA;
import iridia.MyHyperHeuristic;
import khmassi.Ant_Q;
import shafi.ShafiXCJ;
import aco.ACO_HH;
import sim.SimSATS_HH;
import urli.Urli_AVEG_NeptuneHyperHeuristic;
import mcclymont.McClymontMCHHS;

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
 *    |- QAP/
 *
 * <p>Please report any bugs or issues to Dr. Matthew Hyde at mvh@cs.nott.ac.uk.
 * 
 * @author Dr. Matthew Hyde (mvh@cs.nott.ac.uk), School of Computer Science. 
 *      University of Nottingham, U.K.
 * 
 *      Class modification.
 *      @author David Omrai
 */ 

public class CompetitionRunner extends Thread {
  /*
   * These are parameters which can be changed. time - set to ten minutes, but this may need to
   * change depending on your machine spec. Refer to
   * http://www.asap.cs.nott.ac.uk/chesc2011/benchmarking.html problemID - the selected domain
   * instanceID - the selected instance of the problem domain. This should be between 0-4 inclusive
   * rnd - select a random seed
   */
  private long time;
  private String algorithmID;
  private String problemID;
  private int instanceID = 0;// This should be between 0-4 inclusive.
  private Random rnd = new Random(123456789);
  // These are parameters were used for the competition,
  // so if they are changed then the results may not be comparable to those of the competition
  private int numberOfRuns = 31;
  private final int instances = 5;

  private long instanceSeed;
  private Map<String, long[][]> instanceSeeds;

  ArrayList<Double> results = new ArrayList<Double>();

  /**
   * Class constructor with problemID and algorithmID translator.
   * 
   * @param problemID ID of the problem domain
   * @param instanceID ID of the problem instance
   * @throws Exception
   */
  public CompetitionRunner(String algorithmID, String problemID, int instanceID, long runTime,
      int algRuns) throws Exception {
    if (instanceID > instances || instanceID < 0) {
      System.err.println("Wrong input for the problem domain");
      System.exit(-1);
    }
    this.algorithmID = algorithmID;
    this.problemID = problemID;
    this.instanceID = instanceID;
    this.time = runTime;
    this.numberOfRuns = algRuns;

    if (!Arrays.asList(Competition.algorithmIDs).contains(algorithmID)) {
      throw new Exception("Wrong input for the problem domain: " + algorithmID);
    }

    if (!Arrays.asList(Competition.problemIDs).contains(problemID)) {
      throw new Exception("Wrong input for the problem domain: " + problemID);
    }

    setInstanceSeeds();
  }

  public ArrayList<Double> getResults() {
    return results;
  }

  private void setInstanceSeeds() {
    instanceSeeds = new HashMap<>();

    for (String problemID : Competition.problemIDs) {
      long[][] rndNumbers = new long[instances][];
      instanceSeeds.put(problemID, rndNumbers);
      for (int y = 0; y < instances; y++) {
        rndNumbers[y] = new long[numberOfRuns];
        for (int r = 0; r < numberOfRuns; r++) {
          rndNumbers[y][r] = rnd.nextLong();
        }
      }
    }
  }

  /**
   * Method creates new HyperHeuristic object with given parameters and returs it.
   * 
   * @param number number representing the hh
   * @param timeLimit time limit for hh
   * @return HyperHeuristic object
   * @throws Exception
   */
  public HyperHeuristic loadHyperHeuristic(String algorithmID, long timeLimit) throws Exception {
    HyperHeuristic h;
    switch (algorithmID) {
      case "ExampleHyperHeuristic1":
        h = new ExampleHyperHeuristic1(rnd.nextLong());
        break;
      case "EPH":
        h = new EPH(rnd.nextLong());
        break;
      case "LeanGIHH":
        h = new LeanGIHH(rnd.nextLong());
        break;
      case "PearlHunter":
        h = new PearlHunter(rnd.nextLong());
        break;
      case "GIHH":
        h = new GIHH(rnd.nextLong(), loadProblemDomain(problemID).getNumberOfHeuristics(), time,
            "gihh", SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD,
            AcceptanceCriterionType.AdaptiveIterationLimitedListBasedTA);
        break;
      case "ISEA":
        h = new EvoCOPHyperHeuristic(rnd.nextLong());
        break;
      case "GISS":
        h = new GISS(rnd.nextLong());
        break;
      case "Clean":
        h = new Clean(rnd.nextLong());
        break;
      case "Clean02":
        h = new Clean02(rnd.nextLong());
        break;
      case "CSPUTGeneticHiveHyperHeuristic":
        h = new CSPUTGeneticHiveHyperHeuristic(rnd.nextLong());
        break;
      case "elomariSS":
        h = new elomariSS(rnd.nextLong());
        break;
      case "HaeaHH":
        h = new HaeaHH(rnd.nextLong());
        break;
      case "HsiaoCHeSCHyperheuristic":
        h = new HsiaoCHeSCHyperheuristic(rnd.nextLong());
        break;
      case "sa_ilsHyperHeuristic":
        h = new sa_ilsHyperHeuristic(rnd.nextLong());
        break;
      case "JohnstonBiasILS":
        h = new JohnstonBiasILS(rnd.nextLong());
        break;
      case "JohnstonDynamicILS":
        h = new JohnstonDynamicILS(rnd.nextLong());
        break;
      case "LaroseML":
        h = new LaroseML(rnd.nextLong());
        break;
      case "LehrbaumHAHA":
        h = new LehrbaumHAHA(rnd.nextLong());
        break;
      case "MyHyperHeuristic":
        h = new MyHyperHeuristic(rnd.nextLong());
        break;
      case "Ant_Q":
        h = new Ant_Q(rnd.nextLong());
        break;
      case "ShafiXCJ":
        h = new ShafiXCJ(rnd.nextLong());
        break;
      case "ACO_HH":
        h = new ACO_HH(rnd.nextLong());
        break;
      case "SimSATS_HH":
        h = new SimSATS_HH(rnd.nextLong());
        break;
      case "Urli_AVEG_NeptuneHyperHeuristic":
        h = new Urli_AVEG_NeptuneHyperHeuristic(rnd.nextLong());
        break;
      case "McClymontMCHHS":
        h = new McClymontMCHHS(rnd.nextLong());
        break;
      default:
        throw new Exception("There is no hyper-heuristic with this id: " + algorithmID);
    }
    h.setTimeLimit(timeLimit);
    return h;
  }

  /**
   * Method creates new ProblemDomain object with given parameters and returns it.
   * 
   * @param number number of problem
   * @return ProblemDomain object
   * @throws Exception
   */
  public ProblemDomain loadProblemDomain(String problemID) throws Exception {
    ProblemDomain p;
    switch (problemID) {
      case "SAT":
        p = new SAT(instanceSeed);
        break;
      case "BinPacking":
        p = new BinPacking(instanceSeed);
        break;
      case "PersonnelScheduling":
        p = new PersonnelScheduling(instanceSeed);
        break;
      case "FSP":
        p = new FlowShop(instanceSeed);
        break;
      case "TSP":
        p = new TSP(instanceSeed);
        break;
      case "VRP":
        p = new VRP(instanceSeed);
        break;
      case "QAP":
        p = new QAP(instanceSeed);
        break;
      default:
        throw new Exception("There is no problem domain with this id: " + problemID);
    }
    return p;
  }

  /**
   * Method starts the computing of hh on given instance of given problem domain.
   */
  public void run(){
    Map<String, int[]> instancesToUse = new HashMap<>();
    /*
     * These instances are generated by CompetitionInstanceSelector.java Ten instances are included
     * for each problem domain, but these are the instances selected for use in the competition. The
     * last two instances of the first four domains were hidden instances.
     */

    instancesToUse.put("SAT", new int[] {3, 5, 4, 10, 11});
    instancesToUse.put("BinPacking", new int[] {7, 1, 9, 10, 11});
    instancesToUse.put("PersonnelScheduling", new int[] {5, 9, 8, 10, 11});
    instancesToUse.put("FSP", new int[] {1, 8, 3, 10, 11});
    instancesToUse.put("TSP", new int[] {0, 8, 2, 7, 6});
    instancesToUse.put("VRP", new int[] {6, 2, 5, 1, 9});
    instancesToUse.put("QAP", new int[] {0, 4, 7, 8, 9});

    System.out.println("PROBLEM DOMAIN " + problemID);
    int instanceToUse = instancesToUse.get(problemID)[instanceID];
    // int instanceOrder = Arrays.asList(instancesToUse.get(problemID)).indexOf(instanceToUse);
    System.out.println("\tINSTANCE " + Competition.problemInstances.get(problemID).get(instanceID));

    for (int run = 0; run < numberOfRuns; run++) {
      try {
        instanceSeed = instanceSeeds.get(problemID)[instanceID][run];
        System.out.println("\t\tRUN " + (run+1) + "/"+numberOfRuns+" - seed " + instanceSeed);

        ProblemDomain p = loadProblemDomain(problemID);
        HyperHeuristic h = loadHyperHeuristic(algorithmID, time);
        System.out.print("\t\t\tHYPER HEURISTIC " + h.toString());
        p.loadInstance(instanceToUse);
        h.loadProblemDomain(p);

        long initialTime2 = System.currentTimeMillis();
        h.run();

        int[] i = p.getHeuristicCallRecord();
        int counter = 0;
        for (int y : i) {
          counter += y;
        }
        System.out.println("\t" + h.getBestSolutionValue() + "\t" + (h.getElapsedTime() / 1000.0)
            + "\t" + (System.currentTimeMillis() - initialTime2) / 1000.0 + "\t" + counter);

        results.add(h.getBestSolutionValue());
      } catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
