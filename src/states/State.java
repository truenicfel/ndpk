package states;

public interface State {
	enum Msg {
		sendPacket,
		timeout,
		paketReceived
	}
	
	State execute(Msg msg);
}
