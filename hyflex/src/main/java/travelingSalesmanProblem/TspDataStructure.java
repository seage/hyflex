package travelingSalesmanProblem;



/**
 * @author Antonio
 *
 */
/**
 * @author Antonio
 *
 */
public abstract class TspDataStructure {


	public static TspDataStructure create(int[] tour){
		if(tour.length <= 5000)
			return new arrayRepresentation(tour);
		return new TwoLayList(tour);
	}
	
	/**
	 * @param v 
	 * @return The city that is next to v.
	 */
	public abstract int next(int v);

	/**
	 * @param v 
	 * @return The city that is previous to v.
	 */
	public abstract int prev(int v);

	/**
	 * @param a
	 * @param b
	 * @param c
	 * @return true if b is between a and c, false otherwise, 
	 *         false if b==a or b==c 
	 */
	public abstract boolean sequence(int a, int b, int c);

	
	/**
	 * Reverses the path between cities with indices ib and id.
	 * This method typically requires a time close to O( sqrt(numbCities) ).
	 * @param ib
	 * @param id  
	 */
	public abstract void flip(int b, int d);
	public abstract String toString();
	public abstract String toString(int startCity);
	
	/** 
	 * @return permutation representing a tour starting at the specified city
	 */
	public abstract int[] returnTour(int startCity);
	
	/**
	 * Saves in tour[] the permutation representing a tour starting at the specified city
	 * @param startCity
	 * @param tour array where tour will be saved
	 */
	public abstract void returnTour(int startCity, int[] tour);

}

class TwoLayList extends TspDataStructure{
	private  Node[] list;			   
	private Parent[] parents;
	private int numbCities;	
	private int numbParents;		   
	private int maxSegmentSize;	// set to (int)(0.5*sqrt(numbCities)) + 1
	public int sumNodes = 0;
	public int sumFlips = 0;
	
