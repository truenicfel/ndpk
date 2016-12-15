package states;

public class WaitForAck implements State {

	private final int seqNr;
	
	public WaitForAck(int seqNr) {
		this.seqNr = seqNr;
	}
	
	@Override
	public int getSeqNr() {
		return seqNr;
	}

}
