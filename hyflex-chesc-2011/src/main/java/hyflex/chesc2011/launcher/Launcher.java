/**
 * @author David Omrai
 */

package hyflex.chesc2011.launcher;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import hyflex.chesc2011.launcher.commands.Command;
import hyflex.chesc2011.launcher.commands.CompetitionRunBenchmarkCalculator;
import hyflex.chesc2011.launcher.commands.CompetitionRunCommand;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;



public class Launcher {
  @Parameter(names = "--help", help = true)
  private boolean help;

  /**
   * Method process user input and decides what command should be run.
   * @param args user input
   */
  public static void main(String[] args) {
    try {
      HashMap<String, Command> commands = new LinkedHashMap<>();
      commands.put("competition-run", new CompetitionRunCommand());
      commands.put("competition-benchmark-calculator", new CompetitionRunBenchmarkCalculator());

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
      System.out.println(ex.getMessage());
      System.out.println("Try to use --help");
    } catch (Exception ex) {
      System.out.println(ex.getMessage());
    }
  }

  private void run(Command cmd) throws Exception {
    System.out.println("Hyflex running ...");
    cmd.performCommand();
    System.out.println("Hyflex finished ...");
  }
}
