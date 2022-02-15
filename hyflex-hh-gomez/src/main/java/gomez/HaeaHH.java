/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gomez;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

/**
 *
 * @author jgomez
 */
public class HaeaHH  extends HyperHeuristic{

    protected boolean[] isXOver;
    protected boolean[] isLocal;

    protected int[] xover;
    protected int[] mutation;
    protected int[] local;
    protected int[] ruin_recreate;

    protected int[] heuristics;

    protected int HEURISTIC_OPERS;
    protected double[] heuristic_rates;

    protected int LOCAL_HEURISTIC_OPERS;
    protected double[] local_heuristic_rates;


    protected double depth_search_limit = 0.5;
    protected double depth_search_rate;
    protected double delta;

    protected int parent = 0;
    protected int child = 1;
    protected int best = 2;
    protected int tempId;

    protected double[] f = new double[3];

    /**
     * creates a new ExampleHyperHeuristic object with a random seed
     */
    public HaeaHH(long seed){
        super(seed);
    }


    public int[] load_heuristics(){
        heuristics = new int[xover.length+mutation.length+local.length+ ruin_recreate.length];

        int k = 0;
        System.arraycopy(xover, 0, heuristics, k, xover.length);
        k += xover.length;


        System.arraycopy(mutation, 0, heuristics, k, mutation.length);
        k += mutation.length;

        System.arraycopy(local, 0, heuristics, k, local.length);
        k += local.length;

        System.arraycopy(ruin_recreate, 0, heuristics, k, ruin_recreate.length);

        int max = heuristics[0];
        for( int i=1; i<heuristics.length; i++ ){
            if( max < heuristics[i] ){
                max = heuristics[i];
            }
        }

        isXOver = new boolean[max+1];
        for( int i=0; i<xover.length; i++ ){
            isXOver[xover[i]] = true;
        }

        isLocal = new boolean[max+1];
        for( int i=0; i<local.length; i++ ){
            isLocal[local[i]] = true;
        }
        
        return heuristics;
    }


    public static void init( double[] x ){
        double v = 1.0 / x.length;
        for( int i=0; i<x.length; i++ ){
            x[i] = v;
        }
    }

    public static void normalize( double[] x ){
        double s = 0.0;
        for( int i=0; i<x.length; i++ ){
            s += x[i];
        }
        for( int i=0; i<x.length; i++ ){
           x[i] /= s;
        }
    }

    public static int roulette( double[] rates ){
        double x = Math.random();
        int i = 0;
        while( i<rates.length && x >= rates[i] ){
            x -= rates[i];
            i++;
        }
        return i;
    }

    public static void reward( double[] x, int i ){
        double delta = 1.0+Math.random();
        x[i] *= delta;
        normalize(x);
    }

    public static void punish( double[] x, int i ){
        double delta = 1.0-Math.random();
        x[i] *= delta;
        normalize(x);
    }

    public static void permutation( int[] a ){
        int n = a.length;
        int index;
        int t;
        for( int i=0; i<n; i++ ){
            index = (int)(Math.random()*n);
            t = a[i];
            a[i] = a[index];
            a[index] = t;
        }
    }


    public int select_operator( int n ){
        return (int)(Math.random()*n);
    }

    public void reset_operators(){

        init(heuristic_rates);
        init(local_heuristic_rates);

        permutation(local);
        permutation(heuristics);
        int i=0;
        while( i<HEURISTIC_OPERS && !isLocal[heuristics[i]]){ i++; }
        if(i==HEURISTIC_OPERS){
            while( heuristics[i] != local[local.length-1] ){
                i++;
            }
            heuristics[i] = heuristics[0];
            heuristics[0] = local[local.length-1];
        }

    }

    public void init_alpha( ProblemDomain problem ){
        depth_search_rate = 0.1;
        delta = 0.0;
    }

