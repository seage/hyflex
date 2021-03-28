/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResultsCard {
  /**
   * .
   * String: problemId
   *    - String: instanceId
   *        - Double: instanceId value
   */
  Map<String, Map<String, Double>> problemResults;
  /**
   * .
   * String: problemId
   *    - Double: problemId score
   */
  Map<String, Double> scorePerDomain;

  ResultsCard(String[] domains) {
    problemResults = new HashMap<>();
    scorePerDomain = new HashMap<>();

    for (String domain: domains) {
      problemResults.put(domain, new HashMap<>());
      scorePerDomain.put(domain, null);
    }
  }

  /**
   * .
   * @param problemId .
   * @param instanceId .
   * @param value .
   * @return
   */
  public ResultsCard putInstanceValue(String problemId, String instanceId, Double value) {
    problemResults.get(problemId).put(instanceId, value);
    return this;
  }

  /**
   * .
   * @param problemId .
   * @param value .
   * @return
   */
  public ResultsCard putDomainScore(String problemId, Double value) {
    scorePerDomain.put(problemId, value);
    return this;
  }

  public double getInstanceResult(String problemId, String instanceId) {
    return problemResults.get(problemId).get(instanceId);
  }

  public double getScore(String problemId) {
    return scorePerDomain.get(problemId);
  }

  public Set<String> getProblems() {
    return problemResults.keySet();
  }

  public Set<String> getInstances(String problemId) {
    return problemResults.get(problemId).keySet();
  }
}
