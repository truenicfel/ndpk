package receiver.states;

/**
 * <b>This interface describes a Transition from one state to another.</b>
 * 
 * This is what arrows in a state diagram are representing
 */
public interface Transition {

	/**
	 * <b>Execute this Transition.</b>
	 * This method can be called through a lambda-expression.
	 * 
	 * @return is the new state
	 */
	State execute();
	
}
