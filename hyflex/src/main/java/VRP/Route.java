package VRP;
public class Route
{
	private int id;
	private RouteItem first = null;
	private RouteItem last = null;
	private int volume = 0;
	
	public Route(Location l, int iD, int t)
	{
		setId(iD);
		RouteItem depot = new RouteItem(l, null, null, 0);
		first = depot;
		RouteItem depot2 = new RouteItem(l, first, null, t);
		last = depot2;
		first.setNext(last);
	}
	
	public Route copyRoute()
	{
		Route newR = new Route(first.getCurrLocation().copyLocation(), this.id, 0);
		newR.getFirst().setWaitingTime(this.first.getWaitingTime());
		RouteItem currRI = first;
		RouteItem currNewRI = newR.getFirst();
		while((currRI=currRI.getNext()).getNext()!=null)
		{
			currNewRI.setNext(new RouteItem(currRI.getCurrLocation().copyLocation(),currNewRI,currNewRI.getNext(),currRI.getTimeArrived()));
			currNewRI = currNewRI.getNext();
			currNewRI.getNext().setPrev(currNewRI);
			currNewRI.setWaitingTime(currRI.getWaitingTime());
		}
		newR.setVolume(this.volume);
		return newR;
	}
	
	public boolean compareRoute(Route r)
	{
		boolean identical = true;
		RouteItem thisRI = first;
		RouteItem thatRI = r.getFirst();
		while(thisRI!=null)
		{
			if(!(thisRI.compareRouteItem(thatRI)))
			{
				return false;
			}
			thisRI = thisRI.getNext();
			thatRI = thatRI.getNext();
		}
		if(!(this.id==r.getId() && this.volume==r.getVolume()))
		{
			return false;
		}
		return identical;
	}
	
	public void addPenultimate(Location l, double t)
	{
		RouteItem ri = new RouteItem(l, last.getPrev(), last, t);
		last.getPrev().setNext(ri);
		last.setPrev(ri);
		volume+=l.getDemand();
	}
	
	public void insertAfter(RouteItem ri, Location l, double t)
	{
		if(ri.getNext()==null)
		{
			System.out.println("Last location must be depot");
		}
		else
		{
			RouteItem r = new RouteItem(l,ri,ri.getNext(),t);
			ri.getNext().setPrev(r);
			ri.setNext(r);
			volume+=l.getDemand();
		}
	}
	
	public void removeRouteItem(RouteItem ri)
	{
		if((ri.getPrev()==null) || (ri.getNext()==null))
		{
			System.out.println("Cannot delete depot");
		}
		else
		{
			ri.getPrev().setNext(ri.getNext());
			ri.getNext().setPrev(ri.getPrev());
			volume-=ri.getCurrLocation().getDemand();
		}
	}
	
	public void printRoute()
	{
		RouteItem currItem = first;
		while(currItem!=null)
		{
			Location loc = currItem.getCurrLocation();
			System.out.println("Location " + loc.getId() + " at (" + loc.getXCoord() + "," + loc.getYCoord() + ") has been visited at " + currItem.getTimeArrived());
			currItem = currItem.getNext();
		}
		System.out.println(this.volume);
	}
	
	public int sizeOfRoute()
	{
		int size = 1;
		RouteItem curr = first;
		while((curr=curr.getNext())!=null)
		{
			size++;
		}
		return size;
	}
	
	public int calcVolume()
	{
		RouteItem ri = this.first;
		int volume = 0;
		while(ri!=null)
		{
			volume += ri.getCurrLocation().getDemand();
			ri = ri.getNext();
		}
		return volume;
	}

	public RouteItem getFirst() {
		return first;
	}

	public void setFirst(RouteItem first) {
		this.first = first;
	}

	public RouteItem getLast() {
		return last;
	}

	public void setLast(RouteItem last) {
		this.last = last;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
