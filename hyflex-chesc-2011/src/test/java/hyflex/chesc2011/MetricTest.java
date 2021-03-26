package hyflex.chesc2011;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hyflex.chesc2011.IntervalBenchmarkCalculator;
import jdk.jfr.Timestamp;

import org.junit.jupiter.api.Test;

// See https://dzone.com/articles/junit-tutorial-for-beginners-in-5-steps

/**
  * Few unit tests for getMetric method of the IntervalBenchmarkCalculator class.
  * @author David Omrai
  */

public class MetricTest {
  IntervalBenchmarkCalculator toTest = 
      new IntervalBenchmarkCalculator();

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
    toTest.getMetric(-10, 1, 1);
  }

  @Test
  void testBadOptimalInput() throws Exception {
    toTest.getMetric(10, -1, 1);
  }

  @Test
  void testBadWorstInput() throws Exception {
    toTest.getMetric(10, -1, 1);
  }

  @Test
  void testBadCurrentInput() throws Exception {
    toTest.getMetric(10, 1, -1);
  }

  @Test
  void testBadIntervalInput() throws Exception {
    toTest.getMetric(3, 42, 1);
  }

  @Test
  void testBadOutOfIntervaInput() throws Exception {
    toTest.getMetric(42, 3, 1);
  }
}
