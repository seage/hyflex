package VRP;
public class RouteItem
{
	private Location currLocation;
	private RouteItem prev = null;
	private RouteItem next = null;
	private double timeArrived;
	private double waitingTime = 0;
	
	public RouteItem(Location cl, RouteItem p, RouteItem n, double ta)
	{
		setCurrLocation(cl);
		setPrev(p);
		setNext(n);
		setTimeArrived(ta);
	}
	
	public boolean compareRouteItem(RouteItem ri)
	{
		boolean identical = true;
		if(!(this.currLocation.compareLocation(ri.getCurrLocation()) && this.timeArrived==ri.getTimeArrived() && this.waitingTime==ri.getWaitingTime()))
		{
			identical = false;
		}
		return identical;
	}
	
	public void setCurrLocation(Location currLocation)
	{
		this.currLocation = currLocation;
	}

	public Location getCurrLocation()
	{
		return currLocation;
	}

	public void setPrev(RouteItem prev)
	{
		this.prev = prev;
	}

	public RouteItem getPrev()
	{
		return prev;
	}

	public void setNext(RouteItem next)
	{
		this.next = next;
	}

	public RouteItem getNext()
	{
		return next;
	}

	public void setTimeArrived(double timeArrived)
	{
		this.timeArrived = timeArrived;
	}

	public double getTimeArrived()
	{
		return timeArrived;
	}

	public void setWaitingTime(double waitingTime) {
		this.waitingTime = waitingTime;
	}

	public double getWaitingTime() {
		return waitingTime;
	}
}
