package kubalik;

import java.util.Random;
import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
//---

/**
 * POEMS hyper heuristic
 * 
 */

public class EvoCOPHyperHeuristic extends HyperHeuristic {
    EvoCOPASPopulation populationAS;
    EvoCOPHyFlexManager managerHF;
    EvoCOPSolution prototypeHH;    //--- prototype HH, ktera se bude upravovat
    EvoCOPSolution candidateHH;    //--- kandidat na novy prototypeHH
    //---
    int drawerSize;
    int maxGenes;
    double pC;
    double pM;
    double pBitFlip;
    int nTournament;
    int cycleEvals;   //--- pocet ohodnoceni v jedne iteraci
                      //--- pocet iteraci je omezen casem
    //---
    public static boolean restarted;
    public static int[] restartTime = {45000, 60000, 60000};
    public static int[] idleTime = {10000, 10000, 10000};
    //---
    private EvoCOPRandomGenerator rand = EvoCOPRandomGenerator.getInstance();
    protected Random generator = rand.getGenerator();

    /**
     * creates a new ExampleHyperHeuristic object with a random seed
     */
    public EvoCOPHyperHeuristic(long seed) {
        super(seed);
    }

    /**
     * This method defines the strategy of the hyper-heuristic
     * @param problem the problem domain to be solved
     */
    public void solve(ProblemDomain problem) {
        int cycle=1;
        int adjustCounter = 1;
        int confType=0;
        int g = 0;  //--- citac generaci

        // Tady spust POEMS na dany problem a konkretni instanci
        try {
            //--- inicializace HyFlexManageru a populationAS
            EvoCOPHyFlexManager.nonImprovementCount = 1;
            EvoCOPHyFlexManager.adjustProbRate = 60;           //--- aktualizace probehne kazdych 10 sekund
            EvoCOPHyFlexManager.minRestartRate = 60;          //--- timeLimit / 120 = 10s
            EvoCOPHyFlexManager.timeLimit = this.getTimeLimit();
            EvoCOPHyFlexManager.startTime = System.currentTimeMillis();
            EvoCOPHyFlexManager.lastRestartTime = System.currentTimeMillis();
            //---
            confType = initAll(problem);
            adjustProbAcceptWorse();          //--- upravi p-st prijeti zhorsujiciho reseni
            g = populationAS.popSize;
            EvoCOPHyFlexManager.lastRestartTime = System.currentTimeMillis(); //--- odted merim cas od posledniho restartu

            //--- hlavni cyklus POEMS
            while (!hasTimeExpired()) {
                //--- EA cyklus
                int e = populationAS.runEvolutionaryCycle(prototypeHH, candidateHH);
                g += e;    //--- g++; pricte skutecny pocet provedenych ohodnoceni v dane generaci
                hasTimeExpired();
                if((System.currentTimeMillis() - EvoCOPHyFlexManager.startTime) > (adjustCounter * EvoCOPHyFlexManager.timeLimit/EvoCOPHyFlexManager.adjustProbRate)){
                    adjustProbAcceptWorse();          //--- upravi p-st prijeti zhorsujiciho reseni
                    while((System.currentTimeMillis() - EvoCOPHyFlexManager.startTime) > (adjustCounter * EvoCOPHyFlexManager.timeLimit/EvoCOPHyFlexManager.adjustProbRate))
                        adjustCounter++;
                }

                //--- zmena prototypu a re-inicializace populationAS
                if(g > (cycle * cycleEvals)){
                    cycle++;    //--- inkrementuj cislo cyklu
                    //--- 1. aktualizuj prototypeHH
                    prototypeHH.nbActiveLLH =  candidateHH.nbActiveLLH;
                    prototypeHH.solutionHH = candidateHH.deepCopySolutionHH();
                    //--- dopln potrebny pocet aktivnich akci
                    while(prototypeHH.nbActiveLLH < EvoCOPHyFlexManager.nbActiveLLH){
                        EvoCOPLowLevelH newLLH = new EvoCOPLowLevelH();
                        newLLH.initLLH(prototypeHH.nbActiveLLH, 0);   //--- local search
                        prototypeHH.solutionHH.add(newLLH);
                        prototypeHH.nbActiveLLH++;
                    }
                    while(prototypeHH.nbActiveLLH > EvoCOPHyFlexManager.nbActiveLLH){
                        prototypeHH.solutionHH.remove(prototypeHH.nbActiveLLH-1);
                        prototypeHH.nbActiveLLH--;
                    }

                    //--- 2. zmen ohodnocovaci reseni solutionMemory[0]
                    EvoCOPHyFlexManager.problem.copySolution(2, 0);   //--- doposud nejlepsi nalezene reseni nastav jako reseni pro ohodnocovani
                    EvoCOPHyFlexManager.current_obj_function_values[0] = EvoCOPHyFlexManager.current_obj_function_values[2];

                    //--- 3. aplikuj nezmeneny prototyp na ohodnocovaci reseni
                    EvoCOPQuality q = prototypeHH.countFitness(candidateHH);
                    prototypeHH.solutionQuality = q.solutionQuality;

                    //--- 4. pregeneruj populationAS a ohodnot vsechny AS
                    populationAS.generateDrawers();
                    for(int i=populationAS.size()-1; i >= 0; i--){
                        populationAS.evaluateSingleAS(prototypeHH, candidateHH, i);
                        g++;
                        if(hasTimeExpired())
                            break;
                    }
                }
            }
//            System.out.println("Evaluations: " + g);
        } catch (Exception e) {
            System.out.println("testStarter: POEMS stopped because of a fatal error");
            e.printStackTrace();
        }
    }

