package edu.unl.cse.git;

import java.util.Properties;

import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
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
		
		return new BlueprintsDriver(dbengine, dburl);
	}
	
	private Iterable<RevCommit> getCommits( String reponame ) {
		try {
			return RepositoryLoader.getRepository( reponame ).log().call();
		} catch (NoHeadException e) {
			log.trace( e.toString() );
			System.exit( 1 );
		} catch (JGitInternalException e) {
			log.trace( e.toString() );
			System.exit( 1 );
		}
		return null;
	}
	
	public void main() {
		Properties p = GitProperties.props();
		
		BlueprintsDriver bp = connectToGraph(p);
		
		String[] repositories = getProperty( p, "edu.unl.cse.git.repositories" ).split(",");
		for ( String reponame : repositories ) {
			log.info( "Loading Repository: " + reponame );
			bp.saveRepository( reponame );
			// commits
			Iterable<RevCommit> cmts = getCommits( reponame );
			for ( RevCommit cmt : cmts ) {
				bp.saveCommit( cmt );
				bp.saveCommitAuthor( cmt, cmt.getAuthorIdent() );
				bp.saveCommitCommitter( cmt, cmt.getCommitterIdent() );
				bp.saveCommitParents( cmt, cmt.getParents() );
			}
			bp.saveRepositoryCommits( reponame, cmts );
		}
		
		log.info("Shutting down graph");
		bp.shutdown();
	}
}
