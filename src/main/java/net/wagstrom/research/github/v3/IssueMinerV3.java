/*
 * Copyright (c) 2011-2012 IBM Corporation
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
package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IssueMinerV3 extends AbstractMiner {
    private static final String ISSUES_DISABLED = "Issues are disabled for this repo";
    private IssueService service;

    private Logger log = LoggerFactory.getLogger(IssueMinerV3.class); // NOPMD

    private IssueMinerV3() {
        super();
    }

    public IssueMinerV3(final IssueService service) {
        this();
        this.service = service;
    }

    public IssueMinerV3(final IGitHubClient ghc) {
        this();
        service = new IssueService(ghc);
    }

    public Collection<Issue> getIssues(final String username, final String reponame, final String state) {
        HashMap<String, String> params = new HashMap<String, String>();
        Collection<Issue> issues = null;
        params.put(IssueService.FILTER_STATE, state);
        try {
            issues = service.getIssues(username, reponame, params);
        } catch (RequestException r) {
            if (r.getError().getMessage().equals(ISSUES_DISABLED)) {
                log.warn("Issues disabled for repository {}/{}", username, reponame);
            }
            log.error("Request exception in getIssues {}/{}", new Object[]{username, reponame, r});
            log.warn("Message: {}", r.getError().getMessage());
        } catch (IOException e) {
            log.error("IOException in getIssues {}/{}", new Object[]{username, reponame, e});
        } catch (NullPointerException e) {
            log.error("NullPointerException in getIssues {}/{}", new Object[]{username, reponame, e});
        }
        return issues;
    }

    public Collection<Issue> getOpenIssues(final String username, final String reponame) {
        return getIssues(username, reponame, IssueService.STATE_OPEN);
    }

    public Collection<Issue> getClosedIssues(final String username, final String reponame) {
        return getIssues(username, reponame, IssueService.STATE_CLOSED);
    }

    public Collection<Issue> getAllIssues(final String username, final String reponame) {
        Collection<Issue> openIssues = getOpenIssues(username, reponame);
        Collection<Issue> closedIssues = getClosedIssues(username, reponame);
        // simple hack to check if openIssues returned a null set
        if (openIssues == null) {
            // if no open issues, just return the closed ones
            openIssues = closedIssues;
        } else {
            openIssues.addAll(closedIssues);
        }
        return openIssues;
    }

    public Issue getIssue(final String username, final String reponame, final int issueId) {
        Issue issue = null;
        try {
            issue = service.getIssue(username, reponame, issueId);
        } catch (IOException e) {
            log.error("IO Exception Fetching issue {}/{}:{}", new Object[]{username, reponame, issueId, e});
        }
        return issue;
    }

    public List<Comment> getIssueComments(final IRepositoryIdProvider repo, final Issue issue) {
        return getComments(repo, issue.getNumber());
    }
    
    public List<Comment> getPullRequestComments(final IRepositoryIdProvider repo, final PullRequest pullrequest) {
        return getComments(repo, pullrequest.getNumber());
    }
    
    protected List<Comment> getComments(final IRepositoryIdProvider repo, final int issueId) {
        List<Comment> comments = null;
        try {
            comments = service.getComments(repo, issueId);
        } catch (IOException e) {
            log.error("Exception fetching comments for issue/pullrequest {}:{}", new Object[]{repo.generateId(), issueId, e});
        }
        return comments;
    }

    public Collection<IssueEvent> getIssueEvents(final IRepositoryIdProvider repo, final Issue issue) {
        Collection<IssueEvent> events = null;
        try {
            events = service.getIssueEvents(repo, issue);
        } catch (IOException e) {
            log.error("Exception fetching events for issue {}:{}", new Object[]{repo.generateId(), issue.getNumber(), e});
        }
        return events;
    }
}
