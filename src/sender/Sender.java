package sender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import protocolPackets.AlternatingBitPacket;

/**
 * Sender class
 */
public class Sender extends Thread {
	
	/**
	 * The states, that Sender can have
	 */
	private enum State {
		waitForCall0,
		waitForAck0,
		waitForCall1,
		waitForAck1
	}
	
	/**
	 * Current state
	 */
	private State currentState;
	
	/**
	 * File to be send
	 */
	private final Path file;
	
	/**
	 * IP-Adress of the receiver
	 */
	private final String ipAdress;
	
	/**
	 * Constructor with following params
	 * 
	 * @param file
	 * @param ipAdress
	 */
	public Sender(Path file, String ipAdress) {
		this.file = file;
		this.ipAdress = ipAdress;
		
		// set start state
		this.currentState = State.waitForCall0;
	}
	
	/**
	 * Reads the given file and sends it to the given receiver
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			// read file in byte array
			final byte[] data = Files.readAllBytes(file);
			
			// split data in packets
			for (int start = 0; start < data.length; start += AlternatingBitPacket.PACKAGESIZE) {
				// packet is a sub-array of the data array with the length defined in AlternatingBitPacket (last package may be smaller)
				final byte[] packet = Arrays.copyOfRange(data, start, start + AlternatingBitPacket.PACKAGESIZE <= data.length ?  start + AlternatingBitPacket.PACKAGESIZE : data.length);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	
	}
	
	
}
