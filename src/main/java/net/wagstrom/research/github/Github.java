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

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Shell driver class for GitHub mining.
 * 
 * @author Patrick Wagstrom (http://patrick.wagstrom.net/)
 * @see GitHubMain
 * 
 */
public class Github {
    private static final Logger log = LoggerFactory.getLogger(Github.class); // NOPMD


    @Option(name="-c", usage="properties file for configuration")
    private String propsFile = null;

    @Option(name="-l", usage="file for logback configuration")
    private String logbackFile = null;

    /**
     * @param args
     */
    public static void main(String[] args) {
        Github g = new Github();
        g.run(args);
    }

    public Github() {
    }

    public void run(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            log.trace("Parsing arguments...");
            parser.parseArgument(args);

            if (propsFile != null) {
                log.debug("Attempting to read properties file: {}", propsFile);
                GithubProperties.props(propsFile);
            }

            if (logbackFile != null) {
                log.debug("Attempting to read logback configuration file: {}", logbackFile);
                LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

                try {
                    JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(lc);
                    // the context was probably already configured by default configuration 
                    // rules
                    lc.reset(); 
                    configurator.doConfigure(logbackFile);
                } catch (JoranException je) {
                    je.printStackTrace();
                }
                StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
            }
            GitHubMain main = new GitHubMain();

            main.main();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage()); // NOPMD
            System.err.println("\ngithub [options...] arguments..."); //NOPMD
            parser.printUsage(System.err);
        }
    }

    public String getLogbackFile() {
        return logbackFile;
    }

    public void setLogbackFile(String logbackFile) {
        this.logbackFile = logbackFile;
    }

    public String getPropsFile() {
        return propsFile;
    }

    public void setPropsFile(String propsFile) {
        this.propsFile = propsFile;
    }
}
