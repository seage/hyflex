package hyflex.chesc2011.metrics.calculators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hyflex.chesc2011.metrics.metadata.ProblemInstanceMetadata;
import hyflex.chesc2011.metrics.scorecard.ScoreCard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Few unit tests for getMetric method of the UnitMetricScoreCalculator class.
 * 
 * @author David Omrai
 */
public class UnitMetricScoreCalculatorTest {
  static String[] problems = {"TSP"};
  
  //@SuppressWarnings("serial")
  static Map<String, List<String>> problemInstances;

  //@SuppressWarnings("serial")
  static Map<String, ProblemInstanceMetadata> instancesMetadata;
  
  static UnitMetricScoreCalculator sc;

  @BeforeAll
  static void init() {
    
    problemInstances = new HashMap<>() {{
        put("TSP", new ArrayList<>(
            Arrays.asList("testInstance")));
      }
    };

    instancesMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("testInstance", "greedy", 43.0)
            .put("testInstance", "optimum", 1.0)
            .put("testInstance", "size", 9.0));
      }
    };

    sc = new UnitMetricScoreCalculator(instancesMetadata, problemInstances, problems);
  }

  @Test
  void testOptimalScore() throws Exception {
    ScoreCard card = new ScoreCard("optimal", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 1.0);

    ScoreCard result = sc.calculateScore(card);

    assertEquals(
        1.0, result.getInstanceScore(problems[0], problemInstances.get(problems[0]).get(0)), 0.1);
  }

  @Test
  void testGreedyScore() throws Exception {
    ScoreCard card = new ScoreCard("greedy", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 43.0);

    ScoreCard result = sc.calculateScore(card);

    assertEquals(
        0.0, result.getInstanceScore(problems[0], problemInstances.get(problems[0]).get(0)), 0.1);
  }

  @Test
  void testMiddleScore() throws Exception {
    ScoreCard card = new ScoreCard("middle", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 22.0);

    ScoreCard result = sc.calculateScore(card);

    assertEquals(
        0.5, result.getInstanceScore(problems[0], problemInstances.get(problems[0]).get(0)), 0.1);
  }

  @Test
  void testWorseThanGreedyScore() throws Exception {
    ScoreCard card = new ScoreCard("worseThanGreedy", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 44.0);

    ScoreCard result = sc.calculateScore(card);

    assertEquals(
        0.0, result.getInstanceScore(problems[0], problemInstances.get(problems[0]).get(0)), 0.1);
  }

  @Test
  void testBetterThanOptimal() throws Exception {
    ScoreCard card = new ScoreCard("worseThanOptimal", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 43.0);

    ScoreCard result = sc.calculateScore(card);

    assertEquals(
        0.0, result.getInstanceScore(problems[0], problemInstances.get(problems[0]).get(0)), 0.1);
  }

  @Test
  void testNegativeOptimum() {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("testInstance", "greedy", 10.0)
            .put("testInstance", "optimum", -1.0)
            .put("testInstance", "size", 9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);
    
    ScoreCard card = new ScoreCard("one-negative", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 1.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }

  @Test
  void testGreedySmallerThanOptimum() {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("testInstance", "greedy", 0.0)
            .put("testInstance", "optimum", 1.0)
            .put("testInstance", "size", 9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);
    
    ScoreCard card = new ScoreCard("one-negative", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 0.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }

  @Test
  void testNegativeOptimumAndGreedy() {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("testInstance", "greedy", -42.0)
            .put("testInstance", "optimum", -2.0)
            .put("testInstance", "size", 9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);
    
    ScoreCard card = new ScoreCard("one-negative", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 1.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }

  @Test
  void testNegativeInstanceSize() {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("testInstance", "greedy", 42.0)
            .put("testInstance", "optimum", 2.0)
            .put("testInstance", "size", -9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);
    
    ScoreCard card = new ScoreCard("one-negative", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 1.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }

  @Test
  void testNegativeResult() {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("testInstance", "greedy", 10.0)
            .put("testInstance", "optimum", 1.0)
            .put("testInstance", "size", 9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);

    ScoreCard card = new ScoreCard("one-negative", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), -1.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }
}
