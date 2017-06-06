package jgodara.botnet.core;

import jgodara.botnet.Bot;
import jgodara.botnet.Engine;
import jgodara.botnet.Service;
import jgodara.botnet.container.Container;
import jgodara.botnet.container.ContainerBase;
import jgodara.botnet.lifecycle.LifecycleException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class StandardEngine extends ContainerBase implements Engine {

	private static final Logger logger = Logger.getLogger(StandardEngine.class);
	
	public StandardEngine() {
		super();

		backgroundProcessorDelay = 10;
	}

	private Bot[] bots = new Bot[0];
	private Service service;
	private String info = this.getClass().getName() + "/1.0";
	private final Object botsLock = new Object();

	public void setService(Service service) {
		this.service = service;
	}

	public Service getService() {
		return service;
	}

	@Override
	public String getInfo() {
		return info;
	}

	/**
	 * Disallow any attempt to set a parent because engine is always top in the
	 * container hierarchy.
	 */
	@Override
	public void setParent(Container parent) {
		throw new IllegalArgumentException(
				"This container cannot have a parent.");
	}
	
	@Override
	protected synchronized void startInternal() throws LifecycleException {
		
		logger.info("Starting Botnet Engine: " + getInfo());
		
		super.startInternal();
	}
	
	@Override
	public String toString() {
		return "StandardEngine[" + getName() + "]";
	}

	public Bot[] findBots() {
		return bots;
	}

	public Bot findBot(String name) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		Bot bot = null;
		synchronized (botsLock) {
			for (int i = 0 ; i < bots.length ; i++) {
				if (bots[i].getName().equals(name)) {
					bot = bots[i];
					break;
				}
			}
		}
		
		return bot;
	}

	public void addBot(Bot bot) {
		bot.setParent(this);
		
		synchronized (botsLock) {
			Bot[] results = new Bot[bots.length + 1];
			System.arraycopy(bots, 0, results, 0, bots.length);
			results[bots.length] = bot;
			
			if (getState().isAvailable()) {
				try {
					bot.start();
				} catch (LifecycleException ex) {
					// Ignore
				}
			}
		}

		// TODO Add property change support
		// Report this property change to interested listeners
		// support.firePropertyChange("service", null, service);
	}

	public void removeBot(Bot bot) {

		synchronized (botsLock) {
			int j = -1;
			for (int i = 0; i < bots.length; i++) {
				if (service == bots[i]) {
					j = i;
					break;
				}
			}
			if (j < 0)
				return;
			try {
				bots[j].stop();
			} catch (LifecycleException e) {
				// Ignore
			}
			int k = 0;
			Bot results[] = new Bot[bots.length - 1];
			for (int i = 0; i < bots.length; i++) {
				if (i != j)
					results[k++] = bots[i];
			}
			bots = results;

			// TODO Add property change support
			// Report this property change to interested listeners
			// support.firePropertyChange("service", service, null);
		}

	}

}
