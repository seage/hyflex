package VRP;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Instance
{
	private ArrayList<Location> demands = new ArrayList<Location>();
	private String instanceName;
	private int vehicleNumber;
	private int vehicleCapacity;
	private Location depot;

	public Instance(int id)
	{

		String fileName = "data/vrp/";
		if(id==0)
		{
			fileName += ("Solomon_100_customer_instances/RC/RC207.txt");
		}
		else if(id==1)
		{
			fileName += ("Solomon_100_customer_instances/R/R101.txt");
		}
		else if(id==2)
		{
			fileName += ("Solomon_100_customer_instances/RC/RC103.txt");
		}
		else if(id==3)
		{
			fileName += ("Solomon_100_customer_instances/R/R201.txt");
		}
		else if(id==4)
		{
			fileName += ("Solomon_100_customer_instances/R/R106.txt");
		}
		else if(id==5)
		{
			fileName += ("Homberger_1000_customer_instances/C/C1_10_1.TXT");
		}
		else if(id==6)
		{
			fileName += ("Homberger_1000_customer_instances/RC/RC2_10_1.TXT");
		}
		else if(id==7)
		{
			fileName += ("Homberger_1000_customer_instances/R/R1_10_1.TXT");
		}
		else if(id==8)
		{
			fileName += ("Homberger_1000_customer_instances/C/C1_10_8.TXT");
		}
		else if(id==9)
		{
			fileName += ("Homberger_1000_customer_instances/RC/RC1_10_5.TXT");
		}
		
		BufferedReader reader = null;
		try {
			FileReader read = new FileReader(fileName);
			reader = new BufferedReader(read);
		} catch (FileNotFoundException a) {
			try {
				InputStream fis = this.getClass().getClassLoader().getResourceAsStream(fileName); 
				reader = new BufferedReader(new InputStreamReader(fis));
			} catch(NullPointerException n) {
				System.err.println("cannot find file " + fileName);
				System.exit(-1);
			}
		}//end catch
		
			
		try
		{
			instanceName = reader.readLine();
			reader.readLine();
			reader.readLine();
			reader.readLine();
			StringTokenizer info = new StringTokenizer(reader.readLine());
			vehicleNumber = Integer.parseInt(info.nextToken());
			vehicleCapacity = Integer.parseInt(info.nextToken());
			reader.readLine();
			reader.readLine();
			reader.readLine();
			reader.readLine();
			String line = "";
			while((line = reader.readLine()) != null)
			{
				info = new StringTokenizer(line);
				Location loc = new Location(Integer.parseInt(info.nextToken()), Integer.parseInt(info.nextToken()), Integer.parseInt(info.nextToken()), Integer.parseInt(info.nextToken()), Integer.parseInt(info.nextToken()), Integer.parseInt(info.nextToken()), Integer.parseInt(info.nextToken()));
				demands.add(loc);
			}
			setDepot(demands.get(0));
			/*
			System.out.println("Instance name is " + instanceName + ", there are " + vehicleNumber + " vehicles with capacity of " + vehicleCapacity);
			for(int i=0; i<demands.size(); i++)
			{
				Location l = demands.get(i);
				System.out.println("For location " + l.getId() + ", at (" + l.getXCoord() + "," + l.getYCoord() + "), demand is " + l.getDemand() + ", ready time is " + l.getReadyTime() + ", due time is " + l.getDueDate() + " and service time is " + l.getServiceTime());
			}
			 */
		}
		catch(IOException e)
		{
			System.out.println("Exception found: " + e);
			System.out.println("Could not load instance, or instance does not exist");
			System.exit(-1);
		}
	}

	public ArrayList<Location> getDemands() {
		return demands;
	}

	public void setDemands(ArrayList<Location> demands) {
		this.demands = demands;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public int getVehicleNumber() {
		return vehicleNumber;
	}

	public void setVehicleNumber(int vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}

	public int getVehicleCapacity() {
		return vehicleCapacity;
	}

	public void setVehicleCapacity(int vehicleCapacity) {
		this.vehicleCapacity = vehicleCapacity;
	}

	public void setDepot(Location depot) {
		this.depot = depot;
	}

	public Location getDepot() {
		return depot;
	}
}
