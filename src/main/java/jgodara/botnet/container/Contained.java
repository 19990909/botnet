package jgodara.botnet.container;

import jgodara.botnet.lifecycle.Lifecycle;

public interface Contained extends Lifecycle {

	public Container getContainer();

	public void setContainer(Container container);

}
