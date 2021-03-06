
package protocolPackets;

import java.net.DatagramPacket;


public interface Packet {
    
    DatagramPacket createDatagram();
    
    boolean checkChecksum();
    
    boolean checkSequenceNumber(int sequenceNumber);
    
    boolean isACK();

	int getSequenceNumber();

	boolean isEndFlag();

}
