package BinPacking;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import AbstractClasses.ProblemDomain;


/**
 * This class implements the Bin Packing problem domain.
 * @author Matthew Hyde. mvh@cs.nott.ac.uk
 */

public class BinPacking extends ProblemDomain {

	private final static boolean HIGHEST_FIRST = true;
	private final static boolean LOWEST_FIRST = false;
	private static int defaultmemorysize = 2;
	private final int[] mutations = new int[]{0,3,5};
	private final int[] ruinRecreates = new int[]{1,2};
	private final int[] localSearches = new int[]{4,6};
	private final int[] crossovers = new int[]{7};

	private Solution[] solutionMemory;
	private Vector<Bin> bestEverSolution;
	private double bestEverObjectiveFunction = Double.POSITIVE_INFINITY;
	private double bestEverNumberOfBins = Double.POSITIVE_INFINITY;
	private double capacity;
	private double totalpiecesize;//for the sanitycheck
	private int numberOfPieces;
	private Piece[] pieces;

	private int lrepeats;
	private int mrepeats;

	/**
	 * Constructs a new BinPacking object with a seed for the random number generator.
	 * @param seed The seed for the random number generator.
	 */
	public BinPacking(long seed) {//constructor
		super(seed);
	}//end constructor

	public void setDepthOfSearch(double depthOfSearch)
	{
		super.setDepthOfSearch(depthOfSearch);
		if (depthOfSearch <= 0.2) {
			lrepeats = 10;
		} else if (depthOfSearch <= 0.4) {
			lrepeats = 12;
		} else if (depthOfSearch <= 0.6) {
			lrepeats = 14;
		} else if (depthOfSearch <= 0.8) {
			lrepeats = 17;
		} else {//its 0.8<X<1.0
			lrepeats = 20;
		}
	}

	public void setIntensityOfMutation(double intensityOfMutation)
	{
		super.setIntensityOfMutation(intensityOfMutation);
		if (intensityOfMutation <= 0.2) {
			mrepeats = 1;
		} else if (intensityOfMutation <= 0.4) {
			mrepeats = 2;
		} else if (intensityOfMutation <= 0.6) {
			mrepeats = 3;
		} else if (intensityOfMutation <= 0.8) {
			mrepeats = 4;
		} else {//its 0.8<X<1.0
			mrepeats = 5;
		}
	}

	public int[] getHeuristicsThatUseDepthOfSearch() {
		return localSearches;
	}

	public int[] getHeuristicsThatUseIntensityOfMutation() {
		int[] newint = new int[mutations.length + ruinRecreates.length];
		int count = 0;
		for (int x = 0; x < mutations.length; x++) {
			newint[count] = mutations[x];count++;
		}
		for (int x = 0; x < ruinRecreates.length; x++) {
			newint[count] = ruinRecreates[x];count++;
		}
		return newint;
	}

	private void loadInstance(String filename) {//fills an array of 'Piece' objects
		BufferedReader buffread;
		try {
			FileReader read = new FileReader(filename);
			buffread = new BufferedReader(read);
			readInInstance(buffread);
		} catch (FileNotFoundException a) {
			try {
				InputStream fis = this.getClass().getClassLoader().getResourceAsStream(filename); 
				buffread = new BufferedReader(new InputStreamReader(fis));
				readInInstance(buffread);			
			} catch(NullPointerException n) {
				System.err.println("cannot find file " + filename);
				System.exit(-1);
			}
		}//end catch
	}//end method loadInstance

	private void readInInstance(BufferedReader buffread) {
		try {
			buffread.readLine();
			buffread.readLine();//read in the instance name
			String l = buffread.readLine();
			capacity = (Double.parseDouble(l.split(" ")[1]))*10;//read in the capacity of the bin
			numberOfPieces = Integer.parseInt(l.split(" ")[2]);//read in the number of pieces in this instance
			pieces = new Piece[numberOfPieces];
			for (int piece = 0; piece < numberOfPieces; piece++) {
				double piecesize = (Double.parseDouble(buffread.readLine()))*10;
				totalpiecesize += piecesize;
				pieces[piece] = new Piece(piecesize, piece);
			}
			//lowerbound = Math.ceil(totalpiecesize/capacity);
		} catch (IOException a) {
			System.err.println(a.getMessage());
			System.exit(0);
		}//end catch
	}

	public void loadInstance(int instanceID) {
		String ins = "instance doesn't exist: " + instanceID;
		if (instanceID < 2) {
			ins = "data/binpacking/falkenauer/falk1000-" + (instanceID+1) + ".txt";
		} else if (instanceID < 4) {
			ins = "data/binpacking/schoenfield/schoenfieldhard" + (instanceID-1) + ".txt";
		} else if (instanceID < 5) {
			ins = "data/binpacking/2000/10-30/instance1.txt";
		} else if (instanceID < 6) {
			ins = "data/binpacking/2000/10-30/instance2.txt";
		} else if (instanceID < 7) {
			ins = "data/binpacking/trip1002/instance1.txt";
		} else if (instanceID < 8) {
			ins = "data/binpacking/trip2004/instance1.txt";
		} else if (instanceID < 9) {
			ins = "data/binpacking/testdual4/binpack0.txt";
		} else if (instanceID < 10) {
			ins = "data/binpacking/testdual7/binpack0.txt";
		} else {
			System.err.println("instance " + ins + "does not exist");
			System.exit(-1);
		}
		//System.out.println(ins);
		loadInstance(ins);//load the instance from the file
		solutionMemory = new Solution[defaultmemorysize];//set solution memory size
		//initialiseSolution(0);//load all of the pieces into the bins to initialise the solution
	}

