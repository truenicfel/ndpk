package receiver.states;

/**
 * <b>The states, that Receiver can have.</b>
 */
public enum State {
	
	/**
	 * <b>Waiting for data.</b>
	 */
	waitForData;
	
	/**
	 * <b>This interface describes an Action of a State.</b>
	 * 
	 * This is what is happening in a state (for example waiting for an event), other than a Transition
	 */
	public interface Action {

		/**
		 * <b>Execute this StateAction.</b>
		 * This method can be called through a lambda-expression.
		 * 
		 * @return is the new state
		 */
		State execute();
		
	}
}
