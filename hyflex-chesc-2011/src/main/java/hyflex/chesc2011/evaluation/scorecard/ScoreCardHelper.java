package hyflex.chesc2011.evaluation.scorecard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// todo

/**
 * Class is used for retrieving the data from score card file.
 * 
 * @author David Omrai
 */
public class ScoreCardHelper {
  private static final Logger logger = 
      Logger.getLogger(ScoreCardHelper.class.getName());
  /**
   * Array holds information on which line of the result card is which problem domain.
   */
  static String[] cardProblemsOrder = {
      "SAT", // 0
      "BP", // 1
      "PS", // 2
      "FSP", // 3
      "TSP", // 4
      "VRP", // 5
      "QAP", // 6
  };

  /**
   * Method reads the results file and stores the data into a map.
   * 
   * @param path Path where the file is stored.
   * @return Map with algorithm results.
   */
  public static ScoreCard loadCard(String[] problems, Path path, 
      Map<String, List<String>> problemInstances, List<String> implementedProblems) 
      throws Exception {
    logger.info("Loading the card...");

    // Name of the file
    String cardName = path.getFileName().toString();
    ScoreCard result = 
        new ScoreCard(cardName.substring(0, cardName.lastIndexOf(".")), cardProblemsOrder);

    try (Scanner scanner = new Scanner(new File(path.toString()))) {
      scanner.useDelimiter("\n");
      for (String problemId : cardProblemsOrder) {

        if (scanner.hasNextLine() == false) {
          throw new Exception("Not enough lines in " + path.toString() + " file.");
        }
        String line = scanner.nextLine();

        if (Arrays.stream(problems).anyMatch(problemId::equals)) {
          if (!line.contains("---")) {
            implementedProblems.add(problemId);
            try (Scanner lineScanner = new Scanner(line).useDelimiter(", ")) {
              for (String instanceId : problemInstances.get(problemId)) {
    
                if (lineScanner.hasNextLine() == false) {
                  throw new Exception(
                    "Not enough instances results in " + path.toString() + " file.");
                }
                logger.info("Calculating the " + problemId + " " + instanceId + ".");
                result.putInstanceScore(
                    problemId, instanceId, Double.parseDouble(lineScanner.next()));
              }
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Method returns the array of cards names.
   * 
   * @param path Path to the results cards.
   * @return Returns the array of algorithms results file names..
   */
  public static String[] getCardsNames(Path path) {
    File resDir = new File(path.toString());
    return resDir.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return name.toLowerCase().endsWith(".txt");
      }
    });
  }

  /**
   * Method creates the json string with results data.
   * @param results List with algorithm results.
   * @throws IOException Exception when creating the file.
   */
  public static String createResultsJsonString(List<ScoreCard> results) {
    // Create json array for results
    JSONArray resultsArray = new JSONArray();
    // Insert the keys and values
    for (ScoreCard resultCard : results) {
      JSONObject algorithm = new JSONObject();
      // Name of the algorithm
      algorithm.put("algorithmName", resultCard.getName());
      // Total score of the algorithm
      algorithm.put("totalScore", resultCard.getScore());

      JSONObject instances = new JSONObject();
      JSONObject problems = new JSONObject();
      for (String problemId : resultCard.getProblems()) {
        problems.put(problemId, resultCard.getProblemScore(problemId));


        JSONObject problemInstances = new JSONObject();
        // Store instances' scores
        for (String instanceId : resultCard.getInstances(problemId)) {
          problemInstances.put(instanceId, resultCard.getInstanceScore(problemId, instanceId));
        }
        instances.put(problemId, problemInstances);
      }
      algorithm.put("scorePerInstance", instances);
      algorithm.put("scorePerProblem", problems);

      resultsArray.put(algorithm);
    }

    // Create the json results file
    JSONObject jsonResults = new JSONObject();
    jsonResults.put("results", resultsArray);
    return jsonResults.toString(2);
  }

  /**
   * Method stores the results to the json file.
   * @param resultsJsonFile Path to the json result file.
   * @param results List with algorithm results.
   * @throws IOException Exception when creating the file.
   */
  public static void saveResultsToJsonFile(
      String resultsJsonFile, List<ScoreCard> results
  ) throws IOException {
    // Store the file
    try (FileWriter fw = new FileWriter(resultsJsonFile)) {
      fw.write(createResultsJsonString(results));
    }
  }

  /**
   * Method stores the results inside xml file.
   * 
   * @param results Map with results.
   */
  public static void saveResultsToXmlFile(String resultsXmlFile, List<ScoreCard> results)
      throws Exception {
    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
    Document document = documentBuilder.newDocument();

    // root element
    Element root = document.createElement("results");
    document.appendChild(root);

    for (ScoreCard resultCard : results) {
      Element algorithm = document.createElement("algorithm");
      algorithm.setAttribute("name", resultCard.getName());
      algorithm.setAttribute("score", Double.toString(resultCard.getScore()));

      for (String problemId : resultCard.getProblems()) {
        Element problem = document.createElement("problem");
        problem.setAttribute("name", problemId);


        for (String instanceId : resultCard.getInstances(problemId)) {
          Element instance = document.createElement("instance");

          instance.setAttribute(instanceId,
              Double.toString(resultCard.getInstanceScore(problemId, instanceId)));

          problem.appendChild(instance);
        }

        problem.setAttribute("avg", Double.toString(resultCard.getProblemScore(problemId)));
        algorithm.appendChild(problem);
      }

      root.appendChild(algorithm);
    }

    // create the xml file
    // transform the DOM Object to an XML File
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    DOMSource domSource = new DOMSource(document);
    StreamResult streamResult =
        new StreamResult(new PrintWriter(new FileOutputStream(new File(resultsXmlFile), false)));

    transformer.transform(domSource, streamResult);
  }
}
