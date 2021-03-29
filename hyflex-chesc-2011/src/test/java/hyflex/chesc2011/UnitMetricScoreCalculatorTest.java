package hyflex.chesc2011;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import hyflex.chesc2011.metrics.ProblemInstanceMetadata;
import hyflex.chesc2011.metrics.ProblemInstanceMetadataReader;
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
  String[] problems = {"testProblem"};
  
  @SuppressWarnings("serial")
  Map<String, List<String>> problemInstances;

  @SuppressWarnings("serial")
  Map<String, ProblemInstanceMetadata> instancesMetadata;
  
  ScoreCalculator sc;

  // @BeforeAll
  void init(double testInstanceRandom, double testInstanceOptimum, double testInstanceSize) {
    sc = new UnitMetricScoreCalculator(problems, instancesMetadata, problemInstances);

    
    problemInstances = new HashMap<>() {{
        put("testProblem", new ArrayList<>(
            Arrays.asList(
            "testInstance")));
      }
    };

    instancesMetadata = new HashMap<>() {{
        put("testProblem", new ProblemInstanceMetadata()
            .put("testInstance", "random", testInstanceRandom));
        put("testProblem", new ProblemInstanceMetadata()
            .put("testInstance", "optimum", testInstanceOptimum));
        put("testProblem", new ProblemInstanceMetadata()
            .put("testInstance", "size", testInstanceSize));
      }
    };
  }

  @Test
  void testCalculateScoreBoundaries() throws Exception {
    init(42.0, 0.0, 9.0);

    List<ScoreCard> scoreCardList = new ArrayList<>();
    scoreCardList.add(
        new ScoreCard("noname1", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 0.0)
    );
    scoreCardList.add(
        new ScoreCard("noname2", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 42.0)
    );
    scoreCardList.add(
        new ScoreCard("noname3", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 21.0)
    );

    
    List<ScoreCard> cards = sc.calculateScore(scoreCardList);
    assertEquals(3, cards.size());
    assertEquals(1.0, cards.get(0).getInstanceScore(problems[0], problems[0]), 0.1);
    assertEquals(0, cards.get(0).getInstanceScore(problems[0], problems[0]), 0.1);
    assertEquals(0.5, cards.get(0).getInstanceScore(problems[0], problems[0]), 0.1);
  }

  // Remove all following tests, all will be tested as a consequence of the call of sc.calculateScore
  // @Test
  // void testLowerBound() throws Exception {
  // assertEquals(1.0, UnitMetricScoreCalculator.getMetric(1.0, 42.0, 1.0), 0.1);
  // }

  // @Test
  // void tesUpperBound() throws Exception {
  //   assertEquals(0, UnitMetricScoreCalculator.getMetric(1, 42, 42), 0.1);
  // }

  // @Test
  // void testMiddleValue() throws Exception {
  //   assertEquals(0.5, UnitMetricScoreCalculator.getMetric(0, 42, 21), 0.1);
  // }

  // @Test
  // void testBadRandomInput() throws Exception {
  //   assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(1, -10, 1));;
  // }

  // @Test
  // void testBadOptimalInput() throws Exception {
  //   assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(-1, 10, 1));
  // }

  // @Test
  // void testBadWorstInput() throws Exception {
  //   assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(-1, 10, 1));
  // }

  // @Test
  // void testBadCurrentInput() throws Exception {
  //   assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(1, 10, -1));
  // }

  // @Test
  // void testBadIntervalInput() throws Exception {
  //   assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(42, 3, 1));
  // }

  // @Test
  // void testBadOutOfIntervaInput() throws Exception {
  //   assertThrows(Exception.class, () -> UnitMetricScoreCalculator.getMetric(3, 42, 1));
  // }
}