	public void initialiseSolution(int index) {//pack an initial solution with 'first fit'
		solutionMemory[index] = new Solution();//re-initialise the solution at this index
		solutionMemory[index].addBin(new Bin());//initialise solution memory
		//first randomise the pieces
		LinkedList<Piece> piecerandomiser = new LinkedList<Piece>();
		for (int piece = 0; piece < numberOfPieces; piece++) {//loop the pieces, line by line
			piecerandomiser.add(pieces[piece]);
		}//end looping the pieces of the instance
		Collections.shuffle(piecerandomiser, rng);
		//Collections.sort(piecerandomiser);
		for (int piece = 0; piece < numberOfPieces; piece++) {
			pieces[piece] = piecerandomiser.removeLast();
		}//end for looping the pieces to randomise them
		//now pack the pieces
		for (int i = 0; i < numberOfPieces; i++) {//loop the pieces
			Piece currentPiece = pieces[i];
			int numberOfBins = solutionMemory[index].size();
			for (int binNumber = 0; binNumber < numberOfBins; binNumber++) {//loop the bins
				Bin bin = solutionMemory[index].get(binNumber);
				if (currentPiece.getSize() <= (capacity - bin.getFullness())) {//if the piece fits
					bin.addPiece(currentPiece);
					solutionMemory[index].set(binNumber, bin);
					if (binNumber == numberOfBins - 1) {//if its the last bin
						solutionMemory[index].addBin(new Bin());}//add a new empty bin
					break;//stop looping the bins
				}//end if the piece fits
			}//end looping the bins
		}//end looping the pieces
		sortbins(solutionMemory[index].solution, HIGHEST_FIRST);//put the bins in order
		double i = getFunctionValue(index);
		if (i < bestEverObjectiveFunction) {
			bestEverObjectiveFunction = i;
		}
	}//end method constructInitialSolution

	private double evaluateObjectiveFunction(Vector<Bin> bins) {//evaluates the objective function on a bin vector given to it, e.g. a temporary one
		double objectiveFunctionValue = 0;
		double utilisation = 0;
		double binsused = 0;//this is needed because we do not want to include the bins not used when we do the falkenauer utilisation equation
		for (int u = 0; u < bins.size(); u++) {//loop the bins to add up the utilisation
			Bin bin = bins.get(u);//get the current bin
			if (bin.getFullness() != 0) {//if the bin is not empty (so it only counts the bins that have been used)
				utilisation += Math.pow((bin.getFullness() / capacity), 2);//square the space utilisation
				binsused++;//one more bin has been used, because it wasnt empty as we are in this 'if' statement
			}//end if the bin's not empty
		}//end looping the bins and summing the utilisation
		objectiveFunctionValue = 1-(utilisation / binsused);//finally divide by the number of bins and take away from one, so that 0 is the best and 1 is the worst
		//objectiveFunctionValue = binsused;
		return objectiveFunctionValue;
	}//end method evaluateObjectiveFunction

	private void applyBestFit(Bin[] array, Vector<Bin> v) {//adds the pieces in the bin array to the bin vector, with best fit decreasing
		//we will extract the pieces from the bin array, store them in the pieceVector, sort them and then repack them into the bin Vector v.
		Vector<Piece> pieceVector = new Vector<Piece>();
		int pieces = 0;
		for (int i = 0; i < array.length; i++) {//loop the bins
			Bin b = array[i];//get the bin object
			int binpieces = b.numberOfPiecesInThisBin();
			for (int x = 0; x < binpieces; x++) {
				pieceVector.add(b.removePiece(0));//gets the first piece from the bin, and after it is removed the others will shift left
				pieces++;
			}//end looping the pieces in the bin and adding them to the piece vector
		}//end looping the bins
		Collections.sort(pieceVector);//sort them largest first to be packed back into the bins
		for (int y = 0; y < pieces; y++) {//loop the pieces
			Piece currentPiece = pieceVector.remove(0);
			int numberOfBins = v.size();
			double bestgapsofar = Double.POSITIVE_INFINITY;
			int bestbinsofar = -1;
			for (int binNumber = 0; binNumber < numberOfBins; binNumber++) {//loop the bins to pack the piece into
				Bin bin = v.get(binNumber);
				double gap = capacity - currentPiece.getSize() - bin.getFullness();
				if ((gap < bestgapsofar) && (gap >= 0)) {//if it is the smallest gap, && it fits into the bin
					bestgapsofar = gap;
					bestbinsofar = binNumber;
				}
			}//end looping the bins
			//now we have the best bin
			Bin bin = v.get(bestbinsofar);
			bin.addPiece(currentPiece);
			if (bestbinsofar == numberOfBins - 1) {//if its the last bin
				v.add(new Bin());}//add a new empty bin
		}//end looping the pieces in the bin
	}//end method applyFirstFit

