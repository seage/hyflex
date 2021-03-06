package hyflex.chesc2011.evaluation.calculators;

import hyflex.chesc2011.evaluation.metadata.ProblemInstanceMetadata;
import hyflex.chesc2011.evaluation.scorecard.ScoreCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class is used for calculating the unit metric score.
 * 
 * @author David Omrai
 */
public class UnitMetricScoreCalculator implements ScoreCalculator {
  /**
   * Map represents weights for each problem domain.
   */
  @SuppressWarnings("serial")
  private final Map<String, Double> problemsWeightsMap = new HashMap<>() {
    {
      put("SAT", 1.0);
      put("TSP", 1.0);
    }
  };

  // Interval on which are the results being mapped, metric range
  private static final double INTERVAL_MIN = 0.0;
  private static final double INTERVAL_MAX = 1.0;

  private String[] problems;
  private Map<String, ProblemInstanceMetadata> metadata;
  private Map<String, List<String>> problemsInstances;

  /**
   * Constructor.
   */
  public UnitMetricScoreCalculator(Map<String, ProblemInstanceMetadata> metadata,
      Map<String, List<String>> problemsInstances, String[] problems) {
    this.problems = problems;
    this.metadata = metadata;
    this.problemsInstances = problemsInstances;
  }

  /**
   * Method calculates card score.
   * @param card Card with algorithm results.
   * @return ScoreCard with scores.
   */
  public ScoreCard calculateScore(ScoreCard card) throws Exception {
    ScoreCard result = new ScoreCard(card.getName(), problems);

    List<Double> problemsScores = new ArrayList<>();
    List<Double> problemsWeights = new ArrayList<>();

    for (String problemId : problems) {

      List<Double> instancesScores = new ArrayList<>();
      List<Double> sizes = new ArrayList<>();

      for (String instanceId : problemsInstances.get(problemId)) {
        double instanceScore = UnitMetricScoreCalculator.getMetric(
            metadata.get(problemId).get(instanceId, "optimum"),
            metadata.get(problemId).get(instanceId, "greedy"),
            card.getInstanceScore(problemId, instanceId));

        result.putInstanceScore(problemId, instanceId, instanceScore);

        instancesScores.add(instanceScore);

        if (metadata.get(problemId).get(instanceId, "size") < 0) {
          throw new Exception(
              "Bad input values: size of " + instanceId + " instance is negative.");
        }

        sizes.add((double) metadata.get(problemId).get(instanceId, "size"));
      }

      double problemScore = calculateWeightedMean(instancesScores, sizes);
      result.putDomainScore(problemId, problemScore);

      problemsScores.add(problemScore);
      problemsWeights.add(problemsWeightsMap.get(problemId));
    }

    result.setScore(calculateWeightedMean(problemsScores, problemsWeights));

    return result;
  }


  /**
   * Method calculates the scores for given algorithm problem results.
   * 
   * @param cards ScoreCards with algorithms results.
   * @return ScoreCard with scores for each problem domain and total score.
   */
  public List<ScoreCard> calculateScores(List<ScoreCard> cards) throws Exception {
    List<ScoreCard> results = new ArrayList<>();

    for (ScoreCard card : cards) {
      ScoreCard result = calculateScore(card);

      results.add(result);
    }

    return results;
  }


  /**
   * Method returns the metric based on given data.
   * 
   * @param upperBound The value of greedy generator.
   * @param lowerBound The optimal value.
   * @param current    Input value for metric.
   * @return The metric for given value.
   */
  private static double getMetric(double lowerBound, double upperBound, double current)
      throws Exception {
    if (upperBound < 0 || lowerBound < 0 || current < 0) {
      throw new Exception("Bad input values: input parameter < 0");
    }
    if (upperBound < lowerBound) {
      throw new Exception("Bad input values: upperBound < lowerBound");
    }
    if (current < lowerBound) {
      throw new Exception("Bad input values: value can't be better than optimum");
    }

    return INTERVAL_MAX - mapToInterval(lowerBound, upperBound, Math.min(upperBound, current));
  }


  /**
   * Method maps the value of one interval onto a new one.
   * 
   * @param lowerBound    Lower value of the first interval.
   * @param upperBound    Upper value of the first interval.
   * @param value         Value to map to a new interval.
   * @return Return the mapped value of the value.
   */
  private static double mapToInterval(double lowerBound, double upperBound, double value) 
      throws Exception {
    double valueNormalization = (value - lowerBound) / (upperBound - lowerBound);
    double scaling = valueNormalization * (INTERVAL_MAX - INTERVAL_MIN);
    double shifting = scaling + INTERVAL_MIN;

    return shifting;
  }


  /**
   * Method calculates the weighted mean from given arrays.
   * 
   * @param values  Values.
   * @param weights Weights of values.
   * @return Returns the weighted mean.
   */
  private static double calculateWeightedMean(List<Double> values, List<Double> weights)
      throws Exception {
    if (values.size() != weights.size()) {
      throw new Exception("Error: input arrays does not have the same size.");
    }

    /**
     *                   SUM(i=0|n)[size(instance-i)*metric(instance-i)] . 
     * weighted mean = ------------------------------------------------- . 
     *                           SUM(i=0|n)[size(instance-i)] .
     */
    double numerator = 0;
    double nominator = 0;
    for (int i = 0; i < values.size(); i++) {
      numerator += weights.get(i) * values.get(i);

      nominator += weights.get(i);
    }
    return (numerator / nominator);
  }
}
