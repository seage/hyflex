package hfu;

public class Parameters{
	double dos;
	double iom;
	
	void setDOS(double dos){
		this.dos = dos;
	}
	
	void setIOM(double iom){
		this.iom = iom;
	}
	
	public double getIOM(ParameterUsage pu) {
		if(pu.usesIntensityOfMutation()){
			return iom;
		}else{
			System.out.println("Illegal access IOM!");
			return -1;
		}
	}
	
	public double getDOS(ParameterUsage pu) {
		if(pu.usesDepthOfSearch()){
			return dos;
		}else{
			System.out.println("Illegal access DOS!");
			return -1;
		}
	}
}