	/*private void applyFirstFit(Bin[] array, Vector<Bin> v) {//adds the pieces in the bin array to the bin vector, with first fit
		//we will extract the pieces from the bin array, store them in the pieceVector, sort them and then repack them into the bin Vector v.
		Vector<Piece> pieceVector = new Vector<Piece>();
		int pieces = 0;
		for (int i = 0; i < array.length; i++) {//loop the bins
			Bin b = array[i];//get the bin object
			int binpieces = b.numberOfPiecesInThisBin();
			for (int x = 0; x < binpieces; x++) {
				pieceVector.add(b.removePiece(0));//gets the first piece from the bin, and after it is removed the others will shift left
				pieces++;
			}//end looping the pieces in the bin and adding them to the piece vector
		}//end looping the bins
		Collections.sort(pieceVector);//sort them largest first to be packed back into the bins
		for (int y = 0; y < pieces; y++) {//loop the pieces
			Piece currentPiece = pieceVector.remove(0);
			int numberOfBins = v.size();
			for (int binNumber = 0; binNumber < numberOfBins; binNumber++) {//loop the bins to pack the piece into
				Bin bin = v.get(binNumber);
				if (currentPiece.getSize() <= (capacity - bin.getFullness())) {//if the piece fits
					bin.addPiece(currentPiece);//add the piece to the bin
					if (binNumber == numberOfBins - 1) {//if its the last bin
						v.add(new Bin());}//add a new empty bin
					break;//stop looping the bins
				}//end if the piece fits
			}//end looping the bins
		}//end looping the pieces in the bin
	}//end method applyFirstFit*/

	private void applyHeuristic0(Vector<Bin> temporaryBinVector) {//swap a random piece with another random different piece
		for (int r = 0; r < mrepeats; r++) {
			Piece piece1 = pieces[rng.nextInt(numberOfPieces)];//choose a piece
			Piece piece2 = pieces[rng.nextInt(numberOfPieces)];//choose a second piece

			while (true) {
				if (piece1.getSize() == piece2.getSize()) {//if the pieces are the same size, then choose another piece2
					piece2 = pieces[rng.nextInt(numberOfPieces)];//choose another second piece
				} else {break;}//break when we have a piece of different size
			}//end while loop, choosing a second piece

			//System.out.println("swap " + piece1.getNumber() + "," + piece1.getSize() + " with " + piece2.getNumber() + "," + piece2.getSize());

			//swap the pieces
			int bin1index = -1, bin2index = -1;
			for (int x = 0; x < temporaryBinVector.size(); x++) {//look for piece1
				Bin currentbin = temporaryBinVector.get(x);
				if (currentbin.contains(piece1) != -1) {//the piece is in the current bin
					bin1index = x;//store the index of the piece
				}
				if (currentbin.contains(piece2) != -1) {
					bin2index = x;
				}
				if ((bin1index != -1) && (bin2index != -1)) {
					break;
				}
			}//end for loop looking for the bins that the pieces are in
			//System.out.println(bin1index +  " " + bin2index);
			Bin bin1 = temporaryBinVector.get(bin1index);	
			Bin bin2 = temporaryBinVector.get(bin2index);
			bin2.removePiece(piece2);
			if ((bin2.getFullness() + piece1.getSize()) <= capacity) {//if piece1 fits into bin2
				bin2.addPiece(piece1);
			} else {//put it in the last bin
				int numberofbins = temporaryBinVector.size();
				bin2 = temporaryBinVector.get(numberofbins-1);
				bin2.addPiece(piece1);
				temporaryBinVector.add(new Bin());//add a new empty bin because we just put a piece in the last bin
			}
			bin1.removePiece(piece1);
			if ((bin1.getFullness() + piece2.getSize()) <= capacity) {//if piece1 fits into bin2
				bin1.addPiece(piece2);
			} else {//put it in the last bin
				int numberofbins = temporaryBinVector.size();
				bin1 = temporaryBinVector.get(numberofbins-1);
				bin1.addPiece(piece2);
				temporaryBinVector.add(new Bin());//add a new empty bin because we just put a piece in the last bin
			}
			sortbins(temporaryBinVector, HIGHEST_FIRST);
		}//end repeating due to the value of intensityofmutation
	}//end method applyHeuristic0

	private void ruinAndRecreate(int numberOfBinsToRemove, Vector<Bin> temporaryBinVector, boolean highestOrLowest) {//removes some bins and repacks the pieces with best fit
		Bin[] tempBinArray = new Bin[numberOfBinsToRemove];
		if (!highestOrLowest) {sortbins(temporaryBinVector, LOWEST_FIRST);}//sort the bins lowest first if we're removing the lowest filled bins
		for (int x = 0; x < numberOfBinsToRemove; x++) {
			tempBinArray[x] = temporaryBinVector.remove(0);
		}//end looping the number of bins to remove
		if (!highestOrLowest) {sortbins(temporaryBinVector, HIGHEST_FIRST);}//order the bins highest first again if we inverted it before
		applyBestFit(tempBinArray, temporaryBinVector);
	}//end method ruinAndRecreate

	private void applyHeuristic1(Vector<Bin> temporaryBinVector) {//destroy X highest filled bins and repack them with best fit
		//		System.out.println("rr used ");
		//		System.exit(-1);
		if (intensityOfMutation <= 0.2) {
			ruinAndRecreate(3, temporaryBinVector, HIGHEST_FIRST);
		} else if (intensityOfMutation <= 0.4) {
			ruinAndRecreate(6, temporaryBinVector, HIGHEST_FIRST);
		} else if (intensityOfMutation <= 0.6) {
			ruinAndRecreate(9, temporaryBinVector, HIGHEST_FIRST);
		} else if (intensityOfMutation <= 0.8) {
			ruinAndRecreate(12, temporaryBinVector, HIGHEST_FIRST);
		} else {//its 0.8<X<1.0
			ruinAndRecreate(15, temporaryBinVector, HIGHEST_FIRST);
		}
	}//end method applyHeuristic1

