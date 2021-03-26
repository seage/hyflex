/**
 * @author David Omrai
 */

package hyflex.chesc2011;

import java.io.File;
import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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


public class IntervalBenchmarkCalculator {
  String resultsPath = "./results";
  String metadataPath = "./hyflex-chesc-2011/src/main/resources/hyflex/hyflex-chesc-2011";
  String resultsXmlFile = "./results.xml";

  String[] problems = {"SAT", "TSP"};

  public final double intervalFrom = 0.0;
  public final double intervalTo = 1.0;

  @SuppressWarnings("serial")
  HashMap<String, List<String>> cardInstances = new HashMap<String, List<String>>() {{
        put("SAT", new ArrayList<>(
            Arrays.asList(
              "hyflex-sat-3", "hyflex-sat-5", "hyflex-sat-4", "hyflex-sat-10", "hyflex-sat-11")));
        put("TSP", new ArrayList<>(
            Arrays.asList(
              "hyflex-tsp-0", "hyflex-tsp-8", "hyflex-tsp-2", "hyflex-tsp-7", "hyflex-tsp-6")));
    }
  };

  /**
   * .
   * @param args .
   */
  public static void main(String[] args) {
    // Uncomment when its finished
    // if (args.length <= 0) {
    //   return;
    // }

    try {
      IntervalBenchmarkCalculator ibc = new IntervalBenchmarkCalculator();
      ibc.run("1");
    } catch (Exception e) {
      System.out.println("error");
      System.out.println(e.getStackTrace());
    }
  }

  /**
   * .
   * @param id .
   */
  public void run(String id) throws Exception {
    if (doesDirExists(resultsPath + "/" + id) == false) {
      return;
    }
    
    if (doesDirExists(metadataPath) == false) {
      return;
    }

    //HashMap<String, HashMap<String, Integer>> results = new HashMap<>();

    File resDir = new File(resultsPath + "/" + id);
    String[] resFiles = resDir.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return new File(current, name).isFile();
      }
    });

    if (resFiles == null) {
      System.out.println("There are no files inside " + resultsPath + "/" + id + " directory");
      return;
    }

    HashMap<String, HashMap<String,HashMap<String, Integer>>> metadata = loadMetadata();

    HashMap<String, 
        HashMap<String, HashMap<String, HashMap<String, Double>>>> results = new HashMap<>();

    for (String fileName : resFiles) {
      HashMap<String, HashMap<String, Double>> hm = loadCard(
          resultsPath + "/" + id + "/" + fileName);

      if (hm == null) {
        continue;
      }

      HashMap<String, HashMap<String, HashMap<String, Double>>> probRes = new HashMap<>();

      for (String problemId: problems) {
        
        HashMap<String, HashMap<String, Double>> instRes = new HashMap<>();

        for (String instanceId: cardInstances.get(problemId)) {
          HashMap<String, Double> instance = new HashMap<>();
          instance.put("metric", getMetric(
              metadata.get(problemId).get(instanceId).get("random"),
              metadata.get(problemId).get(instanceId).get("optimum"),
              hm.get(problemId).get(instanceId)
          ));
          instance.put(
              "size", (double)metadata.get(problemId).get(instanceId).get("size"));
          
          instRes.put(instanceId, instance);
        }

        probRes.put(problemId, instRes);
      }
      
      results.put(fileName, probRes);
    }

    makeXmlFile(results);
  }


  private HashMap<String, HashMap<String, Double>> loadCard(String path)
      throws Exception {
    HashMap<String, HashMap<String, Double>> results = new HashMap<>();
    
    Scanner scanner = new Scanner(new File(path)).useDelimiter("\n");

    for (String problemId : problems) {
      // System.out.println(problemId);
      
      if (scanner.hasNextLine() == false) {
        scanner.close();
        System.out.println("Not enough lines in " + path + " file.");
        return null;
      }

      HashMap<String, Double> result = new HashMap<>();

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


  private HashMap<String, HashMap<String, Integer>> readXmlFile(String path) throws Exception {
    HashMap<String, HashMap<String, Integer>> results = new HashMap<>();
    // Load the input file
    File inputFile = new File(path);
    // Read the input file
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputFile);
    doc.getDocumentElement().normalize();

    // Get all instances from the file
    NodeList nodeList = doc.getElementsByTagName("Instance");
    
    // For each instance stores its values into a hash map
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node node = nodeList.item(temp);
      
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;

        
        if (isInteger(element.getAttribute("optimum")) == false) {
          continue;
        }
        if (isInteger(element.getAttribute("random")) == false) {
          continue;
        }
        if (isInteger(element.getAttribute("size")) == false) {
          continue;
        }

        HashMap<String, Integer> result = new HashMap<>();

        result.put("optimum", Integer.parseInt(element.getAttribute("optimum")));
        result.put("random", Integer.parseInt(element.getAttribute("random")));
        result.put("size", Integer.parseInt(element.getAttribute("size")));

        results.put(element.getAttribute("id"), result);
        // results.put(element.getAttribute("id"), new ArrayList<Integer>(Arrays.asList(
        //     Integer.parseInt(element.getAttribute("optimum")), 
        //     Integer.parseInt(element.getAttribute("random"))
        // )));
      }    
    }
    return results;
  }

  private void makeXmlFile(HashMap<String, 
      HashMap<String, HashMap<String, HashMap<String, Double>>>> results) 
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
         *                  SUMi=0[size(instance-i)*metric(instance-i)]   .
         * weighted mean = ---------------------------------------------  .
         *                            SUMi=0[size(instance-i)]            .
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

  private HashMap<String, HashMap<String, HashMap<String, Integer>>> loadMetadata() 
      throws Exception {
    HashMap<String, HashMap<String, HashMap<String, Integer>>> results = new HashMap<>();

    for (String problemId: problems) {
      results.put(
          problemId, readXmlFile(metadataPath + "/" + problemId.toLowerCase() + ".metadata.xml"));
    }

    return results;
  }

  private Boolean doesDirExists(String path) {
    return new File(path).exists();
  }

  private Boolean isInteger(String text) {
    try {
      Integer.parseInt(text);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * .
   * @param worst .
   * @param best .
   * @param current .
   * @return
   */
  public double getMetric(int worst, int best, double current) 
      throws Exception {
    if (worst < 0 || best < 0 || current < 0) {
      throw new Exception("Bad input values - input parameter < 0");
    }
    if (worst < best) {
      throw new Exception("Bad input values - worst < best");
    }
    if (current < best || current > worst) {
      throw new Exception("Bad input values - current is not from interval");
    }

    return intervalTo - (mapToInterval(best, worst, intervalFrom, intervalTo, current));
  }

  private double mapToInterval(
      double sourceFrom, double sourceTo, double mapFrom, double mapTo, double value)
      throws Exception {
    return mapFrom + ((mapTo - mapFrom) / (sourceTo - sourceFrom)) * (value - sourceFrom);
  }
}
