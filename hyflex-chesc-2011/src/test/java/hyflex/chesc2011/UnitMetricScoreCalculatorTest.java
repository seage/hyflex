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
  String[] problems = {"TSP"};
  
  //@SuppressWarnings("serial")
  Map<String, List<String>> problemInstances;

  //@SuppressWarnings("serial")
  Map<String, ProblemInstanceMetadata> instancesMetadata;
  
  ScoreCalculator sc;

  // @BeforeAll
  void init(double testInstanceOptimum, double testInstanceRandom, double testInstanceSize) {
    
    problemInstances = new HashMap<>() {{
        put("TSP", new ArrayList<>(
            Arrays.asList("testInstance")));
      }
    };

    instancesMetadata = new HashMap<>() {{
        put("TSP", new ProblemInstanceMetadata()
            .put("testInstance", "random", testInstanceRandom)
            .put("testInstance", "optimum", testInstanceOptimum)
            .put("testInstance", "size", testInstanceSize));
      }
    };

    sc = new UnitMetricScoreCalculator(problems, instancesMetadata, problemInstances);
  }

  @Test
  void testCalculateScoreBoundaries() throws Exception {
    init(0.0, 42.0, 9.0);

    List<ScoreCard> scoreCardList = new ArrayList<>();
    scoreCardList.add(
        new ScoreCard("optimal", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 0.0)
    );
    scoreCardList.add(
        new ScoreCard("random", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 42.0)
    );
    scoreCardList.add(
        new ScoreCard("middle", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 21.0)
    );

    List<ScoreCard> cards = sc.calculateScore(scoreCardList);
    assertEquals(3, cards.size());
    assertEquals(1.0, cards.get(0)
        .getInstanceScore(problems[0], problemInstances.get(problems[0]).get(0)), 0.1);
    assertEquals(0.0, cards.get(1)
        .getInstanceScore(problems[0], problemInstances.get(problems[0]).get(0)), 0.1);
    assertEquals(0.5, cards.get(2)
        .getInstanceScore(problems[0], problemInstances.get(problems[0]).get(0)), 0.1);
  }

  @Test
  void testExceptions() throws Exception {
    // Algorithm result is negative
    init(1.0, 10.0, 9.0);

    List<ScoreCard> scoreCardList0 = new ArrayList<>();
    scoreCardList0.add(
        new ScoreCard("one-negative", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), -1.0)
    );

    assertThrows(Exception.class, () -> sc.calculateScore(scoreCardList0));


    // Negative optimum
    init(-1.0, 10.0, 9.0);
    assertThrows(Exception.class, () -> sc.calculateScore(scoreCardList0));


    // Random is smaller than optimum
    init(1.0, 0.0, 9.0);
    assertThrows(Exception.class, () -> sc.calculateScore(scoreCardList0));


    // Both optimum and random are negative
    init(-2.0, -42.0, 9.0);
    assertThrows(Exception.class, () -> sc.calculateScore(scoreCardList0));


    // Instance size is negative
    init(2.0, 42.0, -9.0);
    assertThrows(Exception.class, () -> sc.calculateScore(scoreCardList0));


    // Algotithm is not on interval
    init(2.0, 42.0, 9.0);

    List<ScoreCard> scoreCardList1 = new ArrayList<>();
    scoreCardList1.add(
        new ScoreCard("better-than-optimum", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 1.0)
    );

    assertThrows(Exception.class, () -> sc.calculateScore(scoreCardList1));

    List<ScoreCard> scoreCardList2 = new ArrayList<>();
    scoreCardList2.add(
        new ScoreCard("worse-than-random", problems)
        .putInstanceScore(problems[0], problemInstances.get(problems[0]).get(0), 43.0)
    );

    assertThrows(Exception.class, () -> sc.calculateScore(scoreCardList2));
  }
}
