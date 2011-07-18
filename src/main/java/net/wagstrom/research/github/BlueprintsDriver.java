/*
 * Copyright 2011 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wagstrom.research.github;

import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Organization;
import com.github.api.v2.schema.Repository;
import com.github.api.v2.schema.User;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper;
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper.CommitManager;

/**
 * 
 * @author Patrick Wagstrom (http://patrick.wagstrom.net/)
 *
 */
public class BlueprintsDriver {
	private static final String TYPE_USER = "USER";
	private static final String TYPE_REPO = "REPOSITORY";
	private static final String TYPE_ORGANIZATION = "ORGANIZATION";
	private static final String EDGE_FOLLOWER = "FOLLOWER";
	private static final String EDGE_FOLLOWING = "FOLLOWING";
	private static final String EDGE_ORGANIZATIONOWNER = "ORGANIZATION_OWNER";
	private static final String EDGE_ORGANIZATIONMEMBER = "ORGANIZATION_MEMBER";
	private static final String EDGE_REPOWATCHED = "REPO_WATCHED";
	private static final String EDGE_REPOOWNER = "REPO_OWNER";
	private static final String EDGE_REPOCOLLABORATOR = "REPO_COLLABORATOR";
	private static final String EDGE_REPOCONTRIBUTOR = "REPO_CONTRIBUTOR";
	private static final String EDGE_REPOFORK = "REPO_FORK";
	private static final String INDEX_USER = "user-idx";
	private static final String INDEX_REPO = "repo-idx";
	private static final String INDEX_TYPE = "type-idx";
	private static final String INDEX_ORGANIZATION = "org-idx";
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	
	private static final int COMMITMGR_COMMITS = 2000;
	
	private IndexableGraph graph = null;
	private Logger log = null;
	private SimpleDateFormat dateFormatter = null;

	private Index <Vertex> useridx = null;
	private Index <Vertex> repoidx = null;
	private Index <Vertex> typeidx = null;
	private Index <Vertex> orgidx = null;
	
