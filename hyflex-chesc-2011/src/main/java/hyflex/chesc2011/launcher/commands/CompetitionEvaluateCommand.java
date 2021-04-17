package hyflex.chesc2011.launcher.commands;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Class is used for competition evaluation.
 * 
 * @author David Omrai
 */
@Parameters(commandDescription = "Perform benchmark calculation on given results.")
public class CompetitionEvaluateCommand extends Command {

  @Parameter(names = {"--help"},
      help = true,
      description = "Displays this help information")
  private boolean help;

  @Parameter(names = {"--id"},
      required = true,
      description = "Name folder containing results.")
  public String id;

  @Parameter(names = {"-m", "--metric"},
      required = false,
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
    if (help == true) {
      JCommander jc = new JCommander(this);
      jc.usage();
      return;
    }

    switch (metric) {
      case "UnitMetric":
        new hyflex.chesc2011.metrics.calculators.BenchmarkCalculator().run(id, metric);
        break;
      case "F1Metric":
        new hyflex.chesc2011.legacy.BenchmarkCalculator().run(id);
        break;
      default:
    }
    
  }
}

