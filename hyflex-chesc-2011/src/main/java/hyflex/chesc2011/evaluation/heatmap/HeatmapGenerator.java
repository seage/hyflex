/**
 * Program generates a heatmap, visualizating given data
 * from hyflex experiment
 * 
 * @author David Omrai
 */

package hyflex.chesc2011.evaluation.heatmap;

import java.util.*;

//xml libraries
// import org.w3c.dom.Document;
// import org.w3c.dom.Element;
// import org.w3c.dom.Node;
// import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

// ---------------------------------------------------

import org.w3c.dom.*;
import javax.xml.XMLConstants;
import com.hubspot.jinjava.*;

public class HeatmapGenerator {
    String[] supportedProblems = {"SAT", "TSP", "FSP", "QAP"};
    String[] gradientC = {"darkred", "red", "yellow", "green", "darkgreen"};
    double[] gradientV = {0, 0.5, 0.75, 0.98, 1.0};
    
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

        HashMap algorithmsResults = hmg.loadXMLFile("results/" + args[0] + "/unit-metric-scores.xml");

        System.out.println(algorithmsResults.keySet());
    }

    public HashMap<String, AlgorithmResult> loadXMLFile(String xmlPath) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        HashMap<String, AlgorithmResult> results = new HashMap<>();

        try ( InputStream is = readXmlFileIntoInputStream(xmlPath) ) {
            
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Get document
            Document document = db.parse(is);

            // Normalize the xml structure
            document.getDocumentElement().normalize();


            // Get all the element by the tag name
            NodeList algorithmsXML = document.getElementsByTagName("algorithm");

            
            // Following code is no longer neccessary
            // LinkedList<String> problemsList = new LinkedList<String>();
            // NodeList problemsXML = ((Element)algorithmsXML.item(0)).getElementsByTagName("problem");
            // for (int i = 0; i < problemsXML.getLength(); i++) {
            //     Node problem = problemsXML.item(i);

            //     if (problem.getNodeType() == Node.ELEMENT_NODE) {
            //         Element problemElement = (Element) problem;
            //         System.out.println("Problem name: " + problemElement.getAttribute("name"));
            //         for (String problemName: supportedProblems) {
            //             if (problemName == problemElement.getAttribute("name")) {
            //                 problemsList.add(problemName);
            //                 break;
            //             }
            //         }
            //     }
            // }


            //LinkedList<AlgorithmResult> results = new LinkedList<>();
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
        } catch (Exception e) {
            System.out.println(e);
        }
        return results;
    }

    private InputStream readXmlFileIntoInputStream(final String fileName) {
        return HeatmapGenerator.class.getClassLoader().getResourceAsStream(fileName);
    }

    public static void createPage(Map<String, AlgorithmResult> results, String pageDest) throws Exception {
        Jinjava jinjava = new Jinjava();

        String template = Resources.toString(Resources.getResource("heatmap.template.svg"), Charsets.UTF_8);
        String renderedTemplate = jinjava.render(template, results);

        // output the file
        try {
            FileWriter fileWriter = new FileWriter(pageDest);
            fileWriter.write(renderedTemplate);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void buildResultsPage(String experimentId) {
        //todo
    }
}

