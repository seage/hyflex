package leangihh;

/**
 * 
 * The implementation of a generic limited capacity FIFO queue.
 * Elements are pushed at the front.
 * When adding a new element exceeds the capacity, an element is automatically 'dropped' at the end.
 * 
 * All supported operations have time complexity O(1):
 * - pushing a new element at the front
 * - random access read
 * - retrieving its size
 * 
 * @author Steven Adriaensen
 *
 * @param <T> The type of elements in the queue
 */
public class CircularBuffer<T> {
	private boolean full;
	private int last;
	private Object[] a;

	/**
	 * Creates an instance with given capacity
	 * @param maximum number of elements the queue may hold
	 */
	public CircularBuffer(int capacity){
		a = new Object[capacity];
	}
	
	/**
	 * @return the number of elements the queue currently holds
	 */
	public int size(){
		return full? a.length: last;
	}
	
	/**
	 * Adds el to the front of the queue (index 0)
	 * 
	 * @param el: element to be added to the front of the queue
	 */
	public void push(T el){
		if(last < a.length){
			a[last] = el;
			last++;
		}else{
			full = true;
			a[0] = el;
			last = 1;
		}
	}
	
	/**
	 * Retrieves the element at index in the queue (if any).
	 * 
	 * @param index: The index of the element to retrieve. Here index 0 holds the element most recently pushed and elements are kept in the order they were pushed.
	 * @return the element at index if any, null otherwise.
	 */
	@SuppressWarnings("unchecked")
	public T get(int index){
		return (T) a[(last-1-index+a.length)%a.length];
	}
	
}
