package jgodara.botnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jgodara.botnet.Bot.BotFacade;
import jgodara.botnet.lifecycle.Lifecycle;
import jgodara.botnet.lifecycle.LifecycleException;
import jgodara.botnet.lifecycle.LifecycleListener;
import jgodara.botnet.lifecycle.LifecycleState;
import jgodara.botnet.security.SecurityConfig;
import jgodara.botnet.utils.ExceptionUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

public abstract class ServerManager {
	
	protected static final Logger logger = Logger.getLogger(ServerManager.class);
	
	private Server server = null;
	
	protected ClassLoader parentClassLoader = ServerManager.class.getClassLoader();
	protected String configFile = "conf/botnet.properties";
	protected boolean await = false;
	protected boolean useShutdownHook = true;
	protected Thread shoutdownHook;

    /**
     * Are we starting a new server?
     *
     * @deprecated  Unused - will be removed in DefaultServerManager 2.0.x
     */
    @Deprecated
    protected boolean starting = false;


    /**
     * Are we stopping an existing server?
     *
     * @deprecated  Unused - will be removed in DefaultServerManager 2.0.x
     */
    @Deprecated
    protected boolean stopping = false;
	
	public ServerManager() {
		setSecurityProtection();
	}
	
	public void start() throws Exception {
		if (getServer() == null) 
			load();
		
		if (getServer() == null) {
			logger.fatal("Server instance could not be created.");
			return;
		}
		
		long timpoint1 = System.nanoTime();
		
		try {
			getServer().start();
		} catch (LifecycleException ex) {
			logger.fatal("Server startup failed!", ex);
			try {
				getServer().destroy();
			} catch (LifecycleException ex1) {
				logger.debug("The failed server could not be destroyed", ex1);
			}
			return;
		}
		
		long timepoint2 = System.nanoTime();
		logger.info("Server started in " + ((timepoint2 - timpoint1) / 1000000) + " miliseconds.");
		
		// Register the shutdown hook.
		if (useShutdownHook) {
			if (shoutdownHook == null)
				shoutdownHook = new BotnetShutdownHook();
			
			Runtime.getRuntime().addShutdownHook(shoutdownHook);
		}
		
		if (await) {
			getServer().await();
			stop();
		}
		
	}
	
	public void stop() {		
		
		try {
			// Remove shutdown hook first so that stop() does not gets called
			// twice.
			if (useShutdownHook)
				Runtime.getRuntime().removeShutdownHook(shoutdownHook);
		} catch (Throwable t) {
			ExceptionUtils.handleThrowable(t);
		}
		
		try {
			Server server = getServer();
			LifecycleState state = server.getState();
			if (LifecycleState.STOPPING_PREP.compareTo(state) <= 0
					&& LifecycleState.DESTROYED.compareTo(state) >= 0) {
				// Already Destroyed
			} else {
				server.stop();
				server.destroy();
			}
		} catch (LifecycleException ex) {
			logger.error("DefaultServerManager.stop", ex);
		}
	}
	
	public void load() throws Exception {
		
		long t1 = System.nanoTime();
		
		initDirectories();
		initNaming();
		
		instantiateServer();
		
		getServer().setServerManager(this);
		
		try {
			getServer().init();
		} catch (LifecycleException ex) {
			if (Boolean.getBoolean("corticera.startup.EXIT_ON_FAILURE")) {
				throw new Error(ex);
			} else {
				logger.error("Could not start server.", ex);
			}
		}
		
		long t2 = System.nanoTime();
		logger.info("Server intialized in " + ((t2 - t1) / 1000000) + " miliseconds.");
	}
	
