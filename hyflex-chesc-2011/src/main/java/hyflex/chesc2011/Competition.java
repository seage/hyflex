package hyflex.chesc2011;

import java.io.File;
// Java libraries
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class Competition {
  private static long  time = 5000;
  private static long  currentTimeMillis = System.currentTimeMillis();

  /**
   * Main method is used for testing each algorithm on all problem domains and instances.
   * @param args input parameters
  */
  public static void main(String [] args) {
    if (args[0].matches("\\d+") == false) {
      System.out.println("ERROR, wrong number format");
      return;
    }
    if (args[1].matches("\\d+") == false) {
      System.out.println("ERROR, wrong number format");
      return;
    }

    long timeout                = Long.parseLong(args[0]);
    int algRuns                 = Integer.parseInt(args[1]);
    String[] inputAlgorithmIDs  = Arrays.asList(args)
    .subList(2, args.length).toArray(new String[0]);

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

    //set time
    time = timeout;

    //make output folder
    makeFolder();

    //run hyper-herutistic on all problem domains
    for (String algorithmID: inputAlgorithmIDs) {
      if (!Arrays.asList(algorithmIDs).contains(algorithmID)) {
        System.out.println("ERROR, wrong algorithm name " + algorithmID);
        continue;
      }
      ArrayList<ArrayList<Double>> results = new ArrayList<ArrayList<Double>>();

      for (String problemID: problemIDs) {
        results.add(run(algorithmID, problemID, algRuns));
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
  public static ArrayList<Double> run(String algorithmID, String problemID, int algRuns) {
    int     [] instanceIDs  = {0, 1, 2, 3, 4};
    ArrayList<Double> resultsMedian = new ArrayList<Double>();

    for (int instanceID: instanceIDs) {
      resultsMedian.add(
                    getMedian(
                        runCompetition(algorithmID, problemID, instanceID, algRuns)
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
  public static Double getMedian(ArrayList<Double> array) {
    int middle = 0;

    try {
      if (array.size() == 1) {
        return array.get(0);
      }
      Collections.sort(array);
      middle = array.size() / 2;
      middle = middle > 0 && middle % 2 == 0 ? middle - 1 : middle;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.exit(0);
    }

    return array.get(middle);
  }

  /**
   * Method creates a folder where is stored the output of program run.
   */
  public static void makeFolder() {
    try {
      File       theDir             = new File(
          "output/results/" + currentTimeMillis + "/"
      );
      theDir.mkdirs();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.exit(0);
    }
  }

  /**
   * Method prints the content of a given array into a file.
   * @param arrays      array of arrays with results
   * @param algorithmID name of given hyper-heuristic algorithm
   */
  public static void makeCard(ArrayList<ArrayList<Double>> arrays, String algorithmID) {    
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
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(0);
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
  public static ArrayList<Double> runCompetition(
      String  algorithmID, 
      String  problemID, 
      int     instanceID,
      int     algRuns
  ) {
    CompetitionRunner r = new CompetitionRunner(algorithmID, problemID, instanceID, time, algRuns);
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