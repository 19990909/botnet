package jgodara.botnet.lifecycle;

public interface LifecycleListener {

	/**
	 * Acknowledge the occurrence of the specified event.
	 *
	 * @param event
	 *            LifecycleEvent that has occurred
	 */
	public void lifecycleEvent(LifecycleEvent event);

}