    public void init( ProblemDomain problem ){
        problem.setMemorySize(3);
        problem.initialiseSolution(0);
        problem.initialiseSolution(1);
        f[0] = problem.getFunctionValue(0);
        f[1] = problem.getFunctionValue(1);
        if( f[0] <= f[1] ){
            parent = 0;
            child = 1;
        }else{
            parent = 1;
            child = 0;
        }
        problem.copySolution(parent, best);
        f[best] = f[parent];

        // Getting the unary heuristics for the problem domain
        xover = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
        mutation = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
        ruin_recreate = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
        local = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);

        // Computing the full set of unary heuristics
        load_heuristics();

        // Building the set of operators used by the evolving candidate solution
        HEURISTIC_OPERS = 4;
        heuristic_rates = new double[HEURISTIC_OPERS];

        LOCAL_HEURISTIC_OPERS = Math.min(HEURISTIC_OPERS, local.length);
        local_heuristic_rates = new double[LOCAL_HEURISTIC_OPERS];

        // Init operator rates
        reset_operators();

        // initializing the alpha/beta parameters
        init_alpha( problem );
    }

    public void update_alpha( ProblemDomain problem ){
        double x = Math.random() * 0.1;
        depth_search_rate += x;
        delta += x;
        if( depth_search_rate > depth_search_limit ){
            depth_search_rate -= depth_search_limit;
        }
    }

    public void swap(){
        tempId = parent;
        parent = child;
        child = tempId;
    }

    public void replacement( ProblemDomain problem, int h, int l ){
        if( f[child] < f[parent] ){
            // reward
            reward(heuristic_rates, h);
            reward(local_heuristic_rates, l);

            if( f[child] < f[best] ){
                problem.copySolution(child, best);
                f[best] = f[child];
            }
            delta = 0.0;
            swap();
        }else{
            // punish            
            punish(heuristic_rates, h);
            punish(local_heuristic_rates, l);
                    
            update_alpha( problem );
            problem.setDepthOfSearch(0.2);
            problem.setIntensityOfMutation(0.2);
            f[best] = problem.applyHeuristic(local[local.length-1], best, best);

            if( delta >= depth_search_limit ){
                delta = 0.0;
                if( f[parent] == f[best] ) {
                    swap();
                }else{
                    if( isXOver[heuristics[heuristics.length-1]] ){
                        f[parent] = problem.applyHeuristic(heuristics[heuristics.length-1], child, best, parent);
                    }else{
                        f[parent] = problem.applyHeuristic(heuristics[heuristics.length-1], best, parent);
                    }
                    f[parent] = problem.applyHeuristic(local[local.length-1], parent, parent);
                    if( f[parent] < f[best] ){
                        f[best] = f[parent];
                        problem.copySolution(parent, best);
                    }
                }
                reset_operators();
            }else{
                if( f[parent] == f[child] ){
                    swap();
                }                
            }
        }
    }

    public void apply_operator( ProblemDomain problem, int h ){
        if( isXOver[heuristics[h]] ){
            f[child] = problem.applyHeuristic(heuristics[h], parent, best, child);
        }else{
            f[child] = problem.applyHeuristic(heuristics[h], parent, child);
        }
    }

    /**
     * This method defines the strategy of the hyper-heuristic
     * @param problem the problem domain to be solved
     */
    public void solve(ProblemDomain problem) {
        int h, lh;

        init(problem);

        //the main loop of any hyper-heuristic, which checks if the time limit has been reached
        while (!hasTimeExpired()) {
            problem.setDepthOfSearch(depth_search_rate);
            problem.setIntensityOfMutation(depth_search_limit-depth_search_rate);

            h = roulette(heuristic_rates);

            apply_operator( problem, h );
            
            lh = roulette(local_heuristic_rates);
            f[child] = problem.applyHeuristic(local[lh], child, child);

            replacement(problem, h, lh);
        }
    }

    /**
     * this method must be implemented, to provide a different name for each hyper-heuristic
     * @return a string representing the name of the hyper-heuristic
     */
    public String toString() {
            return "HAEA: Hybrid Adaptive Evolutionary Algorithm";
    }
}