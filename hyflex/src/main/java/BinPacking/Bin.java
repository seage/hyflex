package BinPacking;
import java.util.ListIterator;
import java.util.Vector;
/**
*
* @author mvh
*/
class Bin implements Comparable<Bin> {

	Vector<Piece> piecesInThisBin;
	public Bin() {
		piecesInThisBin = new Vector<Piece>();
	}
	void addPiece(Piece p) {
		this.piecesInThisBin.add(p);
	}
	
	double getFullness() {
		double fullness = 0.0;
		for (int u = 0; u < this.piecesInThisBin.size(); u++) {
			Piece p = this.piecesInThisBin.get(u);
			//System.out.println(fullness + " " + p.getSize() );
			fullness += p.getSize();
			//System.out.println(fullness);
		}
		return fullness;
	}
	
	int numberOfPiecesInThisBin() {
		return piecesInThisBin.size();
	}
	
	String addToString(String s) {
		s += "[" + this.getFullness() + "]  [";
		for (int u = 0; u < this.piecesInThisBin.size(); u++) {
			Piece p = this.piecesInThisBin.get(u);
			s += p.getSize() + ", ";
		} s += "] \n";
		return s;
	}//end method printContents
	
	boolean contains(int num) {
		ListIterator<Piece> i = this.piecesInThisBin.listIterator();
		for (int u = 0; u < this.piecesInThisBin.size(); u++) {
			Piece item = i.next();
			if (item.getNumber() == num) {
				return true;
			}
		}
		return false;
	}
	
	void copypiecenumbers(Vector<Integer> v) {
		ListIterator<Piece> i = this.piecesInThisBin.listIterator();
		while (i.hasNext()) {
			v.add((int)(i.next().getNumber()));
		}
	}
	
	int contains(Piece p) {
		int index = -1;
		ListIterator<Piece> i = this.piecesInThisBin.listIterator();
		for (int u = 0; u < this.piecesInThisBin.size(); u++) {
			Piece item = i.next();
			if (item.getNumber() == p.getNumber()) {
				index = u;
			}
		}
		return index;
	}
	
	public Bin clone() {
		Bin copy = new Bin();
		for (int u = 0; u < this.piecesInThisBin.size(); u++) {
			Piece p = (this.piecesInThisBin.get(u)).clone();
			copy.addPiece(p);
		}
		return copy;
	}
	
	Piece removePiece(Piece p) {
		return this.piecesInThisBin.remove(piecesInThisBin.indexOf(p));
	}
	
	double getPieceSize(int index) {
		return (this.piecesInThisBin.get(index)).getSize();
	}
	
	Piece[] removeTwoPieces(int a, int b) {
		Piece[] twopieces = new Piece[2];
		Piece p1 = piecesInThisBin.get(a);
		Piece p2 = piecesInThisBin.get(b);
		twopieces[0] = this.removePiece(p1);
		twopieces[1] = this.removePiece(p2);
		return twopieces;
	}
	
	Piece removePiece(int index) {
		return this.piecesInThisBin.remove(index);
	}
	
	public int compareTo(Bin b) {
		int ret = 0;
		if (this.getFullness() > b.getFullness()) {
			ret = -1;
		}
		if (this.getFullness() < b.getFullness()) {
			ret = 1;
		}
		return ret;
	}

	void print() {
		ListIterator<Piece> i = this.piecesInThisBin.listIterator();
		System.out.print("[" + this.getFullness() + " ");
		while (i.hasNext()) {
			System.out.print(i.next().getNumber() + ",");
		}
		System.out.print("] ");
	}
}
