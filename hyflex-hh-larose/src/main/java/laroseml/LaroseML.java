package laroseml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

import BinPacking.BinPacking;
import FlowShop.FlowShop;
import PersonnelScheduling.PersonnelScheduling;
import SAT.SAT;


public class LaroseML extends HyperHeuristic
{
   // Init during "constructor"
   private Random _rng;

   private static int BEST_IND = 0;
   private static int CUR_IND = 1;
   private static int TEMP_COPY = 2;

   private static int POOL_SIZE = 3;

   private long _lastIterImprov;
   private long _maxNumIterWithoutImprov;

   private long _numIter;

   private int _lastDiv;

   // Init during "solve"
   private ProblemDomain _problem;

   private List<Heuristic> _intenHeu;
   private List<Heuristic> _divHeu;

   private RouletteWheelSelection _afterNoImprov;

   private List<RouletteWheelSelection> _duringLocalSearch;

   public LaroseML(long seed)
   {
      super(seed);
      _rng = rng;
      _maxNumIterWithoutImprov = 120;
   }


   private boolean diversification()
   {
      int heuInd = _afterNoImprov.select();

       _lastDiv = heuInd;

      if (heuInd == _divHeu.size())
         return false;

      _problem.applyHeuristic(_divHeu.get(heuInd).index(),
                              CUR_IND,
                              CUR_IND);

      if (hasTimeExpired())
         return false;

      // Reward diversification
      if (checkForNewBestSolution())
      {
	 _afterNoImprov.reward(heuInd);
         return true;
      }

      return false;      
   }
   
   private void initHeuristics()
   {
      _intenHeu = new ArrayList<Heuristic>();
      _divHeu = new ArrayList<Heuristic>();

      int[] intenHeu = _problem.getHeuristicsOfType(
         ProblemDomain.HeuristicType.LOCAL_SEARCH);

      if (intenHeu != null)
      {
         for (int i = 0; i < intenHeu.length; i++)
         {
            _intenHeu.add(new Heuristic(intenHeu[i]));
         }
      }

      
      int[] mutHeu = _problem.getHeuristicsOfType(
         ProblemDomain.HeuristicType.MUTATION);

      if (mutHeu != null)
      {
         for (int i = 0; i < mutHeu.length; i++)
         {
            _divHeu.add(new Heuristic(mutHeu[i]));
         }
      }

      int[] ruinRecreateHeu = _problem.getHeuristicsOfType(
         ProblemDomain.HeuristicType.RUIN_RECREATE);

      if (ruinRecreateHeu != null)
      {
         for (int i = 0; i < ruinRecreateHeu.length; i++)
         {
            _divHeu.add(new Heuristic(ruinRecreateHeu[i]));
         }
      }      
   }
   

   private boolean checkForNewBestSolution()
   {
      if (_problem.getFunctionValue(CUR_IND)
          <  _problem.getFunctionValue(BEST_IND))
      {
         _problem.copySolution(CUR_IND, BEST_IND);

         _lastIterImprov = _numIter;
         return true;
      }

      return false;
   }

   public boolean localSearch()
   {
      List<Experience> history = new ArrayList<Experience>();
      
      boolean foundNewBestSolution = false;

      int lastHeuristic = _intenHeu.size();

      while (true)
      {
         int heuristic = _duringLocalSearch.get(lastHeuristic).select();

         if (heuristic == -1)
            break;
         
         double lastObjValue = _problem.getFunctionValue(CUR_IND);

         double objValue
            = _problem.applyHeuristic(_intenHeu.get(heuristic).index(),
                                      CUR_IND,
                                      CUR_IND);

         boolean newBest = checkForNewBestSolution();
         
         foundNewBestSolution = foundNewBestSolution || newBest;
         
         if (hasTimeExpired())
            return foundNewBestSolution;

         double deltaObj = objValue - lastObjValue;
         
         history.add(new Experience(lastHeuristic, heuristic,
                                    deltaObj));
            

         if (deltaObj < 0)
         {
            for (int i = 0; i < _duringLocalSearch.size() - 1; i++)
            {
               _duringLocalSearch.get(i).enableAllChoices();
            }
         }
         else
         {
            for (int i = 0; i < _duringLocalSearch.size() - 1; i++)
            {
               _duringLocalSearch.get(i).disableChoice(heuristic);
            }
         }

         lastHeuristic = heuristic;
      }

      
      for (int i = 0; i < _duringLocalSearch.size() - 1; i++)
      {
         _duringLocalSearch.get(i).enableAllChoices();
      }


      // Reward localsearch
      if (foundNewBestSolution)
      {
         // Find last improv;
         int index = -1;

         for (int i = 0; i < history.size(); i++)
         {
            if (history.get(i).deltaObj < 0)
               index = i;
         }
         

         for (int i = 0; i <= index; i++)
         {
            Experience exp = history.get(i);

            _duringLocalSearch.get(exp.heuristic).reward(exp.heuristic2);
            
         }
      }

      return foundNewBestSolution;
   }

