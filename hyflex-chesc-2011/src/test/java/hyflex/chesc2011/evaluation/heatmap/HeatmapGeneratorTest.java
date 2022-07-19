package hyflex.chesc2011.evaluation.heatmap;

import java.util.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.net.URL;
import org.junit.jupiter.api.Test;
import hyflex.chesc2011.Competition;

public class HeatmapGeneratorTest {
    // Path where the xml with results
    String xmlPath = "src/test/resources/hyflex/hyflex-chesc-2011/test-unit-metric-scores.xml";

    @Test
    void testLoadXMLFile() throws Exception {
        HeatmapGenerator hmg = new HeatmapGenerator();

        // Load the results
        hmg.loadXmlFile(xmlPath, Competition.algorithmAuthors);

        // test if the results isn't null
        assertNotNull(hmg.results);
        // test the algorithms num
        assertEquals(3, hmg.results.size());
        // test if the problems isn't null
        assertNotNull(hmg.problems);
        // test the problems num
        assertEquals(4, hmg.problems.size());

        // Test each of the results
        assertEquals(4, hmg.results.get(0).problemsResults.size());
        assertEquals(4, hmg.results.get(1).problemsResults.size());
        assertEquals(4, hmg.results.get(2).problemsResults.size());
    }

    @Test 
    void testSortResults() {
        HeatmapGenerator hmg = new HeatmapGenerator();
        
        // Load the results
        hmg.loadXMLFile(xmlPath, Competition.algorithmAuthors);
        // Sort the results
        hmg.sortResults();

        // Test the sorted order
        assertEquals("Algorithm2", hmg.results.get(0).name);
        assertEquals("Algorithm3", hmg.results.get(1).name);
        assertEquals("Algorithm1", hmg.results.get(2).name);
    }

    @Test
    void testResultsToList() throws Exception {
        HeatmapGenerator hmg = new HeatmapGenerator();

        // Load the results
        hmg.loadXMLFile(xmlPath, Competition.algorithmAuthors);
        // Sort the results
        hmg.sortResults();
        // Create list from results
        hmg.resultsToList();

        // Test if the list isn't null
        assertNotNull(hmg.algsOverRes);
        // Test the length
        assertEquals(3, hmg.algsOverRes.size());
        // Test the overall data
        assertEquals("Algorithm2", hmg.algsOverRes.get(0).get(0));
        assertEquals("0.9", hmg.algsOverRes.get(0).get(2));
        assertEquals("Algorithm3", hmg.algsOverRes.get(1).get(0));
        assertEquals("0.6", hmg.algsOverRes.get(1).get(2));
        assertEquals("Algorithm1", hmg.algsOverRes.get(2).get(0));
        assertEquals("0.3", hmg.algsOverRes.get(2).get(2));

        // Test the problems data
        assertEquals(hmg.problems.get(0), hmg.algsProbsRes.get(0).get(0).get(0));
        assertEquals(hmg.problems.get(1), hmg.algsProbsRes.get(0).get(1).get(0));
        assertEquals(hmg.problems.get(2), hmg.algsProbsRes.get(0).get(2).get(0));
        assertEquals(hmg.problems.get(3), hmg.algsProbsRes.get(0).get(3).get(0));
        
    }
}
