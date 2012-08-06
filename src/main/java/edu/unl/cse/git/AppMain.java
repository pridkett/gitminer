package edu.unl.cse.git;

import java.util.HashMap;
import java.util.Properties;

import net.wagstrom.research.github.GithubProperties;

import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppMain {
    private static final Logger log = LoggerFactory.getLogger(AppMain.class); // NOPMD

    public AppMain() {
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

    private CommitBlueprintsDriver connectToGraph( Properties p ) {
        String dbengine = getProperty( p, "edu.unl.cse.git.dbengine");
        String dburl = getProperty( p, "edu.unl.cse.git.dburl");

        // pass through all the db.XYZ properties to the database
        HashMap<String, String> dbprops = new HashMap<String, String>();
        for (Object o : p.keySet()) {
            String s = (String) o;
            if (s.startsWith("db.")) {
                dbprops.put(s.substring(3), p.getProperty(s));
            }
        }

        return new CommitBlueprintsDriver(dbengine, dburl, dbprops);
    }
    
    public void loadRepository(CommitBlueprintsDriver bp, String reponame) {
        log.info( "Loading Repository: " + reponame );
        bp.saveRepository( reponame );
        // commits
        Iterable<RevCommit> cmts = RepositoryLoader.getCommits( reponame );
        if (cmts != null) {
            for ( RevCommit cmt : cmts ) {
                try {
                    bp.saveCommit( cmt );
                    bp.saveCommitAuthor( cmt, cmt.getAuthorIdent() );
                    bp.saveCommitCommitter( cmt, cmt.getCommitterIdent() );
                    bp.saveCommitParents( cmt, cmt.getParents() );
                    Iterable<String> commitFiles = RepositoryLoader.filesChanged( reponame, cmt );
                    for ( String fileName : commitFiles ) {
                        bp.saveFile( fileName );
                    }
                    bp.saveCommitFiles( cmt, commitFiles );
		} catch (java.nio.charset.UnsupportedCharsetException uce) {
                    // FIXME: there should be a more descriptive error message
                    log.error("FIXME: Unsupported character set parsing commit", uce);
                } catch (java.nio.charset.IllegalCharsetNameException ice) {
                    // FIXME: there should be a more descriptive error message
                    log.error("Illegal charset name exception", ice);
                }
            }
            // refresh the iterator otherwise it will be empty
            cmts = RepositoryLoader.getCommits( reponame );
            bp.saveRepositoryCommits( reponame, cmts );
        }
    }
    
    public void main() {
        Properties p = GithubProperties.props();

        CommitBlueprintsDriver bp = connectToGraph(p);

        String[] repositories = getProperty( p, "edu.unl.cse.git.repositories" ).split(",");
        for ( String reponame : repositories ) {
       	    loadRepository(bp, reponame);
            // remove if configured to
            if (getProperty(p,"edu.unl.cse.git.repositories.removeAfterLoad").equals("true")) {
            	log.info("Removing Local Repository: " + reponame);
            	RepositoryLoader.removeRepository(reponame);
            }
        }

        log.info("Shutting down graph");
        bp.shutdown();
    }
}
