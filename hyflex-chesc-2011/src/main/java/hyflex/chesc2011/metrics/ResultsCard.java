/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultsCard {
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

  ResultsCard(String cardName, String[] domains) {
    algorithmName = cardName;

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
   * Method calculates the algorithm score.
   * @param weightsMap Map with weights for each problem domain.
   */
  public void calculateScore(Map<String, Double> weightsMap)
      throws Exception {
    List<String> sortedKeys = new ArrayList<String>(weightsMap.keySet());
    Collections.sort(sortedKeys);

    List<Double> scores = new ArrayList<>(); 
    List<Double> weights = new ArrayList<>();
    for (String problemId: sortedKeys) {
      scores.add(scorePerDomain.get(problemId));
      weights.add(weightsMap.get(problemId));
    }

    totalScore = ScoreCalculator.calculateWeightedMean(scores, weights);
  }

  public double getScore() {
    return totalScore;
  }

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
   * @param problemId Name of the problem domain.
   * @return Returns the value of the problem domain.
   */
  public double getProblemScore(String problemId) {
    return scorePerDomain.get(problemId);
  }

  public Set<String> getProblems() {
    return problemResults.keySet();
  }

  public Set<String> getInstances(String problemId) {
    return problemResults.get(problemId).keySet();
  }
}
