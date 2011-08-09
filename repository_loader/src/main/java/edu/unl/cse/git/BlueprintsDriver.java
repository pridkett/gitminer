package edu.unl.cse.git;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper;
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper.CommitManager;

public class BlueprintsDriver {
	private enum VertexType {
		COMMIT("COMMIT"),
		REPOSITORY("REPOSITORY"),
		TREE("TREE"),
		USER("USER");
		
		private String text;
		VertexType(String text) {
			this.text = text;
		}
		public String toString() {
			return this.text;
		}
	}
	
	private enum EdgeType {
		REPOSITORY( "REPOSITORY" ),
		PARENT( "PARENT" ),
		AUTHOR( "AUTHOR" ),
		COMMITTER( "COMMITTER" ),
		TREE( "TREE" );
		
		
		private String text;
		
		EdgeType(String text) {
			this.text = text;
		}
		
		public String toString() {
			return this.text;
		}
	}

	private static final String INDEX_TYPE = "type-idx";
	private static final String INDEX_COMMIT = "commit-idx";
	private static final String INDEX_REPO = "repo-idx";
	private static final String INDEX_TREE = "tree-idx";
	private static final String INDEX_USER = "user-idx";

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

	private static final int COMMITMGR_COMMITS = 2000;

	private IndexableGraph graph = null;
	private Logger log = null;
	private SimpleDateFormat dateFormatter = null;

	private Index <Vertex> typeidx = null;
	private Index <Vertex> commitidx = null;
	private Index <Vertex> repoidx = null;
	private Index <Vertex> treeidx = null;
	private Index <Vertex> useridx = null;
	
	private CommitManager manager = null;

	/**
	 * Base constructor for BlueprintsDriver
	 * 
	 * @param dbengine The name of the engine to use, e.g. neo4j, orientdb, etc
	 * @param dburl The url of the database to use
	 */
	BlueprintsDriver( String dbengine, String dburl ) {
		log = LoggerFactory.getLogger(this.getClass());
		dateFormatter = new SimpleDateFormat(DATE_FORMAT);
		dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		if (dbengine.toLowerCase().equals("neo4j")) {
			log.info("opening neo4j graph at " + dburl);
			graph = new Neo4jGraph(dburl);
		} else {
			log.error("Unknown database engine: " + dbengine);
			System.exit(-1);
			// throw new Exception();
		}
		
		typeidx = (Index <Vertex>)getOrCreateIndex(INDEX_TYPE);
		commitidx = (Index <Vertex>)getOrCreateIndex(INDEX_COMMIT);
		repoidx = (Index <Vertex>)getOrCreateIndex(INDEX_REPO);
		treeidx = (Index <Vertex>)getOrCreateIndex(INDEX_TREE);
		useridx = (Index <Vertex>)getOrCreateIndex(INDEX_USER);

		manager = TransactionalGraphHelper.createCommitManager((TransactionalGraph) graph, COMMITMGR_COMMITS);
	}
	/**
	 * Gets a reference to the specified index, creating it if it doesn't exist.
	 * 
	 * This probably could be better written if it used generics or something like that
	 * 
	 * @param idxname the name of the index to load/create
	 * @param indexClass the class the index should use, either Vertex or Edge
	 * @return a reference to the loaded/created index
	 */
	public Index<? extends Element> getOrCreateIndex(String idxname, Class indexClass) {
		Index<? extends Element> repoidx = null;
		for (Index<? extends Element> idx : graph.getIndices()) {
			log.debug("Found index name: " + idx.getIndexName() + " class: " + idx.getIndexClass().toString());
			if (idx.getIndexName().equals(idxname) && indexClass.isAssignableFrom(idx.getIndexClass())) {
				log.debug("Found matching index in database");
				repoidx =  idx;
				break;
			}
		}
		if (repoidx == null) {
			repoidx = graph.createManualIndex(idxname, indexClass);
		}
		return repoidx;
	}
	
	/**
	 * Helper function to get Vertex indexes
	 * 
	 * @param idxname name of the index to retrieve
	 * @return the index if it exists, or a new index if it does not
	 */
	public Index<Vertex> getOrCreateIndex(String idxname) {
		return (Index<Vertex>)getOrCreateIndex(idxname, Vertex.class);
	}
	
	
	/**
	 * Helper function to get Edge indexes
	 * 
	 * @param idxname the name of the index to retrieve
	 * @return the index if it exists, or a new index if it does not
	 */
	public Index<Edge> getOrCreateEdgeIndex(String idxname) {
		return (Index<Edge>)getOrCreateIndex(idxname, Edge.class);
	}
	
	public void shutdown() {
		manager.close();
		if (graph != null) {
			graph.shutdown();
		}
	}

	/*
	 * Vertex
	 */
	
	public Vertex saveCommit( RevCommit cmt ) {
		Vertex node = getOrCreateCommit( cmt.getId().toString() );
		setProperty( node, "message", cmt.getFullMessage() );
		setProperty( node, "isMerge", cmt.getParentCount() > 1 );
		return node;
	}
	
	public Vertex saveRepository( String name ) {
		Vertex node = getOrCreateRepository( name );
		//setProperty( node, "isMerge", cmt.getParentCount() > 1 );
		return node;
	}
	
	public Vertex saveTree( RevTree tree ) {
		Vertex node = getOrCreateTree( tree.getId().toString() );
		//setProperty( node, "message", cmt.getFullMessage() );
		return node;
	}
	
	public Vertex saveUser( PersonIdent person ) {
		Vertex node = getOrCreateUser( person.getEmailAddress() );
		setProperty( node, "name", person.getName() );
		return node;
	}
	
