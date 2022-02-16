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

public class EvoCOPAction implements Cloneable {

    private EvoCOPActionParams parameters;   // KUBA: parametry akci
    //-------
    protected String actionType;
    protected String activeActionType;
    public static String[] actionBase = new String [] {"addLLH","removeLLH","changeLLH"};   //--- ,"moveLLH","swapLLH"
                                            // KUBA: nazvy akci, omezit se pouze na aktivni akce
                                            //       NOP bude samostatne
                                            //       Udelat actionBase statickou promennou tridy Action
    protected double[] actionProb;          // KUBA: pravdepodobnosti akci
    private EvoCOPRandomGenerator rand = EvoCOPRandomGenerator.getInstance();
    protected Random generator = rand.getGenerator();

    /**
     * constructor
     */
    public EvoCOPAction() {
        this.parameters = new EvoCOPActionParams();
    }

    /**
     * assign the actionType to this action
     */
    protected final void setActionType(double pActive) {
        if (generator.nextDouble() < pActive) {
            this.actionType = actionBase[generator.nextInt(actionBase.length)];
            this.activeActionType = this.actionType;
        } else {
            this.actionType = "NOP";
            this.activeActionType = actionBase[generator.nextInt(actionBase.length)];
        }
        initParameters();
    }

    /**
     * initialize the parameters
     * put parameters into parameters HashMap
     */
    public void initParameters() {

        if(this.activeActionType.equals("addLLH")){
            int doneOK = 0;
            do{
                switch(generator.nextInt(3)){   // vybere typ akce
                    // local search
                    case 0: //we must check that there are some local search heuristics in this problem domain
                            if (EvoCOPHyFlexManager.local_search_heuristics != null) {
                                parameters.typeLLH = 0;
                                parameters.name = EvoCOPHyFlexManager.local_search_heuristics[generator.nextInt(EvoCOPHyFlexManager.local_search_heuristics.length)];
                                parameters.id1 = EvoCOPHyFlexManager.nbActiveLLH;  // nastavi id nove LLH
                                parameters.id2 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH+1);  // vybere pozici, kam tu novou LLH umisti
                                parameters.depthOfSearch = EvoCOPHyFlexManager.startMinLocalLLH + EvoCOPHyFlexManager.addValueLocalLH * generator.nextDouble();  // zvoli dos z intervalu (0.1, 0.5)
                                doneOK = 1;     // inicializace probehla vporadku
                            }
                            break;
                    // mutation
                    case 1: //we must check that there are some mutation search heuristics in this problem domain
                            if (EvoCOPHyFlexManager.mutation_heuristics != null) {
                                parameters.typeLLH = 1;
                                parameters.name = EvoCOPHyFlexManager.mutation_heuristics[generator.nextInt(EvoCOPHyFlexManager.mutation_heuristics.length)];
                                parameters.id1 = EvoCOPHyFlexManager.nbActiveLLH;  // nastavi id nove LLH
                                parameters.id2 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH+1);  // vybere pozici, kam tu novou LLH umisti
                                parameters.intensityOfMutation = EvoCOPHyFlexManager.startMinMutationLLH + EvoCOPHyFlexManager.addValueMutationLH * generator.nextDouble();  // zvoli iom z intervalu (0.1, 0.5)
                                doneOK = 1;     // inicializace probehla vporadku
                            }
                            break;
                    // ruin_recreate
                    case 2: //we must check that there are some ruin-recreate search heuristics in this problem domain
                            if (EvoCOPHyFlexManager.ruin_recreate_heuristics != null) {
                                parameters.typeLLH = 2;
                                parameters.name = EvoCOPHyFlexManager.ruin_recreate_heuristics[generator.nextInt(EvoCOPHyFlexManager.ruin_recreate_heuristics.length)];
                                parameters.id1 = EvoCOPHyFlexManager.nbActiveLLH;  // nastavi id nove LLH
                                parameters.id2 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH+1);  // vybere pozici, kam tu novou LLH umisti
                                parameters.intensityOfMutation = EvoCOPHyFlexManager.startMinRuinLLH + EvoCOPHyFlexManager.addValueRuinLH * generator.nextDouble();  // zvoli iom z intervalu (0.1, 0.5)
                                doneOK = 1;     // inicializace probehla vporadku
                            }
                            break;
                }
            }while(doneOK == 0);
        }
        else if(this.activeActionType.equals("removeLLH"))
        {
            parameters.id1 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH);  // vybere jednu z existujicich LLH aktualniho prototypu
        }
        else if(this.activeActionType.equals("moveLLH"))
        {
            parameters.id1 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH);  // vybere jednu z existujicich LLH aktualniho prototypu
            if(generator.nextBoolean() == true){
                parameters.id2 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH / 2);  // hodnota, o kolik se ta LLH posune (vlevo/vpravo)
                if(parameters.id2 == 0)
                    parameters.id2 = 1;
            }
            else{
                parameters.id2 = - generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH / 2);  // hodnota, o kolik se ta LLH posune (vlevo/vpravo)
                if(parameters.id2 == 0)
                    parameters.id2 = -1;
            }
        }
        else if(this.activeActionType.equals("swapLLH"))
        {   // vybere dve stavajici LLH z aktualniho prototypu, mohou byt i stejne, protoze neni zaruceno, ze jich bude >1
            parameters.id1 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH);  // vybere prvni z existujicich LLH aktualniho prototypu
            do{
                parameters.id2 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH);  // vybere druhou z existujicich LLH aktualniho prototypu
            }while(parameters.id1 == parameters.id2);
        }
        else if(this.activeActionType.equals("changeLLH"))
        {   //--- Pro nastaveni parametru, potrebuji znat,
            //--- jakeho typu je akce s timto id v aktualnim prototypu
            parameters.id1 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH);  //--- vybere jednu z existujicich LLH aktualniho prototypu
            //--- TODO: zmenit typeLLH a name
            //--- zmeni LLH
            if(generator.nextInt(100) < 50){
                //--- zvol novy typ LLH
                parameters.typeLLH = -1;
                do{
                    int newType = generator.nextInt(3);
                    switch(newType){
                        case 0:
                            if(EvoCOPHyFlexManager.local_search_heuristics != null)
                                parameters.typeLLH = newType;
                            break;
                        case 1:
                            if(EvoCOPHyFlexManager.mutation_heuristics != null)
                                parameters.typeLLH = newType;
                            break;
                        case 2:
                            if(EvoCOPHyFlexManager.ruin_recreate_heuristics != null)
                                parameters.typeLLH = newType;
                            break;
                    }
                }while(parameters.typeLLH == -1);
                //--- Nastav name a DOS a IOM
                switch(parameters.typeLLH){
                    //--- local search
                    case 0:
                        parameters.name = EvoCOPHyFlexManager.local_search_heuristics[generator.nextInt(EvoCOPHyFlexManager.local_search_heuristics.length)];
                        parameters.depthOfSearch = EvoCOPHyFlexManager.startMinLocalLLH + EvoCOPHyFlexManager.addValueLocalLH * generator.nextDouble();  // zvoli dos z intervalu (0.1, 0.5)
                        break;
                    //--- mutation
                    case 1:
                        parameters.name = EvoCOPHyFlexManager.mutation_heuristics[generator.nextInt(EvoCOPHyFlexManager.mutation_heuristics.length)];
                        parameters.intensityOfMutation = EvoCOPHyFlexManager.startMinMutationLLH + EvoCOPHyFlexManager.addValueMutationLH * generator.nextDouble();  // zvoli iom z intervalu (0.1, 0.5)
                        break;
                    //--- ruin
                    case 2:
                        parameters.name = EvoCOPHyFlexManager.ruin_recreate_heuristics[generator.nextInt(EvoCOPHyFlexManager.ruin_recreate_heuristics.length)];
                        parameters.intensityOfMutation = EvoCOPHyFlexManager.startMinRuinLLH + EvoCOPHyFlexManager.addValueRuinLH * generator.nextDouble();  // zvoli iom z intervalu (0.1, 0.5)
                        break;
                }
            }
            //--- Zmeni pouze parametry DOS a IOM
            else{
                parameters.typeLLH = -1;
                if(generator.nextBoolean() == true){
                    parameters.depthOfSearch = 0.01 + EvoCOPHyFlexManager.changeValueAction * generator.nextDouble();
                    parameters.intensityOfMutation = 0.01 + EvoCOPHyFlexManager.changeValueAction * generator.nextDouble();
                }
                else{
                    parameters.depthOfSearch = -(0.01 + EvoCOPHyFlexManager.changeValueAction * generator.nextDouble());
                    parameters.intensityOfMutation = -(0.01 + EvoCOPHyFlexManager.changeValueAction * generator.nextDouble());
                }
            }
        }
    }

    /**
     * modifies the parameters
     */
    public void modifyParameters(){

        //--- Zmeni name a typ akce, bud
        if(generator.nextInt(100) < 25){
            //--- zvol novy typ LLH
            parameters.typeLLH = -1;
            do{
                int newType = generator.nextInt(3);
                switch(newType){
                    case 0:
                        if(EvoCOPHyFlexManager.local_search_heuristics != null)
                            parameters.typeLLH = newType;
                        break;
                    case 1:
                        if(EvoCOPHyFlexManager.mutation_heuristics != null)
                            parameters.typeLLH = newType;
                        break;
                    case 2:
                        if(EvoCOPHyFlexManager.ruin_recreate_heuristics != null)
                            parameters.typeLLH = newType;
                        break;
                }
            }while(parameters.typeLLH == -1);
            //---
            switch(parameters.typeLLH){
                //--- local search
                case 0:
                    parameters.name = EvoCOPHyFlexManager.local_search_heuristics[generator.nextInt(EvoCOPHyFlexManager.local_search_heuristics.length)];
                    parameters.depthOfSearch = EvoCOPHyFlexManager.startMinLocalLLH + EvoCOPHyFlexManager.addValueLocalLH * generator.nextDouble();  // zvoli dos z intervalu (0.1, 0.5)
                    break;
                //--- mutation
                case 1:
                    parameters.name = EvoCOPHyFlexManager.mutation_heuristics[generator.nextInt(EvoCOPHyFlexManager.mutation_heuristics.length)];
                    parameters.intensityOfMutation = EvoCOPHyFlexManager.startMinMutationLLH + EvoCOPHyFlexManager.addValueMutationLH * generator.nextDouble();  // zvoli iom z intervalu (0.1, 0.5)
                    break;
                //--- ruin-recreate
                case 2:
                    parameters.name = EvoCOPHyFlexManager.ruin_recreate_heuristics[generator.nextInt(EvoCOPHyFlexManager.ruin_recreate_heuristics.length)];
                    parameters.intensityOfMutation = EvoCOPHyFlexManager.startMinRuinLLH + EvoCOPHyFlexManager.addValueRuinLH * generator.nextDouble();  // zvoli iom z intervalu (0.1, 0.5)
                    break;
            }
        }
        //--- Cislo akce zustava, meni se pouze parametry
        else{
            //--- zmena parametru IOM a DOS
            double oKolik;
            if(generator.nextBoolean() == true)
                oKolik = 0.01 + EvoCOPHyFlexManager.changeValueAction*generator.nextDouble();
            else
                oKolik = -(0.01 + EvoCOPHyFlexManager.changeValueAction*generator.nextDouble());
            switch(parameters.typeLLH){
                //--- local search
                case 0:
                    parameters.depthOfSearch += oKolik;
                    break;
                //--- mutation
                case 1:
                    parameters.intensityOfMutation += oKolik;
                    break;
                //--- ruin-recreate
                case 2:
                    parameters.intensityOfMutation += oKolik;
                    break;
            }
            //--- zmena id1 a id2
            if(generator.nextInt() < 25){
                parameters.id1 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH);  // vybere prvni z existujicich LLH aktualniho prototypu
                while(parameters.id1 == parameters.id2)
                {
                    parameters.id2 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH);  // vybere druhou z existujicich LLH aktualniho prototypu
                }
            }
            //--- zmena id1 a id2
            if(generator.nextInt() < 25){
                parameters.id2 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH);  // vybere prvni z existujicich LLH aktualniho prototypu
                while(parameters.id1 == parameters.id2)
                {
                    parameters.id1 = generator.nextInt(EvoCOPHyFlexManager.nbActiveLLH);  // vybere druhou z existujicich LLH aktualniho prototypu
                }
            }
        }
    }

    /**
     * mutate the action
     * either change action type
     * or change parameters
     */
    /**
     * Bud se meni nop na active a naopak, anebo se trochu zmeni parametry akce (rozhodne se nebudou kompletne inicializovat)
     */
    public final void mutate() {
        if (generator.nextInt() % 5 == 0) {     //--- 20% mutaci bude menit active <--> nop
            //reset from nop to activeActionType
            if (this.actionType.equals("NOP")) {
                this.actionType = activeActionType;
            } else {
                this.actionType = "NOP";
            }
        } else {
            this.modifyParameters();
        }
    }

    /**
     * deep copy method
     * makes own copies from the solutions object references
     * makes own copies for the new solution object
     * cuts all references to objects of the copied object
     * can be empty if there are no problem specific objects to copy
     */
    public void deepCopy() {
        this.parameters = (EvoCOPActionParams)parameters.clone();   // KUBA: udelat EvoCOPActionParams klonovatelne
    }

    /**
     * shallow copy method
     * copies all data from this action to a new action
     * calls the deepCopy method of the new action object
     * @return exact copy of this action element
     */
    public EvoCOPAction clone() throws CloneNotSupportedException {
        EvoCOPAction copy = (EvoCOPAction) super.clone();
        if (this.actionBase != null) {
            copy.actionBase = this.actionBase.clone();
        }
        if (this.actionType != null) {
            copy.actionType = new String(this.actionType);
        }
        if (this.activeActionType != null) {
            copy.activeActionType = new String(this.activeActionType);
        }
        copy.parameters = (EvoCOPActionParams)this.parameters.clone();   // KUBA: udelat EvoCOPActionParams klonovatelne
//        copy.deepCopy();
        return copy;
    }

    /**
     * @return the type of this action
     */
    public final String getActionType() {
        return actionType;
    }

    public EvoCOPActionParams getParameters() {
        return parameters;
    }

}
