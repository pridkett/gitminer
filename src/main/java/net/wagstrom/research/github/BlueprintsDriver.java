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
import java.util.Collections;
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
import com.ibm.research.govsci.graph.StringableEnum;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

/**
 * 
 * @author Patrick Wagstrom (http://patrick.wagstrom.net/)
 *
 */
public class BlueprintsDriver extends BlueprintsBase implements Shutdownable {

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
	private Index <Vertex> emailidx = null;
	
	/**
	 * Base constructor for BlueprintsDriver
	 * 
	 * @param dbengine The name of the engine to use, e.g. neo4j, orientdb, etc
	 * @param dburl The url of the database to use
	 * @param config additional configuration parameters to be passed to the database
	 */
	public BlueprintsDriver(String dbengine, String dburl, Map<String, String> config) {
		super(dbengine, dburl, config);
		// FIXME: eventually this should be configurable
		setMaxBufferSize(100000);
		log = LoggerFactory.getLogger(this.getClass());

		useridx = getOrCreateIndex(IndexNames.INDEX_USER);
		repoidx = getOrCreateIndex(IndexNames.INDEX_REPO);
		typeidx = getOrCreateIndex(IndexNames.INDEX_TYPE);
		orgidx = getOrCreateIndex(IndexNames.INDEX_ORGANIZATION);
		teamidx = getOrCreateIndex(IndexNames.INDEX_TEAM);
		gistidx = getOrCreateIndex(IndexNames.INDEX_GIST);
		commentidx = getOrCreateIndex(IndexNames.INDEX_COMMENT);
		gistfileidx = getOrCreateIndex(IndexNames.INDEX_GISTFILE);
		issueidx = getOrCreateIndex(IndexNames.INDEX_ISSUE);
		issuelabelidx = getOrCreateIndex(IndexNames.INDEX_ISSUELABEL);
		pullrequestidx = getOrCreateIndex(IndexNames.INDEX_PULLREQUEST);
		discussionidx = getOrCreateIndex(IndexNames.INDEX_DISCUSSION);
		commitidx = getOrCreateIndex(IndexNames.INDEX_COMMIT);
		pullrequestreviewcommentidx = getOrCreateIndex(IndexNames.INDEX_PULLREQUESTREVIEWCOMMENT);
		emailidx = getOrCreateIndex(IndexNames.INDEX_EMAIL);
	}	
	
	/**
	 * Helper function to indicate when a user save is not considered to
	 * be a complete save function.
	 * 
	 * This is used when user information is obtained from sources other than
	 * a full user dump.
	 * 
	 * @param user
	 * @return
	 */
	public Vertex saveUser(User user) {
		return saveUser(user, false);
	}
	
