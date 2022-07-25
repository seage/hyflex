package hyflex.chesc2011.evaluation.metadata;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

// todo

/**
 * Class is used for retrieving the data from metadata file.
 * 
 * @author David Omrai
 */
public class ProblemInstanceMetadataReader {

  /**
   * .
   * @param path
   * @return
   * @throws Exception
   */
  private static ProblemInstanceMetadata readJsonMetadata(Path path) throws Exception {
    ProblemInstanceMetadata result = new ProblemInstanceMetadata();

    InputStream inputStream =
        ProblemInstanceMetadataReader.class.getResourceAsStream(path.toString());

    // Read the xml file
    JSONTokener tokener = new JSONTokener(inputStream);
    JSONObject metadata = new JSONObject(tokener);
 
    JSONObject instances = metadata.getJSONObject("instances");
    // Iterate through metadata
    for (String instanceId : instances.keySet()) {
      JSONObject instance = instances.getJSONObject(instanceId);

      result.put(instanceId, "optimum", instance.getDouble("optimum"));
      result.put(instanceId, "greedy", instance.getDouble("greedy"));
      result.put(instanceId, "random", instance.getDouble("random"));
      result.put(instanceId, "size", instance.getDouble("size"));
    }

    return result;
  }

  /**
   * Method reads the file with metadata and stores the data inside map.
   * 
   * @param path Path where the metadata is stored.
   * @return Returns the map with metadata data.
   */
  private static ProblemInstanceMetadata readXmlMetadata(Path path) throws Exception {
    ProblemInstanceMetadata result = new ProblemInstanceMetadata();

    InputStream inputStream =
        ProblemInstanceMetadataReader.class.getResourceAsStream(path.toString());
    // Read the input file
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
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
        if (isDouble(element.getAttribute("greedy")) == false) {
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
        result.put(instanceId, "greedy", Double.parseDouble(element.getAttribute("greedy")));
        result.put(instanceId, "random", Double.parseDouble(element.getAttribute("random")));
        result.put(instanceId, "size", Double.parseDouble(element.getAttribute("size")));
      }
    }
    return result;
  }


  /**
   * Method tests given string if it contains double.
   * 
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

  /**
   * Method reads the metadata file.
   * 
   * @param problems Problems names.
   * @param metadataPath Path to a metadata file.
   */
  public static Map<String, ProblemInstanceMetadata> readXmlProblemsInstancesMetadata(
      String[] problems, Path metadataPath) throws Exception {
    Map<String, ProblemInstanceMetadata> results = new HashMap<>();

    for (String problemId : problems) {
      Path instanceMetadataPath =
          Paths.get(metadataPath.toString() + "/" + problemId.toLowerCase() + ".metadata.xml");

      results.put(problemId, readXmlMetadata(instanceMetadataPath));
    }
    return results;
  }

  /**
   * .
   * @param problems
   * @param metadataPath
   * @return
   * @throws Exception
   */
  public static Map<String, ProblemInstanceMetadata> readJsonProblemsInstancesMetadata(
      String[] problems, Path metadataPath
  ) throws Exception {
    Map<String, ProblemInstanceMetadata> results = new HashMap<>();

    for (String problemId: problems) {
      Path instanceMetadataPath =
          Paths.get(metadataPath.toString() + "/" + problemId.toLowerCase() + ".metadata.json");

      results.put(problemId, readJsonMetadata(instanceMetadataPath));
    }

    return results;
  }
}
