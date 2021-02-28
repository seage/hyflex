package hyflex.chesc2011;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "=")
public class CompetitionParameters {

  @Parameter(names = {"--help"},
      help = true,
      description = "Displays this help information")
  private boolean help;
  
  @Parameter(names = {"-t", "--timeout"},
      required = true,
      description = "The number represents timeout in milliseconds "
      + "for hyper-heuristics run on one instance"
      + "\n numer should be a positive integer")
  public Long timeout;

  @Parameter(names = {"-r", "--runs"},
      description = "The number of trials of hyper-heuristics on one instance"
      + "number should be a positive ingeger")
  public Integer runs = 31;

  @Parameter(names = {"-h", "--hyperheuristics"},
      required = true,
      variableArity = true,
      description = "Names of hyper-heuristics to be used in competition" 
      + "available values are [GIHH, LeanGIHH, EPH, PearlHunter, ISEA]")
  public List<String> hyperheurictics = new ArrayList<>();

  public boolean isHelp() {
    return help;
  }

  @Override
  public String toString() {
    return    "\nhelp"            + help
            + "\ntimeout"            + timeout
            + "\nruns"            + runs 
            + "\nhyperheurictics" + hyperheurictics;
  }
}
