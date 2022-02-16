package bader;

//import java.math.*;
import java.util.Random;
import java.util.concurrent.*;
import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;


public class Clean02 extends HyperHeuristic {
        final int RR=1, LS=0, CR=2, MU=3;
        final int MANY = 100, TOOMANY=300;
        long    startTime;
        int     mutationList[];
        int     crossList[];
        int     rrList[];
        int     lsList[];
        int     improveList[];
        double  objList[];
	int     memSize;
        int     tempMem;
        int     performance[];
        int active=4;
        double[]  runFactor = {1, 1, 1, 1};
        boolean[] improvedGlobal = {false, false, false, false};
        int[]    hasNotImprovedfor = {0, 0, 0, 0};
        //int[]    runFor = {1,1,1};
        int     globalBest;
        boolean quitAll;
        int[]   runFor;
        int     popSize = 7;
        int     minPerformance = -10000;
        static int performanceStep = -100;
        int     torSize = 2;
        int     torBadSize = 3;
        int     maxRunBest = 1000;
        int maxRep=5;
        int     maxRun = 50;
        //boolean print = false;
        double acceptRate = 0.5; 
        double ruinAcceptRate = 0.3;
        double mutateAcceptRate = 0.3;
        ProblemDomain problem;
        int LOCKED=500;
        
        public Clean02(long seed) {
            super(seed);
	}
        
        public int selectInv() {
            int loc = rng.nextInt(popSize);
            int newLoc;
            for (int i=0; i<torSize; i++)
            {
                newLoc = rng.nextInt(popSize);
                while (true) {
                    if (loc == newLoc) {
			newLoc = rng.nextInt(popSize);
                } else {break;}}
                if (objList[newLoc]<objList[loc]) loc = newLoc;
            }
            return loc;
        }
        
        public int selectInvNotHead() {
            int loc = rng.nextInt(popSize);
            while(loc==globalBest) {loc = rng.nextInt(popSize);}
            int newLoc;
            for (int i=0; i<torSize; i++)
            {
                do{
                    newLoc = rng.nextInt(popSize);}
                while ((newLoc==globalBest)||(newLoc==loc));
   
                if (objList[newLoc]<objList[loc]) loc = newLoc;
            }
            return loc;
        }
        
        public int selectInvToReplaceNotHead() {
           int loc = rng.nextInt(popSize);
            while(loc==globalBest) {loc = rng.nextInt(popSize);}
            int newLoc;
            for (int i=0; i<torSize; i++)
            {
                do{
                    newLoc = rng.nextInt(popSize);}
                while ((newLoc==globalBest)||(newLoc==loc));
   
                if (objList[newLoc]>objList[loc]) loc = newLoc;
            }
            return loc;
        }
        
        public boolean isGlobalBest(double n, int loc) {
            if (problem.getFunctionValue(loc)<objList[globalBest]) 
            {
                quitAll = hasTimeExpired();
                globalBest = loc;
                return true;
            }
            return false;
        }
        
        public boolean cross(int loc1) {
            if (crossList.length<=0) return false;
            int loc2;//, loc1 = selectInv(); 
            do{ loc2 = selectInv(); }while(loc1==loc2);
            int newLoc=selectInvToReplaceNotHead();
            
            objList[newLoc] = problem.applyHeuristic(crossList[rng.nextInt
                    (crossList.length)], loc1, loc2, newLoc);   
            
            if (isGlobalBest(objList[newLoc], newLoc))  
            {
                //if (print) System.out.println("cross  obj "+objList[newLoc]);
                improvedGlobal[CR] = true;
                hasNotImprovedfor[CR]=0;
                return true;
            }
            return false;
        }        
                       
        public boolean ruin(int hToUse, int loc) {
            if (this.rrList.length<=0) return false;
            int newLoc;
            boolean quit=false, globalImproved=false;
            do{
                newLoc = selectInvToReplaceNotHead();
                objList[newLoc] = problem.applyHeuristic(hToUse, loc, newLoc);
                if (isGlobalBest(objList[newLoc], newLoc))
                {
                    //if (print) System.out.println("ruwin "+hToUse+" obj "+objList[newLoc]);
                    globalImproved = improvedGlobal[RR] = true;
                    hasNotImprovedfor[RR]=0;
                }
                else if (rng.nextDouble()<ruinAcceptRate)
                {
                    loc=newLoc;
                    hasNotImprovedfor[RR]++;
                }
                else
                {
                    quit=true;
                    hasNotImprovedfor[RR]++;
                }
            }while((!quit)&&(!quitAll));
            return globalImproved;
        }
        
