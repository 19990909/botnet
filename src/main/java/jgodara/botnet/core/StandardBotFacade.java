package jgodara.botnet.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import jgodara.botnet.Bot;
import jgodara.botnet.Bot.BotFacade;
import jgodara.botnet.lifecycle.LifecycleException;

/**
 *	Standard bot facade that tries to spam an echo service;
 */
public class StandardBotFacade implements BotFacade {
	
	private static final Logger logger = Logger.getLogger(StandardBotFacade.class);
	
	private String apiPath = "";
	private Bot delegate;
	private String result = "";

	public void out(Object data) {
		logger.info(delegate.getName() + " ---> " + data);
	}

	public String name() {
		return delegate.getName();
	}

	public void setProperties(Map<String, String> props) {
		apiPath = props.get("api");
	}

	public void act() {
		if (StringUtils.isEmpty(apiPath)) {
			try {
				delegate.stop();
			} catch (LifecycleException e) {
			}
			return;
		}
		
		try {
			URL obj = new URL(apiPath);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	
			// optional default is GET
			con.setRequestMethod("GET");
	
			int responseCode = con.getResponseCode();
			out("Sending 'GET' request to URL : " + apiPath);
			out("Response Code : " + responseCode);
	
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			result = response.toString();
		} catch (Exception ex) {
			result = "Facade Failure!!!: " + ex.getMessage();
			out("Facade Failed.");
			out(ex);
		}
	}

	public String results() {
		return result;
	}

	public void setDelegate(Bot delegate) {
		this.delegate = delegate;
	}

}
