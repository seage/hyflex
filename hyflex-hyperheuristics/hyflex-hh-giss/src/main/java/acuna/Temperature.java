/*
 Copyright 2011 karim@computer.org

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package acuna;

/**
 *
 * @author Karim
 * @version 0.904
 * edited by Dave Omrai
 *
 */
public class Temperature {


   private static final double alfa=0.04;
   private double currentTemperature;
   
   
   public Temperature(double currentTemperature) {
       this.currentTemperature=currentTemperature;
   }

   public double getCurrentTemperature() {
      return currentTemperature;
   }

   public void setCurrentTemperature(double CurrentTemperature) {
      this.currentTemperature = CurrentTemperature;
   }

   
   public double expDescend(double currentValue, double bestValue, long currentTime, long limitTime){
        
        double fmax = 1.0;
        double fxi = currentValue/bestValue;
        double currentDouble = Double.valueOf(String.valueOf(currentTime));
        double finalDouble = Double.valueOf(String.valueOf(limitTime));
        double k = ((double)1.0 + (double)((double)100.0 * currentDouble/finalDouble ));
        this.setCurrentTemperature(this.getCurrentTemperature()*Math.pow(1.0 - Temperature.alfa , k));
        double exponent = (fxi - fmax)/this.getCurrentTemperature();
        double result = ((double)1.0/Math.exp(exponent));
        return result;
    
    }
   
   

}
