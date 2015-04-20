package eu.linkedtv.dashboard.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesLoader {
	
	final static Logger logger = Logger.getLogger(PropertiesLoader.class);

	public static Properties loadProperties() {
		String resourceName = "application.properties"; // could also be a constant
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Properties props = new Properties();
		try(InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
		    props.load(resourceStream);
		} catch (IOException e) {
			logger.error("Error while loading properties file.", e);
		}
		return props;		
	}

}