	private void applyHeuristic2(Vector<Bin> temporaryBinVector) {//destroy X lowest filled bins and repack them with best fit
		//		System.out.println("rr used ");
		//		System.exit(-1);
		if (intensityOfMutation <= 0.2) {
			ruinAndRecreate(3, temporaryBinVector, LOWEST_FIRST);
		} else if (intensityOfMutation <= 0.4) {
			ruinAndRecreate(6, temporaryBinVector, LOWEST_FIRST);
		} else if (intensityOfMutation <= 0.6) {
			ruinAndRecreate(9, temporaryBinVector, LOWEST_FIRST);
		} else if (intensityOfMutation <= 0.8) {
			ruinAndRecreate(12, temporaryBinVector, LOWEST_FIRST);
		} else {//its 0.8<X<1.0
			ruinAndRecreate(15, temporaryBinVector, LOWEST_FIRST);
		}
	}//end method applyHeuristic2

	private void applyHeuristic3(Vector<Bin> temporaryBinVector) {//destroy lowest filled bin and repack the pieces with best fit
		for (int r = 0; r < mrepeats; r++) {
			ruinAndRecreate(1, temporaryBinVector, LOWEST_FIRST);
			sortbins(temporaryBinVector, HIGHEST_FIRST);
		}//end repeating the process for further depth of search
	}//end method applyHeuristic3

	private void applyHeuristic4(Vector<Bin> temporaryBinVector) {//LS - iterate, one from lowest filled bin and exchange with a smaller piece from a random bin
		//if there is no smaller piece that produces a valid swap then exchange with two pieces that are smaller in total than the first piece
		for (int r = 0; r < lrepeats; r++) {
			sortbins(temporaryBinVector, LOWEST_FIRST);
			//System.out.print("Initial solution: \n" );
			//printtempbins(temporaryBinVector);
			Bin lowestbin = temporaryBinVector.get(0);
			double largestpieceinthisbin = 0;
			int largestpieceindex = -1;
			for (int x = 0; x < lowestbin.numberOfPiecesInThisBin(); x++) {//loop bin pieces looking for largest piece in the bin. it goes from 1 because index zero is the empty bin
				if (lowestbin.getPieceSize(x) > largestpieceinthisbin) {//test for largest piece
					largestpieceinthisbin = lowestbin.getPieceSize(x);
					largestpieceindex = x;
				}//end if looking for the largest piece in the lowestbin
			}//end looping the pieces of this bin
			Piece p1 = lowestbin.removePiece(largestpieceindex);//remove the largest piece
			//System.out.println("size of first piece " + p1.getSize());
			//pick a random non-fully-filled bin
			int bin2 = -1;
			boolean continuelooping = true;
			while(continuelooping) {
				bin2 = rng.nextInt(temporaryBinVector.size());
				if (bin2 != 0) {//if the second bin isnt the one we took the piece from
					break;}//quit the loop because we've found a different bin
			}//end looping to pick the second bin
			//System.out.println("second bin" + bin2);
			Bin randomBin = temporaryBinVector.get(bin2);
			//look in the bin for a smaller piece, or a combination of smaller pieces
			//go through all the pieces that are smaller
			double largestsmallerpiece = 0;
			int largestsmallerpieceindex = -1;
			for (int x = 0; x < randomBin.numberOfPiecesInThisBin(); x++) {
				double piecesize = randomBin.getPieceSize(x);
				if (piecesize < largestpieceinthisbin) {//if the piece is smaller than the piece we are swapping with
					if ((randomBin.getFullness() - piecesize + largestpieceinthisbin) <= capacity) {//if the pieces could be legally swapped
						if (piecesize > largestsmallerpiece) {//if its also the largest of the smaller pieces found so far
							largestsmallerpiece = piecesize;
							largestsmallerpieceindex = x;
						}//end if testing if its the largest small piece so far
					}//end if testing if the original piece can fit in the bin if the current piece is taken out
				}//end if testing if the piece is smaller
			}//end for looping the pieces of the bin to find the largest smaller piece
			//largestsmallerpiece is now the largest smaller piece that we can legally swap
			if (largestsmallerpieceindex != -1) {//if we found one we can legally swap, swap it with p
				Piece p2 = randomBin.removePiece(largestsmallerpieceindex);
				//System.out.println("size of second piece " + p2.getSize());
				lowestbin.addPiece(p2);	//put p2 into lowestBin
				randomBin.addPiece(p1);//put p into randomBin
			} else {//look for *2* that we can legally swap
				int piece1index = -1;
				int piece2index = -1;
				for (int x = 0; x < randomBin.numberOfPiecesInThisBin(); x++) {
					double piece1size = randomBin.getPieceSize(x);
					for (int y = 0; y < randomBin.numberOfPiecesInThisBin(); y++) {
						double piece2size = randomBin.getPieceSize(y);
						if (y != x) {//index x and y must not be the same piece
							if (piece1size+piece2size < largestpieceinthisbin) {//if the piece is smaller than the piece we are swapping with
								if ((randomBin.getFullness() - piece1size - piece2size + largestpieceinthisbin) <= capacity) {//if the pieces could be legally swapped
									piece1index = x;
									piece2index = y;
								}//end if the pieces could be legally swapped
							}//end if the two pieces are smaller than 
						}//end if checking if they're the same piece
					}//end for looping for the second piece
				}//end for looping for the first piece
				if (piece1index != -1) {//swap the pieces because we found two that fit
					Piece[] tworemovedpieces = randomBin.removeTwoPieces(piece1index, piece2index);
					randomBin.addPiece(p1);
					lowestbin.addPiece(tworemovedpieces[0]);
					lowestbin.addPiece(tworemovedpieces[1]);
				} else {//just put the first piece back in the bin it came from, because it hasn't been swapped
					lowestbin.addPiece(p1);
				}//end if
			}//end if else
			//sortbins(temporaryBinVector, HIGHEST_FIRST);
		}//end repeating the process for further depth of search
		//System.out.println("final solution: \n");
		//printtempbins(temporaryBinVector);
	}//end method applyHeuristic4

