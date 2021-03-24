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

    File inputFile = new File("input.txt");
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
    Document doc = dbBuilder.parse(inputFile);
    doc.getDocumentElement().normalize();

    NodeList nList = doc.getElementsByTagName("Instance");
    
    for (int temp = 0; temp < nList.getLength(); temp++) {
      Node nNode = nList.item(temp);
      
      if (nNode.getNodeType() == Node.ELEMENT_NODE) {
        Element eElement = (Element) nNode;

        results.put(eElement.getAttribute("id"), Arrays.asList(
            Integer.parseInt(eElement.getAttribute("worst")), 
            Integer.parseInt(eElement.getAttribute("optimum"))
        ));
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
