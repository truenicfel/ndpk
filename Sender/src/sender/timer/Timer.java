package sender.timer;

import sender.states.Transition;

/**
 * <b>Timer, that executes a Transition after timeout.</b>
 */
public class Timer extends Thread {

	/**
	 * <b>Stores the time to sleep before the Timer timeouts.</b>
	 */
	private final long timeout;

	/**
	 * <b>Stores the Transition to be executed after timeout</b>
	 */
	private final Transition transition;


	
	/**
	 * <b>Create a new object of Timer with following params.</b>
	 * 
	 * @param timeout
	 *            is the time to sleep before the Timer timeouts
	 * @param transition
	 *            is the Transition to be executet after timeout
	 */
	public Timer(long timeout, Transition transition) {
		this.timeout = timeout;
		this.transition = transition;
	}


	
	@Override
	public void run() {
		try {
			System.out.println("Timer started");
			// sleep
			sleep(getTimeout());
			// execute the transition
			getTransition().execute();
		} catch (InterruptedException e) {
			System.out.println("Timer stopped");
		}
	}


	
	/**
	 * <b>Get the time to sleep before the Timer timeouts.</b>
	 * 
	 * @return the time to sleep
	 */
	private long getTimeout() {
		return timeout;
	}

	/**
	 * <b>Get the Transition to be executed after timeout</b>
	 * 
	 * @return the transition
	 */
	private Transition getTransition() {
		return transition;
	}
}
