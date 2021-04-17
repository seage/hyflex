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
            Arrays.asList("tspTestInstance")));
      }
    };

    instancesMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("tspTestInstance", "greedy", 43.0)
            .put("tspTestInstance", "optimum", 1.0)
            .put("tspTestInstance", "size", 9.0));
      }
    };

    sc = new UnitMetricScoreCalculator(instancesMetadata, problemInstances, problems);
  }

  @Test
  void testOptimalScore() throws Exception {
    ScoreCard card = new ScoreCard("optimal", problems)
        .putInstanceScore("TSP", "tspTestInstance", 1.0);

    ScoreCard result = sc.calculateScore(card);

    assertEquals(
        1.0, result.getInstanceScore("TSP", "tspTestInstance"), 0.1);
  }

  @Test
  void testGreedyScore() throws Exception {
    ScoreCard card = new ScoreCard("greedy", problems)
        .putInstanceScore("TSP", "tspTestInstance", 43.0);

    ScoreCard result = sc.calculateScore(card);

    assertEquals(
        0.0, result.getInstanceScore("TSP", "tspTestInstance"), 0.1);
  }

  @Test
  void testMiddleScore() throws Exception {
    ScoreCard card = new ScoreCard("middle", problems)
        .putInstanceScore("TSP", "tspTestInstance", 22.0);

    ScoreCard result = sc.calculateScore(card);

    assertEquals(
        0.5, result.getInstanceScore("TSP", "tspTestInstance"), 0.1);
  }

  @Test
  void testWorseThanGreedyScore() throws Exception {
    ScoreCard card = new ScoreCard("worseThanGreedy", problems)
        .putInstanceScore("TSP", "tspTestInstance", 44.0);

    ScoreCard result = sc.calculateScore(card);

    assertEquals(
        0.0, result.getInstanceScore("TSP", "tspTestInstance"), 0.1);
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
            .put("tspTestInstance1", "greedy", 10.0)
            .put("tspTestInstance1", "optimum", 0.0)
            .put("tspTestInstance1", "size", 6.0)
            .put("tspTestInstance2", "greedy", 10.0)
            .put("tspTestInstance2", "optimum", 0.0)
            .put("tspTestInstance2", "size", 2.0));

        put("SAT", new ProblemInstanceMetadata()
            .put("satTestInstance1", "greedy", 10.0)
            .put("satTestInstance1", "optimum", 0.0)
            .put("satTestInstance1", "size", 6.0)
            .put("satTestInstance2", "greedy", 10.0)
            .put("satTestInstance2", "optimum", 0.0)
            .put("satTestInstance2", "size", 2.0));
      }
    };

    ScoreCard card = new ScoreCard("weightedMeanCalculation", problems1)
        .putInstanceScore("TSP", "tspTestInstance1", 10.0)
        .putInstanceScore("TSP", "tspTestInstance2", 0.0)
        .putInstanceScore("SAT", "satTestInstance1", 5.0)
        .putInstanceScore("SAT", "satTestInstance2", 5.0);
    
    UnitMetricScoreCalculator unitMetricScoreCalculator1 = 
        new UnitMetricScoreCalculator(instancesMetadata1, problemInstances1, problems1);

    ScoreCard result = unitMetricScoreCalculator1.calculateScore(card);

    assertEquals(0.0, result.getInstanceScore("TSP", "tspTestInstance1"), 0.01);
    assertEquals(1.0, result.getInstanceScore("TSP", "tspTestInstance2"), 0.01);
    /**                 6*0.0 + 2*1.0
     * weighted mean = --------------- = 2/8 = 1/4
     *                      6 + 2
     */
    assertEquals(0.25, result.getProblemScore("TSP"), 0.01);

    assertEquals(0.5, result.getInstanceScore("SAT", "satTestInstance1"), 0.01);
    assertEquals(0.5, result.getInstanceScore("SAT", "satTestInstance2"), 0.01);
    /**                 6*0.5 + 2*0.5
     * weighted mean = --------------- = 4/8 = 1/2
     *                      6 + 2
     */
    assertEquals(0.5, result.getProblemScore("SAT"), 0.01);

    assertEquals(0.375, result.getScore(), 0.01);
  }
}
