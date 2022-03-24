package hyflex.chesc2011;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class represents the competition.
 * 
 * @author David Omrai
 */
public class Competition {
  public static final String[] algorithmIDs = {
    "ACO-HH",
    "AdapHH-GIHH",
    "Ant-Q",
    "AVEG-Nep",
    "BiasILS",
    "Clean",
    "Clean-2",
    "DynILS",
    "EPH",
    "GenHive",
    "GISS",
    "HAEA",
    "HAHA",
    "ISEA",
    "KSATS-HH",
    "LeanGIHH",
    "MCHH-S",
    "ML",
    "NAHH",
    "PHUNTER",
    "SA-ILS",
    "SelfSearch",
    "VNS-TW",
    "XCJ",
  };

  public static final String[] problemIDs = {
    "SAT", "BinPacking", "PersonnelScheduling", "FSP", "TSP", "VRP", "QAP"
  };

  /**
   * This map represents what instances are on each line of the results file. It also holds the
   * order of instances for each problem domain in the results file.
   */
  public static final Map<String, List<String>> problemInstances = new HashMap<>() {
    {
      put("SAT", new ArrayList<>(Arrays.asList(
          "pg-525-2276-hyflex-3",
          "pg-696-3122-hyflex-5", 
          "pg-525-2336-hyflex-4",
          "jarv-684-2300-hyflex-10", 
          "hg4-300-1200-hyflex-11"
      )));
      put("TSP", new ArrayList<>(Arrays.asList(
        "pr299-hyflex-0", 
        "usa13509-hyflex-8", 
        "rat575-hyflex-2",
        "u2152-hyflex-7", 
        "d1291-hyflex-6"
      )));
      put("FSP", new ArrayList<>(Arrays.asList(
        "tai100_20_02",
        "tai500_20_02",
        "tai100_20_04",
        "tai200_20_01",
        "tai500_20_03"
      )));
      put("QAP", new ArrayList<>(Arrays.asList(
        "sko100a",
        "tai100a",
        "tai256c",
        "tho150",
        "wil100"
      )));
      put("BinPacking", new ArrayList<>(Arrays.asList(
        "triples2004/instance1",
        "falkenauer/u1000-01",
        "test/testdual7/binpack0",
        "50-90/instance1",
        "test/testdual10/binpack0"
      )));
      put("PersonnelScheduling", new ArrayList<>(Arrays.asList(
        "Ikegami-3Shift-DATA1.2",
        "MER-A",
        "ERRVH-B",
        "BCV-A.12.1",
        "ORTEC01"
      )));
      put("VRP", new ArrayList<>(Arrays.asList(
        "Homberger/RC/RC2-10-1",
        "Solomon/R/R101",
        "Homberger/C/C1-10-1",
        "Solomon/R/R101",
        "Homberger/RC/RC1-10-5"
      )));
    }
  };
  
  final String defaultDirectory = "./results";
  
  /**
   * Method is used for testing each algorithm on all problem domains and instances.
   */
  public void run(
      List<String> hyperheurictics, List<String> problems,
      long timeout, int runs, String id) throws Exception {
    // check if user defined output folder
    if (id.equals("0")) {
      // create new folder for results
      id = "" + System.currentTimeMillis();
    }
    
    // create output folder
    final String resultsDir = Optional.ofNullable(
        System.getenv("RESULTS_DIR")).orElse(defaultDirectory);


    new File(resultsDir + "/" + id + "/").mkdirs();

    System.out.println(resultsDir);

    // run hyper-herutistic on all problem domains
    for (String algorithmID : hyperheurictics) {
      if (!Arrays.asList(algorithmIDs).contains(algorithmID)) {
        System.out.println("ERROR, wrong algorithm name " + algorithmID);
        continue;
      }
      List<List<Double>> results = new ArrayList<List<Double>>();

      for (String problemID : problemIDs) {
        //todo append blank line if user did not define this problem
        if (Arrays.stream(problems.toArray()).anyMatch(problemID::equals)) {
          results.add(runAlg(algorithmID, problemID, runs, timeout));
        } else {
          /**
           * For this problem blank instances results.
           */
          results.add(new ArrayList<Double>() {{
              add(null);//1
              add(null);//2
              add(null);//3
              add(null);//4
              add(null);//5
            }
          });
        }
        
      }
      System.out.println(results);
      makeResultsCard(results, algorithmID, id);
    }
  }

  /** 
   * Method is used for testing all instances of problem domain on given algorithm.
   * 
   * @param algorithmID name of given hyper-heuristic algorihtm
   * @return ArrayList with median values of received results
   */
  public List<Double> runAlg(String algorithmID, String problemID, Integer algRuns, Long timeout)
      throws Exception {
    List<Double> resultsMedian = new ArrayList<Double>();

    for (int instanceIx = 0; instanceIx <= 4; instanceIx++) {
      CompetitionRunner r =
          new CompetitionRunner(algorithmID, problemID, instanceIx, timeout, algRuns);
      // run the competition
      r.run();

      Double median = getMedianFromInstanceResults(r.getResults());
      resultsMedian.add(median);
    }

    return resultsMedian;
  }

  /**
   * Method returns the median of given array.
   * 
   * @param instanceResults array of double numbers
   * @returns
   */
  public static Double getMedianFromInstanceResults(List<Double> instanceResults) throws Exception {
    int middle = 0;

    if (instanceResults.size() == 0) {
      return Double.MAX_VALUE;
    }

    if (instanceResults.size() == 1) {
      return instanceResults.get(0);
    }
    Collections.sort(instanceResults);
    middle = instanceResults.size() / 2;
    middle = middle > 0 && middle % 2 == 0 ? middle - 1 : middle;

    return instanceResults.get(middle);
  }

  /**
   * Method creates a file with given results.
   * File represents a competition card for each competitor.
   * 
   * @param results array of arrays with results
   * @param algorithmID name of given hyper-heuristic algorithm
   */
  public void makeResultsCard(
      List<List<Double>> results, String algorithmID, String id) throws IOException {
    
    final String resultsDir = Optional.ofNullable(
            System.getenv("RESULTS_DIR")).orElse(defaultDirectory);
    try (
        FileWriter fwriter =
            new FileWriter(resultsDir + "/" + id + "/" + algorithmID + ".txt");
        PrintWriter printer = new PrintWriter(fwriter);) {
      String line = "";
      for (List<Double> array : results) {
        line = "";
        for (Double median : array) {
          line += (median == null ? "---" : median) + ", ";
        }
        printer.println(line.substring(0, line.length() - 2));
      }
    }
  }
}