        public boolean mutate(int hToUse, int loc) {
            if (this.rrList.length<=0) return false;
            int newLoc;
            boolean quit=false, globalImproved=false;
            do{
                newLoc = selectInvToReplaceNotHead();
                objList[newLoc] = problem.applyHeuristic(hToUse, loc, newLoc);
                if (isGlobalBest(objList[newLoc], newLoc))
                {
                    //if (print) System.out.println("mutate "+hToUse+" obj "+objList[newLoc]);
                    globalImproved = improvedGlobal[MU] = true;
                    hasNotImprovedfor[MU]=0;
                }
                else if (rng.nextDouble()<mutateAcceptRate)
                {
                    loc=newLoc;
                    hasNotImprovedfor[MU]++;
                }
                else
                {
                    quit=true;
                    hasNotImprovedfor[MU]++;
                }
            }while((!quit)&&(!quitAll));
            return globalImproved;
        }
        
        public boolean improveH(int i/*hToUse*/, int memLoc) {
            if (objList[memLoc]>=(6*objList[globalBest])) return false;
            if (improveList.length<=0) return false;
            
            boolean noHuristicsToUse = true;
            boolean globalBestImroved = false;
            boolean quit;
            int nRun=0;
            int rep=0;
            if (performance[i]>=minPerformance)
            {
                noHuristicsToUse = false;
                quit = false;
                nRun=0;
                do
                {
                    tempMem = selectInvToReplaceNotHead();
                    objList[tempMem] =  problem.applyHeuristic(improveList[i], 
                            memLoc, tempMem);
                    
                    if ((objList[tempMem]<objList[memLoc]))
                    {
                        memLoc = tempMem;
                        //problem.copySolution(tempMem, memLoc);
                        //objList[memLoc]=newObj;
                        if (globalBestImroved=isGlobalBest(objList[tempMem], memLoc)) 
                        {
                            //if (print) System.out.println("improve H"+improveList[i]+" loc "+i+" obj "+objList[tempMem]);
                            performance[i]+=9;
                            improvedGlobal[0] = true;
                            hasNotImprovedfor[LS]=0;
                        }
                        rep=0;
                            //performance[i]++;
                    }
                    else if (rep<maxRep)
                    {
                        memLoc = tempMem;
                        rep ++;
                        hasNotImprovedfor[LS]++;
                    }
                    else if ((rng.nextDouble()<acceptRate))
                    {
                        memLoc = tempMem;
                        rep=0;
                        hasNotImprovedfor[LS]++;
                    }
                    else 
                    {
                        performance[i]--;
                        quit = true;
                        rep=0;
                        hasNotImprovedfor[LS]++;
                    }
                }while((!quit)&&(nRun<maxRun));
            }
            else 
            {
                performance[i]++;
            }
            
            if (noHuristicsToUse) 
            {
                minPerformance += performanceStep;
                //globalBestImroved = improve( problem, memLoc);
            }
            return globalBestImroved;           
        }
        