	private CommitManager manager = null;
	/**
	 * Base constructor for BlueprintsDriver
	 * 
	 * @param dbengine The name of the engine to use, e.g. neo4j, orientdb, etc
	 * @param dburl The url of the database to use
	 */
	public BlueprintsDriver(String dbengine, String dburl) {
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
		useridx = (Index <Vertex>)getOrCreateIndex(INDEX_USER);
		repoidx = (Index <Vertex>)getOrCreateIndex(INDEX_REPO);
		typeidx = (Index <Vertex>)getOrCreateIndex(INDEX_TYPE);
		orgidx = (Index <Vertex>)getOrCreateIndex(INDEX_ORGANIZATION);
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
	
	/**
	 * Saves a User to the graph database
	 * 
	 * Right now this only saves the properties of the user and does not save any relationships.
	 * 
	 * @param user the GitHub user to save
	 * @return the newly created database node
	 */
	public Vertex saveUser(User user) {
		Vertex node = getOrCreateUser(user.getLogin());
		log.info(user.toString());

		if (user.getBlog() != null) node.setProperty("blog", user.getBlog());
		node.setProperty("collaborators", user.getCollaborators());
		if (user.getCompany() != null) node.setProperty("company", user.getCompany());
		if (user.getCreatedAt() != null) node.setProperty("createdAt", dateFormatter.format(user.getCreatedAt()));
		node.setProperty("diskUsage", user.getDiskUsage());
		if (user.getEmail() != null) node.setProperty("email", user.getEmail());
		node.setProperty("followersCount", user.getFollowersCount());
		node.setProperty("followingCount", user.getFollowingCount());
		if (user.getFullname() != null) node.setProperty("fullname", user.getFullname());
		if (user.getGravatarId() != null) node.setProperty("gravatarId", user.getGravatarId());
		if (user.getId() != null) node.setProperty("gitHubId", user.getId()); // note name change
		if (user.getLocation() != null) node.setProperty("location", user.getLocation());
		if (user.getLogin() != null) node.setProperty("login", user.getLogin());
		if (user.getName() != null) node.setProperty("name", user.getName());
		node.setProperty("ownedPrivateRepoCount", user.getOwnedPrivateRepoCount());
		// getPermission
		// getPlan
		node.setProperty("privateGistCount", user.getPrivateGistCount());
		node.setProperty("publicGistCount", user.getPublicGistCount());
		node.setProperty("publicRepoCount", user.getPublicRepoCount());
		node.setProperty("score", user.getScore());
		node.setProperty("totalPrivateRepoCount", user.getTotalPrivateRepoCount());
		if (user.getUsername() != null) node.setProperty("username", user.getUsername());
		node.setProperty("last_updated", dateFormatter.format(new Date())); // save the date of update
		return node;
	}
	
	private Vertex getOrCreateUser(String login) {
		Vertex node = null;
		for (Vertex v : useridx.get("login", login)) {
			node = v;
		}
		if (node == null) {
			node = graph.addVertex(null);
			useridx.put("login", login, node);
			typeidx.put("type", TYPE_USER, node);
			node.setProperty("login", login);
			node.setProperty("created_at", dateFormatter.format(new Date()));
			node.setProperty("type", TYPE_USER);
		}

		manager.incrCounter();
		return node;
	}
	
	private Edge createEdgeIfNotExist(Object id, Vertex outVertex, Vertex inVertex, String label) {
		for (Edge e : outVertex.getOutEdges(label)) {
			if (e.getInVertex().equals(inVertex)) return e;
		}
		Edge re = graph.addEdge(id,  outVertex, inVertex, label);
		re.setProperty("created_at", dateFormatter.format(new Date()));
		manager.incrCounter();
		return re;
	}
	
	public Map<String,Vertex> saveUserFollowersFollowing(String sourceuser, List<String> users, String edgelabel) {
		Vertex source = getOrCreateUser(sourceuser);
		HashMap<String,Vertex> mapper= new HashMap<String,Vertex>();
		for (String user : users) {
			Vertex node = getOrCreateUser(user);
			createEdgeIfNotExist(null, source, node, edgelabel);
			mapper.put(user, node);
		}
		return mapper;
	}
	
	public Map<String,Vertex> saveUserFollowers(String sourceuser, List<String> users) {
		return saveUserFollowersFollowing(sourceuser, users, EDGE_FOLLOWER);
	}

	public Map<String,Vertex> saveUserFollowing(String sourceuser, List<String> users) {
		return saveUserFollowersFollowing(sourceuser, users, EDGE_FOLLOWING);
	}
	
	/**
	 * Given a repository, saves the users that are watching that repository
	 * 
	 * @param project the name of the repository, for example pridkett/github-java-sdk
	 * @param users a list of the usernames to link
	 * @return a map of the username to their representative vertices
	 */
	public Map<String, Vertex> saveRepositoryWatchers(String project, List<String> users) {
		Vertex proj = getOrCreateRepository(project);
		HashMap<String,Vertex> mapper= new HashMap<String,Vertex>();
		for (String user : users) {
			Vertex node = getOrCreateUser(user);
			createEdgeIfNotExist(null, node, proj, EDGE_REPOWATCHED);
			mapper.put(user, node);
		}
		return mapper;
	}
	
	public Map<String, Vertex> saveRepositoryForks(String project, List<Repository> forks) {
		Vertex proj = getOrCreateRepository(project);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Repository repo : forks) {
			String projectFullName = repo.getUsername() + "/" + repo.getName();
			Vertex repoVertex = saveRepository(repo);
			createEdgeIfNotExist(null, proj, repoVertex, EDGE_REPOFORK);
			mapper.put(projectFullName, repoVertex);
		}
		return mapper;
	}
	
