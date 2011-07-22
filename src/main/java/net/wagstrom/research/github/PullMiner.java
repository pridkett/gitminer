package net.wagstrom.research.github;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.PullRequest;
import com.github.api.v2.services.PullRequestService;

public class PullMiner {
	private PullRequestService service = null;
	private Logger log;
	
	public PullMiner(PullRequestService service) {
		this.service = service;
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	public Collection<PullRequest> getPullRequests(String username, String reponame) {
		List<PullRequest> requests = service.getPullRequests(username, reponame);
		log.debug("Retrieved all pull requests for project: {}/{} number: {}", new Object[]{username, reponame, requests.size()});
		return requests;
	}
	
	public PullRequest getPullRequest(String username, String reponame, int issueid) {
		PullRequest req = service.getPullRequest(username, reponame, issueid);
		log.debug("Retrieved pull request {}/{}:{}", new Object[]{username, reponame, issueid});
		return req;
	}
	
}
