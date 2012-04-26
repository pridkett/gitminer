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
import java.util.List;

import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullMinerV3 extends AbstractMiner {
    private final PullRequestService service;

    private static final Logger log = LoggerFactory.getLogger(PullMinerV3.class); // NOPMD

    public PullMinerV3(final IGitHubClient ghc) {
        super();
        service = new PullRequestService(ghc);
    }

    public Collection<PullRequest> getOpenPullRequests(final IRepositoryIdProvider repository) {
        Collection<PullRequest> pullrequests = null;
        try {
            pullrequests = service.getPullRequests(repository, IssueService.STATE_OPEN);
        } catch (IOException e) {
            log.error("IOException in getOpenPullRequests {} {}", new Object[]{repository.generateId(), e});
        }
        return pullrequests;
    }

    public Collection<PullRequest> getClosedPullRequests(final IRepositoryIdProvider repository) {
        Collection<PullRequest> pullrequests = null;
        try {
            pullrequests = service.getPullRequests(repository, IssueService.STATE_CLOSED);
        } catch (IOException e) {
            log.error("IOException in getOpenPullRequests {} {}", new Object[]{repository.generateId(), e});
        }
        return pullrequests;
    }

    public Collection<PullRequest> getAllPullRequests(final IRepositoryIdProvider repository) {
        Collection<PullRequest> openIssues = getOpenPullRequests(repository);
        Collection<PullRequest> closedIssues = getClosedPullRequests(repository);
        // simple hack to check if openIssues returned a null set
        if (openIssues == null) {
            openIssues = closedIssues;
        } else {
            openIssues.addAll(closedIssues);
        }
        return openIssues;
    }

    public PullRequest getPullRequest(final IRepositoryIdProvider repository, final int pullrequestId) {
        PullRequest pullrequest = null;
        try {
            pullrequest = service.getPullRequest(repository, pullrequestId);
        } catch (IOException e) {
            log.error("IO Exception fetching PullRequest {}:{}", new Object[]{repository.generateId(), pullrequestId,  e});
        }
        return pullrequest;
    }

    public List<CommitComment> getComments(final IRepositoryIdProvider repository, final int pullrequestId) {
        List<CommitComment> comments = null;
        try {
            comments = service.getComments(repository, pullrequestId);
        } catch (IOException e) {
            log.error("IO Exception fetching comments {}:{}", new Object[]{repository.generateId(), pullrequestId,  e});
        }
        return comments;
    }

}
