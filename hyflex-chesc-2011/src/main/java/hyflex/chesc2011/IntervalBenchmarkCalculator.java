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
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.StringCharacterIterator;
import java.util.Collections;
import java.util.Optional;

import java.util.Scanner;

/*
 * @author David Omrai
 */

public class IntervalBenchmarkCalculator {
  String resultsPath = "./results";
  String metadataPath = "./hyflex-chesc-2011/src/main/resources/hyflex/hyflex-chesc-2011";

  String[] problems = {"SAT", "TSP"};

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

    HashMap<String, HashMap<String, Integer>> results = new HashMap<>();

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

    int filesNumber = resFiles.length;

    HashMap<String, HashMap<String, List<Integer>>> metadata = loadMetadata();

    for (String fileName : resFiles) {
      HashMap<String, HashMap<String, Double>> hm = loadCard(
          resultsPath + "/" + id + "/" + fileName);

      if (hm == null) {
        continue;
      }

      for (String problemId: problems){
        
        for (String instanceId: cardInstances.get(problemId)) {
          System.out.println(problemId + " " + instanceId);

          // System.out.println(metadata.get(problemId).get(instanceId).get(0));
          // System.out.println(metadata.get(problemId).get(instanceId).get(1));
          // System.out.println(hm.get(problemId).get(instanceId));
          System.out.println(getMetric(
              metadata.get(problemId).get(instanceId).get(0),
              metadata.get(problemId).get(instanceId).get(1),
              hm.get(problemId).get(instanceId)
          ));
        }
        
      }
    }
  }

  private HashMap<String, HashMap<String, List<Integer>>> loadMetadata() throws Exception {
    HashMap<String, HashMap<String, List<Integer>>> results = new HashMap<>();

    for (String problemId: problems) {
      results.put(
          problemId, readXmlFile(metadataPath + "/" + problemId.toLowerCase() + ".metadata.xml"));
    }

    return results;
  }

  private Boolean doesDirExists(String path) {
    return new File(path).exists();
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
        // System.out.println(instanceId);

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


  private HashMap<String, List<Integer>> readXmlFile(String path) throws Exception {
    HashMap<String, List<Integer>> results = new HashMap<String, List<Integer>>();
    // System.out.println(path);
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

        // System.out.println(element.getAttribute("optimum"));
        // System.out.println(element.getAttribute("random"));

        results.put(element.getAttribute("id"), new ArrayList<Integer>(Arrays.asList(
            Integer.parseInt(element.getAttribute("optimum")), 
            Integer.parseInt(element.getAttribute("random"))
        )));
      }    
    }

    return results;
  }

  private Boolean isInteger(String text) {
    try {
      Integer.parseInt(text);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private Double getMetric(int worst, int best, double current) 
      throws Exception {
    return mapToInterval(worst, best, intervalFrom, intervalTo, current);
  }

  private double mapToInterval(
      double sourceFrom, double sourceTo, double mapFrom, double mapTo, double value)
      throws Exception {
    return mapFrom + ((mapTo - mapFrom) / (sourceTo - sourceFrom)) * (value - sourceFrom);
  }
}
