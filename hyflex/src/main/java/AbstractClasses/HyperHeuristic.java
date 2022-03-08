package AbstractClasses;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Random;
/**
 * HyperHeuristic is the superclass of all hyper-heuristic classes which will be entered into the competition.
 * It provides the methods to manage the time limit, and to run the hyper-heuristic. This class
 * ensures that competitors only need to implement the hyper-heuristic strategy, and not be concerned with further implementation details.
 * The timing elements of this class always use CPU seconds, obtained by the ThreadMXBean.getCurrentThreadCpuTime() method. This minimises
 * the effect of other minor processes which are running on the machine in the background.
 * @author mvhj, jav, tec
 */
public abstract class HyperHeuristic {

	/**
	 * The random variable which can be used by subclasses of HyperHeuristic
	 */
	protected Random rng;
	private long timeLimit = 0;
	private long initialTime;
	private ThreadMXBean bean;
	private ProblemDomain problem;
	private double printfraction;
	private double printlimit;
	private int lastprint;
	private boolean initialprint;
	private double lastbestsolution;
	//private FileWriter filewriter;
	//private PrintWriter buffprint;
	private double[] trace;
	private static final int tracecheckpoints = 101;

	/**
	 * Constructs a new HyperHeuristic object, and creates a new random number generator
	 * using the seed provided. 
	 * The HyperHeuristic will then behave the same way when using an identical seed, 
	 * but only if the rng variable is used for every random operation.
	 * @param seed A seed for the random number generator
	 */
	public HyperHeuristic(long seed){
		this.lastbestsolution = -1;
		this.rng = new Random(seed);
	}

	/**
	 * Constructs a new HyperHeuristic object with an unknown random seed. 
	 * The HyperHeuristic will run, but the exact behaviour cannot be replicated.
	 */
	public HyperHeuristic(){
		this.lastbestsolution = -1;
		this.rng = new Random();
	}

	/**
	 * Sets the time limit for the hyper-heuristic in milliseconds.
	 * This method must be called before the run() method.
	 * @param time_in_milliseconds The time limit for the hyper-heuristic in milliseconds (CPU time)
	 */
	public void setTimeLimit(long time_in_milliseconds){
		this.timeLimit = time_in_milliseconds*1000000;//change to nanoseconds
		this.printfraction = time_in_milliseconds*10000;
		this.printlimit = printfraction;
		this.initialprint = false;
		this.lastprint = 0;
	}

	/**
	 * Returns the time limit in milliseconds (CPU time).
	 * */
	public long getTimeLimit() {
		return this.timeLimit/1000000;
	}

	/**
	 * Returns the elapsed time since the hyper-heuristic began the search process.
	 * This time is initialised when the run() method is called.
	 * */
	public long getElapsedTime() 
	{
		if (bean == null) {return 0;}
		//System.out.println((long)(((double)(bean.getCurrentThreadCpuTime()-this.initialTime))/1000000.0));
		return (long)(((double)(bean.getCurrentThreadCpuTime()-this.initialTime))/1000000.0);
	}

	/**
	 * Gets the best solution found before the time limit expires. This variable is updated each time the
	 * hasTimeExpired() method is called. Therefore, this method should only be called after the hasTimeExpired() 
	 * method has been called at least once.
	 * @return the objective function value of the best solution found within the time limit
	 * */
	public double getBestSolutionValue()
	{
		if (lastbestsolution == -1) {
			System.err.println("The hasTimeExpired() method has not been called yet. It must be called at least once before a call to getBestSolutionValue()");
			System.exit(1);
		}
		return lastbestsolution;
	}
	
