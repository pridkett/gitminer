package net.wagstrom.research.github;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Issue.State;
import com.github.api.v2.schema.PullRequest;
import com.github.api.v2.services.PullRequestService;

public class PullMiner {
	private PullRequestService service = null;
	private Logger log;
	
	public PullMiner(PullRequestService service) {
		this.service = service;
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	public Collection<PullRequest> getOpenPullRequests(String username, String reponame) {
		log.trace("Retrieving open pull requests for project: {}/{}", username, reponame);
		List<PullRequest> requests = service.getPullRequests(username, reponame, State.OPEN);
		log.debug("Retrieved open pull requests for project: {}/{} number: {}", new Object[]{username, reponame, requests==null?"null":requests.size()});
		return requests;
	}

	public Collection<PullRequest> getClosedPullRequests(String username, String reponame) {
		log.trace("Retrieving closed pull requests for project: {}/{}", username, reponame);
		List<PullRequest> requests = service.getPullRequests(username, reponame, State.CLOSED);
		log.debug("Retrieved closed pull requests for project: {}/{} number: {}", new Object[]{username, reponame, requests==null?"null":requests.size()});
		return requests;
	}
	
	public Collection<PullRequest> getAllPullRequests(String username, String reponame) {
		Collection<PullRequest> requests = getOpenPullRequests(username, reponame);
		requests.addAll(getClosedPullRequests(username, reponame));
		return requests;
	}
	
	public PullRequest getPullRequest(String username, String reponame, int issueid) {
		log.trace("Retrieving pull request: {}/{}:{}", new Object[]{username, reponame, issueid});
		PullRequest req = service.getPullRequest(username, reponame, issueid);
		log.debug("Retrieved pull request {}/{}:{}", new Object[]{username, reponame, issueid});
		return req;
	}
	
}
