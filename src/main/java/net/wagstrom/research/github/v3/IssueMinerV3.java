package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IssueMinerV3 {
	private IssueService service;
	
	private Logger log;
	
	public IssueMinerV3(GitHubClient ghc) {
		service = new IssueService(ghc);
		log = LoggerFactory.getLogger(IssueMinerV3.class);
	}
	
	public Collection<Issue> getOpenIssues(String username, String reponame) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(IssueService.FILTER_STATE, IssueService.STATE_OPEN);
		try {
			return service.getIssues(username, reponame, params);
		} catch (IOException e) {
			log.error("IOException in getOpenIssues {} {}", new Object[]{username, reponame, e});
			return null;
		}
	}
	
	public Collection<Issue> getClosedIssues(String username, String reponame) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(IssueService.FILTER_STATE, IssueService.STATE_CLOSED);
		try {
			return service.getIssues(username, reponame, params);
		} catch (IOException e) {
			log.error("IOException in getOpenIssues {} {}", new Object[]{username, reponame, e});
			return null;
		}
	}
	
	public Collection<Issue> getAllIssues(String username, String reponame) {
		Collection<Issue> openIssues = getOpenIssues(username, reponame);
		Collection<Issue> closedIssues = getClosedIssues(username, reponame);
		// simple hack to check if openIssues returned a null set
		if (openIssues != null) {
			openIssues.addAll(closedIssues);
			return openIssues;
		} else {
			return closedIssues;
		}
	}
	
	public Issue getIssue(String username, String reponame, int issueId) {
		try {
			return service.getIssue(username, reponame, issueId);
		} catch (IOException e) {
			log.error("IO Exception Fetching issue {}/{}:{}", new Object[]{username, reponame, issueId, e});
			return null;
		}
	}
	
	public List<Comment> getIssueComments(String username, String reponame, int issueId) {
		try {
			return service.getComments(username, reponame, issueId);
		} catch (IOException e) {
			log.error("IO Exception fetching comments on issue {}/{}:{}", new Object[]{username, reponame, issueId, e});
			return null;
		}
	}
}