    //------- Upravi p-st prijeti zhorsujiciho reseni
    //-------   - Pravdepodobnost by mela byt nastavena tak, aby pravdepodobnost
    //-------     prijeti nezlepsujiciho tahu za 1/12 casoveho limitu byla 1.
    //-------   - nonImprovementCount je pocet vsech doposud spocitanych nezlepsujicich reseni
    //-------   - EvoCOPHyFlexManager.timeLimit/EvoCOPHyFlexManager.adjustProbRate je cas mezi dvema
    //-------     aktualizacemi adjustProb

    public void adjustProbAcceptWorse(){
        double minNonImprovements = 200;
        double maxNonImprovements = 50000;
        double logMinNonImprovements = Math.log10(minNonImprovements);
        double logMaxNonImprovements = Math.log10(maxNonImprovements);
        double minTexp = 20.0;
        double maxTexp = 60.0;
        double k = (double)(maxTexp - minTexp)/(double)(logMinNonImprovements - logMaxNonImprovements);
        double b = maxTexp - k*(logMinNonImprovements);
        double expectedTotalNonImprovements;    //--- odhad celkoveho poctu nonimprovements
        double tExp;    //--- ocekavana doba mezi restarty
        double tMin;    //--- ocekavana minimalni doba mezi restarty
        double nonImprPerTexp;  //--- pocet nonImprovements za Texp
        double probFactor = 1.25;    //--- factor, jakym nasobim vyslednou p-st

        //--- varianta 1
        expectedTotalNonImprovements = (double)this.getTimeLimit() * ((double)EvoCOPHyFlexManager.nonImprovementCount / (double)this.getElapsedTime());
        if(expectedTotalNonImprovements < minNonImprovements)
            expectedTotalNonImprovements = minNonImprovements;
        if(expectedTotalNonImprovements > maxNonImprovements)
            expectedTotalNonImprovements = maxNonImprovements;
        //---
        tExp = 1000.0 * (k * Math.log10(expectedTotalNonImprovements) + b);
        tMin = tExp / 2.0;
        EvoCOPHyFlexManager.minRestartRate = (long)((double)(this.getTimeLimit()) / tMin);
        //---
        nonImprPerTexp = expectedTotalNonImprovements * tExp / (double)this.getTimeLimit();
        EvoCOPHyFlexManager.Temperature = probFactor / nonImprPerTexp;

        //---
//        System.out.println("\nexpectedTotalNonImprovements: " + (int)expectedTotalNonImprovements
//                + " (" + EvoCOPHyFlexManager.nonImprovementCount + "), Temperature = " + EvoCOPHyFlexManager.Temperature);
    }

