package receiver;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import decorator.BrokenDatagramSocket;
import generics.Pair;
import protocolPackets.AlternatingBitPacket;
import receiver.states.Message;
import receiver.states.State;
import receiver.states.Transition;

/**
 * <b>Receiver class.</b>
 * 
 * Sends to port 8765 and receives on port 9876
 */
public class Receiver {

	/**
	 * <b>The port to send to.</b>
	 */
	private static final int SEND_PORT = 8765;

	/**
	 * <b>The Port to listen to.</b>
	 */
	private static final int RECEIVE_PORT = 9876;

	/**
	 * <b>Current state.</b>
	 */
	private State currentState;

	/**
	 * <b>Stores the current sequence number.</b>
	 */
	private int currentSeqNr;

	/**
	 * <b>IP-Address of the sender.</b>
	 */
	private String ipAdress;

	/**
	 * <b>Map that maps a State to its code.</b>
	 */
	private final Map<State, State.Action> actions;

	/**
	 * <b>Map that maps a Pair of State and Message to its code.</b>
	 */
	private final Map<Pair<State, Message>, Transition> transitions;

	/**
	 * <b>Stores every byte packet.</b>
	 */
	private final ArrayList<byte[]> data;

	/**
	 * <b>Store if Receiver is receiving or not.</b>
	 */
	private boolean receiving;

	/**
	 * <b>File to write to.</b>
	 */
	private final String file;
	
	
	/**
	 * <b>Constructor with following params.</b>
	 * 
	 * @param file
	 *            is the file to be send
	 * @param ipAdress
	 *            is the IP-Address of the receiver
	 * @throws IOException
	 *             when an error occurs while reading the file in a byte[]
	 */
	public Receiver(String file) {
		this.file = file;
		
		// init data list
		this.data = new ArrayList<>();
		
		// implement all possible actions
		this.actions = new TreeMap<>();
		implementActions();

		// implement all possible transitions
		this.transitions = new TreeMap<>();
		implementTransitions();

		// set start state
		this.currentState = State.waitForData;

		// Receiver is not receiving yet
		this.receiving = false;
	}

	
	
	/**
	 * <b>Start sending the file.</b>
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void receive() throws FileNotFoundException, IOException {
		// program shall run endlessly
		while (true) {
			// set receiving true
			setReceiving(true);
			System.out.println("Start receiving");
			
			// loop as long there was no end-packet
			while (isReceiving()) {
				System.out.println("Now in state " + getCurrentState().toString());
				// get the action of the current state and execute it, next state
				// will be returned and set as new current state
				setCurrentState(getActions().get(getCurrentState()).execute());
			}
			System.out.println("Finished receiving");
			
			// writing file packet by packet
			try (final FileOutputStream writer = new FileOutputStream(getFile())) {
				for (final byte[] packet : getData()) {
					writer.write(packet);
				}
			}
			// clear the data array for next incoming file
			for (int i = 0; i < getData().size(); i++) {
				getData().remove(i);
			}
		}
	}

	/**
	 * <b>Get boolean if Receiver is receiving or not.</b>
	 * 
	 * @return true, if Receiver is currently receiving
	 */
	public boolean isReceiving() {
		return receiving;
	}

	
	
	
	/**
	 * <b>Implement all States and map the code to the State</b>
	 */
	private void implementActions() {
		// implement the waitForData State
		getActions().put(State.waitForData, () -> {
			return waitForData();
		});
	}

	/**
	 * <b>Implement all Transitions and map the code to a Pair of State and
	 * Message.<b>
	 */
	private void implementTransitions() {
		// implement transition waitForData -> sendAck -> waitForData
		getTransitions().put(new Pair<State, Message>(State.waitForData, Message.sendAck), () -> {
			sendAck();
			return State.waitForData;
		});
	}

