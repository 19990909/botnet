package jgodara.botnet.core;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import jgodara.botnet.Executor;
import jgodara.botnet.Server;
import jgodara.botnet.Service;
import jgodara.botnet.container.Container;
import jgodara.botnet.lifecycle.LifecycleBase;
import jgodara.botnet.lifecycle.LifecycleException;
import jgodara.botnet.lifecycle.LifecycleState;

public class StandardService extends LifecycleBase implements Service {

	private static Logger logger = Logger.getLogger(StandardServer.class);

	private String name = "StandardService";

	private Server server = null;
	private Container container = null;

	private ArrayList<Executor> executors = new ArrayList<Executor>();

	public String getInfo() {
		return getClass().getName() + "[" + getName() + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	@Override
	protected void initInternal() throws LifecycleException {

		if (container != null)
			container.init();

		for (Executor executor : findExecutors()) {
			// TODO Executor Domain
			executor.init();
		}
	
		if (logger.isDebugEnabled())
			logger.debug("Service " + getInfo() + " initailized.");

	}

	@Override
	protected void startInternal() throws LifecycleException {
		logger.info("Starting service " + getInfo() + "...");
		setState(LifecycleState.STARTING);

		if (container != null) {
			synchronized (container) {
				container.start();
			}
		}

		synchronized (executors) {
			for (Executor executor : findExecutors()) {
				executor.start();
			}
		}
	}

	@Override
	protected void stopInternal() throws LifecycleException {

		logger.info("Pausing service " + getInfo());
		setState(LifecycleState.STOPPING);

		if (container != null) {
			synchronized (container) {
				container.stop();
			}
		}

		synchronized (executors) {
			for (Executor executor : findExecutors()) {
				executor.stop();
			}
		}
	}

	@Override
	protected void destroyInternal() throws LifecycleException {

		for (Executor executor : findExecutors()) {
			executor.destroy();
		}

		if (container != null)
			container.destroy();
	}
	
	public void addExecutor(Executor ex) {
		synchronized (executors) {
			if (!executors.contains(ex)) {
				executors.add(ex);
				if (getState().isAvailable()) {
					try {
						ex.start();
					} catch (LifecycleException x) {
						logger.error("Executor.start", x);
					}
				}
			}
		}
	}

	public Executor[] findExecutors() {
		synchronized (executors) {
			Executor[] result = new Executor[executors.size()];
			executors.toArray(result);
			return result;
		}
	}

	public Executor getExecutor(String executorName) {
		synchronized (executors) {
			for (Executor executor : executors) {
				if (executorName.equals(executor.getName()))
					return executor;
			}
		}
		return null;
	}

	public void removeExecutor(Executor ex) {
		synchronized (executors) {
			if (executors.remove(ex) && getState().isAvailable()) {
				try {
					ex.stop();
				} catch (LifecycleException e) {
					logger.error("Executor.stop", e);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return getInfo();
	}

}