	/**
	 * @return Instance of TwoLayList representing the given
	 * permutation. 
	 * @param permutation A tour starting at city 0
	 */
	public TwoLayList(int[] permutation) {
		numbCities = permutation.length;
		maxSegmentSize = (int)(0.5*Math.sqrt(numbCities)) + 1;
		numbParents = (int)Math.sqrt(numbCities);		
		int numbCitiesPerSegment = (int)(numbCities/numbParents) + 1;	
		
		//Step 1. Initialise doubly linked lists (Nodes and Parents)
		initialiseList(permutation); 			 					
		initialiseParents(numbCitiesPerSegment); 
		
		// Step 2. Link Node and Parent doubly lists.
		// All cities (Node list) in a segment point to the same parent.
		// Each parent points to the first and last cities in its
		// segment.
		int count = 0;
		Parent tempP;
		for(int i = 0; i < numbParents - 1; i++){			
			tempP = parents[i];
			list[permutation[count]].parent = tempP;				//set parent pointer
			tempP.startSegment = list[permutation[count]];		//start of segment		
			for(int j = 1; j < numbCitiesPerSegment; j++){
				list[permutation[count]].parent = tempP;
				count++;
			}
			
			tempP.endSegment = list[permutation[count-1]];		//end of segment
		}
		
		// Step 2. Continuation for the last segment (smaller)
		tempP = parents[numbParents-1];
		tempP.startSegment = list[permutation[count]];			//start of segment
		for(; count < numbCities; count++){
			list[permutation[count]].parent = tempP;				//set parent pointer
		}
		tempP.endSegment = list[permutation[numbCities-1]];				//end of segment
	}
	/**
	 * @param v 
	 * @return The node of the city that is next to v.
	 */
	public Node next(Node v){
		if(!v.parent.reverse)
			return v.next;
		return v.previous;		
	}
	/**
	 * @param v 
	 * @return The node of the city that is previous to v.
	 */
	public Node prev(Node v){
		if(!v.parent.reverse)
			return v.previous;
		return v.next;
	}	
	/**
	 * @param v 
	 * @return The index of the city that is next to v.
	 */	
	public int next(int v){
		if(!list[v].parent.reverse)
			return list[v].next.city;
		return list[v].previous.city;		
	}
	/**
	 * @param v 
	 * @return The index of the city that is previous to v.
	 */		
	public int prev(int v){
		if(!list[v].parent.reverse)
			return list[v].previous.city;
		return list[v].next.city;		
	}
	/**
	 * @param a
	 * @param b
	 * @param c
	 * @return true if b is between a and c, false otherwise
	 */
	public boolean sequence(int a, int b, int c){		
		
		Parent pa = list[a].parent,
			   pb = list[b].parent,
			   pc = list[c].parent;
		
		int    pa_id = pa.id,
			   pb_id = pb.id,
			   pc_id = pc.id;
		
		//Case 1: The three parents are different
		if(pa != pb && pa != pc && pb != pc){		
			if(    (pa_id < pb_id && pb_id < pc_id) 
			    || (pa_id > pc_id && (pc_id > pb_id 
			    || pb_id > pa_id )))
				return true;
			
			return false;	
		}
		
		int a_id = list[a].id,
			b_id = list[b].id,
			c_id = list[c].id;
		
		//Case 2: The three parents are the same
		if(pa == pb && pb == pc){
			// if no reversed
			if(!pa.reverse){
				if(    (a_id < b_id  && b_id  < c_id) 
				    || (a_id > c_id && ((c_id > b_id ) 
			        || (b_id > a_id))))
					return true;
				return false;					
			}
			else{
				if(    (a_id > b_id && b_id > c_id)
				    || (a_id < c_id && ((c_id < b_id) 
				    || (b_id < a_id))))
					return true;
				return false;				
			}
		}
		
		//Case 3: Parents a and b are the same
		if(pa == pb){
				if(!pa.reverse){
					if(a_id < b_id)
						return true;
				}else{
					if(a_id > b_id)
						return true;			
				}
			return false;		
		}
		
		//Case 4: Parents a and c are the same
		if(pa == pc){
			if(!pa.reverse){
				if(a_id > c_id)
					return true;
			}else{
				if(a_id < c_id)
					return true;			
			}
			return false;	
		}
		
		//Case 5: Parents b and c are the same
		if(!pb.reverse){
			if(b_id < c_id)
				return true;
		}else{
			if(b_id > c_id)
				return true;			
		}
		return false;		
		
	}
	/**
	 * Reverses the path between cities with indices ib and id.
	 * This method typically requires a time close to O(sqrt(numbCities)), 
	 * though this is not guaranteed.
	 * @param ib
	 * @param id  
	 */
	public void flip(int ib, int id) {
		if(ib == id) return;
		Node b = list[ib];
		Node d = list[id];
		if(next(d) == b || prev(b) == next(d)){ //reversing full tour
			reverseTour();
			return;
		}
		Node a = prev(b);
		Node c = next(d);
		
		if (b.parent == d.parent) { // if b and d are in the same segment
			if (c.parent == a.parent) { // if a and c are also in the same segment
				if ((b.id < d.id && !b.parent.reverse) || 
					(b.id > d.id && b.parent.reverse)){ // case 1: b precedes d
					if (Math.abs(b.id - d.id) > this.maxSegmentSize) {
						splitAFirst(b);
						splitALast(d);
					}
					flipSubSegment(b, d);
				} 
				else { // case 2: d precedes b
					 if(Math.abs(c.id - a.id) > this.maxSegmentSize){
						 splitAFirst(c); 
						 splitALast(a);
					 }
					flipSubSegment(c, a);
					reverseTour();
				}
			} 
			else {// b and d are in the same segment but not a and c
				 if(Math.abs(b.id - d.id) > this.maxSegmentSize){
				    splitAFirst(b); 
				    splitALast(d);
				 }
				flipSubSegment(b, d);
			}
			
		} else {// b and d are not in the same segment
			if (c.parent == a.parent) {// if c and a are in the same segment
				if (Math.abs(c.id - a.id) > this.maxSegmentSize) {
						splitAFirst(c);
						splitALast(a);				    
				}
				flipSubSegment(c, a);
				reverseTour();		
				
			} else {// neither (b and d) nor (a and c) are in the same segment
				
				//Case 1: the path b-d is shorter than a-c.
				if (Math.abs(b.parent.id - d.parent.id) <= 
					Math.abs(a.parent.id - c.parent.id) &&
					d.parent.next != b.parent) {
					if (b.parent.returnStart() != b)
						splitAFirst(b);
					if (b.parent != d.parent && d.parent.returnEnd() != d)
						splitALast(d);
					if (b.parent == d.parent){
						if(Math.abs(b.id - d.id) > this.maxSegmentSize){
							splitAFirst(b);
							splitALast(d);
						}
						flipSubSegment(b, d);
					}
					else
						if (b.parent.id > d.parent.id)
							flipContiguousSegments2(b.parent, d.parent);
						else
							flipContiguousSegments(b.parent, d.parent);
					
				} else {//Case 2: the path a-c is shorter than b-d.
					if (c.parent.returnStart() != c)
						splitAFirst(c);
					if (c.parent != a.parent && a.parent.returnEnd() != a)
						splitALast(a);			
					if (c.parent == a.parent) {
						if(Math.abs(c.id-a.id)>this.maxSegmentSize){
							splitAFirst(c);
							splitALast(a);
						}
							flipSubSegment(c, a);
							reverseTour();
					} else {
						if (c.parent.id > a.parent.id)
							flipContiguousSegments2(c.parent, a.parent);
						else
							flipContiguousSegments(c.parent, a.parent);
						reverseTour();
					}
				}
			}
		}
	}	

	
	//UTILITY METHODS	
	/**
	 * @param startCity
	 * @return the tour as a permutation starting at startCity
	 */
	public String toString(int startCity){
		int[] pi = returnTour(startCity);
		StringBuilder stb = new StringBuilder();
		for(int i = 0; i < numbCities; i++){
			stb.append(pi[i]+" ");
		}
		return stb.toString();
	}			
	/**
	 * @see java.lang.Object#toString()
	 * @return the tour as a permutation starting at city 0.
	 */
	public String toString(){
		return toString(0);
	}	
	/**
	 * @param startCity
	 * @return the incumbent tour given as a permutation and starting at startCity
	 */
	public int[] returnTour(int startCity){
		int[] pi = new int[this.numbCities];
		returnTour(startCity, pi);
		return pi;
	}
	
