package jgodara.botnet.security;

import java.security.Security;

import jgodara.botnet.Globals;

import org.apache.log4j.Logger;

public final class SecurityConfig {
	private static SecurityConfig singleton = null;

	private static final Logger log = Logger.getLogger(SecurityConfig.class);

	private static final String PACKAGE_ACCESS = "sun.,"
			+ "jgodara.botnet.";

	// FIX ME package "javax." was removed to prevent HotSpot
	// fatal internal errors
	private static final String PACKAGE_DEFINITION = "java.,sun."
			+ ",jgodara.botnet.";
	/**
	 * List of protected package from conf/botnet.properties
	 */
	private String packageDefinition;

	/**
	 * List of protected package from conf/botnet.properties
	 */
	private String packageAccess;

	/**
	 * Create a single instance of this class.
	 */
	private SecurityConfig() {
		try {
			packageDefinition = Globals.getProperty("package.definition");
			packageAccess = Globals.getProperty("package.access");
		} catch (java.lang.Exception ex) {
			if (log.isDebugEnabled()) {
				log.debug(
						"Unable to load properties using BotnetProperties",
						ex);
			}
		}
	}

	/**
	 * Returns the singleton instance of that class.
	 * 
	 * @return an instance of that class.
	 */
	public static SecurityConfig newInstance() {
		if (singleton == null) {
			singleton = new SecurityConfig();
		}
		return singleton;
	}

	/**
	 * Set the security package.access value.
	 */
	public void setPackageAccess() {
		// If corticera.properties is missing, protect all by default.
		if (packageAccess == null) {
			setSecurityProperty("package.access", PACKAGE_ACCESS);
		} else {
			setSecurityProperty("package.access", packageAccess);
		}
	}

	/**
	 * Set the security package.definition value.
	 */
	public void setPackageDefinition() {
		// If corticera.properties is missing, protect all by default.
		if (packageDefinition == null) {
			setSecurityProperty("package.definition", PACKAGE_DEFINITION);
		} else {
			setSecurityProperty("package.definition", packageDefinition);
		}
	}

	/**
	 * Set the proper security property
	 * 
	 * @param properties
	 *            the package.* property.
	 */
	private final void setSecurityProperty(String properties, String packageList) {
		if (System.getSecurityManager() != null) {
			String definition = Security.getProperty(properties);
			if (definition != null && definition.length() > 0) {
				if (packageList.length() > 0) {
					definition = definition + ',' + packageList;
				}
			} else {
				definition = packageList;
			}

			Security.setProperty(properties, definition);
		}
	}

}
