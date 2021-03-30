/**
 * @author David Omrai
 */

package hyflex.chesc2011;

// Java libraries
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class Competition {
  private static final String[] algorithmIDs = {
    "GIHH", "LeanGIHH", "PearlHunter", "EPH", "ISEA"
  };
  private static final String[] problemIDs = {
    "SAT", "BinPacking", "PersonnelScheduling", "FlowShop", "TSP", "VRP"
  };
  
  final String defaultDirectory = "./results";
  
  /**
   * Method is used for testing each algorithm on all problem domains and instances.
   */
  public void run(
      List<String> hyperheurictics, long timeout, int runs, Long id) throws Exception {
    // check if user defined output folder
    if (id == 0) {
      // create new folder for results
      id = System.currentTimeMillis();
    }
    
    // create output folder
    final String resultsDir = Optional.ofNullable(
        System.getenv("RESULTS_DIR")).orElse(defaultDirectory);


    new File(resultsDir + "/" + Long.toString(id) + "/").mkdirs();

    System.out.println(resultsDir);

    // run hyper-herutistic on all problem domains
    for (String algorithmID : hyperheurictics) {
      if (!Arrays.asList(algorithmIDs).contains(algorithmID)) {
        System.out.println("ERROR, wrong algorithm name " + algorithmID);
        continue;
      }
      List<List<Double>> results = new ArrayList<List<Double>>();

      for (String problemID : problemIDs) {
        results.add(runAlg(algorithmID, problemID, runs, timeout));
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
      List<List<Double>> results, String algorithmID, Long id) throws IOException {
    
    final String resultsDir = Optional.ofNullable(
            System.getenv("RESULTS_DIR")).orElse(defaultDirectory);
    try (
        FileWriter fwriter =
            new FileWriter(resultsDir + "/" + Long.toString(id) + "/" + algorithmID + ".txt");
        PrintWriter printer = new PrintWriter(fwriter);) {
      String line = "";
      for (List<Double> array : results) {
        line = "";
        for (Double median : array) {
          line += median + ", ";
        }
        printer.println(line.substring(0, line.length() - 2));
      }
    }
  }
}
