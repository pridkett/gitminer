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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.research.govsci.graph.BlueprintsBase;
import com.ibm.research.govsci.graph.Shutdownable;
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
    private Index <Vertex> markeridx = null;
    private Index <Vertex> milestoneidx = null;
    private Index <Vertex> issueeventidx = null;
    private Index <Vertex> gravataridx = null;
    private Index <Vertex> gituseridx = null;
    private Index <Vertex> nameidx = null;

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

        useridx = getOrCreateIndex(IndexNames.USER);
        repoidx = getOrCreateIndex(IndexNames.REPOSITORY);
        typeidx = getOrCreateIndex(IndexNames.TYPE);
        orgidx = getOrCreateIndex(IndexNames.ORGANIZATION);
        teamidx = getOrCreateIndex(IndexNames.TEAM);
        gistidx = getOrCreateIndex(IndexNames.GIST);
        commentidx = getOrCreateIndex(IndexNames.COMMENT);
        gistfileidx = getOrCreateIndex(IndexNames.GISTFILE);
        issueidx = getOrCreateIndex(IndexNames.ISSUE);
        issuelabelidx = getOrCreateIndex(IndexNames.ISSUELABEL);
        pullrequestidx = getOrCreateIndex(IndexNames.PULLREQUEST);
        discussionidx = getOrCreateIndex(IndexNames.DISCUSSION);
        commitidx = getOrCreateIndex(IndexNames.COMMIT);
        pullrequestreviewcommentidx = getOrCreateIndex(IndexNames.PULLREQUESTREVIEWCOMMENT);
        emailidx = getOrCreateIndex(IndexNames.EMAIL);
        markeridx = getOrCreateIndex(IndexNames.PULLREQUESTMARKER);
        milestoneidx = getOrCreateIndex(IndexNames.MILESTONE);
        issueeventidx = getOrCreateIndex(IndexNames.ISSUEEVENT);
        gravataridx = getOrCreateIndex(IndexNames.GRAVATAR);
        gituseridx = getOrCreateIndex(IndexNames.GITUSER);
        nameidx = getOrCreateIndex(IndexNames.NAME);
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
                    Date d = propertyToDate(v.getProperty(datekey));
                    m.put((T)v.getProperty(idkey), d);
                } else {
                    m.put((T)v.getProperty(idkey), null);
                }
            } catch (Exception e) {
                log.error("Invalid {} for user: {}", datekey, (String)v.getProperty(idkey));
            }
        }
        return m;
    }

