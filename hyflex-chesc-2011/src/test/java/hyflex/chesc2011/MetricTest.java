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
  void testOptimalSolution() {
    try {
      assertTrue(Math.abs(toTest.intervalTo - toTest.getMetric(42, 1, 1)) <= 0.1);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  void testRandomSolution() {
    try {
      assertTrue(Math.abs(toTest.intervalFrom - toTest.getMetric(42, 1, 42)) <= 0.1);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  void testMiddleSolution() {
    try {
      assertTrue(Math.abs(
          (toTest.intervalTo - toTest.intervalFrom) / 2
          - toTest.getMetric(42, 0, 21)) <= 0.1);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  @Test
  void testBadRandomInput() {
    try {
      toTest.getMetric(-10, 1, 1);
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  void testBadOptimalInput() {
    try {
      toTest.getMetric(10, -1, 1);
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  void testBadWorstInput() {
    try {
      toTest.getMetric(10, -1, 1);
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  void testBadCurrentInput() {
    try {
      toTest.getMetric(10, 1, -1);
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  void testBadIntervalInput() {
    try {
      toTest.getMetric(3, 42, 1);
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  void testBadOutOfIntervaInput() {
    try {
      toTest.getMetric(42, 3, 1);
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }
}
