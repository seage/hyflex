/**
 * @author David Omrai
 */

package hyflex.chesc2011.launcher.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import hyflex.chesc2011.Competition;

import java.util.List;



@Parameters(commandDescription = "Perform the Competition tasks on given hyper-heuristic")
public class CompetitionRunCommand extends Command {
    
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
  public Long id = Long.parseLong("0");
  
  @Parameter(names = {"-h", "--hyperheuristics"},
      required = true,
      variableArity = true,
      description = "Names of hyper-heuristics to be used in competition " 
      + "available values are: GIHH, LeanGIHH, EPH, PearlHunter, ISEA")
  public List<String> hyperheurictics;

  public boolean isHelp() {
    return help;
  }

  @Override
  public String toString() {
    return ""   
      + "\nhelp" + help
      + "\ntimeout" + timeout
      + "\nruns" + runs 
      + "\nhyperheurictics" + hyperheurictics;
  }

  @Override
  public void performCommand() throws Exception {
    new Competition().run(hyperheurictics, timeout * 1000, runs, id);
  }
}