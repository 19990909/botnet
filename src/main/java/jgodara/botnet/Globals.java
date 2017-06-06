package jgodara.botnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import jgodara.botnet.utils.ExceptionUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class Globals {

	private static final Logger logger = Logger.getLogger(Globals.class);

	public static final String BOTNET_HOME_PROP = "botnet.home";
	public static final String BOTNET_BASE_PROP = "botnet.base";
	public static final String BOTNET_CONFIG_PROP = "botnet.config";
	public static final String BOTNET_STARTUP_CLASS = "botnet.startup.class";
	public static final String BOTNET_SERVER_CLASS = "botnet.server.class";
	public static final String BOTNET_SERVER_SERVICE_CLASS = "botnet.server.service.class";
	public static final String BOTNET_SERVER_SERVICE_ENGINE_CLASS = "botnet.server.service.container.class";
	public static final String BOTNET_SERVER_SERCIVE_ENGINE_BOT_CLASS = "botnet.server.service.container.bot.class";
	public static final String BOTNET_SERVER_SERCIVE_ENGINE_BOT_FACADE_CLASS = "botnet.server.service.container.bot.facade.class";

	public static final String USER_DIR_PROP = "user.dir";

	private static Properties properties = null;

	static {
		loadApplicationProperties();
	}

	public static String getProperty(String name) {
		return properties.getProperty(name);
	}

	private static void loadApplicationProperties() {

		InputStream is = null;
		Throwable error = null;

		try {
			File home = new File(getBotnetBase());
			File conf = new File(home, "conf");
			File props = new File(conf, "botnet.properties");
			is = new FileInputStream(props);
		} catch (Throwable t) {
			ExceptionUtils.handleThrowable(t);
			error = t;
		}

		if (is != null) {
			logger.info("Start reading server configuration.");

			try {
				properties = new Properties();
				properties.load(is);
			} catch (Throwable t) {
				ExceptionUtils.handleThrowable(t);
				error = t;
			} finally {
				try {
					is.close();
				} catch (Throwable t) {
					logger.warn("Could not close properties file...", t);
					ExceptionUtils.handleThrowable(t);
				}
			}
		}

		if (is == null || error != null) {
			logger.warn("Could not read server properties...", error);
			properties = new Properties();
		}

		Enumeration<?> enumeration = properties.propertyNames();
		while (enumeration.hasMoreElements()) {
			String propName = (String) enumeration.nextElement();
			String value = getProperty(propName);
			if (!StringUtils.isEmpty(value))
				System.setProperty(propName, value);
		}

		logger.info("Done reading server configuration.");

	}

	public static String getBotnetBase() {
		return System.getProperty(BOTNET_BASE_PROP, getBotnetHome());
	}

	public static String getBotnetHome() {
		return System.getProperty(BOTNET_HOME_PROP,
				System.getProperty(USER_DIR_PROP));
	}

}
