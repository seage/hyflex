package VRP;
import java.util.ArrayList;

public class Solution
{
	private ArrayList<Route> routes = new ArrayList<Route>();

	public Solution(ArrayList<Route> rs)
	{
		setRoutes(rs);
	}
	
	public Solution()
	{
	}
	
	public void setRoutes(ArrayList<Route> routes)
	{
		this.routes = routes;
	}

	public ArrayList<Route> getRoutes()
	{
		return routes;
	}
	
	public Solution copySolution()
	{
		ArrayList<Route> newRoutes = new ArrayList<Route>();
		for(Route r: routes)
		{
			newRoutes.add(r.copyRoute());
		}
		return new Solution(newRoutes);
	}
}