    @Override
	public void solve(ProblemDomain p) 
        {   // ls mu rr or cr
            
            boolean isLocked=false;
            problem = p;
            startTime = System.currentTimeMillis();
            initLists();
                
            quitAll = false;
            int round =0, rateInc=0;
            while (!hasTimeExpired())
            {
                int h;
                //if (print) System.out.println("&&&&&&&&&&&&&&&&& start improve");
                for (int i=0; ((i<(improveList.length*runFactor[LS]))&&(!quitAll)); i++)
                {
                    improveH(h=rng.nextInt(improveList.length), globalBest);
                    if (quitAll=this.hasTimeExpired()) break;
                    improveH(h, tempMem);
                    improveH(rng.nextInt(improveList.length), selectInvNotHead());
                    if (quitAll=this.hasTimeExpired()) break;
                }
                
                if ((round==0)&&(!improvedGlobal[LS])) 
                {
                    acceptRate = 0;
                    runFactor[0]=-100;
                    //if (print) System.out.println("LS stoped---------------------------------------------------");
                }           
       
                if (hasNotImprovedfor[LS]>100)
                {
                    if ((acceptRate>=0.8)&&(active>1)&&(hasNotImprovedfor[RR]<100)&&(hasNotImprovedfor[LS]>300))
                    {
                        runFactor[0]=-100;
                        active--;
                    }
                    acceptRate = Math.min(acceptRate+0.05, 0.8);
                    //rateInc++;    
                    hasNotImprovedfor[LS]=0;
                }
                
    
                /*if ((round>5)&&(!improvedGlobal[0])) 
                {
                    runFactor[0]=-10;
                    //if (print) System.out.println("LS stoped---------------------------------------------------");
                }*/
                
                //if (print) System.out.println("&&&&&&&&&&&&&&&&& start ruin");
                for (int i=0; ((i<(rrList.length*runFactor[RR]))&&(!quitAll)); i++)
                {
                    while (ruin(rng.nextInt(rrList.length), globalBest)&&(!quitAll)){
                        quitAll=hasTimeExpired();
                        //if (print) System.out.println("rr yes");
                        if (quitAll=this.hasTimeExpired()) break;
                    } 
                    while (ruin(rng.nextInt(rrList.length), selectInvNotHead())&&(!quitAll)){
                        quitAll=hasTimeExpired();
                        //if (print) System.out.println("rr yes");
                        if (quitAll=this.hasTimeExpired()) break;
                    } 
                }    
                
                if ((round>=6)&&(!improvedGlobal[RR])&&(active>1)) 
                {
                    runFactor[RR]=-10;
                    
                }
    
                
                //if (print) System.out.println("&&&&&&&&&&&&&&&&& start mutate");
                for (int i=0; ((i<(mutationList.length*runFactor[MU]))&&(!quitAll)); i++)
                {
                    while (mutate(rng.nextInt(mutationList.length), globalBest)&&(!quitAll)){
                        quitAll=hasTimeExpired();
                        //if (print) System.out.println("MU yes");
                        if (quitAll=this.hasTimeExpired()) break;
                    } 
                    while (mutate(rng.nextInt(mutationList.length), selectInvNotHead())&&(!quitAll)){
                        quitAll=hasTimeExpired();
                        //if (print) System.out.println("MU yes");
                        if (quitAll=this.hasTimeExpired()) break;
                    } 
                }    
                
                if ((round>=6)&&(!improvedGlobal[MU])&&(active>1)) 
                {
                    runFactor[MU]=-10;
                    
                }
                
                
                //if (print) System.out.println("&&&&&&&&&&&&&&&&& start cross");
                for (int i=0; ((i<(crossList.length*runFactor[2]))&&(!quitAll)); i++)
                {
                    while (cross(globalBest)&&(!quitAll)){
                        quitAll=hasTimeExpired();
                        //if (print) System.out.println("rr yes");
                    } 
                    cross(selectInvNotHead());
                }   
                if ((round>=4)&&(!improvedGlobal[CR])&&(active>1)) 
                {
                    runFactor[CR]=-10;
                   
                }
                if ((round<=2)&&(!improvedGlobal[2])) 
                    improvedGlobal[2]=false;
                round++;
                
                isLocked=true;
                for (int i=0; i<4; i++)
                {
                    if ((runFactor[i]>0)&&(hasNotImprovedfor[i]<LOCKED))
                        isLocked=false;
                }
                
                if (isLocked)
                {
                    
                    //if (print) System.out.println("&&&&&&& UNLocked &&&&&&&&&&");
                    for (int i=0; i<4; i++)
                    {
                       runFactor[i] = 1;
                       
                     hasNotImprovedfor[i] = 0;
                    }
                       acceptRate = 0.7; 
                    ruinAcceptRate = 0.4;
                    mutateAcceptRate = 0.4;
                    runFactor[CR]=-10;
                    active=3;
                    round=0;
                }
                //for ()
                
                //##########################################################################
                //if (print) System.out.println("Time: "+getElapsedTime()/(double)getTimeLimit());
                /*if ((getElapsedTime()/(double)getTimeLimit())>=0.35) 
                {
                    for (int i=0; i<runFactor.length; i++)
                    {
                        if (improvedGlobal[i] == false) 
                        {
                            runFactor[i]=0;
                            //if (print) System.out.println("stop 0 "+i);
                        }
                    }       
                }
                else if ((getElapsedTime()/(double)getTimeLimit())>=0.20) 
                {
                    for (int i=0; i<runFactor.length; i++)
                    {
                        if (improvedGlobal[i] == false)
                        {
                            runFactor[i]-=0.5;
                            //if (print) System.out.println("stop 0.5 "+i);
                        }
                    }
                }
                else if ((getElapsedTime()/(double)getTimeLimit())>=0.05) 
                {
                    for (int i=0; i<runFactor.length; i++)
                    {
                        improvedGlobal[i] = false;
                    }
                }*/
            }
	}

