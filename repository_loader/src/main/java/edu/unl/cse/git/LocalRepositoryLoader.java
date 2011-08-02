package edu.unl.cse.git;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

public class LocalRepositoryLoader extends RepositoryLoader {
	LocalRepositoryLoader( File path ) {
		try {
			Repository repo = new RepositoryBuilder()
				.setGitDir( path )
				.readEnvironment() // scan environment GIT_* variables
				.findGitDir() // scan up the file system tree
				.build();
			git = new Git( repo );
			miner = new RepositoryMiner( "local", git );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	LocalRepositoryLoader( String path ) {
		this( new File( path ) );
	}
}