	private void applyHeuristic5(Vector<Bin> temporaryBinVector) {//finds a bin with more pieces in than the average, and 
		//splits it into two bins, half the pieces in each
		for (int r = 0; r < mrepeats; r++) {
			//System.out.println("initial solution: \n");
			//printtempbins(temporaryBinVector);
			//find the average number of pieces in the bin
			double averagenumberofpieces = 0;
			for (int x = 0; x < temporaryBinVector.size()-1; x++) {//loop the bins, miss the empty bin at the end
				averagenumberofpieces += (temporaryBinVector.get(x)).numberOfPiecesInThisBin();
			}//end looping the bins
			averagenumberofpieces /= (temporaryBinVector.size()-1);//the average number of pieces in this bin
			//find all of the bins with more than the average number of pieces
			if (averagenumberofpieces != 1) {
				Vector<Integer> v = new Vector<Integer>();
				for (int x = 0; x < temporaryBinVector.size()-1; x++) {//loop the bins, miss the empty bin at the end
					int numberofpiecesinthisbin = (temporaryBinVector.get(x)).numberOfPiecesInThisBin();
					if (numberofpiecesinthisbin >= averagenumberofpieces) {
						v.add(new Integer(x));}//store the index of this bin
				}//end looping bins to get a list of all the bins with greater than average number of pieces
				double[] emptinesses = new double[v.size()];
				double totalemptiness = 0;
				for (int x = 0; x < v.size(); x++) {//for all the bins that have greater than average pieces in
					//System.out.println((temporaryBinVector.get((v.get(x)).intValue())).numberOfPiecesInThisBin());
					totalemptiness += capacity - (temporaryBinVector.get((v.get(x)).intValue())).getFullness();//keep track of the total emptiness of such bins
					emptinesses[x] = totalemptiness;
				}//end for x
				//now we have an array of all the emptinesses of the bins with greater than average number of pieces
				double roulettenumber = rng.nextDouble()*totalemptiness;//pick a number between 0 and totalemptiness
				for (int x = 0; x < emptinesses.length; x++) {//loop the bins, not the empty one
					if (roulettenumber <= emptinesses[x]) {
						//System.out.println("bin to half " + v.get(x));
						Bin binToHalf = temporaryBinVector.get((v.get(x)).intValue());
						int numberofpiecesinthisbin = binToHalf.numberOfPiecesInThisBin();
						int numberofpiecestotakeout = (int)Math.floor((double)numberofpiecesinthisbin/2);//the number of pieces to take out of the bin
						Bin emptybin = new Bin();
						//System.out.println("take out " + numberofpiecestotakeout);
						for (int y = 0; y < numberofpiecestotakeout; y++) {//loop each piece to take out
							emptybin.addPiece(binToHalf.removePiece(rng.nextInt(numberofpiecesinthisbin-y)));
						}//end for y
						temporaryBinVector.add(emptybin);
						//System.out.println("final solution: \n");
						//printtempbins(temporaryBinVector);
						break;
					}//end if
				}//end looping bins		
				sortbins(temporaryBinVector, HIGHEST_FIRST);
			}
		}//end repeating due to the value of intensityofmutation
	}//end method applyHeuristic5

	private void applyHeuristic6(Vector<Bin> temporaryBinVector) {//LS - iterate, swap a random piece with another random different piece
		for (int r = 0; r < lrepeats; r++) {
			Piece piece1 = pieces[rng.nextInt(numberOfPieces)];//choose a piece
			Piece piece2 = pieces[rng.nextInt(numberOfPieces)];//choose a second piece

			while (true) {
				if (piece1.getSize() == piece2.getSize()) {//if the pieces are the same size, then choose another piece2
					piece2 = pieces[rng.nextInt(numberOfPieces)];//choose another second piece
				} else {break;}//break when we have a piece of different size
			}//end while loop, choosing a second piece

			//System.out.println("swap " + piece1.getNumber() + "," + piece1.getSize() + " with " + piece2.getNumber() + "," + piece2.getSize());

			//swap the pieces
			int bin1index = -1, bin2index = -1;
			for (int x = 0; x < temporaryBinVector.size(); x++) {//look for piece1
				Bin currentbin = temporaryBinVector.get(x);
				if (currentbin.contains(piece1) != -1) {//the piece is in the current bin
					bin1index = x;//store the index of the piece
				}
				if (currentbin.contains(piece2) != -1) {
					bin2index = x;
				}
				if ((bin1index != -1) && (bin2index != -1)) {
					break;
				}
			}//end for loop looking for the bins that the pieces are in
			//System.out.println(bin1index +  " " + bin2index);
			Bin bin1 = temporaryBinVector.get(bin1index);	
			Bin bin2 = temporaryBinVector.get(bin2index);

			//check they both can be swapped
			boolean possible = true;
			boolean beneficial = false;
			if (bin1.getFullness() - piece1.getSize() + piece2.getSize() > capacity) {
				possible = false;
			}
			if (bin2.getFullness() - piece2.getSize() + piece1.getSize() > capacity) {
				possible = false;
			}
			if (possible) {
				if (bin1.getFullness() > bin2.getFullness()) {
					if (piece1.getSize() < piece2.getSize()) {//bin1 will get fuller, and it was fuller beforehand
						beneficial = true;
					}
				} else if (bin2.getFullness() > bin1.getFullness()) {
					if (piece2.getSize() < piece1.getSize()) {//bin2 will get fuller, and it was fuller beforehand
						beneficial = true;
					}
				} else {//the bins are the same fullness, so they will get more uneven(because the pieces are different sizes), which is good
					beneficial = true;
				}
			}
			if (beneficial) {
				bin2.removePiece(piece2);
				bin1.removePiece(piece1);
				bin2.addPiece(piece1);
				bin1.addPiece(piece2);
			}
			sortbins(temporaryBinVector, HIGHEST_FIRST);
		}//end repeating due to the value of depthofsearch
	}//end method applyHeuristic6

