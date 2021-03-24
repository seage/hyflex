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

  public void run(String id) throws Exception{
    System.out.println("run");
    if (doesDirExists(resultsPath + "/" + id) == false) {
      return;
    }
    System.out.println("results are there");
    if (doesDirExists(metadataPath) == false) {
      return;
    }
    System.out.println("metadata are there");

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

    for (String fileName : resFiles) {
      System.out.println(resultsPath + "/" + id + "/" + fileName);
      HashMap<String, HashMap<String, Double>> hm = loadCard(
          resultsPath + "/" + id + "/" + fileName);

      System.out.println(hm.values());
      
    }
  }

  private Boolean doesDirExists(String path) {
    return new File(path).exists();
  }

  private HashMap<String, HashMap<String, Double>> loadCard(String path) 
      throws Exception {
    HashMap<String, HashMap<String, Double>> results = new HashMap<>();
    
    Scanner scanner = new Scanner(new File(path)).useDelimiter("\n");

    for (String problemId : problems) {
      System.out.println(problemId);
      
      if (scanner.hasNextLine() == false) {
        scanner.close();
        throw new Exception("Not enough lines in " + path + " file.");
      }

      HashMap<String, Double> result = new HashMap<>();

      Scanner line = new Scanner(scanner.nextLine()).useDelimiter(", ");

      for (String instanceId: cardInstances.get(problemId)) {
        System.out.println(instanceId);


        if (line.hasNextLine() == false) {
          line.close();
          throw new Exception("Not enough instances results in " + path + " file.");
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

  private Double getMetric(double worst, double best, double current) 
      throws Exception {
    return mapToInterval(worst, best, intervalFrom, intervalTo, current);
  }

  private double mapToInterval(
      double sourceFrom, double sourceTo, double mapFrom, double mapTo, double value)
      throws Exception {
    return mapFrom + ((mapTo - mapFrom) / (sourceTo - sourceTo)) * (value - sourceFrom);
  }
}
