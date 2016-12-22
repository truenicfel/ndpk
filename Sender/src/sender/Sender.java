package sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import decorator.BrokenDatagramSocket;
import generics.Pair;
import protocolPackets.AlternatingBitPacket;
import sender.states.Message;
import sender.states.State;
import sender.states.Transition;
import sender.timer.Timer;

/**
 * <b>Sender class.</b>
 * 
 * Sends to port 9876 and receives on port 8765
 */
public class Sender {

	/**
	 * <b>The Port to send to.</b>
	 */
	private static final int SEND_PORT = 9876;

	/**
	 * <b>The port to listen to.</b>
	 */
	private static final int RECEIVE_PORT = 8765;

	/**
	 * <b>Default timeout in ms.</b>
	 */
	private static final long DEFAULT_TIMEOUT = 300;

	/**
	 * <b>Current state.</b>
	 */
	private State currentState;

	/**
	 * <b>Current timer (may be running or not).</b>
	 */
	private Timer timer;

	/**
	 * <b>IP-Address of the receiver.</b>
	 */
	private final String ipAdress;

	/**
	 * <b>Map that maps a State to its code.</b>
	 */
	private final Map<State, State.Action> actions;

	/**
	 * <b>Map that maps a Pair of State and Message to its code.</b>
	 */
	private final Map<Pair<State, Message>, Transition> transitions;

	/**
	 * <b>File to be send as byte[].</b>
	 */
	private final byte[] data;

	/**
	 * <b>Stores the number of bytes send.</b>
	 */
	private int bytesSend;

	/**
	 * <b>Stores the number of bytes send in the last sending process.</b>
	 */
	private int bytesSendInLastPacket;

	/**
	 * <b>Store if Sender is sending or not.</b>
	 */
	private boolean sending;

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
	public Sender(Path file, String ipAdress) throws IOException {
		// store the destination IP-Address
		this.ipAdress = ipAdress;

		// read file in byte array
		this.data = Files.readAllBytes(file);

		// implement all possible actions
		this.actions = new HashMap<>();
		implementActions();

		// implement all possible transitions
		this.transitions = new HashMap<>();
		implementTransitions();

		// set start state
		this.currentState = State.waitForCall0;

		// Sender is not sending yet
		this.bytesSend = 0;
		this.sending = false;
	}

	/**
	 * <b>Start sending the file.</b>
	 */
	public void send() {
		// set sending true
		setSending(true);
		System.out.println("Start sending");

		// loop while Sender is sending
		while (isSending()) {
			System.out.println();
			System.out.println(getCurrentState().toString() + " (" + getBytesSend() + " / " + getData().length + " Bytes send)");
			// get the action of the current state and execute it, next state
			// will be returned and set as new current state
			setCurrentState(getActions().get(getCurrentState()).execute());
		}
		System.out.println("Finished sending");
	}

	/**
	 * <b>Get boolean if Sender is sending or not.</b>
	 * 
	 * @return true, if Sender is currently sending
	 */
	public boolean isSending() {
		return sending;
	}

	/**
	 * <b>Get the number of bytes send.</b>
	 *
	 * @return the number of bytes, that have already been send
	 */
	public int getBytesSend() {
		return bytesSend;
	}

	/**
	 * <b>Implement all States and map the code to the State</b>
	 */
	private void implementActions() {
		// implement the waitForCall0 State
		getActions().put(State.waitForCall0, () -> {
			// when in this state, call transition to send the packet
			return getTransitions().get(new Pair<State, Message>(State.waitForCall0, Message.sendPacket)).execute();
		});

		// implement the waitForAck0 State
		getActions().put(State.waitForAck0, () -> {
			// wait for ACK 0 and return the new State
			return waitForAck(0);
		});

		// implement the waitForCall1 State
		getActions().put(State.waitForCall1, () -> {
			// when in this state, call transition to send the packet
			return getTransitions().get(new Pair<State, Message>(State.waitForCall1, Message.sendPacket)).execute();
		});

		// implement the waitForAck1 State
		getActions().put(State.waitForAck1, () -> {
			// wait for ACK 1 and return the new State
			return waitForAck(1);
		});
	}

