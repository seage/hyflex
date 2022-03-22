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

package be.kuleuven.kahosl.jppf;

import java.io.Serializable;

import org.jppf.server.protocol.JPPFTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import travelingSalesmanProblem.TSP;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;
import BinPacking.BinPacking;
import FlowShop.FlowShop;
import PersonnelScheduling.PersonnelScheduling;
import SAT.SAT;
import VRP.VRP;
import be.kuleuven.kahosl.acceptance.AcceptanceCriterionType;
import be.kuleuven.kahosl.hyperheuristic.GIHH;
import be.kuleuven.kahosl.problem.ProblemName;
import be.kuleuven.kahosl.selection.SelectionMethodType;
import be.kuleuven.kahosl.util.Vars;
import be.kuleuven.kahosl.util.WriteInfo;

/**
 * This class involves a task information including the hyper-heuristic and the target problem
 */
public class CHTask extends JPPFTask implements Serializable {
	
	/** A logger to write various running information/comments/errors **/
	private static final Logger log = LoggerFactory.getLogger(CHTask.class);
	
	/** Seed value for the random number generator **/
	private long seed;

	/** Heuristic selection mechanism type **/
	private SelectionMethodType selection;
	
	/** Move acceptance mecahnism type **/
	private AcceptanceCriterionType acceptance;
	
	/** Target problem's name **/
	private ProblemName problemName;
	
	/** Instance index denoting the problem instance to solve **/
	private int instanceIndex;

	/** Total execution time allowed in milliseconds (stopping condition) **/
	private long totalExecutionTime; 
	
	/** The common part of the files' name including the hyper-heuristic information **/
	private String resultFileName;
	
	/** Subfolder name where the generated files are saved **/
	private String resultSubFolderName;
	
	/** Phase length denoting the number of iterations to check the heuristics' performance (for the selection mechanism) **/
	private int phaseLength;
	
	/** Target problem **/
	private transient ProblemDomain problem = null;
	
	/** Problem object in an xml **/
	private String problemXml = null;
	
	/**
	 * JPPF client task constructor
	 * 
	 * @param selection				heuristic selection mechanism type
	 * @param phaseLength			phase length as number of iterations
	 * @param acceptance			acceptance mechanism type
	 * @param problemName			name of the target problem
	 * @param instanceIndex			instance index denoting the problem instance to solve
	 * @param totalExecutionTime	total execution time allowed in milliseconds
	 * @param resultSubFolderName	subfolder name where the generated files are saved 
	 * @param resultFileName		common part of the files' name including the hyper-heuristic information
	 * @param seed					seed value for the random number generator
	 */
	public CHTask(SelectionMethodType selection, int phaseLength,
			      AcceptanceCriterionType acceptance, 
			      ProblemName problemName, int instanceIndex,
			      long totalExecutionTime, String resultSubFolderName, String resultFileName, long seed) {		
		this.selection = selection;
		this.phaseLength = phaseLength;
		this.acceptance = acceptance;
		this.problemName = problemName;
		this.instanceIndex = instanceIndex;
		this.totalExecutionTime = totalExecutionTime;
		this.resultFileName = resultFileName;
		this.resultSubFolderName = resultSubFolderName;
		this.seed = seed;

		
		if(problemName == ProblemName.MaxSAT){
			problem = new SAT(seed);
		}else if(problemName == ProblemName.BinPacking){
			problem = new BinPacking(seed);
		}else if(problemName == ProblemName.FlowShop){
			problem = new FlowShop(seed);
		}else if(problemName == ProblemName.PersonelScheduling){
			problem = new PersonnelScheduling(seed);
		}else if(problemName == ProblemName.TravellingSalesman){
			problem = new TSP(seed);			
		}else if(problemName == ProblemName.VehicleRouting){
			problem = new VRP(seed);
		}else{
			log.error("Unrecognised problem: "+problem.toString());
			System.exit(1);
		}
		
		
		/** serialize to XML *********************************/
	    XStream xstream = new XStream(new DomDriver());
		problemXml = xstream.toXML(problem);
		/*****************************************************/
	}
	

	/**
	 * Task to run using JPPF
	 */
	@Override
	public void run() {
		
		Vars.totalExecutionTime = totalExecutionTime;		
		
		WriteInfo.resultSubFolderName = resultSubFolderName;
		
		fireNotification("Starting calculation [CH TASK]!");
		
		System.out.println(selection+"_PL"+phaseLength+"_"+acceptance+"_"+problemName+"_INST#"+instanceIndex);
		
		HyperHeuristic hh;
		try {	
			fireNotification("Start solving: "+resultFileName);
			
			/* 
			 * Serialize  the problem object from an XML formed string 
			 */
			XStream xstream = new XStream(new DomDriver());
			problem = (ProblemDomain)xstream.fromXML(problemXml);

			problem.loadInstance(instanceIndex);
			
			Vars.restartSearch = false; 
			Vars.useOverallBestSln = false; 
			Vars.noMoreRestart = false;
			
			hh = new GIHH(seed, problem.getNumberOfHeuristics(), Vars.totalExecutionTime,  
					            resultFileName, selection, acceptance);

			
			hh.setTimeLimit(Vars.totalExecutionTime);
			hh.loadProblemDomain(problem);
			hh.run();
			
			fireNotification("Stopped calculation [CH TASK]!");
		}
		catch(Exception e){
			setException(e);
		}
	}
}