	@Override	
	public void returnTour(int startCity, int[] pi) {
		int count = 0;
		Node start = list[startCity];
		Node aux = next(start);
		pi[0] = start.city;
		count++;
		while(aux != start){
			pi[count] = aux.city;			
			aux = next(aux);
			count++;
		}
	}
	
	
	
	//PRIVATE METHODS		
	/**
	 * Initialises a doubly linked list of nodes representing the 
	 * given permutation. 
	 */
	private void initialiseList(int[] permutation){
		this.list = new Node[numbCities];
			
		//Step 1. Create nodes
		for(int i = 0; i < numbCities; i++)
			list[i] = new Node(i);
		
		//Step 2. Initialise id values
		for(int i = 0; i < numbCities; i++)
			list[permutation[i]].id = numbCities+i; 
		
		//Step 3. Create previous and next links	
		list[permutation[0]].previous = list[permutation[numbCities-1]];
		for(int i = 1; i < numbCities; i++)
			list[permutation[i]].previous = list[permutation[i-1]];
		
		list[permutation[numbCities-1]].next = list[permutation[0]];
		for(int i = 0; i < numbCities - 1; i++)
			list[permutation[i]].next = list[permutation[i+1]];		
	}
	/**
	 * Initialises a doubly linked list of parent nodes
	 */	
	private void initialiseParents(int size){
		int smallSize = numbCities - (numbParents - 1)*size; //Numb of cities in the last segment
		if(smallSize < this.numbParents/2){
			size--;
			smallSize += this.numbParents - 1;
		}
		parents = new Parent[this.numbParents];
		
		for(int i = 0; i < this.numbParents - 1; i++)
			parents[i] = new Parent(i);
		
		parents[this.numbParents - 1] = new Parent(this.numbParents - 1);		
		
		parents[0].previous = parents[this.numbParents - 1];
		for(int i = 1; i < this.numbParents; i++)
			parents[i].previous = parents[i-1];
		
		parents[this.numbParents - 1].next = parents[0];
		for(int i = 0; i < this.numbParents - 1; i++)
			parents[i].next = parents[i+1];
	}	
	/**
	 * Reverses the path b-d, where b and d must belong to the
	 * same segment.
	 */
	private void flipSubSegment(Node b, Node d) {
		this.sumFlips ++;
		this.sumNodes += (Math.abs(b.id - d.id));
		Parent p = b.parent;

		// if b and d are the start and end of the segment,
		if (p.returnStart() == b && p.returnEnd() == d) {
			flipSegment(b, d, prev(b), next(d)); // flip the whole segment
			return;
		}

		// make sure b.id < d.id
		if (b.id > d.id) {
			Node c = b;
			b = d;
			d = c;
		}

		Node a = b.previous, c = d.next;

		// Step 1. Reconnect c and a
		if (c.parent.reverse != p.reverse) {
			c.next = b;
			a.next = d;
		} else {
			c.previous = b;
			if (a.parent.reverse != p.reverse)
				a.previous = d;
			else
				a.next = d;
		}

		// Step 2. Reverse path b-d (two cases)
		if (b.next == d) { // Case 1: b and d are adjacent
			d.previous = a;
			d.next = b;
			b.previous = d;
			b.next = c;
		} else { // Case 2: b and d are not adjacent
			d.next = d.previous;
			Node aux = d.next;
			while (aux != b) {//next = previous
				aux.next = aux.previous;
				aux = aux.previous;
			}
			b.next = c;
			d.previous = a;
			aux = d;
			while (aux != b) {//previous = next
				aux.next.previous = aux;
				aux = aux.next;
			}
		}

		// Step 3. Update start and end segment references in parent node
		if (p.startSegment == b)
			p.startSegment = d;
		else 
			if (p.endSegment == d)
				p.endSegment = b;

		// Step 4. Update id values
		d.id = b.id;
		Node aux = d.next;
		while (aux != b) {
			aux.id = aux.previous.id + 1;
			aux = aux.next;
		}
		b.id = b.previous.id + 1;
		this.updateAllRanks(b.parent);
		
		//this.updateAllRanks(b.parent);
	}
	/**
	 * Reverses an entire segment, where b and d are the start
	 * and the end of the segment, respectively, and a = prev(b) 
	 * and c = next(d) are the segment's external links.
	 */
	private void flipSegment(Node b, Node d, Node a, Node c){
		
		// 1. complement reverse bit in parent
		b.parent.reverse = !b.parent.reverse;
		
		// 2. Swap segment external links
		b.setNext(c);
		c.setPrevious(b);		
		d.setPrevious(a);
		a.setNext(d);
		
		//Note that 1 and 2 must be performed in this order. 
	}
	/**
	 * Reverses the path of segments pb-pd, where
	 * pb must precede pd, i.e. pb.id < pd.id.
	 */
	private void flipContiguousSegments(Parent pb, Parent pd){
		Parent pTemp;

		// Step 1: Swap edges of the start and end nodes
		// in segments pb and pd with the nodes in
		// the neighbouring segments, i.e. a and c.
		Node b = pb.returnStart();
		Node c = pd.returnEnd();
		Node a = prev(b);
		Node d = next(c);
		a.setNext(c);
		b.setPrevious(d);
		c.setNext(a);
		d.setPrevious(b);

		// Step 2 (two cases): reverse edges in parents linked list
		Parent pa = pb.previous, pc = pd.next;
		pa.next = pd;
		pc.previous = pb;

		if (pb.next == pd) {// Case 1: pb and pd are adjacent
			pb.next = pc;
			pb.previous = pd;
			pd.previous = pa;
			pd.next = pb;
		} else {// Case 2: pb and pd are not adjacent
			pd.next = pd.previous;
			pTemp = pd.next;
			while (!pTemp.equals(pb)) {
				pTemp.next = pTemp.previous;
				pTemp = pTemp.previous;
			}
			pb.next = pc;
			pd.previous = pa;
			pTemp = pd;
			while (!pTemp.equals(pb)) {
				pTemp.next.previous = pTemp;
				pTemp = pTemp.next;
			}
		}

		// Steps 3 and 4: update parent id values (ranks)
		// and complement parent reverse bits
		pd.id = pb.id;
		pd.reverse = !pd.reverse;
		pTemp = pd.next;
		while (pTemp != pb) {
			pTemp.reverse = !pTemp.reverse;
			pTemp.id = pTemp.previous.id + 1;
			pTemp = pTemp.next;
		}
		pb.id = pb.previous.id + 1;
		pb.reverse = !pb.reverse;
	}
	/**
	 * Reverses the path of segments pb-pd, where
	 * pb must succeed pd, i.e. pb.id > pd.id.
	 */
	private void flipContiguousSegments2(Parent pb, Parent pd){
		Parent pTemp;
		
		// Step 1: Swap edges of the start and end nodes
		// in segments pb and pd with the nodes in
		// the neighbouring segments, i.e. a and c.
		Node b = pb.returnStart();
		Node c = pd.returnEnd();					
		Node a = prev(b);
		Node d = next(c);
		a.setNext(c);
		b.setPrevious(d);
		c.setNext(a);
		d.setPrevious(b);				

		// Step 2 (two cases): reverse edges in parents linked list
		Parent pa = pb.previous, pc = pd.next;
		pa.next = pd;
		pc.previous = pb;		
				
		if(pb.next == pd){ // Case 1: pb and pd are adjacent	
			pb.next = pc;
			pb.previous = pd;
			pd.previous = pa;
			pd.next = pb;
		} else{ // Case 2: pb and pd are not adjacent
			pb.previous = pb.next;
			pTemp = pb.next;
			while(!pTemp.equals(pd)){
				pTemp.previous = pTemp.next;
				pTemp = pTemp.previous;
			}
			pd.previous = pa;
			pb.next = pc;				
			pTemp = pb;
			while(!pTemp.equals(pd)){
				pTemp.previous.next = pTemp;
				pTemp = pTemp.previous;
			}
		}
		
		// Step 3: update parent id values (ranks)
		updateParentRanks();
		
		// Step 4: complement parent reverse bits
		pTemp = pb;
		pTemp.reverse = !pTemp.reverse;		
		while(pTemp != pd){
			pTemp = pTemp.previous;
			pTemp.reverse = !pTemp.reverse;		   
		}		
	}
	
