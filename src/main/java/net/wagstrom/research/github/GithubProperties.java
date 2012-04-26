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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple container class for properties file
 * 
 * This is a static class and should never be instantiated
 * 
 * @author patrick
 */
public class GithubProperties {
    private static Properties internalProps = null;
    private static final Logger log = LoggerFactory.getLogger(GithubProperties.class); // NOPMD

    /**
     * static method that returns the configuration properties
     * for the file
     * 
     * @return a {java.util.Propeties} reference
     */
    public static synchronized Properties props() {
        if (internalProps == null) {
            internalProps = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream properties = loader.getResourceAsStream("configuration.properties");
            try {
                internalProps.load(properties);
            } catch (IOException e) {
                log.error("Exception loading properties: ", e);
            }
        }
        return internalProps;
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
    public static synchronized Properties props(final String filename) {
        if (internalProps == null) {
            FileInputStream input = null;
            internalProps = new Properties();
            try {
                try {
                    input = new FileInputStream(filename);
                    internalProps.load(input);
                } catch (IOException e) {
                    log.error("Exception loading properties from file {}: ", filename, e);
                } finally {
                    if (input != null) {
                        input.close();
                    }
                }
             
            } catch (IOException e) {
                log.error("Exception closing FileInputStream: {}", e);
            }
        }
        return internalProps;
    }
}
