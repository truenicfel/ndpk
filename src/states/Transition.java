package states;

/**
 * <b>This interface describes a Transition from one state to another.</b>
 */
public interface Transition {

	
	/**
	 * <b>Execute this Transition.</b>
	 * This method can be called through a lamnda-expression.
	 * 
	 * @param currentState is the current state
	 * @param message is the message, defining what to do
	 * @return is the new state
	 */
	State execute(State currentState, Message message);
	
}
