package hyflex.chesc2011.evaluation.calculators;

import hyflex.chesc2011.evaluation.metadata.ProblemInstanceMetadata;
import hyflex.chesc2011.evaluation.scorecard.ScoreCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seage.score.ScoreCalculator;

/**
 * Class is used for calculating the unit metric score.
 * 
 * @author David Omrai
 */
public class UnitMetricScoreCalculator implements HyflexScoreCalculator {
  /**
   * Map represents weights for each problem domain.
   */
  @SuppressWarnings("serial")
  private final Map<String, Double> problemsWeightsMap = new HashMap<>() {
    {
      put("SAT", 1.0);
      put("TSP", 1.0);
      put("FSP", 1.0);
      put("QAP", 1.0);
    }
  };

  private String[] problems;
  private Map<String, ProblemInstanceMetadata> metadata;
  private Map<String, List<String>> problemsInstances;

  /**
   * Constructor.
   */
  public UnitMetricScoreCalculator(Map<String, ProblemInstanceMetadata> metadata,
      Map<String, List<String>> problemsInstances, String[] problems) {
    this.problems = problems;
    this.metadata = metadata;
    this.problemsInstances = problemsInstances;
  }

  /**
   * Method calculates card score.
   * @param card Card with algorithm results.
   * @return ScoreCard with scores.
   */
  public ScoreCard calculateScore(ScoreCard card) throws Exception {
    ScoreCard result = new ScoreCard(card.getName(), problems);

    List<Double> problemsScores = new ArrayList<>();
    List<Double> problemsWeights = new ArrayList<>();

    for (String problemId : problems) {

      List<Double> instancesScores = new ArrayList<>();
      List<Double> sizes = new ArrayList<>();

      for (String instanceId : problemsInstances.get(problemId)) {
        double instanceScore = ScoreCalculator.calculateInstanceScore(
            metadata.get(problemId).get(instanceId, "optimum"),
            metadata.get(problemId).get(instanceId, "greedy"),
            card.getInstanceScore(problemId, instanceId)
        );

        result.putInstanceScore(problemId, instanceId, instanceScore);

        instancesScores.add(instanceScore);

        if (metadata.get(problemId).get(instanceId, "size") < 0) {
          throw new Exception(
              "Bad input values: size of " + instanceId + " instance is negative.");
        }

        sizes.add(metadata.get(problemId).get(instanceId, "size"));
      }

      double problemScore = ScoreCalculator.calculateProblemScore(
          sizes, instancesScores.stream().mapToDouble(d -> d).toArray()
      );
      
      result.putDomainScore(problemId, problemScore);

      problemsScores.add(problemScore);
      problemsWeights.add(problemsWeightsMap.get(problemId));
    }

    result.setScore(ScoreCalculator.calculateExperimentScore(problemsScores));

    return result;
  }


  /**
   * Method calculates the scores for given algorithm problem results.
   * 
   * @param cards ScoreCards with algorithms results.
   * @return ScoreCard with scores for each problem domain and total score.
   */
  public List<ScoreCard> calculateScores(List<ScoreCard> cards) throws Exception {
    List<ScoreCard> results = new ArrayList<>();

    for (ScoreCard card : cards) {
      ScoreCard result = calculateScore(card);

      results.add(result);
    }

    return results;
  }
}