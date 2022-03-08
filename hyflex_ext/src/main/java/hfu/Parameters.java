package hfu;

public class Parameters {
  double dos;
  
  double iom;
  
  void setDOS(double dos) {
    this.dos = dos;
  }
  
  void setIOM(double iom) {
    this.iom = iom;
  }
  
  public double getIOM(ParameterUsage pu) {
    if (pu.usesIntensityOfMutation())
      return this.iom; 
    System.out.println("Illegal access IOM!");
    return -1.0D;
  }
  
  public double getDOS(ParameterUsage pu) {
    if (pu.usesDepthOfSearch())
      return this.dos; 
    System.out.println("Illegal access DOS!");
    return -1.0D;
  }
}


/* Location:              C:\Users\Steve\Documents\GitHub\hyflext\domains\hyflex_ext.jar!\hfu\Parameters.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */