/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CardHandler {
  /**
   * This map represents what instances are on each line of
   * the results file.
   * It also holds the order of instances for each problem domain
   * in the results file.
   */
  @SuppressWarnings("serial")
  static Map<String, List<String>> cardInstances = new HashMap<>() {{
        put("SAT", new ArrayList<>(
            Arrays.asList(
              "hyflex-sat-3", "hyflex-sat-5", "hyflex-sat-4", "hyflex-sat-10", "hyflex-sat-11")));
        put("TSP", new ArrayList<>(
            Arrays.asList(
              "hyflex-tsp-0", "hyflex-tsp-8", "hyflex-tsp-2", "hyflex-tsp-7", "hyflex-tsp-6")));
    }
  };

  /**
   * Method reads the results file and stores the data into a map.
   * @param path Path where the file is stored.
   * @return Map with algorithm results.
   */
  public static Map<String, Map<String, Double>> loadCard(String[] problems, String path)
      throws Exception {
    Map<String, Map<String, Double>> results = new HashMap<>();

    Scanner scanner = new Scanner(new File(path)).useDelimiter("\n");

    for (String problemId : problems) {
      // System.out.println(problemId);
      
      if (scanner.hasNextLine() == false) {
        scanner.close();
        System.out.println("Not enough lines in " + path + " file.");
        return null;
      }

      Map<String, Double> result = new HashMap<>();

      Scanner line = new Scanner(scanner.nextLine()).useDelimiter(", ");

      for (String instanceId: cardInstances.get(problemId)) {

        if (line.hasNextLine() == false) {
          line.close();
          System.out.println("Not enough instances results in " + path + " file.");
          return null;
        }
        
        result.put(instanceId, Double.parseDouble(line.next()));
      }

      line.close();
      results.put(problemId, result);
    }
    scanner.close();

    return results;
  }

 
}
