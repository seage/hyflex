/*
 * FlowShop.java
 *
 * Created on 07 March 2008, 16:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package FlowShop;

import java.util.*;
import java.io.*;


class Instance {
	int n; // number of jobs
	int m; // number of machines
	int[][] processingTimes; // [j][k] processing time of job j in machine k

	Instance(int insnumber) {
		int number = insnumber;
		//		if (insnumber < 6) {
		//			number = 120;
		//		} else if (insnumber < 12) {
		//			number = 131;
		//		} else if (insnumber < 18) {
		//			number = 142;
		//		} else if (insnumber < 24) {
		//			number = 153;
		//		} else if (insnumber < 30) {
		//			number = 164;
		//		} else if (insnumber < 36) {
		//			number = 175;
		//		} else if (insnumber < 42) {
		//			number = 186;
		//		} else if (insnumber < 48) {
		//			number = 197;
		//		} else if (insnumber < 54) {
		//			number = 208;
		//		} else if (insnumber < 60) {
		//			number = 219;
		//		} else if (insnumber < 66) {
		//			number = 230;
		//		} else if (insnumber < 72) {
		//			number = 241;
		//		} else {
		//			number = -1;
		//			System.err.println("instance does not exist " + insnumber);
		//			System.exit(-1);
		//		}
		//		int temp = insnumber % 6;
		//		switch (temp) {
		//		case 0:number += 0;break;
		//		case 1:number += 2;break;
		//		case 2:number += 4;break;
		//		case 3:number += 6;break;
		//		case 4:number += 8;break;
		//		case 5:number += 10;break;
		//		default: number = -1;
		//		System.err.println("instance does not exist " + insnumber);
		//		System.exit(-1);
		//		}
		//		System.out.println(number);
		if (insnumber == 0) {
			number = 80;
		} else if (insnumber == 1) {
			number = 81;
		} else if (insnumber == 2) {
			number = 82;
		} else if (insnumber == 3) {
			number = 83;
		} else if (insnumber == 4) {
			number = 84;
		} else if (insnumber == 5) {
			number = 91;
		} else if (insnumber == 6) {
			number = 92;
		} else if (insnumber == 7) {
			number = 110;
		} else if (insnumber == 8) {
			number = 111;
		} else if (insnumber == 9) {
			number = 113;
		} else if (insnumber == 10) {
			number = 100;
		} else if (insnumber == 11) {
			number = 112;
		} else {
			number = -1;
			System.err.println("instance does not exist " + insnumber);
			System.exit(-1);
		}

		try {
			String fileName = returnNameForFolder(number);
			int[][] data = openDataAsInt(fileName, true);
			this.processingTimes = transposeMatrix(data);
			this.n = processingTimes.length;
			this.m = processingTimes[0].length;
		} catch (Exception ex) {
			try {
				String fileName = returnNameForJar(number);
				int[][] data = openDataAsInt(fileName, false);
				this.processingTimes = transposeMatrix(data);
				this.n = processingTimes.length;
				this.m = processingTimes[0].length;
			} catch (Exception ex2) {
				System.out.println("Could not open file from Folder: "
						+ ex.toString());
				System.out.println("Could not open file from Jar: "
						+ ex2.toString());
				System.exit(0);
			}
		}
	}

	int getM() {
		return this.m;
	}

	int getN() {
		return this.n;
	}

	int getSumP() {
		int sum = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				sum += processingTimes[i][j];
			}
		}
		return sum;
	}

	int[][] getProcTimes(){
		return this.processingTimes;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Processing times: \n");
		for (int i = 0; i < this.n; i++) {
			for (int j = 0; j < this.m; j++) {
				buffer.append(processingTimes[i][j] + " ");
			}
			buffer.append("\n");
		}
		return buffer.toString();
	}

	private int[][] openDataAsInt(String file, boolean fromFolder)
	throws Exception {
		String data = "";
		if (fromFolder)
			data = openFileFromFolder(file);
		else
			data = openFileFromJar(file);
		ArrayList<int[]> list = new ArrayList<int[]>();
		int numbColumns = 0;
		try {
			StringReader rdr = new StringReader(data);
			BufferedReader bfr = new BufferedReader(rdr);
			String line = bfr.readLine();
			StringTokenizer tok = new StringTokenizer(line);
			numbColumns = tok.countTokens();
			int[] lineInt = new int[numbColumns];
			while (line != null) {
				tok = new StringTokenizer(line);
				lineInt = new int[numbColumns];
				for (int i = 0; i < numbColumns; i++) {
					lineInt[i] = Integer.parseInt(tok.nextToken());
				}
				list.add(lineInt.clone());
				line = bfr.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		int[][] finalData = new int[list.size()][];
		for (int i = 0; i < list.size(); i++) {
			finalData[i] = list.get(i);
		}
		return finalData;
	}

	private String openFileFromFolder(String file) throws Exception {
		StringBuffer sbf = new StringBuffer();
		try {
			FileReader fr = new FileReader(file);
			BufferedReader bfr = new BufferedReader(fr);
			{
				String strLine = null;
				while ((strLine = bfr.readLine()) != null) {
					sbf.append(strLine + "\n");
				}
				bfr.close();
			}
		} catch (Exception e) {
			throw e;
		}
		return sbf.toString();
	}

	private String openFileFromJar(String file) throws Exception {
		StringBuffer sbf = new StringBuffer();
		try {
			BufferedReader bfr = new BufferedReader(new InputStreamReader(this
					.getClass().getClassLoader().getResourceAsStream(file)));
			{
				String strLine = null;
				while ((strLine = bfr.readLine()) != null) {
					sbf.append(strLine + "\n");
				}
				bfr.close();
			}
		} catch (Exception e) {
			throw e;
		}
		return sbf.toString();
	}

	private String returnNameForFolder(int number) {
		String fileName = "data\\flowshop\\";
		int a = number / 10;
		int b = number % 10;
		switch (a) {
		case (0):
			fileName += "20x5\\";
		break;
		case (1):
			fileName += "20x10\\";
		break;
		case (2):
			fileName += "20x20\\";
		break;
		case (3):
			fileName += "50x5\\";
		break;
		case (4):
			fileName += "50x10\\";
		break;
		case (5):
			fileName += "50x20\\";
		break;
		case (6):
			fileName += "100x5\\";
		break;
		case (7):
			fileName += "100x10\\";
		break;
		case (8):
			fileName += "100x20\\";
		break;
		case (9):
			fileName += "200x10\\";
		break;
		case (10):
			fileName += "200x20\\";
		break;
		case (11):
			fileName += "500x20\\";
		break;
		}
		return fileName + (b + 1) + ".txt";

	}


	private String returnNameForJar(int number) {
		String fileName = "data/flowshop/";
		int a = number / 10;
		int b = number % 10;
		switch (a) {
		case (0):
			fileName += "20x5/";
		break;
		case (1):
			fileName += "20x10/";
		break;
		case (2):
			fileName += "20x20/";
		break;
		case (3):
			fileName += "50x5/";
		break;
		case (4):
			fileName += "50x10/";
		break;
		case (5):
			fileName += "50x20/";
		break;
		case (6):
			fileName += "100x5/";
		break;
		case (7):
			fileName += "100x10/";
		break;
		case (8):
			fileName += "100x20/";
		break;
		case (9):
			fileName += "200x10/";
		break;
		case (10):
			fileName += "200x20/";
		break;
		case (11):
			fileName += "500x20/";
		break;
		}
		return fileName + (b + 1) + ".txt";
	}

	private int[][] transposeMatrix(int[][] matrix) {
		int[][] newMatrix = new int[matrix[0].length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				newMatrix[j][i] = matrix[i][j];
			}
		}
		return newMatrix;
	}

}
