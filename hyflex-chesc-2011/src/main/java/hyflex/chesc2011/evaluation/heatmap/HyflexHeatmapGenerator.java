/**
 * Program generates a heatmap, visualizating given data from hyflex experiment
 * 
 * @author David Omrai
 */

package hyflex.chesc2011.evaluation.heatmap;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.seage.score.heatmap.HeatmapGenerator;


public class HyflexHeatmapGenerator {
  // Path where the file with results is stored
  protected static String resultsSvgFile = "./results/%s/heatmap.svg";
  // Path where the file with results is stored
  protected static String resultsJsonFile = "./results/%s/unit-metric-scores.xml";

  /**
   * Protecting the class.
   */
  private HyflexHeatmapGenerator() {}

  protected static void createHeatmapFile(
      String renderedTemplate, String experimentId) throws IOException {
    // Output the file
    String resultsSvgFilePath = String.format(resultsSvgFile, experimentId);

    try (FileWriter fileWriter = new FileWriter(resultsSvgFilePath);) {
      fileWriter.write(renderedTemplate);
    }
  }

  /**
   * Method returns the heatmap with given data.
   * @param jsonStream Experiment result file input stream.
   * @param experimentId Id of the experiment.
   * @param algAuthors Map of algorithms authors.
   * @return
   */
  protected static String getHeatmapString(
      InputStream jsonInputStream, String experimentId, Map<String, String> algAuthors
  ) throws IOException {
    return HeatmapGenerator.createHeatmap(jsonInputStream, experimentId, algAuthors);
  }

  /**
   * Method receives neccesary data and create the result svg file.
   * @param experimentId id of the competition experiment
   * @param algAuthors map of algorithm authors
   */
  public static void createHeatmap(
      String experimentId, Map<String, String> algAuthors
  ) throws IOException {
    String jsonResultsPath = String.format(resultsJsonFile, experimentId);

    try (InputStream jsonInputStream = HeatmapGenerator.class.getResourceAsStream(
        jsonResultsPath)) {
      createHeatmapFile(getHeatmapString(jsonInputStream, experimentId, algAuthors), experimentId);
    }
  }
}

