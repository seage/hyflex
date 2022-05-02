package hyflex.chesc2011.launcher.commands;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import hyflex.chesc2011.evaluation.heatmap.HeatmapGenerator;

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

  @Parameter(names = {"-h", "--heatmap"},
      required = false,
      description = "Indicates if to create also a heatmap from results")
  public boolean createHeatmap = false;

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
        new hyflex.chesc2011.evaluation.scorecard.ScoreCardBenchmarkCalculator().run(id, metric);
        if (createHeatmap) {
          // todo add the score card heatmap creation
        }
        break;
      case "F1Metric":
        new hyflex.chesc2011.evaluation.calculators.legacy.F1MetricBenchmarkCalculator().run(id);
        break;
      default:
    }
    
  }
}

