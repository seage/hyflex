package hyflex.chesc2011;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/*
 * @author David Omrai
 */

public class IntervalBenchmarkCalculator {
  String resultsPath = "./results";

  final int intervalFrom = 0;
  final int intervalTo = 1000;

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

  public static void main(String[] args){
    if (args.length <= 0) {
      return;
    }

    try {
      IntervalBenchmarkCalculator ibc = new IntervalBenchmarkCalculator();
      ibc.run(args[0]);
    } catch (Exception e) {
      System.err.println(e.getStackTrace());
    }
  }

  public void run(String id) throws Exception{
    if (isIdThere(id) == false) {
      return;
    }


  }

  private Boolean isIdThere(String id) {
    //get the path to results folder
    final String resultsDirPath = Optional.ofNullable(
        System.getenv("RESULTS_DIR")).orElse(resultsPath);
    
    File resultsDir = new File(resultsDirPath);

    //get all subdrectories from the results directory
    String[] directories = resultsDir.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return new File(current, name).isDirectory();
      }
    });

    //does results directory exists
    if (directories == null) {
      System.out.println("WARNING, directory " + resultsPath + " doesn't exists.");
      return false;
    }

    //is id directory in results folder
    if (Arrays.asList(directories).contains(id)) {
      directories = new String[]{id};
    } else if (id != "") {
      System.out.println("WARNING, directory " + resultsPath + "/" + id + " doesn't exists.");
      return false;
    }

    return true;
  }

  private HashMap<String, List<Integer>> loadCard(String path) throws Exception{

    return null;
  }

  private HashMap<String, List<Integer>> readXmlFile(String path) throws Exception {
    HashMap<String, List<Integer>> results = new HashMap<String, List<Integer>>();

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

        results.put(element.getAttribute("id"), new ArrayList<Integer>(Arrays.asList(
            Integer.parseInt(element.getAttribute("random")), 
            Integer.parseInt(element.getAttribute("optimum"))
        )));
      }    
    }

    return results;
  }

  private Double getMetric(double worst, double best, double current) throws Exception {
    return mapToInterval(worst, best, intervalFrom, intervalTo, current);
  }

  private double mapToInterval(
      double sourceFrom, double sourceTo, double mapFrom, double mapTo, double value)
      throws Exception {
    return mapFrom + ((mapTo - mapFrom) / (sourceTo - sourceTo)) * (value - sourceFrom);
  }
}
