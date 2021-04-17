package hyflex.chesc2011.metrics.calculators;

import hyflex.chesc2011.metrics.scorecard.ScoreCard;
import java.util.List;

public interface ScoreCalculator {
  public List<ScoreCard> calculateScores(List<ScoreCard> card) throws Exception;
}
