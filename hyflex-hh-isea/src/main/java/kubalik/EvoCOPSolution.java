/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package kubalik;

import java.util.*;
import java.util.Random;


/**
 *
 * @author
 */
public class EvoCOPSolution {

    //--- KUBA: reseni je HH, posloupnost LLHs
    public ArrayList<EvoCOPLowLevelH> solutionHH;    // KUBA: seznam LLHs tvoricich HH
    public int nbActiveLLH;                    // KUBA: pocet aktivnich LLHs
    public double solutionQuality;             //--- kvalita reseni dosazeneho pomoci HH
    public double fitness;                     //--- kvalita HH; rozdil (solutionQuality - kvalitaOhodnocovanehoReseni)
    public long time;                          //--- delka vypoctu HH
    private EvoCOPRandomGenerator rand = EvoCOPRandomGenerator.getInstance();
    protected Random generator = rand.getGenerator();

    public EvoCOPSolution() {
        super();
    }

    //--- Inicializace LLH sekvence
    //--- Pevne dana struktura LLH sekvence
    public void initSolutionHH(EvoCOPSolution candidatePrototype) {

        solutionHH = new ArrayList<EvoCOPLowLevelH>(EvoCOPHyFlexManager.nbActiveLLH);
        this.nbActiveLLH = EvoCOPHyFlexManager.nbActiveLLH;

        //--- I. prvni akce je local search
        EvoCOPLowLevelH newLLH1 = new EvoCOPLowLevelH();
        switch(generator.nextInt(1)){
            case 0:
                newLLH1.initLLH(0, 0);   //--- local search
                break;
            case 1:
                newLLH1.initLLH(0, 1);   //--- mutace
                break;
            case 2:
                newLLH1.initLLH(0, 2);   //--- ruin-recreate
                break;
        }
        solutionHH.add(newLLH1);

        //--- II. v prostredni casti muze byt cokoliv
        for(int i=1; i<this.nbActiveLLH-1; i++){
            EvoCOPLowLevelH newLLH = new EvoCOPLowLevelH();
//            switch(1 + generator.nextInt(2)){
            switch(generator.nextInt(3)){
                case 0:
                    newLLH.initLLH(i, 0);   //--- local search
                    break;
                case 1:
                    newLLH.initLLH(i, 1);   //--- mutace
                    break;
                case 2:
                    newLLH.initLLH(i, 2);   //--- ruin-recreate
                    break;
            }
            solutionHH.add(newLLH);
        }

        //--- III. posledni akce je urcite lokalni opt.
        EvoCOPLowLevelH newLLH = new EvoCOPLowLevelH();
        newLLH.initLLH(nbActiveLLH-1, 0);
        solutionHH.add(newLLH);
        //--- spocitej fitness tohoto reseni - teto HH
        EvoCOPQuality q = this.countFitness(candidatePrototype);
        this.fitness = q.fitness;
        this.solutionQuality = q.solutionQuality;
        this.time = q.time;
    }

    //--- Inicializace LLH sekvence
    //--- Podle pravdepodobnosti jednotlivych typu LLH
//    public void initSolutionHH(EvoCOPSolution candidatePrototype) {
//        double probLLH[] = {0.50, 0.25, 0.25};
//        boolean localLLH = false;
//        double r;
//
//        solutionHH = new ArrayList<EvoCOPLowLevelH>(EvoCOPHyFlexManager.nbActiveLLH);
//        this.nbActiveLLH = EvoCOPHyFlexManager.nbActiveLLH;
//
//        //--- II. v prostredni casti muze byt cokoliv
//        for(int i=0; i<this.nbActiveLLH; i++){
//            EvoCOPLowLevelH newLLH = new EvoCOPLowLevelH();
//            r = generator.nextDouble();
//            if(r < probLLH[0]){
//                newLLH.initLLH(i, 0);   //--- local search
//                localLLH = true;
//            }
//            else if(r < probLLH[1])
//                newLLH.initLLH(i, 1);   //--- mutace
//            else
//                newLLH.initLLH(i, 2);   //--- ruin-recreate
//            solutionHH.add(newLLH);
//        }
//
//        //--- Zajistit, ze v sekvenci bude alespon jedna local search LLH
//        if(localLLH == false){
//            int replaced = generator.nextInt(this.nbActiveLLH);
//            EvoCOPLowLevelH newLLH = new EvoCOPLowLevelH();
//            newLLH.initLLH(replaced, 0);   //--- local search
//            solutionHH.set(replaced, newLLH);
//        }
//
//        //--- spocitej fitness tohoto reseni - teto HH
//        EvoCOPQuality q = this.countFitness(candidatePrototype);
//        this.fitness = q.fitness;
//        this.solutionQuality = q.solutionQuality;
//        this.time = q.time;
//    }

