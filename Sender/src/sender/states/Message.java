package sender.states;

/**
 * <b>The Messages, that can be used to communicate with States.</b>
 */
public enum Message {
	
	/**
	 * <b>Send a packet.</b>
	 */
	sendPacket,
	
	/**
	 * <b>A Timeout happened.</b>
	 */
	timeout,
	
	/**
	 * <b>A packet was received.</b>
	 */
	packetReceived

}
