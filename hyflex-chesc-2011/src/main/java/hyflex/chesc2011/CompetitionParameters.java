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
  
  @Parameter(names = {"-t", "--timetout"},
      required = true,
      description = "The numer represents timeout for hyper-heuristics run on one instance")
  public Long time;

  @Parameter(names = {"-r", "--runs"},
      description = "The number of trials of hyper-heuristics on one instance")
  public Integer runs = 31;

  @Parameter(names = {"-h", "--hyperheurictics"},
      required = true,
      variableArity = true,
      description = "Names of hyper-heuristics to be used in competition")
  public List<String> hyperheurictics = new ArrayList<>();

  public boolean isHelp() {
    return help;
  }

  @Override
  public String toString() {
    return    "\nhelp"            + help
            + "\ntime"            + time
            + "\nruns"            + runs 
            + "\nhyperheurictics" + hyperheurictics;
  }
}
