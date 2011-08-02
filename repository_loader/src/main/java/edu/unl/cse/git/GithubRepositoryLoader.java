package edu.unl.cse.git;

import java.io.File;

import org.eclipse.jgit.api.Git;

public class GithubRepositoryLoader extends RepositoryLoader {
	String METHOD = "http://";
	String BASE = "github.com/";
	
	GithubRepositoryLoader( String username, String repoName ) {
		git = Git.cloneRepository()
			.setBare( true )
			.setCloneAllBranches( true )
			.setURI( METHOD + BASE + username + "/" + repoName )
			.setDirectory( new File( "/tmp/1234aoeu" ) )
			.call();
		//miner = new RepositoryMiner( username + repoName, git );
	}
}
