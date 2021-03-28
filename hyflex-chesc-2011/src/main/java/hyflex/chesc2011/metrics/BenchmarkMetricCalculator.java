/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import hyflex.chesc2011.metrics.ResultsCardHandler;
import hyflex.chesc2011.metrics.ProblemInstanceMetadataReader;
import hyflex.chesc2011.metrics.ProblemInstanceMetadata;
import hyflex.chesc2011.metrics.ResultsCard;
import hyflex.chesc2011.metrics.ScoreCalculator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
 * also you have to modify the problems array and cardInstances map
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
  Map<String, Double> problemsWeights = new HashMap<>() {{
      put("SAT", 1.0);
      put("TSP", 1.0);
    }
  };

  // Interval on which are the results being mapped, metric range
  public final double intervalFrom = 0.0;
  public final double intervalTo = 1.0;

  /**
   * This map represents what instances are on each line of
   * the results file.
   * It also holds the order of instances for each problem domain
   * in the results file.
   */
  @SuppressWarnings("serial")
  Map<String, List<String>> cardInstances = new HashMap<>() {{
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
   * .
   * @param id .
   */
  public void run(String id) throws Exception {
    String [] resFiles = ResultsCardHandler.getCardsNames(Paths.get(resultsPath + "/" + id));

    Map<String, ProblemInstanceMetadata> instanceMetadata = new HashMap<>();

    for (String problemId: problems) {
      Path instanceMetadataPath = Paths.get(
          metadataPath + "/" + problemId.toLowerCase() + ".metadata.xml");

      instanceMetadata.put(problemId, ProblemInstanceMetadataReader.read(instanceMetadataPath));
    }

    Map<String, ResultsCard> results = new HashMap<>();

    for (String fileName : resFiles) {
      Path resultsCardPath = Paths.get(resultsPath + "/" + id + "/" + fileName);
      
      ResultsCard algorithmResults = ResultsCardHandler.loadCard(
          problems, resultsCardPath, problems, cardInstances);

      results.put(fileName, calculateScore(algorithmResults, instanceMetadata));
    }

    saveResultsToXmlFile(results);
  }

  /**
   * .
   * @param card .
   * @param instancesMetadata .
   * @return .
   */
  private ResultsCard calculateScore(
        ResultsCard card, Map<String, ProblemInstanceMetadata> instancesMetadata) throws Exception {
    ResultsCard result = new ResultsCard(card.getName(), problems);

    for (String problemId: problems) {
        
      List<Double> scores = new ArrayList<>();
      List<Double> sizes = new ArrayList<>(); 
      for (String instanceId: cardInstances.get(problemId)) {
        double score = ScoreCalculator.getMetric(
            intervalFrom, 
            intervalTo, 
            instancesMetadata.get(problemId).get(instanceId, "optimum"), 
            instancesMetadata.get(problemId).get(instanceId, "random"), 
            card.getInstanceScore(problemId, instanceId)
        );

        result.putInstanceValue(problemId, instanceId, score);

        scores.add(score);
        sizes.add((double)instancesMetadata.get(problemId).get(instanceId, "size"));
      }
      result.calculateScore(problemsWeights);
      result.putDomainScore(problemId, ScoreCalculator.calculateWeightedMean(scores, sizes));
      
    }

    return result;
  }
  

  /**
   * Method stored the results inside xml file.
   * @param results Map with results.
   */
  private void saveResultsToXmlFile(Map<String, ResultsCard> results) 
      throws Exception {
    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
    Document document = documentBuilder.newDocument();

    // root element
    Element root = document.createElement("results");
    document.appendChild(root);

    for (String hhId: results.keySet()) {
      Element algorithm = document.createElement("algorithm");
      algorithm.setAttribute("name", hhId);

      for (String problemId: results.get(hhId).getProblems()) {
        Element problem = document.createElement("problem");
        problem.setAttribute("name", problemId);

        
        for (String instanceId: results.get(hhId).getInstances(problemId)) {
          Element instance = document.createElement("instance");

          instance.setAttribute(
              instanceId, 
              Double.toString(results.get(hhId).getInstanceScore(problemId, instanceId))
          );
          
          problem.appendChild(instance);
        }

        problem.setAttribute(
            "avg", Double.toString(results.get(hhId).getProblemScore(problemId)));
        algorithm.appendChild(problem);
      }

      root.appendChild(algorithm);
    }

    // create the xml file
    //transform the DOM Object to an XML File
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource domSource = new DOMSource(document);
    StreamResult streamResult = new StreamResult(new File(resultsXmlFile));

    transformer.transform(domSource, streamResult);
  }
}