	/**
	 * <b>Implement all Transitions and map the code to a Pair of State and
	 * Message.<b>
	 */
	private void implementTransitions() {
		// implement transition waitForCall0 -> packetReceived -> waitForCall0
		getTransitions().put(new Pair<State, Message>(State.waitForCall0, Message.packetReceived), () -> {
			// nothing to do here, so this transition won't be called.
			// it's just here, so the corresponding diagram is completely
			// implemented.
			return State.waitForCall0;
		});

		// implement transition waitForCall0 -> sendPacket -> waitForAck0
		getTransitions().put(new Pair<State, Message>(State.waitForCall0, Message.sendPacket), () -> {
			// send packet
			sendPacket(State.waitForAck0);
			// return new state
			return State.waitForAck0;
		});

		// implement transition waitForAck0 -> timeout -> waitForAck0
		getTransitions().put(new Pair<State, Message>(State.waitForAck0, Message.timeout), () -> {
			System.out.println("\tTimeout! Resending now...");
			// send packet again
			setBytesSend(getBytesSend() - getBytesSendInLastPacket());
			sendPacket(State.waitForAck0);
			// state won't change
			return State.waitForAck0;
		});

		// implement transition waitForAck0 -> packetReceived -> waitForCall1
		getTransitions().put(new Pair<State, Message>(State.waitForAck0, Message.packetReceived), () -> {
			// stop timer
			getTimer().interrupt();
			System.out.println("\tTimer stopped!");
			// return new state
			return State.waitForCall1;
		});

		// implement transition waitForCall1 -> packetReceived -> waitForCall1
		getTransitions().put(new Pair<State, Message>(State.waitForCall1, Message.packetReceived), () -> {
			// nothing to do here, so this transition won't be called.
			// it's just here, so the corresponding diagram is completely
			// implemented.
			return State.waitForCall1;
		});

		// implement transition waitForCall1 -> sendPacket -> waitForAck1
		getTransitions().put(new Pair<State, Message>(State.waitForCall1, Message.sendPacket), () -> {
			// send packet
			sendPacket(State.waitForAck1);
			// return new state
			return State.waitForAck1;
		});

		// implement transition waitForAck1 -> timeout -> waitForAck1
		getTransitions().put(new Pair<State, Message>(State.waitForAck1, Message.timeout), () -> {
			System.out.println("\tTimeout! Resending now...");
			// send packet again
			setBytesSend(getBytesSend() - getBytesSendInLastPacket());
			sendPacket(State.waitForAck1);
			// state won't change
			return State.waitForAck1;
		});

		// implement transition waitForAck1 -> packetReceived -> waitForCall0
		getTransitions().put(new Pair<State, Message>(State.waitForAck1, Message.packetReceived), () -> {
			// stop timer
			getTimer().interrupt();
			System.out.println("\tTimer stopped!");
			// return new state
			return State.waitForCall0;
		});
	}

	/**
	 * <b>Splits the data in a packet, sends it with a BrokenDatagramSocket and
	 * starts the Timer.</b>
	 * 
	 * @param state
	 *            is the state which the Timer shall send the timeout from
	 */
	private void sendPacket(State state) {
		// content is a sub-array of the data array with the length defined in
		// AlternatingBitPacket (last package may be smaller)
		final byte[] content = Arrays.copyOfRange(getData(), getBytesSend(), getBytesSend() + AlternatingBitPacket.PACKETSIZE);

		// send the packet with the BrokenDatagramSocket to create errors
		try (final BrokenDatagramSocket socket = new BrokenDatagramSocket()) {
			// set sequenceNr depending on which state we are going to
			final int seqNr = state == State.waitForAck0 ? 0 : 1;
			// calculate it this packet is the last packet of the data
			final boolean endFlag = (getBytesSend() + content.length) >= getData().length;
			// create a packet with SeqNr, ACK = false and content
			final AlternatingBitPacket abPacket = new AlternatingBitPacket(seqNr, false, endFlag, content, getIpAdress(), SEND_PORT);
			final DatagramPacket packet = abPacket.createDatagram();

			// store new timer and start it
			setTimer(new Timer(DEFAULT_TIMEOUT, getTransitions().get(new Pair<State, Message>(state, Message.timeout))));
			getTimer().start();
			System.out.println("\tTimer started!");
			
			// send the packet
			socket.send(packet);
			System.out.println("\tPacket " + abPacket.getSequenceNumber() + " send to " + getIpAdress() + ":" + SEND_PORT + "!");
		} catch (UnknownHostException exception) {
			System.err.println("Sorry, the given IP-Address can not be found.");
			exception.printStackTrace();
		} catch (SocketException exception) {
			System.err.println("Sorry, something went wrong with the Socket.");
			exception.printStackTrace();
		} catch (IOException exception) {
			System.err.println("Sorry, something went wrong while sending a packet.");
			exception.printStackTrace();
		}
		
		// update bytes send variables
		setBytesSendInLastPacket(content.length);
		setBytesSend(getBytesSend() + getBytesSendInLastPacket());
	}

