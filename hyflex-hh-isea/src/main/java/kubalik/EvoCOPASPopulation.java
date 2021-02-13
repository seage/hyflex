/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kubalik;

/**
 *
 * @author Kubalik
 */

import java.util.*;
import java.util.ArrayList;

/**
 * @author
 * 11.09.2007
 * 10:58:44
 */
public class EvoCOPASPopulation extends ArrayList<EvoCOPActionSequence> {

    public int drawerSize;
    public int maxGenes;
    public int popSize;     //--- popSize = drawerSize * maxGenes
    public double pC;
    public double pM;
    public double pBitflip;
    public int nTournament;
    //---
    private EvoCOPRandomGenerator rand = EvoCOPRandomGenerator.getInstance();
    protected Random generator = rand.getGenerator();

    public EvoCOPASPopulation() {
    }

    /**
     * initialize some config values
     * @return
     */
    public final void init(int drawerSize, int maxGenes, double pC, double pM, double pBitflip, int nTournament) {
        this.drawerSize = drawerSize;
        this.maxGenes = maxGenes;
        this.popSize = this.drawerSize * this.maxGenes;
        this.pC = pC;
        this.pM = pM;
        this.pBitflip = pBitflip;
        this.nTournament = nTournament;
    }


    /**
     * metoda vygeneruje jedince do jednotlivych supliku dle podminky (cislo supliku <= poctu aktivnich akci)
     * @throws Exception
     */
    public final void generateDrawers() {
        double pActive = 0.50;    //--- p-st aktivni akce [%]

        //--- vymaze vsechny AS
        this.clear();

        //--- vygeneruje nove
        for (int i = 1; i <= maxGenes; i++) {   //--- cislo supliku
            int j = 0;
            do {
                EvoCOPActionSequence AS = new EvoCOPActionSequence(maxGenes, pActive);
                AS.countActions();  //--- spocita aktivni akce
                if (AS.nActive >= i) {  //--- aktivnich akci muze byt hodne, ale to nevadi
                    this.add(AS);
                    j++;
                }
                else{   //--- aktivnich akci je malo, je treba pridat
                    int k = 0;
                    while(AS.nActive < i){
                        if(AS.get(k).actionType.equals("NOP")){
                            EvoCOPAction a = AS.get(k);
                            a.actionType = a.activeActionType;  //--- zmen akci na aktivni
                            AS.set(k, a);   //--- dej upravenou akci zpet na jeji misto v AS
                            AS.nActive++;   //--- inkrementuj pocet aktivnich akci
                        }
                        k++;
                    }
                    //--- pridej AS do populace
                    this.add(AS);
                    j++;
                }
            } while (j < drawerSize);
//            System.out.print("\n" + i + ". suplik hotov ...");
        }
//        System.out.println("");
    }

    //--- Spocita ohodnoceni action sequence s cislem idAS
    public void evaluateSingleAS(EvoCOPSolution prototype, EvoCOPSolution candidatePrototype, int idAS) {
        EvoCOPSolution s;
        double f = 0;

        EvoCOPActionSequence AS = this.get(idAS);
        s = prototype.applyActionSequence(AS);
        //--- penalizace seq. akci ktera s prototyem nic neudela.
        if (prototype.equalsSolution(s)) {
            f = Double.MAX_VALUE;
            AS.setFitness(f);
            AS.setSolutionQuality(f);
            AS.setTime(100000);
            this.set(idAS, AS);
        }
        else{
            EvoCOPQuality q = s.countFitness(candidatePrototype);
            AS.setFitness(q.fitness);
            AS.setSolutionQuality(q.solutionQuality);
            AS.setTime(q.time);
            this.set(idAS, AS);
        }

//        //--- TODO: Kontrola
//        if(idAS % 20 == 0)
//            System.out.println();
//        System.out.print(".");
    }

    /**
     * @return best action sequenci
     */
    public EvoCOPActionSequence getBestAS() throws Exception {

        EvoCOPActionSequence best = this.get(0);
        for (int i = 0; i < this.size(); i++) {
            if(best.getSolutionQuality() > this.get(i).getSolutionQuality())
                best = this.get(i);
            else if((best.getSolutionQuality() == this.get(i).getSolutionQuality()) && (best.getTime() > this.get(i).getTime()))
                best = this.get(i);
        }
        return (EvoCOPActionSequence) best.deepCopy();
    }

