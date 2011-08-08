package edu.unl.cse.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;

public class RepositoryLoader {
	protected Git git;
	
	static String METHOD = "http://";
	static String BASE = "github.com/";
	static File LOCAL_STORE = new File( "/tmp/repo_loader" );
	
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
			repo.fetch();
			return repo;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void main() {
		getRepository( "eclipse/jgit" );
	}

}
