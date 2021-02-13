package pearlhunter;

import java.lang.Math;
import java.util.*;
import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * This class is the Pearl Hunter prepared for <a href="http://www.asap.cs.nott.ac.uk/chesc2011/">CHeSC 2011</a>
 * (Cross-Domain Heuristic Search Challenge 2011) by Fan Xue  (dewolf_matri_x@msn.com) at Hong Kong Polytechnic University.
 * Pearl Hunter is implemented on the HyFlex platform provided by the organizer (See <a href="http://www.asap.cs.nott.ac.uk/chesc2011/hyflex_tutorial.html">http://www.asap.cs.nott.ac.uk/chesc2011/hyflex_tutorial.html</a>). 
 * <p>
 * Pearl Hunter is an iterated 
 * local search (ILS). Hunter considers LOCAL_SEARCH procedures as actions of "<strong>dive</strong>" and CROSSOVER, 
 * MUTATION, RUIN-RECREATE heurisitcs as actions of "<strong>move</strong>." So a typical ILS search is no more than an action sequence
 * "<strong>move</strong>-<strong>dive</strong>-<strong>move</strong>-<strong>dive</strong>" for Hunter. A typical run
 * can be found in the <a href="#solve(AbstractClasses.ProblemDomain)"><code>solve()</code></a> function.
 * <p>
 * <h3>Features:</h3>
 * <p>
 * Differs from common ILS, Pearl Hunter has two types of local search. One is Snorkeling() in "returns if any 
 * improvement" style, another is DeepDive() (scuba?) in "returns if no improvements can be further found" style. Usually 
 * Hunter tries a number of Snorkeling() before selecting one to DeepDive() in a typical <strong>dive</strong> action.
 * In the DeepDive() function, an ordered list of <strong>dive</strong> candidates "ABCD" is repeatedly excuted in two (approximate) parallel "ABACBADCBA" + "DCBACBABAA" 
 * sequences until no improvement found. Because of the inconsistencies of <strong>dive</strong> moves, such a DeepDive() with a proportion of redundant local search 
 * usually returns better results. The Snorkeling() function's test sequence is "ABACADA" and returns if any improvement is found. 
 * However, there are two kinds of exceptions for <strong>dive</strong>s.
 * In some cases, a single "A" action may cost a lot of time (e.g., 3% of overall time). Once such time-consuming local search is detected, 
 * a "sea trench" flag is marked. The parameter (DepthOfSearch) in the DeepDive() is reduced to 1/5, and the parallel sequences 
 * are replaced by "ABACADA" (same as the Snorkeling()'s). 
 * In some other (rare) cases, a "shallow water" flag is marked when "A" and "D" have done hundreds of improvements and "B" and "C" have none.
 * In "shallow water," the sequences of Snorkeling() and DeepDive() are simplified to "AB" and "A" + "BA" + "CA" + "DA" (minimal in four parallel) respectively.
 * <p>
 * Pearl Hunter also enables a combination of <strong>move</strong>s before a <strong>dive</strong>, which is uncommon in ILS. That means "<strong>move</strong>-<strong>move</strong>2-<strong>dive</strong>" or "<strong>move</strong>-<strong>move</strong>2-<strong>move</strong>3-<strong>dive</strong>" is also eligible.
 * In order to get rid of the starting position too far away from known ones, Hunter sets a "buoy" in the level (obj Value) 
 * of first <strong>dive</strong>. if the position of the first <strong>move</strong> does not exceed the "buoy," 
 * Hunter could accept another <strong>move2</strong> before <strong>dive</strong>. In other words, if trapped
 * in a "local optimum", Hunter will consider more diversifications before intensification. This feature is enabled
 * in <a href="#CO_BUOY_MODE">CO_BUOY_MODE</a>, <a href="#AVG_TEST_MODE">AVG_TEST_MODE</a>, and <a href="#AVG_BUOY_MODE">AVG_BUOY_MODE</a>.
 * <p>
 * Pearl Hunter is a clever program. Hunter usually spends first 20% of all job time to test instructed "<strong>dive</strong>s" and "<strong>move</strong>s," 
 * CROSSOVER <strong>move</strong>s (because of two solutions orientation) followed by <strong>dive</strong>s in the first half and MUTATION + RUIN-RECREATE <strong>move</strong> in the 
 * later half. Hunter makes a decision after that to choose a running mode from <a href="#CO_ONLY_MODE">CO_ONLY_MODE</a>, <a href="#CO_BUOY_MODE">CO_BUOY_MODE</a>, and <a href="#AVG_BUOY_MODE">AVG_BUOY_MODE</a>.
 * <p>
 * Pearl Hunter also restricts the number of low level <strong>move</strong>s. For example, if
 * there are 15 MUTATION <strong>move</strong>s, Hunter will test them quickly and choose 4 <strong>move</strong>s to practice later.
 * <p>
 * Other mechanisms, such as hashed memory (cache) of history of <strong>dive</strong> (also used as an 
 * unwanted - iferior to tabu - list), temporary tabu list in the <strong>move</strong>s around the buoy (such as 
 * "<strong>move</strong>2" and "<strong>move</strong>3"), records of best-so-far (pool), pool control, mission termination and restart (restart 
 * if no new solutions are found for x% of overall time), etc, can be found in detail in the codes.
 *
 * @author Fan Xue (dewolf_matri_x@msn.com) at Hong Kong Polytechnic University
 * @version 0.0.6
 * @see PearlHunter#solve
 * 
 */

public class PearlHunter extends HyperHeuristic {
	/**
	* trace level. <br>Possible values: <br><strong>0</strong> - silent(default), <br>1 - messages, <br>2 - verbose
	*/
	public static int TRACELEVEL = 0;
	
	final static int BUOY_MASK = 0x01;
	final static int TEST_MASK = 0x02;
	final static int CO_MASK = 0x04;
	final static int AVG_MASK = 0x08;
	final static int SEA_TRENCH_MASK = 0x10;
	final static int SLOW_LS_MASK = 0x20;
	/**
	* default running mode. <br>The program will test CROSSOVER heuristics in CO_TEST_MODE in the first 10% time, and MUTATION + RUIN-RECREATE heuristics in AVG_TEST_MODE in the later 10% time, and determine which of the following three modes --- CO_ONLY_MODE, CO_BUOY_MODE, AVG_BUOY_MODE --- to run in the rest of time. <br>Particularly, the program will turn into SEA_TRENCH_MODE in very early stage, if the DeepDive() (deep local search) consumes too much time.
	*/
	public final static int AUTO_MODE = 0;
	/**
	* a test mode, usually appears in the first 10% of the time horizon.
	*/
	public final static int CO_TEST_MODE = CO_MASK + TEST_MASK;
	/**
	* a test mode, usually appears in the second 10% of total time. Enables BUOY during this mode.
	*/
	public final static int AVG_TEST_MODE = AVG_MASK + TEST_MASK + BUOY_MASK;
	/**
	* a standard running mode. <br>Stesses on CROSSOVER moves, MUTATION & RUIN-RECREATE moves are completely ignored.
	*/
	public final static int CO_ONLY_MODE = CO_MASK;
	/**
	* a standard running mode. <br>Enables BUOY. <br>Mostly relies on CROSSOVER moves. But in case the solution meets the BUOY (objValue(solution) &lt; buoyInWater), MUTATION & RUIN-RECREATE moves are included.
	*/
	public final static int CO_BUOY_MODE = CO_MASK + BUOY_MASK;
	/**
	* a standard running mode. <br>Enables BUOY. <br>All low level moves are (almost) equally called by their merits. And in case the move meets the BUOY (objValue(solution) &lt; buoyInWater), MUTATION & RUIN-RECREATE moves are included.
	*/
	public final static int AVG_BUOY_MODE = AVG_MASK + BUOY_MASK;
	/**
	* a standard running mode. <br>Activated if actions in DeepDive() or Snorkeling() consumes too much time (&gt; 1% time for a dive). <br>In this mode, the DeepDive() will be replaced by SeaTrenchDive() for a fast descending, and the calls of moves are in "run-judge-run" styles, no more "test-judge-run."
	*/
	public final static int SEA_TRENCH_MODE = SEA_TRENCH_MASK;
	
	/**
	* mode to persist to run. Possible values: <br>AUTO_MODE(default), CO_ONLY_MODE, CO_BUOY_MODE, AVG_BUOY_MODE, SEA_TRENCH_MODE.
	*/
	public static int modeToPersist = AUTO_MODE;
	public static boolean FULL_LS = true;
	/**
	* flag of generating attributes of low level heuristics. <br>Note this flag breaks the normal program modes. <br>false(default) true
	*/
	public static boolean ATT_TEST = false;
	/**
	* flag of generating attributes of modes. <br>Note this flag breaks the normal program modes. <br>false(default) true
	*/
	public static boolean MOD_TEST = false;
	public static boolean SUB_TEST = false;
	public static boolean SUB_APPLY = false;
	public int runMode;
	/**
	* returns an attribute matrix of low level heuristic candidates.
	*/
	public static String heuristicsAttributes;
	/**
	* returns an attribute matrix of standard modes <br>(CO_ONLY_MODE, CO_BUOY_MODE, AVG_BUOY_MODE).
	*/
	public static String modeAttributes;
	
	public HeuristicCandidateList lDivList;
	public HeuristicCandidateList hDivList;
	public HeuristicCandidateList coList;
	public HeuristicCandidateList crsList;
	public HeuristicCandidateList rnrList;
	
	final static int MAX_BUOY_DEPTH = 2;
	final static int MAX_BUOY_TESTS = 12;
	final static int LS_TESTS = 2;
	final static int NON_LS_TESTS = 3;
	
	final static int BASE_CO_SNORKELING = 1;
	final static int BASE_MURR_SNORKELING = 1;
	final static int MAX_SNORKELING = 10;
	
	final static int MAX_QUICK_SIZE = 3000;
	final static int MAX_POOL_SIZE = 4;
	
	final static double FAST = 0.02;
	final static double SLOW = 0.05;
	private double waitTime;
	
	final static String VERSION = " 0.0.6";
	// alias
	private ProblemDomain myProblem;
	// mission related
	private int currentMissionID;
	private int lastBestMission;
	private int newSolutionsFoundInmission;
	
	private long lastBestTime;
	private long overallTime;
	private long overallEndTime;
	private long missionStartTime;
	private long missionEndTime;
	private long trackStartTime;
	
	// memory and objValues
	private double[] objValues;
	private int memSize;
	// pool
	private int poolOffset;
	private int poolSize;
	private boolean isPoolRun;
	// cache and unwanted list
	private Hashtable<String, QuickAttributes> quick;
	private int quickOffset;
	private int quickSize;
	private int quickHits;
	private QuickAttributes dummyQA;
	// DeepDive() variables
	private Vector<String> diveCache;
	private int deepHits;
	private int deepWins;
	private boolean shallowWater;
	// stat of two seq in DeepDive()
	private int seqHits;
	private int revHits;
	private int parHits;
	
	public int bestSolutionHits;
	// MD5 Hash for strings of solutions 
	private MD5 md5;
	
	private boolean divingSimplified = false;
	// TEST modes related
	private double LSMURRtestStartTime;
	private double bestTestedTwo;
	private double LSValue;
	private int ATT_LS_found;
	private int ATT_LS_tests;
	private int ATT_LS_MAX_found;
	// performace stat (3*3)
	private int[] dirHits;
	private int[] snorHits;
	private int[] lsHits;
	// overtime control
	private long overallDiveTime;
	
	// BUOY
	private double buoyInWater;
	private int buoyOffset;
	private HashSet<HeuristicCandidate> buoyTabu;
	HeuristicCandidateList[] buoyList;
	HeuristicCandidate[] buoyHeu;
	
	// SUB
	private int[] subImpCnt;

	/**
	 * Constructs a new Pearl Hunter with a random seed
	 * @param seed the random seed
	 */
	public PearlHunter(long seed) 
	{
		super(seed);
		currentMissionID = 0;
		newSolutionsFoundInmission = 0;
		lastBestMission = 0;
		poolSize = 0;
		diveCache = new Vector<String> (100);
		quick = new Hashtable<String, QuickAttributes>(4000);
		quickSize = 0;
		dummyQA = new QuickAttributes();
		dummyQA.index = -1;
		buoyInWater = -1;
		md5 = new MD5();
		quickHits = 0;
		deepHits = 0;
		shallowWater = false;
		isPoolRun = false;
		deepWins = 0;
		
		ATT_LS_found = 0;
		ATT_LS_tests = 0;
		ATT_LS_MAX_found = 0;
		
		waitTime = FAST;
		
		seqHits = 0;
		revHits = 0;
		parHits = 0;
		dirHits = new int[3];
		snorHits = new int[3];
		lsHits = new int[3];
		for (int i = 0 ; i < 3; i++)
		{
			dirHits[i] = 0;
			snorHits[i] = 0;
			lsHits[i] = 0;
		}
		bestSolutionHits = 0;
		
		buoyTabu = new HashSet<HeuristicCandidate> (MAX_BUOY_TESTS * 2);
		buoyList = new HeuristicCandidateList[MAX_BUOY_TESTS];
		buoyHeu = new HeuristicCandidate[MAX_BUOY_TESTS];
		
		modeAttributes = "";
		heuristicsAttributes = "";
		overallDiveTime = 0;
	}

	/**
	 * This method defines the strategy of the hyper-heuristic. 
	 * <p> A typical run (not SEA_TRENCH_MODE): 
	 * <br> 1. generates two candidate lists that consist of local search dives, one is set up with depth = 0.1 for Snorkeling(), another with depth = 1.0 for DeepDive(). tests the performances of all candidates, then sorts them. 
	 * <br> 2. generates candidate lists of crossover, mutation, ruin-recreate moves, set corresponding param to 0.2, 0.5, 0.8 if the heuristic accepts params. tests and sorts them.
	 * <br> 3. turns into <a href="#CO_TEST_MODE">CO_TEST_MODE</a>, test crossover moves for 10% of time horizon.  
	 * <br> 4. turns into <a href="#AVG_TEST_MODE">AVG_TEST_MODE</a>, test mutation and ruin-recreate moves for 10% of time horizon.  
	 * <br> 5. judges which mode to run officially (one from <a href="#CO_ONLY_MODE">CO_ONLY</a>, <a href="#CO_BUOY_MODE">CO_BUOY</a>, <a href="#AVG_BUOY_MODE">AVG_BUOY</a>), according to following rules:
	 * <br><pre>
Hmu >= 0.18929
|  Hrr >= 0.01538: AVG_BUOY(21.0/1.0)
|  Hrr < 0.01538
|  |  Hdir < 0.06731: AVG_BUOY(6.0/0.0)
|  |  Hdir >= 0.06731: CO_BUOY(7.0/0.0)
Hmu < 0.18929
|  Hdir >= 0.34482: CO_BUOY(10.0/0.0)
|  Hdir < 0.34482
|  |  LSs >= 28.0: CO_ONLY(18.0/1.0)
|  |  LSs < 28.0
|  |  |  Hall < 59.0: CO_ONLY(10.0/1.0)
|  |  |  Hall >= 59.0
|  |  |  |  LSs >= 14.5: CO_BUOY(6.0/0.0)
|  |  |  |  LSs < 14.5
|  |  |  |  |  MUD < 9.0: CO_ONLY(4.0/1.0)
|  |  |  |  |  MUD >= 9.0
|  |  |  |  |  |  Hall < 78.0: CO_BUOY(2.0/0.0)
|  |  |  |  |  |  Hall >= 78.0: AVG_BUOY(2.0/0.0)
where:
  "Top solution" - a solutions s satisfies objValue(s) < objValue(NEXT_BEST_SOLUTION)
  Hmu - percent of top solutions found after Mutation moves (including direct result, by Snorkeling(), by DeepDive())
  Hrr - percent of top solutions found after RuinRecreate moves (including direct result, by Snorkeling(), by DeepDive())
  Hdir - percent of top solutions found immediately after some moves (including three sources of moves: Mutation, RuinRecreate, Crossover)
  LSs - Number of CO_ONLY missions completed in CO_TEST_MODE
  Hall - Number of top solutions
  MUD - Number of top solutions found in the AVG_TEST_MODE mission (Depth of Mutation + RuinRecreate)
</pre>
	 * <br> 6. applies "move*n-dive-move*n-dive" actions according to the determined mode.
	 * <br> Run an uneven sequence for moves until 65% of time horizon has passed from the start of Hunter. E.g., a testing sequence is "ABCDABA" for a move candidate list "ABCD."
	 * <br> In the final 35% time, moves are selected according to how many top solutions have been found in their names. 
	 * @param problem the problem domain to be solved
	 */
	public void solve(ProblemDomain problem) 
	{
		myProblem = problem;
		if (myProblem == null)
		{
			System.out.println("[Error] The problem domain is null.");
			System.out.println("[Error] Pearl Hunter stopped.");
			return;
		}
		
		overallTime = getTimeLimit();
		lastBestTime = System.currentTimeMillis();
		overallEndTime = overallTime + lastBestTime;
		missionEndTime = overallEndTime;
		
		if (lDivList == null)
			// HeuristicCandidateLists of local search with PARAM_LS_LOW (0.1) & PARAM_LS_HIGH (1.0)
			lDivList = new HeuristicCandidateList(myProblem, ProblemDomain.HeuristicType.LOCAL_SEARCH, 
													HeuristicCandidateList.PARAM_LS_LOW);
		if (hDivList == null)
			hDivList = new HeuristicCandidateList(myProblem, ProblemDomain.HeuristicType.LOCAL_SEARCH, 
													HeuristicCandidateList.PARAM_LS_HIGH);
		if (coList == null)
			// HeuristicCandidateLists of crossover, mutation, and ruin-recreate with PARAM_LOW (0.2) + PARAM_MED (0.5) +  + PARAM_HIGH (0.8)
			coList = new HeuristicCandidateList(myProblem, ProblemDomain.HeuristicType.CROSSOVER,
													HeuristicCandidateList.PARAM_ALL);
		if (crsList == null)
			crsList = new HeuristicCandidateList(myProblem, ProblemDomain.HeuristicType.MUTATION,
													HeuristicCandidateList.PARAM_ALL);
		if (rnrList == null)
			rnrList = new HeuristicCandidateList(myProblem, ProblemDomain.HeuristicType.RUIN_RECREATE,
													HeuristicCandidateList.PARAM_ALL);

		if (SUB_TEST )
		{
			if (coList != null && crsList != null && rnrList != null)
				subImpCnt = new int[(coList.Length() + crsList.Length() + rnrList.Length()) * 3];
			else
				subImpCnt = new int[myProblem.getNumberOfHeuristics() * 3];
			for (int i = 0; i < subImpCnt.length; i++)
				subImpCnt[i] = 0;
		}
			
		if (lDivList == null || lDivList.Length() == 0)
		{
			System.out.println("[Warning] The problem domain contains no Local Search algorithms.");
			System.out.println("[Warning] Pearl Hunter can't dive :(");
			//return;
		}
		if (!(modeToPersist == AUTO_MODE || modeToPersist == CO_ONLY_MODE 
			|| modeToPersist == CO_BUOY_MODE || modeToPersist == AVG_BUOY_MODE || modeToPersist == SEA_TRENCH_MODE))
		{
			System.out.println("[Error] 'modeToPersist' is not set as a valid value.");
			return;
		}
		else if (modeToPersist != AUTO_MODE)
			runMode = modeToPersist;

		// memory size = Candidates for LS + test mem for LS (shared) + a safe mem for CO, MU, RR + snorkeling mem + best solution in last mission + mem for cruise_one_cand
		memSize = LS_TESTS + LS_TESTS * lDivList.Length() + Math.max(Math.max(crsList.Length(), coList.Length()), rnrList.Length()) * NON_LS_TESTS + MAX_POOL_SIZE + MAX_QUICK_SIZE + 5 + 2 + 5 + 1 + 1;
		poolOffset = LS_TESTS + LS_TESTS * lDivList.Length() + Math.max(MAX_BUOY_TESTS, Math.max(Math.max(crsList.Length(), coList.Length()), rnrList.Length()) * NON_LS_TESTS);
		buoyOffset = LS_TESTS + LS_TESTS * lDivList.Length();
		quickOffset = poolOffset + MAX_POOL_SIZE;
		myProblem.setMemorySize(memSize);
		
		// a synchronized list of obj values
		objValues = new double[memSize];
		// in the 1st mission, no solution from LAST mission
		objValues[memSize - 2] = -1;
		
		// advise a gc
		System.gc();
		
		// "random" init. Well for some problems, it seems greedy init. 
		InitSolutions(LS_TESTS + LS_TESTS * lDivList.Length());
		
		// tests of low level local search (snorkeling)
		Dive(lDivList, LS_TESTS, LS_TESTS * lDivList.Length());
		
		
		if (isSeaTrench())
		{
			for (HeuristicCandidate hc : lDivList.candidates)
				hc.param1 = HeuristicCandidateList.PARAM_LS_HIGH / 5.0;
			// use same list
			hDivList = lDivList;
		}
		else
		{
			RecordBests(LS_TESTS, LS_TESTS, LS_TESTS * lDivList.Length());
			// tests of deep local search (scuba)
			if (!SUB_APPLY)
			{
				Dive(hDivList, LS_TESTS, LS_TESTS * hDivList.Length());
				RecordBests(LS_TESTS, LS_TESTS, LS_TESTS * hDivList.Length());
			}
		}
		
		isPoolRun = false;
		LSValue = -1;
		
		LSMURRtestStartTime = System.currentTimeMillis();
		if (!isSeaTrench())
			runMode = CO_TEST_MODE;
		bestTestedTwo = Double.MAX_VALUE;
		while(!hasTimeExpired())
		{
			if (ATT_TEST)
				runMode = AVG_BUOY_MODE;
			if (TRACELEVEL > 0)
				System.out.println("Start a new mission ...");
			currentMissionID ++;
			missionStartTime = System.currentTimeMillis();
			if (isAvg())
			{
				isPoolRun = true;
			}
			else
				isPoolRun = false;
			// finds two solutions for solution-based heuristics.  
			if (objValues[memSize - 2] >= 0)
			{
				// not the first mission
				if (objValues[memSize - 2] < objValues[1])
					swap(1 ,memSize - 2);
					
				if (poolSize > 2 && isPoolRun)
				{
					do
					{
						int rn = poolOffset + rng.nextInt(poolSize);
						copy(rn, 0);
					}
					while (myProblem.compareSolutions(0, 1) || myProblem.compareSolutions(0, memSize - 2));
					if (objValues[0] < objValues[memSize - 2])
						swap(0, memSize - 2);
					isPoolRun = true;
				}
				else
				{
				// add one new solution 
					InitSolutions(1);
					DeepDive(hDivList, 0);
					if (objValues[0] < objValues[memSize - 2])
						swap(0, memSize - 2);
				}
			}
			else if (!isSeaTrench())
			{
				// first mission, works on the best two ever got
				DeepDive(hDivList, 0);
				if (buoyInWater < 0)
					buoyInWater = objValues[0];
				DeepDive(hDivList, 1);
				AddPool(1);
				if (objValues[0] < objValues[1])
					swap(0, 1);
				copy(0, memSize - 2);
			}
			else
			{
				DeepDive(hDivList, 0);
				AddPool(0);
				if (objValues[0] < objValues[1])
					swap(0, 1);
				if (buoyInWater < 0)
					buoyInWater = objValues[1];
			}
						
			if (isSeaTrench())
			{
				CruiseSeaTrench(crsList, rnrList, coList, hDivList);
				for (int j = 0; j < 3; j++)
				{
					HeuristicCandidateList myList = coList;
					if (j == 1)
						myList = crsList;
					else if (j ==2)
						myList = rnrList;
					if (SUB_TEST || SUB_APPLY)
					{
						int hid = myList.candidates[0].id;
						int para = 0;
						if (myList.candidates[0].param1 == HeuristicCandidateList.PARAM_MED)
							para = 1;
						else if (myList.candidates[0].param1 == HeuristicCandidateList.PARAM_HIGH)
							para = 2;
						subImpCnt[hid * 3 + para] ++;
					}
				}
			}
			else
			{
				if (currentMissionID == 1 && !SUB_APPLY && !ATT_TEST)
				{
					HeuristicCandidateList toTest = hDivList;
					
					objValues[memSize - 3] = Double.MAX_VALUE;
					objValues[memSize - 4] = Double.MAX_VALUE;
					objValues[memSize - 5] = Double.MAX_VALUE;
					
					// tests performances
					TestCruise(coList, toTest, true);
					TestCruise(crsList, toTest, false);
					TestCruise(rnrList, toTest, false);
					
					if (objValues[memSize - 5] < objValues[0])
					{
						copy(memSize - 5, 0);
						AddPool(0);
					}
					if (objValues[memSize - 4] < objValues[memSize - 2])
					{
						copy(memSize - 4, memSize - 2);
						AddPool(memSize - 2);
					}
					if (objValues[memSize - 3] < objValues[1])
					{
						copy(memSize - 3, 1);
						AddPool(1);
					}
				}
				if (currentMissionID == 1 && runMode == CO_TEST_MODE)
				{
					// backup
					copy(0, memSize - 10);
					copy(1, memSize - 11);
					copy(memSize - 2, memSize - 12);
				}
				
				Cruise(crsList, rnrList, coList, hDivList, lDivList, 0);
				
				double testNow = ((double)System.currentTimeMillis() - LSMURRtestStartTime) / overallTime;
				
				if (runMode == CO_TEST_MODE)
				{
					// recording
					ATT_LS_found += newSolutionsFoundInmission;
					ATT_LS_tests ++;
					ATT_LS_MAX_found = Math.max(newSolutionsFoundInmission, ATT_LS_MAX_found);
				}
				
				if (runMode == CO_TEST_MODE && LSValue < 0 && testNow >= 0.1 && objValues[1] > 0 && objValues[memSize - 2] > 0)
				{
					LSValue = bestTestedTwo;
					bestTestedTwo = Double.MAX_VALUE;
					LSMURRtestStartTime = System.currentTimeMillis();
					
					if (!MOD_TEST && now() < 0.5)
					{
						if (TRACELEVEL > 0)
							System.out.println("[Mode Change] Entering CO_BUOY_MODE mode ...");
						runMode = CO_BUOY_MODE;
					}
					else
					{
						runMode = AVG_TEST_MODE;
						swap(memSize - 10, 0);
						swap(memSize - 11, 1);
						swap(memSize - 12, memSize - 2);
					}
					if (MOD_TEST)
						modeAttributes += "" + ATT_LS_tests + "," + (((double)ATT_LS_found) / ATT_LS_tests) + "," 
											+ ATT_LS_MAX_found + "," + (LSValue / 2.0) + ",";
				}
				else if (runMode == AVG_TEST_MODE && testNow >= 0.1 && objValues[1] > 0 && objValues[memSize - 2] > 0)
				{
					LSMURRtestStartTime = System.currentTimeMillis();
					double myVal = bestTestedTwo;
					int MU_num = currentMissionID - ATT_LS_tests;
					
					if (!shallowWater && hDivList.Length() > 2 && hDivList.candidates[0].weight > 50 && hDivList.candidates[1].weight == 1)
						shallowWater = true;
					
					if (modeToPersist > 0)
					{
						runMode = modeToPersist;
						continue;
					}
					if (MOD_TEST)
					{
						modeAttributes += "" + (currentMissionID - ATT_LS_tests) + "," + newSolutionsFoundInmission + "," + (myVal/2) + "," + (shallowWater? 1 : 0) + ",";
						for (int i = 0; i < 3 ;i ++)
							modeAttributes += "" + lsHits[i]+","+dirHits[i]+","+snorHits[i]+",";
						modeAttributes += "\n";
						return;
					}
					
					// select running mode
					// summarize attributes in this run
					int MUD = newSolutionsFoundInmission;
					double Hmu = lsHits[0] + dirHits[0] + snorHits[0];
					double Hrr = lsHits[1] + dirHits[1] + snorHits[1];
					double Hco = lsHits[2] + dirHits[2] + snorHits[2];
					double Hls = lsHits[0] + lsHits[1] + lsHits[2];
					double Hdir = dirHits[0] + dirHits[1] + dirHits[2];
					double Hsor = snorHits[0] + snorHits[1] + snorHits[2];
					int Hall = (int)(Hmu + Hrr + Hco);
					Hmu /= Hall;
					Hrr /= Hall;
					Hco /= Hall;
					Hls /= Hall;
					Hdir /= Hall;
					Hsor /= Hall;
					int LSs = ATT_LS_tests;
					double LSDa = (double)ATT_LS_found / ATT_LS_tests;
					int LSDm = ATT_LS_MAX_found;
					int Val = 0;
					if (myVal < LSValue)
						Val = -1;
					else if (myVal > LSValue)
						Val = 1;
						
					// choose via rules
					int mode = -1;
					
					//normal rule
					if (Hmu >= 0.18929)
					{
						mode = 2;		// AVG_BUOY
						if (Hrr < 0.01538 && Hdir >= 0.06731)	// minor exceptions
							mode = 3;	// CO_BUOY
					}
					else if (Hdir >= 0.34482)
						mode = 3;		// CO_BUOY
					else
					{
  						mode = 1;		// CO_ONLY
						if (LSs < 14.5 && Hall >= 78.0 && MUD >= 9.0)
							mode = 2;	// AVG_BUOY
						else if (LSs < 14.5 && Hall >= 59.0 && Hall < 78.0 && MUD >= 9.0)
							mode = 3;	// CO_BUOY
						else if (LSs >= 14.5 && LSs < 28.0 && Hall >= 59.0)
							mode = 3;	// CO_BUOY
					}
					
					 /*
					// + QAP rules
					if (Hmu >= 0.18929)
					{
						if (MUD >= 10)
							mode = 2;
						else
						{
							if (Hsor >= 0.96875)
								mode = (MUD < 3.5) ? 2 : 1;
							else
								mode = (Hls < 0.63333 && Hmu >= 0.55) ? 3 : 1;
						}
					}
					else
					{
						if (Hdir >= 0.34482)
							mode = 3;
						else
						{
							if (LSs >= 28.0)
								mode = 1;
							else if (Hall < 59.0)
								mode = 1;
							else
							{
								if (LSs >= 14.5 || (MUD >= 9.0 && Hall < 78.0))
									mode = 3;
								else if (MUD < 9.0)
									mode = 1;
								else
									mode = 2;
							}
						}
					}*/
					
					if (mode < 0)
					{
						System.out.println("[Warning] run mode is not properly determined by program.");
						mode = 3;
					}
					
/*
[*] CO: CO_ONLY;  AB: AVG_BU;  CB: CO_BU
[*] Priority:  CB > CO > AB

weka.classifiers.trees.BFTree -S 1 -M 2 -N 5 -C 1.0 -P POSTPRUNED

Best-First Decision Tree

Hmu >= 0.18929
|  Hrr >= 0.01538: AB(21.0/1.0)
|  Hrr < 0.01538
|  |  Hdir < 0.06731: AB(6.0/0.0)
|  |  Hdir >= 0.06731: CB(7.0/0.0)
Hmu < 0.18929
|  Hdir >= 0.34482: CB(10.0/0.0)
|  Hdir < 0.34482
|  |  LSs >= 28.0: CO(18.0/1.0)
|  |  LSs < 28.0
|  |  |  Hall < 59.0: CO(10.0/1.0)
|  |  |  Hall >= 59.0
|  |  |  |  LSs >= 14.5: CB(6.0/0.0)
|  |  |  |  LSs < 14.5
|  |  |  |  |  MUD < 9.0: CO(4.0/1.0)
|  |  |  |  |  MUD >= 9.0
|  |  |  |  |  |  Hall < 78.0: CB(2.0/0.0)
|  |  |  |  |  |  Hall >= 78.0: AB(2.0/0.0)

Time taken to build model: 0.01 seconds

=== Evaluation on training set ===
=== Summary ===

Correctly Classified Instances          86               95.5556 %
Incorrectly Classified Instances         4                4.4444 %
Kappa statistic                          0.933 
Mean absolute error                      0.0457
Root mean squared error                  0.1512
Relative absolute error                 10.3262 %
Root relative squared error             32.1361 %
Total Number of Instances               90     

=== Detailed Accuracy By Class ===

TP Rate   FP Rate   Precision   Recall  F-Measure   ROC Area  Class
  0.926     0          1         0.926     0.962      0.998    CB
  0.97      0.053      0.914     0.97      0.941      0.988    CO
  0.967     0.017      0.967     0.967     0.967      0.993    AB

=== Confusion Matrix ===

  a  b  c   <-- classified as
 25  2  0 |  a = CB
  0 32  1 |  b = CO
  0  1 29 |  c = AB
  
  
another ---- QAP
Best-First Decision Tree

Hmu < 0.18929
|  Hdir < 0.34482
|  |  LSs < 28.0
|  |  |  Hall < 59.0
|  |  |  |  LSDa < 8.52778: CO(8.0/0.0)
|  |  |  |  LSDa >= 8.52778: CO(2.0/1.0)
|  |  |  Hall >= 59.0
|  |  |  |  LSs < 14.5
|  |  |  |  |  MUD < 9.0: CO(4.0/1.0)
|  |  |  |  |  MUD >= 9.0
|  |  |  |  |  |  Hall < 78.0: CB(2.0/0.0)
|  |  |  |  |  |  Hall >= 78.0: AB(2.0/0.0)
|  |  |  |  LSs >= 14.5: CB(6.0/0.0)
|  |  LSs >= 28.0: CO(18.0/1.0)
|  Hdir >= 0.34482: CB(10.0/0.0)
Hmu >= 0.18929
|  MUD < 10.0
|  |  Hsor < 0.96875
|  |  |  Hls < 0.63333
|  |  |  |  Hmu < 0.55: CO(1.0/1.0)
|  |  |  |  Hmu >= 0.55: CB(17.0/1.0)
|  |  |  Hls >= 0.63333: CO(3.0/2.0)
|  |  Hsor >= 0.96875
|  |  |  MUD < 3.5: AB(8.0/0.0)
|  |  |  MUD >= 3.5: CO(3.0/0.0)
|  MUD >= 10.0: AB(21.0/1.0)

Size of the Tree: 27

Number of Leaf Nodes: 14

Time taken to build model: 0.02 seconds

=== Evaluation on training set ===
=== Summary ===

Correctly Classified Instances         105               92.9204 %
Incorrectly Classified Instances         8                7.0796 %
Kappa statistic                          0.8935
Mean absolute error                      0.0733
Root mean squared error                  0.1915
Relative absolute error                 16.5149 %
Root relative squared error             40.6392 %
Total Number of Instances              113     

=== Detailed Accuracy By Class ===

TP Rate   FP Rate   Precision   Recall  F-Measure   ROC Area  Class
  0.897     0.014      0.972     0.897     0.933      0.977    CB
  1         0.081      0.867     1         0.929      0.987    CO
  0.886     0.013      0.969     0.886     0.925      0.984    AB

=== Confusion Matrix ===

  a  b  c   <-- classified as
 35  3  1 |  a = CB
  0 39  0 |  b = CO
  1  3 31 |  c = AB
*/
					
					if (mode == 1)
					{
						if (TRACELEVEL > 0)
							System.out.println("[Mode Change] Entering CO_ONLY_MODE ...");
						runMode = CO_ONLY_MODE;
					}
					else if (mode == 2)
					{
						// AVG_Buoy mode
						if (TRACELEVEL > 0)
							System.out.println("[Mode Change] Entering AVG_BUOY_MODE ...");
						runMode = AVG_BUOY_MODE;
					}
					else
					{
						// CO_Buoy mode
						if (TRACELEVEL > 0)
							System.out.println("[Mode Change] Entering CO_BUOY_MODE ...");
						runMode = CO_BUOY_MODE;
					}
					
					if (objValues[memSize - 10] < objValues[0])
						swap(memSize - 10, 0);
					if (objValues[memSize - 11] < objValues[1])
						swap(memSize - 11, 1);
					if (objValues[memSize - 12] < objValues[memSize - 2])
						swap(memSize - 12, memSize - 2);
				}
			}
			
			if (hasTimeExpired())
				return;
		}
		return;
	}
	
	/**
	 * This method initialize first x solutions in the pool, by calling initialiseSolution()
	 * @param size how many solutions to init
	 */
	private void InitSolutions(int size)
	{
        if (TRACELEVEL > 0)
		{
			System.out.println("[init] Solutions initialization ...");
			for (int i = 0; i < LS_TESTS && i < size; i++)
				System.out.print("\tNo. " + i);
			System.out.println();
		}
		
		long randStart = System.currentTimeMillis();
		for (int i = 0; i < LS_TESTS && i < size; i++)
		{
			myProblem.initialiseSolution(i);
			objValues[i] = myProblem.getFunctionValue(i);
			if (TRACELEVEL > 0) 
				System.out.print("\t" + objValues[i] );
		}
		if (TRACELEVEL > 0) 
			System.out.println(">");
		for (int i = 0; currentMissionID <= 1 && i < LS_TESTS * 10 && System.currentTimeMillis() - randStart < overallTime / 1000; i++)
		{
			myProblem.initialiseSolution(memSize - 3);
			objValues[memSize - 3] = myProblem.getFunctionValue(memSize - 3);
			if (TRACELEVEL > 0) 
				System.out.print("\t" + objValues[memSize - 3] );
			for (int j = 0; j < LS_TESTS; j++)
			{
				objValues[j] = myProblem.getFunctionValue(j);
				if (objValues[memSize - 3] < objValues[j] && (j == 0 || objValues[memSize - 3] > objValues[j - 1]))
				{
					swap(j, memSize - 3);
					break;
				}
			}
		}
		if (TRACELEVEL > 0) 
			System.out.println(">");
		for (int i = 0; i < size; i++)
		{
			if (i >= LS_TESTS)
			{
				copy(i % LS_TESTS, i);
				continue;
			}
			else
				objValues[i] = myProblem.getFunctionValue(i);
			if (TRACELEVEL > 0) 
				System.out.print("\t" + objValues[i] );
		}
		
        if (TRACELEVEL > 0) 
			System.out.println();
	}
	
	/**
	 * This method tries a given heuristic until no more improvements. The local obj values are updated at the same time. 
	 * @param heuristicID which heuristic to try
	 * @param solutionID which solution to work on
	 * @return obj value
	 */
	private double FullSearch(int heuristicID, int solutionID)
	{
		for (double objValueBefore = Double.POSITIVE_INFINITY; objValueBefore > objValues[solutionID] && !hasTimeExpired(); )
		{
			objValueBefore = objValues[solutionID];
			myProblem.applyHeuristic(heuristicID, solutionID, solutionID);
			objValues[solutionID] = myProblem.getFunctionValue(solutionID);
		}
		return objValues[solutionID];
	}

	/**
	 * This method applies a set of local search heuristics on a block of solutions
	 * @param hList the set of heuristics
	 * @param testBegin the beginning solution
	 * @param length length of the solution block
	 */
	private void Dive(HeuristicCandidateList hList, int testBegin, int length)
	{
		if (TRACELEVEL > 0 && length > 1)
			System.out.println("[Diving tests] Local search tests (n = " + hList.Length() + "), depth of search = " + hList.getParam1(0) + " ...");
		
		// reset performances
		hList.ResetTestedObjValues();
		
		int testEnd = testBegin + length;
		for (int i = testBegin; i < testEnd && !hasTimeExpired(); i += LS_TESTS)
		{
			int candidateIndex = (i - testBegin) / LS_TESTS;
			int heuristicID = hList.getHeuristic(candidateIndex);
			double param1 = hList.getParam1(candidateIndex);
			double beforeSearch = objValues[i];
			// set up DepthOfSearch
			if (param1 > 0)
				myProblem.setDepthOfSearch(param1);
				
			long startTime = System.currentTimeMillis();
			// search
			if (!hList.IsDisabled(candidateIndex))
				FullSearch(heuristicID, i);
			// write its performance
			hList.setTestedObjValues(candidateIndex, objValues[i]);
			
			long time = System.currentTimeMillis() - startTime;
			hList.setTestedTime(candidateIndex, time);
			
			// check if isSeaTrench()
			if (time > overallTime * 0.01 && !isSeaTrench())
			{
				if (TRACELEVEL > 0)
					System.out.println("[Mode change] Entering SEA_TRENCH mode. This mode handles very slow DeepDive() actions.");
				runMode = SEA_TRENCH_MODE;
			}
			else if (time > overallTime * 0.001 && !isSlowLS())
			{
				runMode += SLOW_LS_MASK;
			}
				
			if (TRACELEVEL > 1)
				System.out.print("\t" + objValues[i] );
			if (TRACELEVEL > 1 && i % LS_TESTS == LS_TESTS - 1)
				System.out.println("\t by LS " + heuristicID);
		}
		
		for (int i = testBegin + 1; i < testEnd && now() > 0.85 && !hasTimeExpired(); i += LS_TESTS)
		{
			int candidateIndex = (i - testBegin) / LS_TESTS;
			int heuristicID = hList.getHeuristic(candidateIndex);
			double param1 = hList.getParam1(candidateIndex);
			// set up DepthOfSearch
			if (param1 > 0)
				myProblem.setDepthOfSearch(param1);
				
			long startTime = System.currentTimeMillis();
			// search
			if (!hList.IsDisabled(candidateIndex))
				FullSearch(heuristicID, i);
			// write its performance
			hList.setTestedObjValues(candidateIndex, objValues[i]);
			
			// check if isSeaTrench()
			long time = System.currentTimeMillis() - startTime;
			hList.setTestedTime(candidateIndex, time);
				
			if (TRACELEVEL > 1)
				System.out.print("\t" + objValues[i] );
			if (TRACELEVEL > 1 && i % LS_TESTS == LS_TESTS - 1)
				System.out.println("\t by LS " + heuristicID);
		}
		// sort local search heuristics by performances
		hList.Sort();
	}
	
	/**
	 * calls DeepDive(__, __, 0.1);
	 * @param hList the set of heuristics
	 * @param index index of the solution
	 */
	private boolean DeepDive(HeuristicCandidateList hList, int index)
	{
		return DeepDive(hList, index, 0.1);
	}
	
	/**
	 * This method aims at a deep search (diving), finding all possible improvements. Enables combination of local search (e.g., 0, 1-0, 2-0).
	 * @param hList the set of heuristics
	 * @param index index of the solution
	 * @param timeLimit range = [0-1] of overall time
	 */
	private boolean DeepDive(HeuristicCandidateList hList, int index, double timeLimit)
	{
		String s0, s1;
		s0 = "";
		
		long diveStartMillis = System.currentTimeMillis();
		if (deepHits > 0 && overallEndTime - diveStartMillis < overallDiveTime / deepHits)
			return true;
		
		deepHits++;
		
		//double bestV = objValues[index];
		double initV = objValues[index];
		double diveStart = (double)(overallEndTime - System.currentTimeMillis()) / overallTime;
		
		if (index >= memSize || initV < 0)
			return false;
			
		s0 = getMD5(index);
		diveCache.clear();
		diveCache.add(s0);
		boolean anyQHit = false;
		double before, before1;
		if (!isSeaTrench())
		{
			if (!shallowWater && !isTest() && hList.Length() > 2 && hList.candidates[0].weight > 50 && hList.candidates[1].weight == 1)
				shallowWater = true;
			int offset = LS_TESTS;
			copy(index, memSize - 1);
			if (shallowWater)
			{
				for (int i = 0; i < hList.Length(); i++)
				{
					copy (index, offset + i);
				}
				for (int i = 0; i < hList.Length(); i++)
				{
					objValues[offset + i] = myProblem.applyHeuristic(hList.getHeuristic(i), offset + i, offset + i);
					if (i > 0)
						objValues[offset + i] = myProblem.applyHeuristic(hList.getHeuristic(0), offset + i, offset + i);
					if (objValues[offset + i] < objValues[index])
						copy(offset + i, index);
				}
					s0= getMD5(index); 
					if (quick.containsKey(s0))
					{
						boolean falseQuick = false;
						if (!anyQHit)
						{
							quickHits ++;
							anyQHit = true;
						}
						QuickAttributes qa = quick.get(s0);
						if (qa.index >= 0 && objValues[index] >= objValues[qa.index])
						{
							qa.count ++;
							copy(qa.index, index);
							if (TRACELEVEL > 1)
								System.out.println(" -- A quick diving via hashtable.");
						}
						else
							falseQuick = true;
						//if (objValues[index] < initV)
						//	deepWins++;
					}
					diveCache.add(s0);
			}
			else
			{
				do
				{
					before = objValues[index];
					// PH 
					if (now() > diveStart - timeLimit)
						DiveRedudant(hList, index, timeLimit * 1.1 / 2);
					if (now() > diveStart - timeLimit)
						DiveRedudantReverse(hList, memSize - 1, timeLimit * 1.1 / 2);
					
					if (objValues[index] > objValues[memSize - 1])
					{
						revHits ++;
						copy(memSize - 1, index);
					}
					else if (objValues[index] < objValues[memSize - 1])
						seqHits ++;
					else
						parHits ++;
					
					// Hsiao's 
					//if (now() > diveStart - timeLimit)
					//	DiveQueue(hList, index, timeLimit);
						
					s0= getMD5(index); 
					if (quick.containsKey(s0))
					{
						boolean falseQuick = false;
						if (!anyQHit)
						{
							quickHits ++;
							anyQHit = true;
						}
						QuickAttributes qa = quick.get(s0);
						if (qa.index >= 0 && objValues[index] >= objValues[qa.index])
						{
							qa.count ++;
							copy(qa.index, index);
							if (TRACELEVEL > 1)
								System.out.println(" -- A quick diving via hashtable.");
						}
						else
							falseQuick = true;
						//if (objValues[index] < initV)
						//	deepWins++;
						if (!falseQuick)
							break;
					}
					diveCache.add(s0);
				}
				while (false);
				//while (before > objValues[index] && !hasTimeExpired() && now() > diveStart - timeLimit);
			}
			
			if (TRACELEVEL > 0 && deepHits % 100 ==0)
				System.out.println("seq="+seqHits +", rev="+revHits+", par="+parHits+" @ "+deepHits);
		}
		else
		{
			SeaTrenchDive(hList, index, timeLimit);
		}
		
		if (true)
		{
			QuickAttributes qret = null;
			String ret = getMD5(index);
			diveCache.add(ret);
			for (String s : diveCache)
				if (quick.containsKey(s))
				{
						//break;
						
					QuickAttributes qa = quick.get(s);
					if (qret == null || objValues[qret.index] < 0 
						|| ( qa.index >= 0 && objValues[qa.index] <= objValues[qret.index]))
					{
						if (!anyQHit)
						{
							quickHits ++;
							anyQHit = true;
						}
						qret = qa;
						break;
					}
				}
			
			if (qret != null) 
			{
				qret.count ++;
				if ((qret.index >=0 && objValues[qret.index] >= 0 && objValues[qret.index] < objValues[index]))
				{
					copy(qret.index, index);
				}
				else
					qret = null;
			}
			if (qret == null || qret.index < 0)
			{
				qret = new QuickAttributes();
				qret.count = 1;
				if (quickSize >= MAX_QUICK_SIZE)
					qret.index = -1;
				else
				{
					qret.index = quickOffset + quickSize;
					copy(index, qret.index);
					quickSize++;
				}
			}
			for (String str : diveCache)
				if (!quick.containsKey(str))
				{
					quick.put(str, qret);
					if (TRACELEVEL > 0 && quick.size() % 100 == 0)
						System.out.println(" -- quick hash size = " + quick.size() + ", storage size = " + quickSize + ". ("+ deepWins+ "/"+ deepHits +" q="+quickHits+")");
				}
		}
		if (objValues[index] < initV)
			deepWins++;

		overallDiveTime += System.currentTimeMillis() - diveStartMillis;
		return false;
	}
	
	/**
	* returns if any improvement is found.
	* @param hList list local search algorithms
    * @param index solution index
	*/
	private void Snorkeling(HeuristicCandidateList hList, int index)
	{
		/*
		// for test PH without snorkeling
			String sa = getMD5(index);
			if (quick.containsKey(sa))
			{
				QuickAttributes qa = quick.get(sa);
				if (qa.index >= 0 && objValues[qa.index] < objValues[index])
				{
					copy(qa.index, index);
				}
			}
		if (true)
			return;
		*/
		if (shallowWater)
		{
			String s = getMD5(index);
			if (!quick.containsKey(s))
				quick.put(s, dummyQA);
			objValues[index] = myProblem.applyHeuristic(hList.getHeuristic(0), index, index);
			s = getMD5(index);
			if (!quick.containsKey(s))
				quick.put(s, dummyQA);
			return;
		}
		
		if (TRACELEVEL > 1)
			System.out.print("\n[Snorkeling]: Local search tests at Solution #" + index + " ...");
		
		double bestV = objValues[index];
		// set up DepthOfSearch
		// assume all the params in a local search list are the same.
		if (hList.getParam1(0) > 0)
			myProblem.setDepthOfSearch(hList.getParam1(0));
		int maxIndex = hList.Length();
		double objValueBefore;
		for (int j =  0; j < maxIndex && !hasTimeExpired() && bestV <= objValues[index]; j++)
		{
			int i = j % hList.Length();
			objValueBefore = objValues[index];
			if (hList.IsDisabled(i))
				continue;
			// combines two local search together
			FullSearch(hList.getHeuristic(i), index);
			objValues[index] = myProblem.getFunctionValue(index);
				
			if (TRACELEVEL > 1)
				System.out.print("\t" + objValues[index]);
			if (objValues[index] < bestV)
			{
				hList.candidates[i].weight ++;
				return;
			}
			for (double st = Double.MAX_VALUE; st > objValues[index] && !hasTimeExpired();)
			{
				st = objValues[index];
				FullSearch(hList.getHeuristic(0), index);
				objValues[index] = myProblem.getFunctionValue(index);
			}
			if (objValues[index] < bestV)
				return;
		}
	}
	
	private void SeaTrenchDive(HeuristicCandidateList hList, int index, double timeLimit)
	{
		if (TRACELEVEL > 1)
			System.out.println("\n[Slow diving] at Solution #" + index + " ...");
		
		double diveStart = now();
		
		double bestV = objValues[index];
		double objValueBefore;
		// set up DepthOfSearch
		if (hList.getParam1(0) > 0)
			myProblem.setDepthOfSearch(hList.getParam1(0));
		int maxIndex = Math.min(hList.Length(), 3);
		int backupIndex = memSize - 1;
		
		copy(index, backupIndex);
		
		// slow dive
		int lastImp = maxIndex;
		for (int i = 0; i < lastImp && !hasTimeExpired() && now() > diveStart - timeLimit; i++)
		{
			objValueBefore = objValues[index];
			objValues[index] = myProblem.applyHeuristic(hList.getHeuristic(i % maxIndex), index, index);
			if (TRACELEVEL > 1)
				System.out.print(objValues[index] + "-");
			if (objValues[index] < objValueBefore && now() > diveStart - timeLimit)
			{
				hList.candidates[i % maxIndex].weight ++;
				if (i % maxIndex > 1 && hList.candidates[i % maxIndex].weight > hList.candidates[(i % maxIndex) - 1].weight)
				{
					HeuristicCandidate hc = hList.candidates[(i % maxIndex) - 1];
					hList.candidates[(i % maxIndex) - 1] = hList.candidates[i % maxIndex];
					hList.candidates[i % maxIndex] = hc;
				}
				lastImp = i + maxIndex;
				if (i == 0 && !hasTimeExpired() && now() > diveStart - timeLimit)
				{
					objValueBefore = objValues[index];
					myProblem.setDepthOfSearch(1);
					FullSearch(hList.getHeuristic(0), index);
					// check unexpected up dives...
					if (objValues[index] < objValues[backupIndex])
						copy(index, backupIndex);
					else if (objValues[index] > objValues[backupIndex])
					{
						copy(backupIndex, index);
						return;
					}
				}
				else if (!hasTimeExpired() && now() > diveStart - timeLimit)
				{
					if (hList.getParam1(0) > 0)
						myProblem.setDepthOfSearch(hList.getParam1(0));
					myProblem.applyHeuristic(hList.getHeuristic(0), index, index);
					objValues[index] = myProblem.getFunctionValue(index);
					// check unexpected up dives...
					if (objValues[index] < objValues[backupIndex])
						copy(index, backupIndex);
					else if (objValues[index] > objValues[backupIndex])
					{
						copy(backupIndex, index);
						return;
					}
				}
			}
			if (TRACELEVEL > 1)
				System.out.print(objValues[index] + "..");
		}
		if (TRACELEVEL > 1)
			System.out.println();
	}
	
	private void DiveRedudantReverse(HeuristicCandidateList hList, int index, double timeLimit)
	{
		if (TRACELEVEL > 1)
			System.out.print("\n[Snorkeling]: Local search tests at Solution #" + index + " ...");
		
		double diveStart = now();		
		double bestV = objValues[index];
		int maxIndex = hList.Length();

		double objValueBefore;
		for (int j =  maxIndex - 1; j >= 0  && !hasTimeExpired() && now() > diveStart - timeLimit; j--)
		{
			int i = j % hList.Length();
			//objValueBefore = objValues[index];
			if (hList.IsDisabled(i))
				continue;
			// set up DepthOfSearch
			if (hList.getParam1(i) > 0)
				myProblem.setDepthOfSearch(hList.getParam1(i));
			// combines two local search together
			FullSearch(hList.getHeuristic(i), index);
			objValues[index] = myProblem.getFunctionValue(index);
				
			if (TRACELEVEL > 1)
				System.out.print("\t" + objValues[index]);
			if (objValues[index] < bestV)
			{
				bestV = objValues[index];
				hList.candidates[i].weight ++;
				j =  maxIndex;
				if (i > 0 && hList.candidates[i].weight > hList.candidates[i - 1].weight)
				{
					HeuristicCandidate swapHC = hList.candidates[i];
					hList.candidates[i] = hList.candidates[i - 1];
					hList.candidates[i - 1] = swapHC;
				}
			}
			// Redudant local search
			for (int st = 0; st < i && !hasTimeExpired() && now() > diveStart - timeLimit; st++)
			{
				FullSearch(hList.getHeuristic(st), index);
				objValues[index] = myProblem.getFunctionValue(index);
			}
			if (objValues[index] < bestV)
				j =  maxIndex;
		}
	}
	
	private void DiveRedudant(HeuristicCandidateList hList, int index, double timeLimit)
	{
		if (TRACELEVEL > 1)
			System.out.print("\n[Deep dive]: Local search tests at Solution #" + index + " ...");
			
		double diveStart = now();
		int maxIndex = hList.Length() ;
		if (maxIndex > 3)
			maxIndex = 3;
		for (double objValueBefore = Double.POSITIVE_INFINITY; objValueBefore > objValues[index] && !hasTimeExpired() && now() > diveStart - timeLimit;)
		{
			objValueBefore = objValues[index];
			for (int i = 0; i < maxIndex && !hasTimeExpired() && now() > diveStart - timeLimit; i++)
			{
				if (hList.IsDisabled(i))
					continue;
				// set up DepthOfSearch
				if (hList.getParam1(i) > 0)
					myProblem.setDepthOfSearch(hList.getParam1(i));
				
				// combines two local search together
				double beforeSearch = objValues[index];
				FullSearch(hList.getHeuristic(i), index);
				if (objValues[index] < beforeSearch)
				{
					hList.candidates[i].weight++;
					if (i > 0 && hList.candidates[i].weight > hList.candidates[i - 1].weight)
					{
						HeuristicCandidate swapHC = hList.candidates[i];
						hList.candidates[i] = hList.candidates[i - 1];
						hList.candidates[i - 1] = swapHC;
					}
				}
				// Redudant local search
				for (int st = i - 1; st >=0 && !hasTimeExpired() && now() > diveStart - timeLimit; st--)
				{
					beforeSearch = objValues[index];
					FullSearch(hList.getHeuristic(st), index);
					if (objValues[index] < beforeSearch)
						hList.candidates[st].weight++;
				}
					
				if (TRACELEVEL > 1)
					System.out.print("\t" + objValues[index]);
			}
		}
	}
	
	private void DiveQueue(HeuristicCandidateList hList, int index, double timeLimit)
	{
		if (TRACELEVEL > 1)
			System.out.print("\n[Queue dive]: Local search tests at Solution #" + index + " ...");
		
		double diveStart = now();
		int maxIndex = hList.Length() ;
		if (maxIndex > 3)
			maxIndex = 3;
		
		String s[] = new String[maxIndex];
		int candLeft = 2;
		for (boolean continueQueue = true; continueQueue && candLeft > 0 && !hasTimeExpired() && now() > diveStart - timeLimit; candLeft--)
		{
			continueQueue = false;
			String s0 = getMD5(index);
			for (int i = 0; i < maxIndex && !hasTimeExpired() && now() > diveStart - timeLimit; i++)
			{
				if (hList.IsDisabled(i))
					continue;
				// set up DepthOfSearch
				if (hList.getParam1(i) > 0)
					myProblem.setDepthOfSearch(hList.getParam1(i));
				
				// combines two local search together
				copy(index, LS_TESTS + i);
				FullSearch(hList.getHeuristic(i), LS_TESTS + i);
				if (objValues[LS_TESTS + i] < objValues[index])
				{
					continueQueue = true;
					copy(LS_TESTS + i, index);
					hList.candidates[i].weight++;
					candLeft = 3;
					break;
					/*
					if (i > 0 && hList.candidates[i].weight > hList.candidates[i - 1].weight)
					{
						HeuristicCandidate swapHC = hList.candidates[i];
						hList.candidates[i] = hList.candidates[i - 1];
						hList.candidates[i - 1] = swapHC;
					}*/
				}
				s[i] = getMD5(index);
				if (TRACELEVEL > 1)
					System.out.print("\t" + objValues[LS_TESTS + i]);
			}
			
			for (int i = 0; i < maxIndex && !continueQueue; i++)
				if (objValues[LS_TESTS + i] == objValues[index] && !s0.equals(s[i]))
				{
					continueQueue = true;
				}
				
		}
	}
	
	/**
	 * This method copies best x solutions to the head of the solution pool.
	 * @param size how many best solutions to aqquire
	 * @param beginIndex the beginning solution
	 * @param length length of the solution block
	 */
	private void RecordBests(int size, int beginIndex, int length)
	{
		int endIndex = beginIndex + length;
		double[] values = new double[size];
		int[] bests = new int[size];
		
		for (int i = 0; i < size; i++)
		{
			values[i] = Double.POSITIVE_INFINITY;
			bests[i] = -1;
		}
		
		for (int i = beginIndex; i < endIndex; i++)
			for (int j = 0; j < size; j++)
			{
				if (objValues[i] < values[j])
				{
					for (int k = size - 1; k > j; k--)
					{
						values[k] = values[k - 1];
						bests[k] = bests[k - 1];
					}
					values[j] = objValues[i];
					bests[j] = i;
					break;
				}
			}
			
		for (int i = 0; i < size; i++)
		{
			if (bests[i] >= 0 && objValues[i] > objValues[bests[i]])
			{
				copy(bests[i], i);
				if (i == 0)
					lastBestTime = System.currentTimeMillis();
			}
		}
	}
	
	/**
	 * This method applies a set of non-local-search heuristics on a block of solutions
	 * @param crsList the set of heuristics
	 * @param hDivList the set of local search heuristics for deep search (diving) 
	 * @param isCrossover whether they are crossover operations
	 */
	private void TestCruise(HeuristicCandidateList crsList, HeuristicCandidateList hDivList, boolean isCrossover)
	{
		if (TRACELEVEL > 0)
			System.out.println("\n[Cruise] start test cruise algorithms (" + crsList.Length() + ")...");
			
		int numTests = NON_LS_TESTS;
		if (isSeaTrench() || isSlowLS() || SUB_TEST)
			numTests = 1;
		boolean endNormally = false;
		crsList.ResetTestedObjValues();
		
		// arrays def for analyzing attributes --- start
		double[][] vBefore = new double[crsList.Length()][numTests];
		double[][] vAfter = new double[crsList.Length()][numTests];
		double[] bMax = new double[crsList.Length()];
		double[] bMin = new double[crsList.Length()];
		double[] bAvg = new double[crsList.Length()];
		double[] cMax = new double[crsList.Length()];
		double[] cMin = new double[crsList.Length()];
		double[] cAvg = new double[crsList.Length()];
		double[] vMax = new double[crsList.Length()];
		double[] vMin = new double[crsList.Length()];
		double[] vAvg = new double[crsList.Length()];
		for (int i = 0; i < crsList.Length(); i++)
		{
			bMax[i] = 0;
			bMin[i] = 0;
			bAvg[i] = 0;
			cMax[i] = 0;
			cMin[i] = 0;
			cAvg[i] = 0;
			vMax[i] = 0;
			vMin[i] = 0;
			vAvg[i] = 0;
		}
		int[] bMaxOrd = new int[crsList.Length()];
		int[] bMinOrd = new int[crsList.Length()];
		int[] bAvgOrd = new int[crsList.Length()];
		int[] cMaxOrd = new int[crsList.Length()];
		int[] cMinOrd = new int[crsList.Length()];
		int[] cAvgOrd = new int[crsList.Length()];
		int[] vMaxOrd = new int[crsList.Length()];
		int[] vMinOrd = new int[crsList.Length()];
		int[] vAvgOrd = new int[crsList.Length()];
		for (int i = 0; i < crsList.Length(); i++)
		{
			bMaxOrd[i] = i;
			bMinOrd[i] = i;
			bAvgOrd[i] = i;
			cMaxOrd[i] = i;
			cMinOrd[i] = i;
			cAvgOrd[i] = i;
			vMaxOrd[i] = i;
			vMinOrd[i] = i;
			vAvgOrd[i] = i;
		}
		int[] vRevBefore = new int[numTests];
		int[] vRevAfter = new int[numTests];
		for (int i = 0; i < numTests; i++)
		{
			vRevBefore[i] = i;
			vRevAfter[i] = i;
		}
		
		// arrays def for analyzing attributes --- end
		
		long testStart = System.currentTimeMillis();
		for (int i = 0; i < crsList.Length() && !hasTimeExpired(); i++)
		{
			for (int j = 0; j < numTests; j++)
			{
				// temp mem space for tests
				int crsID = LS_TESTS + LS_TESTS * hDivList.Length() + j * crsList.Length() + i;
				// sets up IntensityOfMutation
				if (crsList.getParam1(i) > 0)
					myProblem.setIntensityOfMutation(crsList.getParam1(i));
				// applies the heuristic i#
				if (isCrossover)
					objValues[crsID] = myProblem.applyHeuristic(crsList.getHeuristic(i), 1, memSize - 2, crsID);
				else
				{
					copy(1, crsID);
					FullSearch(crsList.getHeuristic(i), crsID);
				}
					
				// record value by heuristic i#
				vBefore[i][j] = objValues[crsID];
				
				if (!isSeaTrench())
				{
					// deep dive
					Snorkeling(hDivList, crsID);
					DeepDive(hDivList, crsID, 0.005);
				}
				else
				{
					Snorkeling(hDivList, crsID);
				}
				
				// record value improved by local search
				vAfter[i][j] = objValues[crsID];
					
				// sets up performances
				crsList.setTestedObjValues(i, objValues[crsID]);
				
				if (objValues[crsID] < objValues[memSize - 3])
					copy(crsID, memSize - 3);
				else if (objValues[crsID] < objValues[memSize - 4] && objValues[crsID] > objValues[memSize - 3])
					copy(crsID, memSize - 4);
				else if (objValues[crsID] < objValues[memSize - 5] && objValues[crsID] > objValues[memSize - 4])
					copy(crsID, memSize - 5);
			}
			if (TRACELEVEL > 1)
				System.out.print(crsList.candidates[i].objValue +",");
				
			for (int j = 0; j < numTests; j++)
			{
				bAvg[i] += vBefore[i][j];
				if (bMin[i] == 0 || vBefore[i][j] < bMin[i])
					bMin[i] = vBefore[i][j];
				if (bMax[i] == 0 || vBefore[i][j] > bMax[i])
					bMax[i] = vBefore[i][j];
					
				double vDiff = vBefore[i][j] - vAfter[i][j];
				cAvg[i] += vDiff;
				if (cMin[i] == 0 || vDiff < cMin[i])
					cMin[i] = vDiff;
				if (cMax[i] == 0 || vDiff > cMax[i])
					cMax[i] = vDiff;
					
				vAvg[i] += vAfter[i][j];
				if (vMin[i] == 0 || vAfter[i][j] < vMin[i])
					vMin[i] = vAfter[i][j];
				if (vMax[i] == 0 || vAfter[i][j] > vMax[i])
					vMax[i] = vAfter[i][j];
			}
			// returns same solution always?
			crsList.candidates[i].RevOrder = bMin[i] == bMax[i] ? 1 : 0;
			// rev order ------- start
			crsList.candidates[i].RevOrder = 0;
			for (int m = 0; m < numTests - 1; m++)
				for (int n = m + 1; n < numTests; n++)
				{
					if (vBefore[i][vRevBefore[m]] > vBefore[i][vRevBefore[n]])
						swapInt(vRevBefore, m, n);
					if (vAfter[i][vRevAfter[m]] > vAfter[i][vRevAfter[n]])
						swapInt(vRevAfter, m, n);
				}
			for (int m = 0; m < numTests - 1; m++)
				for (int n = m + 1; n < numTests; n++)
					if (vRevBefore[m] < vRevBefore[n] && vRevAfter[m] > vRevAfter[n])
						crsList.candidates[i].RevOrder ++;
			// rev order ------- end
			
			crsList.candidates[i].SameId = 0;
			crsList.candidates[i].SameParam = 0;	// this att is obsolated. possibly leads over-fitting
			for (int m = 0; m < crsList.Length(); m++)
			{
				if (crsList.getHeuristic(m) == crsList.candidates[i].id)
					crsList.candidates[i].SameId ++;
				if (crsList.getParam1(m) == crsList.candidates[i].param1)
					crsList.candidates[i].SameParam ++;
			}
			
			// param is low / med / or high
			if (crsList.candidates[i].param1 < 0.4)
				crsList.candidates[i].par = 0;
			else if (crsList.candidates[i].param1 > 0.6)
				crsList.candidates[i].par = 2;
			else 
				crsList.candidates[i].par = 1;
				
			if (i == crsList.Length() - 1)
				endNormally = true;
		}
		
		// set up orders of obj values
		for (int m = 0; m < crsList.Length() - 1; m++)
			for (int n = m + 1; n < crsList.Length(); n++)
			{
				if (bMin[bMinOrd[m]] > bMin[bMinOrd[n]])
					swapInt(bMinOrd, m, n);
				if (bMax[bMaxOrd[m]] > bMax[bMaxOrd[n]])
					swapInt(bMaxOrd, m, n);
				if (bAvg[bAvgOrd[m]] > bAvg[bAvgOrd[n]])
					swapInt(bAvgOrd, m, n);
					
				if (cMin[cMinOrd[m]] < cMin[cMinOrd[n]])
					swapInt(cMinOrd, m, n);
				if (cMax[cMaxOrd[m]] < cMax[cMaxOrd[n]])
					swapInt(cMaxOrd, m, n);
				if (cAvg[cAvgOrd[m]] < cAvg[cAvgOrd[n]])
					swapInt(cAvgOrd, m, n);
					
				if (vMin[vMinOrd[m]] > vMin[vMinOrd[n]])
					swapInt(vMinOrd, m, n);
				if (vMax[vMaxOrd[m]] > vMax[vMaxOrd[n]])
					swapInt(vMaxOrd, m, n);
				if (vAvg[vAvgOrd[m]] > vAvg[vAvgOrd[n]])
					swapInt(vAvgOrd, m, n);
			}
		// write order back to candidate
		for (int i = 0; i < crsList.Length(); i++)
		{
			crsList.candidates[bMinOrd[i]].bMin = log2(i) + 1;
			crsList.candidates[bMaxOrd[i]].bMax = log2(i) + 1;
			crsList.candidates[bAvgOrd[i]].bAvg = log2(i) + 1;
			crsList.candidates[cMinOrd[i]].cMin = log2(i) + 1;
			crsList.candidates[cMaxOrd[i]].cMax = log2(i) + 1;
			crsList.candidates[cAvgOrd[i]].cAvg = log2(i) + 1;
			crsList.candidates[vMinOrd[i]].vMin = log2(i) + 1;
			crsList.candidates[vMaxOrd[i]].vMax = log2(i) + 1;
			crsList.candidates[vAvgOrd[i]].vAvg = log2(i) + 1;
		}
		
		if (TRACELEVEL > 1)
			System.out.println();
		if (TRACELEVEL > 1)
			System.out.println();
		if (endNormally)
		{
			// sorts heuristics
			crsList.avgTestTime = (double)(System.currentTimeMillis() - testStart) / crsList.Length();
			crsList.avgTestObjValue = 0;
			for (HeuristicCandidate hc : crsList.candidates)
				crsList.avgTestObjValue += hc.objValue;
			crsList.avgTestObjValue /= crsList.Length() * numTests;
			if (currentMissionID <= 1 && !ATT_TEST && crsList.Type == ProblemDomain.HeuristicType.MUTATION)
			{
				// sort by rules
				for (int i = 0; i < crsList.Length(); i++)
				{
					HeuristicCandidate hc = crsList.candidates[i];
					boolean goodPot = false;
					if (hc.vAvg <= 2)
					{
						goodPot = true;
						// except for 
						if (hc.vMax > 3
							|| hc.bAvg > 2 && hc.cMax == 2
							|| hc.bAvg > 3 && hc.cMax >= 2)
							goodPot = false;
					}
					else 
					{
						goodPot = false;
						if (hc.vMin <= 4)
							if(hc.bMin <= 2 && hc.cMin <= 2 
								|| hc.bMin == 1 && hc.cMin > 3 && hc.RevOrder > 0
								|| hc.vAvg == 3 && hc.bMin > 2 && hc.cAvg > 3 && hc.RevOrder == 0)
								goodPot = true;
					}
/*
Mutation in first 4:

J48 pruned tree
------------------

vAvg <= 2
|   vMax <= 3
|   |   bAvg <= 2: 1 (66.0/3.0)
|   |   bAvg > 2
|   |   |   cMax <= 1: 1 (10.0/2.0)
|   |   |   cMax > 1
|   |   |   |   bAvg <= 3
|   |   |   |   |   cMax <= 2: 0 (3.0)
|   |   |   |   |   cMax > 2: 1 (11.0/2.0)
|   |   |   |   bAvg > 3: 0 (11.0/2.0)
|   vMax > 3: 0 (7.0)
vAvg > 2
|   vMin <= 4
|   |   bMin <= 2
|   |   |   cMin <= 2: 1 (2.0)
|   |   |   cMin > 2
|   |   |   |   bMin <= 1
|   |   |   |   |   RevOrder <= 0: 0 (9.0/2.0)
|   |   |   |   |   RevOrder > 0
|   |   |   |   |   |   cMin <= 3: 0 (3.0/1.0)
|   |   |   |   |   |   cMin > 3: 1 (3.0)
|   |   |   |   bMin > 1: 0 (17.0/2.0)
|   |   bMin > 2
|   |   |   cAvg <= 3: 0 (111.0/7.0)
|   |   |   cAvg > 3
|   |   |   |   vAvg <= 3
|   |   |   |   |   RevOrder <= 0: 1 (3.0)
|   |   |   |   |   RevOrder > 0: 0 (6.0/2.0)
|   |   |   |   vAvg > 3: 0 (37.0/4.0)
|   vMin > 4: 0 (20.0)

Pace Regression Model / Emprical Bayes estimator

tar = 1.0719 +
      0.0257 * param +
     -0.0918 * bAvg +
      0.0527 * cAvg +
     -0.2227 * vAvg
*/
					if (goodPot)
						hc.Predicted = 1;
					else
						hc.Predicted = 100;
				}
				for (int m = 0; m < crsList.Length() - 1; m++)
					for (int n = m + 1; n < crsList.Length(); n++)
						if (crsList.candidates[m].Predicted > crsList.candidates[n].Predicted
							|| (crsList.candidates[m].Predicted == crsList.candidates[n].Predicted
								&& crsList.candidates[m].vAvg > crsList.candidates[n].vAvg))
						{
							HeuristicCandidate hc = crsList.candidates[m];
							crsList.candidates[m] = crsList.candidates[n];
							crsList.candidates[n] = hc;
						}
			}
			else if (currentMissionID <= 1)
				crsList.Sort();
			//RecordBests(LS_TESTS, LS_TESTS + LS_TESTS * hDivList.Length(), crsList.Length() * numTests);
		}
	}
	
	private void CruiseSeaTrench(HeuristicCandidateList crsList, HeuristicCandidateList rnrList, HeuristicCandidateList coList, HeuristicCandidateList hList)
	{
		if (hasTimeExpired())
			return;
		int maxRIndex = rnrList.Length();
		int maxMIndex = crsList.Length();
		int maxCIndex = coList.Length();
			
		if (TRACELEVEL > 0) 
			System.out.print("\n\n [Cruise @ Sea Trench] start cruising (Mutations = " + maxMIndex + ", RnR = " + maxRIndex + ", Crossovers = " + maxCIndex + ")...");
			
		missionEndTime = overallEndTime;
		
		if (hList.getParam1(0) > 0)
			myProblem.setDepthOfSearch(hList.getParam1(0));
			
		HeuristicCandidateList[] lists = new HeuristicCandidateList[maxCIndex + maxMIndex * 2 + maxRIndex * 2];
		int[] heurs = new int[maxCIndex + maxMIndex * 2 + maxRIndex * 2];
		for(int i = 0; i < lists.length; i++)
		{
			lists[i] = null;
			heurs[i] = 0;
		}
		boolean improvement = false;
		
		rnrList.ResetTestedObjValues();
		crsList.ResetTestedObjValues();
		coList.ResetTestedObjValues();
		while (!hasTimeExpired())
		{
			if (TRACELEVEL > 1)
				System.out.print("\n> " + objValues[0]);
			
			boolean lesser = false;
			int sCand = 0;
			for (int i = 0; i < maxRIndex && !hasTimeExpired() && !lesser; i++)
			{
				if (rnrList.getParam1(i) > 0)
					myProblem.setIntensityOfMutation(rnrList.getParam1(i));
				for (int u = 0; u < 2 && !hasTimeExpired(); u++)
				{
					if (u == 1 && objValues[0] >= buoyInWater)
						continue;
					int crsID = LS_TESTS + LS_TESTS * hList.Length() + sCand;
					
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.applyHeuristic(rnrList.getHeuristic(i), 1 - u, memSize - 3);
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.applyHeuristic(hList.getHeuristic(0), memSize - 3, crsID);
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.getFunctionValue(crsID);
					lists[sCand] = rnrList;
					heurs[sCand] = i;
					sCand++;
					if (objValues[crsID] >= objValues[0])
						if (!hasTimeExpired())
							copy(memSize - 3, crsID);
					else if (objValues[crsID] < objValues[1])
					{
						lesser = true;
					}
				}
			}
			for (int i = 0; i < maxMIndex && !hasTimeExpired() && !lesser; i++)
			{
				if (crsList.getParam1(i) > 0)
					myProblem.setIntensityOfMutation(crsList.getParam1(i));
				for (int u = 0; u < 2 && !hasTimeExpired(); u++)
				{
					if (u == 1 && objValues[0] >= buoyInWater)
						continue;
					int crsID = LS_TESTS + LS_TESTS * hList.Length() + sCand;
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.applyHeuristic(crsList.getHeuristic(i), 1 - u, memSize - 3);
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.applyHeuristic(hList.getHeuristic(0), memSize - 3, crsID);
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.getFunctionValue(crsID);
					lists[sCand] = crsList;
					heurs[sCand] = i;
					sCand++;
					if (objValues[crsID] >= objValues[0])
						if (!hasTimeExpired())
							copy(memSize - 3, crsID);
					else if (objValues[crsID] < objValues[1])
					{
						lesser = true;
					}
				}
			}
			for (int i = 0; i < maxCIndex && !hasTimeExpired() && !lesser; i++)
			{
				if (coList.getParam1(i) > 0)
					myProblem.setIntensityOfMutation(coList.getParam1(i));
					
				for (int u = 0; u < 1 && improvement && !lesser; u++)
				{
					if (objValues[0] >= buoyInWater || objValues[1] >= buoyInWater)
						continue;
					int crsID = LS_TESTS + LS_TESTS * hList.Length() + sCand;
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.applyHeuristic(coList.getHeuristic(i), 0, 1, memSize - 3);
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.applyHeuristic(hList.getHeuristic(0), memSize - 3, crsID);
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.getFunctionValue(crsID);
					lists[sCand] = coList;
					heurs[sCand] = i;
					sCand++;
					if (objValues[crsID] >= objValues[0])
						if (!hasTimeExpired())
							copy(memSize - 3, crsID);
					else if (objValues[crsID] < objValues[1])
					{
						lesser = true;
					}
				}
			}
			if (hasTimeExpired())
				return;
			improvement = false;
			int invalid = 0;
			int crsID = 0;
			int maxCoID = -1;
			
			for (int i = 0; i < sCand && !hasTimeExpired(); i++)
			{
				crsID = LS_TESTS + LS_TESTS * hList.Length() + i;
				if ((objValues[crsID] == objValues[0] && myProblem.compareSolutions(0, crsID)) 
					|| (objValues[crsID] == objValues[1] && myProblem.compareSolutions(1, crsID)))
					invalid++;
			}
			int[] minId = new int[sCand];
			int minIdSize = 0;
			for (int i = 0; i < sCand && !hasTimeExpired(); i++)
			{
				crsID = LS_TESTS + LS_TESTS * hList.Length() + i;
				if ((objValues[crsID] == objValues[0] && myProblem.compareSolutions(0, crsID)) 
					|| (objValues[crsID] == objValues[1] && myProblem.compareSolutions(1, crsID)))
					continue;
				minId[minIdSize++] = crsID;
			}
			
			for (int i = 0; i < minIdSize - 1 && minId[i] > 0 && !hasTimeExpired(); i++)
				for (int j = i + 1; j < minIdSize && minId[j] > 0 && !hasTimeExpired(); j++)
					if (objValues[minId[i]] > objValues[minId[j]])
					{
						int swap = minId[i];
						minId[i] = minId[j];
						minId[j] = swap;
					}
			
			int maxTrials = 2;
			for (int i = minIdSize - 1; i >  maxTrials - 1 && minIdSize > 0 && !hasTimeExpired(); i--)
				if (minId[i] > 0)
				{
					minId[maxTrials - 1] = minId[i];
					break;
				}
			int candId = -1;
			for (int i = 0; i < maxTrials && minIdSize > 0 && !hasTimeExpired(); i++)
			{
				if (i < maxTrials)
					crsID = minId[i];
				else
					crsID = maxCoID;
				if (crsID < 0)
					continue;
				if (!hasTimeExpired())
					objValues[crsID] = myProblem.getFunctionValue(crsID);
				if (objValues[crsID] >= objValues[1] && !hasTimeExpired())
					DeepDive(hList, crsID);
				if (hasTimeExpired())
					return;

				boolean improved = objValues[crsID] < objValues[0] && !myProblem.compareSolutions(0, crsID) && !myProblem.compareSolutions(1, crsID);
				if (!improved)
					continue;
				
				candId = crsID - (LS_TESTS + LS_TESTS * hList.Length());
				
				improvement = true;
				missionStartTime = System.currentTimeMillis();
				
				if (objValues[crsID] < objValues[1])
				{
					copy(1, 0);
					copy(crsID, 1);
					break;
				}
				else if (objValues[crsID] <= objValues[0])
					copy(crsID, 0);
			}
			if (hasTimeExpired())
				return;
			for(boolean improved = true; candId >= 0 && improved && !hasTimeExpired();)
			{
				improved = false;
				// repeat candId
				if (lists[candId].getParam1(heurs[candId]) > 0)
					myProblem.setIntensityOfMutation(lists[candId].getParam1(heurs[candId]));
				for (int u = 0; u < 1; u++)
				{
					crsID = LS_TESTS + LS_TESTS * hList.Length() + sCand + u;
					if (lists[candId] == coList)
					{
						if (u == 1)
							break;
						else
							myProblem.applyHeuristic(lists[candId].getHeuristic(heurs[candId]), 0, 1, memSize - 3);
					}
					else
					{
						if (u == 0)
							myProblem.applyHeuristic(lists[candId].getHeuristic(heurs[candId]), 1, memSize - 3);
						else
							myProblem.applyHeuristic(lists[candId].getHeuristic(heurs[candId]), 0, memSize - 3);
					}
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.applyHeuristic(hList.getHeuristic(0), memSize - 3, crsID);
					if (!hasTimeExpired())
						objValues[crsID] = myProblem.getFunctionValue(crsID);
					if (objValues[crsID] >= objValues[0] && !lesser && !hasTimeExpired())
					{
						copy(memSize - 3, crsID);
						if (!hasTimeExpired())
							DeepDive(hList, crsID);
					}
					if (objValues[crsID] < objValues[0] && !myProblem.compareSolutions(0, crsID) && !myProblem.compareSolutions(1, crsID))
					{
						improved = true;
						if (objValues[crsID] < objValues[1])
						{
							copy(1, 0);
							copy(crsID, 1);
						}
						else if (objValues[crsID] <= objValues[0])
							copy(crsID, 0);
						break;
					}
				}
			}
			
			if (sCand == maxRIndex * 2 + maxMIndex * 2 + maxCIndex && !hasTimeExpired())
			{
				for (int i = 0; i < sCand; i++)
					lists[i].setTestedObjValues(heurs[i], objValues[LS_TESTS + LS_TESTS * hList.Length() + i]);
				rnrList.Sort();
				crsList.Sort();
				coList.Sort();
				
				if (maxRIndex > 1)
					maxRIndex -= maxRIndex / 2;
				if (maxMIndex > 1)
					maxMIndex -= maxMIndex / 2;
				if (maxCIndex > 1)
					maxCIndex -= maxCIndex / 2;
					
			}
			
			if (TRACELEVEL > 1)
				System.out.println();
		}
	}
	
	private void Cruise(HeuristicCandidateList crsList, HeuristicCandidateList rnrList, HeuristicCandidateList coList, HeuristicCandidateList hDivList, HeuristicCandidateList lDivList, int src1)
	{
		double ob1, ob2 = 0, ob3;
		int maxRIndex = rnrList.Length();
		int maxMIndex = crsList.Length();
		int maxCIndex = coList.Length();
		
		if (TRACELEVEL > 0) 
			System.out.println("\n [Cruise (Id = " + currentMissionID + ")] start cruising (Mutations = " + maxMIndex + ", RnR = " + maxRIndex + ", Crossovers = " + maxCIndex + ")..." +objValues[src1]+", "+objValues[1]+", "+objValues[memSize - 2]+" LS="+hDivList.getParam1(0) + "  poolRun="+isPoolRun);
			
		if (!ATT_TEST)
		{
			if (maxRIndex > 4)
				maxRIndex = 4;
			if (maxMIndex > 4)
				maxMIndex = 4;
			if (maxCIndex > 4)
				maxCIndex = 4;
		}
			
			
		int myHitId = 0;
			
		int allMovesC = maxCIndex + (maxCIndex >> 1) + (maxCIndex >> 2);
		int allMovesM = maxMIndex + (maxMIndex >> 1) + (maxMIndex >> 2);
		int allMovesR = maxRIndex + (maxRIndex >> 1) + (maxRIndex >> 2);
		if (ATT_TEST)
		{
			allMovesC = maxCIndex;
			allMovesM = maxMIndex;
			allMovesR = maxRIndex;
		}
		int allMoves = allMovesC + allMovesM + allMovesR;
		if (!isPoolRun)
			allMoves = allMovesC;
				
		newSolutionsFoundInmission = 0;
		
		trackStartTime = System.currentTimeMillis();
		double archive = Double.MAX_VALUE;
		for (; !hasTimeExpired();)
		{
			if (TRACELEVEL > 1)
				System.out.println("> " + objValues[src1]);
			archive = objValues[src1];
				
			
				//for (HeuristicCandidate hc : hDivList.candidates)
				//	System.out.print("\t" + hc.weight);
				//System.out.println();
				
			divingSimplified = true;
			if (!divingSimplified && (now() < 0.5 || hDivList.candidates[0].weight > 100 && lDivList.candidates[0].weight > 100))
			//	SimplifyDiving(hDivList, lDivList);
			{
				divingSimplified = true;
				int std = 10;
				int cnt = 0;
				for (HeuristicCandidate hc : hDivList.candidates)
					if (hc.weight <= std)
						cnt++;
				HeuristicCandidate[] hcs = hDivList.candidates;
				hDivList.candidates = new HeuristicCandidate[hcs.length - cnt];
				int newsize = 0;
				for (HeuristicCandidate hc : hcs)
					if (hc.weight > std)
					{
						hDivList.candidates[newsize] = hc;
						newsize ++;
					}
			}
			
			// write attributes if in ATT_TEST mode
			heuristicsAttributes = "";
			for (int i = 0; i < 3 && ATT_TEST; i++)
			{
				HeuristicCandidateList li = null;
				if (i == 0)
					li = crsList;
				else if (i == 1)
					li = rnrList;
				else
					li = coList;
				
				// ignore short candidate list
				if (li.Length() <= 4)
					continue;
				
				// order list by number of best-knowns found
				for (int j = 0; j < li.Length() - 1; j++)
					for (int k = j + 1; k < li.Length(); k++)
						if (li.candidates[j].bkHits < li.candidates[k].bkHits 
						|| li.candidates[j].bkHits == li.candidates[k].bkHits && li.candidates[j].objValue > li.candidates[k].objValue)
						{
							HeuristicCandidate hc = li.candidates[j];
							li.candidates[j] = li.candidates[k];
							li.candidates[k] = hc;
						}
				// write to interface String
				for (int j = 0; j < li.Length(); j++)
				{
					HeuristicCandidate hc = li.candidates[j];
					heuristicsAttributes += hc.type+","+hc.par+","+hc.isConst+","+hc.bMin+","+hc.bAvg+","+hc.bMax+","+hc.cMin+","+hc.cAvg+","+hc.cMax+","+hc.vMin+","+hc.vAvg+","+hc.vMax+","+hc.RevOrder+","+hc.SameId+","+(log2(j) + 1)+","+hc.bkHits+"\n";
				}
			}
			
			
			for (double bestBefore = Double.MAX_VALUE; bestBefore > objValues[src1] && !hasTimeExpired();)
			{
				bestBefore = objValues[src1];
				for (int m = 0; m < allMoves && !hasTimeExpired(); m++)
				{
					HeuristicCandidateList myList = null;
					int myId = 0;
					int snorkelingDir = 0;
					int snorkelingSnor = 0;
					// choose non-LS heuristic
					if (now() < -0.35)
					{
						// time slices
						int sumWeight = 0;
						for (HeuristicCandidate hc : coList.candidates)
							sumWeight += hc.weight;
						for (HeuristicCandidate hc : crsList.candidates)
							sumWeight += hc.weight;
						for (HeuristicCandidate hc : rnrList.candidates)
							sumWeight += hc.weight;
							
						int rnum = rng.nextInt(sumWeight);
						
						for (int k = 0; k < coList.Length(); k++)
						{
							HeuristicCandidate hc = coList.candidates[k];
							rnum -= hc.weight;
							if (rnum < 0)
							{
								myList = coList;
								myId = k;
								break;
							}
						}
						if (rnum >= 0)
						{
							for (int k = 0; k < crsList.Length(); k++)
							{
								HeuristicCandidate hc = crsList.candidates[k];
								rnum -= hc.weight;
								if (rnum < 0)
								{
									myList = crsList;
									myId = k;
									break;
								}
							}
						}
						if (rnum >= 0)
						{
							for (int k = 0; k < rnrList.Length(); k++)
							{
								HeuristicCandidate hc = rnrList.candidates[k];
								rnum -= hc.weight;
								if (rnum < 0)
								{
									myList = rnrList;
									myId = k;
									break;
								}
							}
						}
					}
					else
					{
						// early stage, balanced search
						int p = m;
						if (p < allMovesC)
						{
							myList = coList;
							if (p < maxCIndex)
								myId = p;
							else if (!ATT_TEST && p < maxCIndex + (maxCIndex >> 1))
								myId = p - maxCIndex;
							else if (!ATT_TEST)
								myId = p - maxCIndex - (maxCIndex >> 1);
						}
						else if (p < allMovesC + allMovesM)
						{
							myList = crsList;
							p -= allMovesC;
							if (p < maxMIndex)
								myId = p;
							else if (!ATT_TEST && p < maxMIndex + (maxMIndex >> 1))
								myId = p - maxMIndex;
							else if (!ATT_TEST)
								myId = p - maxMIndex - (maxMIndex >> 1);
						}
						else
						{
							myList = rnrList;
							p -= allMovesC + allMovesM;
							if (p < maxRIndex)
								myId = p;
							else if (!ATT_TEST && p < maxRIndex + (maxRIndex >> 1))
								myId = p - maxRIndex;
							else if (!ATT_TEST)
								myId = p - maxRIndex - (maxRIndex >> 1);
						}
					}
					
					if (myList == crsList)
						myHitId = 0;
					else if (myList == rnrList)
						myHitId = 1;
					else if (myList == coList)
						myHitId = 2;
					
					if (runMode == AVG_TEST_MODE && myList == coList)
						continue;
					if (ATT_TEST && myList.Length() <= 4)
						continue;
					if (myList.IsDisabled(myId))
						continue;
						
					if (myList == coList)
						snorkelingDir = BASE_CO_SNORKELING;
					else
						snorkelingDir = BASE_MURR_SNORKELING;
						
					if (isCo() && myList == coList)
					{
						if (deepWins > 0 && deepWins < deepHits)
						{
							snorkelingSnor = Math.max(deepHits / deepWins, 0);
							if (snorkelingSnor > MAX_SNORKELING)
								snorkelingSnor = MAX_SNORKELING;
						}
						else
							snorkelingSnor = MAX_SNORKELING;
							
						if (quickHits > 0 && quickHits < deepHits)
						{
							snorkelingDir += Math.max((int)(1.0/( 1.0 - (double)quickHits / deepHits)), 0);
							if (snorkelingDir > MAX_SNORKELING)
								snorkelingDir = MAX_SNORKELING;
						}
						else
							snorkelingDir = MAX_SNORKELING;
						if (TRACELEVEL > 1)
							System.out.print(snorkelingDir + "|" + snorkelingSnor + "-");
					}
					/////////////////////
					snorkelingDir = 1;
					snorkelingSnor = 1;
					if (myList == coList)
						ob1 = 1;
					else if (myList == rnrList)
						ob1 = 3;
					else
						ob1 = 2;
					////////////////////////////////
					boolean lsEnabled = Math.max(Math.max(lsHits[myHitId], dirHits[myHitId]), snorHits[myHitId]) < 10 
										|| snorHits[myHitId] + dirHits[myHitId] < lsHits[myHitId] + lsHits[myHitId];
						
					int crsID = LS_TESTS + LS_TESTS * hDivList.Length() + myId;
					if (myList.getParam1(myId) > 0)
					{
						myProblem.setIntensityOfMutation(myList.getParam1(myId));
					}
						
					boolean improvement = false;
					// boolean continious = rng.nextInt(3) == 0;
					int iter, iter2;
					
					// fast descending
					boolean compare01 = myProblem.compareSolutions(0, 1);
					boolean targetPot = false;

					objValues[memSize - 4] = -1;
					objValues[memSize - 7] = -1;
					
					int maxPid = 0;
					if (myList == coList)
					{
						if (isPoolRun)
							maxPid = 2 + poolSize / 2;
						else
							maxPid = 3;
					}
					else
					{
						maxPid = 3 + poolSize;
					}
					for (int pid = 0; pid < maxPid; pid++)
					{
						int tar = 0;
						int rtar = 0;						
						if (myList == coList)
						{
							tar = src1;
							rtar = 1;
							if (pid == 1)
								rtar = memSize - 2;
							else if (pid > 1)
								rtar = poolOffset + rng.nextInt(poolSize);
								
							if (objValues[tar] < 0 || objValues[rtar] < 0 || myProblem.compareSolutions(tar, rtar))
								continue;
						}
						else
						{
							tar = 1;
							if (pid == 0)
								tar = 1;
							else if (pid == 1)
								tar = memSize - 1;
							else if (pid == 2)
								tar = src1;
							else if (pid > 2 && poolSize > 0)
								tar = poolOffset + (pid - 2) % poolSize;
								
							if (objValues[tar] < 0)
								continue;
						}
						
						//if (objValues[0] > objValues[memSize - 2])
							snorkelingSnor = Math.max((snorkelingSnor) / maxPid, 1);
							//if (snorkelingSnor > 1)
							//	System.out.print("*");
						for (iter2 = 0; iter2 < snorkelingSnor; iter2++) 
						{
							boolean IwasInTabu;
							boolean HewasInTabu = true;
							objValues[memSize - 3] = -1;
							
							for (int iter3 = 0; iter3 < snorkelingDir && (iter3 == 0 || myList.candidates[myId].isConst == 0) && !hasTimeExpired(); iter3++)
							{
								//try
								{
									if (myList == coList)
										myProblem.applyHeuristic(myList.getHeuristic(myId), tar, rtar, memSize - 6);
									else
										myProblem.applyHeuristic(myList.getHeuristic(myId), tar, memSize - 6);
									objValues[memSize - 6] = myProblem.getFunctionValue(memSize - 6);
									ob2 = objValues[memSize - 6];
								}
								//catch(Exception e)
								{
								//	System.out.println("[Warning] Something's wrong... Let's continue...");
								//	System.out.println(e.getMessage());
								//	continue;
								}
								if (objValues[memSize - 6] < objValues[0])
								{
									copy(memSize - 6, memSize - 3);
									if (objValues[memSize - 6] <= objValues[memSize - 2])
										dirHits[myHitId]++;
									break;
								}
								
								IwasInTabu = quick.containsKey(getMD5(memSize - 6));
								if ( (HewasInTabu && !IwasInTabu)
									|| objValues[memSize - 3] < 0
									|| (myList == coList && HewasInTabu == IwasInTabu && objValues[memSize - 6] > objValues[memSize - 3])
									|| (myList != coList && HewasInTabu == IwasInTabu && objValues[memSize - 6] < objValues[memSize - 3]))
								{
									copy(memSize - 6, memSize - 3);
									HewasInTabu = IwasInTabu;
								}
							}
							if (objValues[memSize - 3] < objValues[0])
							{
								boolean snorEnabled = ((runMode == CO_ONLY_MODE || runMode == CO_BUOY_MODE) && lsEnabled) 
												|| Math.max(snorHits[myHitId], dirHits[myHitId]) < 10 
												|| dirHits[myHitId] < snorHits[myHitId] + snorHits[myHitId];
								if (snorEnabled)
								{
									double beforeSnor = objValues[memSize - 3];
									Snorkeling(lDivList, memSize - 3);
									ob3 = myProblem.getFunctionValue(memSize - 3);
									if (beforeSnor > objValues[memSize - 3])
										snorHits[myHitId]++;
								}
								break;
							}
							copy(memSize - 3, memSize - 5);
							Snorkeling(lDivList, memSize - 3);
									ob3 = myProblem.getFunctionValue(memSize - 3);
							DeepDive(hDivList, memSize - 5);
							System.out.println(ob1+","+ob2+","+ob3+","+myProblem.getFunctionValue(memSize - 5));
							
							if (objValues[memSize - 3] < objValues[0])
							{
								if (objValues[memSize - 3] <= objValues[memSize - 2])
									snorHits[myHitId]++;
								break;
							}
							else if (isBuoy() && !ATT_TEST && objValues[memSize - 5] < buoyInWater && !hasTimeExpired())
							{
								
								int extraLoop = 2 + rng.nextInt(MAX_BUOY_TESTS - 2);
								int sumBuoyHits = 0;
								boolean newBkFound = false;
								for (int depth = 0; depth < MAX_BUOY_DEPTH && !newBkFound; depth++)
								{
									buoyTabu.clear();
									for (int q = 0; q < extraLoop && !hasTimeExpired(); q++)
									{
										buoyList[q] = null;
										buoyHeu[q] = null;
										sumBuoyHits = 0;
										for (int w = 0; w < maxMIndex; w++)
											if (!buoyTabu.contains(crsList.candidates[w]))
												sumBuoyHits += crsList.candidates[w].bkHits; 
										for (int w = 0; w < maxRIndex; w++)
											if (!buoyTabu.contains(rnrList.candidates[w]))
												sumBuoyHits += rnrList.candidates[w].bkHits;
											
										int rn = rng.nextInt(sumBuoyHits);
										for (int w = 0; w < maxMIndex && rn >= 0; w++)
										{
											if (!buoyTabu.contains(crsList.candidates[w]))
												rn -= crsList.candidates[w].bkHits;
											if (rn < 0)
											{
												buoyList[q] = crsList;
												buoyHeu[q] = buoyList[q].candidates[w];
											}
										}
										for (int w = 0; w < maxRIndex && rn >= 0; w++)
										{
											if (!buoyTabu.contains(rnrList.candidates[w]))
												rn -= rnrList.candidates[w].bkHits;
											if (rn < 0)
											{
												buoyList[q] = rnrList;
												buoyHeu[q] = buoyList[q].candidates[w];
											}
										}
										if (buoyList[q] == null || buoyHeu[q] == null)
											continue;
											
										if (buoyHeu[q].param1 > 0)
											myProblem.setIntensityOfMutation(buoyHeu[q].param1);
										int srcSol = 0;
										if (depth == 0)
											srcSol = memSize - 5;
										else
											srcSol = memSize - 8;
										if (!hasTimeExpired())
										{
											objValues[buoyOffset + q] = myProblem.applyHeuristic(buoyHeu[q].id, srcSol, buoyOffset + q);
										}
										if (buoyHeu[q].isConst == 1)
											buoyTabu.add(buoyHeu[q]);
									}
									// find least & greatest (but within "buoyInWater" level)
									int selectedHeu = 0;
									copy(buoyOffset, memSize - 6);
									copy(buoyOffset, memSize - 8);
									copy(buoyOffset, memSize - 9);
									for (int q = 1; q < extraLoop && !hasTimeExpired(); q++)
									{
										if (objValues[buoyOffset + q] < objValues[memSize - 6])
										{
											copy(buoyOffset + q, memSize - 6);
											copy(buoyOffset + q, memSize - 8);
											selectedHeu = q;
										}
										if (depth < MAX_BUOY_DEPTH 
											&& objValues[buoyOffset + q] > objValues[memSize - 9]
											&& objValues[buoyOffset + q] < buoyInWater)
											copy(buoyOffset + q, memSize - 9);
									}
									if (!hasTimeExpired())
										Snorkeling(lDivList, memSize - 6);
									if (objValues[memSize - 6] < objValues[0])
									{
										copy(memSize - 6, memSize - 3);
										copy(memSize - 8, memSize - 5);
										if (objValues[memSize - 6] <= objValues[memSize - 2])
											snorHits[myHitId]++;
										targetPot = true;
										newBkFound = true;
										if (TRACELEVEL > 0)
										{
											System.out.print(" > BK found from BUOY restart. ");
											System.out.print(buoyList[selectedHeu] == rnrList ? " RR " : " MU ");
											System.out.println(buoyHeu[selectedHeu].id + "#");
										}
										break;
									}
									else if (objValues[memSize - 6] < objValues[memSize - 3] 
										&& objValues[memSize - 6] < objValues[memSize - 8])
									{
										copy(memSize - 6, memSize - 3);
										copy(memSize - 8, memSize - 5);
										if (TRACELEVEL > 0)
										{
											System.out.print(" > solution advised by BUOY restart. ");
											System.out.print(buoyList[selectedHeu] == rnrList ? " RR " : " MU ");
											System.out.println(buoyHeu[selectedHeu].id + "#");
										}
									}
								}
							}
						}
						
						if (objValues[memSize - 3] < objValues[0])
						{
							copy(memSize - 3, crsID);
							break;
						}
							
						boolean IHaveARepair = objValues[memSize - 5] > objValues[memSize - 3] && !quick.containsKey(getMD5(memSize - 3));
						if ((!targetPot && IHaveARepair) || (objValues[crsID] > objValues[memSize - 3] && IHaveARepair == targetPot))
						{
							copy(memSize - 3, crsID);
							copy(memSize - 5, memSize - 7);
							targetPot = IHaveARepair;
						}
						if (pid == 0 || (objValues[memSize - 3] > objValues[memSize - 4] && IHaveARepair))
						{
							copy(memSize - 3, memSize - 4);
						}
					}
					
					boolean lsMinEnabled = lsEnabled || targetPot || snorHits[myHitId] + dirHits[myHitId] < lsHits[myHitId] * 10;
					boolean srcLS = false;
					if (objValues[crsID] >= objValues[0])
					{
						boolean sameTwoSolution = objValues[memSize - 4] > 0 ? myProblem.compareSolutions(crsID, memSize - 4) : true;
						
						//if (lsMinEnabled)
							DeepDive(hDivList, crsID);
						
						System.out.println();
						if (!sameTwoSolution && objValues[crsID] >= objValues[0] && objValues[memSize - 4] > 0)
						{
							if (lsEnabled)
								DeepDive(hDivList, memSize - 4);
							if (objValues[memSize - 4] < objValues[crsID])
								swap(crsID, memSize - 4);
								
							if (deepHits > 100 && deepHits > deepWins * 4 && objValues[crsID] >= objValues[0] && objValues[memSize - 7] > 0)
							{
								if (lsEnabled)
									DeepDive(hDivList, memSize - 7);
								if (objValues[memSize - 7] < objValues[crsID])
									swap(crsID, memSize - 7);
							}
						}
						srcLS = true;
					}
					else
					{
						if (lsMinEnabled)
						{
							double beforeLS = objValues[crsID];
							DeepDive(hDivList, crsID);
							if (beforeLS > objValues[crsID])
								srcLS = true;
						}
					}
					
					if (objValues[crsID] == 0)
					{
						// assume no 0 solutions, to be verified.
						if (TRACELEVEL > 0)
							System.out.println("[Warning] obj Value = 0, ignored. any error ?");
						objValues[crsID] = myProblem.getFunctionValue(crsID);
						hasTimeExpired();
						if (objValues[crsID] == 0)
						{
							myList.SetDisabled(myId);
							if (TRACELEVEL > 0)
								System.out.println("[Warning] obj Value = 0 confirmed. Heuristic #" + myList.getHeuristic(myId) +" is temporarily disabled. Possibly a bug here.");
						}
					}
					
					boolean improved = objValues[crsID] < objValues[src1] 
									&& (objValues[crsID] != objValues[0] || !myProblem.compareSolutions(0, crsID)) 
									&& (objValues[crsID] != objValues[memSize - 2] || !myProblem.compareSolutions(memSize - 2, crsID));
					
					if (!improved)
						continue;
						
					if (improved && src1 != 0)
					{
						if (objValues[crsID] < objValues[src1])
							copy(crsID, src1);
						if (objValues[crsID] >= objValues[0])
							continue;
					}
						
					AddPool(crsID);
					improvement = true;
					trackStartTime = System.currentTimeMillis();
					newSolutionsFoundInmission ++;
					
					copy(crsID, 0);
					if (srcLS && objValues[0] < objValues[memSize - 2])
						lsHits[myHitId]++;
						
					if (TRACELEVEL > 0)
						System.out.println("ls="+lsHits[myHitId]+" dir="+dirHits[myHitId]+" snor="+snorHits[myHitId]);
						
					if (SUB_TEST || SUB_APPLY)
					{
						int hid = myList.candidates[myId].id;
						int para = 0;
						if (myList.candidates[myId].param1 == HeuristicCandidateList.PARAM_MED)
							para = 1;
						else if (myList.candidates[myId].param1 == HeuristicCandidateList.PARAM_HIGH)
							para = 2;
						subImpCnt[hid * 3 + para] ++;
					}
					if (!improvement)
						continue;
					if (TRACELEVEL > 0)
						System.out.println(">" + objValues[0] + " by Heurusitc #" + myList.getHeuristic(myId) + " (" + myList.getType() + ") ls="+lsEnabled+".");
						
					myList.Weight += 1;
					myList.candidates[myId].weight += 1;
					
					if (objValues[1] > objValues[0])
					{
						if (objValues[1] < objValues[memSize - 2])
							swap(memSize - 2, 1);
						swap(0, 1);
						AddPool(1);
						if (isPoolRun)
							copy(1, 0);
						lastBestTime = System.currentTimeMillis();
						lastBestMission = currentMissionID;
						
						myList.Weight += 4;
						myList.candidates[myId].weight += 4;
						myList.candidates[myId].bkHits ++;
						bestSolutionHits = 1;
					}
					else if (objValues[1] == objValues[0] && rng.nextInt(3) == 0)
					{
						swap(0, 1);
						AddPool(1);
						bestSolutionHits ++;
					}
					else if (objValues[memSize - 2] > objValues[0] && !myProblem.compareSolutions(0, 1) && !isSeaTrench())
					{
						swap(0, memSize - 2);
						if (objValues[memSize - 2] <= objValues[1] * 1.1)
							AddPool(memSize - 2);
						if (objValues[1] == objValues[0])
							bestSolutionHits ++;
					}
					
					myList.MoveToHead(myId);
					
					if (isTest() && bestTestedTwo > objValues[1] + Math.min(objValues[0], objValues[memSize - 2]))
						bestTestedTwo = objValues[1] + Math.min(objValues[0], objValues[memSize - 2]);
					
					if (TRACELEVEL > 1)
						System.out.println();
						
					long missionTime = System.currentTimeMillis() - missionStartTime;
					if (isTest() && missionTime > 0.1 * overallTime || hasTimeExpired())
						return;
				}
				if (hasTimeExpired())
					return;
				if (isPoolRun)
					break;
			}
			
			if (hasTimeExpired())
				return;
			
			if (!isPoolRun && archive > objValues[src1])
				continue;
				
			double sinceLastBest = (double)(System.currentTimeMillis() - lastBestTime) / overallTime;
			double MAXsince = 0.02;
			if (objValues[0] <= objValues[memSize - 2])
				MAXsince = 0.05;
			long idleTime = System.currentTimeMillis() - trackStartTime;
			long maxIdleTime = trackStartTime - missionStartTime;
			long missionTime = (idleTime + maxIdleTime) / overallTime;
			//double factor = 0.0;
			double testNow = ((double)System.currentTimeMillis() - LSMURRtestStartTime) / overallTime;

			
			if ((runMode == AVG_BUOY_MODE && isPoolRun && sinceLastBest >= MAXsince) 
				||  (runMode == AVG_TEST_MODE &&  testNow >= 0.1 ))
			{
				if (runMode != AVG_TEST_MODE)
					poolSize = 1;
				InitSolutions(2);
				DeepDive(hDivList, 0);
				AddPool(0);
				copy(0, memSize - 2);
				DeepDive(hDivList, 1);
				AddPool(1);
				if (objValues[1] > objValues[memSize - 2])
					swap(1, memSize - 2);
				lastBestMission = currentMissionID;
				lastBestTime = System.currentTimeMillis();
			
				isPoolRun = false;
				return;
			}
			if ((runMode == AVG_BUOY_MODE && isPoolRun && (sinceLastBest < MAXsince)) 
				|| (runMode == AVG_TEST_MODE && testNow < 0.1))
				continue;
			
			if (runMode == CO_TEST_MODE && testNow >= 0.1)
			{
				
				if (objValues[1] > objValues[memSize - 2])
					swap(1, memSize - 2);
				lastBestMission = currentMissionID;
				lastBestTime = System.currentTimeMillis();
				return;
			}
			if (isCo() && !isPoolRun && sinceLastBest >= waitTime)
			{
				poolSize = 1;
				InitSolutions(2);
				DeepDive(hDivList, 0);
				AddPool(0);
				copy(0, memSize - 2);
				DeepDive(hDivList, 1);
				AddPool(1);
				
				if (objValues[1] > objValues[memSize - 2])
					swap(1, memSize - 2);
				lastBestMission = currentMissionID;
				lastBestTime = System.currentTimeMillis();
				return;
			}
			
			if (!isPoolRun)
				break;
			
		}
		
		if (objValues[memSize - 2] < 0 || (objValues[0] != objValues[1] && objValues[memSize - 2] > objValues[0]))
			copy(0, memSize - 2);
			
		else if (objValues[0] == objValues[memSize - 2] && !myProblem.compareSolutions(0, 1) && rng.nextInt(2) == 0)
		{
			swap(0, memSize - 2);
		}
			double sinceLastBest = (double)(System.currentTimeMillis() - lastBestTime) / overallTime;
		if (!isPoolRun && currentMissionID > lastBestMission + 3)
		{
			if (objValues[memSize - 2] == objValues[1])
				copy(memSize - 2, 1);
			
			InitSolutions(1);
			DeepDive(hDivList, 0);
			copy(0, memSize - 2);
			AddPool(memSize - 2);
			lastBestMission = currentMissionID;
		}
		return;
	}
	
	protected void summerizeSub()
	{
		int[] finds = subImpCnt;
		int best = 0;
		int nextbest = -1;
		for (int m = 1; m < finds.length; m++)
		{
			if (finds[best] < finds[m])
			{
				nextbest = best;
				best = m;
			}
			else if (nextbest < 0 || finds[nextbest] < finds[m])
			{
				nextbest = m;
			}
		}
		
		for (int j =0; j < 3 ; j++)
		{
			HeuristicCandidateList lst = coList;
			if (j == 1)
				lst = crsList;
			else if (j ==2)
				lst = rnrList;
				
			int goodCnt = 0;
			for (int cid = 0; cid < lst.Length(); cid++)
			{
				int m = lst.candidates[cid].id * 3;
				if (lst.candidates[cid].param1 == HeuristicCandidateList.PARAM_MED)
					m ++;
				else if (lst.candidates[cid].param1 == HeuristicCandidateList.PARAM_HIGH)
					m += 2;
				if (m != best && m != nextbest)
					lst.SetDisabled(cid);
				else
				{
					lst.setTestedObjValues(cid, -finds[m]);
					goodCnt ++;
				}
			}
			lst.Sort();
			HeuristicCandidate[] tmphl = lst.candidates;
			lst.candidates = new HeuristicCandidate[goodCnt];
			for (int cc = 0; cc < goodCnt; cc++)
				lst.candidates[cc] = tmphl[cc];
		}
		if (coList.candidates.length == 0)
			modeToPersist = AVG_BUOY_MODE;
		else if (crsList.candidates.length + rnrList.candidates.length == 0)
			modeToPersist = CO_ONLY_MODE;
		else
			modeToPersist = CO_BUOY_MODE;
		return;
	}
	
	private void copy(int src, int dest)
	{
		try
		{
			myProblem.copySolution(src, dest);
			objValues[dest] = objValues[src];
		}
		catch(Exception e)
		{
			System.out.println("[Error] Unable to copy solution from "+ src + " to " + dest + ".");
			System.out.println(e.getMessage());
		}
	}
	
	private void swap(int index1, int index2)
	{
		copy(index1, memSize - 1);
		copy(index2, index1);
		copy(memSize - 1, index2);
	}
	
	private double now()
	{
		return (double)(overallEndTime - System.currentTimeMillis()) / overallTime;
	}
	
	private boolean isBuoy()
	{
		return (runMode & BUOY_MASK) > 0;
	}
	
	private boolean isTest()
	{
		return (runMode & TEST_MASK) > 0;
	}
	
	private boolean isAvg()
	{
		return (runMode & AVG_MASK) > 0;
	}
	
	private boolean isCo()
	{
		return (runMode & CO_MASK) > 0;
	}
	
	private boolean isSeaTrench()
	{
		return (runMode & SEA_TRENCH_MASK) > 0;
	}
	
	private boolean isSlowLS()
	{
		return (runMode & SLOW_LS_MASK) > 0;
	}
	
	private String getMD5(int index)
	{
		try
		{
			String s = myProblem.solutionToString(index);
			md5.Init();
			md5.Update(s, null);
			return md5.asHex();
		}
		catch (Exception e)
		{
			return "ERROR";
		}
	}
	
	private void AddPool(int index)
	{
		int p = -1;
		double objV = 0;
		double factor = 1.1;
		if (poolSize == MAX_POOL_SIZE)
			factor = 1;
		for (int i = 0; i < poolSize; i++)
		{
			if (myProblem.compareSolutions(index, poolOffset + i))
				return;
			if (objValues[poolOffset + i] > objV && objValues[poolOffset + i] > objValues[index] * factor)
			{
				p = i;
				objV = objValues[poolOffset + i];
			}
		}
		if (p < 0 && poolSize < MAX_POOL_SIZE)
		{
			p = poolSize;
			poolSize ++;
		}
		if (p >= 0)
		{
			copy(index, poolOffset + p);
			if (TRACELEVEL > 1)
				System.out.println(" < Adding solution to pool (obj = " + objValues[index] + ", pool size = " + poolSize + ")");
		}
	}
	
	private void swapInt(int[] arr, int id1, int id2)
	{
		int swapI = arr[id1];
		arr[id1] = arr[id2];
		arr[id2] = swapI;
	}
	
	private int log2(int v)
	{
		if (v <= 1)
			return 0;
		
		int ret = 0;
		while (v > 1)
		{
			ret ++;
			v /= 2;
		}
		return ret;
	}
	
	/**
	 * this method provides a name
	 * @return a string representing the name of the hyper-heuristic
	 */
	public String toString()
	{
		return " Pearl Hunter " + VERSION + "\nby Fan Xue @ Hong Kong Polytechnic University";
	}
	
	class QuickAttributes
	{
		public int count;
		public int index;
		
		public QuickAttributes()
		{
			count = 0;
			index = -1;
		}
	}
	
	/**
	 * This class is a supporting class of Pearl Hunter. Some information are stored in this class.
	 * <p>
	 * Note that one heuristic with different parameters (param1) are considered as different candidates.
	 *
	 * @author Fan Xue <dewolf_matri_x@msn.com> at Hong Kong Polytechnic University
	 * @version 0.0.2
	 * @see PearlHunter
	 */
	class HeuristicCandidate implements Comparable
	{
		int id;
		double param1;
		double objValue;
		long timeCost;
		boolean disabled;
		int weight;
		int bkHits;
		
		int type;
		int par;
		int isConst;
		int bMin;	// direct value
		int bAvg;
		int bMax;
		int cMin;	// amount of improvement
		int cAvg;
		int cMax;
		int vMin;	// value after LS
		int vAvg;
		int vMax;
		int RevOrder;	// reverse order
		int SameId;
		int SameParam;
		int Predicted;
		
		
		/**
		 * Constructs an object
		 */
		public HeuristicCandidate()
		{
			this(-1);
		}
		
		/**
		 * Constructs an object with a given heuristic index
		 */
		public HeuristicCandidate(int index)
		{
			id = index;
			param1 = 0;
			objValue = 0;
			timeCost = 0;
			weight = 1;
			bkHits = 10;
			disabled = false;
			
			type = 0;
			par = 0;
			isConst = 0;
			bMin = 1;	// direct value
			bAvg = 1;
			bMax = 1;
			cMin = 1;	// amount of improvement
			cAvg = 1;
			cMax = 1;
			vMin = 1;	// value after LS
			vAvg = 1;
			vMax = 1;
			RevOrder = 1;
			SameId = 1;
			SameParam = 1;
			Predicted = 0;
		}
		
		/**
		 * this method implements the compareTo()
		 * @return whether it's inferior than a given heuristic candiadte
		 */
		public int compareTo(Object anotherCandidate)
		{
			if(!(anotherCandidate instanceof HeuristicCandidate))
				throw new ClassCastException("Invalid object");
		   
			double value = ((HeuristicCandidate) anotherCandidate).objValue;
		   
			if(objValue > value)
				return 1;
			else if (objValue < value)
				return -1;
			else
				return 0;
		}
	}
	/**
	 * This class is a supporting class of Pearl Hunter. Heuristic candidates are stored in list in this class.
	 * <p>
	 * List creation, sorting, and time slice control are implemented.
	 *
	 * @author Fan Xue <dewolf_matri_x@msn.com> at Hong Kong Polytechnic University
	 * @version 0.0.1
	 * @see PearlHunter
	 */
	public class HeuristicCandidateList
	{
		private int bestID;
		private int nBestID;
		
		public ProblemDomain.HeuristicType Type;
		public int Weight;
		public double avgTestTime;
		public double avgTestObjValue;
		public HeuristicCandidate[] candidates;
		public static final double PARAM_LS_LOW = 0.1;
		public static final double PARAM_LS_HIGH = 1.0;
		public static final double PARAM_ZERO = 0;
		public static final double PARAM_LOW = 0.2;
		public static final double PARAM_MED = 0.5;
		public static final double PARAM_HIGH = 0.8;
		public static final double PARAM_ALL = -1;
		
		/**
		 * construts a condidate list with a given type (inclusive or exclusive) and a given parameter
		 * @param p the problem domain
		 * @param t type of heuristics
		 * @param mode inclusive or exclusive
		 * @param params parameter (Low/0.1, MED/0.5, HIGH/1.0, or Both Low and MED)
		 */
		public HeuristicCandidateList(ProblemDomain p, ProblemDomain.HeuristicType t, double params) 
		{
			int[] theList;
			int[] highParamList;
			int ineligible = 0;
			HeuristicCandidate c;
			HeuristicCandidate[] possibleCandidates;
			
			Type = t;
			Weight = 10;
			avgTestTime = 1;
			avgTestObjValue = 1;
			if (p == null)
				return;
				
			int hType = 4;
			if (Type == ProblemDomain.HeuristicType.LOCAL_SEARCH)
				hType = 3;
			else if (Type == ProblemDomain.HeuristicType.CROSSOVER)
				hType = 2;
			else if (Type == ProblemDomain.HeuristicType.RUIN_RECREATE)
				hType = 1;
			else if (Type == ProblemDomain.HeuristicType.MUTATION)
				hType = 0;
				
			try
			{
				theList = p.getHeuristicsOfType(t);
				highParamList = null;
				if (t == ProblemDomain.HeuristicType.LOCAL_SEARCH)
					highParamList = myProblem.getHeuristicsThatUseDepthOfSearch();
				else if (t == ProblemDomain.HeuristicType.MUTATION)
					highParamList = myProblem.getHeuristicsThatUseIntensityOfMutation();
				//System.out.println(theList.length + "-" + highParamList.length);
				if (params == PARAM_ALL)
				{
					int pcSize = 0;
					for(int i = 0; i < theList.length; i++)
					{
						boolean eligible = false;
						if (highParamList != null)
						{
							for (int j = 0; j < highParamList.length && !eligible; j++)
								if (theList[i] == highParamList[j])
									eligible = true;
						}
						if (eligible)
							pcSize += 3;
						else
							pcSize ++;
					}
					
					possibleCandidates = new HeuristicCandidate[pcSize];
				}
				else
					possibleCandidates = new HeuristicCandidate[theList.length];
					
				int pcSize = 0;
				for(int i = 0; i < theList.length; i++)
				{
					if (params != PARAM_ALL)
					{
						// one param
						boolean eligible = false;
						if (highParamList != null)
						{
							for (int j = 0; j < highParamList.length && !eligible; j++)
								if (theList[i] == highParamList[j])
									eligible = true;
						}
						if (eligible)
						{
							c = new HeuristicCandidate(theList[i]);
							c.param1 = params;
							c.type = hType;
							possibleCandidates[i] = c;
						}
						else
						{
							c = new HeuristicCandidate(theList[i]);
							c.param1 = params;
							c.type = hType;
							possibleCandidates[i] = c;
						}
					}
					else
					{
						boolean eligible = false;
						if (highParamList != null)
						{
							for (int j = 0; j < highParamList.length && !eligible; j++)
								if (theList[i] == highParamList[j])
									eligible = true;
						}
						if (eligible)
						{
							c = new HeuristicCandidate(theList[i]);
							c.param1 = PARAM_LOW;
							c.type = hType;
							possibleCandidates[pcSize] = c;
							pcSize++;
							c = new HeuristicCandidate(theList[i]);
							c.param1 = PARAM_MED;
							c.type = hType;
							possibleCandidates[pcSize] = c;
							pcSize++;
							c = new HeuristicCandidate(theList[i]);
							c.param1 = PARAM_HIGH;
							c.type = hType;
							possibleCandidates[pcSize] = c;
							pcSize++;
						}
						else
						{
							c = new HeuristicCandidate(theList[i]);
							c.param1 = PARAM_MED;
							c.type = hType;
							possibleCandidates[pcSize] = c;
							pcSize++;
						}
					}
				}
				
				candidates = possibleCandidates;
			}
			catch(Exception e)
			{
				candidates = new HeuristicCandidate[0];
				System.out.println("[Warning] Cannot build candidate list with the given heuristic type.");
				System.out.println(e.getMessage());
			}
		}
		
		public String getType()
		{
			if (Type == ProblemDomain.HeuristicType.LOCAL_SEARCH)
				return "LS";
			else if (Type == ProblemDomain.HeuristicType.CROSSOVER)
				return "CO";
			else if (Type == ProblemDomain.HeuristicType.MUTATION)
				return "MU";
			else if (Type == ProblemDomain.HeuristicType.RUIN_RECREATE)
				return "RR";
			System.out.println("[Warning] An unknown heuristic type.");
			return "Unknown";
		}
		
		/**
		 * this method returns the length of the candidate list
		 * @return how many heuristic candiadtes are stored
		 */
		public int Length()
		{
			if (candidates == null)
				return 0;
			return candidates.length;
		}
		
		/**
		 * this method resets performances and time slices to 0
		 */
		public void ResetTestedObjValues()
		{
			if (candidates == null)
				return;
			
			for (int i = 0; i < candidates.length; i++)
			{
				if (candidates[i] == null)
					continue;
				candidates[i].objValue = 0;
			}
		}
		
		/**
		 * this method returns the heuristic ID of the given index
		 * @param index index of heuristic
		 * @return heuristic ID 
		 */
		public int getHeuristic(int index)
		{
			if (candidates == null)
				return 0;
			if (index >= candidates.length)
				return 0;
			if (candidates[index] == null)
				return 0;
			return candidates[index].id;
		}
		
		/**
		 * this method returns the parameter of the given index
		 * @param index index of heuristic
		 * @return parameter of the heuristic 
		 */
		public double getParam1(int index)
		{
			if (candidates == null)
				return 0.1;
			if (index >= candidates.length)
				return 0.1;
			if (candidates[index] == null)
				return 0.1;
			return candidates[index].param1;
		}
		
		/**
		 * this method assigns the performance value of a given index
		 * @param index index of heuristic
		 * @param value performance value
		 */
		public void setTestedObjValues(int index, double value)
		{
			if (candidates == null || candidates.length <= index || candidates[index] == null)
			{
				return;
			}
			candidates[index].objValue += value;
		}
		
		public void setTestedTime(int index, long value)
		{
			if (candidates == null || candidates.length <= index || candidates[index] == null)
			{
				return;
			}
			candidates[index].timeCost += value;
		}
		
		
		/**
		 * this method sorts the candidates and sets up time slices
		 */
		public void Sort()
		{
			Arrays.sort(candidates);
		}
		
		/**
		 * this method move the given candidate to head
		 * @param index index of the heuristic to be moved to head
		 */
		public void MoveToHead(int index)
		{
			if (index == 0)
				return;
			HeuristicCandidate c = candidates[index];
			for (int i = index; i > 0; i--)
				candidates[i] = candidates[i - 1];
			candidates[0] = c;
		}
	
		public void CheckOrder(int index)
		{
			if (candidates == null || candidates.length <= index || index == 0)
				return;
			if (candidates[index].weight < candidates[index - 1].weight)
				return;
				
			HeuristicCandidate hc = candidates[index];
			int dest = -1;
			for (int i = index - 1; i >= 0; i--)
			{
				if (hc.weight > candidates[i].weight)
				{
					candidates[i + 1] = candidates[i];
					dest = i;
				}
				else
					break;
			}
			if (dest > 0)
				candidates[dest] = hc;
		}
		
		/**
		 * this method disables a candidate
		 * @param index index of heuristic to be disabled
		 */
		public void SetDisabled(int index)
		{
			candidates[index].disabled = true;
		}
	
		/**
		 * this method returns whether a candidate is disabled
		 * @param index index of heuristic
		 * @return disabled?
		 */
		public boolean IsDisabled(int index)
		{
			return candidates[index].disabled;
		}
	}

	/**
	 * Fast implementation of RSA's MD5 hash generator.
	 * <p>
	 * Originally written by Santeri Paavolainen, Helsinki Finland 1996.<br>
	 * Many changes Copyright (c) 2002 - 2010 Timothy W Macinta<br>
	 * Simplified by Fan Xue 2011
	 * <p>
	 *
	 * @author Santeri Paavolainen <sjpaavol@cc.helsinki.fi>
	 * @author Timothy W Macinta (twm@alum.mit.edu) (optimizations and bug fixes)
	 * @author Fan Xue <dewolf_matri_x@msn.com> at Hong Kong Polytechnic University (simplified)
	 */
	class MD5 {
		
		/**
		 * MD5 state
		 **/
		MD5State state;
		
		/**
		 * If Final() has been called, finals is set to the current finals
		 * state. Any Update() causes this to be set to null.
		 **/
		MD5State finals;
		
		/** 
		 * Padding for Final()
		 **/
		final byte padding[] = {
			(byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		};
		
		private final boolean native_lib_loaded = false;
		
		/**
		 * Initialize MD5 internal state (object can be reused just by
		 * calling Init() after every Final()
		 **/
		public synchronized void Init () {
			state = new MD5State();
			finals = null;
		}
		
		/**
		 * Class constructor
		 **/
		public MD5 () {
			this.Init();
		}
		
		/**
		 * Initialize class, and update hash with ob.toString()
		 *
		 * @param ob Object, ob.toString() is used to update hash
		 *           after initialization
		 **/
		public MD5 (Object ob) {
			this();
			Update(ob.toString());
		}
		
		private void Decode (byte buffer[], int shift, int[] out) {
			/*len += shift;
			for (int i = 0; shift < len; i++, shift += 4) {
				out[i] = ((int) (buffer[shift] & 0xff)) |
					(((int) (buffer[shift + 1] & 0xff)) << 8) |
					(((int) (buffer[shift + 2] & 0xff)) << 16) |
					(((int)  buffer[shift + 3]) << 24);
			}*/
			
			// unrolled loop (original loop shown above)
			
			out[0] = ((int) (buffer[shift] & 0xff)) |
				(((int) (buffer[shift + 1] & 0xff)) << 8) |
				(((int) (buffer[shift + 2] & 0xff)) << 16) |
				(((int)  buffer[shift + 3]) << 24);
			out[1] = ((int) (buffer[shift + 4] & 0xff)) |
				(((int) (buffer[shift + 5] & 0xff)) << 8) |
				(((int) (buffer[shift + 6] & 0xff)) << 16) |
				(((int)  buffer[shift + 7]) << 24);
			out[2] = ((int) (buffer[shift + 8] & 0xff)) |
				(((int) (buffer[shift + 9] & 0xff)) << 8) |
				(((int) (buffer[shift + 10] & 0xff)) << 16) |
				(((int)  buffer[shift + 11]) << 24);
			out[3] = ((int) (buffer[shift + 12] & 0xff)) |
				(((int) (buffer[shift + 13] & 0xff)) << 8) |
				(((int) (buffer[shift + 14] & 0xff)) << 16) |
				(((int)  buffer[shift + 15]) << 24);
			out[4] = ((int) (buffer[shift + 16] & 0xff)) |
				(((int) (buffer[shift + 17] & 0xff)) << 8) |
				(((int) (buffer[shift + 18] & 0xff)) << 16) |
				(((int)  buffer[shift + 19]) << 24);
			out[5] = ((int) (buffer[shift + 20] & 0xff)) |
				(((int) (buffer[shift + 21] & 0xff)) << 8) |
				(((int) (buffer[shift + 22] & 0xff)) << 16) |
				(((int)  buffer[shift + 23]) << 24);
			out[6] = ((int) (buffer[shift + 24] & 0xff)) |
				(((int) (buffer[shift + 25] & 0xff)) << 8) |
				(((int) (buffer[shift + 26] & 0xff)) << 16) |
				(((int)  buffer[shift + 27]) << 24);
			out[7] = ((int) (buffer[shift + 28] & 0xff)) |
				(((int) (buffer[shift + 29] & 0xff)) << 8) |
				(((int) (buffer[shift + 30] & 0xff)) << 16) |
				(((int)  buffer[shift + 31]) << 24);
			out[8] = ((int) (buffer[shift + 32] & 0xff)) |
				(((int) (buffer[shift + 33] & 0xff)) << 8) |
				(((int) (buffer[shift + 34] & 0xff)) << 16) |
				(((int)  buffer[shift + 35]) << 24);
			out[9] = ((int) (buffer[shift + 36] & 0xff)) |
				(((int) (buffer[shift + 37] & 0xff)) << 8) |
				(((int) (buffer[shift + 38] & 0xff)) << 16) |
				(((int)  buffer[shift + 39]) << 24);
			out[10] = ((int) (buffer[shift + 40] & 0xff)) |
				(((int) (buffer[shift + 41] & 0xff)) << 8) |
				(((int) (buffer[shift + 42] & 0xff)) << 16) |
				(((int)  buffer[shift + 43]) << 24);
			out[11] = ((int) (buffer[shift + 44] & 0xff)) |
				(((int) (buffer[shift + 45] & 0xff)) << 8) |
				(((int) (buffer[shift + 46] & 0xff)) << 16) |
				(((int)  buffer[shift + 47]) << 24);
			out[12] = ((int) (buffer[shift + 48] & 0xff)) |
				(((int) (buffer[shift + 49] & 0xff)) << 8) |
				(((int) (buffer[shift + 50] & 0xff)) << 16) |
				(((int)  buffer[shift + 51]) << 24);
			out[13] = ((int) (buffer[shift + 52] & 0xff)) |
				(((int) (buffer[shift + 53] & 0xff)) << 8) |
				(((int) (buffer[shift + 54] & 0xff)) << 16) |
				(((int)  buffer[shift + 55]) << 24);
			out[14] = ((int) (buffer[shift + 56] & 0xff)) |
				(((int) (buffer[shift + 57] & 0xff)) << 8) |
				(((int) (buffer[shift + 58] & 0xff)) << 16) |
				(((int)  buffer[shift + 59]) << 24);
			out[15] = ((int) (buffer[shift + 60] & 0xff)) |
				(((int) (buffer[shift + 61] & 0xff)) << 8) |
				(((int) (buffer[shift + 62] & 0xff)) << 16) |
				(((int)  buffer[shift + 63]) << 24);
		}
		
		private native void Transform_native (int[] state, byte buffer[], int shift, int length);
		
		private void Transform (MD5State state, byte buffer[], int shift, int[] decode_buf) {
			int
				a = state.state[0],
				b = state.state[1],
				c = state.state[2],
				d = state.state[3],
				x[] = decode_buf;
			
			Decode(buffer, shift, decode_buf);
			
			/* Round 1 */
			a += ((b & c) | (~b & d)) + x[ 0] + 0xd76aa478; /* 1 */
			a = ((a << 7) | (a >>> 25)) + b;
			d += ((a & b) | (~a & c)) + x[ 1] + 0xe8c7b756; /* 2 */
			d = ((d << 12) | (d >>> 20)) + a;
			c += ((d & a) | (~d & b)) + x[ 2] + 0x242070db; /* 3 */
			c = ((c << 17) | (c >>> 15)) + d;
			b += ((c & d) | (~c & a)) + x[ 3] + 0xc1bdceee; /* 4 */
			b = ((b << 22) | (b >>> 10)) + c;
			
			a += ((b & c) | (~b & d)) + x[ 4] + 0xf57c0faf; /* 5 */
			a = ((a << 7) | (a >>> 25)) + b;
			d += ((a & b) | (~a & c)) + x[ 5] + 0x4787c62a; /* 6 */
			d = ((d << 12) | (d >>> 20)) + a;
			c += ((d & a) | (~d & b)) + x[ 6] + 0xa8304613; /* 7 */
			c = ((c << 17) | (c >>> 15)) + d;
			b += ((c & d) | (~c & a)) + x[ 7] + 0xfd469501; /* 8 */
			b = ((b << 22) | (b >>> 10)) + c;
			
			a += ((b & c) | (~b & d)) + x[ 8] + 0x698098d8; /* 9 */
			a = ((a << 7) | (a >>> 25)) + b;
			d += ((a & b) | (~a & c)) + x[ 9] + 0x8b44f7af; /* 10 */
			d = ((d << 12) | (d >>> 20)) + a;
			c += ((d & a) | (~d & b)) + x[10] + 0xffff5bb1; /* 11 */
			c = ((c << 17) | (c >>> 15)) + d;
			b += ((c & d) | (~c & a)) + x[11] + 0x895cd7be; /* 12 */
			b = ((b << 22) | (b >>> 10)) + c;
			
			a += ((b & c) | (~b & d)) + x[12] + 0x6b901122; /* 13 */
			a = ((a << 7) | (a >>> 25)) + b;
			d += ((a & b) | (~a & c)) + x[13] + 0xfd987193; /* 14 */
			d = ((d << 12) | (d >>> 20)) + a;
			c += ((d & a) | (~d & b)) + x[14] + 0xa679438e; /* 15 */
			c = ((c << 17) | (c >>> 15)) + d;
			b += ((c & d) | (~c & a)) + x[15] + 0x49b40821; /* 16 */
			b = ((b << 22) | (b >>> 10)) + c;
			
			
			/* Round 2 */
			a += ((b & d) | (c & ~d)) + x[ 1] + 0xf61e2562; /* 17 */
			a = ((a << 5) | (a >>> 27)) + b;
			d += ((a & c) | (b & ~c)) + x[ 6] + 0xc040b340; /* 18 */
			d = ((d << 9) | (d >>> 23)) + a;
			c += ((d & b) | (a & ~b)) + x[11] + 0x265e5a51; /* 19 */
			c = ((c << 14) | (c >>> 18)) + d;
			b += ((c & a) | (d & ~a)) + x[ 0] + 0xe9b6c7aa; /* 20 */
			b = ((b << 20) | (b >>> 12)) + c;
			
			a += ((b & d) | (c & ~d)) + x[ 5] + 0xd62f105d; /* 21 */
			a = ((a << 5) | (a >>> 27)) + b;
			d += ((a & c) | (b & ~c)) + x[10] + 0x02441453; /* 22 */
			d = ((d << 9) | (d >>> 23)) + a;
			c += ((d & b) | (a & ~b)) + x[15] + 0xd8a1e681; /* 23 */
			c = ((c << 14) | (c >>> 18)) + d;
			b += ((c & a) | (d & ~a)) + x[ 4] + 0xe7d3fbc8; /* 24 */
			b = ((b << 20) | (b >>> 12)) + c;
			
			a += ((b & d) | (c & ~d)) + x[ 9] + 0x21e1cde6; /* 25 */
			a = ((a << 5) | (a >>> 27)) + b;
			d += ((a & c) | (b & ~c)) + x[14] + 0xc33707d6; /* 26 */
			d = ((d << 9) | (d >>> 23)) + a;
			c += ((d & b) | (a & ~b)) + x[ 3] + 0xf4d50d87; /* 27 */
			c = ((c << 14) | (c >>> 18)) + d;
			b += ((c & a) | (d & ~a)) + x[ 8] + 0x455a14ed; /* 28 */
			b = ((b << 20) | (b >>> 12)) + c;
			
			a += ((b & d) | (c & ~d)) + x[13] + 0xa9e3e905; /* 29 */
			a = ((a << 5) | (a >>> 27)) + b;
			d += ((a & c) | (b & ~c)) + x[ 2] + 0xfcefa3f8; /* 30 */
			d = ((d << 9) | (d >>> 23)) + a;
			c += ((d & b) | (a & ~b)) + x[ 7] + 0x676f02d9; /* 31 */
			c = ((c << 14) | (c >>> 18)) + d;
			b += ((c & a) | (d & ~a)) + x[12] + 0x8d2a4c8a; /* 32 */
			b = ((b << 20) | (b >>> 12)) + c;
			
			
			/* Round 3 */
			a += (b ^ c ^ d) + x[ 5] + 0xfffa3942;      /* 33 */
			a = ((a << 4) | (a >>> 28)) + b;
			d += (a ^ b ^ c) + x[ 8] + 0x8771f681;      /* 34 */
			d = ((d << 11) | (d >>> 21)) + a;
			c += (d ^ a ^ b) + x[11] + 0x6d9d6122;      /* 35 */
			c = ((c << 16) | (c >>> 16)) + d;
			b += (c ^ d ^ a) + x[14] + 0xfde5380c;      /* 36 */
			b = ((b << 23) | (b >>> 9)) + c;
			
			a += (b ^ c ^ d) + x[ 1] + 0xa4beea44;      /* 37 */
			a = ((a << 4) | (a >>> 28)) + b;
			d += (a ^ b ^ c) + x[ 4] + 0x4bdecfa9;      /* 38 */
			d = ((d << 11) | (d >>> 21)) + a;
			c += (d ^ a ^ b) + x[ 7] + 0xf6bb4b60;      /* 39 */
			c = ((c << 16) | (c >>> 16)) + d;
			b += (c ^ d ^ a) + x[10] + 0xbebfbc70;      /* 40 */
			b = ((b << 23) | (b >>> 9)) + c;
			
			a += (b ^ c ^ d) + x[13] + 0x289b7ec6;      /* 41 */
			a = ((a << 4) | (a >>> 28)) + b;
			d += (a ^ b ^ c) + x[ 0] + 0xeaa127fa;      /* 42 */
			d = ((d << 11) | (d >>> 21)) + a;
			c += (d ^ a ^ b) + x[ 3] + 0xd4ef3085;      /* 43 */
			c = ((c << 16) | (c >>> 16)) + d;
			b += (c ^ d ^ a) + x[ 6] + 0x04881d05;      /* 44 */
			b = ((b << 23) | (b >>> 9)) + c;
			
			a += (b ^ c ^ d) + x[ 9] + 0xd9d4d039;      /* 33 */
			a = ((a << 4) | (a >>> 28)) + b;
			d += (a ^ b ^ c) + x[12] + 0xe6db99e5;      /* 34 */
			d = ((d << 11) | (d >>> 21)) + a;
			c += (d ^ a ^ b) + x[15] + 0x1fa27cf8;      /* 35 */
			c = ((c << 16) | (c >>> 16)) + d;
			b += (c ^ d ^ a) + x[ 2] + 0xc4ac5665;      /* 36 */
			b = ((b << 23) | (b >>> 9)) + c;
			
			
			/* Round 4 */
			a += (c ^ (b | ~d)) + x[ 0] + 0xf4292244; /* 49 */
			a = ((a << 6) | (a >>> 26)) + b;
			d += (b ^ (a | ~c)) + x[ 7] + 0x432aff97; /* 50 */
			d = ((d << 10) | (d >>> 22)) + a;
			c += (a ^ (d | ~b)) + x[14] + 0xab9423a7; /* 51 */
			c = ((c << 15) | (c >>> 17)) + d;
			b += (d ^ (c | ~a)) + x[ 5] + 0xfc93a039; /* 52 */
			b = ((b << 21) | (b >>> 11)) + c;
			
			a += (c ^ (b | ~d)) + x[12] + 0x655b59c3; /* 53 */
			a = ((a << 6) | (a >>> 26)) + b;
			d += (b ^ (a | ~c)) + x[ 3] + 0x8f0ccc92; /* 54 */
			d = ((d << 10) | (d >>> 22)) + a;
			c += (a ^ (d | ~b)) + x[10] + 0xffeff47d; /* 55 */
			c = ((c << 15) | (c >>> 17)) + d;
			b += (d ^ (c | ~a)) + x[ 1] + 0x85845dd1; /* 56 */
			b = ((b << 21) | (b >>> 11)) + c;
			
			a += (c ^ (b | ~d)) + x[ 8] + 0x6fa87e4f; /* 57 */
			a = ((a << 6) | (a >>> 26)) + b;
			d += (b ^ (a | ~c)) + x[15] + 0xfe2ce6e0; /* 58 */
			d = ((d << 10) | (d >>> 22)) + a;
			c += (a ^ (d | ~b)) + x[ 6] + 0xa3014314; /* 59 */
			c = ((c << 15) | (c >>> 17)) + d;
			b += (d ^ (c | ~a)) + x[13] + 0x4e0811a1; /* 60 */
			b = ((b << 21) | (b >>> 11)) + c;
			
			a += (c ^ (b | ~d)) + x[ 4] + 0xf7537e82; /* 61 */
			a = ((a << 6) | (a >>> 26)) + b;
			d += (b ^ (a | ~c)) + x[11] + 0xbd3af235; /* 62 */
			d = ((d << 10) | (d >>> 22)) + a;
			c += (a ^ (d | ~b)) + x[ 2] + 0x2ad7d2bb; /* 63 */
			c = ((c << 15) | (c >>> 17)) + d;
			b += (d ^ (c | ~a)) + x[ 9] + 0xeb86d391; /* 64 */
			b = ((b << 21) | (b >>> 11)) + c;
			
			state.state[0] += a;
			state.state[1] += b;
			state.state[2] += c;
			state.state[3] += d;
		}
		
		/**
		 * Updates hash with the bytebuffer given (using at maximum length bytes from
		 * that buffer)
		 *
		 * @param stat   Which state is updated
		 * @param buffer Array of bytes to be hashed
		 * @param offset Offset to buffer array
		 * @param length Use at maximum `length' bytes (absolute
		 *               maximum is buffer.length)
		 */
		public void Update (MD5State stat, byte buffer[], int offset, int length) {
			int index, partlen, i, start;
			finals = null;
			
			/* Length can be told to be shorter, but not inter */
			if ((length - offset)> buffer.length)
				length = buffer.length - offset;
			
			/* compute number of bytes mod 64 */
			
			index = (int) (stat.count & 0x3f);
			stat.count += length;
			
			partlen = 64 - index;
			
			if (length >= partlen) {
				
				// update state (using native method) to reflect input
				
				if (native_lib_loaded) {
					if (partlen == 64) {
						partlen = 0;
					} else {
						for (i = 0; i < partlen; i++)
							stat.buffer[i + index] = buffer[i + offset];
						Transform_native(stat.state, stat.buffer, 0, 64);
					}
					i = partlen + ((length - partlen) / 64) * 64;
					
					// break into chunks to guard against stack overflow in JNI
					
					int transformLength = length - partlen;
					int transformOffset = partlen + offset;
					final int MAX_LENGTH = 65536; // prevent stack overflow in JNI
					while (true) {
						if (transformLength > MAX_LENGTH) {
							Transform_native(stat.state, buffer, transformOffset, MAX_LENGTH);
							transformLength -= MAX_LENGTH;
							transformOffset += MAX_LENGTH;
						} else {
							Transform_native(stat.state, buffer, transformOffset, transformLength);
							break;
						}
					}
				}
				
				// update state (using only Java) to reflect input
				
				else {
					int[] decode_buf = new int[16];
					if (partlen == 64) {
						partlen = 0;
					} else {
						for (i = 0; i < partlen; i++)
							stat.buffer[i + index] = buffer[i + offset];
						Transform(stat, stat.buffer, 0, decode_buf);
					}
					for (i = partlen; (i + 63) < length; i+= 64) {
						Transform(stat, buffer, i + offset, decode_buf);
					}
				}
				index = 0;
			} else {
				i = 0;
			}
			
			/* buffer remaining input */
			if (i < length) {
				start = i;
				for (; i < length; i++) {
					stat.buffer[index + i - start] = buffer[i + offset];
				}
			}
		}
		
		/* 
		 * Update()s for other datatypes than byte[] also. Update(byte[], int)
		 * is only the main driver.
		 */
		
		/**
		 * Plain update, updates this object
		 **/
		public void Update (byte buffer[], int offset, int length) {
			Update(this.state, buffer, offset, length);
		}
		
		public void Update (byte buffer[], int length) {
			Update(this.state, buffer, 0, length);
		}
		
		/**
		 * Updates hash with given array of bytes
		 *
		 * @param buffer Array of bytes to use for updating the hash
		 **/
		public void Update (byte buffer[]) {
			Update(buffer, 0, buffer.length);
		}
		
		/**
		 * Updates hash with a single byte
		 *
		 * @param b Single byte to update the hash
		 **/
		public void Update (byte b) {
			byte buffer[] = new byte[1];
			buffer[0] = b;
			
			Update(buffer, 1);
		}
		
		/**
		 * Update buffer with given string.  Note that because the version of
		 * the s.getBytes() method without parameters is used to convert the
		 * string to a byte array, the results of this method may be different
		 * on different platforms.  The s.getBytes() method converts the string
		 * into a byte array using the current platform's default character set
		 * and may therefore have different results on platforms with different
		 * default character sets.  If a version that works consistently
		 * across platforms with different default character sets is desired,
		 * use the overloaded version of the Update() method which takes a
		 * string and a character encoding.
		 *
		 * @param s String to be update to hash (is used as s.getBytes())
		 **/
		public void Update (String s) {
			byte chars[] = s.getBytes();
			Update(chars, chars.length);
		}
		
		/**
		 * Update buffer with given string using the given encoding.  If the
		 * given encoding is null, the encoding "ISO8859_1" is used.
		 *
		 * @param s            String to be update to hash (is used as
		 *                     s.getBytes(charset_name))
		 * @param charset_name The character set to use to convert s to a
		 *                     byte array, or null if the "ISO8859_1"
		 *                     character set is desired.
		 * @exception          java.io.UnsupportedEncodingException If the named
		 *                     charset is not supported.
		 **/
		public void Update (String s, String charset_name) throws java.io.UnsupportedEncodingException {
			if (charset_name == null) charset_name = "ISO8859_1";
			byte chars[] = s.getBytes(charset_name);
			Update(chars, chars.length);
		}
		
		/**
		 * Update buffer with a single integer (only & 0xff part is used,
		 * as a byte)
		 *
		 * @param i Integer value, which is then converted to byte as i & 0xff
		 **/
		public void Update (int i) {
			Update((byte) (i & 0xff));
		}
		
		private byte[] Encode (int input[], int len) {
			int i, j;
			byte out[];
			
			out = new byte[len];
			
			for (i = j = 0; j  < len; i++, j += 4) {
				out[j] = (byte) (input[i] & 0xff);
				out[j + 1] = (byte) ((input[i] >>> 8) & 0xff);
				out[j + 2] = (byte) ((input[i] >>> 16) & 0xff);
				out[j + 3] = (byte) ((input[i] >>> 24) & 0xff);
			}
			
			return out;
		}
		
		/**
		 * Returns array of bytes (16 bytes) representing hash as of the
		 * current state of this object. Note: getting a hash does not
		 * invalidate the hash object, it only creates a copy of the real
		 * state which is finalized. 
		 *
		 * @return Array of 16 bytes, the hash of all updated bytes
		 **/
		public synchronized byte[] Final () {
			byte bits[];
			int index, padlen;
			MD5State fin;
			
			if (finals == null) {
				fin = new MD5State(state);
				
				int[] count_ints = {(int) (fin.count << 3), (int) (fin.count >> 29)};
				bits = Encode(count_ints, 8);
				
				index = (int) (fin.count & 0x3f);
				padlen = (index < 56) ? (56 - index) : (120 - index);
				
				Update(fin, padding, 0, padlen);
				Update(fin, bits, 0, 8);
				
				/* Update() sets finals to null */
				finals = fin;
			} 
			
			return Encode(finals.state, 16);
		}
		
		private final char[] HEX_CHARS = {'0', '1', '2', '3',
												 '4', '5', '6', '7',
												 '8', '9', 'a', 'b',
												 'c', 'd', 'e', 'f',};
		
		/**
		 * Turns array of bytes into string representing each byte as
		 * unsigned hex number.
		 * 
		 * @param hash Array of bytes to convert to hex-string
		 * @return Generated hex string
		 */
		public String asHex (byte hash[]) {
			char buf[] = new char[hash.length * 2];
			for (int i = 0, x = 0; i < hash.length; i++) {
				buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
				buf[x++] = HEX_CHARS[hash[i] & 0xf];
			}
			return new String(buf);
		}
		
		/**
		 * Returns 32-character hex representation of this objects hash
		 *
		 * @return String of this object's hash
		 */
		public String asHex () {
			return asHex(this.Final());
		}
		
	}
	
	/**
	 * Supporting class for MD5 hash generatorbr>
	 * Originally written by Santeri Paavolainen, Helsinki Finland 1996 <br>
	 * (c) Santeri Paavolainen, Helsinki Finland 1996 <br>
	 * Some changes Copyright (c) 2002 Timothy W Macinta <br>
	 *
	 * @author	Santeri Paavolainen <sjpaavol@cc.helsinki.fi>
	 * @author	Timothy W Macinta (twm@alum.mit.edu) (optimizations and bug fixes)
	 **/
	class MD5State {
	  /**
	   * 128-bit state 
	   */
	  int	state[];
	  
	  /**
	   * 64-bit character count
	   */
	  long count;
	  
	  /**
	   * 64-byte buffer (512 bits) for storing to-be-hashed characters
	   */
	  byte	buffer[];

	  public MD5State() {
		buffer = new byte[64];
		count = 0;
		state = new int[4];
		
		state[0] = 0x67452301;
		state[1] = 0xefcdab89;
		state[2] = 0x98badcfe;
		state[3] = 0x10325476;

	  }

	  /** Create this State as a copy of another state */
	  public MD5State (MD5State from) {
		this();
		
		int i;
		
		for (i = 0; i < buffer.length; i++)
		  this.buffer[i] = from.buffer[i];
		
		for (i = 0; i < state.length; i++)
		  this.state[i] = from.state[i];
		
		this.count = from.count;
	  }
	};
}
