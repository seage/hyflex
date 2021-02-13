package leangihh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import AbstractClasses.ProblemDomain.HeuristicType;

/**
 * 
 * A java implementation of Lean-GIHH, a simplified version of GIHH a hyper-heuristic originally created by Mustafa Misir and
 * whose source code is available under the GNU GPL v3 license (https://code.google.com/p/generic-intelligent-hyper-heuristic/).
 * 
 * This considers a full re-implementation, where no code was copied. In addition various algorithmic sub-mechanisms were replaced or 
 * eliminated because they were not found to contribute significantly to performance. Please refer to our research paper titled 
 * "A Case Study Applying Accidental Complexity Analysis to a State-of-the-art Selection Hyperheuristic for HyFlex" (Adriaensen et al., 2016)
 * for more information.
 * 
 * @author Steven Adriaensen
 *
 */
public class LeanGIHH  extends HyperHeuristic{

	/* CONSTANTS */
	
	//HyFlex memory indices for the...
	//incumbent solution
	static final private int c_incumbent = 0; 
	//incoming solution
	static final private int c_proposed = 1;
	//best solution since last re-initialization
	static final private int c_runbest = 2;
	//best solution so far
	static final private int c_best = 3;
	//5 solutions used as second solution in crossover
	static final private int[] cs_xo = new int[]{4,5,6,7,8};
	
	//the number of fitness values (of new best solutions) kept by the acceptance condition
	static final private int bestlist_size = 6;
	
	//the number of heuristic indices kept (per heuristic), to be used as second heuristic in relay hybridization
	static final private int r2list_size = 10;
	
	//a vector representing the increment/decrement in val_i for feedback type x (0:nb,1:impr,2:wrs,3:eq)
	static final private double[] diff = new double[]{0.0025,0.00025,-0.00025,-0.00005};
	
	//the learning rate used to update the selection probabilities for the first heuristic in relay hybridization
	static final private double alpha = 0.2;

	//set patience K (increment threshold when exceeded)
	static final private int K = 100;
	
	/* PROBLEM SPECIFIC CONSTANTS */
	//these are set in the setup method (and never changed for the duration of the search)
	
	//the HyFlex problem instance to be solved
	private ProblemDomain problem;
	
	//the number of heuristics in the heuristic set 
	private int N;
	
	//the duration (in #iterations) of a phase
	private int pl;
	
	//a set of indices of the crossover heuristics
	private Set<Integer> xos;
	
	/* VARIABLES */ 
	//all variables are initialized in the setup method
	
	//fitness values for...
	//incumbent solution
	private double f_incumbent;
	//incoming solution
	private double f_proposed;
	//best solution since last re-initialization
	private double f_runbest;
	//best solution so far
	private double f_best;
	
	/* Selection mechanism */
	//the number of iterations that resulted in a new best solution
	private int n_best_total;
	
	//the number of iterations for which (uncanceled) relay hybridization found a new best solution
	private int n_best_relay;
	
	//the number of iterations performed in the current phase
	private int n_pit;
	
	//for each heuristic i...
	//t_spent(i): the total time spent applying it (in ms)
	private double[] t_spent;
	//c_best(i): the number of new best solutions it generated
	private int[] n_best;
	//val_i: the parameter value used as "intensity of mutation" and "depth of search" in its application
	private double[] val;
	//the value proportionally to which it is selected single heuristic selection mechanism.
	private double[] pr;
	//pr_i: the probability of it being selected as first heuristic in relay hybridization
	private double[] prr1;
	//the list of indices of heuristics to be selected as second heuristic (after i) in relay hybridization
	private ArrayList<CircularBuffer<Integer>> r2list;
	
	/* Acceptance mechanism */
	
	//index pointing to the current threshold in runbest_list.
	private int index;
	
	//the fitness values of last bestlist_size new best solutions
	private CircularBuffer<Double> runbest_list;
	
	//the runbest_list of the best run so far (that obtained c_best)
	private CircularBuffer<Double> best_list;
	
	//# worsening iterations after last improving c_runbest or last increment of index
	private int counter_K;
	
	/* Re-initialization mechanism */
	
	//whether the seach is currently stuck (index exceeds maximum threshold)
	private boolean stuck;
	
