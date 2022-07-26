/**
 * Testing the Score Card helper
 * 
 * @author David Omrai
 */

package hyflex.chesc2011.metrics.calculators.scorecard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hyflex.chesc2011.evaluation.scorecard.ScoreCard;
import hyflex.chesc2011.evaluation.scorecard.ScoreCardHelper;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ScoreCardHelperTest {

  @Test
  void testEmptyScoreCardList() throws Exception {
    List<ScoreCard> results = new ArrayList<>();

    String jsonData = ScoreCardHelper.createResultsJsonString(results);
    assertEquals("{\"results\":[]}", jsonData);
  }

  @Test
  void testCreateResultsJsonString() throws Exception {
    // First result card
    ScoreCard firstCard = new ScoreCard(
        "a1", new String[] {"a1-p1", "a1-p2"});
    // Total score
    firstCard.setScore(0.5);
    // Problems result
    firstCard.putDomainScore("a1-p1", 0.5);
    firstCard.putDomainScore("a1-p2", 0.2);
    // Instances result
    firstCard.putInstanceScore(
        "a1-p1", "a1-p1-i1", 0.5);
    firstCard.putInstanceScore(
        "a1-p1", "a1-p1-i2", 0.5);
    firstCard.putInstanceScore(
        "a1-p2", "a1-p2-i1", 0.5);
    firstCard.putInstanceScore(
        "a1-p2", "a1-p2-i2", 0.5);

    // Second card
    ScoreCard secondCard = new ScoreCard(
        "a2", new String[] {"a2-p1", "a2-p2"});
    // Total score
    secondCard.setScore(0.6);
    // Problems result
    secondCard.putDomainScore("a2-p1", 0.5);
    secondCard.putDomainScore("a2-p2", 0.2);
    // Instances result
    secondCard.putInstanceScore(
        "a2-p1", "a2-p1-i1", 0.5);
    secondCard.putInstanceScore(
        "a2-p1", "a2-p1-i2", 0.5);
    secondCard.putInstanceScore(
        "a2-p2", "a2-p2-i1", 0.5);
    secondCard.putInstanceScore(
        "a2-p2", "a2-p2-i2", 0.5);
        
    // Results
    List<ScoreCard> results = new ArrayList<>() {{
        add(firstCard);
        add(secondCard);
      }
    };

    // Test the string
    String jsonData = ScoreCardHelper.createResultsJsonString(results);
    String expectedData = "{\"results\":[{\"scorePerInstance\"" 
        + ":{\"a1-p2\":{\"a1-p2-i2\":0.5,\"a1-p2-i1\":0.5},\"a1-p1\""
        + ":{\"a1-p1-i2\":0.5,\"a1-p1-i1\":0.5}},\"algorithmName\":\""
        + "a1\",\"totalScore\":0.5,\"scorePerProblem\":{\"a1-p2\":0.2,"
        + "\"a1-p1\":0.5}},{\"scorePerInstance\":{\"a2-p2\":{\"a2-p2-i1\""
        + ":0.5,\"a2-p2-i2\":0.5},\"a2-p1\":{\"a2-p1-i1\":0.5,\"a2-p1-i2\"" 
        +  ":0.5}},\"algorithmName\":\"a2\",\"totalScore\":0.6,\"scorePerProblem"
        + "\":{\"a2-p2\":0.2,\"a2-p1\":0.5}}]}";
    assertEquals(expectedData, jsonData);
  }

}
