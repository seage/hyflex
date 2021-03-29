package hyflex.chesc2011;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import hyflex.chesc2011.metrics.ScoreCalculator;
import hyflex.chesc2011.metrics.ScoreCard;
import hyflex.chesc2011.metrics.UnitMetricScoreCalculator;

// import jdk.jfr.Timestamp;

import org.junit.jupiter.api.Test;

// See https://dzone.com/articles/junit-tutorial-for-beginners-in-5-steps

/**
 * Few unit tests for getMetric method of the UnitMetricScoreCalculator class.
 * 
 * @author David Omrai
 */

public class UnitMetricScoreCalculatorTest {
  ScoreCalculator sc;

  @BeforeAll
  void init() {
    // TODO
    ScoreCalculator sc = new UnitMetricScoreCalculator(null, null, null);
  }

  @Test
  void testCalculateScore() {
    // TODO
    List<ScoreCard> cards = sc.calculateScore(null);
    assertEquals(2, cards.size());
  }

  // Remove all following tests, all will be tested as a consequence of the call of sc.calculateScore
  @Test
  void testLowerBound() throws Exception {
    assertEquals(1.0, UnitMetricScoreCalculator.getMetric(1.0, 42.0, 1.0), 0.1);
  }

  @Test
  void tesUpperBound() throws Exception {
    assertEquals(0, UnitMetricScoreCalculator.getMetric(1, 42, 42), 0.1);
  }

  @Test
  void testMiddleValue() throws Exception {
    assertEquals(0.5, UnitMetricScoreCalculator.getMetric(0, 42, 21), 0.1);
  }

  @Test
  void testBadRandomInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(1, -10, 1));;
  }

  @Test
  void testBadOptimalInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(-1, 10, 1));
  }

  @Test
  void testBadWorstInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(-1, 10, 1));
  }

  @Test
  void testBadCurrentInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(1, 10, -1));
  }

  @Test
  void testBadIntervalInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(42, 3, 1));
  }

  @Test
  void testBadOutOfIntervaInput() throws Exception {
    assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(3, 42, 1));
  }
}
