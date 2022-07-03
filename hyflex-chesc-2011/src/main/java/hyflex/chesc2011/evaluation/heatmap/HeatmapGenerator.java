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

import net.mahdilamb.colormap.Colormaps;

public class HeatmapGenerator {
    // Path where the results are stored
    String resultsPath = "./results";
    // Path where the metadata are stored
    String metadataPath = "/hyflex/hyflex-chesc-2011/heatmap.template.svg";
    // Path where the file with results is stored
    String resultsSvgFile = "./results/%s/heatmap.svg";
    // Path where the file with results is stored
    String resultsXmlFile = "./results/%s/unit-metric-scores.xml";
    // Supported problems domains
    String[] supportedProblems = {"SAT", "TSP", "FSP", "QAP"};
    // Gradient colors
    String[] gradientC = {"darkred", "red", "yellow", "green", "darkgreen"};
    // Gradient color bolders
    double[] gradientV = {0, 0.5, 0.75, 0.98, 1.0};
    
    Map<String, String[]> hhInfo = new HashMap<String, String[]>() {{
        put("ACO-HH", new String[] {"José Luis Núñez", "Ant colony optimization"});
        put("AdapHH-GIHH", new String[] {"Mustafa Misir", "Genetic Iterative Hyper-heuristic"});
        put("Ant-Q", new String[] {"Imen Khamassi", ""});
        put("AVEG-Nep", new String[] {"Luca Di Gaspero", ""});
        put("BiasILS", new String[] {"Mark Johnston", ""});
        put("Clean", new String[] {"Mohamed Bader-El-Den", ""});
        put("Clean-2", new String[] {"Mohamed Bader-El-Den", ""});
        put("DynILS", new String[] {"Mark Johnston", ""});
        put("EPH", new String[] {"David Meignan", ""});
        put("GenHive", new String[] {"Michal Frankiewicz", ""});
        put("GISS", new String[] {"Alberto Acuna", ""});
        put("HAEA", new String[] {"Jonatan Gómez", ""});
        put("HAHA", new String[] {"Andreas Lehrbaum", ""});
        put("ISEA", new String[] {"Jiří Kubalík", ""});
        put("KSATS-HH", new String[] {"Kevin Sim", ""});
        put("LeanGIHH", new String[] {"Steven Adriaensen", ""});
        put("MCHH-S", new String[] {"Kent McClymont", ""});
        put("ML", new String[] {"Mathieu Larose", ""});
        put("NAHH", new String[] {"Franco Mascia", ""});
        put("PHUNTER", new String[] {"Fan Xue", ""});
        put("SA-ILS", new String[] {"He Jiang", ""});
        put("SelfSearch", new String[] {"Jawad Elomari", ""});
        put("VNS-TW", new String[] {"Ping-Che Hsiao", ""});
        put("XCJ", new String[] {"Kamran Shafi", ""});
    }};

    private class AlgorithmResult {
        String name;
        double overall;
        int overallColor;
        double score;
        String color;
        // problem instances results
        HashMap<String, Double> problemsResults;        
    }

    public static void main(String[] args) {
        HeatmapGenerator testHeatGen = new HeatmapGenerator();
        testHeatGen.buildResultsPage("96");
    }

    public HashMap<String, AlgorithmResult> loadXMLFile(String xmlPath) {
        HashMap<String, AlgorithmResult> results = new HashMap<>();

        try {
            // Read the xml file
            File xmlFile = new File(xmlPath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            // Normalize the xml structure
            doc.getDocumentElement().normalize();

            // Get the algorithms elements
            NodeList algorithmsXML = doc.getElementsByTagName("algorithm");

            // For all algorithms results
            for (int i = 0; i < algorithmsXML.getLength(); i++) {
                // Get the algorithm results
                Node algorithm = algorithmsXML.item(i);

                // Get the algorithm details
                if (algorithm.getNodeType() == Node.ELEMENT_NODE) {
                    Element algorithmElement = (Element) algorithm;
                    AlgorithmResult result = new AlgorithmResult();

                    // add each result into a new class and put it all into array or map
                    result.name = algorithmElement.getAttribute("name");
                    result.score = Double.parseDouble(String.format("%.5f", Double.parseDouble(algorithmElement.getAttribute("score"))));
                    result.overallColor = 0;


                    // Extract the algorithm results of each problem domain
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
            e.printStackTrace();
        }

        return results;
    }

    public void createPage(HashMap<String, AlgorithmResult> results, String id) throws IOException {
        Jinjava jinjava = new Jinjava();

        String template = Resources.toString(Resources.getResource(metadataPath), Charsets.UTF_8);
        String renderedTemplate = jinjava.render(template, results);

        // output the file
        String resultsSvgFilePath = String.format(resultsSvgFile, id);
        
        try(FileWriter fileWriter = new FileWriter(resultsSvgFilePath);) {    
            fileWriter.write(renderedTemplate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildResultsPage(String experimentId) {
        String xmlResultsPath = String.format(resultsXmlFile, experimentId);
        HashMap results = loadXMLFile(xmlResultsPath);
        try {
            createPage(results, experimentId);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

