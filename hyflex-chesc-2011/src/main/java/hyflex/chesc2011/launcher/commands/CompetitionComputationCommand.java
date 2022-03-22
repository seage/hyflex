package hyflex.chesc2011.launcher.commands;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import hyflex.chesc2011.Competition;
import java.util.Arrays;
import java.util.List;

/**
 * Class is used for competition run.
 * 
 * @author David Omrai
 */
@Parameters(commandDescription = "Perform the Competition tasks on given hyper-heuristic")
public class CompetitionComputationCommand extends Command {
    
  @Parameter(names = {"--help"},
      help = true,
      description = "Displays this help information")
  private boolean help;

  @Parameter(names = {"-t", "--timeout"},
      required = true,
      description = "The number represents timeout in seconds "
      + "for hyper-heuristics run on one instance"
      + "numer should be a positive integer")
  public Long timeout;

  @Parameter(names = {"-n", "--runs"},
      description = "The number of trials of hyper-heuristics on one instance "
      + "number should be a positive ingeger")
  public Integer runs = 31;

  @Parameter(names = {"--id"},
      description = "The name of the results folder")
  public String id = "0";
  
  @Parameter(names = {"-h", "--hyperheuristics"},
      required = true,
      variableArity = true,
      description = "Names of hyper-heuristics to be used in competition " + 
      "available values are:" +
      "AdapHH," + 
      "LeanGIHH," + 
      "EPH," + 
      "PHUNTER," + 
      "ISEA," + 
      "GISS," +
      "Clean," +
      "Clean02," +
      "GenHive," +
      "SelfSearch," +
      "HaeaHH," +
      "HsiaoCHeSCHH," +
      "SA_ILS," +
      "DynILS_b," +
      "DynILS," +
      "LaroseML," +
      "HAHA," +
      "NAHH," +
      "Ant_Q," +
      "XCJ," +
      "ACO_HH," +
      "KSATS_HH," +
      "AVEG_Nep")
  public List<String> hyperheurictics;

  @Parameter(names = {"-p", "--problems"},      
      variableArity = true,
      description = "Names of problems to be used in competition " 
      + "available values are: SAT, BinPacking, PersonnelScheduling, FSP, TSP, VRP, QAP")
  public List<String> problems = Arrays.asList(Competition.problemIDs);

  public boolean isHelp() {
    return help;
  }

  @Override
  public String toString() {
    return ""   
      + "\nhelp" + help
      + "\ntimeout" + timeout
      + "\nruns" + runs 
      + "\nhyperheurictics" + hyperheurictics
      + "\nproblems" + problems;
  }

  @Override
  public void performCommand() throws Exception {
    if (help == true) {
      JCommander jc = new JCommander(this);
      jc.usage();
      return;
    }
    new Competition().run(hyperheurictics, problems, timeout * 1000, runs, id);
  }
}