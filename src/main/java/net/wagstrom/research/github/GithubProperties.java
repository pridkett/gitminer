/*
 * Copyright 2011 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wagstrom.research.github;

import java.io.FileInputStream;
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
	
	/**
	 * load the properties file from a filename
	 * 
	 * If this is called before the other props() method then it will load the properties
	 * from the file. Otherwise we'll use the stuff in the classpath.
	 * 
	 * @param filename
	 * @return
	 */
	public static Properties props(String filename) {
		if (_githubProperties != null) return _githubProperties;
		_githubProperties = new Properties();
		try {
			_githubProperties.load(new FileInputStream(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return _githubProperties;		
	}
}
