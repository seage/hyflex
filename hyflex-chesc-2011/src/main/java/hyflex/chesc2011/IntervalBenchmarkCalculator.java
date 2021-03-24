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

/*
 * @author David Omrai
 */

public class IntervalBenchmarkCalculator {
  String resultsPath = "./results";

  final int intervalFrom = 0;
  final int intervalTo = 1000;

  @SuppressWarnings("serial")
  HashMap<String, List<String>> cardInstances = new HashMap<String, List<String>>() {{
        put("SAT", new ArrayList<>(Arrays.asList("3", "5", "4", "10", "11")));
        put("TSP", new ArrayList<>(Arrays.asList("0", "8", "2", "7", "6")));
    }
  };

  public static void main(String[] args){
  }

  public void run(String id){

  }

  private HashMap<String, List<Integer>> loadCard(String path) {

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

  private Double getMetric(double worst, double best, double current){
    return mapToInterval(worst, best, intervalFrom, intervalTo, current);
  }

  private double mapToInterval(
      double sourceFrom, double sourceTo, double mapFrom, double mapTo, double value) {
    return mapFrom + ((mapTo - mapFrom) / (sourceTo - sourceTo)) * (value - sourceFrom);
  }
}
