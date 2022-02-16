/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kubalik;

import java.util.Random;

/**
 *
 * @author Kubalik
 */
public class EvoCOPLowLevelH implements Cloneable{

    private int id;
    private int name;
    private double depthOfSearch;
    private double intensityOfMutation;
    private int typeLLH;     // KUBA: mutation/localSearch/crossover/ruin-recreate
    private int status;      // KUBA: active/inactive
    private EvoCOPRandomGenerator rand = EvoCOPRandomGenerator.getInstance();
    protected Random generator = rand.getGenerator();


    // prazdna inicializace LLH
    public EvoCOPLowLevelH(){
    }

    // Nahodne nainicializuje LLH
    public int initLLH(int id){
        int doneOK = 0;
        double addValue = 0.15;
        int type;

        do{
            switch(type = generator.nextInt(3)){   // vybere typ akce
                // local search
                case 0: //we must check that there are some local search heuristics in this problem domain
                        if (EvoCOPHyFlexManager.local_search_heuristics != null) {
                            this.id = id;
                            typeLLH = 0;
                            name = EvoCOPHyFlexManager.local_search_heuristics[generator.nextInt(EvoCOPHyFlexManager.local_search_heuristics.length)];
                            depthOfSearch = EvoCOPHyFlexManager.startMinLocalLLH + addValue * generator.nextDouble();  // zvoli dos z intervalu (0.1, 0.5)
                            intensityOfMutation = EvoCOPHyFlexManager.startMinLocalLLH + addValue * generator.nextDouble();  // zvoli iom z intervalu (0.1, 0.5)
                            doneOK = 1;     // inicializace probehla vporadku
                        }
                        break;
                // mutation
                case 1: //we must check that there are some mutation search heuristics in this problem domain
                        if (EvoCOPHyFlexManager.mutation_heuristics != null) {
                            this.id = id;
                            typeLLH = 1;
                            name = EvoCOPHyFlexManager.mutation_heuristics[generator.nextInt(EvoCOPHyFlexManager.mutation_heuristics.length)];
                            depthOfSearch = EvoCOPHyFlexManager.startMinMutationLLH + addValue * generator.nextDouble();  // zvoli dos z intervalu (0.1, 0.5)
                            intensityOfMutation = EvoCOPHyFlexManager.startMinMutationLLH + addValue * generator.nextDouble();  // zvoli iom z intervalu (0.1, 0.5)
                            doneOK = 1;     // inicializace probehla vporadku
                        }
                        break;
                // ruin_recreate
                case 2: //we must check that there are some ruin-recreate search heuristics in this problem domain
                        if (EvoCOPHyFlexManager.ruin_recreate_heuristics != null) {
                            this.id = id;
                            typeLLH = 2;
                            name = EvoCOPHyFlexManager.ruin_recreate_heuristics[generator.nextInt(EvoCOPHyFlexManager.ruin_recreate_heuristics.length)];
                            depthOfSearch = EvoCOPHyFlexManager.startMinRuinLLH + addValue * generator.nextDouble();  // zvoli dos z intervalu (0.1, 0.5)
                            intensityOfMutation = EvoCOPHyFlexManager.startMinRuinLLH + addValue * generator.nextDouble();  // zvoli iom z intervalu (0.1, 0.5)
                            doneOK = 1;     // inicializace probehla vporadku
                        }
                        break;
            }
        }while(doneOK == 0);
        status = 1;
        return type;    //--- vrati typ akce
    }

    // Nahodne nainicializuje LLH
    public int initLLH(int id, int type){
        int doneOK = 0;

        switch(type){   // vybere typ akce
            // local search
            case 0: //we must check that there are some local search heuristics in this problem domain
                    if (EvoCOPHyFlexManager.local_search_heuristics != null) {
                        this.id = id;
                        typeLLH = 0;
                        name = EvoCOPHyFlexManager.local_search_heuristics[generator.nextInt(EvoCOPHyFlexManager.local_search_heuristics.length)];
                        depthOfSearch = EvoCOPHyFlexManager.startMinLocalLLH + EvoCOPHyFlexManager.addValueLocalLH * generator.nextDouble();
                        doneOK = 1;     // inicializace probehla vporadku
                    }
                    break;
            // mutation
            case 1: //we must check that there are some mutation search heuristics in this problem domain
                    if (EvoCOPHyFlexManager.mutation_heuristics != null) {
                        this.id = id;
                        typeLLH = 1;
                        name = EvoCOPHyFlexManager.mutation_heuristics[generator.nextInt(EvoCOPHyFlexManager.mutation_heuristics.length)];
                        intensityOfMutation = EvoCOPHyFlexManager.startMinMutationLLH + EvoCOPHyFlexManager.addValueMutationLH * generator.nextDouble();
                        doneOK = 1;     // inicializace probehla vporadku
                    }
                    break;
            // ruin_recreate
            case 2: //we must check that there are some ruin-recreate search heuristics in this problem domain
                    if (EvoCOPHyFlexManager.ruin_recreate_heuristics != null) {
                        this.id = id;
                        typeLLH = 2;
                        name = EvoCOPHyFlexManager.ruin_recreate_heuristics[generator.nextInt(EvoCOPHyFlexManager.ruin_recreate_heuristics.length)];
                        intensityOfMutation = EvoCOPHyFlexManager.startMinRuinLLH + EvoCOPHyFlexManager.addValueRuinLH * generator.nextDouble();
                        doneOK = 1;     // inicializace probehla vporadku
                    }
                    break;
        }
        if(doneOK == 0)
            type = this.initLLH(id);   //--- vyber nejaky typ LLH nahodne
        status = 1;

        return type;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getName(){
        return name;
    }

    public void setName(int name){
        this.name = name;
    }

    public double getDOS(){
        return depthOfSearch;
    }

    public void setDOS(double dos){
        this.depthOfSearch = dos;
    }

    public double getIOM(){
        return intensityOfMutation;
    }

    public void setIOM(double iom){
        this.intensityOfMutation = iom;
    }

    public int getTypeLLH(){
        return typeLLH;
    }

    public void setTypeLLH(int type){
        this.typeLLH = type;
    }

    public int getStatus(){
        return this.status;
    }

    public void setStatus(int status){
        this.status = status;
    }

    protected Object clone(){
        EvoCOPLowLevelH pom = null;
        try{
            pom = (EvoCOPLowLevelH)super.clone();
            pom.id = this.id;
            pom.depthOfSearch = this.depthOfSearch;
            pom.intensityOfMutation = this.intensityOfMutation;
            pom.typeLLH = this.typeLLH;
            pom.name = this.name;
            pom.status = this.status;
        }
        catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return pom;
    }
}
