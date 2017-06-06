package jgodara.botnet.listeners;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;

import jgodara.botnet.Bot;
import jgodara.botnet.Globals;
import jgodara.botnet.lifecycle.Lifecycle;
import jgodara.botnet.lifecycle.LifecycleEvent;
import jgodara.botnet.lifecycle.LifecycleListener;
import jgodara.botnet.utils.ExceptionUtils;

public class VelocityEventListener implements LifecycleListener {

	private static final Logger logger = Logger
			.getLogger(VelocityEventListener.class);

	@SuppressWarnings("deprecation")
	public void lifecycleEvent(LifecycleEvent event) {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "botnet");
		engine.setProperty("botnet.resource.loader.instance",
				new BotnetResourceLoader());
		engine.init();

		StringWriter out = new StringWriter();

		VelocityContext velocityContext = new VelocityContext();

		File botnetBase = new File(Globals.getBotnetBase());
		File eventHooksDirectory = new File(botnetBase, "bot_hooks");
		File botOutputDirectory = new File(botnetBase, "bot_output");

		Bot target = ((Bot) event.getSource());
		File botGroupDirectory = new File(eventHooksDirectory, target
				.getParent().getName());
		File outDir = new File(botOutputDirectory, target.getParent().getName());
		File eventTemplate = new File(botGroupDirectory, "dummy.vm");
		boolean dump = false;
		if (event.getType().equals(Lifecycle.BEFORE_INIT_EVENT)) {
			eventTemplate = new File(botGroupDirectory, "init.vm");
			dump = true;
		} else if (event.getType().equals(Lifecycle.AFTER_START_EVENT)) {
			eventTemplate = new File(botGroupDirectory, "results.vm");
			dump = true;
		}

		if (dump) {
			Template template = engine.getTemplate(eventTemplate
					.getAbsolutePath());
			velocityContext.put("bot", target.getFacade());
			outDir.mkdirs();
			template.merge(velocityContext, out);
			try {
				String data = "";
				File targetFile = new File(outDir, target.getName() + "_"
						+ event.getType() + ".out");
				if (!targetFile.exists()) {
					targetFile.createNewFile();
				} else {
					data = FileUtils.readFileToString(targetFile);
					data += "\n\n============ " + new Date() + " ============\n\n";
				}

				FileUtils.write(targetFile, data + out);
			} catch (IOException e) {
				logger.fatal(
						"Cannot dump bot output to file for "
								+ target.getName(), e);
				ExceptionUtils.handleThrowable(e);
			}
		}
	}

	public class BotnetResourceLoader extends FileResourceLoader {

		@Override
		public InputStream getResourceStream(String source)
				throws ResourceNotFoundException {
			try {
				File f = new File(source);
				return new FileInputStream(f);
			} catch (Exception ex) {
				return null;
			}
		}

	}

}
