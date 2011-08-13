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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import com.ibm.research.govsci.graph.BlueprintsBase;
import com.ibm.research.govsci.graph.Shutdownable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * 
 * @author Patrick Wagstrom (http://patrick.wagstrom.net/)
 *
 */
public class BlueprintsDriver extends BlueprintsBase implements Shutdownable {

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
	private static final String INDEX_PULLREQUESTREVIEWCOMMENT = "pullrequestreviewcomment-idx";
	private static final String INDEX_DISCUSSION = "discussion-idx";
	private static final String INDEX_COMMIT = "commit-idx";
		
	private Logger log = null;

	private Index <Vertex> useridx = null;
	private Index <Vertex> repoidx = null;
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
	private Index <Vertex> pullrequestreviewcommentidx = null;
	
	/**
	 * Base constructor for BlueprintsDriver
	 * 
	 * @param dbengine The name of the engine to use, e.g. neo4j, orientdb, etc
	 * @param dburl The url of the database to use
	 */
	public BlueprintsDriver(String dbengine, String dburl) {
		super(dbengine, dburl);
		log = LoggerFactory.getLogger(this.getClass());

		useridx = getOrCreateIndex(INDEX_USER);
		repoidx = getOrCreateIndex(INDEX_REPO);
		typeidx = getOrCreateIndex(INDEX_TYPE);
		orgidx = getOrCreateIndex(INDEX_ORGANIZATION);
		teamidx = getOrCreateIndex(INDEX_TEAM);
		gistidx = getOrCreateIndex(INDEX_GIST);
		commentidx = getOrCreateIndex(INDEX_COMMENT);
		gistfileidx = getOrCreateIndex(INDEX_GISTFILE);
		issueidx = getOrCreateIndex(INDEX_ISSUE);
		issuelabelidx = getOrCreateIndex(INDEX_ISSUELABEL);
		pullrequestidx = getOrCreateIndex(INDEX_PULLREQUEST);
		discussionidx = getOrCreateIndex(INDEX_DISCUSSION);
		commitidx = getOrCreateIndex(INDEX_COMMIT);
		pullrequestreviewcommentidx = getOrCreateIndex(INDEX_PULLREQUESTREVIEWCOMMENT);
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

		setProperty(node, "blog", user.getBlog());
		setProperty(node, "collaborators", user.getCollaborators());
		setProperty(node, "company", user.getCompany());
		setProperty(node, "createdAt", user.getCreatedAt());
		setProperty(node, "diskUsage", user.getDiskUsage());
		setProperty(node, "email", user.getEmail());
		setProperty(node, "followersCount", user.getFollowersCount());
		setProperty(node, "followingCount", user.getFollowingCount());
		setProperty(node, "fullname", user.getFullname());
		setProperty(node, "gravatarId", user.getGravatarId());
		setProperty(node, "gitHubId", user.getId()); // note name change
		setProperty(node, "location", user.getLocation());
		setProperty(node, "login", user.getLogin());
		setProperty(node, "name", user.getName());
		setProperty(node, "ownedPrivateRepoCount", user.getOwnedPrivateRepoCount());
		// getPermission
		// getPlan
		setProperty(node, "privateGistCount", user.getPrivateGistCount());
		setProperty(node, "publicGistCount", user.getPublicGistCount());
		setProperty(node, "publicRepoCount", user.getPublicRepoCount());
		setProperty(node, "score", user.getScore());
		setProperty(node, "totalPrivateRepoCount", user.getTotalPrivateRepoCount());
		setProperty(node, "username", user.getUsername());
		setProperty(node, "last_updated", new Date()); // save the date of update
		return node;
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
	
	/**
	 * Saves a list of issues to the project database and connects those issues to the
	 * project node.
	 * 
	 * @param project
	 * @param issues
	 * @return
	 */
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
	 * FIXME: is this ever used?
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
	
	public Vertex getOrCreatePullRequestReviewComment(String commentId) {
		log.debug("Fetching or creating PullRequestReviewComment: {}", commentId);
		return getOrCreateVertexHelper("comment_id", commentId, VertexType.PULLREQUESTREVIEWCOMMENT, pullrequestreviewcommentidx);
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
		setProperty(node, "watchers", repo.getWatchers());
		setProperty(node, "last_updated", new Date());
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
		setProperty(node, "body", comment.getBody());
		setProperty(node, "createdAt", dateFormatter.format(comment.getCreatedAt()));
		// FIXME: perhaps gravatarId should be another node?
		setProperty(node, "gravatarId", comment.getGravatarId());
		setProperty(node, "updatedAt", dateFormatter.format(comment.getUpdatedAt()));
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
	
	public Vertex savePullRequestReviewComent(Comment comment) {
		return saveCommentHelper(comment, EdgeType.PULLREQUESTREVIEWCOMMENT);
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
	
	/**
	 * Save an issue to the database
	 * 
	 * If the issue already exists in the database then it will update the issue
	 * 
	 * @param project name of the project
	 * @param issue the GitHub issue object to save
	 * @return the newly created vertex
	 */
	public Vertex saveIssue(String project, Issue issue) {
		Vertex issuenode = getOrCreateIssue(project, issue);
		if (issue.getBody() != null) setProperty(issuenode, "body", issue.getBody());
		if (issue.getClosedAt() != null) setProperty(issuenode, "closedAt", dateFormatter.format(issue.getClosedAt()));
		setProperty(issuenode, "comments", issue.getComments());
		if (issue.getCreatedAt() != null) setProperty(issuenode, "createdAt", dateFormatter.format(issue.getCreatedAt()));
		if (issue.getGravatarId() != null) setProperty(issuenode, "gravatarId", issue.getGravatarId());
		for (String label : issue.getLabels()) {
			Vertex labelnode = getOrCreateIssueLabel(label);
			createEdgeIfNotExist(null, issuenode, labelnode, EdgeType.ISSUELABEL);
		}
		setProperty(issuenode, "number", issue.getNumber());
		setProperty(issuenode, "position", issue.getPosition());
		if (issue.getState() != null) setProperty(issuenode, "state", issue.getState().toString());
		if (issue.getTitle() != null) setProperty(issuenode, "title", issue.getTitle());
		if (issue.getUpdatedAt() != null) setProperty(issuenode, "updatedAt", issue.getUpdatedAt());
		if (issue.getUser() != null) {
			setProperty(issuenode, "user", issue.getUser());
			Vertex userNode = getOrCreateUser(issue.getUser());
			createEdgeIfNotExist(null, userNode, issuenode, EdgeType.ISSUEOWNER);
		}
		setProperty(issuenode, "votes", issue.getVotes());
		setProperty(issuenode, "updated_at", new Date());
		return issuenode;
	}
	
	/**
	 * Saves the comments that match up with a given issue
	 * 
	 * Also, of note this method sets the property comments_added_at to indicate when
	 * the comments were added to the comment. This can be used to avoid pulling comments
	 * multiple times.
	 * 
	 * @param project
	 * @param issue
	 * @param comments
	 * @return
	 */
	public Map<Long,Vertex> saveRepositoryIssueComments(String project, Issue issue, Collection<Comment> comments) {
		Vertex issuenode = getOrCreateIssue(project, issue);
		HashMap<Long,Vertex> mapper = new HashMap<Long,Vertex>();
		for (Comment comment : comments) {
			Vertex commentnode = saveIssueComment(comment);
			createEdgeIfNotExist(null, issuenode, commentnode, EdgeType.ISSUECOMMENT);
			mapper.put(new Long(comment.getId()), commentnode);
			
		}
		setProperty(issuenode, "sys:comments_added", new Date());
		return mapper;
	}
	
	// FIXME: this code does not look like it functions properly
	public void saveRepositoryPullRequests(String project, Collection<PullRequest> requests) {
		Vertex projectnode = getOrCreateRepository(project);
		for (PullRequest request : requests) {
			saveRepositoryPullRequest(project, request);
		}
	}
	
	public Vertex saveCommit(Commit commit) {
		// FIXME: this should probably have some debugging to see what is in
		// the fields that we're not currently processing
		log.trace("saveCommit: enter");
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
		log.trace("saveCommit: exit");
		return node;
	}
	
	public Vertex savePullRequestReviewComment(PullRequestReviewComment comment) {
		log.trace("savePullRequestReviewComment: enter");
		String commentId = comment.getCommitId() + ":" + dateFormatter.format(comment.getCreatedAt());
		Vertex node = getOrCreatePullRequestReviewComment(commentId);
		setProperty(node, "body", comment.getBody());
		setProperty(node, "commitId", comment.getCommitId());
		setProperty(node, "createdAt", comment.getCreatedAt());
		setProperty(node, "diffHunk", comment.getDiffHunk());
		setProperty(node, "originalCommitId", comment.getOriginalCommitId());
		setProperty(node, "path", comment.getPath());
		setProperty(node, "position", comment.getPosition());
		setProperty(node, "updatedAt", comment.getUpdatedAt());
		if (comment.getUser() != null) {
			Vertex user = saveUser(comment.getUser());
			createEdgeIfNotExist(user, node, EdgeType.PULLREQUESTREVIEWCOMMENTOWNER);
		}
		if (comment.getCommitId() != null) {
			Vertex commit = getOrCreateCommit(comment.getCommitId());
			createEdgeIfNotExist(node, commit, EdgeType.PULLREQUESTREVIEWCOMMENTCOMMIT);
		}
		if (comment.getOriginalCommitId() != null) {
			Vertex commit = getOrCreateCommit(comment.getOriginalCommitId());
			createEdgeIfNotExist(node, commit, EdgeType.PULLREQUESTREVIEWCOMMENTORIGINALCOMMIT);		
		}
		log.trace("savePullRequestReviewComment: exit");
		return node;
	}
	
	public Vertex createCommitFromDiscussion(Discussion disc) {
		log.trace("createCommitFromDiscusion: enter");
		Commit commit = new Commit();
		commit.setCommittedDate(disc.getCommittedDate());
		commit.setAuthoredDate(disc.getAuthoredDate());
		commit.setId(disc.getId());
		commit.setAuthor(disc.getAuthor());
		commit.setCommitter(disc.getCommitter());
		commit.setMessage(disc.getMessage());
		commit.setTree(disc.getTree());
		commit.setParents(disc.getParents());
		Vertex node = saveCommit(commit);
		log.trace("createCommitFromDiscussion: exit");
		return node;
	}
	
	public Vertex createCommentFromDiscussion(Discussion disc) {
		log.trace("createCommentFromDiscussion: enter");
		Comment comment = new Comment();
		comment.setBody(disc.getBody());
		comment.setCreatedAt(disc.getCreatedAt());
		comment.setGravatarId(disc.getGravatarId());
		try {
			comment.setId(Long.parseLong(disc.getId()));
		} catch (NumberFormatException e) {
			log.debug("Discussion has a null id: {}", disc);
			// comment.setId(null);
		}
		comment.setUpdatedAt(disc.getUpdatedAt());
		comment.setUser(disc.getUser().getLogin());
		Vertex node = savePullRequestComment(comment);
		log.trace("createCommentFromDiscussion: exit");
		return node;
	}

	public Vertex createPullRequestReviewCommentFromDiscussion(Discussion disc) {
		log.trace("createPullRequestReviewCommentFromDiscussion: enter");
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
		log.trace("createPullRequestReviewCommentFromDiscussion: exit");
		return node;
	}
	
	public Vertex saveDiscussion(Discussion discussion) {
		log.trace("saveDiscussion: enter");
		Vertex node = null;
		if (discussion.getType() == null && discussion.getBody() != null) {
			log.warn("Discussion type was null now ISSUE_COMMENT: {}", discussion.toString());
			discussion.setType(Discussion.Type.ISSUE_COMMENT);
		}
		log.trace("Discussion type: {}", discussion.getType().toString());
		if (discussion.getType().equals(Discussion.Type.COMMIT)) {
			node = createCommitFromDiscussion(discussion);
		} else if (discussion.getType().equals(Discussion.Type.ISSUE_COMMENT)) {
			node = createCommentFromDiscussion(discussion);
		} else if (discussion.getType().equals(Discussion.Type.PULL_REQUEST_REVIEW_COMMENT)) {
			node = createPullRequestReviewCommentFromDiscussion(discussion);
		} else {
			log.error("Undefined discussion type : {}", discussion.getType().toString());
		}
		if (node != null && discussion.getUser() != null) {
			Vertex user = saveUser(discussion.getUser());
			createEdgeIfNotExist(null, user, node, EdgeType.DISCUSSIONUSER);
		}
		log.trace("saveDiscussion: exit");
		return node;
	}
	
	public Vertex saveRepositoryPullRequest(String reponame, PullRequest request) {
		log.trace("saveRepositoryPullRequest: enter");
		log.trace("Saving pull request {}", request.getNumber());
		log.trace(request.toString());
		Vertex reponode = getOrCreateRepository(reponame);
		Vertex pullnode = getOrCreatePullRequest(reponame + ":" + request.getNumber());
		// getBase()
		setProperty(pullnode, "body", request.getBody());
		setProperty(pullnode, "comments", request.getComments());
		setProperty(pullnode, "createdAt", request.getCreatedAt());
		setProperty(pullnode, "diffUrl", request.getDiffUrl());

		for (Discussion discussion : request.getDiscussion()) {
			Vertex discussionnode = saveDiscussion(discussion);
			log.trace("Created discussion node");
			createEdgeIfNotExist(null, pullnode, discussionnode, EdgeType.PULLREQUESTDISCUSSION);
			setProperty(pullnode, "sys:discussions_added", new Date());
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
		setProperty(pullnode, "number", request.getNumber());
		setProperty(pullnode, "patchUrl", request.getPatchUrl());
		setProperty(pullnode, "position", request.getPosition());
		setProperty(pullnode, "state", request.getState().toString());
		setProperty(pullnode, "title", request.getTitle());
		if (request.getUser() != null) {
			Vertex usernode = saveUser(request.getUser());
			createEdgeIfNotExist(null, usernode, pullnode, EdgeType.PULLREQUESTOWNER);
		}
		setProperty(pullnode, "votes", request.getVotes());
		createEdgeIfNotExist(null, reponode, pullnode, EdgeType.PULLREQUEST);

		setProperty(pullnode, "sys:update_complete", new Date());
		log.trace("saveRepositoryPullRequest: exit");
		return pullnode;
	}

	/**
	 * Gets the date that this repository was last updated
	 * 
	 * @param repoId - the name of the repo, eg: defunkt/resque
	 * @return
	 */
	public Date getRepositoryLastUpdated(String reponame) {
		Vertex node = getOrCreateRepository(reponame);
		try {
			Date rv = dateFormatter.parse((String)node.getProperty("last_updated"));
			return rv;
		} catch (ParseException e) {
			log.error("Error parsing last_updated date for {}: {}", reponame, node.getProperty("last_updated"));
			return null;
		}
	}
	
	public Map<Integer, Date> getIssueCommentsAddedAt(String reponame) {
		Vertex node = getOrCreateRepository(reponame);
		HashMap<Integer, Date> m = new HashMap<Integer, Date>();
		for (Edge edge : node.getOutEdges(EdgeType.ISSUE.toString())) {
			Vertex issue = edge.getInVertex();
			Set<String> keys = issue.getPropertyKeys();
			try {
				if (keys.contains("sys:comments_added")) {
					Date d = dateFormatter.parse((String)issue.getProperty("sys:comments_added"));
					m.put(Integer.parseInt(((String)issue.getProperty("issue_id")).split(":")[1]), d);
				} else {
					log.trace("No sys:comments_added for issue {}", issue.getProperty("issue_id"));
				}
			} catch (ParseException e) {
				log.error("Error parsing sys:comments_added property for {}", issue.getProperty("issue_id"));
			}		
		}
		return m;
	}

	public Map<Integer, Date> getPullRequestDiscussionsAddedAt(String reponame) {
		Vertex node = getOrCreateRepository(reponame);
		HashMap<Integer, Date> m = new HashMap<Integer, Date>();
		for (Edge edge : node.getOutEdges(EdgeType.PULLREQUEST.toString())) {
			Vertex pullrequest = edge.getInVertex();
			Set<String> keys = pullrequest.getPropertyKeys();
			try {
				if (keys.contains("sys:discussions_added")) {
					Date d = dateFormatter.parse((String)pullrequest.getProperty("sys:discussions_added"));
					m.put(Integer.parseInt(((String)pullrequest.getProperty("pullrequest_id")).split(":")[1]), d);
				} else {
					log.trace("No sys:discussions_added for issue {}", pullrequest.getProperty("pullrequest_id"));
				}
			} catch (ParseException e) {
				log.error("Error parsing sys:discussions_added property for {}", pullrequest.getProperty("pullrequest_id"));
			}		
		}		
		return m;
	}
}
