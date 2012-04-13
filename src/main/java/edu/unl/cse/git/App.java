package edu.unl.cse.git;

import net.wagstrom.research.github.GitHubMain;
import net.wagstrom.research.github.GithubProperties;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class App 
{
	private Logger log = null;

	@Option(name="-c", usage="properties file for configuration")
	private String propsFile = null;
	
	@Option(name="-l", usage="file for logback configuration")
	private String logbackFile = null;
	
	public static void main(final String[] args )
    {
		App a = new App();
		a.run(args);
    }
	
	public App() {
		log = LoggerFactory.getLogger(App.class);
	}
	
	public void run(final String[] args) {
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
		    new AppMain().main();							
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("\nrepository_loader [options...] arguments...");
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
