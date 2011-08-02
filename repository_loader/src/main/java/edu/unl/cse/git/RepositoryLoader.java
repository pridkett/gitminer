package edu.unl.cse.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;

public class RepositoryLoader {
	protected Git git;
	protected RepositoryMiner miner;
	
	public void main() {
		System.out.println( miner.getName() );
		for ( RevCommit cmt : miner.getCommits() ) {
			System.out.println( cmt.getTree().getName() );
		}
	}

}