	private void applyHeuristic7(Vector<Bin> temporaryBinVector1, Vector<Bin> temporaryBinVector2) {//xover
		//from 'A genetic algorithm with exon shuffling crossover for hard bin packing problems'
		//System.out.println("h7");
		Vector<Bin> binlist = new Vector<Bin>();
		binlist.addAll(temporaryBinVector1);
		binlist.addAll(temporaryBinVector2);
		Collections.sort(binlist);
		binlist.remove(binlist.size()-1);
		//		ListIterator<Bin> np1 = temporaryBinVector1.listIterator();
		//		while (np1.hasNext()) {
		//			Bin b = np1.next();
		//			b.print();
		//		}System.out.println();
		//		np1 = temporaryBinVector2.listIterator();
		//		while (np1.hasNext()) {
		//			Bin b = np1.next();
		//			b.print();
		//		}System.out.println();
		temporaryBinVector1.removeAllElements();

		//phase 1, add all mutually exclusive bins
		Vector<Integer> numberspacked = new Vector<Integer>();
		ListIterator<Bin> i = binlist.listIterator();
		while (i.hasNext()) {//loop the bins
			Bin b = i.next();
			//check if bin contains only new elements
			boolean newbin = true;
			ListIterator<Integer> np = numberspacked.listIterator();
			while(np.hasNext()) {//loop the numbers that have already been packed
				int p = np.next();
				if (b.contains(p)) {//this bin contains a piece that has already been packed
					newbin = false; break;
				}
			}
			if (newbin) {//include the bin in the child
				temporaryBinVector1.add(b);
				b.copypiecenumbers(numberspacked);
				i.remove();//remove the bin b from the binlist
			}
		}
		//		np1 = temporaryBinVector1.listIterator();
		//		while (np1.hasNext()) {
		//			Bin b = np1.next();
		//			b.print();
		//		}System.out.println();
		//phase 2, use best fit to add the items that remain to be packed
		//get rid of the items still in binlist that are already included in the child
		ListIterator<Bin> np1 = binlist.listIterator();
		while (np1.hasNext()) {
			Bin b = np1.next();
			ListIterator<Piece> piecesinbin = b.piecesInThisBin.listIterator();
			while (piecesinbin.hasNext()) {
				Piece p = piecesinbin.next();
				if (numberspacked.contains(new Integer((int)p.getNumber()))) {
					piecesinbin.remove();
				} else {
					numberspacked.add(new Integer((int)p.getNumber()));
				}
			}
			if (b.numberOfPiecesInThisBin() == 0) {
				np1.remove();
			}
		}
		//		np1 = binlist.listIterator();
		//		while (np1.hasNext()) {
		//			Bin b = np1.next();
		//			b.print();
		//		}System.out.println();
		Bin[] array = new Bin[binlist.size()];
		applyBestFit(binlist.toArray(array), temporaryBinVector1);
		//		np1 = temporaryBinVector1.listIterator();
		//		while (np1.hasNext()) {
		//			Bin b = np1.next();
		//			b.print();
		//		}System.out.println();
		sanitycheck(temporaryBinVector1);
	}

	private Vector<Bin> deepCopyBins(Vector<Bin> vectorToCopy) {//used in the applyheuristic method
		Vector<Bin> copy = new Vector<Bin>();
		for (int x = 0; x < vectorToCopy.size(); x++) {//loop the bins
			Bin b = vectorToCopy.get(x).clone();
			copy.add(b.clone());
		}//end looping the bins
		return copy;
	}//end method deepCopyBins

	private void sortbins(Vector<Bin> bins, boolean highestOrLowest) {//orders the bins largest first
		Collections.sort(bins);
		//check for only one empty bin at the end
		Bin endbin = bins.get(bins.size()-1);
		Bin endbin2 = bins.get(bins.size()-2);
		if (!(endbin.getFullness() == 0.0)) {//if the end bin is not empty
			System.err.println("The last bin is not empty, so there are no empty bins");
			System.exit(0);
		} else if (endbin2.getFullness() == 0.0) {//if the second bin is empty then there is more than one
			System.err.println("Error solution: \n");
			printtempbins(bins);
			System.err.println("There is more than one empty bin");
			System.exit(0);
		}//end if checking for errors
		if (!highestOrLowest) {//invert the sorting to lowest first
			int countleft = 0;
			int countright = bins.size()-2;
			for (int  x = 0; x < bins.size(); x++) {//loop the bins in the vector
				Bin temp = bins.remove(countleft);
				bins.add(countleft, bins.remove(countright-1));//minus 1 because we just removed the bin at 0 so the vector's one shorter
				bins.add(countright,temp);
				countleft++; countright--;
				if (countleft >= countright) {break;}
			}//end looping the bins
		}//end if we want to sort it with lowest first
	}//end method sortbins

	private void printtempbins(Vector<Bin> v) {//just for debugging
		//to test
		for (int binNumber = 0; binNumber < v.size(); binNumber++) {//loop the bins
			Bin b = v.get(binNumber);
			System.out.print(binNumber + " ");
			System.out.print(b.addToString(""));
		}///
	}//*/