	/**
	 * Saves a User to the graph database
	 * 
	 * Right now this only saves the properties of the user and does not save any relationships.
	 * 
	 * @param user the GitHub user to save
	 * @param overwrite if this ia full dump then it will overwrite parameters
	 * @return the newly created database node
	 */
	public Vertex saveUser(User user, boolean overwrite) {
		Vertex node = getOrCreateUser(user.getLogin());
		log.debug("Saving User: {}", user.toString());

		setProperty(node, PropertyName.BLOG, user.getBlog());
		setProperty(node, PropertyName.COMPANY, user.getCompany());
		setProperty(node, PropertyName.CREATED_AT, user.getCreatedAt());
		if (user.getEmail() != null) {
			setProperty(node, PropertyName.EMAIL, user.getEmail());
			Vertex email = getOrCreateEmail(user.getEmail());
			createEdgeIfNotExist(node, email, EdgeType.EMAIL);
		}
		// these are all properties that tend to be 0 when non-full information is passed
		// thus we need to ignore them unless we're doing a full update
		if (overwrite) {
			setProperty(node, PropertyName.DISK_USAGE, user.getDiskUsage());
			setProperty(node, PropertyName.COLLABORATORS, user.getCollaborators());
			setProperty(node, PropertyName.FOLLOWERS, user.getFollowersCount());
			setProperty(node, PropertyName.FOLLOWING, user.getFollowingCount());
			setProperty(node, PropertyName.OWNED_PRIVATE_REPO_COUNT, user.getOwnedPrivateRepoCount());
			setProperty(node, PropertyName.PRIVATE_GIST_COUNT, user.getPrivateGistCount());
			setProperty(node, PropertyName.PUBLIC_GIST_COUNT, user.getPublicGistCount());
			setProperty(node, PropertyName.PUBLIC_REPO_COUNT, user.getPublicRepoCount());
			setProperty(node, PropertyName.SCORE, user.getScore());
			setProperty(node, PropertyName.TOTAL_PRIVATE_REPO_COUNT, user.getTotalPrivateRepoCount());
			setProperty(node, PropertyName.SYS_LAST_FULL_UPDATE.toString(), new Date());
		}
		setProperty(node, PropertyName.FULLNAME, user.getFullname());
		setProperty(node, PropertyName.GRAVATAR_ID, user.getGravatarId());
		setProperty(node, PropertyName.GITHUB_ID, user.getId()); // note name change
		setProperty(node, PropertyName.LOCATION, user.getLocation());
		setProperty(node, PropertyName.LOGIN, user.getLogin());
		setProperty(node, PropertyName.NAME, user.getName());
		setProperty(node, PropertyName.SYS_LAST_UPDATED.toString(), new Date());
		// getPermission
		// getPlan
		
		setProperty(node, PropertyName.USERNAME, user.getUsername());
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
		return getVertexHelper(age, IndexNames.INDEX_USER, VertexType.USER, "username");
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
		return getVertexHelper(age, IndexNames.INDEX_REPO, VertexType.REPOSITORY, "fullname");
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
	
	public Vertex getOrCreateEmail(String email) {
		return getOrCreateVertexHelper("email", email, VertexType.EMAIL, emailidx);
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
		// FIXME: update current database to replace commit_id with hash
		return getOrCreateVertexHelper("hash", commitId, VertexType.COMMIT, commitidx);
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

		setProperty(node, PropertyName.FULLNAME, projectFullName);

		setProperty(node, PropertyName.NAME, repo.getName());
		setProperty(node, PropertyName.ACTIONS, repo.getActions());
		setProperty(node, PropertyName.CREATED_AT, repo.getCreatedAt());
		setProperty(node, PropertyName.DESCRIPTION, repo.getDescription());
		setProperty(node, PropertyName.FOLLOWERS, repo.getFollowers());
		setProperty(node, PropertyName.FORKS, repo.getForks());
		setProperty(node, PropertyName.HOMEPAGE, repo.getHomepage());
		setProperty(node, PropertyName.GITHUB_ID, repo.getId()); // note name change
		// getLanguage
		setProperty(node, PropertyName.OPEN_ISSUES, repo.getOpenIssues());
		setProperty(node, PropertyName.ORGANIZATION, repo.getOrganization());
		setProperty(node, PropertyName.OWNER, repo.getOwner());
		setProperty(node, PropertyName.PARENT, repo.getParent());
		// getPermission
		setProperty(node, PropertyName.PUSHED_AT, repo.getPushedAt()); 
		setProperty(node, PropertyName.SCORE, repo.getScore());
		setProperty(node, PropertyName.SIZE, repo.getSize());
		setProperty(node, PropertyName.SOURCE, repo.getSource());
		setProperty(node, PropertyName.REPO_TYPE, repo.getType()); // note name change
		setProperty(node, PropertyName.URL, repo.getUrl());
		setProperty(node, PropertyName.USERNAME, repo.getUsername());
		// getVisibility
		setProperty(node, PropertyName.WATCHERS, repo.getWatchers());
		setProperty(node, PropertyName.SYS_LAST_UPDATED, new Date());
		return node;
	}
	
	public Vertex saveOrganization(Organization org) {
		Vertex node = getOrCreateOrganization(org.getLogin());
		setProperty(node, PropertyName.BILLING_EMAIL, org.getBillingEmail());
		setProperty(node, PropertyName.BLOG, org.getBlog());
		setProperty(node, PropertyName.COMPANY, org.getCompany());
		setProperty(node, PropertyName.CREATED_AT, org.getCreatedAt());
		setProperty(node, PropertyName.EMAIL, org.getEmail());
		setProperty(node, PropertyName.FOLLOWERS, org.getFollowersCount());
		setProperty(node, PropertyName.FOLLOWING, org.getFollowingCount());
		setProperty(node, PropertyName.GRAVATAR_ID, org.getGravatarId());
		setProperty(node, PropertyName.ID_NUM, org.getId()); // note name change
		setProperty(node, PropertyName.LOCATION, org.getLocation());
		setProperty(node, PropertyName.LOGIN, org.getLogin());
		setProperty(node, PropertyName.NAME, org.getName());
		setProperty(node, PropertyName.OWNED_PRIVATE_REPO_COUNT, org.getOwnedPrivateRepoCount());
		// getPermission
		setProperty(node, PropertyName.PRIVATE_GIST_COUNT, org.getPrivateGistCount());
		setProperty(node, PropertyName.PUBLIC_GIST_COUNT, org.getPublicGistCount());
		setProperty(node, PropertyName.PUBLIC_REPO_COUNT, org.getPublicRepoCount());
		setProperty(node, PropertyName.TOTAL_PRIVATE_REPO_COUNT, org.getTotalPrivateRepoCount());
		setProperty(node, PropertyName.ORG_TYPE, org.getType().toString()); // not certain if this is what we really want
		return node;
	}

	public Vertex saveTeam(Team team) {
		Vertex node = getOrCreateTeam(team.getId());
		setProperty(node, PropertyName.NAME, team.getName());
		// getPermission
		// getRepoNames
		return node;
	}

	protected Vertex saveCommentHelper(Comment comment, EdgeType edgetype) {
		Vertex node = getOrCreateComment(comment.getId());
		setProperty(node, PropertyName.BODY, comment.getBody());
		setProperty(node, PropertyName.CREATED_AT, comment.getCreatedAt());
		// FIXME: perhaps gravatarId should be another node?
		setProperty(node, PropertyName.GRAVATAR_ID, comment.getGravatarId());
		setProperty(node, PropertyName.UPDATED_AT, comment.getUpdatedAt());
		if (comment.getUser() != null) {
			Vertex user = getOrCreateUser(comment.getUser());
			createEdgeIfNotExist(user, node, edgetype);
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
			createEdgeIfNotExist(node, commentnode, EdgeType.GISTCOMMENT);
		}
		setProperty(node, PropertyName.CREATED_AT, gist.getCreatedAt());
		setProperty(node, PropertyName.DESCRIPTION, gist.getDescription());
		for (String file : gist.getFiles()) {
			Vertex filenode = saveGistFile(gist.getRepo(), file);
			createEdgeIfNotExist(null, node, filenode, EdgeType.GISTFILE);
		}
		setProperty(node, PropertyName.OWNER, gist.getOwner());
		setProperty(node, PropertyName.REPO, gist.getRepo());
		return node;
	}
	
	protected Map<String, Vertex> saveOrganizationMembersHelper(String organization, List<User> owners, EdgeType edgetype) {
		Vertex org = getOrCreateOrganization(organization);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (User owner : owners) {
			Vertex usernode = saveUser(owner);
			createEdgeIfNotExist(usernode, org, edgetype);
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
			createEdgeIfNotExist(source, reponode, EdgeType.REPOOWNER);
			mapper.put(projectFullName, reponode);
		}
		return mapper;
	}
	
	public Map<String, Vertex> saveOrganizationTeams(String organization, List<Team> teams) {
		Vertex org = getOrCreateOrganization(organization);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Team team: teams) {
			Vertex teamnode = saveTeam(team);
			createEdgeIfNotExist(org, teamnode, EdgeType.ORGANIZATIONTEAM);
			mapper.put(team.getId(), teamnode);
		}
		return mapper;
	}
	