	//whether restart is disabled or not
	private boolean restart_disabled;
	
	/**
	 * Constructs an instance of Lean-GIHH
	 * 
	 * @param seed: the seed for the random generator
	 */
	public LeanGIHH(long seed){
		//does nothing besides calling the constructor of the super-class.
		super(seed);
	}

	@Override
	protected void solve(ProblemDomain problem) {
		//initialize variables (and problem specific constants)
		setup(problem);
		while(!this.hasTimeExpired()){ //do while t_elapsed < T_total
			/* restart condition */
			if(!restart_disabled){
				//check restart-condition
				if(getFractionElapsed() <= 0.5){
					if(stuck){
						restart(); //perform a restart
					}
				}else{
					disable_restart(); //disable restart
				}
			}
			
			/* selection (and application) mechanism */
			
			//decide between single/relay
			double gamma = (n_best_total-n_best_relay+1.0)/(n_best_relay+1.0);
			//keep gamma in bounds [0.02,50]
			gamma = Math.max(0.02, Math.min(gamma, 50));
			//determine ratio single/relay
			double p_relay = Math.pow((double)n_pit/pl,gamma);
			if(rng.nextDouble() > p_relay){
				/* single selection mechanism */
				//select single
				update_pr();
				int heur = roulette_wheel_selection(pr);
				//apply single
				ApplicationInfo info = apply_heuristic(heur,f_incumbent,c_incumbent,c_proposed);
				f_proposed = info.f_out;
			}else{
				/* relay hybridization */
				//select first heuristic
				int heur1 = roulette_wheel_selection(prr1);
				//apply first
				ApplicationInfo info1 = apply_heuristic(heur1,f_incumbent,c_incumbent,c_proposed);
				f_proposed = info1.f_out;
				if(!info1.new_runbest){ //if no new best
					//check whether the first heuristic generated a solution identical to c_incumbent (was a noop)
					boolean identity1 = (f_incumbent == f_proposed && problem.compareSolutions(c_incumbent, c_proposed));
					//select second
					int heur2;
					int index = rng.nextInt(r2list_size+1);
					if(index < r2list.get(heur1).size()){
						heur2 = r2list.get(heur1).get(index); //from list
					}else{
						heur2 = rng.nextInt(N); //uniformly at random
					}
					//apply second
					ApplicationInfo info2 = apply_heuristic(heur2,f_proposed,c_proposed,c_proposed);
					f_proposed = info2.f_out;
					//update selection probabilities for relay
					if(info2.new_runbest){
						r2list.get(heur1).push(heur2);
						if(!identity1){
							n_best_relay++;
							update_prr1(heur1);
						}
					}
				} //cancel relay hybridization
			}
				
			/* acceptance mechanism */
			if(accept()){
				//accept c_proposed as c_incumbent
				problem.copySolution(c_proposed, c_incumbent); //� replace c_incumbent by c_proposed
				f_incumbent = f_proposed;
			}
			
			//end iteration, update n_pit
			n_pit = (n_pit == pl)? n_pit = 0 : n_pit + 1;
			//System.out.println(pl);
		}
	}
	
	/**
	 * Sets all problem-specific constants and initialize all variables.
	 * 
	 * @param problem: the problem instance to be solved
	 */
	private void setup(ProblemDomain problem){
		/* set problem-specific constants */
		this.problem = problem;
		//set memory size to 9
		problem.setMemorySize(9);
		//set the number of heuristics
		N = problem.getNumberOfHeuristics();
		//set phase length
		pl = (int) (500*Math.round(Math.sqrt(2*N)));
		
		//initialize the set of crossover heuristics
		xos = new HashSet<Integer>();
		int[] xos_temp = problem.getHeuristicsOfType(HeuristicType.CROSSOVER);
		for(int i = 0; i < xos_temp.length; i++){
			xos.add(xos_temp[i]);
		}

		/* initialize variables */
		n_pit = 1;
		
		//t_spent is initialized to 0
		t_spent = new double[N];
		//c_best is initialized to 0
		n_best = new int[N];
		//val is initialized to 0.2
		val = new double[N];
		for(int i = 0; i < N; i++){
			val[i] = 0.2;
		}
		//each heuristic is initially selected uniformly by the single selection mechanism (update_pr will set these)
		pr = new double[N];
		//each heuristic is initially selected uniformly as first heuristic in relay hybridization
		prr1 = new double[N];
		for(int i = 0; i < N; i++){
			prr1[i] = 1.0/N;
		}
		//each heuristic's r2list is initially empty
		r2list = new ArrayList<CircularBuffer<Integer>>(N);
		for(int i = 0; i < N; i++){
			r2list.add(new CircularBuffer<Integer>(r2list_size));
		}
		//f_best is initialized pessimistically
		f_best = Double.POSITIVE_INFINITY;
		//initialize all variables that are re-initialized every run
		init();
	}
	
