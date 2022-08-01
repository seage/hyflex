package hyflex.chesc2011.evaluation.metadata;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
  private static ProblemInstanceMetadata readMetadata(Path path) throws Exception {
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
   * .
   * @param problems
   * @param metadataPath
   * @return
   * @throws Exception
   */
  public static Map<String, ProblemInstanceMetadata> readProblemsInstancesMetadata(
      String[] problems, Path metadataPath
  ) throws Exception {
    Map<String, ProblemInstanceMetadata> results = new HashMap<>();

    for (String problemId: problems) {
      Path instanceMetadataPath =
          Paths.get(metadataPath.toString() + "/" + problemId.toLowerCase() + ".metadata.json");

      results.put(problemId, readMetadata(instanceMetadataPath));
    }

    return results;
  }
}
