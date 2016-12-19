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
    private final static int CHECKOFF = 5;
    private final static int CONTENTOFF = 13;

    //Static Public:
    public final static int PACKETSIZE = 1400;

    //Private:
    /**
     * Saves if this an ACKnowledgement.
     */
    final private boolean ACK;

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
    public AlternatingBitPacket(int sequenceNumber, boolean ACK, byte[] content, String ipAddress, int port) throws UnknownHostException {
        if (content.length > PACKETSIZE) {
            throw new IllegalArgumentException("The content length is to big!");
        } else {
            this.sequenceNumber = sequenceNumber;
            this.ACK = ACK;
            //save the long value of the checksum
//            this.checksum = calculateChecksum(content);
            //create the Payload
            final byte[] payload = createPayload(content);
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
        this.sequenceNumber = getSequenceNumberPackage();
        //do not use the checksum which is in the header field
        //calculate it extra to compare them afterwards
        this.checksum = calculateChecksum(Arrays.copyOfRange(getUdpPacket().getData(), CONTENTOFF, getUdpPacket().getData().length));
    }

    //public Methods
    //--------------------------------------------------------------------------
    
    /**
     * Returns a DatagramPacket representation of this object which can be sent
     * with a DatagramSocket.
     * @return The Datagram packet.
     */
    @Override
    public DatagramPacket createDatagram() {
        return getUdpPacket();
    }

    /**
     * Compares the Checksum which was sent with the packet and the checksum we
     * calculated.
     * @return True if they match false if they dont.
     */
    @Override
    public boolean checkChecksum() {
        return getChecksum() == getCehcksumPackage();
    }

    /**
     * Compares the given sequence Number ("should be sequence Number") to 
     * the actual sequence Number in this class.
     * @param sequenceNumber The Sequence Number you expect.
     * @return True if they match false if they dont.
     */
    @Override
    public boolean checkSequenceNumber(int sequenceNumber) {
        return getSequenceNumber() == sequenceNumber;
    }

    /**
     * Simply returns if this an ACK or not.
     * @return True if it is an ACK false if not.
     */
    @Override
    public boolean isACK() {
        return ACK;
    }

    //private Methods
    //--------------------------------------------------------------------------
    
    /**
     * Checks the Datagram Packet in this object if its an ACK.
     * @return True if the Datagram packet is an ACK
     */
    private boolean isACKPackage() {
        return getUdpPacket().getData()[ACKOFF] == 1;
    }
    /**
     * Returns the Sequence Number of the Datagram Packet.
     * @return The Sequence number as int.
     */
    private int getSequenceNumberPackage() {
        return getUdpPacket().getData()[SEQUENCENUMBEROFF];
    }
    
    /**
     * Gets the checksum from the UDP packet.
     * @return The checksum as long value.
     */
    private long getCehcksumPackage() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(Arrays.copyOfRange(getUdpPacket().getData(), CHECKOFF, CONTENTOFF - 1));
        buffer.flip();
        return buffer.getLong();
    }
    
    /**
     * Calculates the CRC-32 checksum of a given byte array.
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
     * Creates the payload with our additional header (Sequence Number +
     * Checksum)
     *
     *
     * @param content The content to send with the payload.
     * @return
     */
    private byte[] createPayload(byte[] content) {
        //create the payload byte array our header + content
        final byte[] payload = new byte[CONTENTOFF + content.length];
        //fill the first 13 bytes with our header
        //first 13 bytes (sequence number + ACKFlag + checksum)
        final ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + 1 + Long.BYTES);
        //fill the sequence number into the buffer
        buffer.putInt(getSequenceNumber());

        //set the ack flag
        byte ackFlag;
        if (isACK()) {
            ackFlag = 1;
        } else {
            ackFlag = 0;
        }
        buffer.put(ackFlag);
        byte[] header = buffer.array();
        
        final byte[] checksumArray = new byte[CHECKOFF + content.length];
        
        System.arraycopy(header, 0, checksumArray, 0, CHECKOFF);
        
        System.arraycopy(content, 0, checksumArray, CHECKOFF, content.length);
        this.checksum = calculateChecksum(checksumArray);
        
        //fill the checksum into the buffer
        buffer.putLong(getChecksum());
        header = buffer.array();
        
        //now merge the two arrays
        //the header first
        System.arraycopy(header, 0, payload, 0, CONTENTOFF);
        //the content second
        for (int i = 0; i < content.length; i++) {
            payload[i + CONTENTOFF - 1] = content[i];
        }
        return payload;
    }

    /**
     * Get the Sequence Number of this Packet.
     * @return The sequence number (0,1) as int.
     */
    private int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Get the Checksum of this Packet.
     * @return  The checksum as long value.
     */
    private long getChecksum() {
        return checksum;
    }

    /**
     * Gets the Datagram Packet representation of this Packet.
     * @return The DatagramPacket. 
     */
    private DatagramPacket getUdpPacket() {
        return udpPacket;
    }

}