	/**
	 * Initializes all variables that are re-initialized every run (at first and after every restart)
	 */
	private void init(){
		//initialize the incumbent solution
		problem.initialiseSolution(c_incumbent);
		//initialize the runbest solution as incumbent solution
		problem.copySolution(c_incumbent, c_runbest);
		f_incumbent = f_runbest = problem.getFunctionValue(c_incumbent);
		
		//initialize best list to the fitness of the initial solution
		runbest_list = new CircularBuffer<Double>(bestlist_size);;
		for(int i = 0; i < bestlist_size; i++){
			runbest_list.push(f_incumbent);
		}
		//reset counter to 0
		counter_K = 0;
		//reset threshold level to its minimum (pointing to the second element of best_list)
		index = 1;
		
		//initialize solutions for crossover as copies of the initial solution
		if(!xos.isEmpty()){
			for(int i = 0; i < cs_xo.length; i++){
				problem.copySolution(c_incumbent, cs_xo[i]);
			}
		}
	}
	
	/**
	 * Get the fraction of time elapsed.
	 * 
	 * @return the fraction of time elapsed (0->1) (beginning->end)
	 */
	private double getFractionElapsed(){
		return Math.min((double) this.getElapsedTime()/this.getTimeLimit(),1);
	}
	
	/**
	 * Re-initializes the search
	 */
	private void restart(){
		//we're no longer stuck
		stuck = false; 
		//update c_best;
		if(f_runbest < f_best){ // if current best, better than overall best
			//replace c_best by c_runbest
			problem.copySolution(c_runbest, c_best);
			f_best = f_runbest;
			//update best_list
			best_list = runbest_list; 
		}
		//re-initialize search
		init();
	}
	
	/**
	 * Disables future restarts, potentially performing best-initialization
	 */
	private void disable_restart(){
		//we're no longer stuck
		stuck = false;
		//indicate restart is disabled
		restart_disabled = true;
		//check whether to perform best-init
		if(f_best < f_runbest){
			//restore the state to that corresponding to the best run so far
			//restore c_incumbent and c_runbest
			problem.copySolution(c_best, c_incumbent);
			problem.copySolution(c_best, c_runbest);
			f_incumbent = f_runbest = f_best;
			//restore runbest_list
			runbest_list = best_list; //set runbest_list to that of best
			//reset counter to 0
			counter_K = 0;
			//reset threshold level to its minimum (pointing to the second element of best_list)
			index = 1;
		}
	}
	
	/**
	 * The acceptance mechanism
	 * 
	 * @return whether or not to accept c_proposed as new incumbent solution
	 */
	private boolean accept(){
    	if(f_proposed < f_incumbent){ // if improvement
    		if(f_proposed < runbest_list.get(0)){ // if new best
    			//no longer stuck
    			stuck = false;
    			//add new best to the list
    			runbest_list.push(f_proposed);
    			//reset threshold level
    			index = 1;
    			//reset counter_K
				counter_K = 0; 
			}
    		//always accept improvement
    		return true; 
    	}else if(f_proposed == f_incumbent){ // if equal
    		// always accept equal quality solutions
    		return true; 
    	}else{ //worsening proposal
    		//accept worsing whitin threshold
    		boolean acp = f_proposed <= runbest_list.get(index);
    		//increment counter_K
    		counter_K++;
    		if(counter_K >= K){ //if patience_K is exceeded
    			//reset counter_K
    			counter_K = 0;
    			//increment the index/threshold
    			index++;
    			if(index >= bestlist_size){ //if index exceeds the size of the best list
    				//communicate we're stuck at re-initialization mechanism
    				stuck = true; 
    				//keep threshold at its maximum value
    				index--; 
    			}
    		}
    		return acp;
		}
	}
	