//    public Vertex createCommentFromDiscussion(Discussion disc) {
//        log.trace("createCommentFromDiscussion: enter");
//        Comment comment = new Comment();
//        comment.setBody(disc.getBody());
//        comment.setCreatedAt(disc.getCreatedAt());
//        comment.setGravatarId(disc.getGravatarId());
//        try {
//            comment.setId(Long.parseLong(disc.getId()));
//        } catch (NumberFormatException e) {
//            log.debug("Discussion has a null id: {}", disc);
//        }
//        comment.setUpdatedAt(disc.getUpdatedAt());
//        comment.setUser(disc.getUser().getLogin());
//        Vertex node = savePullRequestComment(comment);
//        log.trace("createCommentFromDiscussion: exit");
//        return node;
//    }
//
//    public Vertex createCommitFromDiscussion(Discussion disc) {
//        log.trace("createCommitFromDiscusion: enter");
//        Commit commit = new Commit();
//        commit.setCommittedDate(disc.getCommittedDate());
//        commit.setAuthoredDate(disc.getAuthoredDate());
//        commit.setId(disc.getId());
//        commit.setAuthor(disc.getAuthor());
//        commit.setCommitter(disc.getCommitter());
//        commit.setMessage(disc.getMessage());
//        commit.setTree(disc.getTree());
//        commit.setParents(disc.getParents());
//        Vertex node = saveCommit(commit);
//        log.trace("createCommitFromDiscussion: exit");
//        return node;
//    }
//
//    public Vertex createPullRequestReviewCommentFromDiscussion(Discussion disc) {
//        log.trace("createPullRequestReviewCommentFromDiscussion: enter");
//        PullRequestReviewComment comment = new PullRequestReviewComment();
//        comment.setDiffHunk(disc.getDiffHunk());
//        comment.setBody(disc.getBody());
//        comment.setPath(disc.getPath());
//        comment.setPosition(disc.getPosition());
//        comment.setCommitId(disc.getCommitId());
//        comment.setOriginalCommitId(disc.getOriginalCommitId());
//        comment.setUser(disc.getUser());
//        comment.setCreatedAt(disc.getCreatedAt());
//        comment.setUpdatedAt(disc.getUpdatedAt());
//        Vertex node = savePullRequestReviewComment(comment);
//        log.trace("createPullRequestReviewCommentFromDiscussion: exit");
//        return node;
//    }

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

        addValuesFromIterable(pipe, m, PropertyName.NUMBER, PropertyName.SYS_COMMENTS_ADDED);

        return m;
    }

    /**
     * Get a map of date when events were added to each issue
     * 
     * In the case that sys_events_added is not set null is inserted into the map.
     * 
     * @param reponame the name of the repository to mine
     * @return a Map that maps issue_ids to the date that the events were downloaded
     */
    public Map<Integer, Date> getIssueEventsAddedAt(IRepositoryIdProvider repo) {
        Vertex node = getOrCreateRepository(repo.generateId());
        HashMap<Integer, Date> m = new HashMap<Integer, Date>();

        GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<Vertex, Vertex>();
        pipe.start(node).out(EdgeType.ISSUE.toString());

        addValuesFromIterable(pipe, m, PropertyName.NUMBER, PropertyName.SYS_EVENTS_ADDED);

        return m;
    }

    public Vertex getOrCreateComment(long commentId) {
        return getOrCreateVertexHelper(IdCols.COMMENT, commentId, VertexType.COMMENT, commentidx);
    }

    public Vertex getOrCreateCommit(String commitId) {
        log.debug("Fetching or creating commit: {}", commitId);
        // FIXME: update current database to replace commit_id with hash
        return getOrCreateVertexHelper(IdCols.COMMIT, commitId, VertexType.COMMIT, commitidx);
    }

    public Vertex getOrCreateDiscussion(String discussionId) {
        return getOrCreateVertexHelper(IdCols.DISCUSSION, discussionId, VertexType.DISCUSSION, discussionidx);
    }

    public Vertex getOrCreateEmail(String email) {
        return getOrCreateVertexHelper(IdCols.EMAIL, email, VertexType.EMAIL, emailidx);
    }

    private Vertex getOrCreateEvent(long id) {
        return getOrCreateVertexHelper(IdCols.EVENT, id, VertexType.ISSUE_EVENT, issueeventidx);
    }

    public Vertex getOrCreateGist(String repoId) {
        return getOrCreateVertexHelper(IdCols.GIST, repoId, VertexType.GIST, gistidx);
    }

    public Vertex getOrCreateGistFile(String repoid, String filename) {
        String gistFileId = repoid + "/" + filename;
        return getOrCreateVertexHelper(IdCols.GISTFILE,  gistFileId, VertexType.GISTFILE, gistfileidx);
    }

    private Vertex getOrCreateIssue(String project, Issue issue) {
        String issueId = project + ":" + issue.getNumber();
        return getOrCreateVertexHelper(IdCols.ISSUE, issueId, VertexType.ISSUE, issueidx);
    }

    private Vertex getOrCreateIssue(
            Repository repo,
            Issue issue) {
        String issueId = repo.generateId() + ":" + issue.getNumber();
        return getOrCreateVertexHelper(IdCols.ISSUE, issueId, VertexType.ISSUE, issueidx);
    }



    // FIXME: labels vary by projects, so this may require an additional identifier
    private Vertex getOrCreateIssueLabel(Label label) {
        return getOrCreateVertexHelper(IdCols.LABEL, label.getName(), VertexType.LABEL, issuelabelidx);
    }

    public Vertex getOrCreateIssueLabel(String label) {
        return getOrCreateVertexHelper(IdCols.LABEL, label, VertexType.LABEL, issuelabelidx);
    }

    /**
     * The Id for a milestone is something like pridkett/gitminer:fse2012
     *                                          |---- REPO -----| |TITLE|
     * @param milestone
     * @return
     */
    private Vertex getOrCreateMilestone(String milestone) {
        return getOrCreateVertexHelper(IdCols.MILESTONE, milestone, VertexType.MILESTONE, milestoneidx);
    }

    public Vertex getOrCreateOrganization(String login) {
        return getOrCreateVertexHelper(IdCols.ORGANIZATION, login, VertexType.ORGANIZATION, orgidx);
    }

    public Vertex getOrCreatePullRequest(String idval) {
        return getOrCreateVertexHelper(IdCols.PULLREQUEST, idval, VertexType.PULLREQUEST, pullrequestidx);
    }

    private Vertex getOrCreatePullRequestMarker(PullRequestMarker head) {
        return getOrCreateVertexHelper(IdCols.PULLREQUESTMARKER, head.getSha(), VertexType.PULLREQUESTMARKER, markeridx);
    }

    public Vertex getOrCreatePullRequestReviewComment(String commentId) {
        log.debug("Fetching or creating PullRequestReviewComment: {}", commentId);
        return getOrCreateVertexHelper(IdCols.PULLREQUESTREVIEWCOMMENT, commentId, VertexType.PULLREQUESTREVIEWCOMMENT, pullrequestreviewcommentidx);
    }

    public Vertex getOrCreateRepository(IRepositoryIdProvider repo) {
        return getOrCreateVertexHelper(IdCols.REPOSITORY, repo.generateId(), VertexType.REPOSITORY, repoidx);
    }

    public Vertex getOrCreateRepository(String reponame) {
        return getOrCreateVertexHelper(IdCols.REPOSITORY, reponame, VertexType.REPOSITORY, repoidx);
    }

    public Vertex getOrCreateTeam(Team team) {
        return getOrCreateVertexHelper(IdCols.TEAM, team.getId(), VertexType.TEAM, teamidx);
    }
    
    public Vertex getOrCreateTeam(String teamId) {
        return getOrCreateVertexHelper(IdCols.TEAM, teamId, VertexType.TEAM, teamidx);
    }

    private Vertex getOrCreateUser(User user) {
        return getOrCreateVertexHelper(IdCols.USER, user.getLogin(), VertexType.USER, useridx);
    }

    public Vertex getOrCreateUser(String login) {
        return getOrCreateVertexHelper(IdCols.USER, login, VertexType.USER, useridx);
    }

    public Vertex getOrCreateGitUser( String name, String email ) {
        String key = name + " <" + email + ">";
        return getOrCreateVertexHelper(IdCols.GITUSER, key, VertexType.GIT_USER, gituseridx);
    }

    public Vertex getOrCreateName( String name ) {
        return getOrCreateVertexHelper(IdCols.NAME, name, VertexType.NAME, nameidx);
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
     * Gets the date that this repository was last updated
     * 
     * @param repoId - the name of the repo, eg: defunkt/resque
     * @return
     */
    public Date getRepositoryLastUpdated(String reponame) {
        Vertex node = getOrCreateRepository(reponame);
        return propertyToDate(node.getProperty("last_updated"));
    }

    protected Vertex saveCommentHelper(Comment comment, String edgetype) {
        Vertex node = getOrCreateComment(comment.getId());
        setProperty(node, PropertyName.BODY, comment.getBody());
        setProperty(node, PropertyName.BODY_HTML, comment.getBodyHtml());
        setProperty(node, PropertyName.BODY_TEXT, comment.getBodyText());
        setProperty(node, PropertyName.URL, comment.getUrl());
        setProperty(node, PropertyName.CREATED_AT, comment.getCreatedAt());
        setProperty(node, PropertyName.UPDATED_AT, comment.getUpdatedAt());
        if (comment.getUser() != null) {
            Vertex user = getOrCreateUser(comment.getUser());
            createEdgeIfNotExist(user, node, edgetype);
        }
        return node;
    }

    /**
     * Saves a commit object into the data
     *
     * there are some critical elements of information that are no longer
     * saved in the v3 api vs v2. Namely the date of the commit and the
     * parents of the commit.
     * 
     * @param commit
     * @return
     */
    public Vertex saveCommit(Commit commit) {
        log.trace("saveCommit: enter");
        Vertex node = getOrCreateCommit(commit.getSha());
        if (commit.getAuthor() != null) {
            Vertex author = saveUser(commit.getAuthor());
            createEdgeIfNotExist(node, author, EdgeType.COMMITAUTHOR);
            setProperty(node, PropertyName.AUTHORED_DATE, commit.getAuthor().getDate());
        }
        if (commit.getCommitter() != null) {
            Vertex committer = saveUser(commit.getCommitter());
            createEdgeIfNotExist(node, committer, EdgeType.COMMITTER);
            setProperty(node, PropertyName.COMMITTED_DATE, commit.getCommitter().getDate());
        }
        setProperty(node, PropertyName.COMMIT_ID, commit.getSha());
        setProperty(node, PropertyName.MESSAGE, commit.getMessage());

        setProperty(node, PropertyName.TREE, commit.getTree());
        setProperty(node, PropertyName.URL, commit.getUrl());
        log.trace("saveCommit: exit");
        return node;
    }

//    public Vertex saveDiscussion(Discussion discussion) {
//        log.trace("saveDiscussion: enter");
//        Vertex node = null;
//        if (discussion.getType() == null && discussion.getBody() != null) {
//            log.warn("Discussion type was null now ISSUE_COMMENT: {}", discussion.toString());
//            discussion.setType(Discussion.Type.ISSUE_COMMENT);
//        }
//        log.trace("Discussion type: {}", discussion.getType().toString());
//        if (discussion.getType().equals(Discussion.Type.COMMIT)) {
//            node = createCommitFromDiscussion(discussion);
//        } else if (discussion.getType().equals(Discussion.Type.ISSUE_COMMENT)) {
//            node = createCommentFromDiscussion(discussion);
//        } else if (discussion.getType().equals(Discussion.Type.PULL_REQUEST_REVIEW_COMMENT)) {
//            node = createPullRequestReviewCommentFromDiscussion(discussion);
//        } else {
//            log.error("Undefined discussion type : {}", discussion.getType().toString());
//        }
//        if (node != null && discussion.getUser() != null) {
//            Vertex user = saveUser(discussion.getUser());
//            createEdgeIfNotExist(null, user, node, EdgeType.DISCUSSIONUSER);
//        }
//        log.trace("saveDiscussion: exit");
//        return node;
//    }

    public Vertex saveGist(Gist gist) {
        Vertex node = getOrCreateGist(gist.getId());
        
        setProperty(node, PropertyName.CREATED_AT, gist.getCreatedAt());
        setProperty(node, PropertyName.DESCRIPTION, gist.getDescription());

        for (Map.Entry<String, GistFile> gistFile : gist.getFiles().entrySet()) {
            Vertex filenode = saveGistFile(gist.getId(), gistFile.getValue());
            createEdgeIfNotExist(null, node, filenode, EdgeType.GISTFILE);
        }
        setProperty(node, PropertyName.OWNER, gist.getUser().getLogin());
        setProperty(node, PropertyName.COMMENTS, gist.getComments());
        setProperty(node, PropertyName.GIT_PULL_URL, gist.getGitPullUrl());
        setProperty(node, PropertyName.GIT_PUSH_URL, gist.getGitPushUrl());
        setProperty(node, PropertyName.URL, gist.getUrl());
        setProperty(node, PropertyName.UPDATED_AT, gist.getUpdatedAt());
        setProperty(node, PropertyName.HTML_URL, gist.getHtmlUrl());
        return node;
    }

    public Collection<Vertex> saveGistComments(Gist gist,
            List<Comment> comments) {
        ArrayList<Vertex> commentList = new ArrayList<Vertex>();
        Vertex node = getOrCreateGist(gist.getId());
        for (Comment comment : comments) {
            Vertex commentnode = saveGistComment(comment);
            createEdgeIfNotExist(node, commentnode, EdgeType.GISTCOMMENT);
            commentList.add(commentnode);
        }
        return commentList;
    }
    
    public Vertex saveGistComment(Comment comment) {
        return saveCommentHelper(comment, EdgeType.GISTCOMMENTOWNER);
    }

    public Vertex saveGistFile(String repoid, GistFile gistFile) {
        Vertex node = getOrCreateGistFile(repoid, gistFile.getFilename());
        return node;
    }

    /**
     * Saves a v3 API issue to the database
     * 
     * FIXME: this should have some way to save comments associated with an issue
     * 
     * @param repo
     * @param issue
     * @return
     */
    private Vertex saveIssue(Repository repo,
            Issue issue) {
        Vertex issuenode = getOrCreateIssue(repo.generateId(), issue);
        if (issue.getAssignee() != null) {
            setProperty(issuenode, PropertyName.ASSIGNEE, issue.getAssignee().getLogin());
            Vertex userNode = getOrCreateUser(issue.getAssignee());
            createEdgeIfNotExist(userNode, issuenode, EdgeType.ISSUEASSIGNEE);
        }
        // FIXME: need to add support for getClosedBy
        setProperty(issuenode, PropertyName.BODY, issue.getBody());
        setProperty(issuenode, PropertyName.BODY_HTML, issue.getBodyHtml());
        setProperty(issuenode, PropertyName.CLOSED_AT, issue.getClosedAt());
        setProperty(issuenode, PropertyName.COMMENTS, issue.getComments());
        setProperty(issuenode, PropertyName.CREATED_AT, issue.getCreatedAt());
        setProperty(issuenode, PropertyName.HTML_URL, issue.getHtmlUrl());
        setProperty(issuenode, PropertyName.HTML_URL, issue.getHtmlUrl());
        setProperty(issuenode, PropertyName.GITHUB_ID, issue.getId());
        for (Label label : issue.getLabels()) {
            Vertex labelnode = getOrCreateIssueLabel(label);
            createEdgeIfNotExist(issuenode, labelnode, EdgeType.ISSUELABEL);
        }
        setProperty(issuenode, PropertyName.GITHUB_ID, issue.getId());
        if (issue.getMilestone() != null) {
            Milestone m = issue.getMilestone();
            Vertex msVtx = saveMilestone(repo, m);
            createEdgeIfNotExist(issuenode, msVtx, EdgeType.MILESTONE);
        }
        setProperty(issuenode, PropertyName.NUMBER, issue.getNumber());
        // Fix for the v3 API always creating a pull request object
        if (issue.getPullRequest() != null && issue.getPullRequest().getId() != 0L) {
            PullRequest pr = issue.getPullRequest();
            Vertex prnode = savePullRequest(repo, pr);
            createEdgeIfNotExist(issuenode, prnode, EdgeType.PULLREQUEST);
        }
        setProperty(issuenode, PropertyName.STATE, issue.getState().toString());
        setProperty(issuenode, PropertyName.TITLE, issue.getTitle());
        setProperty(issuenode, PropertyName.UPDATED_AT, issue.getUpdatedAt());
        if (issue.getUser() != null) {
            setProperty(issuenode, PropertyName.USER, issue.getUser().getLogin());
            Vertex userNode = getOrCreateUser(issue.getUser());
            createEdgeIfNotExist(userNode, issuenode, EdgeType.ISSUEOWNER);
        }
        setProperty(issuenode, PropertyName.SYS_LAST_UPDATED, new Date());
        return issuenode;
    }

    public Vertex saveIssueComment(Comment comment) {
        return saveCommentHelper(comment, EdgeType.ISSUECOMMENTOWNER);
    }

    public void saveIssueComments(Repository repo,
            Issue issue,
            Collection<Comment> issueComments) {
        for (Comment comment : issueComments) {
            saveIssueComment(repo, issue, comment);
        }
    }

    private Vertex saveIssueComment(Repository repo,
            Issue issue,
            Comment comment) {
        // FIXME: this shouldn't be saveIssue, should just be a fetch
        Vertex issuenode = saveIssue(repo, issue);
        Vertex commentnode = this.getOrCreateComment(comment.getId());
        createEdgeIfNotExist(issuenode, commentnode, EdgeType.ISSUECOMMENT);
        setProperty(commentnode, PropertyName.BODY, comment.getBody());
        setProperty(commentnode, PropertyName.BODY_HTML, comment.getBodyHtml());
        setProperty(commentnode, PropertyName.BODY_TEXT, comment.getBodyText());
        setProperty(commentnode, PropertyName.CREATED_AT, comment.getCreatedAt());
        setProperty(commentnode, PropertyName.GITHUB_ID, comment.getId());
        setProperty(commentnode, PropertyName.URL, comment.getUrl());
        setProperty(commentnode, PropertyName.UPDATED_AT, comment.getUpdatedAt());
        if (comment.getUser() != null) {
            Vertex user = saveUser(comment.getUser());
            createEdgeIfNotExist(user, commentnode, EdgeType.ISSUECOMMENTOWNER);
        }
        return commentnode;
    }

    public void saveIssueEvents(Repository repo,
            Issue issue,
            Collection<IssueEvent> issueEvents) {
        Vertex issuenode = getOrCreateIssue(repo, issue);
        for (IssueEvent event : issueEvents) {
            saveIssueEvent(repo, issuenode, event);
        }
        setProperty(issuenode, PropertyName.SYS_EVENTS_ADDED, new Date());
    }

    private Vertex saveIssueEvent(Repository repo,
            Vertex issuenode,
            IssueEvent event) {
        Vertex eventnode = getOrCreateEvent(event.getId());
        createEdgeIfNotExist(issuenode, eventnode, EdgeType.ISSUEEVENT);
        if (event.getActor() != null) {
            Vertex usernode = saveUser(event.getActor());
            createEdgeIfNotExist(usernode, eventnode, EdgeType.ISSUEEVENTACTOR);
        }
        if (event.getCommitId() != null) {
            setProperty(eventnode, PropertyName.COMMIT_ID, event.getCommitId());
            Vertex commit = this.getOrCreateCommit(event.getCommitId());
            createEdgeIfNotExist(eventnode, commit, EdgeType.EVENTCOMMIT);
        }
        setProperty(eventnode, PropertyName.CREATED_AT, event.getCreatedAt());
        setProperty(eventnode, PropertyName.EVENT, event.getEvent());
        setProperty(eventnode, PropertyName.GITHUB_ID, event.getId());
        if (event.getIssue() != null) {
            Vertex altissuenode = saveIssue(repo, event.getIssue());
            createEdgeIfNotExist(eventnode, altissuenode, EdgeType.ISSUEALTEVENT);
        }
        setProperty(eventnode, PropertyName.URL, event.getUrl());
        return null;
    }

    private Vertex saveMilestone(Repository repo, Milestone m) {
        Vertex msnode = getOrCreateMilestone(repo.generateId() + ":" + m.getTitle());
        setProperty(msnode, PropertyName.CLOSED_ISSUES, m.getClosedIssues());
        setProperty(msnode, PropertyName.CREATED_AT, m.getCreatedAt());
        if (m.getCreator() != null) {
            Vertex u = saveUser(m.getCreator());
            createEdgeIfNotExist(msnode, u, EdgeType.CREATOR);
        }
        setProperty(msnode, PropertyName.DESCRIPTION, m.getDescription());
        setProperty(msnode, PropertyName.DUE_DATE, m.getDueOn());
        setProperty(msnode, PropertyName.NUMBER, m.getNumber());
        setProperty(msnode, PropertyName.OPEN_ISSUES, m.getOpenIssues());
        setProperty(msnode, PropertyName.STATE, m.getState());
        setProperty(msnode, PropertyName.UPDATED_AT, new Date());

        return msnode;
    }

    /**
     * In the v3 API organizations are just users with a type of "organization"
     * 
     * @param organization the owning organization
     * @param members a list of the organization members
     */
    public void saveOrganizationPublicMembers(
            User organization,
            Collection<User> members) {
        Vertex org = saveUser(organization);
        for (User user : members) {
            Vertex usernode = saveUser(user);
            createEdgeIfNotExist(usernode, org, EdgeType.ORGANIZATIONMEMBER);
            log.warn("adding {} to organization {}", user.getLogin(), organization.getLogin());
        }
    }
    
    
    protected Map<String, Vertex> saveOrganizationMembersHelper(String organization, List<User> owners, String edgetype) {
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
            Vertex reponode = saveRepository(repo);
            createEdgeIfNotExist(source, reponode, EdgeType.REPOOWNER);
            mapper.put(repo.generateId(), reponode);
        }
        return mapper;
    }

    public Map<String, Vertex> saveOrganizationTeams(String organization, List<Team> teams) {
        Vertex org = getOrCreateOrganization(organization);
        HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
        for (Team team: teams) {
            Vertex teamnode = saveTeam(team);
            createEdgeIfNotExist(org, teamnode, EdgeType.ORGANIZATIONTEAM);
            mapper.put(team.getName(), teamnode);
        }
        return mapper;
    }

    public Vertex savePullRequestComment(Comment comment) {
        return saveCommentHelper(comment, EdgeType.PULLREQUESTCOMMENTOWNER);
    }

    private Vertex savePullRequestMarker(PullRequestMarker head) {
        Vertex markernode = getOrCreatePullRequestMarker(head);
        setProperty(markernode, PropertyName.LABEL, head.getLabel());
        setProperty(markernode, PropertyName.SHA, head.getSha());
        setProperty(markernode, PropertyName.REF, head.getRef());
        User user = head.getUser();
        if (user != null) {
            Vertex usernode = saveUser(user);
            createEdgeIfNotExist(markernode, usernode, EdgeType.PULLREQUESTMARKERUSER);
        }
        Repository repo = head.getRepo();
        if (repo != null) {
            Vertex reponode = saveRepository(repo);
            createEdgeIfNotExist(markernode, reponode, EdgeType.REPOSITORY);
        }
        return markernode;
    }

