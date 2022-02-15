package iridia;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import java.util.Random;

/**
 * Some general fixed parameters which are used across the code and are not
 * passed through constructors.
 */
public class Shared {

    // this parameters I was sick to bring them around and passing them through
    // constructors
    public static ProblemDomain problem = null;
    public static MyHyperHeuristic hyperHeuristic = null;
    public static Random rng = null;
    public static AvailableHeuristics availableHeuristics = null;

    public Configuration conf = Configuration.getInstance();
}
