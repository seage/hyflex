package hyflex.chesc2011.launcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import hyflex.chesc2011.launcher.commands.Command;
import hyflex.chesc2011.launcher.commands.CompetitionComputationCommand;
import hyflex.chesc2011.launcher.commands.CompetitionEvaluateCommand;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * Class is used as a launcher.
 * 
 * @author David Omrai
 */
public class Launcher {
  private static final Logger logger = 
      Logger.getLogger(Launcher.class.getName());

  @Parameter(names = "--help", help = true)
  private boolean help;

  /**
   * Method process user input and decides what command should be run.
   * @param args user input
   */
  public static void main(String[] args) {
    try {
      HashMap<String, Command> commands = new LinkedHashMap<>();
      commands.put("competition", new CompetitionComputationCommand());
      commands.put("evaluation", new CompetitionEvaluateCommand());

      Launcher launcher = new Launcher();
      JCommander jc = new JCommander(launcher);
      for (Entry<String, Command> e : commands.entrySet()) {
        jc.addCommand(e.getKey(), e.getValue());
      }
      jc.parse(args);
      
      if (args.length == 0 || launcher.help) {
        jc.usage();
        return;
      }
      launcher.run(commands.get(jc.getParsedCommand()));
    } catch (ParameterException ex) {
      logger.info(ex.getMessage());
      logger.info("Try to use --help");
    } catch (Exception ex) {
      logger.severe(ex.getMessage());
    }
  }

  private void run(Command cmd) throws Exception {
    logger.info("Hyflex running ...");
    cmd.performCommand();
    logger.info("Hyflex finished ...");
  }
}