    public int initAll(ProblemDomain problem){

        //--- inicializuj EvoCOPHyFlexManager
        managerHF = new EvoCOPHyFlexManager(problem);

        //--- test slozitosti resene ulohy
        int taskComplexity = testTimeComplexity();  //--- 0...jednoducha uloha; 1...stredne tezka uloha; 2...tezka uloha
        configurePOEMS(taskComplexity);

        //--- inicializace prototypeHH
        candidateHH = new  EvoCOPSolution();
        prototypeHH = new EvoCOPSolution();
        prototypeHH.initSolutionHH(candidateHH);
//        System.out.println("Initial prototype: " + prototypeHH.solutionQuality);
        hasTimeExpired();

        // pokud je solutionMemory[2] lepsi nez solutionMemory[0], tak solutionMemory[0] prepis
        if(prototypeHH.solutionQuality < EvoCOPHyFlexManager.current_obj_function_values[0]){
            EvoCOPHyFlexManager.problem.copySolution(2, 0);
            EvoCOPHyFlexManager.current_obj_function_values[0] = prototypeHH.solutionQuality;
        }

        //--- inicializace populationAS
        populationAS = new EvoCOPASPopulation();
        //--- nastaveni parametru POEMS: init(drawerSize, maxGenes, pC, pM, pBitflip, nTournament);
        populationAS.init(drawerSize, maxGenes, pC, pM, pBitFlip, nTournament);
        populationAS.generateDrawers();
        //--- ohodnoceni vsech jedincu v populaci
//        for(int i=0; i < populationAS.size(); i++){
        for(int i=populationAS.size()-1; i >= 0; i--){
            populationAS.evaluateSingleAS(prototypeHH, candidateHH, i);
            if(hasTimeExpired())
                break;
        }

        //---
        return taskComplexity;
    }

    public int testTimeComplexity(){
        int nLLHs = 0;  //--- pocet testovanych LLHs

        EvoCOPHyFlexManager.problem.setDepthOfSearch(0.5);        // nastavi DOS
        EvoCOPHyFlexManager.problem.setIntensityOfMutation(0.5);  // nastavi IOM
        long startTime = System.currentTimeMillis();
        if (EvoCOPHyFlexManager.local_search_heuristics != null){
            //--- 1. LLH
            int name = EvoCOPHyFlexManager.local_search_heuristics[generator.nextInt(EvoCOPHyFlexManager.local_search_heuristics.length)];
            double value = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 1);
            if(value < EvoCOPHyFlexManager.problem.getFunctionValue(0))
               EvoCOPHyFlexManager.problem.copySolution(1, 0);
            //--- 2. LLH
            name = EvoCOPHyFlexManager.local_search_heuristics[generator.nextInt(EvoCOPHyFlexManager.local_search_heuristics.length)];
            value = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 1);
            if(value < EvoCOPHyFlexManager.problem.getFunctionValue(0))
               EvoCOPHyFlexManager.problem.copySolution(1, 0);
            //--- 3. LLH
            name = EvoCOPHyFlexManager.local_search_heuristics[generator.nextInt(EvoCOPHyFlexManager.local_search_heuristics.length)];
            value = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 1);
            if(value < EvoCOPHyFlexManager.problem.getFunctionValue(0))
               EvoCOPHyFlexManager.problem.copySolution(1, 0);
        }
        else if(EvoCOPHyFlexManager.mutation_heuristics != null)
        {
            //--- 1. LLH
            int name = EvoCOPHyFlexManager.mutation_heuristics[generator.nextInt(EvoCOPHyFlexManager.mutation_heuristics.length)];
            double value = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 1);
            if(value < EvoCOPHyFlexManager.problem.getFunctionValue(0))
               EvoCOPHyFlexManager.problem.copySolution(1, 0);
            //--- 2. LLH
            name = EvoCOPHyFlexManager.mutation_heuristics[generator.nextInt(EvoCOPHyFlexManager.mutation_heuristics.length)];
            value = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 1);
            if(value < EvoCOPHyFlexManager.problem.getFunctionValue(0))
               EvoCOPHyFlexManager.problem.copySolution(1, 0);
            //--- 3. LLH
            name = EvoCOPHyFlexManager.mutation_heuristics[generator.nextInt(EvoCOPHyFlexManager.mutation_heuristics.length)];
            value = EvoCOPHyFlexManager.problem.applyHeuristic(name, 0, 1);
            if(value < EvoCOPHyFlexManager.problem.getFunctionValue(0))
               EvoCOPHyFlexManager.problem.copySolution(1, 0);
        }
        long endTime = System.currentTimeMillis();
