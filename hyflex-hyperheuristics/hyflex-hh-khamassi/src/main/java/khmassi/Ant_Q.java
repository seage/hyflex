package khmassi;

/**
 * edited by Dave Omrai
 */

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import java.util.Random;

public class Ant_Q extends HyperHeuristic{
	Random r = new Random(); 
	public Ant_Q(long seed) {
		super(seed);
	}
	
	public void initialisationAQ(double [][]AQ,double val,int L, int C)
	{ for(int i=0;i<L;i++)
		{for(int j=0;j<C;j++)
			AQ[i][j]=val;
		}
	}
	
	public double maxLigneTab(double[]tabW,int C)
	{ double max=-1;
		for(int j=0;j<C;j++)
		{if(tabW[j]> max) max=tabW[j];}
		return (max);
	}
	
	public int maxIndiceTab(double []tabW,int C)
	{ double max=-1;
	  int indice=0;
		for(int j=0;j<C;j++)
		{if(tabW[j]>= max) 
			{max=tabW[j];
			indice=j;}
			}
		return (indice);
	}
	public double maxLigneMat(double [][]T,int L,int C)
	{ double max=-1;
		for(int j=0;j<C;j++)
		{if(T[L][j]> max) max=T[L][j];}
		return (max);
	}
	
	public int maxIndiceMat(double [][]T,int L,int C)
	{ double max=-1;
	  int indice=0;
		for(int j=0;j<C;j++)
		{if(T[L][j]> max) 
			{max=T[L][j];
			indice=j;}
			}
		return (indice);
	}
	
	public void globalUpdateAQ(double [][]AQ,double alpha, double gamma,double deltaAQ,int C, int ind1, int ind2)
	{
			AQ[ind1][ind2]=AQ[ind1][ind2]+alpha*deltaAQ;
	 }
	public void evaporation(double [][]AQ,double alpha,int hs)
		{for(int i=0;i<hs;i++)
		{for( int j=0;j<hs;j++)
			AQ[i][j]=(1- alpha)*AQ[i][j];
		}
			}
	public void localUpdateAQ2(double [][]AQ,double alpha, double gamma, int C)
	{for(int i=0;i<C;i++)
	{for( int j=0;j<C;j++)
			AQ[i][j]=(1- alpha)*AQ[i][j]+alpha*(gamma* this.maxLigneMat(AQ, j, C));
	 }}
	
	public void localUpdateAQ(double [][]AQ,double alpha, double gamma, int C, int ind1, int ind2)
	{
		AQ[ind1][ind2]=(1- alpha)*AQ[ind1][ind2]+alpha*(gamma* this.maxLigneMat(AQ, ind2, C));
	}
	
	public void tabMultiplication(double []tabW, double[][]HE,double [][]AQ,double sigma,double beta,int L1, int L2,int C)
	{for(int j=0;j<C;j++)
	 tabW[j]=Math.pow(HE[L1][j],sigma)*Math.pow(AQ[L2][j],beta);}
	
	public void tabProbability(double [] TabProb,double []tabW, double sommeProb,int hs)
	{ double val;
	  for(int i=0;i<hs;i++)
		  TabProb[i]=0;
	  for(int i=0;i<hs;i++)
	  {
		  val=tabW[i]/sommeProb;
		  if(i==0)
		  TabProb[i]=val;
		  else
			  TabProb[i]=TabProb[i-1]+val; 
	  }
		
		
	}
	
	public int[] tabInd(double [][]HE, double val,int nbAnt, int hs )
	{ int []T=new int[nbAnt];
	  boolean trouve=false; int i=0,j,k=0,m;
	  for( m=0;m<nbAnt;m++)
		  T[m]=-1;
      while (i<nbAnt)
      {j=0;
 	    while ((j<hs)&&(!trouve))
         {if (HE[i][j]==val)
     	  {trouve=true;T[k]=i;k++;}
           j++;}
 	    i++;
       }
	  return(T);
	}
	
	public int searchIndiceBestVal(double []T,int size, double val)
	{boolean trouve=false;
	 int i=0,ind=-1;
	 while((i<size)&&(!trouve))
	 {if (T[i]==val)
	 {trouve=true; ind=i; }
	 i++;}
	 return(ind);
	}
	
