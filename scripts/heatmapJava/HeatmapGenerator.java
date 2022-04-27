/**
 * Program generates a heatmap, visualizating given data
 * from hyflex experiment
 * 
 * @author David Omrai
 */

package heatmap;

import java.util.*;

//xml libraries
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    public static void main(String[] args) {
        loadXMLFile("results/" + args[0] + "/unit-metric-scores.xml");
    }

    public static void loadXMLFile(String xmlPath) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // process xml securely
            //dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse xml file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlPath));

            // normalize the document
            doc.getDocumentElement().normalize();

            System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void createPage(LinkedList results, LinkedList problems, String pageDest) {
        //todo
    }

    public static void buildResultsPage(String experimentId) {
        //todo
    }
}