	public int getNumberOfHeuristics() {
		return  8;
	}

	public double applyHeuristic(int heuristicID, int source, int destination) {
		//System.out.println(heuristicID);
		long startTime = System.currentTimeMillis();
		Vector<Bin> temporaryBinVector = deepCopyBins(solutionMemory[source].solution);
		//check if its a crossover heuristic
		boolean isCrossover = false;
		int[] crossovers = getHeuristicsOfType(HeuristicType.CROSSOVER);
		if (!(crossovers == null)) {
			for (int x = 0; x < crossovers.length; x++) {
				if (crossovers[x] == heuristicID) {
					isCrossover = true;
					break;}
			}//end for looping the crossover heuristics
		}//end if
		if (isCrossover) {
			//a crossover has been asked to operate on just one solution. The solution will be returned unmodified
		} else {
			if (heuristicID == 0) {//swap a random piece with another random different piece
				applyHeuristic0(temporaryBinVector);
			} else if (heuristicID == 1) {
				applyHeuristic1(temporaryBinVector);
			} else if (heuristicID == 2) {
				applyHeuristic2(temporaryBinVector);
			} else if (heuristicID == 3) {
				applyHeuristic3(temporaryBinVector);
			} else if (heuristicID == 4) {
				applyHeuristic4(temporaryBinVector);
			} else if (heuristicID == 5) {
				applyHeuristic5(temporaryBinVector);
			} else if (heuristicID == 6) {
				applyHeuristic6(temporaryBinVector);
			} else {
				System.err.println("Heuristic " + heuristicID + " does not exist");
				System.exit(0);
			}
			heuristicCallRecord[heuristicID]++;
			heuristicCallTimeRecord[heuristicID] += (int)(System.currentTimeMillis() - startTime);
		}//end checking if its a crossover heuristic
		//end choosing the heuristic to test
		//after applying the heuristic, sort the bins highest to lowest and remove any extra empty bins ...
		//this is for admin, to make sure there is always a consistent solution after the heuristic has been applied
		sortbins(temporaryBinVector, HIGHEST_FIRST);

		sanitycheck(temporaryBinVector);

		double newobjectiveFunctionValue = evaluateObjectiveFunction(temporaryBinVector);
		//check if this is the best solution ever found and store it if it is
		if (newobjectiveFunctionValue < bestEverObjectiveFunction) {
			bestEverObjectiveFunction = newobjectiveFunctionValue;
			bestEverNumberOfBins = temporaryBinVector.size()-1;
			bestEverSolution = deepCopyBins(temporaryBinVector);
		}//end if the new solution is the best found so far

		//copy the solution to the destination index
		if (solutionMemory[destination] == null) {// if the destination index does not contain an initialised solution then initialise it
			solutionMemory[destination] = new Solution();
		}
		solutionMemory[destination].solution = deepCopyBins(temporaryBinVector);

		return newobjectiveFunctionValue;
	}//end method applyHeuristic

	private boolean sanitycheck(Vector<Bin> v) {
		//check bin fullnesses, number of pieces, and total size of pieces
		int totalnumberofpieces = 0;
		double totalfullness = 0;
		Boolean[] allnumbers = new Boolean[numberOfPieces];
		for (int x = 0; x < v.size(); x++) {//loop bins
			Bin b = v.get(x);
			totalnumberofpieces += b.numberOfPiecesInThisBin();
			totalfullness += b.getFullness();
			ListIterator<Piece> i = b.piecesInThisBin.listIterator();
			while (i.hasNext()) {
				allnumbers[(int)(i.next().getNumber())] = true;
			}
			if (b.getFullness() > capacity) {
				System.err.println("bin " + x + " is overfilled");
				System.exit(0);
			}//end if
		}//end for
		for (int x = 0; x < numberOfPieces; x++) {
			if (!allnumbers[x]) {//one piece number is not in the solution
				System.err.println("piece number " + x + " is not present in the solution");
				System.exit(0);
			}
		}
		if (totalnumberofpieces != numberOfPieces) {
			System.err.println("there are not the correct number of pieces");
			System.exit(0);
		}
		if (totalfullness != totalpiecesize) {
			System.err.println("the pieces do not add up to the correct size");
			System.exit(0);
		}
		return true;
	}//end method sanitycheck