	public int indHeuristicPro(double [] TabProb, double q,int hs)
	{ int H=-1;
		if ((q > 0) && (q <= TabProb[0]))
         { H = 0; }
        else
        {
        for (int i = 1; i < hs; i++)
        {
            if ((q > TabProb[i - 1]) && (q <= TabProb[i]))
            { H = i; }
        }
        }
    return(H);
	}
	
	public int indBestSolution(ProblemDomain problem,int solutionmemorysize)
	{ double max=0;
	  int j=-1,i=0;
		for(i=0;i<solutionmemorysize;i++)
		{if (1/(problem.getFunctionValue(i))>max)
			{max=1/(problem.getFunctionValue(i));
			 j=i;
			}
		}
		return(j);
		
	}
	public int indWorstSolution(ProblemDomain problem,int ind1, int ind2)
	{ double min=100000000;
	  int j=-1,i;
		for(i=ind1;i<ind2;i++)
		{if (1/(problem.getFunctionValue(i))<min)
			{min=1/(problem.getFunctionValue(i));
			 j=i;
			}
		}
		return(j);
		
	}
	
	public int indBestSolution(ProblemDomain problem,int ind1, int ind2)
	{ double max=0;
	  int j=-1,i=0;
		for(i=ind1;i<ind2;i++)
		{if (1/(problem.getFunctionValue(i))>max)
			{max=1/(problem.getFunctionValue(i));
			 j=i;
			}
		}
		return(j);
		
	}
	
	public boolean recherchTab(int []T,int h)
	{ boolean trouve=false;
	 int i=0;
	 
	 while((i<T.length)&&(!trouve))
	 {if(T[i]==h)
		 trouve=true;
	 	 i++;}
		 return(trouve);
	}
	public int categorieHeuristic(ProblemDomain problem,int h)
	{int[] local_search_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);
	 int[] mutation_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
	 int[] crossover_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
	 int[] ruin_recreate_heuristics = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
	 