	public Map<String,Vertex> saveTeamMembers(String team, List<User> users) {
		Vertex teamnode = getOrCreateTeam(team);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (User user : users) {
			Vertex usernode = saveUser(user);
			createEdgeIfNotExist(usernode, teamnode, EdgeType.TEAMMEMBER);
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
			createEdgeIfNotExist(teamnode, reponode, EdgeType.REPOOWNER);
			mapper.put(projectFullName, reponode);
		}
		return mapper;
	}
	
	public Map<String,Vertex> saveUserGists(String user, List<Gist> gists) {
		Vertex usernode = getOrCreateUser(user);
		HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
		for (Gist gist : gists) {
			Vertex gistnode = saveGist(gist);
			createEdgeIfNotExist(usernode, gistnode, EdgeType.GISTOWNER);
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
		setProperty(issuenode, PropertyName.BODY, issue.getBody());
		setProperty(issuenode, PropertyName.CLOSED_AT, issue.getClosedAt());
		setProperty(issuenode, PropertyName.COMMENTS, issue.getComments());
		setProperty(issuenode, PropertyName.CREATED_AT, issue.getCreatedAt());
		setProperty(issuenode, PropertyName.GRAVATAR_ID, issue.getGravatarId());
		for (String label : issue.getLabels()) {
			Vertex labelnode = getOrCreateIssueLabel(label);
			createEdgeIfNotExist(issuenode, labelnode, EdgeType.ISSUELABEL);
		}
		setProperty(issuenode, PropertyName.NUMBER, issue.getNumber());
		setProperty(issuenode, PropertyName.POSITION, issue.getPosition());
		setProperty(issuenode, PropertyName.STATE, issue.getState().toString());
		setProperty(issuenode, PropertyName.TITLE, issue.getTitle());
		setProperty(issuenode, PropertyName.UPDATED_AT, issue.getUpdatedAt());
		if (issue.getUser() != null) {
			setProperty(issuenode, PropertyName.USER, issue.getUser());
			Vertex userNode = getOrCreateUser(issue.getUser());
			createEdgeIfNotExist(userNode, issuenode, EdgeType.ISSUEOWNER);
		}
		setProperty(issuenode, PropertyName.VOTES, issue.getVotes());
		setProperty(issuenode, PropertyName.UPDATED_AT, new Date());
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
			createEdgeIfNotExist(issuenode, commentnode, EdgeType.ISSUECOMMENT);
			mapper.put(new Long(comment.getId()), commentnode);
			
		}
		setProperty(issuenode, PropertyName.SYS_COMMENTS_ADDED.toString(), new Date());
		return mapper;
	}
	
