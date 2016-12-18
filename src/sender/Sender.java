package sender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import protocolPackets.AlternatingBitPacket;

/**
 * <b>Sender class.</b>
 */
public class Sender extends Thread {
	
	/**
	 * <b>Current state.</b>
	 */
	private states.State currentState;
	
	/**
	 * <b>File to be send.</b>
	 */
	private final Path file;
	
	/**
	 * <b>IP-Address of the receiver.</b>
	 */
	private final String ipAdress;
	
	/**
	 * <b>Constructor with following params.</b>
	 * 
	 * @param file is the file to be send
	 * @param ipAdress is the IP-Address of the receiver
	 */
	public Sender(Path file, String ipAdress) {
		this.file = file;
		this.ipAdress = ipAdress;
		
		// set start state
		this.currentState = states.State.waitForAck0;
	}
	
	/**
	 * <b>Reads the given file and sends it to the given receiver.</b>
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			// read file in byte array
			final byte[] data = Files.readAllBytes(getFile());
			// split data in packets
			for (int start = 0; start < data.length; start += AlternatingBitPacket.PACKAGESIZE) {
				// packet is a sub-array of the data array with the length defined in AlternatingBitPacket (last package may be smaller)
				final byte[] packet = Arrays.copyOfRange(data, start, start + AlternatingBitPacket.PACKAGESIZE <= data.length ?  start + AlternatingBitPacket.PACKAGESIZE : data.length);
				
				// TODO
			}
			
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	
	
	}
	
	/**
	 * <b>Start sending the file.</b>
	 * 
	 * This method just calls the Thread.start() method, but still is preferred using.
	 */
	public void send() {
		start();
	}
	
	
	
	/**
	 * <b>Get the current state.</b>
	 * 
	 * @return the current state
	 */
	private states.State getCurrentState() {
		return currentState;
	}
	
	/**
	 * <b>Set the current state.</b>
	 * 
	 * @param currentState is the new current state
	 */
	private void setCurrentState(states.State currentState) {
		this.currentState = currentState;
	}
	
	/**
	 * <b>Get the file to be send.</b>
	 * 
	 * @return the file
	 */
	private Path getFile() {
		return file;
	}

	/**
	 * <b>Get IP-Adress of the receiver.</b>
	 * 
	 * @return the IP-Address
	 */
	private String getIpAdress() {
		return ipAdress;
	}
}