    protected void rewritePerson(int index, EvoCOPActionSequence child) throws Exception {
        this.add(index, child);
        this.remove(index + 1);
    }

//******************************************************************************
    /**
     * run tournament selection once and return winner
     * @return an exact copy of the selected actionsequence
     */
    protected EvoCOPActionSequence runTournament(EvoCOPSolution prototype) throws Exception{
        int i = 0;
        double bestQ = 1.0e20;  //--- vitezna solution quality
        int selectedDrawer;

        EvoCOPActionSequence bestAS = null;
        selectedDrawer = generator.nextInt(maxGenes);
        while (i < nTournament) {
//            selectedDrawer = generator.nextInt(maxGenes);
            EvoCOPActionSequence AS = this.get(selectedDrawer * drawerSize + generator.nextInt(drawerSize));
            double q = AS.getSolutionQuality();
//            System.out.println(q);
            if (i == 0) {
                bestQ = q;
                bestAS = AS;
            }
            if(q < bestQ) {
                bestQ = q;
                bestAS = AS;
            }
            i++;
            //System.out.println("turnament bestQ " + bestQ);
        }
//        return (EvoCOPActionSequence) bestAS.clone();
        return (EvoCOPActionSequence) bestAS.deepCopy();
    }

    /**
     * conducts the following algorithm:
     * do
     * {Parent1, Parent2} <- Tournament(OldPop)
     * if (Pcross > Random(0,1)) then {child1,child2} <- cross(Parent1,Parent2)
     * else {child1,child2} <- mutate(Parent1,Parent2)
     * NewPop <- {child1,child2}
     * until (NewPop complete}
     * @todo adjust this comment to the implementation :-)
     * @return new population of action sequences
     */
//    predelat spravne zarazeni do supliku
    public int runEvolutionaryCycle(EvoCOPSolution prototype, EvoCOPSolution candidatePrototype) throws Exception{
        //long startTime = System.currentTimeMillis();
        int ev = 0;
        int worst = 0;

        EvoCOPActionSequence parent1 = this.runTournament(prototype);
        EvoCOPActionSequence parent2 = this.runTournament(prototype);
        EvoCOPActionSequence child1;
        EvoCOPActionSequence child2;

        if (pC > generator.nextDouble()) {
            child2 = parent1.doCrossover(parent2);
            child1 = parent1;
        } else{
            child1 = parent1;
            child2 = parent2;
        }
        if (pM > generator.nextDouble()) {
            child1.doMutate(pBitflip);
        }
        if (pM > generator.nextDouble()) {
            child2.doMutate(pBitflip);
        }
        ev += child1.evaluate(prototype, candidatePrototype);
        ev += child2.evaluate(prototype, candidatePrototype);

        //--- Umisteni child1 do populace s omezenim na dane supliky
        worst = 0;
        for (int j = 0; j < child1.countActions() * drawerSize; j++) {
            //--- hlavni kriterium kvality je fitness
            if (this.get(j).getSolutionQuality() > this.get(worst).getSolutionQuality())
                worst = j;
        }
        if (this.get(worst).getSolutionQuality() > child1.getSolutionQuality()) {
            this.add(worst, child1);    //--- TODO: nejde to udelat jednou akci?
            this.remove(worst + 1);
        }

        //--- Umisteni child2 do populace s omezenim na dane supliky
        worst = 0;
        for (int j = 0; j < child2.countActions() * drawerSize; j++) {
            //--- hlavni kriterium kvality je fitness
            if (this.get(j).getSolutionQuality() > this.get(worst).getSolutionQuality())
                worst = j;
        }
        if (this.get(worst).getSolutionQuality() > child2.getSolutionQuality()) {
            this.add(worst, child2);    //--- TODO: nejde to udelat jednou akci?
            this.remove(worst + 1);
        }

        return ev;
    }
}

