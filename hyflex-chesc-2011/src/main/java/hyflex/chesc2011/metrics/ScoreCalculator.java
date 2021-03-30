package hyflex.chesc2011.metrics;

import java.util.List;

public interface ScoreCalculator {
  public List<ScoreCard> calculateScore(List<ScoreCard> card) throws Exception;
}
