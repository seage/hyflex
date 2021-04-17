package hyflex.chesc2011.metrics.calculators;

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
public class UnitMetricScoreCalculatorExceptionsTest {
  static String[] problems = {"TSP"};
  
  //@SuppressWarnings("serial")
  static Map<String, List<String>> problemInstances;
  
  @BeforeAll
  static void init() {
    
    problemInstances = new HashMap<>() {{
        put("TSP", new ArrayList<>(
            Arrays.asList("tspTestInstance")));
      }
    };
  }
 
  @Test
  void testBetterThanOptimal() throws Exception {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
          put("TSP", new ProblemInstanceMetadata()
              .put("tspTestInstance", "greedy", 42.0)
              .put("tspTestInstance", "optimum", 1.0)
              .put("tspTestInstance", "size", 9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);
  
    ScoreCard card = new ScoreCard("betterThanOptimal", problems)
        .putInstanceScore("TSP", "tspTestInstance", 0.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }

  @Test
  void testNegativeOptimum() {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("tspTestInstance", "greedy", 10.0)
            .put("tspTestInstance", "optimum", -1.0)
            .put("tspTestInstance", "size", 9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);
    
    ScoreCard card = new ScoreCard("negativeOptimum", problems)
        .putInstanceScore("TSP", "tspTestInstance", 1.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }

  @Test
  void testGreedySmallerThanOptimum() {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("tspTestInstance", "greedy", 0.0)
            .put("tspTestInstance", "optimum", 1.0)
            .put("tspTestInstance", "size", 9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);
    
    ScoreCard card = new ScoreCard("greedySmallerThanOptimum", problems)
        .putInstanceScore("TSP", "tspTestInstance", 0.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }

  @Test
  void testNegativeGreedyAndOptimum() {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("tspTestInstance", "greedy", -42.0)
            .put("tspTestInstance", "optimum", -2.0)
            .put("tspTestInstance", "size", 9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);
    
    ScoreCard card = new ScoreCard("negativeGreedyAndOptimum", problems)
        .putInstanceScore("TSP", "tspTestInstance", 1.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }

  @Test
  void testNegativeInstanceSize() {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("tspTestInstance", "greedy", 42.0)
            .put("tspTestInstance", "optimum", 2.0)
            .put("tspTestInstance", "size", -9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);
    
    ScoreCard card = new ScoreCard("negativeInstanceSize", problems)
        .putInstanceScore("TSP", "tspTestInstance", 1.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }

  @Test
  void testNegativeResult() {
    Map<String, ProblemInstanceMetadata> insMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("tspTestInstance", "greedy", 10.0)
            .put("tspTestInstance", "optimum", 1.0)
            .put("tspTestInstance", "size", 9.0));
      }
    };

    UnitMetricScoreCalculator csc = 
        new UnitMetricScoreCalculator(insMetadata, problemInstances, problems);

    ScoreCard card = new ScoreCard("negativeResult", problems)
        .putInstanceScore("TSP", "tspTestInstance", -1.0);

    assertThrows(Exception.class, () -> csc.calculateScore(card));
  }
}