	// FIXME: this code does not look like it functions properly
	public void saveRepositoryPullRequests(String project, Collection<PullRequest> requests) {
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
			createEdgeIfNotExist(author, node, EdgeType.COMMITAUTHOR);
		}
		setProperty(node, PropertyName.AUTHORED_DATE, commit.getAuthoredDate());
		setProperty(node, PropertyName.COMMITTED_DATE, commit.getCommittedDate());
		if (commit.getCommitter() != null) {
			Vertex committer = saveUser(commit.getCommitter());
			createEdgeIfNotExist(committer, node, EdgeType.COMMITTER);
		}
		setProperty(node, PropertyName.DATE, commit.getDate());
		setProperty(node, PropertyName.GRAVATAR_ID, commit.getGravatar());
		setProperty(node, PropertyName.COMMIT_ID, commit.getId());
		setProperty(node, PropertyName.LOGIN, commit.getLogin());
		setProperty(node, PropertyName.MESSAGE, commit.getMessage());
		// modified
		for (Id id : commit.getParents()) {
			Vertex parent = getOrCreateCommit(id.getId());
			createEdgeIfNotExist(node, parent, EdgeType.COMMITPARENT);
		}
		// removed
		setProperty(node, PropertyName.SPACE, commit.getSpace());
		setProperty(node, PropertyName.TIME, commit.getTime());
		setProperty(node, PropertyName.TREE, commit.getTree());
		setProperty(node, PropertyName.URL, commit.getUrl());
		log.trace("saveCommit: exit");
		return node;
	}
	
	public Vertex savePullRequestReviewComment(PullRequestReviewComment comment) {
		log.trace("savePullRequestReviewComment: enter");
		String commentId = comment.getCommitId() + ":" + comment.getCreatedAt()==null?"UNKNOWNDATE":dateFormatter.format(comment.getCreatedAt());
		Vertex node = getOrCreatePullRequestReviewComment(commentId);
		setProperty(node, PropertyName.BODY, comment.getBody());
		setProperty(node, PropertyName.COMMIT_ID, comment.getCommitId());
		setProperty(node, PropertyName.CREATED_AT, comment.getCreatedAt());
		setProperty(node, PropertyName.DIFF_HUNK, comment.getDiffHunk());
		setProperty(node, PropertyName.ORIGINAL_COMMIT_ID, comment.getOriginalCommitId());
		setProperty(node, PropertyName.PATH, comment.getPath());
		setProperty(node, PropertyName.POSITION, comment.getPosition());
		setProperty(node, PropertyName.UPDATED_AT, comment.getUpdatedAt());
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
	
	/**
	 * Helper function to save an individual pull request
	 * 
	 * This function assumes that the pull request is not the full information,
	 * thus it does not set all of the properties, such as sys:discussions_added
	 * 
	 * @param reponame
	 * @param request
	 * @return
	 */
	public Vertex saveRepositoryPullRequest(String reponame, PullRequest request) {
		return saveRepositoryPullRequest(reponame, request, false);
	}
	
	/**
	 * Main function to save an individual pull request
	 * 
	 * @param reponame
	 * @param request
	 * @param full whether or not this is a full update. If true then it sets parameters such as sys:discussions_added
	 * @return
	 */
	public Vertex saveRepositoryPullRequest(String reponame, PullRequest request, boolean full) {
		log.trace("saveRepositoryPullRequest: enter");
		log.trace("Saving pull request {}", request.getNumber());
		log.trace(request.toString());
		Vertex reponode = getOrCreateRepository(reponame);
		Vertex pullnode = getOrCreatePullRequest(reponame + ":" + request.getNumber());
		// getBase()
		setProperty(pullnode, PropertyName.BODY, request.getBody());
		setProperty(pullnode, PropertyName.COMMENTS, request.getComments());
		setProperty(pullnode, PropertyName.CREATED_AT, request.getCreatedAt());
		setProperty(pullnode, PropertyName.DIFF_URL, request.getDiffUrl());

		for (Discussion discussion : request.getDiscussion()) {
			Vertex discussionnode = saveDiscussion(discussion);
			log.trace("Created discussion node");
			createEdgeIfNotExist(null, pullnode, discussionnode, EdgeType.PULLREQUESTDISCUSSION);
		}
		setProperty(pullnode, PropertyName.GRAVATAR_ID, request.getGravatarId());
		// request.getHead()
		setProperty(pullnode, PropertyName.HTML_URL, request.getHtmlUrl());
		setProperty(pullnode, PropertyName.ISSUE_CREATED_AT, request.getIssueCreatedAt());
		setProperty(pullnode, PropertyName.ISSUE_UPDATED_AT, request.getIssueUpdatedAt());
		if (request.getIssueUser() != null) {
			Vertex usernode = saveUser(request.getIssueUser());
			createEdgeIfNotExist(usernode, pullnode, EdgeType.PULLREQUESTISSUEUSER);
		}
		for (String label : request.getLabels()) {
			Vertex labelnode = getOrCreateIssueLabel(label);
			createEdgeIfNotExist(null, pullnode, labelnode, EdgeType.PULLREQUESTLABEL);
		}
		setProperty(pullnode, PropertyName.NUMBER, request.getNumber());
		setProperty(pullnode, PropertyName.PATCH_URL, request.getPatchUrl());
		setProperty(pullnode, PropertyName.POSITION, request.getPosition());
		setProperty(pullnode, PropertyName.STATE, request.getState().toString());
		setProperty(pullnode, PropertyName.TITLE, request.getTitle());
		if (request.getUser() != null) {
			Vertex usernode = saveUser(request.getUser());
			createEdgeIfNotExist(usernode, pullnode, EdgeType.PULLREQUESTOWNER);
		}
		setProperty(pullnode, PropertyName.VOTES, request.getVotes());
		createEdgeIfNotExist(reponode, pullnode, EdgeType.PULLREQUEST);

		if (full == true) {
			setProperty(pullnode, PropertyName.SYS_DISCUSSIONS_ADDED.toString(), new Date());
			setProperty(pullnode, PropertyName.SYS_UPDATE_COMPLETE.toString(), new Date());
		}
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
	
	/**
	 * Get a map of date when comments were added to each issue
	 * 
	 * In the case that sys:comments_added is not set null is inserted into the map.
	 * 
	 * @param reponame the name of the repository to mine
	 * @return a Map that maps issue_ids to the date that the comments were downloaded
	 */
	public Map<Integer, Date> getIssueCommentsAddedAt(String reponame) {
		Vertex node = getOrCreateRepository(reponame);
		HashMap<Integer, Date> m = new HashMap<Integer, Date>();
		
		GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<Vertex, Vertex>();
		pipe.start(node).out(EdgeType.ISSUE.toString());
				
//		ScriptEngine engine = new GremlinScriptEngine();
//		List<Vertex> list = new ArrayList<Vertex>();
//		engine.put("g", this.graph);
//		engine.put("list", list);		
//		engine.put("node", node);

		// try {
			// engine.eval("node._().out('" + EdgeType.ISSUE + "') >> list");
		addValuesFromIterable(pipe, m, PropertyName.NUMBER, PropertyName.SYS_COMMENTS_ADDED);
//		} catch (ScriptException e) {
//			log.error("ScriptException encountered in getIssueCommentsAddedAt");
//		}
		return m;
	}

	/**
	 * Similar to getIssueCommentsAddedAt except it infers missing values
	 * 
	 * The method is pretty simple, find the largest value obtained from
	 * getIssueCommentsAt and insert null values for all of the values going
	 * up to that point.
	 * 
	 * This function is needed because there are times that GitHub chokes on
	 * getting the issues for a particular project. In particular this happens
	 * with mxcl/homebrew which has about 7000 closed issues.
	 * 
	 * @param reponame name of the repository to mine
	 * @return a Map that maps issue_ids to the date that the comments were downloaded
	 */
	public Map<Integer, Date> getIssueCommentsAddedAtBruteForce(String reponame) {
		Map<Integer, Date> m = getIssueCommentsAddedAt(reponame);
		int max = Collections.max(m.keySet());
		for (int i=1; i < max; i ++) {
			if (!m.containsKey(i)) {
				m.put(i, null);
			}
		}
		return m;
	}
	
	/**
	 * Return a mapping between pull requests and the date they were augmented
	 * with discussions.
	 * 
	 * @param reponame
	 * @return
	 */
	public Map<Integer, Date> getPullRequestDiscussionsAddedAt(String reponame) {
		Vertex node = getOrCreateRepository(reponame);
		HashMap<Integer, Date> m = new HashMap<Integer, Date>();

		GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<Vertex, Vertex>();
		pipe.start(node).out(EdgeType.PULLREQUEST.toString());
		addValuesFromIterable(pipe, m, PropertyName.NUMBER, PropertyName.SYS_DISCUSSIONS_ADDED);
		return m;
	}

	/**
	 * A generic method that goes over an iterable and adds the appropriate value to a map
	 * 
	 * This is most commonly used when iterating over a list of nodes obtained
	 * from a Gremlin traversal. It stores properties in a map of idkey:datekey. Thus,
	 * right now this ONLY works if you're looking at dates, which is a common
	 * occurance.
	 * 
	 * @param it The iterable to iterate over. Usually and ArrayList<Vertex>
	 * @param m The map that will map class T to Date. Usually T is String or Integer
	 * @param idkey The property of Elements in it that contains the value of T in the map
	 * @param datekey The property of Element in it that contains the value of Date in the map
	 * @return the updated map
	 */
	@SuppressWarnings("unchecked")
	private <T, I extends Element> Map<T, Date> addValuesFromIterable(Iterable<I> it, Map<T, Date> m, String idkey, String datekey) {
		for (I v : it) {
			Set<String> keys = v.getPropertyKeys();
			try {
				if (!keys.contains(idkey)) {
					log.warn("Node found with no idkey: {}", v);
					continue;
				}
				if (keys.contains(datekey)) {
					Date d = dateFormatter.parse((String)v.getProperty(datekey));
					m.put((T)v.getProperty(idkey), d);
				} else {
					m.put((T)v.getProperty(idkey), null);
				}
			} catch (ParseException e) {
				log.error("Invalid {} for user: {}", datekey, (String)v.getProperty(idkey));
			}
		}
		return m;
	}

	private <T, I extends Element> Map<T, Date> addValuesFromIterable(Iterable<I> it, Map<T, Date> m, String idkey, StringableEnum datekey) {
		return addValuesFromIterable(it, m, idkey, datekey.toString());
	}

	private <T, I extends Element> Map<T, Date> addValuesFromIterable(Iterable<I> it, Map<T, Date> m, StringableEnum idkey, StringableEnum datekey) {
		return addValuesFromIterable(it, m, idkey.toString(), datekey.toString());
	}
	
	/**
	 * An aggressive method that attempts to get the last date that ALL the users
	 * associated with a project were updated.
	 * 
	 * This looks at watchers, collaborators, contributors, and all those active
	 * on issues and pullrequests (including subdiscussions).
	 * 
	 * This method is commonly used to get a list of users and the dates they were
	 * updated for purposes of crawling all of the users again.
	 * 
	 * @param reponame the name of the repository, eg mxcl/homebrew
	 * @return a mapping of usernames to the date they last had a full update
	 */
	public Map<String, Date> getProjectUsersLastFullUpdate(String reponame) {
		Vertex node = getOrCreateRepository(reponame);
		HashMap<String, Date> m = new HashMap<String, Date>();
		GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<Vertex, Vertex>();
		
		// first: get all the users watching the project
		pipe.start(node).in(EdgeType.REPOWATCHED.toString());
		addValuesFromIterable(pipe, m, PropertyName.LOGIN, PropertyName.SYS_LAST_FULL_UPDATE);
		
		// add the collaborators
		pipe = new GremlinPipeline<Vertex, Vertex>();
		pipe.start(node).out(EdgeType.REPOCOLLABORATOR.toString());
		addValuesFromIterable(pipe, m, PropertyName.LOGIN, PropertyName.SYS_LAST_FULL_UPDATE);	

		// add the contributors
		pipe = new GremlinPipeline<Vertex, Vertex>();
		pipe.start(node).out(EdgeType.REPOCONTRIBUTOR.toString());
		addValuesFromIterable(pipe, m, PropertyName.LOGIN, PropertyName.SYS_LAST_FULL_UPDATE);	

		// add the issue owners
		pipe = new GremlinPipeline<Vertex, Vertex>();
		pipe.start(node).out(EdgeType.ISSUE.toString()).in(EdgeType.ISSUEOWNER.toString()).dedup();
		addValuesFromIterable(pipe, m, PropertyName.LOGIN, PropertyName.SYS_LAST_FULL_UPDATE);	

		// add the individuals who commented on the issues
		pipe = new GremlinPipeline<Vertex, Vertex>();
		pipe.start(node).out(EdgeType.ISSUE.toString()).out(EdgeType.ISSUECOMMENT.toString()).in(EdgeType.ISSUECOMMENTOWNER.toString()).dedup();
		addValuesFromIterable(pipe, m, PropertyName.LOGIN, PropertyName.SYS_LAST_FULL_UPDATE);	

		// add the pull request owners
		pipe = new GremlinPipeline<Vertex, Vertex>();
		pipe.start(node).out(EdgeType.PULLREQUEST.toString()).in(EdgeType.PULLREQUESTOWNER.toString()).dedup();
		addValuesFromIterable(pipe, m, PropertyName.LOGIN, PropertyName.SYS_LAST_FULL_UPDATE);	

		// add the pull request commenters
		pipe = new GremlinPipeline<Vertex, Vertex>();
		pipe.start(node).out(EdgeType.PULLREQUEST.toString()).out(EdgeType.PULLREQUESTDISCUSSION.toString()).in().filter(new PipeFunction<Vertex, Boolean>() {
			public Boolean compute(Vertex argument) {
				return (argument.getProperty(PropertyName.TYPE.toString()).equals(VertexType.USER.toString()));
			}
		}).dedup();
		addValuesFromIterable(pipe, m, PropertyName.LOGIN, PropertyName.SYS_LAST_FULL_UPDATE);	
		
		return m;
	}
}