	private void updateParentRanks(){
		Parent start = list[0].parent;
		int count = 0;
		start.id = count;
		Parent aux = start.next;
		while(aux != start){
			count++;
			aux.id = count;
			aux = aux.next;
		}
	}
	
	/**
	 * Splits a's segment in such a way that a is the first 
	 * city of its segment. Either all nodes previous to a are moved
	 * to the end of the previous segment, or a and its successive nodes are
	 * moved to the start of the next segment.
	 */
	private void splitAFirst(Node a){
		
		if(Math.abs(a.parent.returnStart().id - a.id)//Case 0: Move cities before a	
		   <= Math.abs(a.parent.returnEnd().id - a.id ) + 1){ //to previous segment
			
			Node aux = a.parent.returnStart();
			Parent pTemp = aux.parent.previous;
			Node aux2 = pTemp.returnEnd();
			if(aux.parent.reverse){
				if(aux.parent.reverse == pTemp.reverse){
					while(aux != a){
						aux.parent = pTemp;
						aux = aux.previous;
					}
				}
				else{
					Node aux3;
					while(aux != a){
						aux.parent = pTemp;
						aux3 = aux.next;
						aux.next = aux.previous;
						aux.previous = aux3;						
						aux = aux.next;
					}					
				}
			}
			else{
				if(aux.parent.reverse == pTemp.reverse){
					while(aux != a){
						aux.parent = pTemp;
						aux = aux.next;
					}
				}
				else{
					Node aux3;
					while(aux != a){
						aux.parent = pTemp;
						aux3 = aux.next;
						aux.next = aux.previous;
						aux.previous = aux3;						
						aux = aux.previous;
					}					
				}				
			}
			pTemp.setEnd(prev(aux));
			a.parent.setStart(a);
			//this.updateAllRanks(pTemp);
			updateRanksForw(aux2);			
		}
		else{// Case 2: Move a and following cities to next segment 
			Node aux = a.parent.returnEnd();			
			Parent pTemp = aux.parent.next;
			Node aux2 = pTemp.returnStart();
			if(aux.parent.reverse){		
				if(aux.parent.reverse == pTemp.reverse){
					while(aux != a){
						aux.parent = pTemp;
						aux = aux.next;
					}
				}
				else{
					Node aux3;
					while(aux != a){
						aux.parent = pTemp;
						aux3 = aux.next;
						aux.next = aux.previous;
						aux.previous = aux3;						
						aux = aux.previous;
					}					
				}
			}
			else{
				if(aux.parent.reverse == pTemp.reverse){
					while(aux != a){
						aux.parent = pTemp;
						aux = aux.previous;
					}
				}
				else{
					Node aux3;
					while(aux != a){
						aux.parent = pTemp;
						aux3 = aux.next;
						aux.next = aux.previous;
						aux.previous = aux3;						
						aux = aux.next;
					}					
				}			
			}			
			a.parent.setEnd(prev(a));
			if(a.parent.reverse != pTemp.reverse){
				aux = a.next;
				a.next = a.previous;
				a.previous = aux;
			}
			a.parent = pTemp;
			a.parent.setStart(a);
			updateRanksBack(aux2);
			//updateAllRanks(pTemp);
		}		
	}
	/**
	 * Splits a's segment in such a way that a is the last 
	 * city of its segment. Either a and all nodes previous to a are moved
	 * to the end of the previous segment, or its successive nodes are
	 * moved to the start of the next segment.
	 */
	private void splitALast(Node a){
		if(Math.abs(a.parent.returnStart().id - a.id) + 1
				<= Math.abs(a.parent.returnEnd().id - a.id )){
			Node aux = a.parent.returnStart();
			Parent pTemp = aux.parent.previous;
			Node aux2 = pTemp.returnEnd();			
			if(aux.parent.reverse){
				if(aux.parent.reverse == pTemp.reverse){
					while(aux != a){
						aux.parent = pTemp;
						aux = aux.previous;
					}
				}else{
					Node aux3;
					while(aux != a){
						aux.parent = pTemp;
						aux3 = aux.next;
						aux.next = aux.previous;
						aux.previous = aux3;
						aux = aux.next;
					}					
				}
			}else{
				if(aux.parent.reverse == pTemp.reverse){
					while(aux != a){
						aux.parent = pTemp;
						aux = aux.next;
					}
				}else{
					Node aux3;
					while(aux != a){
						aux.parent = pTemp;
						aux3 = aux.next;
						aux.next = aux.previous;
						aux.previous = aux3;						
						aux = aux.previous;
					}					
				}
			}
			a.parent.setStart(next(a));
			if(a.parent.reverse != pTemp.reverse){
				aux = a.next;
				a.next = a.previous;
				a.previous = aux;
			}
			a.parent = pTemp;
			pTemp.setEnd(a);
			updateRanksForw(aux2);
			//this.updateAllRanks(pTemp);
		}
		else{
			Node aux = a.parent.returnEnd();
			Parent pTemp = aux.parent.next;
			Node aux2 = pTemp.returnStart();	
			if(aux.parent.reverse){
				if(aux.parent.reverse == pTemp.reverse){
					while(aux != a){
						aux.parent = pTemp;							
						aux = aux.next;
					}
				}else{
					Node aux3;
					while(aux != a){
						aux.parent = pTemp;
						aux3 = aux.next;
						aux.next = aux.previous;
						aux.previous = aux3;
						aux = aux.previous;
					}					
				}
			}
			else{
				if(aux.parent.reverse == pTemp.reverse){
					while(aux != a){
						aux.parent = pTemp;
						aux = aux.previous;
					}				
				}else{
					Node aux3;
					while(aux != a){
						aux.parent = pTemp;
						aux3 = aux.next;
						aux.next = aux.previous;
						aux.previous = aux3;						
						aux = aux.next;
					}							
				}
			}
			pTemp.setStart(next(a));
			a.parent.setEnd(a);
			//updateAllRanks(pTemp);
			updateRanksBack(aux2);
		}
	}
	/**
	 * Updates the node id's starting from a and towards the 
	 * end of the segment.
	 */
	private void updateRanksForw(Node a) {

		if (!a.parent.reverse) {
			Node aux = a;
			Node end = a.parent.endSegment;
			int count = a.id;
			while (aux != end) {
				aux.id = count;
				aux = aux.next;
				count++;
			}
			end.id = count;
		} else {
			Node aux = a;
			Node end = a.parent.startSegment;
			int count = a.id;
			while (aux != end) {
				aux.id = count;
				aux = aux.previous;
				count--;
			}
			end.id = count;
		}
	}
	/**
	 * Updates the node id's starting from a and towards the 
	 * beginning of the segment.
	 */
	private void updateRanksBack(Node a){
		if (!a.parent.reverse) {
			Node aux = a;
			Node end = a.parent.startSegment;
			int count = a.id;
			while (aux != end) {
				aux.id = count;
				aux = aux.previous;
				count--;
			}
			end.id = count;
		} else {
			Node aux = a;
			Node end = a.parent.endSegment;
			int count = a.id;
			while (aux != end) {
				aux.id = count;
				aux = aux.next;
				count++;
			}
			end.id = count;
		}		
	}
	/**
	 * Reverses the direction of the whole tour.
	 * Swaps the next and previous edges and complements
	 * the reversed bit of all parent nodes
	 */
	private void reverseTour(){
		Parent temp, temp2;
		
		//Step 1: swap previous and next links
		for(int i = 0; i < numbParents; i++){
			temp = parents[i];
			temp2 = temp.previous;
			temp.previous = temp.next;
			temp.next = temp2;
			temp.reverse = !temp.reverse;
		}
		
		//Step 2. Update ranks
		int count = 0;
		parents[0].id = count;
		temp = parents[0].next;
		while(temp != parents[0]){
			count ++;
			temp.id = count;
			temp = temp.next;
		}			
	}

