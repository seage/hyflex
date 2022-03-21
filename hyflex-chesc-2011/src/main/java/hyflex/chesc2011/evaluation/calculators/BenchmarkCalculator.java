package hyflex.chesc2011.evaluation.calculators;

import hyflex.chesc2011.Competition;
import hyflex.chesc2011.evaluation.metadata.ProblemInstanceMetadata;
import hyflex.chesc2011.evaluation.metadata.ProblemInstanceMetadataReader;
import hyflex.chesc2011.evaluation.scorecard.ScoreCard;
import hyflex.chesc2011.evaluation.scorecard.ScoreCardHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Class represents benchmark calculator for each solutions card stored inside results file.
 * .
 * File has the following format 
 * line 
 *  0 | SAT: 3, 5, 4, 10, 11
 *  1 | BP: 7, 1, 9, 10, 11
 *  2 | PS: 5, 9, 8, 10, 11
 *  3 | FS: 1, 8, 3, 10, 11
 *  4 | TSP: 0, 8, 2, 7, 6
 *  5 | VRP: 6, 2, 5, 1, 9 
 * . 
 * Lines represents
 * problem domains and collumns instances of this domain You can change what problem is where, all
 * that's needed to be done is to keep the same format for all results files. 
 * . 
 * IMPORTANT!!! If you
 * decide to change the order of lines, keep in mind, you have to change it also in the
 * ScoreCardHelper class. 
 * . 
 * And finally if you modify the order of problem domains or instances also
 * you have to modify the problems array and problemInstances map
 * 
 * @author David Omrai
 */
public class BenchmarkCalculator {
  private static final Logger logger = 
      Logger.getLogger(BenchmarkCalculator.class.getName());
  // Path where the results are stored
  String resultsPath = "./results";
  // Path where the metadata are stored
  String metadataPath = "/hyflex/hyflex-chesc-2011";
  // Path where the file with results is stored
  String resultsXmlFile = "./results/%s/unit-metric-scores.xml";

  // This arrays represents problems with metadata
  String[] problems = {"SAT", "TSP", "FSP", "QAP"};

  /**
   * Main method of this class. It creates a new object and execute run method.
   * 
   * @param args Name of directory where results files are stored.
   */
  public static void main(String[] args) {
    try {
      if (args.length <= 1) {
        throw new Exception("Error: No results directory name given.");
      }

      List<String> problems = new ArrayList<>();
      problems.add("SAT");
      problems.add("TSP");
      problems.add("FSP");
      problems.add("QAP");

      BenchmarkCalculator ibc = new BenchmarkCalculator();
      ibc.run(args[0], args[1]);
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
  }


  /**
   * Method evaluates all algorithms and stores results into file.
   * 
   * @param id Name of the directory where algorithm problem results are stored.
   * @param metric Name of the metric to be used.
   */
  public void run(String id, String metric) throws Exception {
    logger.info("Evaluation is running...");
    
    if (!Files.exists(Paths.get(resultsPath, id))) {
      throw new Exception(String.format("Competition id '%s' does not exist", id));
    }

    Map<String, ProblemInstanceMetadata> instancesMetadata = ProblemInstanceMetadataReader
        .readProblemsInstancesMetadata(problems, Paths.get(metadataPath));

    String[] resFiles = ScoreCardHelper.getCardsNames(Paths.get(resultsPath, id));

    List<ScoreCard> results = new ArrayList<>();

    for (String fileName : resFiles) {
      logger.info("Evaluating the " + fileName);
      Path scoreCardPath = Paths.get(resultsPath, id, fileName);

      // Get algorihtm results
      List<String> implementedProblems = new ArrayList<>();
      ScoreCard algorithmResults =
          ScoreCardHelper.loadCard(problems, scoreCardPath, Competition.problemInstances, implementedProblems);
      
      UnitMetricScoreCalculator scoreCalculator =
          new UnitMetricScoreCalculator(
          instancesMetadata, Competition.problemInstances, 
          implementedProblems.toArray(new String[]{}));

      // Calculate algorithm scores
      ScoreCard algorithmScores = scoreCalculator.calculateScore(algorithmResults);
      results.add(algorithmScores);
    }

    resultsXmlFile = String.format(resultsXmlFile, id);
    ScoreCardHelper.saveResultsToXmlFile(resultsXmlFile, results);
    logger.info("The score file stored to " + resultsXmlFile);
  }
}