	/**
	 * Updates the selection probabilities for the single selection mechanism
	 */
	private void update_pr(){
		//compute the fraction of time remaining
		double tf = 1-getFractionElapsed();
		//update the single selection probabilities for each heuristic i
		for(int i = 0; i < N; i++){
			pr[i] = Math.pow((n_best[i]+1)/Math.max(1, t_spent[i]),1.0+(3.0*tf*tf*tf));
		}
	}
	
	/**
	 * Updates the probabilities of selecting heur1 as first heuristic in relay hybridization.
	 * Called if the combination of heur1+heur2 found a new best solution this iteration.
	 * 
	 * @param heur: first heuristic used in relay hybridization this iteration 
	 */
	private void update_prr1(int heur){
		//increase prob of using heur as first heuristic
		prr1[heur] += alpha*(1.0-prr1[heur]); 
		//lower the probability of all other heuristics (such that all sum to 1)
		for(int i = 0; i < N; i++){
			if(i != heur){
				prr1[i] -= alpha*prr1[i];
			}
		}
	}
	
	/**
	 * Selects a value i in [0,pr.length-1] with a probability proportionally to pr[i]
	 * @param pr: values indices are selected proportionally to
	 * @return the selected index
	 */
	private int roulette_wheel_selection(double[] pr){
		//compute the sum of elements in pr
		double norm = 0;
		for(int i = 0; i < pr.length; i++){
			norm += pr[i];
		}
		//select an index i proportionally to pr[i]
		double pivot = norm*rng.nextDouble();
		int select = 0;
		double accum = pr[0];
		while(accum <= pivot){
			select++;
			accum += pr[select];
		}
		return select;
	}
	
	/**
	 * Apply heuristic heur to solution at c_in and store the result in c_out
	 * @param heur: the index of the heuristic to be applied
	 * @param f_in: the fitness value of the input
	 * @param c_in: memory location to be used as input
	 * @param c_out: memory location to be used as output
	 * @return The fitness of the output and whether it generated a new_best solution
	 */
	private ApplicationInfo apply_heuristic(int heur, double f_in, int c_in, int c_out){
		//set parameters
		problem.setIntensityOfMutation(val[heur]); //� DUPLICATE
		problem.setDepthOfSearch(val[heur]); //�
		//actual application
		long before = this.getElapsedTime();
		double f_out;
		if(xos.contains(heur)){
			//crossover
			int c_xo = cs_xo[rng.nextInt(cs_xo.length)];
			f_out = problem.applyHeuristic(heur, c_in, c_xo, c_out);
		}else{
			//perturbation
			f_out = problem.applyHeuristic(heur, c_in, c_out);
		}
		
		//update performance metrics
		t_spent[heur] += this.getElapsedTime()-before;
		int x;
		if(f_out < f_in){
			//improvement
			x = 1;
			if(f_out < f_runbest){
				//new runbest
				hasTimeExpired(); //required to update new best returned (hyflex weirdness...)
				problem.copySolution(c_out, c_runbest);
				problem.copySolution(c_out, cs_xo[rng.nextInt(cs_xo.length)]); //replace a random solution for crossover
				f_runbest = f_out;
				n_best[heur]++;
				n_best_total++;
				x = 0;
			}
		}else if(f_out == f_in){
			//equal
			x = 3;
		}else{
			//worsening
			x = 2;
		}
		//update parameters
		val[heur] += diff[x];
		//enforce bounds [0.2,1.0]
		val[heur] = Math.max(0.2, Math.min(val[heur],1.0));
		return new ApplicationInfo(f_out, x == 0);
	}

	@Override
	public String toString() {
		//The name of this hyper-heuristic
		return "Lean-GIHH";
	}
	
	/**
	 *	A record representing the following information about a heuristic application:
	 *  - the fitness of the generated solution
	 *  - whether the generated solution was a new best
	 */
	class ApplicationInfo{
		double f_out;
		boolean new_runbest;
		
		ApplicationInfo(double f, boolean new_runbest){
			this.f_out = f;
			this.new_runbest = new_runbest;
		}
	}


}
