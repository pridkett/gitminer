package net.wagstrom.research.github;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Simple container class for properties file
 * 
 * This is a static class and should never be instantiated
 * 
 * @author patrick
 */
public class GithubProperties {
	static Properties _githubProperties = null;
	
	/**
	 * static method that returns the configuration properties
	 * for the file
	 * 
	 * @return a {java.util.Propeties} reference
	 */
	public static Properties props() {
		if (_githubProperties != null) return _githubProperties;
		_githubProperties = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream properties = loader.getResourceAsStream("configuration.properties");
		try {
			_githubProperties.load(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return _githubProperties;
	}
}
