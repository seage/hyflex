package VRP;
import java.io.BufferedReader;
import java.io.File;
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

	public Instance(int id)
	{

		String fileName = "data/vrp/";
		if(id<17)
		{
			fileName = fileName+"C/C";
			if(id<9)
			{
				fileName = fileName+"10"+(id+1)+".txt";
			}
			else
			{
				fileName = fileName+"20"+(id-8)+".txt";
			}
		}
		else if(id<40)
		{
			fileName = fileName+"R/R";
			if(id<26)
			{
				fileName = fileName+"10"+(id-16)+".txt";
			}
			else if(id<29)
			{
				fileName = fileName+"11"+(id-26)+".txt";
			}
			else if(id<38)
			{
				fileName = fileName+"20"+(id-28)+".txt";
			}
			else
			{
				fileName = fileName+"21"+(id-38)+".txt";
			}
		}
		else
		{
			fileName = fileName+"RC/RC";
			if(id<48)
			{
				fileName = fileName+"10"+(id-39)+".txt";
			}
			else
			{
				fileName = fileName+"20"+(id-47)+".txt";
			}
		}
		
		BufferedReader reader = null;
		try	{
			reader = new BufferedReader(new FileReader(new File(fileName)));
		} catch (FileNotFoundException a) {
			try {
				InputStream fis = this.getClass().getClassLoader().getResourceAsStream(fileName); 
				reader = new BufferedReader(new InputStreamReader(fis));	
			} catch(NullPointerException n) {
				System.err.println("cannot find file " + fileName);
				System.exit(-1);
			}
		}
			
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
}
