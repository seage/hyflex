/**
 * Program generates a heatmap, visualizating given data
 * from hyflex experiment
 * 
 * @author David Omrai
 */

package hyflex.chesc2011.evaluation.heatmap;

import java.util.*;

//xml libraries

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import com.hubspot.jinjava.Jinjava;
import java.io.File;

// ---------------------------------------------------

import org.w3c.dom.*;
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
    // Gradient maps - for each two colors on the spectrum
    private SequentialColormap[] gradMaps = {
        new SequentialColormap(gradColors[0]),
        new SequentialColormap(gradColors[1]),
        new SequentialColormap(gradColors[2]),
        new SequentialColormap(gradColors[3])
    };
    // Gradient color bolders
    double[] gradBorders = {0.5, 0.75, 0.98, 1.0};
    // Info about each hyper-heuristic - name and author
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
    // Sorted list of hhs results
    List<AlgorithmResult> results;
    // List of problems
    List<String> problems;
    // Algorithms overall results
    List<List<String>> algsOverRes;
    // Algorithms problems results
    List<List<List<String>>> algsProbsRes;

    /**
     * Class represents a structure where are data
     * about problem results stored 
     */
    private class AlgorithmProblemResult {
        String name;
        double score;
        Color color;
        // Red color 0-255
        int rColor;
        // Green color 0-255
        int gColor;
        // Blue color 0-255
        int bColor;
    }
    /**
     * Class represents a structure where are data
     * about overall algorithm stored
     */
    private class AlgorithmResult {
        String name;
        double score;
        String author;
        Color color;
        int rColor;
        int gColor;
        int bColor;
        // problem instances results
        HashMap<String, AlgorithmProblemResult> problemsResults;
    }

    /**
     * Method is meant for testing
     * @param args Input arguments
     */
    public static void main(String[] args) {
        HeatmapGenerator testHeatGen = new HeatmapGenerator();
        testHeatGen.buildResultsPage("96");
    }

    /**
     * Method turns the given position into a 
     * specific color based on the location on
     * the gradient
     * @param pos double value in range [0,1]
     * @return A appropriate color on the gradient
     */
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

    /**
     * Method sorts the results list using the 
     * hhs overall scores
     */
    public void sortResults() {
        // Sort the results by their overall score
        Collections.sort(results, new Comparator<AlgorithmResult>() {
            @Override
            public int compare(AlgorithmResult lar, AlgorithmResult rar) {
                return lar.score > rar.score ? -1: (lar.score < rar.score) ? 1 : 0;
            }
        });
    }

    /**
     * Method loads the xml file
     * @param xmlPath path to the xml file
     * @return A list of algorithm results
     */
    public void loadXMLFile(String xmlPath) {
        results = new ArrayList<>();
        try {
            // Read the xml file
            File xmlFile = new File(xmlPath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            // Normalize the xml structure
            doc.getDocumentElement().normalize();

            // Element
            Element root = doc.getDocumentElement();
            // Get the algorithms elements
            NodeList algorithmsXML = root.getElementsByTagName("algorithm");

            // For all algorithms results
            for (int i = 0; i < algorithmsXML.getLength(); i++) {
                // Get the algorithm results
                Node algorithmNode = algorithmsXML.item(i);

                // Get the algorithm details
                if (algorithmNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element algorithmElement = (Element) algorithmNode;
                    AlgorithmResult result = new AlgorithmResult();

                    // add each result into a new class and put it all into array or map
                    result.name = algorithmElement.getAttribute("name");
                    result.score = Double.parseDouble(String.format("%.5f", Double.parseDouble(algorithmElement.getAttribute("score"))));
                    result.author = hhInfo.containsKey(result.name) ? hhInfo.get(result.name)[0] : "";
                    result.color = getColor(result.score);
                    result.rColor = result.color.getRed();
                    result.gColor = result.color.getGreen();
                    result.bColor = result.color.getBlue();

                    // Extract the algorithm results of each problem domain
                    NodeList problemsXML = algorithmElement.getElementsByTagName("problem");

                    result.problemsResults = new HashMap<>();

                    for (int problemId = 0; problemId < problemsXML.getLength(); problemId++) {
                        Node problemNode = problemsXML.item(problemId);

                        if (problemNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element problemElement = (Element) problemNode;

                            // Create new structure
                            AlgorithmProblemResult newRes = new AlgorithmProblemResult();
                            
                            // set the problem result parameters
                            newRes.name = problemElement.getAttribute("name");
                            newRes.score = Double.parseDouble(String.format("%.5f", Double.parseDouble(problemElement.getAttribute("avg"))));
                            newRes.color = getColor(newRes.score);
                            newRes.rColor = newRes.color.getRed();
                            newRes.gColor = newRes.color.getGreen();
                            newRes.bColor = newRes.color.getBlue();
                            // add new problem results to algorithm
                            result.problemsResults.put(newRes.name, newRes);
                        }
                    }
                    results.add(result);
                }
            }  
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method turns the structures in given lists into a 
     * arrays, that can be read by jinja
     * @param results list with results (structures)
     * @param problems list of problems names
     * @return structure with arrays for overall and each problem results
     */
    public void resultsToList() {
        // Inicialize the arrays
        algsOverRes = new ArrayList<>();
        algsProbsRes = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            // Initialize list for algorithm results
            List<String> algOverRes = new ArrayList<>();

            // Store results
            AlgorithmResult algRes = results.get(i);
            algOverRes.add(algRes.name);
            algOverRes.add(algRes.author);
            algOverRes.add(String.valueOf(algRes.score));
            algOverRes.add(String.valueOf(algRes.rColor));
            algOverRes.add(String.valueOf(algRes.gColor));
            algOverRes.add(String.valueOf(algRes.bColor));

            // Loop over problems results
            List<List<String>> algProbsRes = new ArrayList<>();
            for (int j = 0; j < problems.size(); j++) {
                List<String> algProbRes = new ArrayList<>();

                AlgorithmProblemResult probRes = algRes.problemsResults.get(problems.get(j));
                algProbRes.add(probRes.name);
                algProbRes.add(String.valueOf(probRes.score));
                algProbRes.add(String.valueOf(probRes.rColor));
                algProbRes.add(String.valueOf(probRes.gColor));
                algProbRes.add(String.valueOf(probRes.bColor));

                // Add to problems results array
                algProbsRes.add(algProbRes);
            }
            // Store the results
            algsOverRes.add(algOverRes);
            algsProbsRes.add(algProbsRes);
        }
    }

    /**
     * Method generates a svg file with given data
     * @param results list of algorithms results
     * @param problems list of problem names
     * @param id id of the experiment
     * @throws IOException
     */
    public void createPage(String id) throws IOException {
        Jinjava jinjava = new Jinjava();

        // Get the transformed data
        resultsToList();
        Map<String, Object> context = new HashMap<>();
        context.put("overallResults", algsOverRes);
        context.put("problemsResults", algsProbsRes);
        context.put("problems", problems);

        // Loead the jinja vsg template
        InputStream inputStream = HeatmapGenerator.class.getResourceAsStream(metadataPath);
        String svgFile = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        // Render the template
        String renderedTemplate = jinjava.render(svgFile, context);
        // output the file
        String resultsSvgFilePath = String.format(resultsSvgFile, id);
        
        try(FileWriter fileWriter = new FileWriter(resultsSvgFilePath);) {    
            fileWriter.write(renderedTemplate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method builds the results page
     * First it loads the data from experiment and
     * then it stores them into a svg file
     * @param experimentId id of experiment
     */
    public void buildResultsPage(String experimentId) {
        String xmlResultsPath = String.format(resultsXmlFile, experimentId);
        loadXMLFile(xmlResultsPath);
        // Sort the results by their overall score
        sortResults();

        // Get the problems list
        problems = results.isEmpty() ?
            new ArrayList<>() : new ArrayList<>(results.get(0).problemsResults.keySet());

        try {
            createPage(experimentId);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}