    public ArrayList<EvoCOPLowLevelH> deepCopySolutionHH() {

        ArrayList<EvoCOPLowLevelH> temp = new ArrayList<EvoCOPLowLevelH>();

        for(int i=0; i<this.solutionHH.size(); i++){
            EvoCOPLowLevelH llh = (EvoCOPLowLevelH)this.solutionHH.get(i).clone();
            temp.add(llh);
        }

        return temp;
    }

    //--- Naklonuje solutionHH pouze s aktivnimi LLH
    public ArrayList<EvoCOPLowLevelH> deepCopySolutionHHActive() {

        ArrayList<EvoCOPLowLevelH> temp = new ArrayList<EvoCOPLowLevelH>();
        int j=0;

        for(int i=0; i<this.solutionHH.size(); i++){
            if(this.solutionHH.get(i).getStatus() == 1){
                EvoCOPLowLevelH llh = (EvoCOPLowLevelH)this.solutionHH.get(i).clone();
                llh.setId(j);
                j++;
                temp.add(llh);
            }
        }

        return temp;
    }

    public EvoCOPSolution applyActionSequence(EvoCOPActionSequence AS) {

        //--- KUBA: vytvor nove reseni 's' a pamet pro jeho 'solutionHH'
        EvoCOPSolution s = new EvoCOPSolution();
        s.nbActiveLLH = this.nbActiveLLH;           //--- zkopiruj do noveho reseni pocet aktivnich akci
        Iterator<EvoCOPAction> iterAction = AS.iterator();
        ArrayList<EvoCOPLowLevelH> temp = deepCopySolutionHH();  //--- KUBA: naklonovane stavajici reseni, ktere se bude menit

        //--- KUBA: aplikuj AS na solutionHH
        while (iterAction.hasNext()) {
            EvoCOPAction a = (EvoCOPAction) iterAction.next();
            String aType = a.getActionType();
            EvoCOPActionParams params = a.getParameters();
            //--- KUBA: prida LLH na danou pozici
            if(aType.equals("addLLH")) {
                //---
                EvoCOPLowLevelH newLLH = new EvoCOPLowLevelH();
                newLLH.setName(params.name);
                newLLH.setId(params.id1);
                newLLH.setDOS(params.depthOfSearch);
                newLLH.setIOM(params.intensityOfMutation);
                newLLH.setTypeLLH(params.typeLLH);
                newLLH.setStatus(1);                // nastav status=1, LLH je aktivni
                //
                temp.add(params.id2, newLLH);   // vlozi novou LLH na pozici id2
                s.nbActiveLLH++;
            }
            else if(aType.equals("removeLLH")){
                // najdi LLH s danym id1
                int pos = -1;
                for(int i=0; i<temp.size(); i++)
                    if(temp.get(i).getId() == params.id1){
                        pos = i;
                        break;
                    }
                if(pos == -1 )
                    System.out.println("\nError (removeLLH): solutionHH neobsahuje LLH s id=" + params.id1);
                // nastav status==0
                EvoCOPLowLevelH newLLH = temp.get(pos);   // zkopiruji akci na dane pozici
                if(newLLH.getStatus() == 1)
                    s.nbActiveLLH--;
                newLLH.setStatus(0);                // nastav status=0, LLH je neaktivni
                //
                temp.set(pos, newLLH);              // vrat LLH na jeji pozici id1
            }
            else if(aType.equals("moveLLH")){
                // najdi LLH s danym id1
                int pos = -1;
                for(int i=0; i<temp.size(); i++)
                    if(temp.get(i).getId() == params.id1){
                        pos = i;
                        break;
                    }
                if(pos == -1 )
                    System.out.println("\nError (moveLLH): solutionHH neobsahuje LLH s id=" + params.id1);
                //--- posun danou LLH o kolik to jde
                EvoCOPLowLevelH newLLH = temp.remove(pos);        // odeber LLH
                if(((pos + params.id2) >= 0) && ((pos + params.id2) < temp.size()))
                    temp.add(pos + params.id2, newLLH);                  // dej LLH na novou pozici
                else if((pos + params.id2) < 0)
                    temp.add(0, newLLH);                    // dej LLH na zacatek temp
                else if((pos + params.id2) >= temp.size())
                    temp.add(newLLH);                       // dej LLH na konec temp
            }
            else if(aType.equals("swapLLH")){
                //--- najdi LLH s danym id1
                int pos1 = -1;
                int pos2 = -1;
                for(int i=0; i<temp.size(); i++)
                    if(temp.get(i).getId() == params.id1){
                        pos1 = i;
                    }
                    else if(temp.get(i).getId() == params.id2)
                    {
                        pos2 = i;
                    }
                if(pos1 == -1){
                    System.out.println("\nError (swapLLH): solutionHH neobsahuje LLH s id=" + params.id1);
                }
                if(pos2 == -1){
                    System.out.println("\nError (swapLLH): solutionHH neobsahuje LLH s id=" + params.id2);
                }
                //---
                EvoCOPLowLevelH newLLH1 = temp.get(pos1); // zkopiruji prvni LLH
                EvoCOPLowLevelH newLLH2 = temp.get(pos2); // zkopiruji druhou LLH
                temp.set(pos1, newLLH2);            // prohod LLH1 a LLH2
                temp.set(pos2, newLLH1);
            }
            else if(aType.equals("changeLLH")){
                //--- najdi LLH s danym id1
                int pos = -1;
                for(int i=0; i<temp.size(); i++)
                    if(temp.get(i).getId() == params.id1){
                        pos = i;
                        break;
                    }
                if(pos == -1 )
                    System.out.println("\nError (changeLLH): solutionHH neobsahuje LLH s id=" + params.id1);
                //--- odeber LLH
                EvoCOPLowLevelH newLLH = temp.get(pos);
                //--- Kontrola rozsahu
                if(params.typeLLH == -1){   //--- meni se pouze DOS a IOM
                    double newDOS = newLLH.getDOS() + params.depthOfSearch;
                    double newIOM = newLLH.getIOM() + params.intensityOfMutation;
                    newLLH.setDOS(newDOS);
                    newLLH.setIOM(newIOM);
                }
                else{   //--- meni se cela akce
                    newLLH.setTypeLLH(params.typeLLH);
                    newLLH.setName(params.name);
                    newLLH.setDOS(params.depthOfSearch);
                    newLLH.setIOM(params.intensityOfMutation);
                }
                temp.set(pos, newLLH);
            }
        }

        s.solutionHH = temp;
        return s;
    }

