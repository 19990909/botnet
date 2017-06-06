package jgodara.botnet.startup;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import jgodara.botnet.Globals;
import jgodara.botnet.ServerManager;

public class DefaultServerManager extends ServerManager {

	private static final Logger logger = Logger
			.getLogger(DefaultServerManager.class);

	@Override
	protected void initDirectories() {

		String botnetHome = System.getProperty(Globals.BOTNET_HOME_PROP);
		if (StringUtils.isEmpty(botnetHome)) {
			String j2eeHome = System.getProperty("com.sun.enterprise.home");
			if (!StringUtils.isEmpty(j2eeHome)) {
				botnetHome = j2eeHome;
			} else if (!StringUtils.isEmpty(System
					.getProperty(Globals.BOTNET_BASE_PROP))) {
				botnetHome = System.getProperty(Globals.BOTNET_BASE_PROP);
			}
		}

		if (StringUtils.isEmpty(botnetHome))
			botnetHome = System.getProperty(Globals.USER_DIR_PROP);

		if (!StringUtils.isEmpty(botnetHome)) {
			File home = new File(botnetHome);
			if (!home.isAbsolute()) {
				try {
					botnetHome = home.getCanonicalPath();
				} catch (IOException ex) {
					botnetHome = home.getAbsolutePath();
				}
			}

			System.setProperty(Globals.BOTNET_HOME_PROP, botnetHome);
		}

		if (StringUtils
				.isEmpty(System.getProperty(Globals.BOTNET_BASE_PROP))) {
			System.setProperty(Globals.BOTNET_BASE_PROP, botnetHome);
		} else {
			String botnetBase = System
					.getProperty(Globals.BOTNET_BASE_PROP);
			File base = new File(botnetBase);
			if (!base.isAbsolute()) {
				try {
					botnetBase = base.getCanonicalPath();
				} catch (IOException e) {
					botnetBase = base.getAbsolutePath();
				}
			}

			System.setProperty(Globals.BOTNET_BASE_PROP, botnetBase);
		}

		String temp = System.getProperty("java.io.tmpdir");
		File tempDir = new File(temp);
		if (temp == null || (!tempDir.exists()) || (!tempDir.isDirectory())) {
			logger.error("Cannot find specified temporary folder at " + temp);
		}

	}

	@Override
	protected void initNaming() {

		if (logger.isDebugEnabled())
			logger.debug("Initializing naming...");

	}

}
