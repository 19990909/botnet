package jgodara.botnet.startup;

import jgodara.botnet.lifecycle.Lifecycle;
import jgodara.botnet.lifecycle.LifecycleEvent;
import jgodara.botnet.lifecycle.LifecycleListener;

import org.apache.log4j.Logger;

public class ServerInfoLogListener implements LifecycleListener {
	
	private static final Logger logger = Logger.getLogger(ServerInfoLogListener.class);

	public void lifecycleEvent(LifecycleEvent event) {
		if (event.getType().equals(Lifecycle.BEFORE_INIT_EVENT)) {
			logger.info("Botnet v1.0");
			
			logger.info("Operating System Name: " + System.getProperty("os.name"));
			logger.info("Operating System Version: " + System.getProperty("os.version"));
			logger.info("Operating System Architecture: " + System.getProperty("os.arch"));
			
			logger.info("Java Home: " + System.getProperty("java.home"));
			logger.info("Java VM Version: " + System.getProperty("java.runtime.version"));
			logger.info("Java VM Vendor: " + System.getProperty("java.vm.vendor"));
		}
	}

}
