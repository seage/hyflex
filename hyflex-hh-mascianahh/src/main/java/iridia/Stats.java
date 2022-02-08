package iridia;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class holds performance measures and computes aggregate statistics.
 * I couldn't find a way to make type invariance and generics work in a decent 
 * (read C++) way. Code is pretty ugly and maybe error-prone.
 */
public class Stats<T extends Number & Comparable<? super T>> {

    private T median;
    private T q1;
    private T q3;
    private ArrayList<T> measures;

    /**
     * Allocates the necessary data structures.
     */
    public Stats(T defaultValue) {
        measures = new ArrayList<T>();
        q1 = q3 = median = defaultValue;
    }

    /**
     * computes aggregate statistics from the collected measures.
     */
    public void updateStats() {
        if (measures.isEmpty()) {
            return;
        } else if (measures.size() == 1) {
            q1 = q3 = median = measures.get(0);
        } else {
            Collections.sort(measures);
            // compute the median
            int pos = measures.size() / 2;
            if (measures.size() % 2 == 1) {
                median = measures.get(pos);
            } else {
                // have to go through number...
                Number averaged = ((measures.get(pos - 1).doubleValue() + measures.get(pos).doubleValue()) / 2.0);
                median = (T) averaged;
            }

            // compute the quartiles
            q1 = measures.get((int) (measures.size() * 0.25));
            q3 = measures.get((int) (measures.size() * 0.75));
        }
    }

    /**
     * Adds a measure to the collection.
     *
     * @param measure measure
     */
    public void addMeasure(T measure) {
        measures.add(measure);
    }

    /**
     * @return the median
     */
    public T getMedian() {
        return median;
    }

    /**
     * @return the lower quartile
     */
    public T getQ1() {
        return q1;
    }

    /**
     * @return the upper quartile
     */
    public T getQ3() {
        return q3;
    }

    /**
     * @return the number of measures stored
     */
    public int getNumberOfMeasures() {
        return measures.size();
    }

}
