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


public class Competition {
  private long                    currentTimeMillis = System.currentTimeMillis();
  final CompetitionParameters     mainArgs          = new CompetitionParameters();

  /**
   * Main method is used for testing each algorithm on all problem domains and instances.
   * @param args input parameters
  */
  public static void main(String [] args) {
    
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

  void handleInputArgs(String [] args) throws ParameterException {
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
    String  [] algorithmIDs = {
      "GIHH",
      "LeanGIHH",
      "PearlHunter",
      "EPH",
      "ISEA"
    };

    String  [] problemIDs   = {
      "SAT", 
      "BinPacking", 
      "PersonnelScheduling", 
      "FlowShop",
      "TSP",
      "VRP"
    };

    //make output folder
    makeFolder();

    //run hyper-herutistic on all problem domains
    for (String algorithmID: mainArgs.hyperheurictics) {
      if (!Arrays.asList(algorithmIDs).contains(algorithmID)) {
        System.out.println("ERROR, wrong algorithm name " + algorithmID);
        continue;
      }
      ArrayList<ArrayList<Double>> results = new ArrayList<ArrayList<Double>>();

      for (String problemID: problemIDs) {
        results.add(run(algorithmID, problemID, mainArgs.runs, mainArgs.timeout));
      }
      System.out.println(results);
      makeCard(results, algorithmID);
    }
  }

  /**
   * Method is used for testing all instances of problem domain on given algorithm.
   * @param algorithmID name of given hyper-heuristic algorihtm
   * @return            ArrayList with median values of received results
   */
  public ArrayList<Double> run(
      String algorithmID, 
      String problemID, 
      Integer algRuns, 
      Long timeout
  ) throws Exception {
    int     [] instanceIDs  = {0, 1, 2, 3, 4};
    ArrayList<Double> resultsMedian = new ArrayList<Double>();

    for (int instanceID: instanceIDs) {
      resultsMedian.add(
                    getMedian(
                        runCompetition(algorithmID, problemID, instanceID, algRuns, timeout)
                    )
      );
    }

    return resultsMedian;
  }

  /**
   * Method returns the median of given array.
   * @param array array of double numbers
   * @returns
   */
  public static Double getMedian(ArrayList<Double> array) throws Exception {
    int middle = 0;
    
    if (array.size() == 1) {
      return array.get(0);
    }
    Collections.sort(array);
    middle = array.size() / 2;
    middle = middle > 0 && middle % 2 == 0 ? middle - 1 : middle;
    
    return array.get(middle);
  }

  /**
   * Method creates a folder where is stored the output of program run.
   */
  public void makeFolder() throws Exception {
    File       theDir             = new File(
        "output/results/" + currentTimeMillis + "/"
    );
    theDir.mkdirs();
  }

  /**
   * Method prints the content of a given array into a file.
   * @param arrays      array of arrays with results
   * @param algorithmID name of given hyper-heuristic algorithm
   */
  public void makeCard(
      ArrayList<ArrayList<Double>> arrays, 
      String algorithmID
  ) throws IOException {
    try (
        FileWriter fwriter = new FileWriter(
            "output/results/" + currentTimeMillis + "/" + algorithmID + ".txt"
        );
        PrintWriter printer = new PrintWriter(fwriter);
    ) {
      String line = "";
      for (ArrayList<Double> array: arrays) {
        line = "";
        for (Double median: array) {
          line += median + ", ";
        }
        printer.println(line.substring(0, line.length() - 2));
      }
    }
  }

  /**
   * Method runs CompetitionRunner on given algorithm and instance of problem domain, specific time.
   * @param algorithmID name of given hyper-heuristic
   * @param problemID   name of given problem domain
   * @param instanceID  name of given problem domain instance
   * @param algRuns     number of runs per problem instance
   * @return            ArrayList with result for each run
   */
  public ArrayList<Double> runCompetition(
      String  algorithmID, 
      String  problemID, 
      Integer instanceID,
      Integer algRuns,
      Long    timeout
  ) {
    CompetitionRunner r = new CompetitionRunner(
        algorithmID, 
        problemID, 
        instanceID, 
        timeout, 
        algRuns
      );
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
