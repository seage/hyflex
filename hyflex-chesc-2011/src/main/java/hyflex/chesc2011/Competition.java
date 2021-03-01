package hyflex.chesc2011;

// JComander libraries
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
// Java libraries
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Competition {
  private static final String[] algorithmIDs = {
    "GIHH", "LeanGIHH", "PearlHunter", "EPH", "ISEA"
  };
  private static final String[] problemIDs = {
    "SAT", "BinPacking", "PersonnelScheduling", "FlowShop", "TSP", "VRP"
  };

  private long currentTimeMillis;
  final CompetitionParameters mainArgs = new CompetitionParameters();

  /**
   * Main method is used for testing each algorithm on all problem domains and instances.
   * 
   * @param args input parameters
   */
  public static void main(String[] args) {
    try {
      Competition competition = new Competition();
      competition.handleInputArgs(args);
      competition.run();
    } catch (ParameterException e) {
      System.out.println(e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void handleInputArgs(String[] args) throws ParameterException {
    JCommander jcommander = new JCommander(mainArgs);
    try {
      jcommander.setProgramName("CHeSC 2011 competition");
      jcommander.parse(args);
      if (mainArgs.isHelp()) {
        jcommander.usage();
        throw new ParameterException("");
      }
    } catch (ParameterException e) {
      jcommander.usage();
      throw e;
    }
  }

  /**
   * Method is used for testing each algorithm on all problem domains and instances.
   */
  public void run() throws Exception {
    // create output folder
    new File("output/results/" + currentTimeMillis + "/").mkdirs();

    // run hyper-herutistic on all problem domains
    for (String algorithmID : mainArgs.hyperheurictics) {
      if (!Arrays.asList(algorithmIDs).contains(algorithmID)) {
        System.out.println("ERROR, wrong algorithm name " + algorithmID);
        continue;
      }
      List<List<Double>> results = new ArrayList<List<Double>>();

      for (String problemID : problemIDs) {
        results.add(runAlg(algorithmID, problemID, mainArgs.runs, mainArgs.timeout));
      }
      System.out.println(results);
      makeResultsCard(results, algorithmID);
    }
  }

  /** 
   * Method is used for testing all instances of problem domain on given algorithm.
   * 
   * @param algorithmID name of given hyper-heuristic algorihtm
   * @return ArrayList with median values of received results
   */
  public List<Double> runAlg(String algorithmID, String problemID, Integer algRuns, Long timeout)
      throws Exception {
    List<Double> resultsMedian = new ArrayList<Double>();

    for (int instanceIx = 0; instanceIx <= 4; instanceIx++) {
      List<Double> instanceResults = 
          runCompetition(algorithmID, problemID, instanceIx, algRuns, timeout);
      Double median = getMedianFromInstanceResults(instanceResults);
      resultsMedian.add(median);
    }

    return resultsMedian;
  }

  /**
   * Method returns the median of given array.
   * 
   * @param instanceResults array of double numbers
   * @returns
   */
  public static Double getMedianFromInstanceResults(List<Double> instanceResults) throws Exception {
    int middle = 0;

    if (instanceResults.size() == 1) {
      return instanceResults.get(0);
    }
    Collections.sort(instanceResults);
    middle = instanceResults.size() / 2;
    middle = middle > 0 && middle % 2 == 0 ? middle - 1 : middle;

    return instanceResults.get(middle);
  }

  /**
   * Method creates a file with given results.
   * File represents a competition card for each competitor.
   * 
   * @param results array of arrays with results
   * @param algorithmID name of given hyper-heuristic algorithm
   */
  public void makeResultsCard(List<List<Double>> results, String algorithmID) throws IOException {
    try (
        FileWriter fwriter =
            new FileWriter("output/results/" + currentTimeMillis + "/" + algorithmID + ".txt");
        PrintWriter printer = new PrintWriter(fwriter);) {
      String line = "";
      for (List<Double> array : results) {
        line = "";
        for (Double median : array) {
          line += median + ", ";
        }
        printer.println(line.substring(0, line.length() - 2));
      }
    }
  }

  /**
   * Method runs CompetitionRunner on given algorithm and instance of problem domain, specific time.
   * 
   * @param algorithmID name of given hyper-heuristic
   * @param problemID name of given problem domain
   * @param instanceIx name of given problem domain instance
   * @param algRuns number of runs per problem instance
   * @return ArrayList with result for each run
   */
  public ArrayList<Double> runCompetition(
      String algorithmID,
      String problemID, 
      Integer instanceIx,
      Integer algRuns, 
      Long timeout) {
    CompetitionRunner r =
        new CompetitionRunner(algorithmID, problemID, instanceIx, timeout, algRuns);
    r.start();
    try {
      r.join();
    } catch (InterruptedException e) {
      System.out.println(e);
      System.exit(0);
    }
    return r.getResults();
  }
}
