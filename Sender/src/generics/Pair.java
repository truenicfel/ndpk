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

	/**
	 * <b>Overwriting the hashCode-Method to use this class as a Map key.</b>
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((t == null) ? 0 : t.hashCode());
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		return result;
	}

	/**
	 * <b>Overwriting the equals-Method to use this class as a Map key.</b>
	 * Two Pairs are equal, if their variables t and u are equal
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof Pair<?, ?>) {
			final Pair<?, ?> pair = (Pair<?, ?>) obj;
			result = this.getT().equals(pair.getT()) && this.getU().equals(pair.getU());
		}
		return result;
	}
	
	/**
	 * <b>Get t of type T.</b>
	 * 
	 * @return t
	 */
	public T getT() {
		return t;
	}
	
	/**
	 * <b>Get u of Type U.</b>
	 * 
	 * @return u
	 */
	public U getU() {
		return u;
	}
}
