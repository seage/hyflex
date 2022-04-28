/**
 * Program generates a heatmap, visualizating given data
 * from hyflex experiment
 * 
 * @author David Omrai
 */

package heatmap;

import java.util.*;

//xml libraries
// import org.w3c.dom.Document;
// import org.w3c.dom.Element;
// import org.w3c.dom.Node;
// import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

// ---------------------------------------------------

import org.w3c.dom.*;
import javax.xml.XMLConstants;



class HeatmapGenerator {
    String[] supportedProblems = {"SAT", "TSP", "FSP", "QAP"};
    Map<String, String[]> hhInfo = new HashMap<String, String[]>() {{
        put("ACO-HH", new String[] {"José Luis Núñez", "Ant colony optimization"});
        put("AdapHH-GIHH", new String[] {"", ""});
        put("Ant-Q", new String[] {"", ""});
        put("AVEG-Nep", new String[] {"", ""});
        put("BiasILS", new String[] {"", ""});
        put("Clean", new String[] {"", ""});
        put("Clean-2", new String[] {"", ""});
        put("DynILS", new String[] {"", ""});
        put("EPH", new String[] {"", ""});
        put("GenHive", new String[] {"", ""});
        put("GISS", new String[] {"", ""});
        put("HAEA", new String[] {"", ""});
        put("HAHA", new String[] {"", ""});
        put("ISEA", new String[] {"", ""});
        put("KSATS-HH", new String[] {"", ""});
        put("LeanGIHH", new String[] {"", ""});
        put("MCHH-S", new String[] {"", ""});
        put("ML", new String[] {"", ""});
        put("NAHH", new String[] {"", ""});
        put("PHUNTER", new String[] {"", ""});
        put("SA-ILS", new String[] {"", ""});
        put("SelfSearch", new String[] {"", ""});
        put("VNS-TW", new String[] {"", ""});
        put("XCJ", new String[] {"", ""});
    }};

    private class AlgorithmResult {
        String name;
        double overall;
        int overallColor;
        double score;
        // problem instances results
        HashMap<String, Double> problemsResults;        
    }

    public static void main(String[] args) {
        HeatmapGenerator hmg = new HeatmapGenerator();

        hmg.loadXMLFile("results/" + args[0] + "/unit-metric-scores.xml");
    }

    public void loadXMLFile(String xmlPath) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try ( InputStream is = readXmlFileIntoInputStream(xmlPath) ) {
            
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Get document
            Document document = db.parse(is);

            // Normalize the xml structure
            document.getDocumentElement().normalize();


            // Get all the element by the tag name
            NodeList algorithmsXML = document.getElementsByTagName("algorithm");

            LinkedList<String> problemsList = new LinkedList<String>();
            NodeList problemsXML = ((Element)algorithmsXML.item(0)).getElementsByTagName("problem");
            for (int i = 0; i < problemsXML.getLength(); i++) {
                Node problem = problemsXML.item(i);

                if (problem.getNodeType() == Node.ELEMENT_NODE) {
                    Element problemElement = (Element) problem;
                    System.out.println("Problem name: " + problemElement.getAttribute("name"));
                    for (String problemName: supportedProblems) {
                        if (problemName == problemElement.getAttribute("name")) {
                            problemsList.add(problemName);
                            break;
                        }
                    }
                }
            }




            //LinkedList<AlgorithmResult> results = new LinkedList<>();
            HashMap<String, AlgorithmResult> results = new HashMap<>();
            for (int i = 0; i < algorithmsXML.getLength(); i++) {
                Node algorithm = algorithmsXML.item(i);

                if (algorithm.getNodeType() == Node.ELEMENT_NODE) {
                    Element algorithmElement = (Element) algorithm;
                    AlgorithmResult result = new AlgorithmResult();

                    // add each result into a new class and put it all into array or map
                    result.name = algorithmElement.getAttribute("name");
                    result.score = Double.parseDouble(String.format("%.5f", Double.parseDouble(algorithmElement.getAttribute("score"))));
                    result.overallColor = 0;


                    // extract the rest
                    NodeList problems = algorithmElement.getElementsByTagName("problem");
                    result.problemsResults = new HashMap<>();

                    for (int problemId = 0; i < problems.getLength(); i++) {
                        Node problem = problems.item(problemId);

                        if (problem.getNodeType() == Node.ELEMENT_NODE) {
                            Element problemElement = (Element) problem;

                            // set the problem result parameters
                            String problemName = problemElement.getAttribute("name");
                            double problemAvg = Double.parseDouble(String.format("%.5f", Double.parseDouble(problemElement.getAttribute("avg"))));
                            // add new problem results to algorithm
                            result.problemsResults.put(problemName, problemAvg);
                        }
                    }
                    results.put(result.name, result);
                }
            }
            System.out.println(results.keySet());
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private InputStream readXmlFileIntoInputStream(final String fileName) {
        return HeatmapGenerator.class.getClassLoader().getResourceAsStream(fileName);
    }

    public static void createPage(LinkedList results, LinkedList problems, String pageDest) {
        //todo
    }

    public static void buildResultsPage(String experimentId) {
        //todo
    }
}

