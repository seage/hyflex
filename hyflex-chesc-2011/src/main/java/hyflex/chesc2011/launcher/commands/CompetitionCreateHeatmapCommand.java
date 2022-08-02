package hyflex.chesc2011.launcher.commands;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import hyflex.chesc2011.Competition;
import hyflex.chesc2011.evaluation.heatmap.HyflexHeatmapGenerator;

/**
 * Class is used for creating a heatmap using evaluated results.
 * 
 * @author David Omrai
 */
@Parameters(commandDescription = "Create a heatmap using given evaluated results.")
public class CompetitionCreateHeatmapCommand extends Command {


  @Parameter(names = {"--help"},
      help = true,
      description = "Displays this help information")
  private boolean help;

  @Parameter(names = {"--id"},
      required = true,
      description = "Name folder containing evaluated results.")
  public String id;

  public boolean isHelp() {
    return help;
  }

  @Override
  public String toString() {
    return ""   
      + "\nhelp" + help
      + "\nid" + id;
  }

  @Override
  public void performCommand() throws Exception {
    if (help) {
      JCommander jc = new JCommander(this);
      jc.usage();
      return;
    }
    HyflexHeatmapGenerator.createHeatmap(id, Competition.algorithmAuthors);
  }
}

