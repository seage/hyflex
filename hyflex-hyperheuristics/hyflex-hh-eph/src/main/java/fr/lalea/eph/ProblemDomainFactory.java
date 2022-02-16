/**
 * Copyright 2011-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package fr.lalea.eph;

import java.lang.reflect.Constructor;

import travelingSalesmanProblem.TSP;
import PersonnelScheduling.PersonnelScheduling;
import SAT.SAT;
import VRP.VRP;
import AbstractClasses.ProblemDomain;
import BinPacking.BinPacking;
import FlowShop.FlowShop;

/**
 * This class manage the instantiation of problems their names.
 * 
 * @author David Meignan
 */
public class ProblemDomainFactory {

	/**
	 * Available problems and instances.
	 * Note: the structure of HyFlex does not allow a nicer access to the
	 * different problems. In particular, there is no "problem repository" and
	 * no standard for qualified names of problem classes.
	 */
	
	/*
	 * List of problem names.
	 */
	private static String[] problemNames = {
		"SAT",
		"BinPacking",
		"PersonnelScheduling",
		"FlowShop",
		"TSP",
		"VRP"
	};
	
	/*
	 * List of problem classes.
	 */
	@SuppressWarnings("rawtypes")
	private static Class[] problemClasses = {
		SAT.class,
		BinPacking.class,
		PersonnelScheduling.class,
		FlowShop.class,
		TSP.class,
		VRP.class
	};
	
	/*
	 * List of available instance per problem.
	 */
	private static int[][] problemInstanceIDs = {
		{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},	// For SAT
		{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},	// For BinPacking
		{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},	// For PersonnelScheduling
		{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},	// For FlowShop
		{0, 1, 2, 3, 4},	// For TSP
		{0, 1, 2, 3, 4}	// For VRP
	};
	
	/**
	 * Returns the problem name from a reference in form of PROBLEM.INSTANCE.
	 * Returns <code>null</code> if no problem corresponds to the reference. 
	 * 
	 * @param problemInstanceRef the reference to the problem instance.
	 * @return the problem name from a reference in form of PROBLEM.INSTANCE.
	 */
	public static String getProblemName(String problemInstanceRef) {
		Integer problemIndex = getProblemIndex(problemInstanceRef);
		if (problemIndex == null)
			return null;
		return problemNames[problemIndex];
	}
	
	/**
	 * Returns the problem index from a reference in form of PROBLEM.INSTANCE.
	 * Returns <code>null</code> if no problem corresponds to the reference. 
	 * 
	 * @param problemInstanceRef the reference to the problem instance.
	 * @return the problem index from a reference in form of PROBLEM.INSTANCE.
	 */
	private static Integer getProblemIndex(String problemInstanceRef) {
		try {
			String[] idPair = problemInstanceRef.split("\\.");
			if (idPair.length != 2)
				return null;
			for (int idx=0; idx<problemNames.length; idx++) {
				if (idPair[0].compareToIgnoreCase(problemNames[idx]) == 0) {
					return idx;
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
	
	/**
	 * Returns the instance ID from a reference in form of PROBLEM.INSTANCE.
	 * Returns <code>null</code> if no problem instance corresponds to the reference. 
	 * 
	 * @param problemInstanceRef the reference to the problem instance.
	 * @return the instance ID.
	 */
	public static Integer getInstanceID(String problemInstanceRef) {
		try {
			Integer problemIndex = getProblemIndex(problemInstanceRef);
			if (problemIndex == null)
				return null;
			String[] idPair = problemInstanceRef.split("\\.");
			int instanceID = Integer.parseInt(idPair[1]);
			if (idPair.length != 2)
				return null;
			// Check instance
			for (int existingID: problemInstanceIDs[problemIndex]) {
				if (instanceID == existingID) {
					return instanceID;
				}
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
	
	/**
	 * Returns <code>true</code> if the problem instance can be instantiated.
	 * Returns <code>false</code> if the problem is unknown or the instance index
	 * does not exist.
	 * 
	 * @param problemName the name of the problem.
	 * @param instanceID the index of the instance.
	 * @return <code>true</code> if the problem instance can be instantiated.
	 * Returns <code>false</code> if the problem is unknown or the instance index
	 * does not exist.
	 */
	public static boolean isValidProblemInstance(String problemName, int instanceID) {
		// Check name
		if (problemName == null || problemName.length() == 0)
			return false;
		Integer problemIndex = null;
		for (int idx=0; idx<problemNames.length && problemIndex==-1; idx++) {
			if (problemName.compareToIgnoreCase(problemNames[idx]) == 0) {
				problemIndex = idx;
			}
		}
		if (problemIndex == null)
			return false;
		// Check instance
		for (int existingID: problemInstanceIDs[problemIndex]) {
			if (instanceID == existingID) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Instantiate a problem and load the instance from its reference.
	 * 
	 * @param problemInstanceRef the problem reference in form of PROBLEM.INSTANCE.
	 * @param seedValue the seed value for random number generator.
	 * @return the problem or <code>null</code> if the reference is not valid.
	 */
	public static ProblemDomain loadProblemInstance(String problemInstanceRef,
			long seedValue) {
		Integer problemIndex = getProblemIndex(problemInstanceRef);
		Integer instanceID = getInstanceID(problemInstanceRef);
		if (problemIndex == null || instanceID == null)
			return null;
		
		ProblemDomain problem = null;
		try {
			// Create problem object
			@SuppressWarnings("unchecked")
			Class<? extends ProblemDomain> pClass = problemClasses[problemIndex];
			Constructor<? extends ProblemDomain> ctorP = 
					pClass.getDeclaredConstructor(Long.TYPE);
			problem = (ProblemDomain)ctorP.newInstance(seedValue);
			// Load problem instance
			problem.loadInstance(instanceID);
		} catch (Exception e) {
			System.err.println("Unable to instantiate the problem.");
			return null;
		}
		return problem;
	}
	

}
