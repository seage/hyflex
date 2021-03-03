package hyflex.chesc2011.launcher.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import hyflex.chesc2011.BenchmarkCalculator;



@Parameters(commandDescription = "Perform the Competition tasks on given hyper-heuristic")
public class CompetitionEvaluateCommand extends Command {
    
  @Parameter(names = {"--help"},
      help = true,
      description = "Displays this help information")
  private boolean help;

  @Parameter(names = {"-f", "--folder"},
      description = "The name of the results folder")
  public String folder = "";


  public boolean isHelp() {
    return help;
  }

  @Override
  public String toString() {
    return "\nhelp" + help;
  }

  @Override
  public void performCommand() throws Exception {
    new BenchmarkCalculator().run(folder);
  }
}