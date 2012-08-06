package edu.unl.cse.git;

import java.util.HashMap;
import java.util.Map;

import net.wagstrom.research.github.BlueprintsDriver;
import net.wagstrom.research.github.EdgeType;
import net.wagstrom.research.github.IdCols;
import net.wagstrom.research.github.PropertyName;
import net.wagstrom.research.github.VertexType;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.research.govsci.graph.Shutdownable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class CommitBlueprintsDriver extends BlueprintsDriver implements Shutdownable {
    private final static Logger log = LoggerFactory.getLogger(CommitBlueprintsDriver.class); // NOPMD

    /**
     * Base constructor for BlueprintsDriver
     * 
     * @param dbengine The name of the engine to use, e.g. neo4j, orientdb, etc
     * @param dburl The url of the database to use
     */
    CommitBlueprintsDriver(final String dbengine, final String dburl, final Map <String, String> config ) {
        super(dbengine, dburl, config);
    }

    /*
     * Vertex
     */

    public Vertex saveCommit( final RevCommit cmt ) {
        log.info( "Save Commit: {}", gitHash( cmt) );
        Vertex node = getOrCreateCommit( gitHash( cmt ) );
        setProperty( node, PropertyName.DATE, cmt.getCommitTime());
        try {
            setProperty( node, PropertyName.MESSAGE, cmt.getFullMessage() );
        } catch (java.nio.charset.IllegalCharsetNameException ice) {
            // FIXME: should check to see if we can brute force the character set here
            log.error("Illegal charset saving message for commit {}:", gitHash(cmt), ice);
        }
        setProperty( node, PropertyName.IS_MERGE, cmt.getParentCount() > 1 );
        return node;
    }

    /**
     * This is a much simplified method to save a repository.
     * 
     * It only saves the name of the repository.
     * 
     * @param name the name of the repository
     * @return the vertex for the repository
     */
    public Vertex saveRepository( final String name ) {
        log.info( "Save Repository: " + name );
        Vertex node = getOrCreateRepository( name );
        //setProperty( node, "isMerge", cmt.getParentCount() > 1 );
        return node;
    }

    public Vertex saveFile( final String token ) {
        log.info( "Save File: " + token );
        Vertex node = getOrCreateFile( token );
        return node;
    }

    public Vertex saveGitUser( final PersonIdent person ) {
        log.info( "Save GitUser: " + person.getEmailAddress() );
        String sName = person.getName();
        String sEmail = person.getEmailAddress();
        Vertex gitUser = getOrCreateGitUser( sName, sEmail );
        Vertex vName = getOrCreateName( sName );
        Vertex vEmail = getOrCreateEmail( sEmail );
        createEdgeIfNotExist( gitUser, vName, EdgeType.NAME );
        createEdgeIfNotExist( gitUser, vEmail, EdgeType.EMAIL );
        setProperty(gitUser, PropertyName.TIMEZONE_OFFSET, person.getTimeZoneOffset());
        setProperty(gitUser, PropertyName.TIMEZONE, person.getTimeZone().toString());
        setProperty(gitUser, PropertyName.DATE, person.getWhen());
        return gitUser;
    }

    /*
     * Edges
     */

    public Map<RevCommit, Vertex> saveRepositoryCommits( final String reponame, final Iterable<RevCommit> cmts ) {
        HashMap<RevCommit, Vertex> mapper = new HashMap<RevCommit, Vertex>();
        Vertex repo_node = getOrCreateRepository( reponame );
        for ( RevCommit cmt : cmts ) {
            Vertex cmt_node = getOrCreateCommit( gitHash( cmt ) );
            createEdgeIfNotExist( cmt_node, repo_node, EdgeType.REPOSITORY );
            mapper.put( cmt, cmt_node );
        }
        return mapper;
    }

    public Map<RevCommit, Vertex> saveCommitParents( final RevCommit cmt, final RevCommit[] parents ) {
        HashMap<RevCommit, Vertex> mapper = new HashMap<RevCommit, Vertex>();
        Vertex child = getOrCreateCommit( gitHash( cmt ) );
        for ( RevCommit parent : parents ) {
            Vertex node = getOrCreateCommit( gitHash( parent ) );
            createEdgeIfNotExist( child, node, EdgeType.COMMITPARENT );
            mapper.put( cmt, node );
        }
        return mapper;
    }

    public Map<String, Vertex> saveCommitFiles( final RevCommit cmt, final Iterable<String> fileTokens ) {
        Vertex cmtNode = getOrCreateCommit( gitHash( cmt ) );
        HashMap<String, Vertex> mapper = new HashMap<String, Vertex>();
        for ( String token : fileTokens ) {
            Vertex fileNode = getOrCreateFile( token );
            createEdgeIfNotExist( cmtNode, fileNode, EdgeType.CHANGED );
            mapper.put( token, fileNode );
        }
        return mapper;
    }

    public Vertex saveCommitAuthor( final RevCommit cmt, final PersonIdent author ) {
        if ( author == null ) { return null; }
        Vertex cmt_node = getOrCreateCommit( gitHash( cmt ) );
        Vertex author_node = saveGitUser( author );
        Edge edge = createEdgeIfNotExist( cmt_node, author_node, EdgeType.COMMITAUTHOR );
        setProperty( edge, PropertyName.WHEN, author.getWhen() );
        return author_node;
    }

    public Vertex saveCommitCommitter( final RevCommit cmt, final PersonIdent committer ) {
        Vertex cmt_node = getOrCreateCommit( gitHash( cmt ) );
        Vertex committer_node = saveGitUser( committer );
        Edge edge = createEdgeIfNotExist( cmt_node, committer_node, EdgeType.COMMITTER );
        setProperty( edge, PropertyName.WHEN, committer.getWhen() );
        return committer_node;
    }

    /*
     * get or creates
     */

    public Vertex getOrCreateFile( String token ) {
        return getOrCreateVertexHelper(IdCols.FILE, token, VertexType.FILE, fileidx);
    }

    /*
     * git helpers
     */

    private String gitHash( AnyObjectId obj ) {
        return obj.getName();
    }
}
