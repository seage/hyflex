/*
    Copyright (c) 2012  Mustafa MISIR, KU Leuven - KAHO Sint-Lieven, Belgium
 	
 	This file is part of GIHH v1.0.
 	
    GIHH is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    	
*/

package be.kuleuven.kahosl.acceptance;

import java.io.Serializable;
import java.util.Random;

/**
 * This class is an abstract class to implement the acceptance mechanism.
 * It can be extended to implement new acceptance mechanisms.
 */
public abstract class AcceptanceCriterion implements Serializable {
	
	public Random r;
	
	public AcceptanceCriterion(Random r){
		this.r = r;
	}
	
	public static int numberOfIterations = 0;
	public abstract boolean accept(double newFitness, double currentFitness, double bestFitness);
	protected static final double epsilon = 0.00001;
	
	public double getCurrentThreshold(){ return -1; }
	
	public String getIterationLimitWithThresholdValuesDetailsAsStr(){ return null; }
	
	public void resetAcceptanceList(double newFitness){}
	public void useNoMoreRestartCaseList(){}
	
}
