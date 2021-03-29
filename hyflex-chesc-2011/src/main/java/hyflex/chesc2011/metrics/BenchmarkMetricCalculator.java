/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class represents benchmark calculator for each solutions card
 * stored inside results file.
 * .
 * File has the following format
 * SAT: 3, 5, 4, 10, 11
 * BP:  7, 1, 9, 10, 11
 * PS:  5, 9, 8, 10, 11
 * FS:  1, 8, 3, 10, 11
 * TSP: 0, 8, 2, 7, 6 
 * VRP: 6, 2, 5, 1, 9 
 * .
 * Lines represents problem domains and collumns instances of this domain
 * You can change what problem is where, all that's needed to be done is 
 * to keep the same format for all results files.
 * .
 * And finally if you modify the order of problem domains or instances
 * also you have to modify the problems array and problemInstances map
 */
public class BenchmarkMetricCalculator {
  // Path where the results are stored
  String resultsPath = "./results";
  // Path where the metadata are stored
  String metadataPath = "/hyflex/hyflex-chesc-2011";
  // Path where the file with results is stored
  String resultsXmlFile = "./results.xml";

  // This arrays holds the order of problem domains in results file
  String[] problems = {"SAT", "TSP"};


  /**
   * Map represents weights for each problem domain.
   */
  @SuppressWarnings("serial")
  Map<String, Double> problemsWeightsMap = new HashMap<>() {{
      put("SAT", 1.0);
      put("TSP", 1.0);
    }
  };


  // Interval on which are the results being mapped, metric range
  public final double scoreIntervalFrom = 0.0;
  public final double scoreIntervalTo = 1.0;


  /**
   * This map represents what instances are on each line of
   * the results file.
   * It also holds the order of instances for each problem domain
   * in the results file.
   */
  @SuppressWarnings("serial")
  Map<String, List<String>> problemInstances = new HashMap<>() {{
        put("SAT", new ArrayList<>(
            Arrays.asList(
              "hyflex-sat-3", "hyflex-sat-5", "hyflex-sat-4", "hyflex-sat-10", "hyflex-sat-11")));
        put("TSP", new ArrayList<>(
            Arrays.asList(
              "hyflex-tsp-0", "hyflex-tsp-8", "hyflex-tsp-2", "hyflex-tsp-7", "hyflex-tsp-6")));
    }
  };


  /**
   * Main method of this class.
   * It creates a new object and execute run method.
   * @param args Name of directory where results files are stored.
   */
  public static void main(String[] args) {
    try {
      if (args.length <= 0) {
        throw new Exception("Error: No results directory name given.");
      }

      BenchmarkMetricCalculator ibc = new BenchmarkMetricCalculator();
      ibc.run(args[0]);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  
  /**
   * Method evaluates all algorithms and stores results into file.
   * @param id Name of the directory where algorithm problem results are stored.
   */
  public void run(String id) throws Exception {
    UnitMetricScoreCalculator scoreCalculator = new UnitMetricScoreCalculator(
        problems, 
        metadataPath, problemInstances, problemsWeightsMap, scoreIntervalFrom, scoreIntervalTo);
    
    String [] resFiles = ScoreCardHandler.getCardsNames(Paths.get(resultsPath + "/" + id));

    List<ScoreCard> results = new ArrayList<>();

    for (String fileName : resFiles) {
      Path scoreCardPath = Paths.get(resultsPath + "/" + id + "/" + fileName);
      
      ScoreCard algorithmResults = ScoreCardHandler.loadCard(
          problems, scoreCardPath, problems, problemInstances);

      results.add(scoreCalculator.calculateScore(algorithmResults));
    }

    ScoreCardHandler.saveResultsToXmlFile(resultsXmlFile, results);
  }
}
