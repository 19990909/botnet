package jgodara.botnet;

import jgodara.botnet.container.Container;

public interface Engine extends Container {

	public String getName();

	public void setName(String name);

	public void setService(Service service);

	public Service getService();

}