    // KUBA: Pozor, porovnavam HH, ne vysledne reseni generovane danou HH
    public boolean equalsSolution(EvoCOPSolution ks) {
        int act1=0;    // index aktualni aktivni LLH v this.solutionHH
        int act2=0;    // index aktualni aktivni LLH v s.solutionHH

        if(this.nbActiveLLH != ks.nbActiveLLH)
            return false;   // jsou ruzne

        int k = this.nbActiveLLH;
        while(k > 0){
            while((act1 < this.solutionHH.size()) && (this.solutionHH.get(act1).getStatus() == 0))
                act1++;
            while((act2 < ks.solutionHH.size()) && (ks.solutionHH.get(act2).getStatus() == 0))
                act2++;
            if((act1 == this.solutionHH.size()) || (act2 == ks.solutionHH.size())){
                System.out.println("Error (HyFlexSolution::equalsSolution()): nesedi pocet aktivnich LLH v solutionHH");
                return false;
            }
            if(this.solutionHH.get(act1).getTypeLLH() != ks.solutionHH.get(act2).getTypeLLH())
                return false;   //--- jsou ruzneho typu
            else if(this.solutionHH.get(act1).getName() != ks.solutionHH.get(act2).getName())
                return false;   // jsou ruzneho jmena
            else if(this.solutionHH.get(act1).getTypeLLH() == 0){  // local search
                if(Math.abs(this.solutionHH.get(act1).getDOS() - ks.solutionHH.get(act2).getDOS()) > 0.025)
                    return false;   // jsou ruzne
            }
            else if(this.solutionHH.get(act1).getTypeLLH() == 1){  // mutation
                if(Math.abs(this.solutionHH.get(act1).getIOM() - ks.solutionHH.get(act2).getIOM()) > 0.01)
                    return false;   // jsou ruzne
            }
            else if(this.solutionHH.get(act1).getTypeLLH() == 2){  // ruin-recreate
                if(Math.abs(this.solutionHH.get(act1).getIOM() - ks.solutionHH.get(act2).getIOM()) > 0.01)
                    return false;   // jsou ruzne
            }
            k--;
            act1++;
            act2++;
        }

        return true;    // proslo to az sem, tak jsou stejne
    }

