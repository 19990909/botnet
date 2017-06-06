package jgodara.botnet.startup;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import jgodara.botnet.Globals;
import jgodara.botnet.ServerManager;
import jgodara.botnet.startup.ClassLoaderFactory.Repository;
import jgodara.botnet.startup.ClassLoaderFactory.RepositoryType;
import jgodara.botnet.utils.ExceptionUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class Bootstrap {

	private static final Logger logger = Logger.getLogger(Bootstrap.class);

	private static Bootstrap instance = null;
	private static ServerManager serverInstance = null;

	ClassLoader commonLoader;
	ClassLoader appServerLoader;
	ClassLoader sharedLoader;

	public void init() throws Exception {
		setBotnetHome();
		setBotnetBase();

		initClassLoaders();

		Thread.currentThread().setContextClassLoader(appServerLoader);

		String startupClassName = System.getProperty(
				Globals.BOTNET_STARTUP_CLASS,
				"jgodara.botnet.startup.DefaultServerManager");

		if (logger.isDebugEnabled())
			logger.debug("Loading Startup class " + startupClassName);

		Class<?> appServerClass = appServerLoader.loadClass(startupClassName);
		Object startupObject = appServerClass.newInstance();

		if (!(startupObject instanceof ServerManager))
			throw new Exception("Startup class should be an instance of "
					+ ServerManager.class);

		ServerManager startable = (ServerManager) startupObject;
		startable.setParentClassLoader(sharedLoader);

		if (logger.isDebugEnabled())
			logger.debug("Startup class has finished loading.");

		serverInstance = startable;
	}

	private void setBotnetHome() {
		if (!StringUtils.isEmpty(System
				.getProperty(Globals.BOTNET_HOME_PROP)))
			return;

		File bootstrapJar = new File(System.getProperty(Globals.USER_DIR_PROP,
				"bootstrap.jar"));
		if (bootstrapJar.exists()) {
			try {
				System.setProperty(
						Globals.BOTNET_HOME_PROP,
						new File(System
								.getProperty(Globals.USER_DIR_PROP, ".."))
								.getCanonicalPath());
			} catch (Exception ex) {
				System.setProperty(Globals.BOTNET_HOME_PROP,
						System.getProperty(Globals.USER_DIR_PROP));
			}
		} else {
			System.setProperty(Globals.BOTNET_HOME_PROP,
					System.getProperty(Globals.USER_DIR_PROP));
		}
	}

	private void setBotnetBase() {
		if (!StringUtils.isEmpty(System
				.getProperty(Globals.BOTNET_BASE_PROP)))
			return;

		if (!StringUtils.isEmpty(System
				.getProperty(Globals.BOTNET_HOME_PROP)))
			System.setProperty(Globals.BOTNET_BASE_PROP,
					System.getProperty(Globals.BOTNET_HOME_PROP));
		else
			System.setProperty(Globals.BOTNET_BASE_PROP,
					System.getProperty(Globals.USER_DIR_PROP));
	}

	private void initClassLoaders() {
		try {
			commonLoader = createClassLoader("common", null);
			if (commonLoader == null)
				// No loader property specified in the server.properties file.
				// Default to this loader.
				commonLoader = this.getClass().getClassLoader();

			sharedLoader = createClassLoader("shared", commonLoader);
			appServerLoader = createClassLoader("server", commonLoader);
		} catch (Throwable t) {
			ExceptionUtils.handleThrowable(t);
			logger.error("Class loader creation threw exception...", t);
			System.exit(1);
		}
	}

	private ClassLoader createClassLoader(String name, ClassLoader parent)
			throws Exception {
		String loaderName = Globals.getProperty(name + ".loader");

		if (StringUtils.isEmpty(loaderName))
			return parent;

		loaderName = replace(loaderName);

		List<Repository> repositories = new ArrayList<ClassLoaderFactory.Repository>();

		StringTokenizer tokenizer = new StringTokenizer(loaderName, ",");
		while (tokenizer.hasMoreElements()) {
			String repository = tokenizer.nextToken().trim();
			if (repository.length() == 0) {
				continue;
			}

			// Check for a JAR URL repository
			try {
				@SuppressWarnings("unused")
				URL url = new URL(repository);
				repositories
						.add(new Repository(repository, RepositoryType.URL));
				continue;
			} catch (MalformedURLException e) {
				// Ignore
			}

			// Local repository
			if (repository.endsWith("*.jar")) {
				repository = repository.substring(0, repository.length()
						- "*.jar".length());
				repositories
						.add(new Repository(repository, RepositoryType.GLOB));
			} else if (repository.endsWith(".jar")) {
				repositories
						.add(new Repository(repository, RepositoryType.JAR));
			} else {
				repositories
						.add(new Repository(repository, RepositoryType.DIR));
			}
		}

		return ClassLoaderFactory.createClassLoader(repositories, parent);
	}

	/**
	 * System property replacement in the given string.
	 * 
	 * @param str
	 *            The original string
	 * @return the modified string
	 */
	protected String replace(String str) {
		// Implementation is copied from ClassLoaderLogManager.replace(),
		// but added special processing for corticera.home and corticera.base.
		String result = str;
		int pos_start = str.indexOf("${");
		if (pos_start >= 0) {
			StringBuilder builder = new StringBuilder();
			int pos_end = -1;
			while (pos_start >= 0) {
				builder.append(str, pos_end + 1, pos_start);
				pos_end = str.indexOf('}', pos_start + 2);
				if (pos_end < 0) {
					pos_end = pos_start - 1;
					break;
				}
				String propName = str.substring(pos_start + 2, pos_end);
				String replacement;
				if (propName.length() == 0) {
					replacement = null;
				} else if (Globals.BOTNET_HOME_PROP.equals(propName)) {
					replacement = Globals.getBotnetHome();
				} else if (Globals.BOTNET_BASE_PROP.equals(propName)) {
					replacement = Globals.getBotnetBase();
				} else {
					replacement = System.getProperty(propName);
				}
				if (replacement != null) {
					builder.append(replacement);
				} else {
					builder.append(str, pos_start, pos_end + 1);
				}
				pos_start = str.indexOf("${", pos_end + 1);
			}
			builder.append(str, pos_end + 1, str.length());
			result = builder.toString();
		}
		return result;
	}

	public void load(String[] args) throws Exception {

		if (args == null || args.length == 0) {
			serverInstance.load();
		} else {
			serverInstance.load(args);
		}

	}

	public void start() throws Exception {
		if (serverInstance == null)
			init();

		serverInstance.start();
	}

	public static void main(String[] args) {

		if (instance == null) {
			Bootstrap bootstrap = new Bootstrap();
			try {
				bootstrap.init();
			} catch (Throwable t) {
				ExceptionUtils.handleThrowable(t);
				logger.error(t);
				t.printStackTrace();
				return;
			}
			instance = bootstrap;
		} else {
			Thread.currentThread().setContextClassLoader(
					instance.appServerLoader);
		}

		try {
			String command = "start";
			if (args.length > 0) {
				command = args[args.length - 1];
			}

			if ("start".equals(command)) {
				serverInstance.setAwait(true);
				instance.load(args);
				instance.start();
			} else if ("stop".equals(command)) {
				serverInstance.stop();
			} else {
				logger.warn("Bootstrap: command \"" + command
						+ "\" does not exist.");
			}
		} catch (Throwable t) {
			if (t instanceof InvocationTargetException && t.getCause() != null) {
				t = t.getCause();
			}

			t.printStackTrace();
			System.exit(1);
		}

	}

}