	/**
	 * <b>Waits for a incoming packet and validates it.</b> Returns a new State
	 * after calling the transition, if the packet was valid
	 * 
	 * @param ackNr
	 *            is the expected ACK-Nr
	 * @return the next State (might be the same as before)
	 */
	private State waitForAck(int ackNr) {
		State nextState = getCurrentState();

		try (final DatagramSocket socket = new DatagramSocket(RECEIVE_PORT)) {
			// Received data will be stored in this array (header and content)
			final byte[] receivedData = new byte[AlternatingBitPacket.PACKETSIZE + AlternatingBitPacket.HEADERSIZE];
			// receive packet
			final DatagramPacket datagramPacket = new DatagramPacket(receivedData, receivedData.length);
			socket.receive(datagramPacket);
			// Wrap in AlternatingBitPacket
			final AlternatingBitPacket packet = new AlternatingBitPacket(datagramPacket);

			System.out.println("\tACK " + packet.getSequenceNumber() + " received!");
			
			// check if packet is correct
			final boolean isAck = packet.isACK();
			final boolean ackValid = packet.checkSequenceNumber(ackNr);
			final boolean checksumValid = packet.checkChecksum();

			// valid?
			if (isAck && ackValid && checksumValid) {
				System.out.println("\tACK " + packet.getSequenceNumber() + " accepted!");
				// execute transition and set next state
				nextState = getTransitions().get(new Pair<State, Message>(getCurrentState(), Message.packetReceived))
						.execute();
				// stop sending if all bytes are send
				setSending(getData().length > getBytesSend());
			}
		} catch (IOException exception) {
			System.err.println("Ups, somethig went wrong while receiving the receivers answer. Trying again...");
		}
		// return current state if there was an error or next state if it was
		// successful
		return nextState;
	}

	/**
	 * <b>Store if Sender is sending or not.</b>
	 * 
	 * @param sending
	 *            is the value to set
	 */
	private void setSending(boolean sending) {
		this.sending = sending;
	}

	/**
	 * <b>Set the number of bytes send.</b>
	 * 
	 * @param bytesSend
	 *            is the number of bytes that have been send
	 */
	private void setBytesSend(int bytesSend) {
		this.bytesSend = bytesSend;
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
	 * <b>Get the file to be send as byte[].</b>
	 * 
	 * @return the file as byte[]
	 */
	private byte[] getData() {
		return data;
	}

	/**
	 * <b>Get IP-Adress of the receiver.</b>
	 * 
	 * @return the IP-Address
	 */
	private String getIpAdress() {
		return ipAdress;
	}

	/**
	 * <b>Get the current timer (may be running or not).</b>
	 * 
	 * @return the timer
	 */
	private Timer getTimer() {
		return timer;
	}

	/**
	 * <b>Set the current timer (may be running or not).</b>
	 * 
	 * @param timer
	 *            is the timer
	 */
	private void setTimer(Timer timer) {
		this.timer = timer;
	}

	/**
	 * <b>Set the number of bytes send in the last sending process.</b>
	 * 
	 * @param bytesSendInLastPacket
	 *            is the number of bytes
	 */
	private void setBytesSendInLastPacket(int bytesSendInLastPacket) {
		this.bytesSendInLastPacket = bytesSendInLastPacket;
	}

	/**
	 * <b>Get the number of bytes send in the last sending process.</b>
	 * 
	 * @return the number of bytes
	 */
	private int getBytesSendInLastPacket() {
		return bytesSendInLastPacket;
	}
}
