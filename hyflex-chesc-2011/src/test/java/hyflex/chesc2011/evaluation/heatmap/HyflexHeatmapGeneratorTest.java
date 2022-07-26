/**
 * Class contains two unit tests testing the heatmap
 * creation
 * 
 * @author David Omrai
 */

package hyflex.chesc2011.evaluation.heatmap;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import hyflex.chesc2011.Competition;
import java.io.FileWriter;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

public class HyflexHeatmapGeneratorTest {
  // Path where the xml with results
  String jsonPath = "/hyflex/hyflex-chesc-2011/test-unit-metric-scores.json";

  @Test
  void testLoadXmlFile() throws Exception {
    // Load the results
    try (InputStream jsonInputStream = 
        HyflexHeatmapGeneratorTest.class.getResourceAsStream(jsonPath)) {

      String heatmapSvg = HyflexHeatmapGenerator.getHeatmapString(
          jsonInputStream, "test", Competition.algorithmAuthors);
      
      assertNotEquals("", heatmapSvg);
    }
  }

  @Test
  void testCreateHeatmap() throws Exception {
    // Load the results
    try (InputStream jsonInputStream = 
        HyflexHeatmapGeneratorTest.class.getResourceAsStream(jsonPath)) {

      String heatmapSvg = HyflexHeatmapGenerator.getHeatmapString(
          jsonInputStream, "test", Competition.algorithmAuthors);
      
      // Test the result
      assertNotNull(heatmapSvg);

      // Store the svg file
      String tmpDir = System.getProperty("java.io.tmpdir");

      try (FileWriter fileWriter = new FileWriter(tmpDir + "/heatmap.svg");) {
        fileWriter.write(heatmapSvg);
      }     
    }
  }
}
