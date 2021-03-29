/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitMetricScoreCalculator {
  // Array of problems domains ids.
  String[] problems;
  // Path to metadata xml files.
  String metadataPath;
  // Name of the problems instances.
  Map<String, List<String>> problemsInstances; 
  // Map with weight for each problem domain.
  Map<String, Double> problemsWeightsMap;
  // Score interval lower bound.
  double scoreIntervalFrom;
  // Score interval upper bound.
  double scoreIntervalTo;


  /**
   * Constructor, sets all necessary parameters.
   */
  UnitMetricScoreCalculator(
      String[] problems,
      String metadataPath, 
      Map<String, List<String>> problemsInstances, 
      Map<String, Double> problemsWeightsMap,
      double scoreIntervalFrom, 
      double scoreIntervalTo) {
    this.problems = problems;
    this.metadataPath = metadataPath;
    this.problemsInstances = problemsInstances;
    this.problemsWeightsMap = problemsWeightsMap;
    this.scoreIntervalFrom = scoreIntervalFrom;
    this.scoreIntervalTo = scoreIntervalTo;
  }


  /**
   * Method calculates the score for given algorithm problem results.
   * @param cards ScoreCards with algorithms results.
   * @return ScoreCard with scores for each problem domain and total score.
   */
  public List<ScoreCard> calculateScore(List<ScoreCard> cards) throws Exception {
    List<ScoreCard> results = new ArrayList<>();

    for (ScoreCard card: cards) {

    
      Map<String, ProblemInstanceMetadata> instancesMetadata = new HashMap<>();

      for (String problemId: problems) {
        Path instanceMetadataPath = Paths
            .get(metadataPath + "/" + problemId.toLowerCase() + ".metadata.xml");

        instancesMetadata.put(problemId, ProblemInstanceMetadataReader.read(instanceMetadataPath));
      }

      ScoreCard result = new ScoreCard(card.getName(), problems);

      List<Double> problemsScores = new ArrayList<>();
      List<Double> problemsWeights = new ArrayList<>();

      for (String problemId: problems) {
          
        List<Double> instancesScores = new ArrayList<>();
        List<Double> sizes = new ArrayList<>(); 

        for (String instanceId: problemsInstances.get(problemId)) {
          double instanceScore = UnitMetricScoreCalculator.getMetric(
              scoreIntervalFrom, 
              scoreIntervalTo, 
              instancesMetadata.get(problemId).get(instanceId, "optimum"), 
              instancesMetadata.get(problemId).get(instanceId, "random"), 
              card.getInstanceScore(problemId, instanceId)
          );

          result.putInstanceScore(problemId, instanceId, instanceScore);

          instancesScores.add(instanceScore);
          sizes.add((double)instancesMetadata.get(problemId).get(instanceId, "size"));
        }

        double problemScore = calculateWeightedMean(instancesScores, sizes);
        result.putDomainScore(problemId, problemScore);

        problemsScores.add(problemScore);
        problemsWeights.add(problemsWeightsMap.get(problemId));
      }

      result.setScore(calculateWeightedMean(problemsScores, problemsWeights));
      results.add(result);
    }
    
    return results;
  }


  /**
   * Method returns the metric based on given data.
   * @param upperBound The value of random generator.
   * @param lowerBound The optimal value.
   * @param current Input value for metric.
   * @return The metric for given value.
   */
  public static double getMetric(
      double scoreIntervalFrom, 
      double scoreIntervalTo, double lowerBound, double upperBound, double current) 
      throws Exception {
    if (upperBound < 0 || lowerBound < 0 || current < 0) {
      throw new Exception("Bad input values: input parameter < 0");
    }
    if (upperBound < lowerBound) {
      throw new Exception("Bad input values: upperBound < lowerBound");
    }
    if (current < lowerBound || current > upperBound) {
      throw new Exception("Bad input values: current is not from interval");
    }

    return scoreIntervalTo - (
      mapToInterval(lowerBound, upperBound, scoreIntervalFrom, scoreIntervalTo, current));
  }


  /**
    * Method maps the value of one interval onto a new one.
    * @param lowerBound Lower value of the first interval.
    * @param upperBound Upper value of the first interval.
    * @param intervalLower Lower value of the new interval.
    * @param intervalUpper Upper value of the new interval.
    * @param value Value to map to a new interval.
    * @return Return the mapped value of the value.
    */
  private static double mapToInterval(
      double lowerBound, 
      double upperBound, double intervalLower, double intervalUpper, double value)
      throws Exception {
    double valueNormalization = (value - lowerBound) / (upperBound - lowerBound);
    double scaling = valueNormalization * (intervalUpper - intervalLower);
    double shifting = scaling + intervalLower;

    return shifting;
  }


  /**
   * Method calculates the weighted mean from given arrays.
   * @param values Values.
   * @param weights Weights of values.
   * @return Returns the weighted mean.
   */
  public static double calculateWeightedMean(
      List<Double> values, List<Double> weights) throws Exception {
    if (values.size() != weights.size()) {
      throw new Exception("Error: input arrays does not have the same size.");
    }

    /**
     *                  SUM(i=0|n)[size(instance-i)*metric(instance-i)]   .
     * weighted mean = -------------------------------------------------  .
     *                            SUM(i=0|n)[size(instance-i)]            .
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