	private Map<String, Vertex> saveUserRepositoriesHelper(String user, List<Repository> repositories, String edgelabel) {
		Vertex source = getOrCreateUser(user);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Repository repo : repositories) {
			String projectFullName = repo.getUsername() + "/" + repo.getName();
			Vertex reponode = saveRepository(repo);
			createEdgeIfNotExist(null, source, reponode, edgelabel);
			mapper.put(projectFullName, reponode);
		}
		return mapper;
	}
	
	public Map<String, Vertex> saveUserWatchedRepositories(String user, List<Repository> repos) {
		return saveUserRepositoriesHelper(user, repos, EDGE_REPOWATCHED);
	}
	
	public Map<String, Vertex> saveUserRepositories(String user, List<Repository> repos) {
		return saveUserRepositoriesHelper(user, repos, EDGE_REPOOWNER);
	}
	
	/**
	 * Simple helper function that subtracts d2 from d1
	 * 
	 * @param d1
	 * @param d2
	 * @return difference in days as a double
	 */
	public double dateDifference(Date d1, Date d2) {
		double diff = (d1.getTime() - d2.getTime())/1000/86400;
		log.info("Date1: " + d1.getTime());
		log.info("Date2: " + d2.getTime());
		log.info("Difference: " + diff);
		return diff;
	}
	
	/**
	 * Helper function that gets all of the vertices of a particular type from
	 * the database provided they have not been updated in age days.
	 * 
	 * Vertices that lack a last_updated parameter are always returned
	 * 
	 * FIXME: right now this does NOT use indexes
	 * 
	 * @param age number of days since last_updated
	 * @param idxname the name of the index to use (currently ignored)
	 * @param vtxtype the type of vertex to examine
	 * @param namefield the name of the field to return in the set
	 * @return
	 */
	public Set<String> getVertexHelper(double age, String idxname, String vtxtype, String fieldname) {
		Set<String> s = new HashSet<String>();
		// FIXME: How do we get all of the values from an index?
		// Right now we iterate over all of the nodes, which is CRAPTASTIC
		for (Vertex vtx: graph.getVertices()) {
			Set<String> props = vtx.getPropertyKeys();
			if (props.contains("type") && vtx.getProperty("type").equals(vtxtype)
					&& props.contains("username")) {
				try {
					if (!props.contains("last_updated") ||
							dateDifference(new Date(), dateFormatter.parse((String)vtx.getProperty("last_updated"))) > age)
						s.add((String)vtx.getProperty(fieldname));
				} catch (ParseException e) {
					log.info("Error parsing date: " + vtx.getProperty("last_updated"));
				}
			}
		}
		return s;
	}
	
	
	/**
	 * Gets all of the users that have not been updated in a number of days
	 * @param age the number of days since the last update
	 * @return a set of the users that have not been updated
	 */
	public Set<String> getUsers(double age) {
		return getVertexHelper(age, INDEX_USER, TYPE_USER, "username");
	}
	
	/**
	 * Gets all the repositories that have not been updated in a number of days
	 * 
	 * @param age
	 * @return 
	 */
	public Set<String> getRepos(double age) {
		return getVertexHelper(age, INDEX_REPO, TYPE_REPO, "fullname");
	}

	public Map<String,Vertex> saveRepositoryCollaborators(String reponame, List<String> collabs) {
		HashMap<String, Vertex> mapper = new HashMap<String, Vertex>();
		Vertex repo = getOrCreateRepository(reponame);
		for (String username : collabs) {
			Vertex user = getOrCreateUser(username);
			createEdgeIfNotExist(null, repo, user, EDGE_REPOCOLLABORATOR);
			mapper.put(username, user);
		}
		return mapper;
	}
	
	public Map<String,Vertex> saveRepositoryContributors(String reponame, List<User> contributors) {
		HashMap<String, Vertex> mapper = new HashMap<String, Vertex>();
		Vertex repo = getOrCreateRepository(reponame);
		for (User user : contributors) {
			Vertex usernode = saveUser(user);
			createEdgeIfNotExist(null, repo, usernode, EDGE_REPOCONTRIBUTOR);
			mapper.put(user.getLogin(), usernode);
		}
		return mapper;
	}
	
	public Vertex getOrCreateRepository(String reponame) {
		Vertex node = null;
		Iterable<Vertex> results = repoidx.get("fullname", reponame);
		for (Vertex v : results) {
			node = v;
			break;
		}
		if (node == null) {
			node = graph.addVertex(null);
			node.setProperty("type", TYPE_REPO);
			node.setProperty("fullname", reponame);
			node.setProperty("created_at", dateFormatter.format(new Date()));
			repoidx.put("fullname", reponame, node);
			typeidx.put("type", TYPE_REPO, node);
		}
		manager.incrCounter();
		return node;
	}
	
	public Vertex getOrCreateOrganization(String login) {
		Vertex node = null;
		Iterable<Vertex> results = orgidx.get("login", login);
		for (Vertex v : results) {
			node = v;
			break;
		}
		if (node == null) {
			node = graph.addVertex(null);
			node.setProperty("type", TYPE_ORGANIZATION);
			node.setProperty("login", login);
			node.setProperty("created_at", dateFormatter.format(new Date()));
			orgidx.put("login", login, node);
			typeidx.put("type", TYPE_ORGANIZATION, node);
			manager.incrCounter();
		}
		return node;
	}
	
	/**
	 * Saves a repository to the graph database.
	 * 
	 * Repositories are keyed according to their full project name, which is a combination
	 * of the username and the project name. This allows differentiation between different
	 * forks of a project.
	 * 
	 * @param repo
	 * @return the newly created database node representing the repository
	 */
	public Vertex saveRepository(Repository repo) {
		String projectFullName = repo.getOwner() + "/" + repo.getName();
		Vertex node = getOrCreateRepository(projectFullName);
		
		node.setProperty("fullname", projectFullName);
		node.setProperty("name", repo.getName());
		node.setProperty("actions", repo.getActions());
		if (repo.getCreatedAt() != null) node.setProperty("createdAt", dateFormatter.format(repo.getCreatedAt()));
		if (repo.getDescription() != null) node.setProperty("description", repo.getDescription());
		node.setProperty("followers", repo.getFollowers());
		node.setProperty("forks", repo.getForks());
		if (repo.getHomepage() != null) node.setProperty("homepage", repo.getHomepage());
		if (repo.getId() != null) node.setProperty("gitHubId", repo.getId()); // note name change
		// getLanguage
		node.setProperty("openIssues", repo.getOpenIssues());
		if (repo.getOrganization() != null) node.setProperty("organization", repo.getOrganization());
		if (repo.getOwner() != null) node.setProperty("owner", repo.getOwner());
		if (repo.getParent() != null) node.setProperty("parent", repo.getParent());
		// getPermission
		if (repo.getPushedAt() != null) node.setProperty("pushedAt", dateFormatter.format(repo.getPushedAt())); 
		node.setProperty("score", repo.getScore());
		node.setProperty("size", repo.getSize());
		if (repo.getSource() != null) node.setProperty("source", repo.getSource());
		if (repo.getType() != null) node.setProperty("repoType", repo.getType()); // note name change
		if (repo.getUrl() != null) node.setProperty("url", repo.getUrl());
		if (repo.getUsername() != null) node.setProperty("username", repo.getUsername());
		// getVisibility
		node.setProperty("watchers", repo.getWatchers());
		node.setProperty("last_updated", dateFormatter.format(new Date())); // save the date of update
		return node;
	}
	
	public Vertex saveOrganization(Organization org) {
		Vertex node = getOrCreateOrganization(org.getLogin());
		if (org.getBillingEmail() != null) node.setProperty("billingEmail", org.getBillingEmail());
		if (org.getBlog() != null) node.setProperty("blog", org.getBlog());
		if (org.getCompany() != null) node.setProperty("company", org.getCompany());
		if (org.getCreatedAt() != null) node.setProperty("createdAt", dateFormatter.format(org.getCreatedAt()));
		if (org.getEmail() != null) node.setProperty("email", org.getEmail());
		node.setProperty("followers", org.getFollowersCount());
		node.setProperty("following", org.getFollowingCount());
		if (org.getGravatarId() != null) node.setProperty("gravatarId", org.getGravatarId());
		if (org.getId() != null) node.setProperty("idNum", org.getId()); // note name change
		if (org.getLocation() != null) node.setProperty("location", org.getLocation());
		if (org.getLogin() != null) node.setProperty("login", org.getLogin());
		if (org.getName() != null) node.setProperty("name", org.getName());
		node.setProperty("ownedPrivateRepoCount", org.getOwnedPrivateRepoCount());
		// getPermission
		node.setProperty("privateGistCount", org.getPrivateGistCount());
		node.setProperty("publicGistCount", org.getPublicGistCount());
		node.setProperty("publicRepoCount", org.getPublicRepoCount());
		node.setProperty("totalPrivateRepoCount", org.getTotalPrivateRepoCount());
		node.setProperty("orgType", org.getType().toString()); // not certain if this is what we really want
		return node;
	}

	protected Map<String, Vertex> saveOrganizationMembersHelper(String organization, List<User> owners, String edgetype) {
		Vertex org = getOrCreateOrganization(organization);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (User owner : owners) {
			Vertex usernode = saveUser(owner);
			createEdgeIfNotExist(null, usernode, org, edgetype);
			mapper.put(owner.getLogin(), usernode);
		}
		return mapper;
	}

	public Map<String, Vertex> saveOrganizationOwners(String organization, List<User> owners) {
		return saveOrganizationMembersHelper(organization, owners, EDGE_ORGANIZATIONOWNER);
	}
	
	public Map<String, Vertex> saveOrganizationPublicMembers(String organization, List<User> members) {
		return saveOrganizationMembersHelper(organization, members, EDGE_ORGANIZATIONMEMBER);
	}
	
	/**
	 * FIXME: this should be integrated somehow with saveUserRepositoriesHelper
	 * 
	 * @param organization
	 * @param repositories
	 * @return
	 */
	public Map<String, Vertex> saveOrganizationPublicRepositories(String organization, List<Repository> repositories) {
		Vertex source = getOrCreateUser(organization);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Repository repo : repositories) {
			String projectFullName = repo.getUsername() + "/" + repo.getName();
			Vertex reponode = saveRepository(repo);
			createEdgeIfNotExist(null, source, reponode, EDGE_REPOOWNER);
			mapper.put(projectFullName, reponode);
		}
		return mapper;
	}
	
}
