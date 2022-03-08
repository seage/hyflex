package VRP;
public class Location
{
	private int id;
	private int xCoord;
	private int yCoord;
	private int demand;
	private int dueDate;
	private int readyTime;
	private int serviceTime;
	private boolean serviced = false;
	
	public Location(int iD, int xc, int yc, int d, int rt, int dd, int st)
	{
		setId(iD);
		setXCoord(xc);
		setYCoord(yc);
		setDemand(d);
		setDueDate(dd);
		setServiceTime(st);
		setReadyTime(rt);
	}

	public Location copyLocation()
	{
		return new Location(this.id, this.xCoord, this.yCoord, this.demand, this.readyTime, this.dueDate, this.serviceTime);
	}
	
	public boolean compareLocation(Location loc)
	{
		boolean identical = true;
		if(!(this.id==loc.getId() && this.xCoord==loc.getXCoord() && this.yCoord==loc.getYCoord() && this.demand==loc.getDemand() && this.dueDate==loc.getDueDate() && this.readyTime==loc.getReadyTime() && this.serviceTime==loc.getServiceTime()))
		{
			identical = false;
		}
		return identical;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}

	public int getId()
	{
		return id;
	}

	public void setXCoord(int xCoord)
	{
		this.xCoord = xCoord;
	}

	public int getXCoord()
	{
		return xCoord;
	}

	public void setYCoord(int yCoord)
	{
		this.yCoord = yCoord;
	}

	public int getYCoord()
	{
		return yCoord;
	}

	public void setDemand(int demand)
	{
		this.demand = demand;
	}

	public int getDemand()
	{
		return demand;
	}

	public void setDueDate(int dueDate)
	{
		this.dueDate = dueDate;
	}

	public int getDueDate()
	{
		return dueDate;
	}

	public void setServiceTime(int serviceTime)
	{
		this.serviceTime = serviceTime;
	}

	public int getServiceTime()
	{
		return serviceTime;
	}

	public void setServiced(boolean serviced)
	{
		this.serviced = serviced;
	}

	public boolean isServiced()
	{
		return serviced;
	}

	public void setReadyTime(int readyTime) {
		this.readyTime = readyTime;
	}

	public int getReadyTime() {
		return readyTime;
	}
	
	
}
