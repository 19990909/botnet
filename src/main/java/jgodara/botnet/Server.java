package jgodara.botnet;

import jgodara.botnet.lifecycle.Lifecycle;

public interface Server extends Lifecycle {

	public String getInfo();

	public int getPort();

	public void setPort(int port);

	public void setAddress(String address);

	public String getAddress();

	public void setShutdown(String shutdown);

	public String getShutdown();

	public void await();
	
	public void setServerManager(ServerManager serverManager);
	
	public ServerManager getServerManager();

	public Service[] findServices();

	public Service findService(String serviceName);

	public void addService(Service service);

	public void removeService(Service service);

	public void setParentClassLoader(ClassLoader loader);

	public ClassLoader getParentClassLoader();

}
