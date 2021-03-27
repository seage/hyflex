/**
 * @author David Omrai
 */

package hyflex.chesc2011;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    if (args.length <= 0) {
      return;
    }

    try {
      BenchmarkMetricCalculator ibc = new BenchmarkMetricCalculator();
      ibc.run(args[0]);
    } catch (Exception e) {
      System.out.println("error");
      System.out.println(e.getStackTrace());
    }
  }

  /**
   * This method finds and loads all results files and metadata.
   * For each algorithm results calculates the metric and stores it
   * inside xml file.
   * @param id .
   */
  public void run(String id) throws Exception {
    Path resultsDirPath = Paths.get(resultsPath + "/" + id);

    if (doesDirExists(resultsDirPath) == false) {
      return;
    }

    File resDir = new File(resultsDirPath.toString());
    String[] resFiles = resDir.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return new File(current, name).isFile();
      }
    });

    if (resFiles == null) {
      System.out.println("There are no files inside " + resultsDirPath.toString() + " directory");
      return;
    }

    /**
     * First string: problemId.
     * Second string: instanceId.
     * Third string: [optimum, random, size]
     * Double: Parameter value.
     */
    Map<String, Map<String,Map<String, Double>>> metadata = loadMetadata();

    /**
     * First String: fileName.
     * Second String: problemId
     * Third String: instanceId
     * Double: instance metric
     */
    Map<String, 
        Map<String, Map<String, Map<String, Double>>>> results = new HashMap<>();

    for (String fileName : resFiles) {
      /**
       * First String: problemId.
       * Second String: instanceId.
       * Double: instance result.
       */
      Map<String, Map<String, Double>> algorithmResults = loadCard(
          resultsDirPath.toString() + "/" + fileName);

      if (algorithmResults == null) {
        continue;
      }

      /**
       * First String: problemId.
       * Second String: instanceId.
       * Third String: [metric, size].
       * Double: metric or size value.
       */
      Map<String, Map<String, Map<String, Double>>> probRes = new HashMap<>();

      for (String problemId: problems) {
        
        Map<String, Map<String, Double>> instRes = new HashMap<>();

        for (String instanceId: cardInstances.get(problemId)) {
          Map<String, Double> instance = new HashMap<>();
          instance.put("metric", getMetric(
              metadata.get(problemId).get(instanceId).get("optimum"),
              metadata.get(problemId).get(instanceId).get("random"),
              algorithmResults.get(problemId).get(instanceId)
          ));
          instance.put(
              "size", (double)metadata.get(problemId).get(instanceId).get("size"));
          
          instRes.put(instanceId, instance);
        }

        probRes.put(problemId, instRes);
      }
      
      results.put(fileName, probRes);
    }

    saveResultsToXmlFile(results);
  }

  /**
   * Method reads the results file and stores the data into a map.
   * @param path Path where the file is stored.
   * @return Map with algorithm results.
   */
  private Map<String, Map<String, Double>> loadCard(String path)
      throws Exception {
    Map<String, Map<String, Double>> results = new HashMap<>();
    
    Scanner scanner = new Scanner(new File(path)).useDelimiter("\n");

    for (String problemId : problems) {
      // System.out.println(problemId);
      
      if (scanner.hasNextLine() == false) {
        scanner.close();
        System.out.println("Not enough lines in " + path + " file.");
        return null;
      }

      Map<String, Double> result = new HashMap<>();

      Scanner line = new Scanner(scanner.nextLine()).useDelimiter(", ");

      for (String instanceId: cardInstances.get(problemId)) {

        if (line.hasNextLine() == false) {
          line.close();
          System.out.println("Not enough instances results in " + path + " file.");
          return null;
        }
        
        result.put(instanceId, Double.parseDouble(line.next()));
      }

      line.close();
      results.put(problemId, result);
    }
    scanner.close();

    return results;
  }

  /**
   * Method reads the file with metadata and stores the data inside map.
   * @param path Path where the metadata is stored.
   * @return Returns the map with metadata data.
   */
  private Map<String, Map<String, Double>> readMetadata(Path path) throws Exception {
    Map<String, Map<String, Double>> results = new HashMap<>();
    // Read the input file
    Document doc = DocumentBuilderFactory
        .newInstance().newDocumentBuilder().parse(getClass().getResourceAsStream(path.toString()));
    doc.getDocumentElement().normalize();

    // Get all instances from the file
    NodeList nodeList = doc.getElementsByTagName("Instance");
    
    // For each instance stores its values into a hash map
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node node = nodeList.item(temp);
      
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;

        
        if (isDouble(element.getAttribute("optimum")) == false) {
          continue;
        }
        if (isDouble(element.getAttribute("random")) == false) {
          continue;
        }
        if (isDouble(element.getAttribute("size")) == false) {
          continue;
        }

        Map<String, Double> result = new HashMap<>();

        result.put("optimum", Double.parseDouble(element.getAttribute("optimum")));
        result.put("random", Double.parseDouble(element.getAttribute("random")));
        result.put("size", Double.parseDouble(element.getAttribute("size")));

        results.put(element.getAttribute("id"), result);
      }    
    }
    return results;
  }

  /**
   * Method stored the results inside xml file.
   * @param results Map with results.
   */
  private void saveResultsToXmlFile(Map<String, 
      Map<String, Map<String, Map<String, Double>>>> results) 
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

      for (String problemId: results.get(hhId).keySet()) {
        Element problem = document.createElement("problem");
        problem.setAttribute("name", problemId);

        /**
         *                  SUM(i=0|n)[size(instance-i)*metric(instance-i)]   .
         * weighted mean = -------------------------------------------------  .
         *                            SUM(i=0|n)[size(instance-i)]            .
         */
        double numerator = 0;
        double nominator = 0;
        for (String isntanceId: results.get(hhId).get(problemId).keySet()) {
          Element instance = document.createElement("instance");
          instance.setAttribute(
              isntanceId, 
              Double.toString(results.get(hhId).get(problemId).get(isntanceId).get("metric")));
          
          numerator += (
              results.get(hhId).get(problemId).get(isntanceId).get("size") 
              * results.get(hhId).get(problemId).get(isntanceId).get("metric"));
          nominator += results.get(hhId).get(problemId).get(isntanceId).get("size");
          
          problem.appendChild(instance);
        }

        problem.setAttribute(
            "avg", Double.toString(numerator / nominator));
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

  /**
   * Method is uesd by other methods for retrieval of metadata.
   * @return The map with metadata data.
   */
  private Map<String, Map<String, Map<String, Double>>> loadMetadata() 
      throws Exception {    
    Map<String, Map<String, Map<String, Double>>> results = new HashMap<>();

    for (String problemId: problems) {
      results.put(
          problemId, 
          readMetadata(Paths.get(metadataPath + "/" + problemId.toLowerCase() + ".metadata.xml")));
    }

    return results;
  }

  /**
   * Method tests if the given path leads to directory.
   * @param path Path to directory.
   * @return True if path leads to directory, false otherwise.
   */
  private Boolean doesDirExists(Path path) {
    return new File(path.toString()).exists();
  }

  /**
   * Method tests given string if it contains double.
   * @param text String to test.
   * @return True if the string can be translated to double, false otherwise.
   */
  private Boolean isDouble(String text) {
    try {
      Double.parseDouble(text);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Method returns the metric based on given data.
   * @param upperBound The value of random generator.
   * @param lowerBound The optimal value.
   * @param current Input value for metric.
   * @return The metric for given value.
   */
  public double getMetric(double lowerBound, double upperBound, double current) 
      throws Exception {
    if (upperBound < 0 || lowerBound < 0 || current < 0) {
      throw new Exception("Bad input values: input parameter < 0");
    }
    if (upperBound < lowerBound) {
      throw new Exception("Bad input values: upperBound < lowerBound");
    }
    if (current < lowerBound || current > upperBound) {
      throw new Exception("Bad input values: current is not from interval");
    }

    return intervalTo - (mapToInterval(lowerBound, upperBound, intervalFrom, intervalTo, current));
  }

  /**
   * Method maps the value of one interval onto a new one.
   * @param lowerBound Lower value of the first interval.
   * @param upperBound Upper value of the first interval.
   * @param intervalLower Lower value of the new interval.
   * @param intervalUpper Upper value of the new interval.
   * @param value Value to map to a new interval.
   * @return Return the mapped value of the value.
   */
  private double mapToInterval(
      double lowerBound, 
      double upperBound, double intervalLower, double intervalUpper, double value)
      throws Exception {
    double valueNormalization = (value - lowerBound) * (1 / (upperBound - lowerBound));
    double scaling = valueNormalization * (intervalUpper - intervalLower);
    double shifting = scaling + intervalLower;
    
    return shifting;
  }
}