	private void updateAllRanks(Parent parent){
		Node aux = parent.startSegment;
		Node end = parent.endSegment;
		int count = numbCities;
		while(aux != end){
			aux.id = count;
			aux = aux.next;
			count++;
		}
		end.id = count;
	}
	
	public int[] getSegmentSizes(){
		int[] segSizes = new int[numbParents];
		for(int i = 0; i < numbParents; i++){
			segSizes[i] = Math.abs(parents[i].startSegment.id - parents[i].endSegment.id);
		}
		return segSizes;
	}
	public int getNumbParents(){
		return this.numbParents;
	}
	public boolean verifyEnumeration(){
		Parent p;
		int count = 0, count2 = 0;
		Node next;
		for(int i = 0; i < this.numbParents; i++){
			p = parents[i];
			count = p.startSegment.id;
			count2 = count;
			next = p.startSegment.next;
			while(next != p.endSegment && p.startSegment != p.endSegment){
				count++;
				count2--;
				if( !(next.id == count || next.id == count2) )
					return false;
				next = next.next;
			}
		}
		return true;
	}
	
	
	//SUPPORTING CLASSES
	private class Node{
		private int id, city;
		private Node previous, next;
		private Parent parent;		
		private Node(int city){
			this.city = city;
		}
		private void setNext(Node next){
			if(parent.reverse){
				this.previous = next;
				return;
			}
			this.next = next;
		}
		private void setPrevious(Node previous){
			if(parent.reverse){
				this.next = previous;
				return;
			}
			this.previous = previous;
		}			
	}
	private class Parent{
		private boolean reverse;
		private int id;
		private Parent previous, next;
		private Node startSegment, endSegment;
		private Parent(int id){
			this.id = id;
			this.reverse = false;		
		}	    
		private Node returnStart(){
	    	if(reverse)
	    		return endSegment;
	    	return startSegment;
	    }	    
		private Node returnEnd(){
	    	if(reverse)
	    		return startSegment;
	    	return endSegment;	    	
	    }		
		private void setStart(Node a){
			if(reverse)
				this.endSegment = a;
			else
				this.startSegment = a;
		}
		void setEnd(Node a){
			if(reverse)
				this.startSegment = a;
			else
				this.endSegment = a;
		}
		
	}
}


