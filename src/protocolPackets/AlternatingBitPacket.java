package protocolPackets;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class AlternatingBitPacket implements Packet {

    public final static int CONTENTOFF = 10;
    
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
    final private long checksum;

    /**
     * Saves the udpPacket.
     */
    final private DatagramPacket udpPacket;
    

    public AlternatingBitPacket(int sequenceNumber, boolean ACK, byte[] content) {
        if (content.length > 1000000000) {
            throw new IllegalArgumentException("The content length is to big!");
        } else {
            this.sequenceNumber = sequenceNumber;
            this.ACK = ACK;
            //create checksum object
            final Checksum checksumCRC32 = new CRC32();
            //update the checksum with the content
            checksumCRC32.update(content, 0, content.length);
            //save the long value of the checksum
            this.checksum = checksumCRC32.getValue();
            //create the payload byte array our header + content
            final byte[] payload = new byte[10 + content.length];
            //fill the first ten bytes with our header
            //first 8 bytes (64 bit)
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(checksum);
            System.arraycopy(buffer.array(), 0, payload, 0, CONTENTOFF-1);
            System.arraycopy(content, 0, payload, CONTENTOFF, payload.length);
            this.udpPacket = new DatagramPacket(payload, payload.length);
            
        }

    }

    @Override
    public DatagramPacket createDatagram() {
        return new DatagramPacket(new byte[10], 10);
    }

    @Override
    public boolean checkChecksum() {
        return false;
    }

    @Override
    public boolean checkSequenceNumber() {
        return false;
    }

    @Override
    public boolean isACK() {
        return ACK;
    }

    private int getSequenceNumber() {
        return sequenceNumber;
    }

    private long getChecksum() {
        return checksum;
    }

    private DatagramPacket getUdpPacket() {
        return udpPacket;
    }

}
