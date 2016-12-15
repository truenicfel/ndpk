package states;

public class WaitForAck implements State {

	private final int seqNr;
	
	public WaitForAck(int seqNr) {
		this.seqNr = seqNr;
	}
	
	@Override
	public State execute(Msg msg) {
		// TODO Auto-generated method stub
		return null;
	}

}