//    public Vertex savePullRequestReviewComent(Comment comment) {
//        return saveCommentHelper(comment, EdgeType.PULLREQUESTREVIEWCOMMENT);
//    }
//
//    public Vertex savePullRequestReviewComment(PullRequestReviewComment comment) {
//        log.trace("savePullRequestReviewComment: enter");
//        String commentId = comment.getCommitId() + ":" + comment.getCreatedAt()==null?"UNKNOWNDATE":String.valueOf(comment.getCreatedAt().getTime()/1000L);
//        Vertex node = getOrCreatePullRequestReviewComment(commentId);
//        setProperty(node, PropertyName.BODY, comment.getBody());
//        setProperty(node, PropertyName.COMMIT_ID, comment.getCommitId());
//        setProperty(node, PropertyName.CREATED_AT, comment.getCreatedAt());
//        setProperty(node, PropertyName.DIFF_HUNK, comment.getDiffHunk());
//        setProperty(node, PropertyName.ORIGINAL_COMMIT_ID, comment.getOriginalCommitId());
//        setProperty(node, PropertyName.PATH, comment.getPath());
//        setProperty(node, PropertyName.POSITION, comment.getPosition());
//        setProperty(node, PropertyName.UPDATED_AT, comment.getUpdatedAt());
//        if (comment.getUser() != null) {
//            Vertex user = saveUser(comment.getUser());
//            createEdgeIfNotExist(user, node, EdgeType.PULLREQUESTREVIEWCOMMENTOWNER);
//        }
//        if (comment.getCommitId() != null) {
//            Vertex commit = getOrCreateCommit(comment.getCommitId());
//            createEdgeIfNotExist(node, commit, EdgeType.PULLREQUESTREVIEWCOMMENTCOMMIT);
//        }
//        if (comment.getOriginalCommitId() != null) {
//            Vertex commit = getOrCreateCommit(comment.getOriginalCommitId());
//            createEdgeIfNotExist(node, commit, EdgeType.PULLREQUESTREVIEWCOMMENTORIGINALCOMMIT);		
//        }
//        log.trace("savePullRequestReviewComment: exit");
//        return node;
//    }

    /**
     * {@link #saveRepository(Repository)} modified to V3 api
     * @param repo
     * @return
     */
    public Vertex saveRepository(Repository repo) {
        Vertex node = getOrCreateRepository(repo.generateId());

        setProperty(node, PropertyName.FULLNAME, repo.generateId());
        setProperty(node, PropertyName.CLONE_URL, repo.getCloneUrl());
        setProperty(node, PropertyName.CREATED_AT, repo.getCreatedAt());
        setProperty(node, PropertyName.DESCRIPTION, repo.getDescription());
        setProperty(node, PropertyName.FORKS, repo.getForks());
        setProperty(node, PropertyName.GIT_URL, repo.getGitUrl());
        setProperty(node, PropertyName.HOMEPAGE, repo.getHomepage());
        setProperty(node, PropertyName.HTML_URL, repo.getHtmlUrl());
        setProperty(node, PropertyName.GITHUB_ID, repo.getId());
        setProperty(node, PropertyName.LANGUAGE, repo.getLanguage());
        setProperty(node, PropertyName.MASTER_BRANCH, repo.getMasterBranch());
        setProperty(node, PropertyName.MIRROR_URL, repo.getMirrorUrl());
        setProperty(node, PropertyName.NAME, repo.getName());
        setProperty(node, PropertyName.OPEN_ISSUES, repo.getOpenIssues());
        User user = repo.getOwner();
        if (user != null) {
            Vertex owner = saveUser(user);
            createEdgeIfNotExist(owner, node, EdgeType.REPOOWNER);
        }
        Repository altrepo = repo.getParent();
        if (altrepo != null) {
            Vertex parentNode = saveRepository(altrepo);
            createEdgeIfNotExist(node, parentNode, EdgeType.REPOPARENT);
        }
        setProperty(node, PropertyName.PUSHED_AT, repo.getPushedAt()); 
        setProperty(node, PropertyName.SIZE, repo.getSize());
        altrepo = repo.getSource();
        if (altrepo != null) {
            Vertex sourceNode = saveRepository(altrepo);
            createEdgeIfNotExist(node, sourceNode, EdgeType.REPOSOURCE);
        }
        setProperty(node, PropertyName.SSH_URL, repo.getSshUrl());
        setProperty(node, PropertyName.SVN_URL, repo.getSvnUrl());
        setProperty(node, PropertyName.UPDATED_AT, repo.getUpdatedAt());
        setProperty(node, PropertyName.URL, repo.getUrl());
        setProperty(node, PropertyName.WATCHERS, repo.getWatchers());

        setProperty(node, PropertyName.IS_FORK, repo.isFork());
        setProperty(node, PropertyName.HAS_DOWNLOADS, repo.isHasDownloads());
        setProperty(node, PropertyName.HAS_ISSUES, repo.isHasIssues());
        setProperty(node, PropertyName.HAS_WIKI, repo.isHasWiki());
        setProperty(node, PropertyName.IS_PRIVATE, repo.isPrivate());
        
        // Former v2 Properties not in v3 API
        // getPermission
        //		setProperty(node, PropertyName.ACTIONS, repo.getActions());
        //		setProperty(node, PropertyName.FOLLOWERS, repo.getFollowers());
        //		setProperty(node, PropertyName.ORGANIZATION, repo.getOrganization());
        //		setProperty(node, PropertyName.SCORE, repo.getScore());
        //		setProperty(node, PropertyName.REPO_TYPE, repo.getType()); // note name change
        //		setProperty(node, PropertyName.USERNAME, repo.getUsername());

        setProperty(node, PropertyName.SYS_LAST_UPDATED, new Date());
        return node;
    }

    /**
     * Given a repository and a list of collaborators, saves and links the collaborators
     * 
     * @param repo repository of interest
     * @param collaborators list of collaborators to save
     * @return a map of the User object to Vertex in database
     */
    public Map<User, Vertex> saveRepositoryCollaborators(
            Repository repo,
            List<User> collaborators) {
        HashMap<User, Vertex> mapper = new HashMap<User, Vertex>();
        Vertex repoVtx = getOrCreateRepository(repo);
        if (collaborators == null) {
            log.warn("saveRepositoryCollaborators - collaborators are null");
        }
        for (User user : collaborators) {
            Vertex userVtx = getOrCreateUser(user);
            createEdgeIfNotExist(null, repoVtx, userVtx, EdgeType.REPOCOLLABORATOR);
            mapper.put(user, userVtx);
        }
        return mapper;
    }

    /**
     * Given a repository and a list of collaborators, saves and links the collaborators
     * 
     * @param repo repository of interest
     * @param collaborators list of collaborators to save
     * @return a map of the User object to Vertex in database
     */
    public Map<Contributor, Vertex> saveRepositoryContributors(
            Repository repo,
            List<Contributor> contributors) {
        HashMap<Contributor, Vertex> mapper = new HashMap<Contributor, Vertex>();
        Vertex repoVtx = getOrCreateRepository(repo);
        if (contributors == null) {
            log.warn("saveRepositoryCollaborators - collaborators are null");
        }
        for (Contributor contributor : contributors) {
            Vertex contributorVtx = saveContributor(contributor);
            Edge contributorEdge = createEdgeIfNotExist(null, repoVtx, contributorVtx, EdgeType.REPOCONTRIBUTOR);
            setProperty(contributorEdge, PropertyName.CONTRIBUTIONS, contributor.getContributions());
            mapper.put(contributor, contributorVtx);
        }
        return mapper;
    }

    /**
     * Saves a contributor from the github v3 api
     * 
     * A contributor is similar to a user, but has a little bit less information associated with it
     * 
     * @param contributor
     * @return
     */
    private Vertex saveContributor(Contributor contributor) {
        Vertex contributorVtx = getOrCreateUser(contributor.getLogin());
        setProperty(contributorVtx, PropertyName.GRAVATAR_ID, contributor.getAvatarUrl());
        log.info("Saving avatar URL as: {}", contributor.getAvatarUrl());
        setProperty(contributorVtx, PropertyName.NAME, contributor.getName());
        setProperty(contributorVtx, PropertyName.GITHUB_ID, contributor.getId());
        // setProperty(contributorVtx, PropertyName.contributor.getType();
        setProperty(contributorVtx, PropertyName.URL, contributor.getUrl());
        return contributorVtx;
    }

    public Map<String,Vertex> saveRepositoryContributors(String reponame, List<User> contributors) {
        HashMap<String, Vertex> mapper = new HashMap<String, Vertex>();
        Vertex repo = getOrCreateRepository(reponame);
        if (contributors == null) {
            log.warn("saveRepositoryContributors contributors are null");
            return mapper;
        }
        for (User user : contributors) {
            Vertex usernode = saveUser(user);
            createEdgeIfNotExist(null, repo, usernode, EdgeType.REPOCONTRIBUTOR);
            mapper.put(user.getLogin(), usernode);
        }
        return mapper;
    }

    /**
     * Maps all of the forks to their repository
     * 
     * Edge direction goes: SOURCE --[EdgeType.REPOFORK]--> FORK
     * @param repo
     * @param forks
     * @return
     */
    public Map<Repository,Vertex> saveRepositoryForks(
            Repository repo,
            List<Repository> forks) {
        Vertex repoVtx = getOrCreateRepository(repo);
        HashMap<Repository,Vertex> mapper = new HashMap<Repository,Vertex>();
        if (forks == null) {
            log.warn("saveRepositoryForks forks are null");
            return mapper;
        }
        for (Repository fork : forks) {
            Vertex forkVtx = saveRepository(fork);
            createEdgeIfNotExist(null, repoVtx, forkVtx, EdgeType.REPOFORK);
            mapper.put(fork, forkVtx);
        }
        return mapper;
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
        setProperty(issuenode, PropertyName.SYS_COMMENTS_ADDED, new Date());
        return mapper;
    }

    /**
     * {@link #saveRepositoryIssueComments(String, Issue, Collection)} updated for v3 api
     * @param proj
     * @param issue
     * @param issueComments
     */
    public Map<Long, Vertex> saveRepositoryIssueComments(String project,
            Issue issue,
            List<Comment> comments) {
        Vertex issuenode = getOrCreateIssue(project, issue);
        HashMap<Long,Vertex> mapper = new HashMap<Long,Vertex>();
        for (Comment comment : comments) {
            Vertex commentnode = saveIssueComment(comment);
            createEdgeIfNotExist(issuenode, commentnode, EdgeType.ISSUECOMMENT);
            mapper.put(new Long(comment.getId()), commentnode);

        }
        setProperty(issuenode, PropertyName.SYS_COMMENTS_ADDED, new Date());
        return mapper;
    }


    /**
     * {@link #saveRepositoryIssues(String, Collection)} modified for v3 api
     * 
     * @param repo
     * @param issues
     * @return
     */
    public Map<String, Vertex> saveRepositoryIssues(
            Repository repo,
            Collection<Issue> issues) {
        Vertex proj = getOrCreateRepository(repo);
        HashMap<String,Vertex> mapper = new HashMap<String,Vertex>();
        for (Issue issue : issues) {
            String issueId = repo.generateId() + ":" + issue.getNumber();
            Vertex issuenode = saveIssue(repo, issue);
            createEdgeIfNotExist(null, proj, issuenode, EdgeType.ISSUE);
            mapper.put(issueId, issuenode);
        }
        return mapper;
    }

    private Vertex savePullRequest(
            Repository repo,
            PullRequest request) {
        return savePullRequest(repo, request, false);
    }

    /**
     * {@link #savePullRequest(String, PullRequest, boolean)} updated for v3 api
     * @param repo
     * @param request
     * @param full
     * @return
     */
    public Vertex savePullRequest(
            Repository repo,
            PullRequest request, boolean full) {
        log.trace("Saving pull request {}", request.getNumber());
        log.trace(request.toString());
        String reponame = repo.generateId();

        Vertex reponode = getOrCreateRepository(reponame);
        Vertex pullnode = getOrCreatePullRequest(reponame + ":" + request.getNumber());
        // getBase()
        
        setProperty(pullnode, PropertyName.BODY, request.getBody());
        setProperty(pullnode, PropertyName.BODY_HTML, request.getBodyHtml());
        setProperty(pullnode, PropertyName.BODY_TEXT, request.getBodyText());
        setProperty(pullnode, PropertyName.COMMENTS, request.getComments());
        setProperty(pullnode, PropertyName.COMMITS, request.getCommits());
        setProperty(pullnode, PropertyName.CREATED_AT, request.getCreatedAt());
        setProperty(pullnode, PropertyName.CLOSED_AT, request.getClosedAt());
        setProperty(pullnode, PropertyName.DIFF_URL, request.getDiffUrl());
        setProperty(pullnode, PropertyName.PATCH_URL, request.getPatchUrl());
        setProperty(pullnode, PropertyName.ADDITIONS, request.getAdditions());
        setProperty(pullnode, PropertyName.DELETIONS, request.getDeletions());
        setProperty(pullnode, PropertyName.UPDATED_AT, request.getUpdatedAt());
        setProperty(pullnode, PropertyName.UPDATED_AT, request.getUpdatedAt());
        setProperty(pullnode, PropertyName.URL, request.getUrl());

        // FIXME: it does not appear that getDiscussions exists in the v3 api
        //		for (Discussion discussion : request.getDiscussion()) {
        //			Vertex discussionnode = saveDiscussion(discussion);
        //			log.trace("Created discussion node");
        //			createEdgeIfNotExist(null, pullnode, discussionnode, EdgeType.PULLREQUESTDISCUSSION);
        //		}

        setProperty(pullnode, PropertyName.HTML_URL, request.getHtmlUrl());
        setProperty(pullnode, PropertyName.ISSUE_CREATED_AT, request.getCreatedAt());
        setProperty(pullnode, PropertyName.ISSUE_UPDATED_AT, request.getUpdatedAt());
        setProperty(pullnode, PropertyName.GITHUB_ID, request.getId());
        setProperty(pullnode, PropertyName.ISSUE_URL, request.getIssueUrl());
        setProperty(pullnode, PropertyName.MERGED_AT, request.getMergedAt());

        // FIXME: it does not appear that getIssueUser exists in the v3 api
        //		if (request.getIssueUser() != null) {
        //			Vertex usernode = saveUser(request.getIssueUser());
        //			createEdgeIfNotExist(usernode, pullnode, EdgeType.PULLREQUESTISSUEUSER);
        //		}

        // FIXME: it does not appear that getLabels exists in the v3 api
        //		for (Label label : request.getLabels()) {
        //			Vertex labelnode = getOrCreateIssueLabel(label);
        //			createEdgeIfNotExist(null, pullnode, labelnode, EdgeType.PULLREQUESTLABEL);
        //		}

        setProperty(pullnode, PropertyName.NUMBER, request.getNumber());
        setProperty(pullnode, PropertyName.PATCH_URL, request.getPatchUrl());
        // FIXME: it does not appear that getPosition exists in the v3 api
        //		setProperty(pullnode, PropertyName.POSITION, request.getPosition());
        if (request.getState() != null) {
            setProperty(pullnode, PropertyName.STATE, request.getState().toString());
        }
        setProperty(pullnode, PropertyName.TITLE, request.getTitle());
        if (request.getUser() != null) {
            Vertex usernode = saveUser(request.getUser());
            createEdgeIfNotExist(usernode, pullnode, EdgeType.PULLREQUESTOWNER);
        }

        if (request.getMergedBy() != null) {
            Vertex usernode = saveUser(request.getMergedBy());
            createEdgeIfNotExist(pullnode, usernode, EdgeType.PULLREQUESTMERGEDBY);
        }
        setProperty(pullnode, PropertyName.MERGED_AT, request.getMergedAt());

        // FIXME: it does not appear that getVotes exists in the v3 api
        //		setProperty(pullnode, PropertyName.VOTES, request.getVotes());
        createEdgeIfNotExist(reponode, pullnode, EdgeType.PULLREQUEST);

        PullRequestMarker head = request.getHead();
        if (head != null) {
            Vertex headnode = savePullRequestMarker(head);
            createEdgeIfNotExist(pullnode, headnode, EdgeType.PULLREQUESTHEAD);
        }
        PullRequestMarker base = request.getBase();
        if (base != null) {
            Vertex basenode = savePullRequestMarker(base);
            createEdgeIfNotExist(pullnode, basenode, EdgeType.PULLREQUESTBASE);
        }

        if (full == true) {
            setProperty(pullnode, PropertyName.SYS_DISCUSSIONS_ADDED.toString(), new Date());
            setProperty(pullnode, PropertyName.SYS_UPDATE_COMPLETE.toString(), new Date());
        }

        return pullnode;
    }

    /**
     * {@link #savePullRequests(String, Collection)} updated for v3 api
     * 
     * @param repo
     * @param requests3
     */
    public void savePullRequests(
            Repository repo,
            Collection<PullRequest> requests3) {
        for (PullRequest request : requests3) {
            savePullRequest(repo, request);
        }
    }

    /**
     * Given a repository, saves the users that are watching that repository
     * 
     * @param project the name of the repository, for example pridkett/github-java-sdk
     * @param users a list of the usernames to link
     * @return a map of the username to their representative vertices
     * @deprecated
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


    /**
     * Saves users and maps them to watching a repository
     * 
     * @param repo repository of interest
     * @param watchers a list of users watching the repository
     * @return a mapping of the users to the repository
     */
    public Map<User, Vertex> saveRepositoryWatchers(
            Repository repo,
            List<User> watchers) {
        Vertex repoVtx = getOrCreateRepository(repo);
        HashMap<User,Vertex> mapper= new HashMap<User,Vertex>();
        for (User user : watchers) {
            Vertex userVtx = getOrCreateUser(user);
            createEdgeIfNotExist(null, userVtx, repoVtx, EdgeType.REPOWATCHED);
            mapper.put(user, userVtx);
        }
        return mapper;
    }
    
    public Vertex saveTeam(Team team) {
        Vertex node = getOrCreateTeam(team);
        setProperty(node, PropertyName.NAME, team.getName());
        setProperty(node, PropertyName.PERMISSION, team.getPermission());
        setProperty(node, PropertyName.GITHUB_ID, team.getId());
        setProperty(node, PropertyName.URL, team.getUrl());
        setProperty(node, PropertyName.MEMBERS, team.getMembersCount());
        setProperty(node, PropertyName.REPOSITORIES, team.getReposCount());
        return node;
    }

    public Map<User,Vertex> saveTeamMembers(Team team, List<User> users) {
        Vertex teamnode = getOrCreateTeam(team);
        HashMap<User,Vertex> mapper = new HashMap<User,Vertex>();
        for (User user : users) {
            Vertex usernode = saveUser(user);
            createEdgeIfNotExist(usernode, teamnode, EdgeType.TEAMMEMBER);
            mapper.put(user, usernode);
        }
        return mapper;
    }

    public Map<Repository,Vertex> saveTeamRepositories(Team team,
            List<Repository> repos) {
        Vertex teamnode = getOrCreateTeam(team);
        HashMap<Repository,Vertex> mapper = new HashMap<Repository,Vertex>();
        for (Repository repo : repos) {
            Vertex reponode = saveRepository(repo);
            createEdgeIfNotExist(teamnode, reponode, EdgeType.REPOOWNER);
            mapper.put(repo, reponode);
        }
        return mapper;
    }

    private Vertex saveUser(CommitUser user) {
        String sName = user.getName();
        String sEmail = user.getEmail();
        Vertex gitUser = getOrCreateGitUser( sName, sEmail );
        Vertex vName = getOrCreateName( sName );
        Vertex vEmail = getOrCreateEmail( sEmail );
        createEdgeIfNotExist( gitUser, vName, EdgeType.NAME );
        createEdgeIfNotExist( gitUser, vEmail, EdgeType.EMAIL );
        return gitUser;
    }

    public Vertex saveUser(User user, boolean overwrite) {
        Vertex node = getOrCreateUser(user.getLogin());
        log.debug("Saving User: {}", user.toString());

        setProperty(node, PropertyName.BIOGRAPHY, user.getBiography());
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
            setProperty(node, PropertyName.FOLLOWERS, user.getFollowers());
            setProperty(node, PropertyName.FOLLOWING, user.getFollowing());
            setProperty(node, PropertyName.OWNED_PRIVATE_REPO_COUNT, user.getOwnedPrivateRepos());
            setProperty(node, PropertyName.PRIVATE_GIST_COUNT, user.getPrivateGists());
            setProperty(node, PropertyName.PUBLIC_GIST_COUNT, user.getPublicGists());
            setProperty(node, PropertyName.PUBLIC_REPO_COUNT, user.getPublicRepos());
            // FIXME: I don't think there is a getScore method in the v3 api
            //			setProperty(node, PropertyName.SCORE, user.getScore());
            setProperty(node, PropertyName.TOTAL_PRIVATE_REPO_COUNT, user.getTotalPrivateRepos());
            setProperty(node, PropertyName.SYS_LAST_FULL_UPDATE.toString(), new Date());
        }
        setProperty(node, PropertyName.URL, user.getUrl());
        setProperty(node, PropertyName.FULLNAME, user.getName());
        setProperty(node, PropertyName.GRAVATAR_ID, user.getAvatarUrl());
        setProperty(node, PropertyName.GITHUB_ID, user.getId()); // note name change
        setProperty(node, PropertyName.LOCATION, user.getLocation());
        setProperty(node, PropertyName.LOGIN, user.getLogin());
        setProperty(node, PropertyName.NAME, user.getName());
        setProperty(node, PropertyName.USER_TYPE, user.getType());
        setProperty(node, PropertyName.SYS_LAST_UPDATED.toString(), new Date());
        // getPermission
        // getPlan

        // FIXME: I don't think there is a getUsername method in the v3 api
        //		setProperty(node, PropertyName.USERNAME, user.getUsername());
        return node;
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

    public Map<User, Vertex> saveUserFollowers(String user,
            List<User> followers) {
        return saveUserFollowersFollowing(user, followers, EdgeType.FOLLOWER);
    }

    public Map<User, Vertex> saveUserFollowersFollowing(String sourceuser, List<User> users, String edgetype) {
        Vertex source = getOrCreateUser(sourceuser);
        HashMap<User, Vertex> mapper= new HashMap<User, Vertex>();
        for (User user : users) {
            Vertex node = getOrCreateUser(user);
            createEdgeIfNotExist(null, source, node, edgetype);
            mapper.put(user, node);
        }
        return mapper;
    }

    public Map<User, Vertex> saveUserFollowing(String sourceuser,
            List<User> users) {
        return saveUserFollowersFollowing(sourceuser, users, EdgeType.FOLLOWING);
    }

    public Map<Gist, Vertex> saveUserGists(String user, List<Gist> gists) {
        Vertex usernode = getOrCreateUser(user);
        HashMap<Gist, Vertex> mapper = new HashMap<Gist, Vertex>();
        for (Gist gist : gists) {
            Vertex gistnode = saveGist(gist);
            createEdgeIfNotExist(usernode, gistnode, EdgeType.GISTOWNER);
            mapper.put(gist, gistnode);
        }
        return mapper;
    }

    public Map<Repository, Vertex> saveUserRepositories(String user,
            List<Repository> repos) {
        return saveUserRepositoriesHelper(user, repos, EdgeType.REPOOWNER);
    }

    private Map<Repository, Vertex> saveUserRepositoriesHelper(String user,
            List<Repository> repositories,String edgetype) {
        
        Vertex source = getOrCreateUser(user);
        HashMap<Repository,Vertex> mapper = new HashMap<Repository, Vertex>();
        for (Repository repo : repositories) {
            Vertex reponode = saveRepository(repo);
            createEdgeIfNotExist(null, source, reponode, edgetype);
            mapper.put(repo, reponode);
        }
        return mapper;
    }

    public Map<Repository, Vertex> saveUserWatchedRepositories(String user,
            List<Repository> watchedRepos) {
        return saveUserRepositoriesHelper(user, watchedRepos, EdgeType.REPOWATCHED);
    }

}
