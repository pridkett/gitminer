package edu.unl.cse.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.wagstrom.research.github.GithubProperties;

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
    private final static String METHOD = "https://";
    private final static String BASE = "github.com/";
    private final static File LOCAL_STORE = new File(GithubProperties.props().getProperty("edu.unl.cse.git.localStore", "/tmp/repo_loader" ));
    private final static Logger log = LoggerFactory.getLogger(RepositoryLoader.class); // NOPMD

    static public Git getRepository(final String username, final String repoName) {
        return getRepository(username +  repoName);
    }

    static public Git getRepository(final String name ) {
        if ( !repositoryIsCloned( name ) ) { 
            return cloneRepository( name );
        }
        return updateRepository( name );
    }

    static private boolean repositoryIsCloned(final String name) {
        // simple for now, determine if more robust checking is needed...
        return new File( LOCAL_STORE, name ).exists();
    }

    static private Git cloneRepository(final String name) {
        return Git.cloneRepository()
                .setBare( true )
                .setCloneAllBranches( true )
                .setURI( METHOD + BASE + name + ".git" )
                .setDirectory( new File( LOCAL_STORE, name ) )
                .call();
    }

    static private Git updateRepository(final String name) {
        try {
            Git repo = Git.open( new File( LOCAL_STORE, name ) );
            //TODO i'm unsure about this...
            //repo.fetch();
            return repo;
        } catch (IOException e) {
            log.error("Exception encountered opening repository:", e);
        }
        return null;
    }
    
    static private boolean deleteFile(final File f) {
    	if (f.isDirectory()) {
    		for (File child : f.listFiles()) {
    			deleteFile(child);
    		}
    	}
    	return f.delete();
    }
    
    static public boolean removeRepository(final String name) {
    	return deleteFile(new File(LOCAL_STORE, name));
    }

    static public Iterable<RevCommit> getCommits(final String reponame) {
        try {
            return RepositoryLoader.getRepository( reponame ).log().call();
        } catch (NoHeadException e) {
            log.error("NoHeadException: ", e);
            // System.exit( 1 );
        } catch (JGitInternalException e) {
            log.error("JGitInternalException: ", e);
            // System.exit( 1 );
        } catch (NullPointerException npe) {
            log.error("NullPointerException: ", npe);
        }
        return null;
    }

    static public String fileToken(final String reponame, final String fileName) {
        // need to split the username away from the repository name before forming into token
        return reponame.split( "/" )[1] + "--" + fileName;
    }

    static public List<String> filesChanged(final String reponame, final RevCommit cmt) {
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
            log.error("Exception encountered getting changed files:",e);
        } catch (IncorrectObjectTypeException e) {
            log.error("Exception encountered getting changed files:",e);
        } catch (CorruptObjectException e) {
            log.error("Exception encountered getting changed files:",e);
        } catch (IOException e) {
            log.error("Exception encountered getting changed files:",e);
        }
        return null;
    }
}