	/**
	 * Gets an array of the fitness of the best initial solution and the fitness of the best solutions found at 100 checkpoints through the search.
	 * Before a run, the timelimit is divided by 100, to obtain the length of the interval between checkpoints.
	 * The fitness at index 0 is the fitness of the best solution found when the first call is made to the hasTimeExpired() method. 
	 * If the hyper-heuristic only uses one solution in memory, then the first fitness value recorded in the trace will be the fitness of the 
	 * initial solution. If the hyper-heuristic uses multiple solutions in memory, and initialises more than one solution before the first 
	 * call to hasTimeExpired(), then the first fitness value recorded in the trace will be the best of those.
	 * The fitness at index 1 is after the first checkpoint.
	 * The fitness at index 100 is the last recorded fitness of the best solution before the time limit is exceeded.
	 * @return an array of 101 double values, which represent the fitness of the best solution found so far, at 101 checkpoints
	 * in the search process, including the initial solution and the last recorded best solution before the time limit is exceeded.
	 * */
	public double[] getFitnessTrace() {
		return trace;
	}

	/**
	 * Tests if the time limit has been reached and it records the best solution value found so far, for scoring in the competition.
	 * This method should be called as often as possible by an implemented hyper-heuristic
	 * within its solve() method, to ensure that the time limit is not exceeded by a significant amount.
	 * It is important to note that each call to hasTimeExpired() registers the current best solution found, and it is this record that 
	 * will be used to determine the best solution found within the time limit.
	 * This is a mechanism which is intended to ensure that there is NO BENEFIT to exceeding the time limit. 
	 * @return Returns true if the time limit has been exceeded, and false if it has not been exceeded.
	 * */
	protected boolean hasTimeExpired()
	{
		long time = (bean.getCurrentThreadCpuTime()-this.initialTime);
		if (!this.initialprint) {
			this.initialprint = true;
			double res = problem.getBestSolutionValue();
			trace[0] = res;	lastbestsolution = res;
		} else if (time >= printlimit) {
			int thisprint = (int)(time/printfraction);
			//System.out.println(time+ " " + printlimit + " " + (time/1000000000) + " " + thisprint + " " + problem.getBestSolutionValue());
			if (thisprint > 100) {thisprint = 100;}
			for (int x = 0; x < thisprint - lastprint; x++) {
				if (time <= this.timeLimit) {
					double res = problem.getBestSolutionValue();
					trace[lastprint+x+1] = res;
					lastbestsolution = res;
				} else {
					trace[lastprint+x+1] = lastbestsolution;
				}
				printlimit += printfraction;
			}
			lastprint = thisprint;
		}
		if (time >= this.timeLimit) {
			return true;
		} else {
			lastbestsolution = problem.getBestSolutionValue();
			return false;
		}
	}

	/**
	 * Assigns the problem domain to be solved by this hyper-heuristic, 
	 * when the run method is called. This method must be called before the run() method.
	 * @param problemdomain The problem domain to be solved by this hyper-heuristic.
	 */
	public void loadProblemDomain(ProblemDomain problemdomain) {
		if (this.timeLimit == 0) {
			System.err.println("No problem instance has been loaded in the ProblemDomain object with problem.loadInstance()");
			System.exit(1);
		}
		problem = problemdomain;
	}

	/**
	 * Runs the hyper-heuristic on the current problem domain to produce a solution. 
	 * The hyper-heuristic will run for the length of time specified by the user to the setTimeLimit() method.
	 */
	public void run() {
		if (problem == null) {
			System.err.println("No problem domain has been loaded with loadProblemDomain()");
			System.exit(1);
		}
		if (this.timeLimit == 0) {
			System.err.println("No time limit has been set with setTimeLimit()");
			System.exit(1);
		}
		trace = new double[tracecheckpoints];
		this.bean = ManagementFactory.getThreadMXBean( );
		this.initialTime = bean.getCurrentThreadCpuTime();
		solve(problem);
	}

	/**
	 * Solves the instance of the problem domain which is provided as an argument. 
	 * The exact instance and time limit can be set via methods in the problem domain class.
	 * This method must be implemented by hyper-heuristic objects, as it specifies the hyper-heuristic's strategy.
	 * @param problem The problem that is to be solved.
	 */
	protected abstract void solve(ProblemDomain problem);

	/**
	 * returns the name of this hyper-heuristic
	 */
	public abstract String toString();

}
