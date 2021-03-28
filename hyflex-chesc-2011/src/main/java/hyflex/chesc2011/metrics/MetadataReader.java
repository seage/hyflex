/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import hyflex.chesc2011.metrics.ProblemInstanceMetadata;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Scanner;

public class MetadataReader {

  /**
    * Method reads the file with metadata and stores the data inside map.
    * @param path Path where the metadata is stored.
    * @return Returns the map with metadata data.
    */
  public static ProblemInstanceMetadata read(Path path) throws Exception {
    ProblemInstanceMetadata result = new ProblemInstanceMetadata();


    // Read the input file
    Document doc = DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder().parse(MetadataReader.class.getResourceAsStream(path.toString()));
    System.out.println(doc);
    doc.getDocumentElement().normalize();

    Scanner scan = new Scanner(MetadataReader.class.getResourceAsStream(path.toString()));

    System.out.println(scan.nextLine());
    System.out.println(scan.nextLine());
    System.out.println(scan.nextLine());
    System.out.println(scan.nextLine());

    System.out.println(doc);


    // Get all instances from the file
    NodeList nodeList = doc.getElementsByTagName("Instance");
    
    // For each instance stores its values into a hash map
    for (int temp = 0; temp < nodeList.getLength(); temp++) {
      Node node = nodeList.item(temp);
      System.out.println(node);
      
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

        String instanceId = element.getAttribute("id");

        result.put(instanceId, "optimum", Double.parseDouble(element.getAttribute("optimum")));
        result.put(instanceId, "random", Double.parseDouble(element.getAttribute("random")));
        result.put(instanceId, "size", Double.parseDouble(element.getAttribute("size")));
      }    
    }
    return result;
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
