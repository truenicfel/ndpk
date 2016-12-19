package generics;

/**
 * <b>Generic class to store two different objects together.</b>
 * 
 * @param <T> is the type of the first object
 * @param <U> is the type of the second object
 */
public class Pair<T, U> {

	/**
	 * <b>Store the first object</b>
	 */
	final T t;
	
	/**
	 * <b>Store the second object</b>
	 */
	final U u;

	/**
	 * <b>Constructor.</b>
	 * Create a new immutable pair object with given params
	 * 
	 * @param t is the first object to be paired
	 * @param u is the second object to be paired
	 */
	public Pair(T t, U u) {
		this.t = t;
		this.u = u;
	}
}