//        System.out.print("\nComputation complexity estimate: " + (double)((endTime-startTime)/1000.0) + "s");
        //---
        if((endTime-startTime) < (this.getTimeLimit()/10000)){         //--- zvladne vice nez 10.000 jedincu
//            System.out.println(" (lehka)");
            return 0;   //--- lehka uloha
        }
        else if((endTime - startTime) < (this.getTimeLimit() / 1000)){ //--- zvladne vice nez 1.000 jedincu
//            System.out.println(" (stredne tezka)");
            return 1;   //--- lehka uloha
        }
        else{
//            System.out.println(" (tezka)");
            return 2;   //--- tezka uloha
        }
    }

    public void configurePOEMS(int type){

        drawerSize = 10;
        pC = 0.7;
        pM = 0.25;
        pBitFlip = 0.5;
        nTournament = 2;
        //--- JEDNODUCHA uloha
        if(type == 0){
            maxGenes = 5;
            cycleEvals = 200;   //--- pocet ohodnoceni v jedne iteraci
            //--- TODO: konfigurace manageru
            EvoCOPHyFlexManager.nbActiveLLH = 5;       //--- zacina s 5 LLH v prototypu HH
            EvoCOPHyFlexManager.maxActiveLLH = EvoCOPHyFlexManager.nbActiveLLH + maxGenes;     //--- maximalni velikost HH

            //--- Resampling parameters
            EvoCOPHyFlexManager.startRecreate = 0.15;  //--- 0.05
            EvoCOPHyFlexManager.addRecreate = 0.15;    //--- 0.15
            EvoCOPHyFlexManager.startMutation = 0.15;  //--- 0.01
            EvoCOPHyFlexManager.addMutation = 0.15;    //--- 0.04
            //---
            EvoCOPHyFlexManager.ruinRate = 5;
            EvoCOPHyFlexManager.mutationRate = 5;

            //--- Standard evolution parameters
            EvoCOPHyFlexManager.startMinLocalLLH = 0.40;    //--- 0.2
            EvoCOPHyFlexManager.addValueLocalLH = 0.50;
            //---
            EvoCOPHyFlexManager.startMinMutationLLH = 0.20;
            EvoCOPHyFlexManager.addValueMutationLH = 0.50;
            //---
            EvoCOPHyFlexManager.startMinRuinLLH = 0.20;
            EvoCOPHyFlexManager.addValueRuinLH = 0.50;
            //---
            EvoCOPHyFlexManager.changeValueAction = 0.30;

            //--- Boundaries
            EvoCOPHyFlexManager.minLocalLLH = 0.2;
            EvoCOPHyFlexManager.minMutationLLH = 0.05;
            EvoCOPHyFlexManager.minRuinLLH = 0.2;
            //---
            EvoCOPHyFlexManager.maxLocalLLH = 1.0;
            EvoCOPHyFlexManager.maxMutationLLH = 0.7;
            EvoCOPHyFlexManager.maxRuinLLH = 0.7;
        }
        //--- STREDNE TEZKA uloha
        else if(type == 1){
            maxGenes = 4;
            cycleEvals = 100;   //--- pocet ohodnoceni v jedne iteraci
            //--- TODO: konfigurace manageru
            EvoCOPHyFlexManager.nbActiveLLH = 4;       //--- zacina s 5 LLH v prototypu HH
            EvoCOPHyFlexManager.maxActiveLLH = EvoCOPHyFlexManager.nbActiveLLH + maxGenes;     //--- maximalni velikost HH

            //--- Resampling parameters
            EvoCOPHyFlexManager.startRecreate = 0.1;  //--- 0.05
            EvoCOPHyFlexManager.addRecreate = 0.15;    //--- 0.15
            EvoCOPHyFlexManager.startMutation = 0.1;  //--- 0.01
            EvoCOPHyFlexManager.addMutation = 0.15;    //--- 0.04
            //---
            EvoCOPHyFlexManager.ruinRate = 5;
            EvoCOPHyFlexManager.mutationRate = 5;

            //--- Standard evolution parameters
            EvoCOPHyFlexManager.startMinLocalLLH = 0.20;
            EvoCOPHyFlexManager.addValueLocalLH = 0.50;
            //---
            EvoCOPHyFlexManager.startMinMutationLLH = 0.20;
            EvoCOPHyFlexManager.addValueMutationLH = 0.30;
            //---
            EvoCOPHyFlexManager.startMinRuinLLH = 0.20;
            EvoCOPHyFlexManager.addValueRuinLH = 0.30;
            //---
            EvoCOPHyFlexManager.changeValueAction = 0.20;

            //--- Boundaries
            EvoCOPHyFlexManager.minLocalLLH = 0.2;
            EvoCOPHyFlexManager.minMutationLLH = 0.05;
            EvoCOPHyFlexManager.minRuinLLH = 0.1;
            //---
            EvoCOPHyFlexManager.maxLocalLLH = 0.75;
            EvoCOPHyFlexManager.maxMutationLLH = 0.5;
            EvoCOPHyFlexManager.maxRuinLLH = 0.5;
        }
        //--- TEZKA uloha
        else{
//            System.out.println("Very hard instance");
            maxGenes = 3;
            cycleEvals = 50;   //--- pocet ohodnoceni v jedne iteraci
            //--- TODO: konfigurace manageru
            EvoCOPHyFlexManager.nbActiveLLH = 3;       //--- zacina se 2 LLH v prototypu HH
            EvoCOPHyFlexManager.maxActiveLLH = EvoCOPHyFlexManager.nbActiveLLH + maxGenes;     //--- maximalni velikost HH
            //--- Restarts parameters
            EvoCOPHyFlexManager.startRecreate = 0.05;  //--- 0.025
            EvoCOPHyFlexManager.addRecreate = 0.1;     //--- 0.075
            EvoCOPHyFlexManager.startMutation = 0.05;   //--- 0.01
            EvoCOPHyFlexManager.addMutation = 0.1;     //--- 0.04
            //---
            EvoCOPHyFlexManager.ruinRate = 5;
            EvoCOPHyFlexManager.mutationRate = 5;

            //--- Standard evolution parameters
            EvoCOPHyFlexManager.startMinLocalLLH = 0.20;
            EvoCOPHyFlexManager.addValueLocalLH = 0.50;
            //---
            EvoCOPHyFlexManager.startMinMutationLLH = 0.10;
            EvoCOPHyFlexManager.addValueMutationLH = 0.20;
            //---
            EvoCOPHyFlexManager.startMinRuinLLH = 0.10;
            EvoCOPHyFlexManager.addValueRuinLH = 0.20;
            //---
            EvoCOPHyFlexManager.changeValueAction = 0.20;

            //--- Boundaries
            EvoCOPHyFlexManager.minLocalLLH = 0.2;
            EvoCOPHyFlexManager.minMutationLLH = 0.05;
            EvoCOPHyFlexManager.minRuinLLH = 0.05;
            //---
            EvoCOPHyFlexManager.maxLocalLLH = 0.5;
            EvoCOPHyFlexManager.maxMutationLLH = 0.3;
            EvoCOPHyFlexManager.maxRuinLLH = 0.3;
        }
    }


    /**
     * this method must be implemented, to provide a different name for each hyper-heuristic
     * @return a string representing the name of the hyper-heuristic
     */
    public String toString() {
            return "ISEA Hyper-Heuristic";
    }
}