   public void solve(ProblemDomain problem)
   {
      _problem = problem;
      _problem.setMemorySize(POOL_SIZE);

      initHeuristics();

      _problem.initialiseSolution(BEST_IND);
      _problem.copySolution(BEST_IND, CUR_IND);
      _problem.copySolution(BEST_IND, TEMP_COPY);

      _numIter = 0;
      _lastIterImprov = 0;

      _afterNoImprov = new RouletteWheelSelection(_divHeu.size() + 1, -1, _rng);
      
      _duringLocalSearch = new ArrayList<RouletteWheelSelection>();

      for (int i = 0; i < _intenHeu.size(); i++)
      {
         _duringLocalSearch.add(new RouletteWheelSelection(_intenHeu.size(),
                                                           i, _rng));
      }

      // After diversification
      _duringLocalSearch.add(new RouletteWheelSelection(_intenHeu.size(),
                                                        -1, _rng));

      while (!hasTimeExpired())
      {
         _problem.copySolution(CUR_IND, TEMP_COPY);
         
         boolean divFoundNewBestSolution = diversification();

         if (hasTimeExpired())
            break;
         
         boolean foundNewBestSolution = localSearch();

         if (hasTimeExpired())
            break;

         // Reward indirectly diversification
         if (!divFoundNewBestSolution && foundNewBestSolution)
         {
            _afterNoImprov.reward(_lastDiv);
         }

         boolean lessThan = _problem.getFunctionValue(CUR_IND) <
            _problem.getFunctionValue(TEMP_COPY);
         
         boolean greaterThan = _problem.getFunctionValue(CUR_IND) >
            _problem.getFunctionValue(TEMP_COPY);
         
         boolean maxIter = _numIter -
            _lastIterImprov > _maxNumIterWithoutImprov;
         

         if (lessThan)
         {
            _lastIterImprov = _numIter;
         }
         else if (maxIter && greaterThan)
         {
            _lastIterImprov = _numIter;

         }
         else if (greaterThan)
         {
            // Undo modification on current solution

            _problem.copySolution(TEMP_COPY, CUR_IND);
         }


         _numIter++;
      }
   }

   public String toString()
   {
      return "ML";
   }

   // 0: time; 1: problem domain; 2: instance
   public static void main(String[] args) throws java.io.IOException
   {
      Random rng = new Random();
      long domainSeed = rng.nextLong();
      int problemDomainIndex = Integer.parseInt(args[1]);
      int instance = Integer.parseInt(args[2]);

      ProblemDomain problemDomain = null;

      switch(problemDomainIndex)
      {
      case 0:
         problemDomain = new SAT(domainSeed);
         break;
         
      case 1:
         problemDomain = new BinPacking(domainSeed);
         break;

      case 2:
         problemDomain = new PersonnelScheduling(domainSeed);
         break;

      case 3:
         problemDomain = new FlowShop(domainSeed);
         break;
      }
         
      LaroseML hyper = new LaroseML(rng.nextLong());
      
      hyper.setTimeLimit(Long.parseLong(args[0]) * 1000);
      problemDomain.loadInstance(instance);
      hyper.loadProblemDomain(problemDomain);
            
      hyper.run();

      double bestSolutionValue = hyper.getBestSolutionValue();

      System.out.println(bestSolutionValue);
   }
}


class Heuristic
{
   private int _index;
   
   public Heuristic(int index)
   {
      _index = index;
   }

   public int index() { return _index; }
}


class RouletteWheelSelection
{
   private Random _rng;
   private int[] _weights;
   private int _sum;
   private int[] _weightsCopy;
   
   public RouletteWheelSelection(int numChoices, int selfChoice, Random rng)
   {
      _rng = rng;
      
      _weights = new int[numChoices];
      Arrays.fill(_weights, 1);
      _sum = _weights.length;

      _weightsCopy = new int[numChoices];
      Arrays.fill(_weightsCopy, 0);

      if (selfChoice >= 0)
      {
         _weights[selfChoice] = 0;
         _sum -= 1;
      }
   }

   public void disableChoice(int choice)
   {
      _sum -= _weights[choice];
      _weightsCopy[choice] = _weights[choice];
      _weights[choice] = 0;
   }

   public void enableAllChoices()
   {
      for (int i = 0; i < _weights.length; i++)
      {
         _sum += _weightsCopy[i];
         _weights[i] += _weightsCopy[i];
         _weightsCopy[i] = 0;
      }
   }

   public void reward(int choice)
   {
      _sum += 1;
      _weights[choice] += 1;
   }

   public int select()
   {
      if (_sum == 0)
         return -1;


      int randNum = _rng.nextInt(_sum) + 1;

      int index;
      for (index = 0; index < _weights.length && randNum > 0; index++)
      {
         randNum -= _weights[index];
      }
      
      return index - 1;
   }

   public String toString()
   {
      String repr = new String();
      
      for (int i = 0; i < _weights.length; i++)
      {
         repr += _weights[i]+ " ";
      }

      return repr;
   }
}

class Experience
{
   public int heuristic;
   public int heuristic2;
   public double deltaObj;
   
   public Experience(int heuristic, int heuristic2, double deltaObj)
   {
      this.heuristic = heuristic;
      this.heuristic2 = heuristic2;
      this.deltaObj = deltaObj;
   }
}
