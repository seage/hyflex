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

package be.kuleuven.kahosl.util;

import java.io.Serializable;

/**
 * This class consists of the parameters to determine which information to include in a .log file
 */
public class Print implements Serializable {

	/** Hyper-heuristic related general information logging option **/
	public static boolean hyperheuristic = true;
	
	/** Selection mechanism related general information logging option **/
	public static boolean selection = true;
	
	/** Mentoring (heuristics' parameter adaptation and relay hybridisation) related general information logging option **/
	public static boolean mentoring = true;
	
	/** Acceptance mechanism related general information logging option **/
	public static boolean acceptance = true;
	
	/** Whether the search status is periodically kept **/
	public static boolean iterationBasedInfo = true;
	
	/** The number of iterations to capture the periodic information **/
	public final static int iterationNum = 1000;
}
