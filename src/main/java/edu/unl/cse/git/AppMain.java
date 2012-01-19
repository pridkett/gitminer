package edu.unl.cse.git;

import java.util.HashMap;
import java.util.Properties;

import org.eclipse.jgit.revwalk.RevCommit;
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
		
		// pass through all the db.XXX properties to the database
		HashMap<String, String> dbprops = new HashMap<String, String>();
        for (Object o : p.keySet()) {
            String s = (String) o;
            if (s.startsWith("db.")) {
                dbprops.put(s.substring(3), p.getProperty(s));
            }
        }
		
		return new BlueprintsDriver(dbengine, dburl, dbprops);
	}
	
	public void main() {
		Properties p = GitProperties.props();
		
		BlueprintsDriver bp = connectToGraph(p);
		
		String[] repositories = getProperty( p, "edu.unl.cse.git.repositories" ).split(",");
		for ( String reponame : repositories ) {
			log.info( "Loading Repository: " + reponame );
			bp.saveRepository( reponame );
			// commits
			Iterable<RevCommit> cmts = RepositoryLoader.getCommits( reponame );
			for ( RevCommit cmt : cmts ) {
				bp.saveCommit( cmt );
				bp.saveCommitAuthor( cmt, cmt.getAuthorIdent() );
				bp.saveCommitCommitter( cmt, cmt.getCommitterIdent() );
				bp.saveCommitParents( cmt, cmt.getParents() );
				Iterable<String> commitFiles = RepositoryLoader.filesChanged( reponame, cmt );
				for ( String fileName : commitFiles ) {
					bp.saveFile( fileName );
				}
				bp.saveCommitFiles( cmt, commitFiles );
			}
			bp.saveRepositoryCommits( reponame, cmts );
		}
		
		log.info("Shutting down graph");
		bp.shutdown();
	}
}
