/**
 * @author David Omrai
 */

package hyflex.chesc2011.metrics;

import hyflex.chesc2011.metrics.ResultsCard;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ResultsCardHandler {
  /**
   * Method reads the results file and stores the data into a map.
   * @param path Path where the file is stored.
   * @return Map with algorithm results.
   */
  public static ResultsCard loadCard(
      String[] problems, Path path, String[] domains, Map<String, List<String>> cardInstances)
      throws Exception {
    //Map<String, Map<String, Double>> results = new HashMap<>();
    String cardName = path.getFileName().toString();
    ResultsCard result = new ResultsCard(
        cardName.substring(0, cardName.lastIndexOf(".")), domains);

    Scanner scanner = new Scanner(new File(path.toString())).useDelimiter("\n");

    for (String problemId : problems) {
      
      if (scanner.hasNextLine() == false) {
        scanner.close();
        System.out.println("Not enough lines in " + path.toString() + " file.");
        return null;
      }

      Scanner line = new Scanner(scanner.nextLine()).useDelimiter(", ");

      for (String instanceId: cardInstances.get(problemId)) {

        if (line.hasNextLine() == false) {
          line.close();
          System.out.println("Not enough instances results in " + path.toString() + " file.");
          return null;
        }
        
        result.putInstanceValue(problemId, instanceId, Double.parseDouble(line.next()));
      }

      line.close();
    }
    scanner.close();

    return result;
  }

  /**
   * .
   * @param path .
   * @return .
   */
  public static String[] getCardsNames(Path path) {
    File resDir = new File(path.toString());

    String[] resFiles = resDir.list(new FilenameFilter() {
      @Override
      public boolean accept(File current, String name) {
        return new File(current, name).isFile();
      }
    });

    return resFiles;
  }
}
