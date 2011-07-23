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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Comment;
import com.github.api.v2.schema.Commit;
import com.github.api.v2.schema.Discussion;
import com.github.api.v2.schema.Gist;
import com.github.api.v2.schema.Id;
import com.github.api.v2.schema.Issue;
import com.github.api.v2.schema.Organization;
import com.github.api.v2.schema.PullRequest;
import com.github.api.v2.schema.Repository;
import com.github.api.v2.schema.Team;
import com.github.api.v2.schema.User;
import com.github.api.v2.schema.Discussion.Type;
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

	private enum VertexType {
		COMMIT("COMMIT"),
		USER("USER"),
		REPOSITORY("REPOSITORY"),
		ORGANIZATION("ORGANIZATION"),
		TEAM("TEAM"),
		GIST("GIST"),
		ISSUE("ISSUE"),
		LABEL("LABEL"),
		COMMENT("COMMENT"),
		GISTFILE("GISTFILE"),
		PULLREQUEST("PULLREQUEST"),
		DISCUSSION("DISCUSSION");
		
		private String text;
		VertexType(String text) {
			this.text = text;
		}
		public String toString() {
			return this.text;
		}
	}
	
	private enum EdgeType {
		COMMITAUTHOR("COMMIT_AUTHOR"),
		COMMITPARENT("COMMIT_PARENT"),
		COMMITTER("COMMITTER"),
		DISCUSSIONAUTHOR("DISCUSSION_AUTHOR"),
		DISCUSSIONCOMMIT("DISCUSSION_COMMIT"),
		FOLLOWER("FOLLOWER"),
		FOLLOWING("FOLLOWING"),
		GISTCOMMENT("GIST_COMMENT"),
		GISTCOMMENTOWNER("GIST_COMMENT_OWNER"),
		GISTFILE("GIST_FILE"),
		GISTOWNER("GIST_OWNER"),
		ISSUE("ISSUE"),
		ISSUELABEL("ISSUE_LABEL"),
		ISSUEOWNER("ISSUE_OWNER"),
		ISSUECOMMENT("ISSUE_COMMENT"),
		ISSUECOMMENTOWNER("ISSUE_COMMENT_OWNER"),
		ORGANIZATIONOWNER("ORGANIZATION_OWNER"),
		ORGANIZATIONMEMBER("ORGANIZATION_MEMBER"),
		ORGANIZATIONTEAM("ORGANIZATION_TEAM"),
		PULLREQUEST("PULLREQUEST"),
		PULLREQUESTDISCUSSION("PULLREQUEST_DISCUSSION"),
		PULLREQUESTLABEL("PULLREQUEST_LABEL"),
		PULLREQUESTOWNER("PULLREQUEST_OWNER"),
		PULLREQUESTISSUEUSER("PULLREQUEST_ISSUE_USER"),
		PULLREQUESTISSUECOMMENT("PULLREQUEST_ISSUE_COMMENT"),
		PULLREQUESTCOMMIT("PULLREQUEST_COMMIT"),
		PULLREQUESTREVIEWCOMMENT("PULLREQUEST_REVIEW_COMMENT"),
		PULLREQUESTCOMMENTOWNER("PULLREQUEST_COMMENT_OWNER"),
		REPOWATCHED("REPO_WATCHED"),
		REPOOWNER("REPO_OWNER"),
		REPOCOLLABORATOR("REPO_COLLABORATOR"),
		REPOCONTRIBUTOR("REPO_CONTRIBUTOR"),
		REPOFORK("REPO_FORK"),
		TEAMMEMBER("TEAM_MEMBER");
		
		private String text;
		
		EdgeType(String text) {
			this.text = text;
		}
		
		public String toString() {
			return this.text;
		}
	}

	private static final String INDEX_USER = "user-idx";
	private static final String INDEX_REPO = "repo-idx";
	private static final String INDEX_TYPE = "type-idx";
	private static final String INDEX_ORGANIZATION = "org-idx";
	private static final String INDEX_TEAM = "team-idx";
	private static final String INDEX_GIST = "gist-idx";
	private static final String INDEX_GISTFILE = "gistfile-idx";
	private static final String INDEX_ISSUE = "issue-idx";
	private static final String INDEX_COMMENT = "comment-idx";
	private static final String INDEX_ISSUELABEL = "issuelabel-idx";
	private static final String INDEX_PULLREQUEST = "pullrequest-idx";
	private static final String INDEX_DISCUSSION = "discussion-idx";
	private static final String INDEX_COMMIT = "commit-idx";
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	
	private static final int COMMITMGR_COMMITS = 2000;
	
	private IndexableGraph graph = null;
	private Logger log = null;
	private SimpleDateFormat dateFormatter = null;

	private Index <Vertex> useridx = null;
	private Index <Vertex> repoidx = null;
	private Index <Vertex> typeidx = null;
	private Index <Vertex> orgidx = null;
	private Index <Vertex> teamidx = null;
	private Index <Vertex> gistidx = null;
	private Index <Vertex> gistfileidx = null;
	private Index <Vertex> commentidx = null;
	private Index <Vertex> issueidx = null;
	private Index <Vertex> issuelabelidx = null;
	private Index <Vertex> pullrequestidx = null;
	private Index <Vertex> discussionidx = null;
	private Index <Vertex> commitidx = null;
	
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
		teamidx = (Index <Vertex>)getOrCreateIndex(INDEX_TEAM);
		gistidx = (Index <Vertex>)getOrCreateIndex(INDEX_GIST);
		commentidx = (Index <Vertex>)getOrCreateIndex(INDEX_COMMENT);
		gistfileidx = (Index <Vertex>)getOrCreateIndex(INDEX_GISTFILE);
		issueidx = (Index <Vertex>)getOrCreateIndex(INDEX_ISSUE);
		issuelabelidx = (Index <Vertex>)getOrCreateIndex(INDEX_ISSUELABEL);
		pullrequestidx = (Index <Vertex>)getOrCreateIndex(INDEX_PULLREQUEST);
		discussionidx = (Index <Vertex>)getOrCreateIndex(INDEX_DISCUSSION);
		commitidx = (Index <Vertex>)getOrCreateIndex(INDEX_COMMIT);
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
		log.debug(user.toString());

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
	
	private Edge createEdgeIfNotExist(Object id, Vertex outVertex, Vertex inVertex, EdgeType edgetype) {
		for (Edge e : outVertex.getOutEdges(edgetype.toString())) {
			if (e.getInVertex().equals(inVertex)) return e;
		}
		Edge re = graph.addEdge(id,  outVertex, inVertex, edgetype.toString());
		re.setProperty("created_at", dateFormatter.format(new Date()));
		manager.incrCounter();
		return re;
	}
	
	public Map<String,Vertex> saveUserFollowersFollowing(String sourceuser, List<String> users, EdgeType edgetype) {
		Vertex source = getOrCreateUser(sourceuser);
		HashMap<String,Vertex> mapper= new HashMap<String,Vertex>();
		for (String user : users) {
			Vertex node = getOrCreateUser(user);
			createEdgeIfNotExist(null, source, node, edgetype);
			mapper.put(user, node);
		}
		return mapper;
	}
	
	public Map<String,Vertex> saveUserFollowers(String sourceuser, List<String> users) {
		return saveUserFollowersFollowing(sourceuser, users, EdgeType.FOLLOWER);
	}

	public Map<String,Vertex> saveUserFollowing(String sourceuser, List<String> users) {
		return saveUserFollowersFollowing(sourceuser, users, EdgeType.FOLLOWING);
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
			createEdgeIfNotExist(null, node, proj, EdgeType.REPOWATCHED);
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
			createEdgeIfNotExist(null, proj, repoVertex, EdgeType.REPOFORK);
			mapper.put(projectFullName, repoVertex);
		}
		return mapper;
	}
	
	private Map<String, Vertex> saveUserRepositoriesHelper(String user, List<Repository> repositories, EdgeType edgetype) {
		Vertex source = getOrCreateUser(user);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Repository repo : repositories) {
			String projectFullName = repo.getUsername() + "/" + repo.getName();
			Vertex reponode = saveRepository(repo);
			createEdgeIfNotExist(null, source, reponode, edgetype);
			mapper.put(projectFullName, reponode);
		}
		return mapper;
	}
	
	public Map<String, Vertex> saveRepositoryIssues(String project, Collection<Issue> issues) {
		Vertex proj = getOrCreateRepository(project);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Issue issue : issues) {
			String issueId = project + ":" + issue.getNumber();
			Vertex issuenode = saveIssue(project, issue);
			createEdgeIfNotExist(null, proj, issuenode, EdgeType.ISSUE);
			mapper.put(issueId, issuenode);
		}
		return mapper;
	}
	
	
	public Map<String, Vertex> saveUserWatchedRepositories(String user, List<Repository> repos) {
		return saveUserRepositoriesHelper(user, repos, EdgeType.REPOWATCHED);
	}
	
	public Map<String, Vertex> saveUserRepositories(String user, List<Repository> repos) {
		return saveUserRepositoriesHelper(user, repos, EdgeType.REPOOWNER);
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
	public Set<String> getVertexHelper(double age, String idxname, VertexType vtxtype, String fieldname) {
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
		return getVertexHelper(age, INDEX_USER, VertexType.USER, "username");
	}
	
	/**
	 * Gets all the repositories that have not been updated in a number of days
	 * 
	 * @param age
	 * @return 
	 */
	public Set<String> getRepos(double age) {
		return getVertexHelper(age, INDEX_REPO, VertexType.REPOSITORY, "fullname");
	}

	public Map<String,Vertex> saveRepositoryCollaborators(String reponame, List<String> collabs) {
		HashMap<String, Vertex> mapper = new HashMap<String, Vertex>();
		Vertex repo = getOrCreateRepository(reponame);
		for (String username : collabs) {
			Vertex user = getOrCreateUser(username);
			createEdgeIfNotExist(null, repo, user, EdgeType.REPOCOLLABORATOR);
			mapper.put(username, user);
		}
		return mapper;
	}
	
	public Map<String,Vertex> saveRepositoryContributors(String reponame, List<User> contributors) {
		HashMap<String, Vertex> mapper = new HashMap<String, Vertex>();
		Vertex repo = getOrCreateRepository(reponame);
		for (User user : contributors) {
			Vertex usernode = saveUser(user);
			createEdgeIfNotExist(null, repo, usernode, EdgeType.REPOCONTRIBUTOR);
			mapper.put(user.getLogin(), usernode);
		}
		return mapper;
	}
		
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
	
	public Vertex getOrCreateUser(String login) {
		return getOrCreateVertexHelper("login", login, VertexType.USER, useridx);
	}
	
	public Vertex getOrCreateRepository(String reponame) {
		return getOrCreateVertexHelper("reponame", reponame, VertexType.REPOSITORY, repoidx);
	}
	
	public Vertex getOrCreateOrganization(String login) {
		return getOrCreateVertexHelper("login", login, VertexType.ORGANIZATION, orgidx);
	}

	public Vertex getOrCreateTeam(String teamId) {
		return getOrCreateVertexHelper("team_id", teamId, VertexType.TEAM, teamidx);
	}
	
	public Vertex getOrCreateGist(String repoId) {
		return getOrCreateVertexHelper("gist_id", repoId, VertexType.GIST, gistidx);
	}
	
	public Vertex getOrCreateComment(long commentId) {
		return getOrCreateVertexHelper("comment_id", commentId, VertexType.COMMENT, commentidx);
	}
	
	public Vertex getOrCreateGistFile(String repoid, String filename) {
		String gistFileId = repoid + "/" + filename;
		return getOrCreateVertexHelper("gistfile_id",  gistFileId, VertexType.GISTFILE, gistfileidx);
	}
	
	public Vertex getOrCreateIssue(String repoid, Issue issue) {
		String issueId = repoid + ":" + issue.getNumber();
		return getOrCreateVertexHelper("issue_id", issueId, VertexType.ISSUE, issueidx);
	}
	
	public Vertex getOrCreateIssueLabel(String label) {
		return getOrCreateVertexHelper("label", label, VertexType.LABEL, issuelabelidx);
	}
	
	public Vertex getOrCreatePullRequest(String idval) {
		return getOrCreateVertexHelper("pullrequest_id", idval, VertexType.PULLREQUEST, pullrequestidx);
	}
	
	public Vertex getOrCreateDiscussion(String discussionId) {
		return getOrCreateVertexHelper("discussion_id", discussionId, VertexType.DISCUSSION, discussionidx);
	}
	
	public Vertex getOrCreateCommit(String commitId) {
		log.debug("Fetching or creating commit: {}", commitId);
		return getOrCreateVertexHelper("commit_id", commitId, VertexType.COMMIT, commitidx);
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

	public Vertex saveTeam(Team team) {
		Vertex node = getOrCreateTeam(team.getId());
		if (team.getName() != null) node.setProperty("name", team.getName());
		// getPermission
		// getRepoNames
		return node;
	}

	protected Vertex saveCommentHelper(Comment comment, EdgeType edgetype) {
		Vertex node = getOrCreateComment(comment.getId());
		if (comment.getBody() != null) node.setProperty("comment", comment.getBody());
		if (comment.getCreatedAt() != null) node.setProperty("createdAt", dateFormatter.format(comment.getCreatedAt()));
		// FIXME: perhaps gravatarId should be another node?
		if (comment.getGravatarId() != null) node.setProperty("gravatarId", comment.getGravatarId());
		if (comment.getUpdatedAt() != null) node.setProperty("updatedAt", dateFormatter.format(comment.getUpdatedAt()));
		if (comment.getUser() != null) {
			Vertex user = getOrCreateUser(comment.getUser());
			createEdgeIfNotExist(null, user, node, edgetype);
		}
		return node;
	}

	public Vertex saveGistComment(Comment comment) {
		return saveCommentHelper(comment, EdgeType.GISTCOMMENTOWNER);
	}
	
	public Vertex saveIssueComment(Comment comment) {
		return saveCommentHelper(comment, EdgeType.ISSUECOMMENTOWNER);
	}
	
	public Vertex savePullRequestComment(Comment comment) {
		return saveCommentHelper(comment, EdgeType.PULLREQUESTCOMMENTOWNER);
	}
	
	public Vertex saveGistFile(String repoid, String filename) {
		Vertex node = getOrCreateGistFile(repoid, filename);
		return node;
	}
	
	public Vertex saveGist(Gist gist) {
		Vertex node = getOrCreateGist(gist.getRepo());
		for (Comment comment : gist.getComments()) {
			Vertex commentnode = saveGistComment(comment);
			createEdgeIfNotExist(null, node, commentnode, EdgeType.GISTCOMMENT);
		}
		if (gist.getCreatedAt() != null) node.setProperty("createdAt", dateFormatter.format(gist.getCreatedAt()));
		if (gist.getDescription() != null) node.setProperty("description", gist.getDescription());
		for (String file : gist.getFiles()) {
			Vertex filenode = saveGistFile(gist.getRepo(), file);
			createEdgeIfNotExist(null, node, filenode, EdgeType.GISTFILE);
		}
		if (gist.getOwner() != null) node.setProperty("owner", gist.getOwner());
		if (gist.getRepo() != null) node.setProperty("repo", gist.getRepo());
		return node;
	}
	
	protected Map<String, Vertex> saveOrganizationMembersHelper(String organization, List<User> owners, EdgeType edgetype) {
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
		return saveOrganizationMembersHelper(organization, owners, EdgeType.ORGANIZATIONOWNER);
	}
	
	public Map<String, Vertex> saveOrganizationPublicMembers(String organization, List<User> members) {
		return saveOrganizationMembersHelper(organization, members, EdgeType.ORGANIZATIONMEMBER);
	}
	
	/**
	 * FIXME: this should be integrated somehow with saveUserRepositoriesHelper
	 * 
	 * @param organization
	 * @param repositories
	 * @return
	 */
	public Map<String, Vertex> saveOrganizationPublicRepositories(String organization, List<Repository> repositories) {
		Vertex source = getOrCreateOrganization(organization);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Repository repo : repositories) {
			String projectFullName = repo.getUsername() + "/" + repo.getName();
			Vertex reponode = saveRepository(repo);
			createEdgeIfNotExist(null, source, reponode, EdgeType.REPOOWNER);
			mapper.put(projectFullName, reponode);
		}
		return mapper;
	}
	
	public Map<String, Vertex> saveOrganizationTeams(String organization, List<Team> teams) {
		Vertex org = getOrCreateOrganization(organization);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Team team: teams) {
			Vertex teamnode = saveTeam(team);
			createEdgeIfNotExist(null, org, teamnode, EdgeType.ORGANIZATIONTEAM);
			mapper.put(team.getId(), teamnode);
		}
		return mapper;
	}
	
	public Map<String,Vertex> saveTeamMembers(String team, List<User> users) {
		Vertex teamnode = getOrCreateTeam(team);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (User user : users) {
			Vertex usernode = saveUser(user);
			createEdgeIfNotExist(null, usernode, teamnode, EdgeType.TEAMMEMBER);
			mapper.put(user.getLogin(), usernode);
		}
		return mapper;
	}
	
	public Map<String,Vertex> saveTeamRepositories(String team, List<Repository> repos) {
		Vertex teamnode = getOrCreateTeam(team);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Repository repo : repos) {
			String projectFullName = repo.getUsername() + "/" + repo.getName();
			Vertex reponode = saveRepository(repo);
			createEdgeIfNotExist(null, teamnode, reponode, EdgeType.REPOOWNER);
			mapper.put(projectFullName, reponode);
		}
		return mapper;
	}
	
	public Map<String,Vertex> saveUserGists(String user, List<Gist> gists) {
		Vertex usernode = getOrCreateUser(user);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Gist gist : gists) {
			Vertex gistnode = saveGist(gist);
			createEdgeIfNotExist(null, usernode, gistnode, EdgeType.GISTOWNER);
			mapper.put(gist.getRepo(),gistnode);
		}
		return mapper;
	}
	
	public Vertex saveIssue(String project, Issue issue) {
		Vertex issuenode = getOrCreateIssue(project, issue);
		if (issue.getBody() != null) issuenode.setProperty("body", issue.getBody());
		if (issue.getClosedAt() != null) issuenode.setProperty("closedAt", dateFormatter.format(issue.getClosedAt()));
		issuenode.setProperty("comments", issue.getComments());
		if (issue.getCreatedAt() != null) issuenode.setProperty("createdAt", dateFormatter.format(issue.getCreatedAt()));
		if (issue.getGravatarId() != null) issuenode.setProperty("gravatarId", issue.getGravatarId());
		// issue.getLabels()
		for (String label : issue.getLabels()) {
			Vertex labelnode = getOrCreateIssueLabel(label);
			createEdgeIfNotExist(null, issuenode, labelnode, EdgeType.ISSUELABEL);
		}
		issuenode.setProperty("number", issue.getNumber());
		issuenode.setProperty("position", issue.getPosition());
		if (issue.getState() != null) issuenode.setProperty("state", issue.getState().toString());
		if (issue.getTitle() != null) issuenode.setProperty("title", issue.getTitle());
		if (issue.getUpdatedAt() != null) issuenode.setProperty("updatedAt", dateFormatter.format(issue.getUpdatedAt()));
		if (issue.getUser() != null) {
			issuenode.setProperty("user", issue.getUser());
			Vertex userNode = getOrCreateUser(issue.getUser());
			createEdgeIfNotExist(null, userNode, issuenode, EdgeType.ISSUEOWNER);
		}
		issuenode.setProperty("votes", issue.getVotes());
		return issuenode;
	}
	
	public Map<Long,Vertex> saveRepositoryIssueComments(String project, Issue issue, Collection<Comment> comments) {
		Vertex issuenode = getOrCreateIssue(project, issue);
		HashMap<Long,Vertex> mapper = new HashMap<Long,Vertex>();
		for (Comment comment : comments) {
			Vertex commentnode = saveIssueComment(comment);
			createEdgeIfNotExist(null, issuenode, commentnode, EdgeType.ISSUECOMMENT);
			mapper.put(new Long(comment.getId()), commentnode);
		}
		return mapper;
	}
	
	public void saveRepositoryPullRequests(String project, Collection<PullRequest> requests) {
		Vertex projectnode = getOrCreateRepository(project);
		for (PullRequest request : requests) {
			saveRepositoryPullRequest(project, request);
		}
	}
	
	public void setProperty(Element elem, String propname, String property) {
		if (property != null) elem.setProperty(propname, property);
		log.debug("{} = {}", propname, property);
	}
	public void setProperty(Element elem, String propname, Date propdate) {
		if (propdate != null) {
			elem.setProperty(propname, dateFormatter.format(propdate));
			log.debug("{} = {}", propname, dateFormatter.format(propdate));
		} else {
			log.debug("{} = null", propname);
		}
	}
	public void setProperty(Element elem, String propname, int propvalue) {
		elem.setProperty(propname, propvalue);
		log.debug("{} = {}", propname, propvalue);
	}
	public void setProperty(Element elem, String propname, long propvalue) {
		elem.setProperty(propname, propvalue);
		log.debug("{} = {}", propname, propvalue);
	}
	
	public Vertex saveCommit(Commit commit) {
		// FIXME: this should probably have some debugging to see what is in
		// the fields that we're not currently processing
		Vertex node = getOrCreateCommit(commit.getId());
		// commit.getAdded()
		if (commit.getAuthor() != null) {
			Vertex author = saveUser(commit.getAuthor());
			createEdgeIfNotExist(null, author, node, EdgeType.COMMITAUTHOR);
		}
		setProperty(node, "authoredDate", commit.getAuthoredDate());
		setProperty(node, "committedDate", commit.getCommittedDate());
		if (commit.getCommitter() != null) {
			Vertex committer = saveUser(commit.getCommitter());
			createEdgeIfNotExist(null, committer, node, EdgeType.COMMITTER);
		}
		setProperty(node, "date", commit.getDate());
		setProperty(node, "gravatar", commit.getGravatar());
		setProperty(node, "commit_id", commit.getId());
		setProperty(node, "login", commit.getLogin());
		setProperty(node, "message", commit.getMessage());
		// modified
		for (Id id : commit.getParents()) {
			Vertex parent = getOrCreateCommit(id.getId());
			createEdgeIfNotExist(null, node, parent, EdgeType.COMMITPARENT);
		}
		// removed
		setProperty(node, "space", commit.getSpace());
		setProperty(node, "time", commit.getTime());
		setProperty(node, "tree", commit.getTree());
		setProperty(node, "url", commit.getUrl());

		return node;
	}
	
	public Vertex savePullRequestReviewComment(PullRequestReviewComment comment) {
		// Vertex node = getOrCreatePullRequestReviewComment()
		log.warn("Attempt to save PullRequestReviewComment but I don't know how to create one!");
		return null;
	}
	
	public Vertex createCommitFromDiscussion(Discussion disc) {
		log.debug("Building commit from Discussion: {}", disc);
		Commit commit = new Commit();
		commit.setCommittedDate(disc.getCommittedDate());
		log.debug("commited date: {}", disc.getCommittedDate());
		commit.setAuthoredDate(disc.getAuthoredDate());
		log.debug("authored date: {}", disc.getAuthoredDate());
		commit.setId(disc.getId());
		log.debug("commit id: {}", disc.getId());
		commit.setAuthor(disc.getAuthor());
		log.debug("author: {}", disc.getAuthor());
		commit.setCommitter(disc.getCommitter());
		log.debug("committer: {}", disc.getCommitter());
		commit.setMessage(disc.getBody()); // FIXME: check to make sure this correct
		log.debug("body: {}", disc.getSubject());
		// commit.setUser(disc.getUser());
		commit.setTree(disc.getTree());
		log.debug("tree: {}", disc.getTree());
		commit.setParents(disc.getParents());
		log.debug("parents: {}", disc.getParents());
		Vertex node = saveCommit(commit);
		return node;
	}
	
	public Vertex createCommentFromDiscussion(Discussion disc) {
		Comment comment = new Comment();
		comment.setBody(disc.getBody());
		comment.setCreatedAt(disc.getCreatedAt());
		comment.setGravatarId(disc.getGravatarId());
		comment.setId(Long.parseLong(disc.getId()));
		comment.setUpdatedAt(disc.getUpdatedAt());		
		comment.setUser(disc.getUser().getLogin());
		Vertex node = savePullRequestComment(comment);
		return node;
	}

	public Vertex createPullRequestReviewCommentFromDiscussion(Discussion disc) {
		PullRequestReviewComment comment = new PullRequestReviewComment();
		comment.setDiffHunk(disc.getDiffHunk());
		comment.setBody(disc.getBody());
		comment.setPath(disc.getPath());
		comment.setPosition(disc.getPosition());
		comment.setCommitId(disc.getCommitId());
		comment.setOriginalCommitId(disc.getOriginalCommitId());
		comment.setUser(disc.getUser());
		comment.setCreatedAt(disc.getCreatedAt());
		comment.setUpdatedAt(disc.getUpdatedAt());
		Vertex node = savePullRequestReviewComment(comment);
		return node;
	}
	
	public Vertex saveDiscussion(Discussion discussion) {
		Vertex node = getOrCreateDiscussion(discussion.getId());
		node.setProperty("discussion_type", discussion.getType().toString());
		if (discussion.getAuthor() != null) {
			Vertex author = saveUser(discussion.getAuthor());
			createEdgeIfNotExist(null, author, node, EdgeType.DISCUSSIONAUTHOR);
		}
		if (discussion.getAuthoredDate() != null) node.setProperty("authoredDate", dateFormatter.format(discussion.getAuthoredDate()));
		if (discussion.getBody() != null) node.setProperty("body", discussion.getBody());
		if (discussion.getCommitId() != null) {
			node.setProperty("commitId", discussion.getCommitId());
			Vertex commit = getOrCreateCommit(discussion.getCommitId());
			createEdgeIfNotExist(null, node, commit, EdgeType.DISCUSSIONCOMMIT);
		}

		log.debug("Discussion type: {}", discussion.getType().toString());
		if (discussion.getType().equals(Discussion.Type.COMMIT)) {
			Vertex commitnode = createCommitFromDiscussion(discussion);
			if (commitnode != null)
				createEdgeIfNotExist(null, node, commitnode, EdgeType.PULLREQUESTCOMMIT);
		} else if (discussion.getType().equals(Discussion.Type.ISSUE_COMMENT)) {
			Vertex issuenode = createCommentFromDiscussion(discussion);
			if (issuenode != null)
				createEdgeIfNotExist(null, node, issuenode, EdgeType.PULLREQUESTISSUECOMMENT);
		} else if (discussion.getType().equals(Discussion.Type.PULL_REQUEST_REVIEW_COMMENT)) {
			Vertex reviewnode = createPullRequestReviewCommentFromDiscussion(discussion);
			if (reviewnode != null)
				createEdgeIfNotExist(null, node, reviewnode, EdgeType.PULLREQUESTREVIEWCOMMENT);
		}
		return node;
	}
	
	public Vertex saveRepositoryPullRequest(String reponame, PullRequest request) {
		log.info("Saving pull request {}", request.getNumber());
		log.info(request.toString());
		Vertex reponode = getOrCreateRepository(reponame);
		Vertex pullnode = getOrCreatePullRequest(reponame + ":" + request.getNumber());
		// getBase()
		if (request.getBody() != null) pullnode.setProperty("body", request.getBody());
		pullnode.setProperty("comments", request.getComments());
		if (request.getCreatedAt() != null) pullnode.setProperty("createdAt", dateFormatter.format(request.getCreatedAt()));
		if (request.getDiffUrl() != null) pullnode.setProperty("diffUrl", request.getDiffUrl());
		log.info("Getting discussion");
		for (Discussion discussion : request.getDiscussion()) {
			Vertex discussionnode = saveDiscussion(discussion);
			log.info("Created discussion node");
			createEdgeIfNotExist(null, pullnode, discussionnode, EdgeType.PULLREQUESTDISCUSSION);
		}
		setProperty(pullnode, "gravatarId", request.getGravatarId());
		// request.getHead()
		setProperty(pullnode, "htmlUrl", request.getHtmlUrl());
		setProperty(pullnode, "issueCreatedAt", request.getIssueCreatedAt());
		setProperty(pullnode, "issueUpdatedAt", request.getIssueUpdatedAt());
		if (request.getIssueUser() != null) {
			Vertex usernode = saveUser(request.getIssueUser());
			createEdgeIfNotExist(null, usernode, pullnode, EdgeType.PULLREQUESTISSUEUSER);
		}
		for (String label : request.getLabels()) {
			Vertex labelnode = getOrCreateIssueLabel(label);
			createEdgeIfNotExist(null, pullnode, labelnode, EdgeType.PULLREQUESTLABEL);
		}
		pullnode.setProperty("number", request.getNumber());
		if (request.getPatchUrl() != null) pullnode.setProperty("patchUrl", request.getPatchUrl());
		pullnode.setProperty("position", request.getPosition());
		if (request.getState() != null) pullnode.setProperty("state", request.getState().toString());
		if (request.getTitle() != null) pullnode.setProperty("title", request.getTitle());
		if (request.getUser() != null) {
			Vertex usernode = saveUser(request.getUser());
			createEdgeIfNotExist(null, usernode, pullnode, EdgeType.PULLREQUESTOWNER);
		}
		pullnode.setProperty("votes", request.getVotes());
		createEdgeIfNotExist(null, reponode, pullnode, EdgeType.PULLREQUEST);
		return pullnode;
	}
}
