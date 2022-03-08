package VRP;
import java.util.ArrayList;

import AbstractClasses.ProblemDomain;


public class VRP extends ProblemDomain
{

	private Instance instance;
	private Solution []solutions;
	double bestSolutionValue = Double.POSITIVE_INFINITY;
	Solution bestSolution = new Solution();
	private final int[] mutations = new int[]{0,1};
	private final int[] ruinRecreates = new int[]{2,3};
	private final int[] localSearches = new int[]{4,5};
	private final int[] crossovers = new int[]{6,7};
	
	public VRP(long seed)
	{
		super(seed);
	}

	@Override
	public int[] getHeuristicsOfType(HeuristicType heuristicType)
	{
		switch (heuristicType)
		{
		case LOCAL_SEARCH : return localSearches;
		case RUIN_RECREATE : return ruinRecreates;
		case MUTATION : return mutations;
		case CROSSOVER : return crossovers;
		default: return null;
		}
	}

	@Override
	public int[] getHeuristicsThatUseIntensityOfMutation()
	{
		return new int[]{0,1,2,3};
	}

	@Override
	public int[] getHeuristicsThatUseDepthOfSearch()
	{
		return new int[]{4};
	}

	@Override
	public void loadInstance(int instanceID)
	{
		this.instance = new Instance(instanceID);
	}

	@Override
	public void setMemorySize(int size)
	{
		Solution[] newSolutionMemory = new Solution[size];
		if (solutions != null)
		{
			for (int x = 0; x < solutions.length; x++) {
				if (x < size)
				{
					newSolutionMemory[x] = solutions[x];
				}
			}
		}
		solutions = newSolutionMemory;
	}

	@Override
	public void initialiseSolution(int index)
	{
		this.solutions[index] = constructiveHeuristic(this.instance);
		getFunctionValue(index);
	}

	@Override
	public int getNumberOfHeuristics()
	{
		return 8;
	}

	@Override
	public double applyHeuristic(int heuristicID, int solutionSourceIndex,
			int solutionDestinationIndex)
	{
		long startTime = System.currentTimeMillis();
		boolean isCrossover = false;
		int[] crossovers = getHeuristicsOfType(HeuristicType.CROSSOVER);
		for (int x = 0; x < crossovers.length; x++)
		{
			if (crossovers[x] == heuristicID)
			{
				isCrossover = true;
				break;
			}
		}
		if (isCrossover)
		{
			solutions[solutionDestinationIndex] = solutions[solutionSourceIndex].copySolution();
			return getFunctionValue(solutionDestinationIndex);
		}
		else
		{
			double score = 0;
			if(heuristicID==0)
			{
				score = twoOpt(solutionSourceIndex,solutionDestinationIndex);
			}
			else if(heuristicID==1)
			{
				score = orOpt(solutionSourceIndex,solutionDestinationIndex);
			}
			else if(heuristicID==2)
			{
				score = locRR(solutionSourceIndex,solutionDestinationIndex);
			}
			else if(heuristicID==3)
			{
				score = timeRR(solutionSourceIndex,solutionDestinationIndex);
			}
			else if(heuristicID==4)
			{
				score = shift(solutionSourceIndex,solutionDestinationIndex);
			}
			else if(heuristicID==5)
			{
				score = interchange(solutionSourceIndex,solutionDestinationIndex);
			}
			else
			{
				System.err.println("Heuristic " + heuristicID + " does not exist");
				return 0;
			}
			heuristicCallRecord[heuristicID]++;
			heuristicCallTimeRecord[heuristicID] += (int)(System.currentTimeMillis() - startTime);
			return score;
		}
	}

	@Override
	public double applyHeuristic(int heuristicID, int solutionSourceIndex1,
			int solutionSourceIndex2, int solutionDestinationIndex)
	{
		long startTime = System.currentTimeMillis();
		boolean isCrossover = false;
		int[] crossovers = getHeuristicsOfType(HeuristicType.CROSSOVER);
		for (int x = 0; x < crossovers.length; x++)
		{
			if (crossovers[x] == heuristicID)
			{
				isCrossover = true;
				break;
			}
		}
		if (isCrossover)
		{
			double score = 0;
			if(heuristicID==6)
			{
				score = combine(solutionSourceIndex1,solutionSourceIndex2,solutionDestinationIndex);
			}
			else if(heuristicID==7)
			{
				score = combineLong(solutionSourceIndex1,solutionSourceIndex2,solutionDestinationIndex);
			}
			else
			{
				System.err.println("Heuristic " + heuristicID + " does not exist");
				return 0;
			}
			heuristicCallRecord[heuristicID]++;
			heuristicCallTimeRecord[heuristicID] += (int)(System.currentTimeMillis() - startTime);
			return score;
		}
		else
		{
			double score = 0;
			if(heuristicID==0)
			{
				score = twoOpt(solutionSourceIndex1,solutionDestinationIndex);
			}
			else if(heuristicID==1)
			{
				score = orOpt(solutionSourceIndex1,solutionDestinationIndex);
			}
			else if(heuristicID==2)
			{
				score = locRR(solutionSourceIndex1,solutionDestinationIndex);
			}
			else if(heuristicID==3)
			{
				score = timeRR(solutionSourceIndex1,solutionDestinationIndex);
			}
			else if(heuristicID==4)
			{
				score = shift(solutionSourceIndex1,solutionDestinationIndex);
			}
			else if(heuristicID==5)
			{
				score = interchange(solutionSourceIndex1,solutionDestinationIndex);
			}
			else
			{
				System.err.println("Heuristic " + heuristicID + " does not exist");
				return 0;
			}
			heuristicCallRecord[heuristicID]++;
			heuristicCallTimeRecord[heuristicID] += (int)(System.currentTimeMillis() - startTime);
			return score;
		}
	}
	
