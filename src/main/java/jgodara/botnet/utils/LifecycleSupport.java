package jgodara.botnet.utils;

import jgodara.botnet.lifecycle.Lifecycle;
import jgodara.botnet.lifecycle.LifecycleEvent;
import jgodara.botnet.lifecycle.LifecycleListener;

public final class LifecycleSupport {

	public LifecycleSupport(Lifecycle lifecycle) {
		super();
		this.lifecycle = lifecycle;
	}

	private Lifecycle lifecycle;

	private LifecycleListener[] listeners = new LifecycleListener[0];

	private final Object lockingObject = new Object();

	public void addLifecycleListener(LifecycleListener listener) {

		synchronized (lockingObject) {

			LifecycleListener[] results = new LifecycleListener[listeners.length + 1];

			for (int i = 0; i < listeners.length; i++)
				results[i] = listeners[i];

			results[listeners.length] = listener;
			listeners = results;
		}

	}

	public LifecycleListener[] findLifecycleListeners() {
		return listeners;
	}

	public void fireLifecycleEvent(String type, Object data) {

		LifecycleEvent event = new LifecycleEvent(lifecycle, type, data);
		LifecycleListener[] events = listeners;
		for (LifecycleListener listener : events)
			listener.lifecycleEvent(event);

	}

	public void removeLifecycleListener(LifecycleListener listener) {

		synchronized (lockingObject) {
			int n = -1;
			for (int i = 0; i < listeners.length; i++) {
				if (listeners[i] == listener) {
					n = i;
					break;
				}
			}
			if (n < 0)
				return;
			LifecycleListener results[] = new LifecycleListener[listeners.length - 1];
			int j = 0;
			for (int i = 0; i < listeners.length; i++) {
				if (i != n)
					results[j++] = listeners[i];
			}
			listeners = results;
		}

	}

}
