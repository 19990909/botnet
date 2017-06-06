package jgodara.botnet;

import java.util.Map;

import jgodara.botnet.container.Container;

public interface Bot extends Container {
	
	public String getName();
	
	public void setName(String name);
	
	public void setBotFacade(BotFacade facade);
	
	public BotFacade getFacade();
	
	public interface BotFacade {
		
		public void out(Object data);
		
		public String name();
		
		public void setProperties(Map<String, String> props);
		
		public void act();
		
		public String results();
		
		public void setDelegate(Bot delegate);

	}

}
