package BinPacking;

class Piece implements Comparable<Piece> {

	private double size = 0.0;
	private int number;
	public Piece(double s, int n) {
		size = s;
		number = n;
	}
	public boolean equals(Object piece) {
		Piece p = (Piece)piece;
		if (p.number == this.number) { return true; } else { return false; }
	}
	double getSize() {
		return this.size;
	}
	double getNumber() {
		return this.number;
	}
	public Piece clone() {
		return new Piece(this.size, this.number);
	}
	public int compareTo(Piece p) {
		int ret = 0;
		if (this.size > p.size) {
			ret = -1;
		}
		if (this.size < p.size) {
			ret = 1;
		}
		return ret;
	}
}
