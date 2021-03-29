/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScoreCard {
  /**
   * Name of the algorithm.
   */
  String algorithmName;

  /**
   * Total algorithm score.
   */
  double totalScore;

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


  /**
   * Constructor, sets all necessary parameters.
   * @param algorithmName Name of the algorithm.
   * @param domains Array of problem domains names.
   */
  public ScoreCard(String algorithmName, String[] domains) {
    this.algorithmName = algorithmName;

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
  public ScoreCard putInstanceScore(String problemId, String instanceId, Double value) {
    problemResults.get(problemId).put(instanceId, value);
    return this;
  }


  /**
   * Method stores given problemId value.
   * @param problemId Name of the problem domain.
   * @param value Value of the problem domain.
   * @return Returns this.
   */
  public ScoreCard putDomainScore(String problemId, Double value) {
    scorePerDomain.put(problemId, value);
    return this;
  }


  /**
   * Method sets algorithm score.
   * @param score Algorithm score.
   */
  public void setScore(Double score) {
    totalScore = score;
  }


  /**
   * Method returns the problem domain score.
   * @return Problem domain score.
   */
  public double getScore() {
    return totalScore;
  }

  
  /**
   * Method returns the name of the algorithm.
   * @return Name of the algorithm.
   */
  public String getName() {
    return algorithmName;
  }


  /**
   * Method returns value of given instance.
   * @param problemId Name of the problem doamain.
   * @param instanceId Name of the intance.
   * @return Returns the value of given instacne.
   */
  public double getInstanceScore(String problemId, String instanceId) {
    return problemResults.get(problemId).get(instanceId);
  }


  /**
   * Returns the value of given problem domain.
   * @param problemId Name of a problem domain.
   * @return Returns the value of a problem domain.
   */
  public double getProblemScore(String problemId) {
    return scorePerDomain.get(problemId);
  }


  /**
   * Method returns the set of problem domains names.
   * @return Set of problem domains names.
   */
  public Set<String> getProblems() {
    return problemResults.keySet();
  }


  /**
   * Method returns the set of problem domains names.
   * @param problemId Set of problem domains names.
   * @return
   */
  public Set<String> getInstances(String problemId) {
    return problemResults.get(problemId).keySet();
  }
}
