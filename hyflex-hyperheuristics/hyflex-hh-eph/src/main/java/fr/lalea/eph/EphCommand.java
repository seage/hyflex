/**
 * Copyright 2011-2014, Universitaet Osnabrueck
 * Author: David Meignan
 */
package fr.lalea.eph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import com.beust.jcommander.JCommander;

/**
 * The <code>EphCommand</code> class allows to run EPH on the 
 * CHeSC benchmark using command line parameters.
 * 
 * @author David Meignan
 */
public class EphCommand {

	/**
	 * Runs EPH on the CHeSC benchmark.
	 * 
	 * @param args arguments that are managed by the
	 * <code>BenchmarkParameters</code> class.
	 */
	public static void main(String[] args) {
		// Parse parameters
		BenchmarkParameters benchParam = new BenchmarkParameters();
		try {
			new JCommander(benchParam, args);
			benchParam.validate();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// Create the output file
		File outputFile = null;
		try {
			outputFile = new File(benchParam.getOutputFileName());
			outputFile.createNewFile();
			// Write a comment in output file
			BufferedWriter outputStream = null;
			try {
				outputStream = new BufferedWriter(new FileWriter(outputFile, true));
				outputStream.write("\n# Run EPH, " + getTimeStamp() + "\n");
				outputStream.write("# Time limit (seconds): " + 
						Integer.toString(benchParam.getTimeLimitSeconds()) + "\n");
				outputStream.write("# Runs: " + 
						Integer.toString(benchParam.getRuns()) + "\n");
				outputStream.close();
			} finally {
				if (outputStream != null)
					outputStream.close();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		// Run
		for (String problemInstanceRef: benchParam.problemInstanceRefs()) {
			for (int runIdx=0; runIdx<benchParam.getRuns(); runIdx++) {
				try {
					// Create the run object
					IndividualRun run = new IndividualRun(
							problemInstanceRef,
							benchParam.getTimeLimitSeconds(),
							benchParam.getSeedStartingValue()+runIdx
							);
					// Run EPH
					runAndMonitorEPH(run, !benchParam.isProgressDisplayDisable());
					// Write the result on the output file
					storeResults(run, outputFile);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		}
		
		// End comment
		try {
			// Write a comment in output file
			BufferedWriter outputStream = null;
			try {
				outputStream = new BufferedWriter(new FileWriter(outputFile, true));
				outputStream.write("# End of the runs, " + getTimeStamp() + "\n");
				outputStream.close();
			} finally {
				if (outputStream != null)
					outputStream.close();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		System.out.println("End of the runs.");
		System.exit(0);
	}

	/**
	 * Returns the current time value as a string with the format:
	 * "yyyy-MM-dd'T'HH'h'mm'm'ss"
	 */
	public static String getTimeStamp() {
		DateFormat df = DateFormat.getDateTimeInstance();
		Date d = new Date();
		return df.format(d);
	}
	
	/**
	 * Runs EPH and monitor the progress of the optimization process.
	 * 
	 * @param run the configuration to run.
	 * @throws InterruptedException if the thread is interrupted.
	 */
	private static void runAndMonitorEPH(IndividualRun run, 
			boolean displayProgress) throws	InterruptedException {
		System.out.println("Run EPH on "+run.getProblemName()+
				", instance: "+run.getInstanceID());
		NumberFormat formatter = new DecimalFormat("#0.00"); 
		// Create and start thread
		Thread runnerThread = new Thread(run);
		runnerThread.start();
		// Print progress and current best found value
		while(runnerThread.isAlive()) {
			if (displayProgress) {
				String progress = formatter.format(run.getProgress());
				double bestFoundValue = run.getBestFoundValue();
				System.out.print("\r" +
						"Progress "+progress+"%, best found value: "+
						((Double.isNaN(bestFoundValue))?("INF"):(bestFoundValue))+
						"                                           ");
			}
			// Sleep for few milliseconds
			Thread.sleep(500);
		}
		if (displayProgress) {
			String progress = formatter.format(run.getProgress());
			double bestFoundValue = run.getBestFoundValue();
			System.out.print("\r" +
					"Progress "+progress+"%, best found value: "+
					((Double.isNaN(bestFoundValue))?("INF"):(bestFoundValue))+
					"                                           \n");
		}
	}

	
	/**
	 * Write the result of a run.
	 * 
	 * @param run the configuration that has been completed.
	 * @param outputFile the file in which the result has to be
	 * appended.
	 * @throws IOException when the file cannot be written.
	 */
	private static void storeResults(IndividualRun run, File outputFile) 
			throws IOException {
		BufferedWriter outputStream = null;
		try {
			outputStream = new BufferedWriter(new FileWriter(outputFile, true));
			outputStream.write(run.getProblemName());
			outputStream.write("\t");
			outputStream.write(Integer.toString(run.getInstanceID()));
			outputStream.write("\t");
			outputStream.write(Double.toString(run.getBestFoundValue()));
			outputStream.write("\n");
			outputStream.close();
		} finally {
			if (outputStream != null)
				outputStream.close();
		}
	}

}