class arrayRepresentation extends TspDataStructure {

	private int[] tour;
    private int[] inverse;            //inverse[i] = position of city i in tour 
	public boolean reversed = false;
	private int numbCities;

	//PUBLIC METHODS
	/**
	 * @param tour permutation representing a tour, starting at city 0.
	 * @return an array representation of the given tour.
	 */
	public arrayRepresentation(int[] tour) {
		this.tour = tour;
		this.numbCities = tour.length;
		this.inverse = new int[numbCities];
		for (int i = 0; i < this.numbCities; i++)
			this.inverse[tour[i]] = i;
	}	
	/* (non-Javadoc)
	 * @see travelingSalesmanProblem.tspDataStructure#next(int)
	 */
	public int next(int v) {
		if (!reversed) {
			if ((inverse[v] + 1) == numbCities)
				return tour[0];
			return tour[inverse[v] + 1];
		} else {
			if (inverse[v] == 0)
				return tour[numbCities - 1];
			return tour[inverse[v] - 1];
		}
	}
	/* (non-Javadoc)
	 * @see travelingSalesmanProblem.tspDataStructure#prev(int)
	 */
	public int prev(int v) {
		if (!reversed) {
			if (inverse[v] == 0)
				return tour[numbCities - 1];
			return tour[inverse[v] - 1];
		} else {
			if ((inverse[v] + 1) == numbCities)
				return tour[0];
			return tour[inverse[v] + 1];
		}
	}
	/* (non-Javadoc)
	 * @see travelingSalesmanProblem.tspDataStructure#sequence(int, int, int)
	 */
	public boolean sequence(int a, int b, int c) {
		int ia = inverse[a],
		    ib = inverse[b],
		    ic = inverse[c];
		if(!reversed){
			if(ia < ic){
				if(ib < ic && ib > ia)
					return true;
			}
			else{
				if(ib > ia || ib < ic)
					return true;
			}
		}
		else{
			if(ic < ia){
				if(ib < ia && ib > ic)
					return true;
			}
			else{
				if( ib < ia || ib > ic)
					return true;
			}
		}
		return false;
	}	
	/* (non-Javadoc)
	 * @see travelingSalesmanProblem.tspDataStructure#flip(int, int)
	 */
	public void flip(int b, int d) {
		
		//Special cases
		if(b == d)	//illegal movement
			return;				
		if (next(d) == b || prev(b) == next(d)){ //reversing whole tour
			reversed = !reversed;
			return;
		}
		
		//General cases (4)
		if(!reversed)			
			if(inverse[b] < inverse[d]){
				flip1(inverse[b], inverse[d]);	//Case 1: b.id < d.id and tour IS NOT reversed				
			}
			else
				flip2(inverse[b], inverse[d]);  //Case 2: b.id > d.id and tour IS NOT reversed
		
		else
			if(inverse[b] < inverse[d])
				flip3(inverse[b], inverse[d]);	//Case 3: b.id < d.id and tour IS reversed
			else
				flip4(inverse[b], inverse[d]);	//Case 4: b.id > d.id and tour IS reversed
	}		
	