    @Override
	public String toString() {
		return "TestNoThreads Hyper Heuristic";
	}
    
        public int getHSize(int n) {
            int sum = 0;
            if ((n&1)>0)  sum+= (problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH)).length;
            if ((n&2)>0)  sum+= (problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION)).length;
            if ((n&4)>0)  sum+= (problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE)).length;
            if ((n&8)>0)  sum+= (problem.getHeuristicsOfType(ProblemDomain.HeuristicType.OTHER)).length;
            if ((n&16)>0) sum+= (problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER)).length;
            return sum;
        }
        
        public int[] loadHOfType(int n) { // ls mu rr or cr
            int[] ls = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
            int[] mu = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
            int[] rr = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
            int[] or = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.OTHER);
            int[] cr = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
            int lsSize = 0, muSize=0, rrSize =0, orSize=0, crSize=0;    
            if ((ls!=null)&&((n&1)>0))  lsSize=ls.length;
            if ((mu!=null)&&((n&2)>0))  muSize=mu.length;
            if ((rr!=null)&&((n&4)>0))  rrSize=rr.length;
            if ((or!=null)&&((n&8)>0))  orSize=or.length;
            if ((cr!=null)&&((n&16)>0)) crSize=cr.length;
            
            int[] full =  new int[lsSize+muSize+rrSize+orSize+crSize];
            
            int xyz=0;
            if ((n&1)>0)
                for (int i=0; i<lsSize; i++ )
                {
                    full[xyz]=ls[i];
                    xyz++;
                }
            if ((n&2)>0)
                for (int i=0; i<muSize; i++ )
                {
                    full[xyz]=mu[i];
                    xyz++;
                }
            if ((n&4)>0)
                for (int i=0; i<rrSize; i++ )
                {
                    full[xyz]=rr[i];
                    xyz++;
                }
            if ((n&8)>0)
                for (int i=0; i<orSize; i++ )
                {
                    full[xyz]=or[i];
                    xyz++;
                }
            if ((n&16)>0)
                for (int i=0; i<crSize; i++ )
                {
                    full[xyz]=cr[i];
                    xyz++;
                }
            return full;
        }
        
        public void initLists() { // 1-LS 2-MU 4-RR 8-OR 16-CR 
            crossList = loadHOfType(16);
            /*if (getHSize(1) < getHSize(2)){
                improveList = loadHOfType(2);
                rrList = loadHOfType(5); }
            else{
                improveList = this.loadHOfType(1);
                rrList = loadHOfType(6);}*/
            if (getHSize(1) < getHSize(2))
            {
                improveList = this.loadHOfType(1);
                mutationList = loadHOfType(2);
            }
            else {
                mutationList = loadHOfType(1);
                improveList = this.loadHOfType(2);
            }
            rrList = loadHOfType(4);
            
                        performance = new int[improveList.length];
            runFor = new int[improveList.length];
            //if (print) System.out.println("imrov size "+rrList.length);
                
            memSize = popSize+1;
            tempMem = memSize - 1;
            problem.setMemorySize(memSize);
            objList = new double[memSize];
            problem.initialiseSolution(0);
            globalBest = 0;
            objList[0] = problem.getFunctionValue(0);
            for (int x = 1; x < popSize; x++) 
            {
                problem.initialiseSolution(x);
                objList[x] = problem.getFunctionValue(x);
                if (objList[x]<objList[globalBest]) globalBest=x;
            }
        }
        
        public void printTail() {
            
            
            //if (print) System.out.println("--------- "+minPerformance);
            //if (print) for (int i=0; i<improveList.length; i++)
            //{

                //if (print) System.out.println(i+" "+improveList[i]+" "+performance[i]+" -- "+(minPerformance-performance[i]));
            //}

            //long endTime = System.currentTimeMillis();
            //if (print) System.out.println("Total elapsed time in execution of method "+ "callMethod() is :"+ ((endTime-startTime)/1000.0)+ " min " + ((endTime-startTime)/60000.0) );
        }
}