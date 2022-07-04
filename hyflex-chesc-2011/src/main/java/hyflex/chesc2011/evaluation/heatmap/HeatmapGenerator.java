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
import java.util.stream.Collectors;

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
import net.mahdilamb.colormap.Colors;
import net.mahdilamb.colormap.SequentialColormap;
import java.awt.Color;

public class HeatmapGenerator {
    // Path where the results are stored
    String resultsPath = "./results";
    // Path where the metadata are stored
    String metadataPath = "/hyflex/hyflex-chesc-2011/heatmap.template.svg";
    // Path where the file with results is stored
    String resultsSvgFile = "./results/%s/heatmap.svg";
    // Path where the file with results is stored
    String resultsXmlFile = "./results/%s/unit-metric-scores.xml";
    
    // Gradient colors
    private Color[][] gradColors = {
        {new Color(128,0,0), new Color(255,0,0)}, // dark red - red
        {new Color(255,0,0), new Color(255,255,0)}, // red - yellow
        {new Color(255,255,0), new Color(0,128,0)}, // yellow - green
        {new Color(0,128,0), new Color(0,100,0)}, // green - dark green
    };
    private SequentialColormap[] gradMaps = {
        new SequentialColormap(gradColors[0]),
        new SequentialColormap(gradColors[1]),
        new SequentialColormap(gradColors[2]),
        new SequentialColormap(gradColors[3])
    };
    // Gradient color bolders
    double[] gradBorders = {0.5, 0.75, 0.98, 1.0};

    String[] supportedProblems = {"SAT", "TSP", "FSP", "QAP"};
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

    private class AlgorithmProblemResult {
        String name;
        double score;
        Color color;
    }
    private class AlgorithmResult {
        String name;
        Color color;
        double score;
        // problem instances results
        HashMap<String, AlgorithmProblemResult> problemsResults;        
    }
    private class SVGData {
        HashMap<String, AlgorithmResult> results;
        // Supported problems domains
    }

    public static void main(String[] args) {
        HeatmapGenerator testHeatGen = new HeatmapGenerator();
        testHeatGen.buildResultsPage("96");
    }

    public Color getColor(Double pos) {
        // Find appropriate gradient
        int colPos;
        for (colPos = 0; colPos < gradBorders.length; colPos++) {
            if (pos <= gradBorders[colPos]) {
                break;
            }
        }

        // Scale old position
        double fromPos = colPos == 0 ? 0.0 : gradBorders[colPos - 1];
        double toPos = gradBorders[colPos];

        double newPos = (pos-fromPos)/(toPos-fromPos);

        // Return the color
        return gradMaps[colPos].get(newPos);
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
                    result.color = getColor(result.score);


                    // Extract the algorithm results of each problem domain
                    NodeList problems = algorithmElement.getElementsByTagName("problem");
                    result.problemsResults = new HashMap<>();

                    for (int problemId = 0; i < problems.getLength(); i++) {
                        Node problem = problems.item(problemId);

                        if (problem.getNodeType() == Node.ELEMENT_NODE) {
                            Element problemElement = (Element) problem;

                            // Create new structure
                            AlgorithmProblemResult newRes = new AlgorithmProblemResult();
                            // set the problem result parameters
                            newRes.name = problemElement.getAttribute("name");
                            newRes.score = Double.parseDouble(String.format("%.5f", Double.parseDouble(problemElement.getAttribute("avg"))));
                            newRes.color = getColor(newRes.score);
                            // add new problem results to algorithm
                            result.problemsResults.put(newRes.name, newRes);
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

    public void createPage(SVGData sData, String id) throws IOException {
        Jinjava jinjava = new Jinjava();
        Map<String, Object> context = new HashMap<>();
        context.put("results", sData.results);
        context.put("problems", supportedProblems);


        String template = Resources.toString(Resources.getResource(metadataPath), Charsets.UTF_8);
        String renderedTemplate = jinjava.render(template, context);

        // output the file
        String resultsSvgFilePath = String.format(resultsSvgFile, id);
        
        try(FileWriter fileWriter = new FileWriter(resultsSvgFilePath);) {    
            fileWriter.write(renderedTemplate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildResultsPage(String experimentId) {
        SVGData sData = new SVGData();
        String xmlResultsPath = String.format(resultsXmlFile, experimentId);
        sData.results = loadXMLFile(xmlResultsPath);

        System.out.println(sData.results.get("Clean").color);
        // try {
        //     createPage(results, experimentId);
        // } catch (IOException ioe) {
        //     ioe.printStackTrace();
        // }
    }
}

