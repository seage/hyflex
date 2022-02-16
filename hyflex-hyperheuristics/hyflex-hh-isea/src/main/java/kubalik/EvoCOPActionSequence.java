/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kubalik;

import java.util.ArrayList;
import java.util.Random;

public class EvoCOPActionSequence extends ArrayList<EvoCOPAction> implements Cloneable {
    //set the serialVersionUID to version number

    private static final long serialVersionUID = 1L;
    public double fitness;
    public double solutionQuality;
    public long time;
    public int maxGenes;    //--- maximalni pocet akci, delka sekvence
    public int nActive;     //--- pocet aktivnich akci
//    private String problem;
//    private double pActive;
    private EvoCOPRandomGenerator rand = EvoCOPRandomGenerator.getInstance();
    private Random generator = rand.getGenerator();

    /**
     * constructor
     * sets the maxGenes value
     */
    public EvoCOPActionSequence(int maxGenes) {
        this.maxGenes = maxGenes;
    }

    /**
     * creates a new ActionSequence with actions
     * defined by the parameter problem
     */
    public EvoCOPActionSequence(int maxGenes, double pActive) {
        this.maxGenes = maxGenes;
        int i = 0;
        while (i < maxGenes) {
//            Object o = this.getActionClassInstance();
            EvoCOPAction a = new EvoCOPAction();
            a.setActionType(pActive);
            if (this.add(a)) {
                i++;
            }
        }
    }

    /**
     * shallow copy method
     * copies all data from this actionsequence to a new actionsequence
     * @return exact copy of this actionsequence element
     */
    public EvoCOPActionSequence clone() {
        
        EvoCOPActionSequence copy = (EvoCOPActionSequence) super.clone();
        copy.fitness = this.fitness;
        copy.solutionQuality = this.solutionQuality;

        return copy;
    }

    public EvoCOPActionSequence deepCopy() throws Exception{
        EvoCOPActionSequence copy = new EvoCOPActionSequence(maxGenes);

        for(int i=0; i<this.size(); i++){
            EvoCOPAction a = (EvoCOPAction) this.get(i).clone();
            copy.add(a);
        }
        copy.fitness = this.fitness;
        copy.solutionQuality = this.solutionQuality;
        copy.time = this.time;
        copy.nActive = this.nActive;

        return copy;
    }

    /**
     * count actions
     * @return number of skore actions
     */
    public int countActions() {
        int count = 0;

        for (int i = 0; i < this.size(); i++) {
            EvoCOPAction a = this.get(i);
            if (a.actionType.equals("NOP")) {
            } else {
                count++;
            }
        }
        this.nActive = count;
        return count;
    }

    /**
     * vytiskne sekvenci akci
     * @throws Exception
     */
    protected void printAction() {
        for (int i = 0; i < this.size(); i++) {
            System.out.print(this.get(i) + " ");

        }
    }
//******************************************************************************


    /**
     * assigns the fitness value to this ActionSequence
     */
    protected void setFitness(double f) {
        this.fitness = f;
    }

    /**
     * @return the fitness value of this ActionSequence
     */
    protected double getFitness() {
        return fitness;
    }

    protected void setSolutionQuality(double sq) {
        this.solutionQuality = sq;
    }

    /**
     * @return the fitness value of this ActionSequence
     */
    protected double getSolutionQuality() {
        return solutionQuality;
    }

    /**
     * assigns the fitness value to this ActionSequence
     */
    protected void setTime(long t) {
        this.time = t;
    }

    /**
     * @return the fitness value of this ActionSequence
     */
    protected long getTime() {
        return time;
    }

