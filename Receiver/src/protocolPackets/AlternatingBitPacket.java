package protocolPackets;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class AlternatingBitPacket implements Packet {

    //Object Variables
    //--------------------------------------------------------------------------
    //Static Private:
    private final static int SEQUENCENUMBEROFF = 3;
    private final static int ACKOFF = 4;
    private final static int ENDOFF = 5;
    private final static int CHECKOFF = 6;
    private final static int CONTENTOFF = 14;

    //Static Public:
    public final static int PACKETSIZE = 1400;
    public final static int HEADERSIZE = CONTENTOFF;

    //Private:
    /**
     * Saves if this an ACKnowledgement.
     */
    final private boolean ACK;

    /**
     * Saves if this is the last packet of the file.
     */
    final private boolean endFlag;

    /**
     * Saves the sequenceNumber of this package.
     */
    final private int sequenceNumber;

    /**
     * Saves the checksum of this package's content.
     */
    private long checksum;

    /**
     * Saves the udpPacket.
     */
    final private DatagramPacket udpPacket;

    //Constructors
    //--------------------------------------------------------------------------
    /**
     * Initialize Alternating Bit Packet. This C-tor will create a packet from
     * given constants.
     *
     * @param sequenceNumber The sequence number either 1 or 0.
     * @param ACK Is this a acknowledgement (True = yes).
     * @param content The content this package will have.
     * @throws UnknownHostException
     */
    public AlternatingBitPacket(int sequenceNumber, boolean ACK, boolean endFlag, byte[] content, String ipAddress, int port) throws UnknownHostException {
        if (content.length > PACKETSIZE) {
            throw new IllegalArgumentException("The content length is to big!");
        } else {
            this.sequenceNumber = sequenceNumber;
            this.ACK = ACK;
            this.endFlag = endFlag;
            //create the first part of the header (sequence number + ack flag)
            final byte[] firstHeaderPart = createHeader();
            //create the an array for calculating the checksum (sequence number + ackflag + content)
            final byte[] checksumArray = new byte[CHECKOFF + content.length];
            System.arraycopy(firstHeaderPart, 0, checksumArray, 0, CHECKOFF);
            System.arraycopy(content, 0, checksumArray, CHECKOFF, content.length);
            //calculate the checksum
            this.checksum = calculateChecksum(checksumArray);
            //create the second part of the header ( 8 bytes filled with the checksumm)
            final byte[] secondHeaderPart = createChecksum();
            //create the Payload (first + second header part + content)
            final byte[] payload = createPayload(firstHeaderPart, secondHeaderPart, content);
            this.udpPacket = new DatagramPacket(payload, payload.length, InetAddress.getByName(ipAddress), port);
        }

    }

    /**
     * Initialize Alternating Bit Packet. This C-tor will create a packet from a
     * existing udpPacket.
     *
     * @param udpPacket
     */
    public AlternatingBitPacket(DatagramPacket udpPacket) {
        this.udpPacket = udpPacket;
        this.ACK = isACKPackage();
        this.endFlag = isEndFlagPackage();
        this.sequenceNumber = getSequenceNumberPackage();

        // get content
        final byte[] content = Arrays.copyOfRange(getUdpPacket().getData(), CONTENTOFF, getUdpPacket().getData().length);
        final byte[] checksumArray = new byte[CHECKOFF + content.length];
        final byte[] firstHeaderPart = Arrays.copyOfRange(getUdpPacket().getData(), 0, CHECKOFF);
        System.arraycopy(firstHeaderPart, 0, checksumArray, 0, CHECKOFF);
        System.arraycopy(content, 0, checksumArray, CHECKOFF, content.length);
        
        //do not use the checksum which is in the header field
        //calculate it extra to compare them afterwards
        this.checksum = calculateChecksum(checksumArray);
    }


	//public Methods
    //--------------------------------------------------------------------------
    /**
     * Returns a DatagramPacket representation of this object which can be sent
     * with a DatagramSocket.
     *
     * @return The Datagram packet.
     */
    @Override
    public DatagramPacket createDatagram() {
        return getUdpPacket();
    }

    /**
     * Compares the Checksum which was sent with the packet and the checksum we
     * calculated.
     *
     * @return True if they match, false if they dont.
     */
    @Override
    public boolean checkChecksum() {
        return getChecksum() == getChecksumPackage();
    }

    /**
     * Compares the given sequence Number ("should be sequence Number") to the
     * actual sequence Number in this class.
     *
     * @param sequenceNumber The Sequence Number you expect.
     * @return True if they match false if they dont.
     */
    @Override
    public boolean checkSequenceNumber(int sequenceNumber) {
        return getSequenceNumber() == sequenceNumber;
    }

    /**
     * Simply returns if this an ACK or not.
     *
     * @return True if it is an ACK false if not.
     */
    @Override
    public boolean isACK() {
        return ACK;
    }

    /**
     * Returns it this is the last packet or not
     * 
     * @see protocolPackets.Packet#isEndFlag()
     * @return true, if this is the last packet of the file
     */
    @Override
    public boolean isEndFlag() {
		return endFlag;
	}
    
    /**
     * Get the Sequence Number of this Packet.
     *
     * @return The sequence number (0,1) as int.
     */
    @Override
    public int getSequenceNumber() {
    	return sequenceNumber;
    }
    
    /**
     * <b>toString implemented.</b>
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	
    	return "Object: "
    			+ "SeqNr: " + getSequenceNumber() 
    			+ ", ACK: " + isACK() 
    			+ ", END :" + isEndFlag()
    			+ ", Checksum: " + getChecksum()
    			+ "\r\n"
    			+ "Package: "
    	    	+ "SeqNr: " + getSequenceNumberPackage() 
    	    	+ ", ACK: " + isACKPackage()
    	    	+ ", END :" + isEndFlagPackage()
    	    	+ ", Checksum: " + getChecksumPackage();
    }

    //private Methods
    //--------------------------------------------------------------------------
    /**
     * Checks the Datagram Packet in this object if its an ACK.
     *
     * @return True if the Datagram packet is an ACK
     */
    private boolean isACKPackage() {
        return getUdpPacket().getData()[ACKOFF] == 1;
    }

    /**
     * CHecks the Datagram Packet in this object if its the last packet of the file.
     * 
     * @return true, if it is the last packet
     */
    private boolean isEndFlagPackage() {
    	return getUdpPacket().getData()[ENDOFF] == 1;
    }

    /**
     * Returns the Sequence Number of the Datagram Packet.
     *
     * @return The Sequence number as int.
     */
    private int getSequenceNumberPackage() {
        return getUdpPacket().getData()[SEQUENCENUMBEROFF];
    }

    /**
     * Gets the checksum from the UDP packet.
     *
     * @return The checksum as long value.
     */
    private long getChecksumPackage() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(Arrays.copyOfRange(getUdpPacket().getData(), CHECKOFF, CONTENTOFF));
        buffer.flip();
        return buffer.getLong();
    }

    /**
     * Calculates the CRC-32 checksum of a given byte array.
     *
     * @param content The byte array to calculate the checksum from.
     * @return The checksum as a long value.
     */
    private long calculateChecksum(byte[] content) {
        //create checksum object
        final Checksum checksumCRC32 = new CRC32();
        //update the checksum with the content
        checksumCRC32.update(content, 0, content.length);
        //return long value
        return checksumCRC32.getValue();
    }

    /**
     * Merges the three byte Arrays to return the final payload.
     *
     *
     * @param content The content to send with the payload.
     * @return
     */
    private byte[] createPayload(byte[] firstHeaderPart, byte[] secondHeaderPart, byte[] content) {
        final byte[] payload = new byte[CONTENTOFF + content.length];
        System.arraycopy(firstHeaderPart, 0, payload, 0, CHECKOFF);
        System.arraycopy(secondHeaderPart, 0, payload, CHECKOFF, secondHeaderPart.length);
        System.arraycopy(content, 0, payload, CONTENTOFF, content.length);
        return payload;
    }

    /**
     * Creates the checksum part of the header from the long value checksum.
     *
     * @return byte[] filled with the checksum.
     */
    private byte[] createChecksum() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(getChecksum());
        return buffer.array();
    }

    /**
     * Creates the header without the checksum.
     *
     * @return
     */
    private byte[] createHeader() {

        //first 13 bytes (sequence number + ACKFlag + ENDFlag)
        final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + 1 + 1);
        //fill the sequence number into the buffer
        buffer.putInt(getSequenceNumber());
        //set flags
        final byte ackFlag = (byte) (isACK() ? 1 : 0);
        final byte endFlag = (byte) (isEndFlag() ? 1 : 0);
        //put flags to buffer
        buffer.put(ackFlag);
        buffer.put(endFlag);
        //return the new array
        return buffer.array();
    }


    /**
     * Get the Checksum of this Packet.
     *
     * @return The checksum as long value.
     */
    private long getChecksum() {
        return checksum;
    }

    /**
     * Gets the Datagram Packet representation of this Packet.
     *
     * @return The DatagramPacket.
     */
    private DatagramPacket getUdpPacket() {
        return udpPacket;
    }

}
