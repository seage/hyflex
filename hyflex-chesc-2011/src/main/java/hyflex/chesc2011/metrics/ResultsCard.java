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
   * Method stores given instanceId value.
   * @param problemId Name of the problem domain.
   * @param instanceId Name of the problem domain instance.
   * @param value Value of the instance.
   * @return Returns this.
   */
  public ResultsCard putInstanceValue(String problemId, String instanceId, Double value) {
    problemResults.get(problemId).put(instanceId, value);
    return this;
  }

  /**
   * Method stores given problemId value.
   * @param problemId Name of the problem domain.
   * @param value Value of the problem domain.
   * @return Returns this.
   */
  public ResultsCard putDomainScore(String problemId, Double value) {
    scorePerDomain.put(problemId, value);
    return this;
  }

  /**
   * Method returns value of given instance.
   * @param problemId Name of the problem doamain.
   * @param instanceId Name of the intance.
   * @return Returns the value of given instacne.
   */
  public double getInstanceResult(String problemId, String instanceId) {
    return problemResults.get(problemId).get(instanceId);
  }

  /**
   * Returns the value of given problem domain.
   * @param problemId Name of the problem domain.
   * @return Returns the value of the problem domain.
   */
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