	public void load(String[] args) {
	
		try {
			if (checkAgruments(args)) {
				load();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	protected abstract void initDirectories();
	
	protected abstract void initNaming();
	
	protected void instantiateServer() throws Exception {
		
		String serverClassName = System.getProperty(Globals.BOTNET_SERVER_CLASS
				, "jgodara.botnet.StandardServer");
		
		if (logger.isDebugEnabled())
			logger.debug("Using server " + serverClassName);
		
		Server serverInstance = (Server) Class.forName(serverClassName).newInstance();
		
		if (!StringUtils.isEmpty(System.getProperty("botnet.port"))) {
			try {
				int port = Integer.parseInt(System.getProperty("botnet.port"));
				serverInstance.setPort(port);
			} catch (NumberFormatException ex) {
				logger.warn("The port is malformed, using default.", ex);
			} catch (Exception e) {
				logger.warn("Cannot read port from system properties, using default.", e);
			}
		}

		InputStream is = null;
		Throwable error = null;
		try {
			String confUrl = System.getProperty(Globals.BOTNET_CONFIG_PROP);
			if (!StringUtils.isEmpty(confUrl))
				is = new FileInputStream(new File(confUrl));
		} catch (Throwable t) {
			ExceptionUtils.handleThrowable(t);
		}
		
		if (is == null) {
			try {
				File home = new File(Globals.getBotnetBase());
				File conf = new File(home, "conf");
				File corticeraXml = new File(conf, "botnet.xml");
				is = new FileInputStream(corticeraXml);
			} catch (Throwable t) {
				ExceptionUtils.handleThrowable(t);
				error = t;
			}
		}
		
		if (is == null || error != null) {
			Exception ex = new Exception("botnet.xml could not be loaded.", error);
			logger.error(ex);
			if (Boolean.getBoolean("corticera.startup.EXIT_ON_FAILURE")) {
				return;
			}
		}
		
		try {
			loadConfigFile(is, serverInstance);
		} catch (Throwable t) {
			logger.fatal("Cannot load botnet.xml file!!!!!", t);
			return;
		}
		
		setServer(serverInstance);
	}
	
	@SuppressWarnings("unchecked")
	protected void loadConfigFile(InputStream is, Server serverInstance) throws Exception {		
		Document document = DocumentHelper.parseText(new String(IOUtils.toByteArray(is)));
		
		Node rootNode = document.selectSingleNode("/Botnet");
		registerListeners(rootNode, serverInstance);
		
		List<Node> servicesInDOM = rootNode.selectNodes("./Srvc");
		
		for (Node serviceNode : servicesInDOM) {
			
			String serviceClassName = System.getProperty(Globals.BOTNET_SERVER_SERVICE_CLASS,
										"jgodara.botnet.StandardService");
			
			if (serverInstance.findService(serviceNode.valueOf("@name")) != null) {
				logger.warn("Cannot add service " + serviceNode.valueOf("@name") + " (Already Added).");
				continue;
			}
			
			if (!StringUtils.isEmpty(serviceNode.valueOf("@class")))
				serviceClassName = serviceNode.valueOf("@class");
			
			Service serviceObject = (Service) Class.forName(serviceClassName).newInstance();
			serviceObject.setName(serviceNode.valueOf("@name"));
			serviceObject.setServer(serverInstance);			
			
			Engine engineInstance = createEngine(serviceNode);
			registerListeners(serviceNode, serviceObject);
			
			serviceObject.setContainer(engineInstance);		
			
			if (logger.isDebugEnabled())
				logger.debug("Adding service " + serviceObject.getInfo());
			
			serverInstance.addService(serviceObject);			
		}
	}
	
	@SuppressWarnings("unchecked")
	private void registerListeners(Node node, Lifecycle target) 
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		List<Node> listeners = node.selectNodes("./Listener");
		
		for (Node listenerNode : listeners) {		
			LifecycleListener listener = (LifecycleListener) 
					Class.forName(listenerNode.valueOf("@class")).newInstance();
			
			if (logger.isDebugEnabled())
				logger.debug("Adding lifecycle listener " + listener.getClass().getName() + ".");
			
			target.addLifecycleListener(listener);
			
		}
	}
	
	private Engine createEngine(Node serviceNode) throws InstantiationException, IllegalAccessException, ClassNotFoundException {;
	
		Map<String, Service> engineRef = new HashMap<String, Service>();
		
		Node engineNode = serviceNode.selectSingleNode("./Engine");
		
		if (engineRef.get(engineNode.valueOf("@name")) != null) {
			IllegalArgumentException ex = new IllegalArgumentException("Engine name '"
					+ engineNode.valueOf("@name") + " is already bound to " + engineRef.get(engineNode.valueOf("@name")));
			throw ex;
		}
		
		String engineClassName = System.getProperty(Globals.BOTNET_SERVER_SERVICE_ENGINE_CLASS,
									"jgodara.botnet.StandardEngine");
		
		if (!StringUtils.isEmpty(engineNode.valueOf("@class")))
			engineClassName = engineNode.valueOf("@class");
		
		Engine engineInstance = (Engine) Class.forName(engineClassName).newInstance();
		engineInstance.setName(engineNode.valueOf("@name"));
		
		List<Bot> bots = createBots(engineNode, engineInstance);
		for (Bot bot : bots) {
			registerListeners(engineNode, bot);
		}
		
		return engineInstance;
	}
	
	private List<Bot> createBots(Node engineNode, Engine engine) 
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		List<Bot> bots = new ArrayList<Bot>();
		int botNum = 25;
		try {
			botNum = Integer.parseInt(engineNode.valueOf("@bots"));
			if (botNum <= 0)
				throw new Exception("Number of bots should be more than zero.");
		} catch (Exception ex) {
			logger.warn("Bot number is malformed. Using default (25)", ex);
			ExceptionUtils.handleThrowable(ex);
		}
		for (int i = 0 ; i < botNum ; i++) {
			
			String botClass = System.getProperty(Globals.BOTNET_SERVER_SERCIVE_ENGINE_BOT_CLASS,
					"jgodara.botnet.core.StandardBot");
			Bot botinstance = (Bot) Class.forName(botClass).newInstance();
			
			String botName = "BOT-" + i;
			if (!StringUtils.isEmpty(engineNode.valueOf("@botnamepattern"))) {
				botName = engineNode.valueOf("@botnamepattern").replaceAll("%id%", i + "");
			}
			
			String botFacadeClass = System.getProperty(Globals.BOTNET_SERVER_SERCIVE_ENGINE_BOT_FACADE_CLASS,
					"jgodara.botnet.core.StandardBotFacade");
			BotFacade botFacadeInstance = (BotFacade) Class.forName(botFacadeClass).newInstance();
			
			botFacadeInstance.setDelegate(botinstance);
			
			botinstance.setBotFacade(botFacadeInstance);
			botinstance.setName(botName);
			botinstance.setParent(engine);
			
			engine.addChild(botinstance);
			registerListeners(engineNode, botinstance);
			
		}
		
		return bots;
	}
	
	protected void setSecurityProtection() {
		SecurityConfig securityConfig = SecurityConfig.newInstance();
		securityConfig.setPackageAccess();
		securityConfig.setPackageDefinition();
	}
	
	protected boolean checkAgruments(String[] args) {

        boolean isConfig = false;

        if (args.length < 1) {
            usage();
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            if (isConfig) {
                configFile = args[i];
                isConfig = false;
            } else if (args[i].equals("-config")) {
                isConfig = true;
            } else if (args[i].equals("-help")) {
                usage();
                return (false);
            } else if (args[i].equals("start")) {
                starting = true;
                stopping = false;
            } else if (args[i].equals("configtest")) {
                starting = true;
                stopping = false;
            } else if (args[i].equals("stop")) {
                starting = false;
                stopping = true;
            } else {
                usage();
                return false;
            }
        }

        return true;

	}
	
	protected void usage() {

        System.out.println
            ("usage: java jgodara.botnet.ServerManager"
             + " [ -config {pathname} ]"
             + " { -help | start | stop }");

    }

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public ClassLoader getParentClassLoader() {
		return parentClassLoader;
	}

	public void setParentClassLoader(ClassLoader parentClassLoader) {
		this.parentClassLoader = parentClassLoader;
	}

	public boolean isAwait() {
		return await;
	}

	public void setAwait(boolean await) {
		this.await = await;
	}
	
	protected class BotnetShutdownHook extends Thread {
		
		@Override
		public void run() {
			try {
				if (getServer() != null) {
					ServerManager.this.stop();
				}
			} catch (Throwable t) {
				ExceptionUtils.handleThrowable(t);
				logger.error("Shoutdown Hook Failed!!!", t);
			}
		}
		
	}

}
