package states;

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
	
}
