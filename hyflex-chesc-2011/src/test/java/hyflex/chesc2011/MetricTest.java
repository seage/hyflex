package hyflex.chesc2011;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hyflex.chesc2011.BenchmarkMetricCalculator;

//import jdk.jfr.Timestamp;

import org.junit.jupiter.api.Test;

// See https://dzone.com/articles/junit-tutorial-for-beginners-in-5-steps

/**
  * Few unit tests for getMetric method of the BenchmarkMetricCalculator class.
  * @author David Omrai
  */

public class MetricTest {
  BenchmarkMetricCalculator toTest = 
      new BenchmarkMetricCalculator();

  @Test
  void testOptimalSolution() throws Exception {
    assertEquals(toTest.intervalTo, toTest.getMetric(42, 1, 1), 0.1);
  }

  @Test
  void testRandomSolution() throws Exception {
    assertEquals(toTest.intervalFrom, toTest.getMetric(42, 1, 42), 0.1);
  }

  @Test
  void testMiddleSolution() throws Exception {
    assertEquals(
        (toTest.intervalTo - toTest.intervalFrom) / 2, 
        toTest.getMetric(42, 0, 21), 
        0.1);
  }

  @Test
  void testBadRandomInput() throws Exception {
    assertThrows(Exception.class, () -> toTest.getMetric(-10, 1, 1));
    ;
  }

  @Test
  void testBadOptimalInput() throws Exception {
    assertThrows(Exception.class, () -> toTest.getMetric(10, -1, 1));
  }

  @Test
  void testBadWorstInput() throws Exception {
    assertThrows(Exception.class, () -> toTest.getMetric(10, -1, 1));
  }

  @Test
  void testBadCurrentInput() throws Exception {
    assertThrows(Exception.class, () -> toTest.getMetric(10, 1, -1));
  }

  @Test
  void testBadIntervalInput() throws Exception {
    assertThrows(Exception.class, () -> toTest.getMetric(3, 42, 1));
  }

  @Test
  void testBadOutOfIntervaInput() throws Exception {
    assertThrows(Exception.class, () -> toTest.getMetric(42, 3, 1));
  }
}
