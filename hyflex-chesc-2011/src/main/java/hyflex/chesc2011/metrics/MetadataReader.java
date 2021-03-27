/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MetadataReader {

  /**
    * Method reads the file with metadata and stores the data inside map.
    * @param path Path where the metadata is stored.
    * @return Returns the map with metadata data.
    */
  public static Map<String, Map<String, Double>> read(Path path) throws Exception {
    Map<String, Map<String, Double>> results = new HashMap<>();
    // Read the input file
    Document doc = DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder().parse(MetadataReader.class.getResourceAsStream(path.toString()));
    doc.getDocumentElement().normalize();

    // Get all instances from the file
    NodeList nodeList = doc.getElementsByTagName("Instance");
    
    // For each instance stores its values into a hash map
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node node = nodeList.item(temp);
      
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;

        
        if (isDouble(element.getAttribute("optimum")) == false) {
          continue;
        }
        if (isDouble(element.getAttribute("random")) == false) {
          continue;
        }
        if (isDouble(element.getAttribute("size")) == false) {
          continue;
        }

        Map<String, Double> result = new HashMap<>();

        result.put("optimum", Double.parseDouble(element.getAttribute("optimum")));
        result.put("random", Double.parseDouble(element.getAttribute("random")));
        result.put("size", Double.parseDouble(element.getAttribute("size")));

        results.put(element.getAttribute("id"), result);
      }    
    }
    return results;
  }

  /**
   * Method tests given string if it contains double.
   * @param text String to test.
   * @return True if the string can be translated to double, false otherwise.
   */
  private static Boolean isDouble(String text) {
    try {
      Double.parseDouble(text);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
