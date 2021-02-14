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

package be.kuleuven.kahosl.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.kuleuven.kahosl.acceptance.AcceptanceCriterionType;
import be.kuleuven.kahosl.selection.SelectionMethodType;
import be.kuleuven.kahosl.util.Vars;
import be.kuleuven.kahosl.util.WriteInfo;


/**
 * This class is used to create and write performance related files.
 */
public class StatFiles {
	
	/**
	 * Stat file types 
	 */
	public enum WriteFileType {
		BFFile, QIFile, TDFile, ITFile, HCntFile, ACFile, RHFile, LOCFile, LRMFile, IMPRFile
	}
	
	/**
	 * Writing details of a stat file 
	 */
	class WriteFileDetail {
		boolean writeInfo;
		BufferedWriter bufferedWriter;
		
		public WriteFileDetail(boolean writeInfo, BufferedWriter bufferedWriter){
			this.writeInfo = writeInfo;
			this.bufferedWriter = bufferedWriter;
		}
	}
	
	/** A logger to write various running information/comments/errors **/
	private static final Logger log = LoggerFactory.getLogger(StatFiles.class);
	
	private int numberOfHeuristics;
	private SelectionMethodType selectionType;
	private AcceptanceCriterionType acceptanceType;
	
	//BufferedWriter parameters for each file type
    private BufferedWriter outTD = null;
    private BufferedWriter outQI = null;
    private BufferedWriter outBF = null;
    private BufferedWriter outIT = null;
    private BufferedWriter outHCnt = null;
    private BufferedWriter outAC = null;
    private BufferedWriter outRH = null; 
    private BufferedWriter outLOC = null;
    private BufferedWriter outLRM = null;
    private BufferedWriter outIMPR = null;
    
    
    /**
     * StatFiles constructor
     * 
     * @param numberOfHeuristics
     * @param selectionType
     * @param acceptanceType
     */
    public StatFiles(int numberOfHeuristics, SelectionMethodType selectionType, AcceptanceCriterionType acceptanceType){
    	this.numberOfHeuristics = numberOfHeuristics;
    	
    	this.selectionType = selectionType;
    	this.acceptanceType = acceptanceType;
    }
    
    /** 
     * Create all requested files 
     * 
     * @param filename		common name for all the files
     */
	public void statFileInitialisation(String filename){
		
		try {
			if(WriteInfo.writeBFFile){
				
				new File(WriteInfo.mainFolder+WriteInfo.resultSubFolderName).mkdirs();
				
				outBF = new BufferedWriter(
						new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+filename+"_BF.csv", true));
				
				
				outBF.write("Iteration;Time;BestFitness;LastCalledHeur;IsRelay;");
				
				for(int i = 0; i < numberOfHeuristics; i++){
					outBF.write("NumOfMoves-LLH"+i+";");
				}
				
				for(int i = 0; i < numberOfHeuristics; i++){
					outBF.write("LAProb-LLH"+i+";");
				}
				outBF.write("\n");
			}
			
			if(selectionType == SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD && WriteInfo.writeQIFile){
				outQI = new BufferedWriter(
						new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+filename+"_QI.csv", true));
				
				outQI.write("Iteration;Time;");
				
				for(int i = 0; i < numberOfHeuristics; i++){
					outQI.write("QI-"+i+";");
				}
				outQI.write("\n");
			}
			
			if(selectionType == SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD && WriteInfo.writeTDFile){
				outTD = new BufferedWriter(
						new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+filename+"_TD.csv", true));
				
				outTD.write("Iteration;Time;");
				
