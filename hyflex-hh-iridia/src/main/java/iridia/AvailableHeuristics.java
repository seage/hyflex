package iridia;

import AbstractClasses.ProblemDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;

/**
 * This class stores the available low level heuristics for the problem domain
 * at hand.
 */
public class AvailableHeuristics {

    private EnumMap<ProblemDomain.HeuristicType, ArrayList<LowLevelHeuristic>> heuristics;
    /**
     * parameter to be passed to low level heuristics
     */
    private int posTempSolution; 

    /**
     * low level heuristics should also collect statistics of their performances
     */
    private boolean stats;
    
    /**
     * Prepares the data structures.
     * 
     * @param posTempSolution solution used by low level heuristics (LS have to be applied until local minima is found)
     * @param stats true if the low level heuristics should also collect statistics of their performances
     */
    public AvailableHeuristics(int posTempSolution, boolean stats) {
        this.posTempSolution = posTempSolution;
        this.stats = stats;
        heuristics = new EnumMap<ProblemDomain.HeuristicType, ArrayList<LowLevelHeuristic>>(ProblemDomain.HeuristicType.class);
        for (ProblemDomain.HeuristicType ht : ProblemDomain.HeuristicType.values()) {              
            heuristics.put(ht, new ArrayList<LowLevelHeuristic>());
        }
    }

    /**
     * Adds a low level heuristics to the pool.
     *
     * @param id identifier of the heuristic used in ProblemDomain applyHeurisitcs
     * @param type type of the heuristic
     */
    public void add(int id, ProblemDomain.HeuristicType type) {
        ArrayList<LowLevelHeuristic> temp = heuristics.get(type);
        if (stats) {
            temp.add(new LowLevelHeuristicStats(id, temp.size(), posTempSolution, type));
        } else {
            temp.add(new LowLevelHeuristic(id, temp.size(), posTempSolution, type));            
        }
    }

    /**
     * Returns a collection of heuristics of the specified type.
     *
     * @param type type of heuristic
     * @return ArrayList containing available heuristics of this type
     */
    public ArrayList<LowLevelHeuristic> getHeuristicsOfType(ProblemDomain.HeuristicType type) {
        return heuristics.get(type);
    }

    /**
     * Returns a random of heuristic of the specified type.
     * 
     * @param type type of heuristic
     * @return random low level heuristic of the specified type
     */
    public LowLevelHeuristic getRandomHeuristicOfType(ProblemDomain.HeuristicType type) {
        ArrayList<LowLevelHeuristic> temp = heuristics.get(type);
        return temp.get(Shared.rng.nextInt(temp.size()));
    }

    /**
     * Comparator using for sorting the heuristics (e.g. LS for VND ordered by increasing time).
     */
    private class TimeStatsComparator implements Comparator {
        public int compare(Object ls1, Object ls2) {
            return (int) (((LowLevelHeuristicStats) ls1).getMedianTime() - ((LowLevelHeuristicStats) ls2).getMedianTime());
        }
    }


    /**
     * Comparator using for sorting the heuristics by increasing solution quality.
     */
    private class QualityStatsComparator implements Comparator {
        public int compare(Object ls1, Object ls2) {
            return (int) (((LowLevelHeuristicStats) ls1).getMedianQuality() - ((LowLevelHeuristicStats) ls2).getMedianQuality());
        }
    }    
    
    /**
     * Sorts the internal collection of low level heuristics of the specified type.
     * The heuristics are sorted in ascending median time for a single run.
     *
     * @param type type of heuristic
     */
    public void sortHeuristicsByTime(ProblemDomain.HeuristicType type) {
        if (stats) {
            ArrayList<LowLevelHeuristic> temp = heuristics.get(type);
            Collections.sort(temp, new TimeStatsComparator());
        } else {
            System.err.println("ERROR: no stats available.");
        }
    }

    /**
     * Sorts the internal collection of low level heuristics of the specified type.
     * The heuristics are sorted in ascending median time for a single run.
     *
     * @param type type of heuristic
     */
    public void sortHeuristicsByQuality(ProblemDomain.HeuristicType type) {
        if (stats) {
            ArrayList<LowLevelHeuristic> temp = heuristics.get(type);
            Collections.sort(temp, new QualityStatsComparator());
        } else {
            System.err.println("ERROR: no stats available.");
        }
    }

}