    //--- Vraci true, kdyz reseni je v tabuListu, jinak vraci false
    public boolean checkTabuList(int solutionId){
        
        for(int i=EvoCOPHyFlexManager.operationalLength; i< EvoCOPHyFlexManager.operationalLength + EvoCOPHyFlexManager.tabuListLength; i++){
            if(EvoCOPHyFlexManager.problem.compareSolutions(solutionId, i) == true){
//                System.out.println("\n--- duplikat");
                return true;
            }
        }
        
        //--- kdyz to proslo az sem, tak reseni "solutionId" v tabulList neni
        return false;
    }

    //--- Vicekolovy vypocet fitness
    public EvoCOPQuality countFitness(EvoCOPSolution candidatePrototype) {
        double f;
        double q, origQuality;
        long t = -1, startTime = 0, endTime = 0;
        double value, currQuality;
        boolean zlepseni;
        boolean unikatni = true;

        // zkopiruj solutionMemory[0] do solutionMemory[1]
        EvoCOPHyFlexManager.problem.copySolution(0, 1);
        currQuality = EvoCOPHyFlexManager.current_obj_function_values[0];
        origQuality = EvoCOPHyFlexManager.current_obj_function_values[0]; //--- puvodni kvalita ohodnocovaciho reseni
        f = Double.MAX_VALUE;
        q = Double.MAX_VALUE;

        // aplikuj this.solutionHH na solutionMemory[1] a vysledek uloz do solutionMemory[1]
        startTime = System.currentTimeMillis();
        do{
            int j = 0;  //--- citac provedenych aktivnich akci
            for(int i=0; i<this.solutionHH.size(); i++){    //--- aplikuj vsechny aktivni LLHs v solutionHH
                //apply the chosen heuristic to the solution at index 0 in the memory
                //the new solution is then stored at index 1 of the solution memory
                if((j < EvoCOPHyFlexManager.maxActiveLLH) && (this.solutionHH.get(i).getStatus() == 1)){    // je to aktivni LLH
                    //--- kontrola rozsahu a nastaveni DOS a IOM
                    switch(this.solutionHH.get(i).getTypeLLH()){
                        case 0: //--- local search
                            if(this.solutionHH.get(i).getDOS() > EvoCOPHyFlexManager.maxLocalLLH){
                                EvoCOPHyFlexManager.problem.setDepthOfSearch(EvoCOPHyFlexManager.maxLocalLLH);    // nastavi DOS
                            }
                            else if(this.solutionHH.get(i).getDOS() < EvoCOPHyFlexManager.minLocalLLH){
                                EvoCOPHyFlexManager.problem.setDepthOfSearch(EvoCOPHyFlexManager.minLocalLLH);    // nastavi DOS
                            }
                            else
                                EvoCOPHyFlexManager.problem.setDepthOfSearch(this.solutionHH.get(i).getDOS());    // nastavi DOS
                            break;
                        case 1: //--- mutation a
                        case 2: //--- ruin-recreate
                            //--- TODO: zkontrolovat, ze staci jen IOS
                            if(this.solutionHH.get(i).getIOM() > EvoCOPHyFlexManager.maxLocalLLH){
                                EvoCOPHyFlexManager.problem.setIntensityOfMutation(EvoCOPHyFlexManager.maxLocalLLH);    // nastavi DOS
                            }
                            else if(this.solutionHH.get(i).getIOM() < EvoCOPHyFlexManager.minLocalLLH){
                                EvoCOPHyFlexManager.problem.setIntensityOfMutation(EvoCOPHyFlexManager.minLocalLLH);    // nastavi DOS
                            }
                            else
                                EvoCOPHyFlexManager.problem.setIntensityOfMutation(this.solutionHH.get(i).getIOM());    // nastavi DOS
                            break;
                    }
                    //--- pokud je nalezena kvalitnejsi value a jeji reseni neni stejne jako ohodnocovaci reseni
                    value = EvoCOPHyFlexManager.problem.applyHeuristic(this.solutionHH.get(i).getName(), 1, 1);
                    if((q >= value) &&
                       (EvoCOPHyFlexManager.problem.compareSolutions(1, 0) == false) &&
                       (unikatni = (checkTabuList(1) == false)))
                        q = value;  //--- aktualizuj vyslednou q, ta je rovna nejlepsi fitness pozorovane behem aplikace solutionHH
                    //--- pokud je solutionMemory[1] lepsi nez solutionMemory[2], tak solutionMemory[2] prepis
                    if((value < EvoCOPHyFlexManager.current_obj_function_values[2]) && (unikatni))
                    //--- TODO: zkontrolovat cely tabuList
                    {
                            System.out.println(" " + value);
                            //--- uprav nejlepsi zname reseni
                            EvoCOPHyFlexManager.problem.copySolution(1, 2);
                            EvoCOPHyFlexManager.current_obj_function_values[2] = value;
                            //--- ohodnocovaci reseni
                            EvoCOPHyFlexManager.problem.copySolution(1, 0);
                            EvoCOPHyFlexManager.current_obj_function_values[0] = value;
                            //--- aktualizuj candidatePrototype
                            candidatePrototype.solutionHH = this.deepCopySolutionHHActive();
                            candidatePrototype.nbActiveLLH = candidatePrototype.solutionHH.size();
                            //--- aktualizuj tabuList
                            EvoCOPHyFlexManager.problem.copySolution(1, EvoCOPHyFlexManager.operationalLength + EvoCOPHyFlexManager.currTabuId);
                            EvoCOPHyFlexManager.current_obj_function_values[EvoCOPHyFlexManager.currTabuId] = value;
                            EvoCOPHyFlexManager.currTabuId = (EvoCOPHyFlexManager.currTabuId + 1) % EvoCOPHyFlexManager.tabuListLength;
                    }
                    else if((value == EvoCOPHyFlexManager.current_obj_function_values[2]) &&
                            (EvoCOPHyFlexManager.problem.compareSolutions(1, 0) == false) &&
                            unikatni)
                            //--- BACKUP: EvoCOPHyFlexManager.problem.compareSolutions(1, 0) == false
                    {
    //                        System.out.println(" " + value);
                            //--- uprav nejlepsi zname reseni
                            EvoCOPHyFlexManager.problem.copySolution(1, 2);
                            EvoCOPHyFlexManager.current_obj_function_values[2] = value;
                            //--- ohodnocovaci reseni
                            EvoCOPHyFlexManager.problem.copySolution(1, 0);
                            EvoCOPHyFlexManager.current_obj_function_values[0] = value;
                            //--- aktualizuj tabuList
                            EvoCOPHyFlexManager.problem.copySolution(1, EvoCOPHyFlexManager.operationalLength + EvoCOPHyFlexManager.currTabuId);
                            EvoCOPHyFlexManager.current_obj_function_values[EvoCOPHyFlexManager.currTabuId] = value;
                            EvoCOPHyFlexManager.currTabuId = (EvoCOPHyFlexManager.currTabuId + 1) % EvoCOPHyFlexManager.tabuListLength;
                    }
                    //---
                    j++;
                }
            }
            //---
            if(t == -1)
                endTime = System.currentTimeMillis();
            t = endTime - startTime;    //--- doba vypoctu HH
            //---
            if(q < currQuality)
                zlepseni = true;
            else
                zlepseni = false;
            currQuality = q;
        }while(zlepseni);

        //---
        f = q - origQuality;    //--- fitness je zlepseni dosazene na ohodnocovacim reseni
        //--- kdyz nedoslo ke zlepseni
        if(q >= origQuality){
            EvoCOPHyFlexManager.nonImprovementCount++;
            //---
            if(q > origQuality){
//                System.out.print("-");
            }
            if(q == origQuality){
//                System.out.print("=");
            }
            //--- pokud od posledniho restartu neuplynulo timeLimit/minRestartRate, napr. 30s, tak nic nedelej
            if(((System.currentTimeMillis() - EvoCOPHyFlexManager.lastRestartTime) > (EvoCOPHyFlexManager.timeLimit / 10)) || //--- ubehlo vic nez minuta
               ((generator.nextDouble() < EvoCOPHyFlexManager.Temperature) &&
               ((System.currentTimeMillis() - EvoCOPHyFlexManager.lastRestartTime) > (EvoCOPHyFlexManager.timeLimit / EvoCOPHyFlexManager.minRestartRate)))){
                //--- uprav nejlepsi zname reseni
                perturbBaseSolution(0);     //--- meni se best-so-far
//                System.out.println("       ***: " + EvoCOPHyFlexManager.current_obj_function_values[0] + "(" + (System.currentTimeMillis()-EvoCOPHyFlexManager.startTime)/1000 + "s)");
            }
        }
        EvoCOPQuality sq = new EvoCOPQuality(f, q, t);
        return sq;
    }

