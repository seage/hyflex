package jiang;

/**
 * edited by Dave Omrai
 */

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;


public class sa_ilsHyperHeuristic extends HyperHeuristic {

        public sa_ilsHyperHeuristic(long seed){
		super(seed);
	}

	public void solve(ProblemDomain problem) {

            int number_of_heuristics = problem.getNumberOfHeuristics();
            int[] local_search= problem.getHeuristicsOfType(ProblemDomain.HeuristicType.LOCAL_SEARCH);

            if(local_search!=null){

                problem.initialiseSolution(0);  

                long now_time=0;
                boolean problem_consumed=false;
                boolean very_consumed=false;

                int heuristic_to_apply;

                double best_value = Double.POSITIVE_INFINITY;
                double new_value = Double.POSITIVE_INFINITY;

                if(local_search.length==2){
                    if(rng.nextBoolean()){
                        int temp=local_search[0];
                        local_search[0]=local_search[1];
                        local_search[1]=temp;
                    }
                }else if(local_search.length>2){
                    for(int i=0; i<local_search.length; i++){
                        int pos=rng.nextInt(local_search.length);
                        int temp=local_search[i];
                        local_search[i]=local_search[pos];
                        local_search[pos]=temp;
                    }
                }

                now_time=this.getElapsedTime();
                for(int i=0;i<local_search.length;i++){
                    best_value=problem.applyHeuristic(local_search[i], 0, 0);
                    if(i%2==0)
                        hasTimeExpired();
                }

                if((this.getElapsedTime()-now_time)/local_search.length>316)
                    problem_consumed=true;
                if((this.getElapsedTime()-now_time)/local_search.length>8775)
                    very_consumed=true;

                int times_set=7;
                int times_to_accept=0;

                int[] crossover=problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
                int[] mutation=problem.getHeuristicsOfType(ProblemDomain.HeuristicType.MUTATION);
                int[] ruin_recreate=problem.getHeuristicsOfType(ProblemDomain.HeuristicType.RUIN_RECREATE);
                int c=0;
                int m=0;
                int rr=0;  
                if(crossover!=null)
                    c=crossover.length;
                if(mutation!=null)
                    m=mutation.length;
                if(ruin_recreate!=null)
                    rr=ruin_recreate.length;

                if(problem_consumed){

                    problem.initialiseSolution(1);

                    problem.setIntensityOfMutation(0.4);
                    problem.setDepthOfSearch(0.4);

                }else{

                    problem.copySolution(0, 1);
                    new_value=best_value;

                    problem.setIntensityOfMutation(0.8);
                    problem.setDepthOfSearch(0.6);
                }

                while(!hasTimeExpired()){

                    if(problem_consumed){

                        times_to_accept++;

                        int selection=0;
                        do{
                            selection=rng.nextInt(m+rr+c+1);
                        }while(selection==0);

                        if(selection<=m){
                            heuristic_to_apply = mutation[rng.nextInt(m)];
                            problem.applyHeuristic(heuristic_to_apply,0,1);
                        }
                        else if(selection>m&&selection<=(m+rr)){
                            heuristic_to_apply = ruin_recreate[rng.nextInt(rr)];
                            problem.applyHeuristic(heuristic_to_apply,0,1);
                        }
                        else{
                            heuristic_to_apply = crossover[rng.nextInt(c)];
                            problem.applyHeuristic(heuristic_to_apply,0,1,1);
                        }

                        if(local_search.length==2){
                            if(rng.nextBoolean()){
                                int temp=local_search[0];
                                local_search[0]=local_search[1];
                                local_search[1]=temp;
                            }
                        }else if(local_search.length>2){
                            for(int i=0; i<local_search.length; i++){
                                int pos=rng.nextInt(local_search.length);
                                int temp=local_search[i];
                                local_search[i]=local_search[pos];
                                local_search[pos]=temp;
                            }
                        }
                        for(int i=0;i<local_search.length;i++){
                            new_value=problem.applyHeuristic(local_search[i],1,1);
                            if(very_consumed)
                                hasTimeExpired();
                            else if(i%2==0)
                                hasTimeExpired();
                        }

                        if(new_value<best_value){
                            problem.copySolution(1, 0);
                            best_value=new_value;
                            times_to_accept=0;

                        }else if(times_to_accept>times_set){
                            problem.copySolution(1, 0);
                            best_value=new_value;
                            times_to_accept=0;
                        }

                    }else{
                        int i=0;
                        int times=0;
                        double tmp_value=Double.POSITIVE_INFINITY;
                        while(times<times_set){
                            i++;
                            times++;
                            tmp_value=new_value;
                            heuristic_to_apply = local_search[rng.nextInt(local_search.length)];
                            new_value=problem.applyHeuristic(heuristic_to_apply, 1, 1);
                            if(new_value<tmp_value){
                                times=0;
                            }
                            if(i%6==0&&hasTimeExpired())
                                times=times_set+1;
                        }

                        if(new_value<best_value){
                            problem.copySolution(1, 0);
                            best_value=new_value;
                        }else{
                            int selection=0;
                            do{
                                selection=rng.nextInt(m+rr+c+1);
                            }while(selection==0);

                            if(selection<=m){
                                heuristic_to_apply = mutation[rng.nextInt(m)];
                                best_value=problem.applyHeuristic(heuristic_to_apply,0,1);
                            }
                            else if(selection>m&&selection<=(m+rr)){
                                heuristic_to_apply = ruin_recreate[rng.nextInt(rr)];
                                best_value=problem.applyHeuristic(heuristic_to_apply,0,1);
                            }
                            else{
                                heuristic_to_apply = crossover[rng.nextInt(c)];
                                best_value=problem.applyHeuristic(heuristic_to_apply,0,1,1);
                            }
                        }
                    }
                }

            }else{

                problem.setMemorySize(number_of_heuristics+2);

                problem.initialiseSolution(number_of_heuristics);
                problem.initialiseSolution(number_of_heuristics+1);

                int[] crossover=problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER);
                int c=0;
                if(crossover!=null)
                    c=crossover.length;

                int[] tabu_table=new int[number_of_heuristics];
                int tabu_length=number_of_heuristics*2/3;
                double[] values=new double[number_of_heuristics];

                for(int i=0;i<number_of_heuristics;i++){
                    tabu_table[i]=0;
                    values[i]=0;
                }

                while (!hasTimeExpired()){

                    for(int i=0;i<number_of_heuristics;i++){
                        boolean h_is_crossover=false;
                        for(int j=0;j<c;j++){
                            if(i==crossover[j]){
                                h_is_crossover=true;
                                if(tabu_table[i]==0)
                                    values[i]=problem.applyHeuristic(i, number_of_heuristics,number_of_heuristics+1, i);
                                break;
                            }
                        }
                        if(!h_is_crossover&&tabu_table[i]==0)
                            values[i]=problem.applyHeuristic(i, number_of_heuristics, i);
                    }

                    int best_i=0;
                    int old_i=0;
                    double best_value=values[0];

                    for(int i=1;i<number_of_heuristics;i++)
                        if(values[i]!=0){
                            if(best_value==0){
                                best_value=values[i];
                                best_i=i;
                            }else if(values[i]<best_value){
                                best_value=values[i];
                                old_i=best_i;
                                best_i=i;
                            }
                        }

                    problem.copySolution(best_i, number_of_heuristics);
                    problem.copySolution(old_i, number_of_heuristics+1);
                    tabu_table[best_i]=tabu_length;

                     for(int i=0;i<number_of_heuristics;i++){
                         values[i]=0;
                         if(tabu_table[i]!=0){
                             tabu_table[i]--;
                         }

                     }
                }
            }
	}

	public String toString() {
		return "sa_ilsHyperHeuristic  :  ";
	}
}
