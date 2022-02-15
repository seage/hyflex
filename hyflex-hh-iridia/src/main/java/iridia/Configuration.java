/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package iridia;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author mascia
 */
public final class Configuration {

    private static Configuration instance;
    private Properties defaultProps;
    private Properties applicationProps;
    private Set<String> keys;

    private boolean debug;
    
    private Configuration() {
        try {
            load();
        } catch (Exception ex) {
            defaults();
            try {
                save();
            } catch (Exception ex2) {
                System.err.println("Can not write properties on disk.");
            }
        }
        // we also see if the user has specified some values
        setValues();
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public void defaults() {
        defaultProps = new Properties();
        defaultProps.setProperty("DEBUG", "false");
        defaultProps.setProperty("precision", "0.0000009");                        // Precision to be used when comparing solution qualities.

        defaultProps.setProperty("LL_MaxTimeToSetDepthOfSearch", "10000");         // Local search heuristics which take more than 10 seconds to reach a local minimum do not apply the DepthOfSearch parameter but keep the default 0.1.
        defaultProps.setProperty("LL_DepthOfSearch", "0.8892");                    // Framework parameter.
        defaultProps.setProperty("LL_IntensityOfMutation", "0.1528");              // Framework parameter.
        
        defaultProps.setProperty("RD_KeepBest", "2");                              // Keep at least k best even if they are dominated and not complementary.
        
        defaultProps.setProperty("LLSTATS_MaxFractionOfTime", "0.075");            // Maximum fraction of the total time for collecting stats on low level heuristics.
        defaultProps.setProperty("LLSTATS_MaxIterations", "25");                   // Maximum number of iteration for each low level heuristic for collecting stats.

        defaultProps.setProperty("ILS_ProbabilityAcceptingWorsening", "0.0");      // Probability of keeping a worsening solution (not restoring backup).
        defaultProps.setProperty("ILS_PerturbationSize", "1");                     // Number of times a MUTATION heuristic has to be applied.

        defaultProps.setProperty("IGFS_TemperatureAcceptingWorsening", "0.87");       // Temperature for probability of keeping a worsening solution (not restoring backup).
        defaultProps.setProperty("IGFS_numberOfRRmin", "1");                       // Number of ruin and reacrate selected uniformly randomly (min of dist).
        defaultProps.setProperty("IGFS_numberOfRRspread", "1");                    // Number of ruin and reacrate selected uniformly randomly (spread of dist).
        defaultProps.setProperty("IGFS_numberLS", "1");                           // Number of local searches after ruin and recreate.
        
        defaultProps.setProperty("INTERLEAVED_FractionORTBeforeDropping", "0.025"); // Fraction of the remaining time allowed to each heuristic before dropping.
    }

    public void load() throws FileNotFoundException, IOException {
        defaultProps = new Properties();
        FileInputStream in = new FileInputStream("defaultProperties");
        defaultProps.load(in);
        in.close();
    }

    public void save() throws FileNotFoundException, IOException {
        FileOutputStream out = new FileOutputStream("defaultProperties");
        defaultProps.store(out, "no comment");
        out.close();
    }

    public void setValues() {
        applicationProps = new Properties(defaultProps);
        keys = applicationProps.stringPropertyNames();

        // reading user set values
        Set<String> userKeys = System.getProperties().stringPropertyNames();
        for (String key : userKeys) {
            if (keys.contains(key)) {
                applicationProps.setProperty(key, System.getProperty(key));
//                System.out.println("User ovverides: " + key + ": " + System.getProperty(key));
            }
        }

        // listing the properties
//        applicationProps.list(System.out);
//        System.out.println("------------------------");

//        System.out.println("");
//        System.out.println("+--System Properties (set with -D parameter for the VM)--+");
//        System.out.println("|                                                        |");
//        printProperty("DEBUG", 54, System.out);
//        printProperty("precision", 54, System.out);
//        System.out.println("|                                                        |");
//        printProperty("LL_MaxTimeToSetDepthOfSearch", 54, System.out);
//        printProperty("LL_DepthOfSearch", 54, System.out);
//        printProperty("LL_IntensityOfMutation", 54, System.out);
//        System.out.println("|                                                        |");
//        printProperty("LLSTATS_MaxFractionOfTime", 54, System.out);
//        printProperty("LLSTATS_MaxIterations", 54, System.out);
//        System.out.println("|                                                        |");
//        printProperty("RD_KeepBest", 54, System.out);
//        System.out.println("|                                                        |");
//        printProperty("ILS_ProbabilityAcceptingWorsening", 54, System.out);
//        printProperty("ILS_PerturbationSize", 54, System.out);
//        printProperty("IGFS_TemperatureAcceptingWorsening", 54, System.out);        
//        printProperty("IGFS_numberOfRRmin", 54, System.out);        
//        printProperty("IGFS_numberOfRRspread", 54, System.out);        
//        printProperty("IGFS_numberLS", 54, System.out);                
//        System.out.println("|                                                        |");
//        printProperty("INTERLEAVED_FractionORTBeforeDropping", 54, System.out);
//        System.out.println("|                                                        |");
//        System.out.println("+--(SMART same parameters of ILS_*, IG set accordingly)--+");        
//        System.out.println("");

        debug = Boolean.valueOf(applicationProps.getProperty("DEBUG"));
    }

    private void printProperty(String key, int tab, PrintStream out) {
        out.printf("| " + key + "%1$" + (tab - key.length()) +"s |\n", applicationProps.getProperty(key));
    }

    public boolean DEBUG() {
        return debug;
    }

    public double getDouble(String key) {
        if (debug && !keys.contains(key)) {
            System.err.println("No parameter \"" + key + "\" found.");
            System.exit(1);
        }
        return Double.parseDouble(applicationProps.getProperty(key));
    }

    public double[] getDoubleArray(String key) {
        if (debug && !keys.contains(key)) {
            System.err.println("No parameter \"" + key + "\" found.");
            System.exit(1);
        }   
        String value = applicationProps.getProperty(key).trim();
        String[] stringVals = value.substring(1, value.length() - 1).split(",");
        double[] res = new double[stringVals.length];
        for (int i = 0; i < stringVals.length; i++) {
            res[i] = Double.parseDouble(stringVals[i]);
        }
        return res;
    }
    
    public int getInteger(String key) {
        if (debug && !keys.contains(key)) {
            System.err.println("No parameter \"" + key + "\" found.");
            System.exit(1);
        }
        return Integer.parseInt(applicationProps.getProperty(key));
    }

    public String getString(String key) {
        if (debug && !keys.contains(key)) {
            System.err.println("No parameter \"" + key + "\" found.");
            System.exit(1);
        }
        return applicationProps.getProperty(key);
    }

    public void setDouble(String key, double value) {
        if (debug && !keys.contains(key)) {
            System.err.println("No parameter \"" + key + "\" found.");
            System.exit(1);
        }
        applicationProps.setProperty(key, Double.toString(value));
    }

    public boolean getBoolean(String key) {
        if (debug && !keys.contains(key)) {
            System.err.println("No parameter \"" + key + "\" found.");
            System.exit(1);
        }
        return Boolean.parseBoolean(applicationProps.getProperty(key));
    }
}
