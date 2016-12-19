package sender.states;

/**
 * <b>The states, that Sender can have.</b>
 */
public enum State {
	
	/**
	 * <b>Waiting for a call with SeqNr 0.</b>
	 */
	waitForCall0,
	
	/**
	 * <b>Waiting for an ACK with SeqNr 0.</b>
	 */
	waitForAck0,
	
	/**
	 * <b>Waiting for a call with SeqNr 1.</b>
	 */
	waitForCall1,
	
	/**
	 * <b>Waiting for an ACK with SeqNr 1.</b>
	 */
	waitForAck1;
	
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
