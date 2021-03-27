/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import java.util.HashMap;
import java.util.Map;

public class MetadataResults {
  /**
   * .
   * String: instanceId
   *    - String: [optimum, random, size]
   *        - Double: instanceId parameter value
   */
  Map<String, Map<String, Double>> instanceResults;

  MetadataResults() {
    instanceResults = new HashMap<>();
  }

  /**
   * .
   * @param instanceId .
   * @param parameter .
   * @param value .
   * @return
   */
  public MetadataResults put(String instanceId, String parameter, Double value) {
    instanceResults.get(instanceId).put(parameter, value);
    return this;
  }

  public double get(String instanceId, String parameter) {
    return instanceResults.get(instanceId).get(parameter);
  }
}
