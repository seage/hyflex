package hyflex.chesc2011.launcher.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import hyflex.chesc2011.metrics.BenchmarkCalculator;

@Parameters(commandDescription = "Perform benchmark calculation on given results.")
public class CompetitionRunBenchmarkCalculator extends Command {

  @Parameter(names = {"--help"},
      help = true,
      description = "Displays this help information")
  private boolean help;

  @Parameter(names = {"--id"},
      required = true,
      description = "Name folder containing results.")
  public String id = "0";

  @Parameter(names = {"-m", "--metric"},
      required = true,
      description = "Name of the metric to be used "
      + "available values are: UnitMetric, F1Metric")
  public String metric = "UnitMetric";

  public boolean isHelp() {
    return help;
  }

  @Override
  public String toString() {
    return ""   
      + "\nhelp" + help
      + "\nid" + id
      + "\nmetric" + metric;
  }

  @Override
  public void performCommand() throws Exception {
    new BenchmarkCalculator().run(id, metric);
  }
}

