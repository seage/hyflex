package hyflex.chesc2011;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hyflex.chesc2011.metrics.BenchmarkCalculator;
import hyflex.chesc2011.metrics.UnitMetricScoreCalculator;

//import jdk.jfr.Timestamp;

import org.junit.jupiter.api.Test;

// See https://dzone.com/articles/junit-tutorial-for-beginners-in-5-steps

/**
  * Few unit tests for getMetric method of the BenchmarkMetricCalculator class.
  * @author David Omrai
  */

public class MetricTest {
  BenchmarkCalculator calculator = 
      new BenchmarkCalculator();

  @Test
  void testLowerBound() throws Exception {
    assertEquals(
        calculator.scoreIntervalTo, 
        UnitMetricScoreCalculator.getMetric(
          calculator.scoreIntervalFrom, calculator.scoreIntervalTo, 1.0, 42.0, 1.0), 0.1);
  }

  @Test
  void tesUpperBound() throws Exception {
    assertEquals(calculator.scoreIntervalFrom, UnitMetricScoreCalculator.getMetric(
        calculator.scoreIntervalFrom, calculator.scoreIntervalTo, 1, 42, 42), 0.1);
  }

  @Test
  void testMiddleValue() throws Exception {
    assertEquals(
        (calculator.scoreIntervalTo - calculator.scoreIntervalFrom) / 2, 
        UnitMetricScoreCalculator.getMetric(
          calculator.scoreIntervalFrom, calculator.scoreIntervalTo, 42, 0, 21), 
        0.1);
  }

  @Test
  void testBadRandomInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(
        calculator.scoreIntervalFrom, calculator.scoreIntervalTo, 1, -10, 1));
    ;
  }

  @Test
  void testBadOptimalInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(
        calculator.scoreIntervalFrom, calculator.scoreIntervalTo, -1, 10, 1));
  }

  @Test
  void testBadWorstInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(
        calculator.scoreIntervalFrom, calculator.scoreIntervalTo, -1, 10, 1));
  }

  @Test
  void testBadCurrentInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(
        calculator.scoreIntervalFrom, calculator.scoreIntervalTo, 1, 10, -1));
  }

  @Test
  void testBadIntervalInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(
        calculator.scoreIntervalFrom, calculator.scoreIntervalTo, 42, 3, 1));
  }

  @Test
  void testBadOutOfIntervaInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(
        calculator.scoreIntervalFrom, calculator.scoreIntervalTo, 3, 42, 1));
  }
}