    /**
     * odonoceni sekvence akci, aplikovani na prototyp a vypocitani fitness
     * @param prototype
     * @param fitnessStrategy
     */
    public int evaluate(EvoCOPSolution prototype, EvoCOPSolution candidatePrototype) {
        EvoCOPSolution s;
        double f = 0;
        s = prototype.applyActionSequence(this);
        if (prototype.equalsSolution(s)) {
            //--- minimalizacni problem
            f = Double.MAX_VALUE;
            this.setFitness(f);
            this.setSolutionQuality(f);
            return 0;   //--- fitness se nepocitala
        }
        EvoCOPQuality q = s.countFitness(candidatePrototype);
        this.setFitness(q.fitness);
        this.setSolutionQuality(q.solutionQuality);
        this.setTime(q.time);
        return 1;   //--- fitness se pocitala
    }

    /**
     * cross this actionsequence with the parameter ActionSequence as
     * @return newly created actionsequence with actions of both parents
     */
    protected EvoCOPActionSequence doCrossover(EvoCOPActionSequence as) throws Exception{
        EvoCOPActionSequence child1 = new EvoCOPActionSequence(maxGenes);
        EvoCOPActionSequence child2 = new EvoCOPActionSequence(maxGenes);
        //add the index of all chosen actions from first parent to this list
        ArrayList<Integer> c1IndexList = new ArrayList<Integer>(maxGenes);
        for (int x = 0; x < maxGenes; x++) {
            c1IndexList.add(x, 0);
        }
        //add the index of all chosen actions from second parent to this list
        ArrayList<Integer> c2IndexList = new ArrayList<Integer>(maxGenes);
        for (int x = 0; x < maxGenes; x++) {
            c2IndexList.add(x, 0);
        }
        int i = 0;
        while (i < maxGenes) {
            //take AS either from first parent, or from second parent
            if (generator.nextInt() % 2 == 0) { //choose first parent
                int iChoosenAction = generator.nextInt(maxGenes);
                //try to add an action from parent1, if already chosen go back to choose parent
                if (c1IndexList.get(iChoosenAction) == 0) {
                    EvoCOPAction a = this.get(iChoosenAction);
                    child1.add(a.clone());
                    c1IndexList.set(iChoosenAction, 1);
                    i++;
                }
            } // choose second parent
            else {
                int iChoosenAction = generator.nextInt(maxGenes);
                if (c2IndexList.get(iChoosenAction) == 0) {
                    EvoCOPAction a = as.get(iChoosenAction);
                    child1.add(a.clone());
                    c2IndexList.set(iChoosenAction, 1);
                    i++;
                }
            }
        }

        int e = 0;
        //add all non chosen actions to child2
        while (e < maxGenes) {
            boolean flag1 = false;
            if (c1IndexList.get(e) == 0) {
                flag1 = false;
            }
            if (c1IndexList.get(e) == 1) {
                flag1 = true;
            }


            boolean flag2 = false;
            if (c2IndexList.get(e) == 0) {
                flag2 = false;
            }
            if (c2IndexList.get(e) == 1) {
                flag2 = true;
            }

            //add action from first and second parent to child2
            if (!flag1 && !flag2) {
                EvoCOPAction a = this.get(e);
                child2.add(a.clone());

                EvoCOPAction c = as.get(e);
                child2.add(c.clone());
                e++;
            }
            //add action from first parent to child2
            if (!flag1 && flag2) {
                EvoCOPAction a = this.get(e);
                child2.add(a.clone());
                e++;
            }
            //add action from second parent to child2
            if (flag1 && !flag2) {
                EvoCOPAction c = as.get(e);
                child2.add(c.clone());
                e++;
            }
            //prevent non finishing loop when in both parents the same action has been selected
            if (flag1 && flag2) {
                e++;
            }
        }
        //make this AS contains all actions from created child1
        this.clear();
        this.addAll(child1);
        return child2;
    }

    /**
     * mutate an action of this actionsequence
     */
    protected void doMutate(double pBitflip) throws Exception{
        int i = 0;
        //for every action mutate if pM (probability of mutation) is greater than random
        while (i < this.size()) {
            if (pBitflip > generator.nextDouble()) {
                EvoCOPAction a = this.get(i);
                EvoCOPAction b = a.clone();
                b.mutate();
                this.set(i, b);
            }
            i++;
        }
    }
}
