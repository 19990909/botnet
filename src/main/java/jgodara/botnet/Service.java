package jgodara.botnet;

import jgodara.botnet.container.Container;
import jgodara.botnet.lifecycle.Lifecycle;

public interface Service extends Lifecycle {

	public String getInfo();

	public String getName();

	public void setName(String name);

	public Server getServer();

	public void setServer(Server server);

	public Container getContainer();

	public void setContainer(Container container);;

	public void addExecutor(Executor executor);

	public Executor[] findExecutors();

	public Executor getExecutor(String name);

	public void removeExecutor(Executor executor);

}
