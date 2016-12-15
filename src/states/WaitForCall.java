package states;

public class WaitForCall implements State {

	private final int seqNr;
	
	public WaitForCall(int seqNr) {
		this.seqNr = seqNr;
	}
	
	@Override
	public int getSeqNr() {
		return seqNr;
	}

}