	Solution constructiveHeuristic(Instance i)
	{
		ArrayList<Location> locations = new ArrayList<Location>();
		ArrayList<Location> tempLocs = i.getDemands();
		for(int j=0; j<tempLocs.size(); j++)
		{
			locations.add(tempLocs.get(j).copyLocation());
		}
		ArrayList<Route> routes = new ArrayList<Route>();
		Location depot = locations.get(0);
		locations.remove(0);
		int numRoutes = 1;
		Route route = new Route(depot,(numRoutes-1),0);
		routes.add(route);
		while(!locations.isEmpty())
		{
			int bestIndex = -1;
			double bestScore = 1000000;
			double bestTimeMinusReady = 0;
			for(int j=0; j<locations.size(); j++)
			{
				if((routes.get(numRoutes-1).getVolume()+locations.get(j).getDemand())<i.getVehicleCapacity())
				{
					double diff1 = ((routes.get(numRoutes-1).getLast().getCurrLocation().getDueDate())-(locations.get(j).getReadyTime()+locations.get(j).getServiceTime()+calcDistance(locations.get(j),routes.get(numRoutes-1).getLast().getCurrLocation())));
					if(diff1>0)
					{
						RouteItem lastStop = routes.get(numRoutes-1).getLast().getPrev();
						Location prevLocation = lastStop.getCurrLocation();
						double dist = calcDistance(prevLocation,locations.get(j));
						int due = locations.get(j).getDueDate();
						double lastTime = lastStop.getTimeArrived();
						double timeDiff = (due)-(lastTime+prevLocation.getServiceTime()+dist);
						if(timeDiff>=0)
						{
							int readyDueDiff = due - locations.get(j).getReadyTime();
							if(diff1>=(readyDueDiff-timeDiff))
							{
								double score = (dist+(due-lastTime))*rng.nextFloat();
								if(score<bestScore)
								{
									bestIndex=j;
									bestScore=score;
									bestTimeMinusReady = timeDiff-readyDueDiff;
								}
							}
						}
					}
				}
				if(j==(locations.size()-1))
				{
					if(bestIndex>=0)
					{
						RouteItem lastStop = routes.get(numRoutes-1).getLast().getPrev();
						Location prevLocation = lastStop.getCurrLocation();
						if((bestTimeMinusReady)>0)
						{
							lastStop.setWaitingTime(bestTimeMinusReady);
						}
						else
						{
							lastStop.setWaitingTime(0);
						}
						routes.get(numRoutes-1).addPenultimate(locations.get(bestIndex), lastStop.getTimeArrived()+prevLocation.getServiceTime()+lastStop.getWaitingTime()+calcDistance(prevLocation,locations.get(bestIndex)));
						locations.remove(bestIndex);
						break;
					}
					numRoutes++;
					Route route2 = new Route(depot,(numRoutes-1),0);
					routes.add(route2);
					//System.out.println("Num routes = " + numRoutes + "and locations = " + locations.size());
				}
			}
		}
		Solution solution = new Solution(routes);
		//System.out.println("Size = " + routes.size());
		return solution;
	}
	
