package jgodara.botnet.core;

import org.apache.log4j.Logger;

import jgodara.botnet.Bot;
import jgodara.botnet.container.Container;
import jgodara.botnet.container.ContainerBase;
import jgodara.botnet.lifecycle.LifecycleException;
import jgodara.botnet.lifecycle.LifecycleState;

public class StandardBot extends ContainerBase implements Bot {
	
	private static final Logger logger = Logger.getLogger(StandardBot.class);
	
	private String name = getClass().getName() + "/v1.0";
	private BotFacade facade;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBotFacade(BotFacade facade) {
		this.facade = facade;
	}

	public BotFacade getFacade() {
		return facade;
	}

	@Override
	protected void initInternal() throws LifecycleException {
		logger.info(getParent().getName() + "[" + getName() + "] is ready!");
	}

	@Override
	protected void startInternal() throws LifecycleException {
		setState(LifecycleState.STARTING);
		getFacade().act();
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		setState(LifecycleState.STOPPING);
		logger.info(getName() + " is shutting down");
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		// NOOP		
	}
	
	// A bot should not have any children
	@Override
	public Container getChild(String name) {
		throw new IllegalAccessError("A Bot does not have a child.");
	}
	
	@Override
	public Container[] findChildren() {
		throw new IllegalAccessError("A Bot does not have a child.");
	}
	
	@Override
	public void addChild(Container child) {
		throw new IllegalAccessError("A Bot does not have a child.");
	}
	
	@Override
	public void removeChild(Container child) {
		throw new IllegalAccessError("A Bot does not have a child.");
	}

}
