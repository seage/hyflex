package hyflex.chesc2011.evaluation.calculators;

import hyflex.chesc2011.evaluation.scorecard.ScoreCard;

import java.util.List;

/**
 * Interface for score calculators.
 * 
 * @author David Omrai
 */
public interface ScoreCalculatorInterface {
  public List<ScoreCard> calculateScores(List<ScoreCard> card) throws Exception;
}
