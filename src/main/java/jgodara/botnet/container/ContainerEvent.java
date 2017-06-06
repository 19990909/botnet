package jgodara.botnet.container;

import java.util.EventObject;

public class ContainerEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	/**
	 * The event data associated with this event.
	 */
	private Object data = null;

	/**
	 * The event type this instance represents.
	 */
	private String type = null;

	/**
	 * Construct a new ContainerEvent with the specified parameters.
	 *
	 * @param container
	 *            Container on which this event occurred
	 * @param type
	 *            Event type
	 * @param data
	 *            Event data
	 */
	public ContainerEvent(Container container, String type, Object data) {

		super(container);
		this.type = type;
		this.data = data;

	}

	/**
	 * Return the event data of this event.
	 */
	public Object getData() {

		return (this.data);

	}

	/**
	 * Return the Container on which this event occurred.
	 */
	public Container getContainer() {

		return (Container) getSource();

	}

	/**
	 * Return the event type of this event.
	 */
	public String getType() {

		return (this.type);

	}

	/**
	 * Return a string representation of this event.
	 */
	@Override
	public String toString() {

		return ("ContainerEvent['" + getContainer() + "','" + getType() + "','"
				+ getData() + "']");

	}

}
