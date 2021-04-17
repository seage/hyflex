package hyflex.chesc2011.metrics.calculators;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  void testWeightedMeanCalculation() throws Exception {
    String[] problems1 = {"TSP", "SAT"};

    Map<String, List<String>> problemInstances1 = new HashMap<>() {{
        put("TSP", new ArrayList<>(
            Arrays.asList(new String[]{"tspTestInstance1", "tspTestInstance2"})));
        put("SAT", new ArrayList<>(
            Arrays.asList(new String[]{"satTestInstance1", "satTestInstance2"})));
      }
    };

    Map<String, ProblemInstanceMetadata> instancesMetadata1 = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("tspTestInstance1", "greedy", 42.0)
            .put("tspTestInstance1", "optimum", 1.0)
            .put("tspTestInstance1", "size", 9.0)
            .put("tspTestInstance2", "greedy", 12.0)
            .put("tspTestInstance2", "optimum", 0.0)
            .put("tspTestInstance2", "size", 6.0));

        put("SAT", new ProblemInstanceMetadata()
            .put("satTestInstance1", "greedy", 42.0)
            .put("satTestInstance1", "optimum", 1.0)
            .put("satTestInstance1", "size", 9.0)
            .put("satTestInstance2", "greedy", 12.0)
            .put("satTestInstance2", "optimum", 0.0)
            .put("satTestInstance2", "size", 6.0));
      }
    };

    ScoreCard card = new ScoreCard("weightedMeanCalculation", problems1)
        .putInstanceScore(problems1[0], problemInstances1.get(problems1[0]).get(0), 1.0)
        .putInstanceScore(problems1[0], problemInstances1.get(problems1[0]).get(1), 6.0)
        .putInstanceScore(problems1[1], problemInstances1.get(problems1[1]).get(0), 1.0)
        .putInstanceScore(problems1[1], problemInstances1.get(problems1[1]).get(1), 6.0);
    
    UnitMetricScoreCalculator unitMetricScoreCalculator1 = 
        new UnitMetricScoreCalculator(instancesMetadata1, problemInstances1, problems1);

    ScoreCard result = unitMetricScoreCalculator1.calculateScore(card);

    assertEquals(0.8, result.getProblemScore(problems1[0]), 0.01);

    assertEquals(0.8, result.getProblemScore(problems1[1]), 0.01);

    assertEquals(0.8, result.getScore(), 0.01);
  }
}
