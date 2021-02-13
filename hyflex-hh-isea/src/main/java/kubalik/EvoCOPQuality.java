/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kubalik;

/**
 *
 * @author Kubalik
 */
public class EvoCOPQuality {
    public double fitness;
    public double solutionQuality;
    public long time;

    EvoCOPQuality(double fitness, double solutionQuality, long time){
        this.fitness = fitness;
        this.solutionQuality = solutionQuality;
        this.time = time;
    }
}