    //--- type == 0, meni se best-so-far
    //--- type == 1, meni se aktualni ohodnocovaci
    //--- type == 2, nahodne vybere bud best-so-far nebo aktualni ohodnocovaci
    public void perturbBaseSolution(int type){

        //--- 0. nejdriv uprav celkove best-so-far reseni solutionMemory[3]
        if(EvoCOPHyFlexManager.current_obj_function_values[3] > EvoCOPHyFlexManager.current_obj_function_values[2]){
            EvoCOPHyFlexManager.problem.copySolution(2, 3);
            EvoCOPHyFlexManager.current_obj_function_values[3] = EvoCOPHyFlexManager.current_obj_function_values[2];
        }

        //--- vyber reseni solution[3], ktere se ma perturbovat
        EvoCOPHyFlexManager.problem.copySolution(3, 0);
        EvoCOPHyFlexManager.current_obj_function_values[0] = EvoCOPHyFlexManager.current_obj_function_values[3];

        do{
            //--- 1a. zmen ohodnocovaci reseni solutionMemory[0]
            if(generator.nextInt(EvoCOPHyFlexManager.ruinRate + EvoCOPHyFlexManager.mutationRate) < EvoCOPHyFlexManager.ruinRate){
                //--- nejdrvi se zkusi ruin-recreate
                if(EvoCOPHyFlexManager.ruin_recreate_heuristics != null){
                    do{
                        int name = EvoCOPHyFlexManager.ruin_recreate_heuristics[generator.nextInt(EvoCOPHyFlexManager.ruin_recreate_heuristics.length)];
                        EvoCOPHyFlexManager.problem.setIntensityOfMutation(EvoCOPHyFlexManager.startRecreate + EvoCOPHyFlexManager.addRecreate*generator.nextDouble() );
                        EvoCOPHyFlexManager.current_obj_function_values[0] = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 1);
                    }while(EvoCOPHyFlexManager.problem.compareSolutions(0, 1) == true);
                    EvoCOPHyFlexManager.problem.copySolution(1, 0);
                }
                //--- potom se zkusi mutace
                else if(EvoCOPHyFlexManager.mutation_heuristics != null)
                {
                    do{
                        //--- proved mutaci
                        int name = EvoCOPHyFlexManager.mutation_heuristics[generator.nextInt(EvoCOPHyFlexManager.mutation_heuristics.length)];
                        EvoCOPHyFlexManager.problem.setIntensityOfMutation(EvoCOPHyFlexManager.startMutation + EvoCOPHyFlexManager.addMutation*generator.nextDouble());
                        EvoCOPHyFlexManager.current_obj_function_values[0] = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 1);
                    }while(EvoCOPHyFlexManager.problem.compareSolutions(0, 1) == true);
                    EvoCOPHyFlexManager.problem.copySolution(1, 0);   //--- nastav zmenene ohodnocovaci reseni
                }
            }
            //--- 1b. zmen ohodnocovaci reseni solutionMemory[0]
            else{
                //--- nejdrvi se zkusi mutace
                if(EvoCOPHyFlexManager.mutation_heuristics != null)
                {
                    do{
                        int name = EvoCOPHyFlexManager.mutation_heuristics[generator.nextInt(EvoCOPHyFlexManager.mutation_heuristics.length)];
                        EvoCOPHyFlexManager.problem.setIntensityOfMutation(EvoCOPHyFlexManager.startMutation + EvoCOPHyFlexManager.addMutation*generator.nextDouble());
                        EvoCOPHyFlexManager.current_obj_function_values[0] = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 1);
                    }while(EvoCOPHyFlexManager.problem.compareSolutions(0, 1) == true);
                    EvoCOPHyFlexManager.problem.copySolution(1, 0);   //--- nastav zmenene ohodnocovaci reseni
                }
                //--- potom se zkusi ruin-recreate
                else if(EvoCOPHyFlexManager.ruin_recreate_heuristics != null)
                {
                    do{
                        int name = EvoCOPHyFlexManager.ruin_recreate_heuristics[generator.nextInt(EvoCOPHyFlexManager.ruin_recreate_heuristics.length)];
                        EvoCOPHyFlexManager.problem.setIntensityOfMutation(EvoCOPHyFlexManager.startRecreate + EvoCOPHyFlexManager.addRecreate*generator.nextDouble() );
                        EvoCOPHyFlexManager.current_obj_function_values[0] = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 1);
                    }while(EvoCOPHyFlexManager.problem.compareSolutions(0, 1) == true);
                    EvoCOPHyFlexManager.problem.copySolution(1, 0);   //--- nastav zmenene ohodnocovaci reseni
                }

