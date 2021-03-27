/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import java.util.List;

public class ScoreCalculator {
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
    double valueNormalization = (value - lowerBound) * (1 / (upperBound - lowerBound));
    double scaling = valueNormalization * (intervalUpper - intervalLower);
    double shifting = scaling + intervalLower;

    return shifting;
  }

  /**
   * .
   * @param values .
   * @param weights .
   * @return
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
