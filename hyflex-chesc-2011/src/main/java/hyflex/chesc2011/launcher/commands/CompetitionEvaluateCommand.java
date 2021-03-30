/**
 * @author David Omrai
 */

package hyflex.chesc2011.launcher.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import hyflex.chesc2011.legacy.BenchmarkCalculator;



@Parameters(commandDescription = "Perform the Competition tasks on given hyper-heuristic")
public class CompetitionEvaluateCommand extends Command {
    
  @Parameter(names = {"--help"},
      help = true,
      description = "Displays this help information")
  private boolean help;

  @Parameter(names = {"--id"},
      description = "The name of the results folder")
  public String id = "";


  public boolean isHelp() {
    return help;
  }

  @Override
  public String toString() {
    return "\nhelp" + help;
  }

  @Override
  public void performCommand() throws Exception {
    new BenchmarkCalculator().run(id);
  }
}