	//UTILITY METHODS
	/* (non-Javadoc)
	 * @see travelingSalesmanProblem.tspDataStructure#toString()
	 */
	public String toString() {
		return toString(0);
	}
	/* (non-Javadoc)
	 * @see travelingSalesmanProblem.tspDataStructure#toString(int)
	 */	
	public String toString(int startCity){
		int[] tour = returnTour(startCity);
		StringBuilder stb = new StringBuilder();
		for (int i = 0; i < numbCities; i++) {
			stb.append(tour[i] + " ");
		}
		return stb.toString();		
	}
	/* (non-Javadoc)
	 * @see travelingSalesmanProblem.tspDataStructure#returnTour(int)
	 */
	public int[] returnTour(int startCity){
		int[] tour = new int[numbCities];
		returnTour(startCity, tour);
		return tour;
	}
	/* (non-Javadoc)
	 * @see travelingSalesmanProblem.tspDataStructure#returnTour(int, int[])
	 */
	public void returnTour(int startCity, int[] tour){
		tour[0] = startCity;
		int aux = next(startCity);
		for(int i = 1; i < numbCities; i++){
			tour[i] = aux;
			aux = next(aux);
		}
	}
	
	//PRIVATE METHODS
	/**
	 * Reverses the path between cities ib and id.
	 * Case 1: ib < id and tour is no reversed
	 */
	private void flip1(int ib, int id) {
	    //int ib = inverse[b], id = inverse[d];	
		if ((id - ib + 1) < (numbCities / 2))    // if path b-d shorter than c-a
			flipIn(ib, id);
		else {									 // path c-a shorter than b-d	
			reversed = !reversed;
			if (ib == 0)
				flipIn(id + 1, numbCities-1);
			else
				if (id == numbCities - 1)
					flipIn(0, ib - 1);
				 else{
					flipOut(ib - 1, id + 1);					
				 }
		}
	}
	/**
	 * Reverses the path between cities ib and id.
	 * Case 2: ib > id and tour is no reversed
	 */
	private void flip2(int ib, int id) {
		//int ib = inverse[b], id = inverse[d]; 				
		int	ia = ib - 1, ic = id + 1;		
		if ((ia - ic + 1) < (numbCities / 2)){
			reversed = !reversed;
			flipIn(ic, ia);
		}
		else {				
			flipOut(id, ib);
		}
	}
	/**
	 * Reverses the path between cities ib and id.
	 * Case 3: ib < id and tour is reversed
	 */
	private void flip3(int ib, int id){
		int ia = ib + 1;
		int ic = id - 1;
		
		// if path c-a is shorter than b-d
		if ((ic - ia + 1) < (numbCities / 2)) { 
			reversed = !reversed;
			this.flipIn(ia, ic);
		} else //path b-d is shorter than c-a 
			this.flipOut(ib, id);
	}
	/**
	 * Reverses the path between cities ib and id.
	 * Case 4: ib > id and tour is reversed
	 */
	private void flip4(int ib, int id){
		
		//if path b-d is shorter than c-a
		if ((ib - id + 1) < (numbCities / 2))		
			flipIn(id, ib);
		else {// path c-a is shorter than b-d
			reversed = !reversed;
			if (id == 0)
				flipIn(ib + 1, numbCities-1);
			else
				if (ib == numbCities - 1)
					flipIn(0, id - 1);
				 else
					flipOut(id - 1, ib + 1);
		}
	}
	/**
	 * Reverses the segment ib-id in an inner manner, i.e.
	 * each pair (ib, id), (ib+1,id-1), (ib+2, id-2)... swap places.
	 */
	private void flipIn(int ib, int id){
		int temp;
		int q = (id - ib + 1) / 2;		
		//Swapping (ib and id), (ib+1 and id-1), ...
		for (int count = 0; count < q; count++) {
			temp = tour[ib + count];
			inverse[tour[id - count]] = ib + count;
			tour[ib + count] = tour[id - count];
			inverse[temp] = id - count;
			tour[id - count] = temp;
		}
	}
	/**
	 * Reverses the segment ib-id in an outer manner, i.e.
	 * each pair (ib,id), (ib-1,id+1), (ib-2, id+2),... swap places.
	 */
	private void flipOut(int ib, int id){		
		int temp;
		int q = ib+1 < numbCities-id ? ib+1 : numbCities-id;// min(ib + 1, n - id);
		
		//Step 1. swap (ib with id), (ib-1 with id+1) ... 
		for (int count = 0; count < q; count++) {
			temp = tour[id + count];
			inverse[tour[ib - count]] = id + count;
			tour[id + count] = tour[ib - count];
			inverse[temp] = ib - count;
			tour[ib - count] = temp;
		}
				
		/* Step 2. Reverse non-swapped segments before ib or after id. 
		 *  This may or not be necessary. For example in the tour
		 *  1 2 3 4 5 6 7 8, 2 and 4 are flipped. In Step 1,
		 *  (2,4) and (1,5) are swapped giving 5 4 3 2 1 6 7 8. The path
		 *  6-8 has to be reversed giving 5 4 3 2 1 8 7 6.    
		 */
		
		//if there is a non-swapped path after id
		if((numbCities - 1) - id - q > 0){
			flipIn(id + q, numbCities-1); //reverse path
			
		}else{
			//if there is a non-swapped path before ib
			if(ib - q > 0){
				flipIn(0, ib - q); //reverse path
			}
		}
	}
	
}