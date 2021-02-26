package hyflex.chesc2011;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "=")
public class CompetitionParameters {

  @Parameter(names = {"-h", "--help"},
      help = true,
      description = "Displays help informatioin")
  private boolean help;
  
  @Parameter(names = {"-t", "--timetout"},
      required = true,
      description = "Numer represents timeout for algorithm run on one instance")
  private Long time;

  @Parameter(names = {"-r", "--runs"},
      description = "Number of repeat of algorithm on one instance")
  private Integer runs = 31;

  @Parameter(names = {"-a", "--algorithms"},
      required = true,
      variableArity = true,
      description = "Name of hyper-heuristics to be used in competition")
  public List<String> algorithms = new ArrayList<>();

  public boolean isHelp() {
    return help;
  }

  @Override
  public String toString() {
    return    "\nhelp"       + help
            + "\ntime"       + time
            + "\nruns"       + runs 
            + "\nalgorithms" + algorithms;
  }
}