	/*
	 * Edges
	 */
	
	private Edge createEdgeIfNotExist(Object id, Vertex outVertex, Vertex inVertex, EdgeType edgetype) {
		for (Edge e : outVertex.getOutEdges(edgetype.toString())) {
			if (e.getInVertex().equals(inVertex)) return e;
		}
		Edge re = graph.addEdge(id,  outVertex, inVertex, edgetype.toString());
		re.setProperty("created_at", dateFormatter.format(new Date()));
		manager.incrCounter();
		return re;
	}
		
	public Map<String, Vertex> saveRepositoryCommits( String name ) {
		try {
			HashMap<String, Vertex> mapper = new HashMap<String, Vertex>();
			Git repo = RepositoryLoader.getRepository(name);
			Vertex repo_node = getOrCreateRepository( name );
			Iterable<RevCommit> cmts = repo.log().call();
			for ( RevCommit cmt : cmts ) {
				Vertex cmt_node = saveCommit( cmt );
				createEdgeIfNotExist( null, cmt_node, repo_node, EdgeType.REPOSITORY );
				mapper.put( cmt.getId().toString(), cmt_node );
			}
			return mapper;
		} catch (NoHeadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JGitInternalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Map<String, Vertex> saveCommitParents( RevCommit cmt ) {
		HashMap<String, Vertex> mapper = new HashMap<String, Vertex>();
		Vertex child = getOrCreateCommit( cmt.getId().toString() );
		RevCommit[] parents = cmt.getParents();
		for ( RevCommit parent : parents ) {
			Vertex node = getOrCreateCommit( parent.getId().toString() );
			createEdgeIfNotExist( null, child, node, EdgeType.PARENT );
			mapper.put( cmt.getId().toString(), node );
		}
		return mapper;
	}
	
	public Vertex saveCommitTree( RevCommit cmt ) {
		Vertex cmt_node = getOrCreateCommit( cmt.getId().toString() );
		Vertex tree_node = saveTree( cmt.getTree() );
		createEdgeIfNotExist( null, cmt_node, tree_node, EdgeType.TREE );
		return tree_node;
	}
	
	public Vertex saveCommitAuthor( RevCommit cmt ) {
		PersonIdent author = cmt.getAuthorIdent();
		if ( author == null ) { return null; }
		Vertex cmt_node = getOrCreateCommit( cmt.getId().toString() );
		Vertex author_node = saveUser( author );
		Edge edge = createEdgeIfNotExist( null, cmt_node, author_node, EdgeType.AUTHOR );
		edge.setProperty( "when", author.getWhen() );
		return author_node;
	}
	
	public Vertex saveCommitCommitter( RevCommit cmt ) {
		PersonIdent committer = cmt.getAuthorIdent();
		Vertex cmt_node = getOrCreateCommit( cmt.getId().toString() );
		Vertex committer_node = saveUser( committer );
		Edge edge = createEdgeIfNotExist( null, cmt_node, committer_node, EdgeType.COMMITTER );
		edge.setProperty( "when", committer.getWhen() );
		return committer_node;
	}
	
	/*
	 * get or creates
	 */
	protected Vertex getOrCreateVertexHelper(String idcol, Object idval, VertexType vertexType, Index <Vertex> index) {
		Vertex node = null;
		Iterable<Vertex> results = index.get(idcol, idval);
		for (Vertex v : results) {
			node = v;
			break;
		}
		if (node == null) {
			node = graph.addVertex(null);
			node.setProperty(idcol, idval);
			node.setProperty("type", vertexType.toString());
			node.setProperty("created_at", dateFormatter.format(new Date()));
			index.put(idcol, idval, node);
			typeidx.put("type", vertexType.toString(), node);
			manager.incrCounter();
		}
		return node;
	}
	
	public Vertex getOrCreateCommit( String hash ) {
		return getOrCreateVertexHelper("reponame", hash, VertexType.COMMIT, commitidx);
	}
	
	public Vertex getOrCreateRepository( String reponame ) {
		return getOrCreateVertexHelper("reponame", reponame, VertexType.REPOSITORY, repoidx);
	}
	
	public Vertex getOrCreateTree( String hash ) {
		return getOrCreateVertexHelper("reponame", hash, VertexType.TREE, treeidx);
	}
	
	public Vertex getOrCreateUser( String email ) {
		return getOrCreateVertexHelper("reponame", email, VertexType.USER, useridx);
	}
	
	/*
	 * Node property helpers.
	 */
	public void setProperty(Element elem, String propname, String property) {
		// don't save null or empty strings
		if (property != null && !property.trim().equals("")) elem.setProperty(propname, property);
		log.trace("{} = {}", propname, property);
	}
	public void setProperty(Element elem, String propname, Date propdate) {
		if (propdate != null) {
			elem.setProperty(propname, dateFormatter.format(propdate));
			log.trace("{} = {}", propname, dateFormatter.format(propdate));
		} else {
			log.trace("{} = null", propname);
		}
	}
	public void setProperty(Element elem, String propname, int propvalue) {
		elem.setProperty(propname, propvalue);
		log.trace("{} = {}", propname, propvalue);
	}
	public void setProperty(Element elem, String propname, long propvalue) {
		elem.setProperty(propname, propvalue);
		log.trace("{} = {}", propname, propvalue);
	}	
	public void setProperty(Element elem, String propname, double propvalue) {
		elem.setProperty(propname, propvalue);
		log.trace("{} = {}", propname, propvalue);
	}
	public void setProperty(Element elem, String propname, boolean propvalue) {
		elem.setProperty(propname, propvalue);
		log.trace("{} = {}", propname, propvalue);
	}
	
}
