package edu.unl.cse.git;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppMain {
	Logger log = null;
	
	public AppMain() {
		log = LoggerFactory.getLogger(this.getClass());		
	}
	
	private String getProperty( Properties p, String key ) {
		try {
			return p.getProperty( key ).trim();
		} catch (NullPointerException e) {
			log.error("property " + key + " undefined");
			System.exit(1);
		}
		return null;
	}
	
	private BlueprintsDriver connectToGraph( Properties p ) {
		String dbengine = getProperty( p, "edu.unl.cse.git.dbengine");
		String dburl = getProperty( p, "edu.unl.cse.git.dburl");
		
		return new BlueprintsDriver(dbengine, dburl);
	}
	
	public void main() {
		//ArrayList <String> repositories = new ArrayList<String> ();
		
		Properties p = GitProperties.props();
		
		BlueprintsDriver bp = connectToGraph(p);
		
		String[] repositories = getProperty( p, "edu.unl.cse.git.repositories" ).split(",");
		for ( String repo : repositories ) {
			log.info( "Loading Repository: " + repo );
			bp.saveRepository( repo );
			// commits
			bp.saveRepositoryCommits( repo );
		}
		
		log.info("Shutting down graph");
		bp.shutdown();
	}
}
