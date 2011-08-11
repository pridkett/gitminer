package edu.unl.cse.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryLoader {
	final static String METHOD = "http://";
	final static String BASE = "github.com/";
	final static File LOCAL_STORE = new File( "/tmp/repo_loader" );
	final static Logger log = LoggerFactory.getLogger(RepositoryLoader.class);
	
	static public Git getRepository( String username, String repoName ) {
		return getRepository( username +  repoName );
	}
	
	static public Git getRepository( String name ) {
		if ( !repositoryIsCloned( name ) ) { 
			return cloneRepository( name );
		}
		return updateRepository( name );
	}

	static private boolean repositoryIsCloned( String name ) {
		// simple for now, determine if more robust checking is needed...
		return new File( LOCAL_STORE, name ).exists();
	}
	
	static private Git cloneRepository( String name ) {
		return Git.cloneRepository()
				.setBare( true )
				.setCloneAllBranches( true )
				.setURI( METHOD + BASE + name + ".git" )
				.setDirectory( new File( LOCAL_STORE, name ) )
				.call();
	}
	
	static private Git updateRepository( String name ) {
		try {
			Git repo = Git.open( new File( LOCAL_STORE, name ) );
			//TODO i'm unsure about this...
			//repo.fetch();
			return repo;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	static public Iterable<RevCommit> getCommits( String reponame ) {
		try {
			return RepositoryLoader.getRepository( reponame ).log().call();
		} catch (NoHeadException e) {
			e.printStackTrace();
			System.exit( 1 );
		} catch (JGitInternalException e) {
			e.printStackTrace();
			System.exit( 1 );
		}
		return null;
	}
	
	static public String fileToken( String reponame, String fileName ) {
		//TODO: need to split the username away from the repository name before forming into token
		//reponame.split( "/" );
		return reponame + "--" + fileName;
	}
	
	static public List<String> filesChanged( String reponame, RevCommit cmt ) {
		try {
			List<String> changed = new ArrayList<String>();
			TreeWalk walker = new TreeWalk( getRepository( reponame ).getRepository() );
			walker.setFilter( TreeFilter.ANY_DIFF );
			walker.setRecursive( true );
			walker.addTree( cmt.getTree() );
			for ( RevCommit parent : cmt.getParents() ) {
				walker.addTree( parent.getTree() );
			}
			while ( walker.next() ) {
				changed.add( fileToken( reponame, walker.getPathString() ) );
			}
			return changed;
		} catch (MissingObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CorruptObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
