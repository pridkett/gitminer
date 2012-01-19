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

package edu.unl.cse.git;

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
public class GitProperties {
	static Properties _gitProperties = null;
	
	/**
	 * static method that returns the configuration properties
	 * for the file
	 * 
	 * @return a {java.util.Propeties} reference
	 */
	public static Properties props() {
		if (_gitProperties != null) return _gitProperties;
		_gitProperties = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream properties = loader.getResourceAsStream("configuration.properties");
		try {
			_gitProperties.load(properties);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return _gitProperties;
	}
}
