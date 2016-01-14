package charakajg.marshr.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
	private static final String CONFIG_PROP_FILE_NAME = "config.properties";
	private static Properties config;

	public static Properties getConfig() {
		if (config == null) {
			config = new Properties();
			try {
		   		config.load(new FileInputStream(CONFIG_PROP_FILE_NAME));
		   	} catch (IOException ioe) {
		   		ioe.printStackTrace();
		    }
			
		}
		return config;
	}	
}
