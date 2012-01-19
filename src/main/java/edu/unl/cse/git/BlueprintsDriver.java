package edu.unl.cse.git;

import java.util.HashMap;
import java.util.Map;

import net.wagstrom.research.github.EdgeType;
import net.wagstrom.research.github.VertexType;
import net.wagstrom.research.github.PropertyName;
import net.wagstrom.research.github.IndexNames;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.research.govsci.graph.BlueprintsBase;
import com.ibm.research.govsci.graph.Shutdownable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;

public class BlueprintsDriver extends BlueprintsBase implements Shutdownable {
	private Logger log = null;

	private Index <Vertex> commitidx = null;
	private Index <Vertex> fileidx = null;
	private Index <Vertex> repoidx = null;
	private Index <Vertex> gituseridx = null;
	private Index <Vertex> nameidx = null;
	private Index <Vertex> emailidx = null;
	
	/**
	 * Base constructor for BlueprintsDriver
	 * 
	 * @param dbengine The name of the engine to use, e.g. neo4j, orientdb, etc
	 * @param dburl The url of the database to use
	 */
	BlueprintsDriver( String dbengine, String dburl, Map <String, String> config ) {
		super(dbengine, dburl, config);
		log = LoggerFactory.getLogger(this.getClass());
		
		typeidx = getOrCreateIndex(IndexNames.INDEX_TYPE);
		commitidx = getOrCreateIndex(IndexNames.INDEX_COMMIT);
		fileidx = getOrCreateIndex(IndexNames.INDEX_FILE);
		repoidx = getOrCreateIndex(IndexNames.INDEX_REPO);
		gituseridx = getOrCreateIndex(IndexNames.INDEX_GIT_USER);
		nameidx = getOrCreateIndex(IndexNames.INDEX_NAME);
		emailidx = getOrCreateIndex(IndexNames.INDEX_EMAIL);
	}
		
	/*
	 * Vertex
	 */
	
	public Vertex saveCommit( RevCommit cmt ) {
		log.info( "Save Commit: " + gitHash( cmt) );
		Vertex node = getOrCreateCommit( gitHash( cmt ) );
		setProperty( node, PropertyName.MESSAGE, cmt.getFullMessage() );
		setProperty( node, PropertyName.IS_MERGE, cmt.getParentCount() > 1 );
		return node;
	}
	
	public Vertex saveRepository( String name ) {
		log.info( "Save Repository: " + name );
		Vertex node = getOrCreateRepository( name );
		//setProperty( node, "isMerge", cmt.getParentCount() > 1 );
		return node;
	}
	
	public Vertex saveFile( String token ) {
		log.info( "Save File: " + token );
		Vertex node = getOrCreateFile( token );
		return node;
	}
	
	public Vertex saveGitUser( PersonIdent person ) {
		log.info( "Save GitUser: " + person.getEmailAddress() );
		String sName = person.getName();
		String sEmail = person.getEmailAddress();
		Vertex gitUser = getOrCreateGitUser( sName, sEmail );
		Vertex vName = getOrCreateName( sName );
		Vertex vEmail = getOrCreateEmail( sEmail );
		createEdgeIfNotExist( gitUser, vName, EdgeType.NAME );
		createEdgeIfNotExist( gitUser, vEmail, EdgeType.EMAIL );
		return gitUser;
	}

	/*
	 * Edges
	 */
			
	public Map<RevCommit, Vertex> saveRepositoryCommits( String reponame, Iterable<RevCommit> cmts ) {
		HashMap<RevCommit, Vertex> mapper = new HashMap<RevCommit, Vertex>();
		Vertex repo_node = getOrCreateRepository( reponame );
		for ( RevCommit cmt : cmts ) {
			Vertex cmt_node = getOrCreateCommit( gitHash( cmt ) );
			createEdgeIfNotExist( cmt_node, repo_node, EdgeType.REPOSITORY );
			mapper.put( cmt, cmt_node );
		}
		return mapper;
	}
	
	public Map<RevCommit, Vertex> saveCommitParents( RevCommit cmt, RevCommit[] parents ) {
		HashMap<RevCommit, Vertex> mapper = new HashMap<RevCommit, Vertex>();
		Vertex child = getOrCreateCommit( gitHash( cmt ) );
		for ( RevCommit parent : parents ) {
			Vertex node = getOrCreateCommit( gitHash( parent ) );
			createEdgeIfNotExist( child, node, EdgeType.COMMITPARENT );
			mapper.put( cmt, node );
		}
		return mapper;
	}
	
	public Map<String, Vertex> saveCommitFiles( RevCommit cmt, Iterable<String> fileTokens ) {
		Vertex cmtNode = getOrCreateCommit( gitHash( cmt ) );
		HashMap<String, Vertex> mapper = new HashMap<String, Vertex>();
		for ( String token : fileTokens ) {
			Vertex fileNode = getOrCreateFile( token );
			createEdgeIfNotExist( cmtNode, fileNode, EdgeType.CHANGED );
			mapper.put( token, fileNode );
		}
		return mapper;
	}
	
	public Vertex saveCommitAuthor( RevCommit cmt, PersonIdent author ) {
		if ( author == null ) { return null; }
		Vertex cmt_node = getOrCreateCommit( gitHash( cmt ) );
		Vertex author_node = saveGitUser( author );
		Edge edge = createEdgeIfNotExist( null, cmt_node, author_node, EdgeType.COMMITAUTHOR );
		setProperty( edge, PropertyName.WHEN, author.getWhen() );
		return author_node;
	}
	
	public Vertex saveCommitCommitter( RevCommit cmt, PersonIdent committer ) {
		Vertex cmt_node = getOrCreateCommit( gitHash( cmt ) );
		Vertex committer_node = saveGitUser( committer );
		Edge edge = createEdgeIfNotExist( null, cmt_node, committer_node, EdgeType.COMMITTER );
		setProperty( edge, PropertyName.WHEN, committer.getWhen() );
		return committer_node;
	}
	
	/*
	 * get or creates
	 */
	
	public Vertex getOrCreateCommit( String hash ) {
		//log.info( "Get or Create Commit: " + hash );
		return getOrCreateVertexHelper("hash", hash, VertexType.COMMIT, commitidx);
	}
	
	public Vertex getOrCreateRepository( String reponame ) {
		return getOrCreateVertexHelper("reponame", reponame, VertexType.REPOSITORY, repoidx);
	}
	
	public Vertex getOrCreateFile( String token ) {
		return getOrCreateVertexHelper("token", token, VertexType.FILE, fileidx);
	}
	
	public Vertex getOrCreateGitUser( String name, String email ) {
		String key = name + " <" + email + ">";
		return getOrCreateVertexHelper("string", key, VertexType.GIT_USER, gituseridx);
	}
	
	public Vertex getOrCreateName( String name ) {
		return getOrCreateVertexHelper("name", name, VertexType.NAME, nameidx);
	}
	
	public Vertex getOrCreateEmail( String email ) {
		return getOrCreateVertexHelper("email", email, VertexType.EMAIL, emailidx);
	}
	
	/*
	 * git helpers
	 */
	
	private String gitHash( AnyObjectId obj ) {
		return obj.getName();
	}
}