	public double applyHeuristic(int heuristicID, int source1, int source2, int destination) {

		long startTime = System.currentTimeMillis();
		Vector<Bin> temporaryBinVector = deepCopyBins(solutionMemory[source1].solution);
		Vector<Bin> temporaryBinVector2 = deepCopyBins(solutionMemory[source2].solution);
		//check if its a crossover heuristic
		boolean isCrossover = false;
		int[] crossovers = getHeuristicsOfType(HeuristicType.CROSSOVER);
		if (!(crossovers == null)) {
			for (int x = 0; x < crossovers.length; x++) {
				if (crossovers[x] == heuristicID) {
					isCrossover = true;
					break;}
			}//end for looping the crossover heuristics
		}//end if
		if (isCrossover) {
			if (heuristicID == 7) {
				applyHeuristic7(temporaryBinVector, temporaryBinVector2);
			} else {
				System.err.println("Heuristic " + heuristicID + " is not a crossover operator");
				System.exit(0);
			}
		} else {
			if (heuristicID == 0) {//swap a random piece with another random different piece
				applyHeuristic0(temporaryBinVector);
			} else if (heuristicID == 1) {
				applyHeuristic1(temporaryBinVector);
			} else if (heuristicID == 2) {
				applyHeuristic2(temporaryBinVector);
			} else if (heuristicID == 3) {
				applyHeuristic3(temporaryBinVector);
			} else if (heuristicID == 4) {
				applyHeuristic4(temporaryBinVector);
			} else if (heuristicID == 5) {
				applyHeuristic5(temporaryBinVector);
			} else if (heuristicID == 6) {
				applyHeuristic6(temporaryBinVector);
			} else {
				System.err.println("Heuristic " + heuristicID + "does not exist");
				System.exit(0);
			}
			heuristicCallRecord[heuristicID]++;
			heuristicCallTimeRecord[heuristicID] += (int)(System.currentTimeMillis() - startTime);
		}//end checking if its a crossover heuristic
		//end choosing the heuristic to test
		//after applying the heuristic, sort the bins highest to lowest and remove any extra empty bins ...
		//this is for admin, to make sure there is always a consistent solution after the heuristic has been applied
		sortbins(temporaryBinVector, HIGHEST_FIRST);

		sanitycheck(temporaryBinVector);

		double newobjectiveFunctionValue = evaluateObjectiveFunction(temporaryBinVector);
		//check if this is the best solution ever found and store it if it is
		if (newobjectiveFunctionValue < bestEverObjectiveFunction) {
			bestEverObjectiveFunction = newobjectiveFunctionValue;
			bestEverNumberOfBins = temporaryBinVector.size()-1;
			bestEverSolution = deepCopyBins(temporaryBinVector);
		}//end if the new solution is the best found so far

		//copy the solution to the destination index
		if (solutionMemory[destination] == null) {// if the destination index does not contain an initialised solution then initialise it
			solutionMemory[destination] = new Solution();
		}
		solutionMemory[destination].solution = deepCopyBins(temporaryBinVector);

		return newobjectiveFunctionValue;
	}//end applyheuristic

	public void copySolution(int source, int destination) {
		Vector<Bin> temporaryBinVector = deepCopyBins(solutionMemory[source].solution);
		if (solutionMemory[destination] == null) {// if the destination index does not contain an initialised solution then initialise it
			solutionMemory[destination] = new Solution();
		}
		solutionMemory[destination].solution = temporaryBinVector;
	}

	public String solutionToString(int index) {
		String s = "";
		for (int binNumber = 0; binNumber < solutionMemory[0].size(); binNumber++) {//loop the bins
			Bin b = solutionMemory[0].get(binNumber);
			s += binNumber + " ";
			s = b.addToString(s);
		}//end looping the bins of the final solution
		return s;
	}

	public String bestSolutionToString() {
		String s = "";
		s += "Best Solution Found:" + "\n";
		for (int binNumber = 0; binNumber < bestEverSolution.size(); binNumber++) {//loop the bins
			Bin b = bestEverSolution.get(binNumber);
			s = b.addToString(s);
		}//end looping the bins of the final solution
		s += "Objective Function Value: " + bestEverNumberOfBins + "\n";
		return s;
	}

	public double getFunctionValue(int index) {
		return evaluateObjectiveFunction(this.solutionMemory[index].solution);
	}

	public double getBestSolutionValue() {
		return bestEverObjectiveFunction;
	}

	public void setMemorySize(int size) {
		Solution[] newSolutionMemory = new Solution[size];
		if (solutionMemory != null) {
			for (int x = 0; x < solutionMemory.length; x++) {//copy each solution into the new memory
				if (x < size) {//checks that we do not try to go beyond the length of the new solution memory
					newSolutionMemory[x] = solutionMemory[x];
				}//end if
			}//end looping the current solutionmemory
		}
		solutionMemory = newSolutionMemory;
	}//end method setMemorySize

	public int getNumberOfInstances() {
		return 10;
	}

	public String toString() {
		return "BinPacking";
	}

	public boolean compareSolutions(int solutionIndex1, int solutionIndex2) {
		Solution s1 = solutionMemory[solutionIndex1];
		Solution s2 = solutionMemory[solutionIndex2];
		if (s1.size() != s2.size()) {
			return false;}
		for (int i = 0; i < s1.size(); i++) {
			Bin b1 = s1.get(i);Bin b2 = s2.get(i);
			int piecesinb1 = b1.numberOfPiecesInThisBin();
			int piecesinb2 = b2.numberOfPiecesInThisBin();
			if ((b1.compareTo(b2) != 0) || (piecesinb1 != piecesinb2)) {
				return false;
			} else {//loop the individual pieces
				for (int x = 0; x < piecesinb1; x++) {
					if (b1.getPieceSize(x) != b2.getPieceSize(x)) {
						return false;
					}
				}
			}//end if else
		}//end looping bins
		return true;
	}//end comparesolutions

	public int[] getHeuristicsOfType(HeuristicType hType) {
		switch (hType)
		{
		case LOCAL_SEARCH : return localSearches;
		case RUIN_RECREATE : return ruinRecreates;
		case MUTATION : return mutations;
		case CROSSOVER : return crossovers;
		default: return null;
		}
	}

	class returnSolution {
		public double value;
		public Vector<Piece> solution; 
		public returnSolution(Vector<Piece> s, double v) {
			this.solution = s;
			this.value = v;
		}
	}

	class Solution {
		public Vector<Bin> solution;
		public Solution() {
			solution = new Vector<Bin>();
		}
		public void addBin(Bin b) {
			solution.add(b);
		}
		public int size() {
			return this.solution.size();
		}
		public Bin get(int index) {
			return this.solution.get(index);
		}
		public void set(int index, Bin b) {
			this.solution.set(index, b);
		}
	}


}//end class binPacking
