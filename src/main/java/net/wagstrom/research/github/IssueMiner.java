package net.wagstrom.research.github;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Comment;
import com.github.api.v2.schema.Issue;
import com.github.api.v2.schema.Issue.State;
import com.github.api.v2.services.IssueService;

public class IssueMiner {
	private IssueService service = null;
	private Logger log;
	
	public IssueMiner(IssueService service) {
		this.service = service;
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	public List<Issue> getOpenIssues(String username, String reponame) {
		List<Issue> issues = service.getIssues(username, reponame, State.OPEN);
		log.debug("Retrieved open issues for project: {}/{} number: {}", new Object[]{username, reponame, issues.size()});
		return issues;
	}
	
	public List<Issue> getClosedIssues(String username, String reponame) {
		List<Issue> issues = service.getIssues(username, reponame, State.CLOSED);
		log.debug("Retrieved closed issues for project: {}/{} number: {}", new Object[]{username, reponame, issues.size()});
		return issues;
	}
	
	public Collection<Issue> getAllIssues(String username, String reponame) {
		HashSet<Issue> issues = new HashSet<Issue>(getOpenIssues(username, reponame));
		issues.addAll(getClosedIssues(username, reponame));
		log.debug("Retrieved all issues for project: {}/{} number: {}", new Object[]{username, reponame, issues.size()});
		return issues;
	}
	
	public List<Comment> getIssueComments(String username, String reponame, int issueid) {
		List<Comment> comments = service.getIssueComments(username, reponame, issueid);
		log.debug("Retrived issues for project: {}/{} issue: {} number: {}", new Object[]{username, reponame, issueid, comments.size()});
		return comments;
	}
}
