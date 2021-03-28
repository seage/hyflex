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

public class ScoreCalculator {
  String[] problems;
  String metadataPath;
  Map<String, List<String>> cardInstances; 
  Map<String, Double> problemsWeightsMap;
  double intervalFrom;
  double intervalTo;

  ScoreCalculator(
      String[] problems,
      String metadataPath, 
      Map<String, List<String>> cardInstances, 
      Map<String, Double> problemsWeightsMap,
      double intervalFrom, 
      double intervalTo) {
    this.problems = problems;
    this.metadataPath = metadataPath;
    this.cardInstances = cardInstances;
    this.problemsWeightsMap = problemsWeightsMap;
    this.intervalFrom = intervalFrom;
    this.intervalTo = intervalTo;
  }

  /**
   * Method calculates the score for given algorithm problem results.
   * @param card ResultsCard with algorithm results.
   * @return ResultsCard with scores for each problem domain and total score.
   */
  public ResultsCard calculateScore(ResultsCard card) throws Exception {

    Map<String, ProblemInstanceMetadata> instancesMetadata = new HashMap<>();

    for (String problemId: problems) {
      Path instanceMetadataPath = Paths
          .get(metadataPath + "/" + problemId.toLowerCase() + ".metadata.xml");

      instancesMetadata.put(problemId, ProblemInstanceMetadataReader.read(instanceMetadataPath));
    }

    ResultsCard result = new ResultsCard(card.getName(), problems);

    List<Double> problemsScores = new ArrayList<>();
    List<Double> problemsWeights = new ArrayList<>();

    for (String problemId: problems) {
        
      List<Double> instancesScores = new ArrayList<>();
      List<Double> sizes = new ArrayList<>(); 

      for (String instanceId: cardInstances.get(problemId)) {
        double instanceScore = ScoreCalculator.getMetric(
            intervalFrom, 
            intervalTo, 
            instancesMetadata.get(problemId).get(instanceId, "optimum"), 
            instancesMetadata.get(problemId).get(instanceId, "random"), 
            card.getInstanceScore(problemId, instanceId)
        );

        result.putInstanceValue(problemId, instanceId, instanceScore);

        instancesScores.add(instanceScore);
        sizes.add((double)instancesMetadata.get(problemId).get(instanceId, "size"));
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
   * Method returns the metric based on given data.
   * @param upperBound The value of random generator.
   * @param lowerBound The optimal value.
   * @param current Input value for metric.
   * @return The metric for given value.
   */
  public static double getMetric(
      double intervalFrom, double intervalTo, double lowerBound, double upperBound, double current) 
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

    return intervalTo - (mapToInterval(lowerBound, upperBound, intervalFrom, intervalTo, current));
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
