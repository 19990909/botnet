package jgodara.botnet.container;

import jgodara.botnet.Loader;
import jgodara.botnet.lifecycle.Lifecycle;

public interface Container extends Lifecycle {

	public static final String ADD_CHILD_EVENT = "addChild";
	public static final String REMOVE_CHILD_EVENT = "removeChild";
	public static final String ADD_VALVE_EVENT = "addValve";
	public static final String REMOVE_VALVE_EVENT = "removeValve";

	public String getInfo();

	public String getName();

	public void setName(String name);

	public Container getParent();

	public void setParent(Container parent);

	// TODO Add DirectoryContext support
	// public DirContext getResources();
	//
	// public void setResources(DirContext resources);

	public void backgroundProcess();

	public void addChild(Container child);

	public Container getChild(String name);

	public void removeChild(Container child);

	public Container[] findChildren();

	public void addContainerListener(ContainerListener listener);

	public void removeContainerListener(ContainerListener listener);

	public ContainerListener[] findContainerListeners();

	public void fireContainerEvent(String type, Object data);

	public void setStartStopThreads(int threads);

	public void setBackgroungProcessorDelay(int backgroundProcessorDelay);

	public int getBackgroundProcessorDelay();

	public Loader getLoader();

	public void setLoader(Loader loader);

	// TODO Add accessLogger

}