//                //--- 2. Lokalni doladeni noveho ohodnocovaci reseni solutionMemory[0]
//                for(int it=0; it<2; it++){
//                    int name = EvoCOPHyFlexManager.local_search_heuristics[generator.nextInt(EvoCOPHyFlexManager.local_search_heuristics.length)];
//                    EvoCOPHyFlexManager.problem.setDepthOfSearch(EvoCOPHyFlexManager.startMinLocalLLH + EvoCOPHyFlexManager.addValueLocalLH*generator.nextDouble());
//                    EvoCOPHyFlexManager.current_obj_function_values[0] = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 0);
//                }
            }
        }while(EvoCOPHyFlexManager.problem.compareSolutions(0, 3) == true); //--- Nove ohodnocovaci reseni
                                                                             //--- je stejne jako best-so-far
        //--- 2. zmen doposud nejlepsi reseni solutionMemory[2]
        EvoCOPHyFlexManager.problem.copySolution(0, 2);
        EvoCOPHyFlexManager.current_obj_function_values[2] = EvoCOPHyFlexManager.current_obj_function_values[0];

        //--- 3. uprav cas posledniho restartovani
        EvoCOPHyFlexManager.lastRestartTime = System.currentTimeMillis();
//        System.out.println("--> " + EvoCOPHyFlexManager.current_obj_function_values[0]);
    }

    public void printSolutionVector() {
        for (int i = 0; i < this.solutionHH.size(); i++) {
            System.out.print("[" + this.solutionHH.get(i).getTypeLLH() + "," + this.solutionHH.get(i).getName() + "," + this.solutionHH.get(i).getDOS() + "," + this.solutionHH.get(i).getIOM() + "] ");
        }
    }

    public String defineSaveSolution(String name, EvoCOPSolution s, double value, long seed) {
        return s.getSolutionVector() + " " + s.solutionQuality + " " + seed;
    }

    public String getSolutionVector() {
        String s = "";
        for (int i = 0; i < this.solutionHH.size(); i++) {
            s = s + "[" + this.solutionHH.get(i).getTypeLLH();
            s = s + "," + this.solutionHH.get(i).getName();
            s = s + "," + this.solutionHH.get(i).getDOS();
            s = s + "," + this.solutionHH.get(i).getIOM() + "] ";
        }
        return s;
    }
}
