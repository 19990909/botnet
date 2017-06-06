package jgodara.botnet;

import java.util.concurrent.TimeUnit;

import jgodara.botnet.lifecycle.Lifecycle;

public interface Executor extends Lifecycle {

	public String getName();

	/**
	 * Executes the given command at some time in the future. The command may
	 * execute in a new thread, in a pooled thread, or in the calling thread, at
	 * the discretion of the <tt>Executor</tt> implementation. If no threads are
	 * available, it will be added to the work queue. If the work queue is full,
	 * the system will wait for the specified time until it throws a
	 * RejectedExecutionException
	 *
	 * @param command
	 *            the runnable task
	 * @throws java.util.concurrent.RejectedExecutionException
	 *             if this task cannot be accepted for execution - the queue is
	 *             full
	 * @throws NullPointerException
	 *             if command or unit is null
	 */
	void execute(Runnable command, long timeout, TimeUnit unit);

}