	/**
	 * <b>Builds a ACK and sends it with a BrokenDatagramSocket.</b>
	 */
	private void sendAck() {
		// send the packet with the BrokenDatagramSocket to create errors
		try (final BrokenDatagramSocket socket = new BrokenDatagramSocket(new DatagramSocket())) {
			// set sequenceNr depending on which state we are going to
			// create a packet with SeqNr, ACK = false and content
			final DatagramPacket packet = new AlternatingBitPacket(getCurrentSeqNr(), true, new byte[0], getIpAdress(), SEND_PORT)
					.createDatagram();
			// send the packet
			socket.send(packet);
		} catch (SocketException exception) {
			System.err.println("Sorry, something went wrong with the Socket.");
			exception.printStackTrace();
		} catch (IOException exception) {
			System.err.println("Sorry, something went wrong while sending a packet.");
			exception.printStackTrace();
		}
	}

	/**
	 * <b>Waits for a incoming packet and validates it.</b> Returns a new State
	 * after calling the transition, if the packet was valid
	 * 
	 * @return the next State (might be the same as before)
	 */
	private State waitForData() {
		State nextState = getCurrentState();

		try (final DatagramSocket socket = new DatagramSocket(RECEIVE_PORT)) {
			// Received data will be stored in this array
			final byte[] receivedData = new byte[AlternatingBitPacket.PACKETSIZE];
			// receive packet
			final DatagramPacket datagramPacket = new DatagramPacket(receivedData, receivedData.length);
			socket.receive(datagramPacket);
			// Wrap in AlternatingBitPacket
			final AlternatingBitPacket packet = new AlternatingBitPacket(datagramPacket);

			// check if packet is correct
			final boolean checksumValid = packet.checkChecksum();

			// are both valid?
			if (checksumValid) {
				// store sender IP-Address
				setIpAdress(datagramPacket.getAddress().getHostAddress());
				// store sequence number
				setCurrentSeqNr(packet.getSequenceNumber());
				// store received data
				getData().add(datagramPacket.getData());
				// execute transition and set next state
				nextState = getTransitions().get(new Pair<State, Message>(getCurrentState(), Message.sendAck))
						.execute();
				// TODO: stop receiving if end-of-file flag was set
				setReceiving(true);
			}
		} catch (IOException exception) {
			System.err.println("Ups, somethig went wrong while receiving the data. Waiting for next packet...");
		}
		// return current state if there was an error or next state if it was
		// successful
		return nextState;
	}

	/**
	 * <b>Store if Receiver is receiving or not.</b>
	 * 
	 * @param receiving
	 *            is the value to set
	 */
	private void setReceiving(boolean receiving) {
		this.receiving = receiving;
	}

	/**
	 * <b>Get the map that maps a State to its code.</b>
	 * 
	 * @return the mapping
	 */
	private Map<State, State.Action> getActions() {
		return actions;
	}

	/**
	 * <b>Get the map that maps a Pair of State and Message to its code.</b>
	 * 
	 * @return the mapping
	 */
	private Map<Pair<State, Message>, Transition> getTransitions() {
		return transitions;
	}

	/**
	 * <b>Get the current state.</b>
	 * 
	 * @return the current state
	 */
	private State getCurrentState() {
		return currentState;
	}

	/**
	 * <b>Set the current state.</b>
	 * 
	 * @param currentState
	 *            is the new current state
	 */
	private void setCurrentState(State currentState) {
		this.currentState = currentState;
	}

	/**
	 * <b>Get all byte packets.</b>
	 * 
	 * @return a list of all byte packets
	 */
	private ArrayList<byte[]> getData() {
		return data;
	}

	/**
	 * <b>Get IP-Address of the sender.</b>
	 * 
	 * @return the IP-Address
	 */
	private String getIpAdress() {
		return ipAdress;
	}
	
	/**
	 * <b>Set IP-Address of the sender.</b>
	 * 
	 * @param ipAdress is the new IP-Address
	 */
	private void setIpAdress(String ipAdress) {
		this.ipAdress = ipAdress;
	}
	
	/**
	 * <b>Get the current sequence number.</b>
	 * 
	 * @return the current sequence number
	 */
	private int getCurrentSeqNr() {
		return currentSeqNr;
	}
	
	/**
	 * <b>Set the current sequence number.</b>
	 * 
	 * @param currentSeqNr is the new sequence number
	 */
	private void setCurrentSeqNr(int currentSeqNr) {
		this.currentSeqNr = currentSeqNr;
	}

	/**
	 * <b>Get the file to write to.</b>
	 * 
	 * @return the file path as String
	 */
	private String getFile() {
		return file;
	}
}