				for(int i = 0; i < numberOfHeuristics; i++){
					outTD.write("TD-"+i+";");
				}
				outTD.write("\n");
			}
			
			if(acceptanceType == AcceptanceCriterionType.AdaptiveIterationLimitedListBasedTA && WriteInfo.writeITFile){
				outIT = new BufferedWriter(
						new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+filename+"_IT.csv", true));
				
				outIT.write("Iteration;Time;BestFitness;IterLimit;");
				
				for(int i = 0; i < Vars.aillaListSize; i++){
					outIT.write("Threshold-"+i+";");
				}
				outIT.write("\n");
			}
			
			if(WriteInfo.writeHCntFile){
				outHCnt = new BufferedWriter(
                          	  new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+filename+"_HCnt.csv", true));
			
				outHCnt.write("Iteration;Time;BestFitness;");
				
				for(int i = 0; i < numberOfHeuristics; i++){
					outHCnt.write("Call-"+i+";Best-"+i+";Imp-"+i+";Eq-"+i+";Wrs-"+i+";Tm-"+i+";");
				}
				outHCnt.write("\n");
			}
			
			if(WriteInfo.writeACFile){
				outAC = new BufferedWriter(
						    new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+filename+"_AC.csv", true));
				
				outAC.write("Iteration;Time;BestFitness;CurrentFitness;Threshold");
				
				outAC.write("\n");
			}
			
			if(WriteInfo.writeRHFile){
				outRH = new BufferedWriter(
							new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+filename+"_RH.csv", true));
				
				outRH.write("Iteration;Time;BestFitness;PrevFitness;FirstFitness;SecondFitness;" +
						    "1stHeur;2ndHeur;1st-LOC;2nd-LOC;NewBestCntAll;NewBestCntValid;SpentIter;ValidSpentIter;SpentTime;");
				for(int i = 0; i < numberOfHeuristics; i++){ //@17052011
					outRH.write("After-LLH"+i+";");
				}
				for(int i = 0; i < numberOfHeuristics; i++){
					outRH.write("RelayProb-LLH"+i+";");
				}
				outRH.write("\n");
			}
			
			if(WriteInfo.writeLOCFile){
				outLOC = new BufferedWriter(
						    new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+filename+"_LOC.csv", true));
				
				outLOC.write("Iteration;Time;BestFitness;");
				
				for(int i = 0; i < numberOfHeuristics; i++){
					outLOC.write("LOC-LLH"+i+";");
				}
				
				for(int i = 0; i < numberOfHeuristics; i++){
					outLOC.write("CT-LLH"+i+";");
				}
				
				outLOC.write("\n");
			}
			
			if(WriteInfo.writeLRMFile){
				outLRM = new BufferedWriter(
						    new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+filename+"_LRM.csv", true));
				
				outLRM.write("Iteration;Time;BestFitness;");
				
				for(int i = 0; i < numberOfHeuristics; i++){
					outLRM.write("LRM-LLH"+i+";");
				}
				
				outLRM.write("\n");
			}
			
			if(WriteInfo.writeIMPRFile){
				outIMPR = new BufferedWriter(
						    new FileWriter(WriteInfo.mainFolder+WriteInfo.resultSubFolderName+"/"+filename+"_IMPR.csv", true));
				
				outIMPR.write("Iteration;Time;BestFitness;");
				
				for(int i = 0; i < numberOfHeuristics; i++){
					outIMPR.write("LB-H"+i+";"+"UB-H"+i+";");
				}
				
				outIMPR.write("\n");
			}
		} catch (IOException e) {
			System.out.println("\n\n\n\n ERROR: \n\n\n\n"+e);
		} 
	}
	
    /**
     * Close buffered writers to finalise writing process
     */
	public void closeBufferedWriters(){
		try {
			if(WriteInfo.writeACFile && outAC != null){
				outAC.close();
			}
			
			if(WriteInfo.writeBFFile && outBF != null){
				outBF.close();
			}
			
			if(WriteInfo.writeITFile && outIT != null){
				outIT.close();
			}
			
			if(WriteInfo.writeQIFile && outQI != null){
				outQI.close();
			}
			
			if(WriteInfo.writeTDFile && outTD != null){
				outTD.close();
			}
			
			if(WriteInfo.writeHCntFile && outHCnt != null){
				outHCnt.close();
			}

			if(WriteInfo.writeRHFile && outRH != null){
				outRH.close();
			}
			
			if(WriteInfo.writeLOCFile && outLOC != null){
				outLOC.close();
			}
			
			if(WriteInfo.writeLRMFile && outLRM != null){
				outLRM.close();
			}
			
			if(WriteInfo.writeIMPRFile && outIMPR != null){
				outIMPR.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**  
	 * Get the requested bufferedWriter object
	 * 
	 * @param wfType	file type
	 * @return 			the buffered writer object for the requested file type
	 */
	private WriteFileDetail getCorrespondingBufferedWriter(WriteFileType wfType){
		
		switch (wfType) {
			case BFFile: return new WriteFileDetail(WriteInfo.writeBFFile, outBF);
			case QIFile: 
				if(selectionType == SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD){
					return new WriteFileDetail(WriteInfo.writeQIFile, outQI);
				}else{
					return new WriteFileDetail(false, null);
				}
			case TDFile: 
				if(selectionType == SelectionMethodType.AdaptiveLimitedLAassistedDHSMentorSTD){
					return new WriteFileDetail(WriteInfo.writeTDFile, outTD);
				}else{
					return new WriteFileDetail(false, null);
				}
			case ITFile: 
				if(acceptanceType == AcceptanceCriterionType.AdaptiveIterationLimitedListBasedTA){
					return new WriteFileDetail(WriteInfo.writeITFile, outIT);
				}else{
					return new WriteFileDetail(false, null);
				}
			case HCntFile: return new WriteFileDetail(WriteInfo.writeHCntFile, outHCnt);
			case ACFile: return new WriteFileDetail(WriteInfo.writeACFile, outAC);
			case RHFile: return new WriteFileDetail(WriteInfo.writeRHFile, outRH);
			case LOCFile: return new WriteFileDetail(WriteInfo.writeLOCFile, outLOC);
			case LRMFile: return new WriteFileDetail(WriteInfo.writeLRMFile, outLRM);
			case IMPRFile: return new WriteFileDetail(WriteInfo.writeIMPRFile, outIMPR);
			
			default: System.err.println("Unrecognised WriteFileType : "+wfType.toString()); System.exit(1); return null;
		}
	}

	/** write a line of data to the requested file 
	 * 
	 * @param wfType	the file type to write
	 * @param data		the data for writing to the requested file
	 */
	public void writeIntoFile(WriteFileType wfType, String data){
		WriteFileDetail wfDetail = getCorrespondingBufferedWriter(wfType);
		
		if(wfDetail.writeInfo){
			try {
				wfDetail.bufferedWriter.write(data+"\n");
			} catch (IOException e) {
				System.out.println("\n\n\n\n ERROR: \n\n\n\n"+e);
			} 
		}
	}
}