	public double twoOpt(int solutionSourceIndex, int solutionDestinationIndex)
	{
		Solution copyS = solutions[solutionSourceIndex].copySolution();
		ArrayList<Route> rs = copyS.getRoutes();
		int numRoutesToBeMutated = (int)(this.intensityOfMutation*rs.size());
		int []routesToBeMutated = new int[numRoutesToBeMutated];
		for(int j=0; j<numRoutesToBeMutated; j++)
		{
			routesToBeMutated[j] = -1;
		}
		for(int i=0; i<numRoutesToBeMutated; i++)
		{
			int ref = rng.nextInt(rs.size());
			while(appears(routesToBeMutated,ref))
			{
				ref = rng.nextInt(rs.size());
			}
			routesToBeMutated[i] = ref;
		}
		for(int i=0; i<numRoutesToBeMutated; i++)
		{
			Route routeToMutate = rs.get(routesToBeMutated[i]);
			if(routeToMutate.sizeOfRoute()>=4)
			{
				int startRI;
				if(routeToMutate.sizeOfRoute()==4)
				{
					startRI = 0;
				}
				else
				{
					startRI = rng.nextInt(routeToMutate.sizeOfRoute()-4);
				}
				Route r2 = routeToMutate.copyRoute();
				RouteItem ri = r2.getFirst();
				for(int j=0; j<startRI; j++)
				{
					ri = ri.getNext();
				}
				RouteItem tRI = ri.getNext();
				ri.setNext(ri.getNext().getNext());
				ri.getNext().setPrev(ri);
				tRI.setNext(ri.getNext().getNext());
				tRI.setPrev(ri.getNext());
				ri.getNext().setNext(tRI);
				tRI.getNext().setPrev(tRI);
				boolean feasible = true;
				while((ri=ri.getNext()).getNext()!=null)
				{
					RouteItem prev = ri.getPrev();
					double diff = (ri.getCurrLocation().getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation())));
					if(diff>=0)
					{
						int readyDueDiff = ri.getCurrLocation().getDueDate()-ri.getCurrLocation().getReadyTime();
						if(diff>readyDueDiff)
						{
							prev.setWaitingTime(diff-readyDueDiff);
						}
						else
						{
							prev.setWaitingTime(0);
						}
						ri.setTimeArrived(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+prev.getWaitingTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation()));
					}
					else
					{
						feasible = false;
						break;
					}
				}
				RouteItem rL = r2.getLast();
				double depotDiff = (rL.getCurrLocation().getDueDate()-(rL.getPrev().getTimeArrived()+rL.getPrev().getCurrLocation().getServiceTime()+calcDistance(rL.getCurrLocation(),rL.getPrev().getCurrLocation())));
				if(depotDiff<0)
				{
					feasible = false;
				}
				if(feasible)
				{
					routeToMutate = r2;
					rs.set(routesToBeMutated[i], r2);
				}
			}
			
		}
		solutions[solutionDestinationIndex] = copyS;
		double func = getFunctionValue(solutionDestinationIndex);
		return func;
	}
	
	public double orOpt(int solutionSourceIndex, int solutionDestinationIndex)
	{
		Solution copyS = solutions[solutionSourceIndex].copySolution();
		ArrayList<Route> rs = copyS.getRoutes();
		int numRoutesToBeMutated = (int)(this.intensityOfMutation*rs.size());
		int []routesToBeMutated = new int[numRoutesToBeMutated];
		for(int j=0; j<numRoutesToBeMutated; j++)
		{
			routesToBeMutated[j] = -1;
		}
		for(int i=0; i<numRoutesToBeMutated; i++)
		{
			int ref = rng.nextInt(rs.size());
			while(appears(routesToBeMutated,ref))
			{
				ref = rng.nextInt(rs.size());
			}
			routesToBeMutated[i] = ref;
		}
		for(int i=0; i<numRoutesToBeMutated; i++)
		{
			Route routeToMutate = rs.get(routesToBeMutated[i]);
			if(routeToMutate.sizeOfRoute()>=6)
			{
				int startRI;
				if(routeToMutate.sizeOfRoute()==6)
				{
					startRI = 0;
				}
				else
				{
					startRI = rng.nextInt(routeToMutate.sizeOfRoute()-6);
				}
				Route r2 = routeToMutate.copyRoute();
				RouteItem ri = r2.getFirst();
				for(int j=0; j<startRI; j++)
				{
					ri = ri.getNext();
				}
				RouteItem tRI = ri.getNext();
				RouteItem tRI2 = ri;
				ri.setNext(ri.getNext().getNext().getNext());
				ri.getNext().setPrev(ri);
				ri=ri.getNext().getNext();
				ri.getNext().setPrev(tRI.getNext());
				tRI.getNext().setNext(ri.getNext());
				tRI.setPrev(ri);
				ri.setNext(tRI);
				ri=tRI2;
				boolean feasible = true;
				while((ri=ri.getNext()).getNext()!=null)
				{
					RouteItem prev = ri.getPrev();
					double diff = (ri.getCurrLocation().getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation())));
					if(diff>=0)
					{
						int readyDueDiff = ri.getCurrLocation().getDueDate()-ri.getCurrLocation().getReadyTime();
						if(diff>readyDueDiff)
						{
							prev.setWaitingTime(diff-readyDueDiff);
						}
						else
						{
							prev.setWaitingTime(0);
						}
						ri.setTimeArrived(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+prev.getWaitingTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation()));
					}
					else
					{
						feasible = false;
						break;
					}
				}
				RouteItem rL = r2.getLast();
				double depotDiff = (rL.getCurrLocation().getDueDate()-(rL.getPrev().getTimeArrived()+rL.getPrev().getCurrLocation().getServiceTime()+calcDistance(rL.getCurrLocation(),rL.getPrev().getCurrLocation())));
				if(depotDiff<0)
				{
					feasible = false;
				}
				if(feasible)
				{
					routeToMutate = r2;
					rs.set(routesToBeMutated[i], r2);
				}
			}
			
		}
		solutions[solutionDestinationIndex] = copyS;
		double func = getFunctionValue(solutionDestinationIndex);
		return func;
	}
	
	public double locRR(int solutionSourceIndex, int solutionDestinationIndex)
	{
		Solution copyS = solutions[solutionSourceIndex].copySolution();
		ArrayList<Route> rs = copyS.getRoutes();
		Route rChoice = rs.get(rng.nextInt(rs.size()));
		int routePos = (rng.nextInt(rChoice.sizeOfRoute()-1));
		RouteItem rii = rChoice.getFirst();
		for(int i=0; i<routePos; i++)
		{
			rii = rii.getNext();
		}
		Location baseLocation = rii.getCurrLocation();
		double furthest = 0;
		for(Route r: rs)
		{
			rii = r.getFirst();
			while(rii!=null)
			{
				if(calcDistance(rii.getCurrLocation(),baseLocation)>furthest)
					furthest = calcDistance(rii.getCurrLocation(),baseLocation);
				rii = rii.getNext();
			}
		}
		double distanceWindow = (this.intensityOfMutation*(3*(furthest/4)));
		ArrayList<Location> locs = new ArrayList<Location>();
		ArrayList<Route> routesToDelete = new ArrayList<Route>();
		for(Route r: rs)
		{
			RouteItem ri = r.getFirst();
			while((ri=ri.getNext()).getNext()!=null)
			{
				double dist = calcDistance(ri.getCurrLocation(),baseLocation);
				if(dist<distanceWindow)
				{
					locs.add(ri.getCurrLocation());
					ri.getPrev().setNext(ri.getNext());
					ri.getNext().setPrev(ri.getPrev());
				}
			}
			if(r.sizeOfRoute()<=2)
			{
				routesToDelete.add(r);
			}
			else
			{
				ri = r.getFirst();
				while((ri=ri.getNext()).getNext()!=null)
				{
					RouteItem prev = ri.getPrev();
					double diff = (ri.getCurrLocation().getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation())));
					int readyDueDiff = ri.getCurrLocation().getDueDate()-ri.getCurrLocation().getReadyTime();
					if(diff>readyDueDiff)
					{
						prev.setWaitingTime(diff-readyDueDiff);
					}
					else
					{
						prev.setWaitingTime(0);
					}
					ri.setTimeArrived(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+prev.getWaitingTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation()));
				}
			}
		}
		for(Route r: routesToDelete)
		{
			rs.remove(r);
		}
		for(Location l: locs)
		{
			rs = insertLocIntoRoute(rs,l);
		}
		solutions[solutionDestinationIndex] = copyS;
		double func = getFunctionValue(solutionDestinationIndex);
		return func;
	}
	
	public double timeRR(int solutionSourceIndex, int solutionDestinationIndex)
	{
		Solution copyS = solutions[solutionSourceIndex].copySolution();
		ArrayList<Route> rs = copyS.getRoutes();
		int timeRef = rng.nextInt(rs.get(0).getLast().getCurrLocation().getDueDate());
		double timeWindow = (this.intensityOfMutation*(rs.get(0).getLast().getCurrLocation().getDueDate()/2));
		ArrayList<Location> locs = new ArrayList<Location>();
		ArrayList<Route> routesToDelete = new ArrayList<Route>();
		for(Route r: rs)
		{
			RouteItem ri = r.getFirst();
			while((ri=ri.getNext()).getNext()!=null)
			{
				double timeDiff = Math.abs(ri.getTimeArrived()-timeRef);
				if(timeDiff<timeWindow)
				{
					locs.add(ri.getCurrLocation());
					ri.getPrev().setNext(ri.getNext());
					ri.getNext().setPrev(ri.getPrev());
				}
			}
			if(r.sizeOfRoute()<=2)
			{
				routesToDelete.add(r);
			}
			else
			{
				ri = r.getFirst();
				while((ri=ri.getNext()).getNext()!=null)
				{
					RouteItem prev = ri.getPrev();
					double diff = (ri.getCurrLocation().getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation())));
					int readyDueDiff = ri.getCurrLocation().getDueDate()-ri.getCurrLocation().getReadyTime();
					if(diff>readyDueDiff)
					{
						prev.setWaitingTime(diff-readyDueDiff);
					}
					else
					{
						prev.setWaitingTime(0);
					}
					ri.setTimeArrived(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+prev.getWaitingTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation()));
				}
			}
		}
		for(Route r: routesToDelete)
		{
			rs.remove(r);
		}
		for(Location l: locs)
		{
			rs = insertLocIntoRoute(rs,l);
		}
		solutions[solutionDestinationIndex] = copyS;
		double func = getFunctionValue(solutionDestinationIndex);
		return func;
	}
	
	public double shift(int solutionSourceIndex, int solutionDestinationIndex)
	{
		Solution copyS = solutions[solutionSourceIndex].copySolution();
		Solution copyS2 = solutions[solutionSourceIndex].copySolution();
		ArrayList<Route> rs = copyS.getRoutes();
		int numRoutesToUse = (int)(this.depthOfSearch*rs.size());
		if(numRoutesToUse<1)
			numRoutesToUse=1;
		int []routesToUse = new int[numRoutesToUse];
		for(int i=0; i<numRoutesToUse; i++)
			routesToUse[i]=-1;
		for(int i=0; i<numRoutesToUse; i++)
		{
			int r = rng.nextInt(numRoutesToUse);
			while(appears(routesToUse,r))
				r = rng.nextInt(numRoutesToUse);
			routesToUse[i]=rs.get(r).getId();	
		}
		for(int i=0; i<numRoutesToUse; i++)
		{
			ArrayList<Route> routes = copyS2.getRoutes();
			boolean there = false;
			Route r = routes.get(0);
			for(int m=0; m<routes.size(); m++)
			{
				if(routes.get(m).getId()==routesToUse[i])
				{
					r = routes.get(m);
					there = true;
				}
			}
			if(!there)
				continue;
			double firstFunc = calcFunction(routes);
			int bestPos = 1;
			double greatestDistance = 0;
			int pos = 0;
			RouteItem ri = r.getFirst();
			while((ri=ri.getNext()).getNext()!=null)
			{
				pos++;
				double dist = (calcDistance(ri.getPrev().getCurrLocation(),ri.getCurrLocation())+calcDistance(ri.getNext().getCurrLocation(),ri.getCurrLocation()))*rng.nextFloat();
				if(dist>greatestDistance)
				{
					greatestDistance=dist;
					bestPos=pos;
				}
			}
			ri = r.getFirst();
			for(int j=0; j<bestPos; j++)
			{
				ri = ri.getNext();
			}
			Location locToInsert = ri.getCurrLocation();
			ri.getPrev().setNext(ri.getNext());
			ri.getNext().setPrev(ri.getPrev());
			ri = ri.getPrev();
			if(r.sizeOfRoute()<=2)
			{
				routes.remove(r);
			}
			else
			{
				while((ri=ri.getNext()).getNext()!=null)
				{
					RouteItem prev = ri.getPrev();
					double diff = (ri.getCurrLocation().getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation())));
					int readyDueDiff = ri.getCurrLocation().getDueDate()-ri.getCurrLocation().getReadyTime();
					if(diff>readyDueDiff)
					{
						prev.setWaitingTime(diff-readyDueDiff);
					}
					else
					{
						prev.setWaitingTime(0);
					}
					ri.setTimeArrived(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+prev.getWaitingTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation()));
				}
			}
			routes = insertLocIntoRoute(routes,locToInsert);
			double newFunc = calcFunction(routes);
			if(newFunc>=firstFunc)
			{
				copyS2.setRoutes(copyS.copySolution().getRoutes());
			}
			else
			{
				copyS.setRoutes(routes);
				copyS = copyS.copySolution();
			}
		}
		solutions[solutionDestinationIndex] = copyS;
		double func = getFunctionValue(solutionDestinationIndex);
		return func;
	}
	
	public double interchange(int solutionSourceIndex, int solutionDestinationIndex)
	{
		Solution copyS = solutions[solutionSourceIndex].copySolution();
		Solution copyS2 = solutions[solutionSourceIndex].copySolution();
		ArrayList<Route> rs = copyS.getRoutes();
		//Get two routes for interchange
		Route r1 = rs.get(rng.nextInt(rs.size()));
		Route r2 = rs.get(rng.nextInt(rs.size()));
		//Ensure routes are different
		while(r1==r2)
		{
			r2 = rs.get(rng.nextInt(rs.size()));
		}
		//Get two locations to interchange based upon a time and distance matrix
		RouteItem ri = r1.getFirst();
		double tdScore = 1000000;
		RouteItem ri1 = r1.getFirst().getNext();
		RouteItem ri2 = r2.getFirst().getNext();
		for(int i=0; i<(r1.sizeOfRoute()-2); i++)
		{
			ri = ri.getNext();
			RouteItem rij = r2.getFirst();
			for(int j=0; j<(r2.sizeOfRoute()-2); j++)
			{
				rij = rij.getNext();
				double matrix = calcDistance(ri.getCurrLocation(),rij.getCurrLocation())+Math.abs(ri.getCurrLocation().getDueDate()-rij.getCurrLocation().getDueDate());
				if(matrix<tdScore)
				{
					tdScore = matrix;
					ri1 = ri;
					ri2 = rij;
				}
			}
		}
		//Remove locations and fix pointers
		Location loc1 = ri1.getCurrLocation();
		Location loc2 = ri2.getCurrLocation();
		ri1.getPrev().setNext(ri1.getNext());
		ri1.getNext().setPrev(ri1.getPrev());
		ri2.getPrev().setNext(ri2.getNext());
		ri2.getNext().setPrev(ri2.getPrev());
		r1 = reOptimise(r1);
		r2 = reOptimise(r2);
		//Insert location 1 in the best possible place in route 2, or create new route if neccesary
		double score = 1000000;
		double bestPos = -1;
		ri = r2.getFirst();
		int routeElemPosition = 0;
		while((ri=ri.getNext()).getNext()!=null)
		{
			routeElemPosition++;
			if(checkFeasibility(r2,routeElemPosition,loc1))
			{
				double matrix = calcDistance(ri.getPrev().getCurrLocation(),loc1)+Math.abs(ri.getPrev().getCurrLocation().getDueDate()-loc1.getDueDate());
				if(matrix<score)
				{
					score = matrix;
					bestPos = routeElemPosition;
				}
			}
		}
		if(bestPos!=-1)
		{
			ri = r2.getFirst();
			for(int i=0; i<routeElemPosition; i++)
			{
				ri = ri.getNext();
			}
			RouteItem newRI = new RouteItem(loc1,ri.getPrev(),ri,0);
			ri.getPrev().setNext(newRI);
			ri.setPrev(newRI);
			r2 = reOptimise(r2);
		}
		else
		{
			Route newR = new Route(r2.getFirst().getCurrLocation(),rs.size(),0);
			ri = newR.getFirst();
			RouteItem newRI = new RouteItem(loc1,ri,ri.getNext(),0);
			ri.getNext().setPrev(newRI);
			ri.setNext(newRI);
			newR = reOptimise(newR);
			rs.add(newR);
		}
		//Insert location 2 in the best possible place in route 1, or create new route if neccesary
		score = 1000000;
		bestPos = -1;
		ri = r1.getFirst();
		routeElemPosition = 0;
		while((ri=ri.getNext()).getNext()!=null)
		{
			routeElemPosition++;
			if(checkFeasibility(r1,routeElemPosition,loc2))
			{
				double matrix = calcDistance(ri.getPrev().getCurrLocation(),loc2)+Math.abs(ri.getPrev().getCurrLocation().getDueDate()-loc2.getDueDate());
				if(matrix<score)
				{
					score = matrix;
					bestPos = routeElemPosition;
				}
			}
		}
		if(bestPos!=-1)
		{
			ri = r1.getFirst();
			for(int i=0; i<routeElemPosition; i++)
			{
				ri = ri.getNext();
			}
			RouteItem newRI = new RouteItem(loc2,ri.getPrev(),ri,0);
			ri.getPrev().setNext(newRI);
			ri.setPrev(newRI);
			r1 = reOptimise(r1);
		}
		else
		{
			Route newR = new Route(r1.getFirst().getCurrLocation(),rs.size(),0);
			ri = newR.getFirst();
			RouteItem newRI = new RouteItem(loc1,ri,ri.getNext(),0);
			ri.getNext().setPrev(newRI);
			ri.setNext(newRI);
			newR = reOptimise(newR);
			rs.add(newR);
		}

		double oldFunc = calcFunction(copyS2.getRoutes());
		double newFunc = calcFunction(copyS.getRoutes());
		if(newFunc<oldFunc)
		{
		   solutions[solutionDestinationIndex] = copyS;
		   if(newFunc<bestSolutionValue)
		   {
		       bestSolutionValue = newFunc;
		       bestSolution = copyS.copySolution();
		   }
		   return newFunc;
		}
		else
		{
		   solutions[solutionDestinationIndex] = copyS2;
		   return oldFunc;
		} 
	}
	
	public double combine(int solutionSourceIndex1, int solutionSourceIndex2, int solutionDestinationIndex)
	{
		//Get copies of source solutions and routes
		Solution copyS1 = solutions[solutionSourceIndex1].copySolution();
		Solution copyS2 = solutions[solutionSourceIndex2].copySolution();
		ArrayList<Route> rs1 = copyS1.getRoutes();
		ArrayList<Route> rs2 = copyS2.getRoutes();
		//List of all locations, for reference
		ArrayList<Integer> locations = new ArrayList<Integer>();
		for(Route r: rs1)
		{
			RouteItem ri = r.getFirst();
			while((ri=ri.getNext()).getNext()!=null)
			{
				locations.add(ri.getCurrLocation().getId());
			}
		}
		//Choose rough percentage of routes to keep from chosen solution (between 25-75%)
		int rand = rng.nextInt(50);
		double perc = (((double)(75-rand))/100);
		//Choose which solution to take routes from
		rand = rng.nextInt(2);
		ArrayList<Route> chosenOnes;
		ArrayList<Route> others;
		if(rand==0)
		{
			chosenOnes = rs1;
			others = rs2;
		}
		else
		{
			chosenOnes = rs2;
			others = rs1;
		}
		//New solution
		ArrayList<Route> newRoutes = new ArrayList<Route>();
		ArrayList<Integer> addedLocations = new ArrayList<Integer>();
		//Based on the percentage above, choose some routes to be included in the new solution
		for(Route r: chosenOnes)
		{
			if((rng.nextFloat())<perc)
			{
				newRoutes.add(r);
				RouteItem ri = r.getFirst();
				while((ri=ri.getNext()).getNext()!=null)
				{
					addedLocations.add(ri.getCurrLocation().getId());
				}
			}
		}
		//Add any routes from the other routes that don't have any conflicting locations
		for(Route r: others)
		{
			if(useableRoute(r,addedLocations))
			{
				newRoutes.add(r);
				RouteItem ri = r.getFirst();
				while((ri=ri.getNext()).getNext()!=null)
				{
					addedLocations.add(ri.getCurrLocation().getId());
				}
			}
		}
		//Insert remaining locations into solution in the order they appear in others
		for(Route r: others)
		{
			RouteItem ri = r.getFirst();
			while((ri=ri.getNext()).getNext()!=null)
			{
				if(!containsID(ri.getCurrLocation().getId(),addedLocations))
				{
					addedLocations.add(ri.getCurrLocation().getId());
					newRoutes = insertLocIntoRoute(newRoutes, ri.getCurrLocation());
				}
			}
		}
		//Copy new solutions to destination place
		Solution newSolution = new Solution(newRoutes);
		solutions[solutionDestinationIndex] = newSolution;
		double func = getFunctionValue(solutionDestinationIndex);
		return func;
	}
	
	public double combineLong(int solutionSourceIndex1, int solutionSourceIndex2, int solutionDestinationIndex)
	{
		//Get copies of source solutions and routes
		Solution copyS1 = solutions[solutionSourceIndex1].copySolution();
		Solution copyS2 = solutions[solutionSourceIndex2].copySolution();
		ArrayList<Route> rs1 = copyS1.getRoutes();
		ArrayList<Route> rs2 = copyS2.getRoutes();
		//List of locations added to new solution
		ArrayList<Integer> addedLocations = new ArrayList<Integer>();
		//New list of routes
		ArrayList<Route> newRoutes = new ArrayList<Route>();
		//Ordered list of routes by length
		ArrayList<Route> orderedRoutes = new ArrayList<Route>();
		//Insert routes from first set
		for(Route r: rs1)
		{
			for(int i=0; i<orderedRoutes.size();i++)
			{
				if(r.sizeOfRoute()>orderedRoutes.get(i).sizeOfRoute())
				{
					orderedRoutes.add(i,r);
					break;
				}
				if(i==(orderedRoutes.size()-1))
				{
					orderedRoutes.add(r);
					break;
				}
			}
			if(orderedRoutes.size()==0)
			{
				orderedRoutes.add(r);
			}
		}
		//Insert routes from second set
		for(Route r: rs2)
		{
			for(int i=0; i<orderedRoutes.size();i++)
			{
				if(r.sizeOfRoute()>orderedRoutes.get(i).sizeOfRoute())
				{
					orderedRoutes.add(i,r);
					break;
				}
				if(i==(orderedRoutes.size()-1))
				{
					orderedRoutes.add(r);
					break;
				}
			}
			if(orderedRoutes.size()==0)
			{
				orderedRoutes.add(r);
			}
		}
		//Pick non-conflicting routes to include in solution, searching from largest to smallest
		for(Route r: orderedRoutes)
		{
			if(useableRoute(r,addedLocations))
			{
				newRoutes.add(r);
				RouteItem ri = r.getFirst();
				while((ri=ri.getNext()).getNext()!=null)
				{
					addedLocations.add(ri.getCurrLocation().getId());
				}
			}
		}
		//Pick route set at random to get remaining location order from
		int choose = rng.nextInt(2);
		ArrayList<Route> remainingLocsRoutes = rs1;
		if(choose==0)
			remainingLocsRoutes = rs2;
		//Insert remaining locations into new routes
		for(Route r: remainingLocsRoutes)
		{
			RouteItem ri = r.getFirst();
			while((ri=ri.getNext()).getNext()!=null)
			{
				if(!containsID(ri.getCurrLocation().getId(),addedLocations))
				{
					addedLocations.add(ri.getCurrLocation().getId());
					newRoutes = insertLocIntoRoute(newRoutes, ri.getCurrLocation());
				}
			}
		}
		//Copy new solutions to destination place
		Solution newSolution = new Solution(newRoutes);
		solutions[solutionDestinationIndex] = newSolution;
		double func = getFunctionValue(solutionDestinationIndex);
		return func;
	}
	
	public boolean containsID(int iD, ArrayList<Integer> ids)
	{
		for(Integer i: ids)
		{
			if(i==iD)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean useableRoute(Route r, ArrayList<Integer> ls)
	{
		RouteItem ri = r.getFirst();
		while((ri=ri.getNext()).getNext()!=null)
		{
			for(Integer i: ls)
			{
				if(i==ri.getCurrLocation().getId())
				{
					return false;
				}
			}
		}
		return true;
	}
	
	public Route reOptimise(Route r)
	{
		RouteItem ri = r.getFirst();
		while((ri=ri.getNext()).getNext()!=null)
		{
			RouteItem prev = ri.getPrev();
			double diff = (ri.getCurrLocation().getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation())));
			int readyDueDiff = ri.getCurrLocation().getDueDate()-ri.getCurrLocation().getReadyTime();
			if(diff>readyDueDiff)
			{
				prev.setWaitingTime(diff-readyDueDiff);
			}
			else
			{
				prev.setWaitingTime(0);
			}
			ri.setTimeArrived(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+prev.getWaitingTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation()));
		}
		return r;
	}
	
	private ArrayList<Route> insertLocIntoRoute(ArrayList<Route> rs, Location loc)
	{
		int routeElemPosition = 0;
		int bestRouteNum = -1;
		int bestRouteElemPosition = 0;
		double bestWaitingTime = 1000000;
		for(int i=0; i<rs.size(); i++)
		{
			routeElemPosition = 0;
			RouteItem ri = rs.get(i).getFirst();
			while((ri = ri.getNext()).getNext()!=null)
			{
				routeElemPosition++;
				if(checkFeasibility(rs.get(i),routeElemPosition,loc))
				{
					RouteItem prev = ri.getPrev();
					double timeDiff = (loc.getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(loc,prev.getCurrLocation())));
					int readyDueDiff = loc.getDueDate()-loc.getReadyTime();
					if(timeDiff>readyDueDiff)
					{
						if(bestWaitingTime>(timeDiff-readyDueDiff+calcDistance(loc,prev.getCurrLocation())))
						{
							bestWaitingTime = (timeDiff-readyDueDiff+calcDistance(loc,prev.getCurrLocation()));
							bestRouteNum = i;
							bestRouteElemPosition = routeElemPosition;
						}
					}
					else
					{
						if(bestWaitingTime>calcDistance(loc,prev.getCurrLocation()))
						{
							bestWaitingTime = calcDistance(loc,prev.getCurrLocation());
							bestRouteNum = i;
							bestRouteElemPosition = routeElemPosition;
						}
					}
				}
			}
		}
		if(bestRouteNum==-1)
		{
			Route newR = new Route(rs.get(0).getFirst().getCurrLocation(),rs.size(),0);
			RouteItem ri = newR.getFirst();
			double timeDiff = (loc.getDueDate()-(ri.getTimeArrived()+ri.getCurrLocation().getServiceTime()+calcDistance(loc,ri.getCurrLocation())));
			int readyDueDiff = loc.getDueDate()-loc.getReadyTime();
			if(timeDiff>readyDueDiff)
			{
				ri.setWaitingTime(timeDiff-readyDueDiff);
			}
			else
			{
				ri.setWaitingTime(0);
			}
			RouteItem newRI = new RouteItem(loc,ri,ri.getNext(),ri.getTimeArrived()+ri.getCurrLocation().getServiceTime()+ri.getWaitingTime()+calcDistance(loc,ri.getCurrLocation()));
			ri.getNext().setPrev(newRI);
			ri.setNext(newRI);
			rs.add(newR);
		}
		else
		{
			Route currR = rs.get(bestRouteNum);
			RouteItem ri = currR.getFirst();
			for(int i=0; i<bestRouteElemPosition; i++)
			{
				ri = ri.getNext();
			}
			RouteItem prev = ri.getPrev();
			double timeDiff = (loc.getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(loc,prev.getCurrLocation())));
			int readyDueDiff = loc.getDueDate()-loc.getReadyTime();
			if(timeDiff>readyDueDiff)
			{
				prev.setWaitingTime(timeDiff-readyDueDiff);
			}
			else
			{
				prev.setWaitingTime(0);
			}
			RouteItem newRI = new RouteItem(loc,prev,prev.getNext(),prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+prev.getWaitingTime()+calcDistance(loc,prev.getCurrLocation()));
			prev.getNext().setPrev(newRI);
			prev.setNext(newRI);
			ri = newRI;
			while((ri=ri.getNext()).getNext()!=null)
			{
				prev = ri.getPrev();
				double diff = (ri.getCurrLocation().getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation())));
				readyDueDiff = ri.getCurrLocation().getDueDate()-ri.getCurrLocation().getReadyTime();
				if(diff>readyDueDiff)
				{
					prev.setWaitingTime(diff-readyDueDiff);
				}
				else
				{
					prev.setWaitingTime(0);
				}
				ri.setTimeArrived(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+prev.getWaitingTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation()));
			}
		}
		return rs;
	}
	
	private boolean checkFeasibility(Route r, int position, Location l)
	{
		if(r.calcVolume()+l.getDemand()>instance.getVehicleCapacity())
		{
			return false;
		}
		else
		{
			Route route = r.copyRoute();
			RouteItem ri = route.getFirst();
			for(int i=0; i<position; i++)
			{
				ri = ri.getNext();
			}
			RouteItem prev = ri.getPrev();
			double diff = (l.getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(l,prev.getCurrLocation())));
			if(diff<0)
			{
				return false;
			}
			int readyDueDiff = l.getDueDate()-l.getReadyTime();
			if(diff>readyDueDiff)
			{
				prev.setWaitingTime(diff-readyDueDiff);
			}
			else
			{
				prev.setWaitingTime(0);
			}
			RouteItem newRI = new RouteItem(l,prev,prev.getNext(),prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+prev.getWaitingTime()+calcDistance(l,prev.getCurrLocation()));
			prev.getNext().setPrev(newRI);
			prev.setNext(newRI);
			ri = newRI;
			while((ri=ri.getNext()).getNext()!=null)
			{
				prev = ri.getPrev();
				diff = (ri.getCurrLocation().getDueDate()-(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation())));
				readyDueDiff = ri.getCurrLocation().getDueDate()-ri.getCurrLocation().getReadyTime();
				if(diff<0)
					return false;
				if(diff>readyDueDiff)
				{
					prev.setWaitingTime(diff-readyDueDiff);
				}
				else
				{
					prev.setWaitingTime(0);
				}
				ri.setTimeArrived(prev.getTimeArrived()+prev.getCurrLocation().getServiceTime()+prev.getWaitingTime()+calcDistance(ri.getCurrLocation(),prev.getCurrLocation()));
			}
			ri = route.getLast();
			if((ri.getCurrLocation().getDueDate())<(ri.getPrev().getTimeArrived()+ri.getPrev().getCurrLocation().getServiceTime()+calcDistance(ri.getPrev().getCurrLocation(),ri.getCurrLocation())))
				return false;
		}
		return true;
	}
	
	private boolean appears(int []arr, int ref)
	{
		boolean appear = false;
		for(int i: arr)
		{
			if(i==ref)
			{
				appear=true;
				break;
			}
		}
		return appear;
	}

	@Override
	public void copySolution(int solutionSourceIndex,
			int solutionDestinationIndex)
	{
		solutions[solutionDestinationIndex] = solutions[solutionSourceIndex].copySolution();
	}

	@Override
	public String toString()
	{
		return "Vehicle Routing";
	}

	@Override
	public int getNumberOfInstances()
	{
		return 56;
	}

	@Override
	public String bestSolutionToString()
	{
		return printToString(bestSolution);
	}

	@Override
	public double getBestSolutionValue()
	{
		return this.bestSolutionValue;
	}

	@Override
	public String solutionToString(int solutionIndex)
	{
		return printToString(solutions[solutionIndex]);
	}
	
	private String printToString(Solution s)
	{
		ArrayList<Route> rs = s.getRoutes();
		String nL = System.getProperty("line.separator");
		String printedString = "";
		for(int i=0; i<rs.size(); i++)
		{
			printedString = (printedString+"Route "+i+nL);
			RouteItem rI = rs.get(i).getFirst();
			while(rI!=null)
			{
				printedString = (printedString+"Location "+rI.getCurrLocation().getId()+" visited at "+rI.getTimeArrived()+nL);
				rI = rI.getNext();
			}
		}
		return printedString;
	}

	@Override
	public double getFunctionValue(int solutionIndex)
	{
		ArrayList<Route> routes = solutions[solutionIndex].getRoutes();
		double value = calcFunction(routes);
		if(value<bestSolutionValue)
		{
			bestSolutionValue = value;
			bestSolution = solutions[solutionIndex].copySolution();
		}
		return value;
	}
	
	public double calcFunction(ArrayList<Route> rs)
	{
		ArrayList<Route> routes = rs;
		int numRs = routes.size();
		double distance = 0;
		for(Route r: routes)
		{
			RouteItem rItem = r.getFirst();
			while(rItem.getNext()!=null)
			{
				distance += calcDistance(rItem.getCurrLocation(),rItem.getNext().getCurrLocation());
				rItem = rItem.getNext();
			}
		}
		double value = (1000*numRs)+distance;
		return value;
	}

	@Override
	public boolean compareSolutions(int solutionIndex1, int solutionIndex2)
	{
		ArrayList<Route> rs1 = solutions[solutionIndex1].getRoutes();
		ArrayList<Route> rs2 = solutions[solutionIndex2].getRoutes();
		for(int i=0; i<rs1.size(); i++)
		{
			if(!(rs1.get(i).compareRoute(rs2.get(i))))
			{
				return false;
			}
		}
		return true;
	}
	
	public void traverseBack(Solution s)
	{
		ArrayList<Route> rs = s.getRoutes();
		for(Route r: rs)
		{
			RouteItem ri = r.getLast();
			while((ri=ri.getPrev())!= null)
			{
				System.out.println(ri.getCurrLocation().getId());
			}
		}
	}
	
	double calcDistance(Location l1, Location l2)
	{
		int xdiff = Math.abs(l1.getXCoord()-l2.getXCoord());
		int ydiff = Math.abs(l1.getYCoord()-l2.getYCoord());
		return Math.sqrt((xdiff*xdiff)+(ydiff*ydiff));
	}
	
	public static void main(String []args)
	{
		VRP vrp = new VRP(4234);
		vrp.loadInstance(0);
		vrp.setMemorySize(3);
		vrp.initialiseSolution(0);
		System.out.println(vrp.getFunctionValue(0));
		vrp.initialiseSolution(1);
		System.out.println(vrp.getFunctionValue(1));
		vrp.setIntensityOfMutation(0.1);
		vrp.setDepthOfSearch(1);
		System.out.println(vrp.applyHeuristic(6,0,1,2));
		//System.out.println(vrp.solutionToString(0));
		//System.out.println(vrp.solutionToString(1));
		//System.out.println(vrp.solutionToString(2));
	}

}