	 if(this.recherchTab(local_search_heuristics,h)==true)
			return(1);
		else if(this.recherchTab(mutation_heuristics,h)==true)
			return(2);
		else if(this.recherchTab(crossover_heuristics,h)==true)
			return(3);
		else if(this.recherchTab(ruin_recreate_heuristics,h)==true)
			return(4);
		else return(0);
	}
	
	public void solve(ProblemDomain problem) 
	{	
		
		int i,j,k,nbIteration=100,nbAnt;
		double AQ0,alpha=0.1,gamma=0.3,W=10,sigma=1,beta=2,q0=0.9,q;
		int solutionmemorysize;
		int hs= problem.getNumberOfHeuristics();
		if ((problem.toString()).equals("Personnel Scheduling")==true)
		nbAnt=1;
		else nbAnt=2;
		solutionmemorysize=nbAnt*hs;
		double [][]HE=new double [nbAnt][hs];
		double [][]AQ=new double [hs][hs];
		int [][]seqHE=new int[nbAnt][250];
		double []TabProb=new double[hs];
		double []tabW=new double[hs];
		problem.setMemorySize(solutionmemorysize);
		
		//Solutions initialisation
		for(i=0;i<solutionmemorysize;i++)
		{problem.initialiseSolution(i);}
		
		AQ0=1/(problem.getBestSolutionValue());
		this.initialisationAQ(AQ, AQ0,hs, hs);
		k=0;
		for(i=0;i<solutionmemorysize;i=i+hs)
		{ 
		  for(j=0;j<hs;j++)
		   {problem.applyHeuristic(j,i+j,i+j); 
		    HE[k][j]=1/(problem.getFunctionValue(i+j));
		   }  
		  k++;
		}
		//initialise seqHE
		for(i=0;i<nbAnt;i++)
		{for(j=0;j<nbIteration;j++)
			seqHE[i][j]=-1;}
		
		// remplissage du tab de sequence d'heuristique
		i=0;
		for(j=0;j<nbAnt;j++)
		{   if(j<hs)
			{seqHE[j][0]=j;}
		    else
		    { seqHE[j][0]=j-hs;}
		}
		
		// general traitement
		while (!hasTimeExpired()) 
		{ double deltaAQ, BestVal=0,sommeProb;
		  int count,c,m=0,IndBestVal,IndBestAnt=-1, worstSolution,worstSolution2, bestSolution2, bestSolution;
		  count=0;
		  for(int h=0;h<nbIteration;h++)
		   {   m=0;
			    for(i=0;i<nbAnt;i++)
				{	problem.setIntensityOfMutation(0.2);
		     	    problem.setDepthOfSearch(0.2);
			    	sommeProb=0;
			    	c=seqHE[i][count];
			    	this.tabMultiplication(tabW, HE, AQ,sigma,beta, i, c, hs);
					for(int a=0;a<hs;a++)
					sommeProb=sommeProb+tabW[a];
					this.tabProbability(TabProb, tabW, sommeProb, hs);
						q=r.nextDouble();
						if(q<=q0)
							IndBestVal=this.indHeuristicPro(TabProb, q, hs);
				        else
				        	IndBestVal=this.maxIndiceTab(tabW, hs);
						////////////////////////////////////////////////////////////
						if(BestVal<(1/problem.getBestSolutionValue()))
						{BestVal=(1/problem.getBestSolutionValue());
						 IndBestAnt=i;}
						  //////////////////////////////////////////////////////////
						  worstSolution2=-1;
						  int K=0;
						  for(j=0;j<hs;j++)
					      {   bestSolution=this.indBestSolution(problem, 0, solutionmemorysize);
					          worstSolution=this.indWorstSolution(problem, m, m+hs);
					          bestSolution2=this.indBestSolution(problem, m, m+hs);
					          
					          if(worstSolution2==worstSolution)
					          {   int heu=r.nextInt(hs);
				                  int l=r.nextInt(solutionmemorysize);
				                  if (this.categorieHeuristic(problem, IndBestVal)==3)
					        	   problem.applyHeuristic(heu,bestSolution,worstSolution,j);
				                  else
				            	  problem.applyHeuristic(heu,worstSolution,j);
					              K++; }
					          
					          if(bestSolution2==worstSolution)
					          {   int heu=r.nextInt(hs);
					              int l=r.nextInt(solutionmemorysize);
					              if (this.categorieHeuristic(problem, IndBestVal)==3)
						        	problem.applyHeuristic(heu,bestSolution,l,worstSolution);
					              else
					            	problem.applyHeuristic(heu,l,worstSolution);
					        	  break;
					          }
					          
					        if (j==0)
							{if (this.categorieHeuristic(problem, IndBestVal)==3)
							{problem.applyHeuristic(IndBestVal,bestSolution,bestSolution2,j+m);}
					        else if (this.categorieHeuristic(problem, IndBestVal)==1)
					        {problem.applyHeuristic(IndBestVal,bestSolution,j+m);}
					        else if (this.categorieHeuristic(problem, IndBestVal)==2)
					        {problem.applyHeuristic(IndBestVal,bestSolution,j+m);}
					        else if (this.categorieHeuristic(problem, IndBestVal)==4)
					        {problem.applyHeuristic(IndBestVal,bestSolution,j+m);}
					        else
					        {problem.applyHeuristic(IndBestVal,bestSolution,j+m);}
							}
					        
					        else
					        {int heu=r.nextInt(hs);
				              if (this.categorieHeuristic(problem, IndBestVal)==3)
					        	problem.applyHeuristic(heu,bestSolution,j+m,j+m);
				              else
				            	problem.applyHeuristic(heu,bestSolution,j+m);}
					        	
					        HE[i][j]=1/(problem.getFunctionValue(j+m));
					        worstSolution2=worstSolution;
					        if (K==2) break;
					        }  
				 //local update
				 this.localUpdateAQ(AQ, alpha, gamma, hs, c,IndBestVal);
				 seqHE[i][count+1]=IndBestVal;
				 m=m+hs;
				}
			    
			    count++;
		   }
		       //global up date
		       this.evaporation(AQ, alpha, hs);
		       deltaAQ=W*(1/problem.getBestSolutionValue());
		       for(int n=1;n<nbIteration;n++)
		       this.globalUpdateAQ(AQ, alpha, gamma, deltaAQ, hs,seqHE[IndBestAnt][n-1],seqHE[IndBestAnt][n]);
		     
		}
		
	}
	
	
	
	
	
	
	
	public String toString() {
		return "Ant-Q Hyper Heuristic";}

}
