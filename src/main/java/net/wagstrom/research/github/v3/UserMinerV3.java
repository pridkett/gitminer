package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMinerV3 extends V3Miner {
    private UserService service;
    
    private Logger log;

    public UserMinerV3(IGitHubClient ghc) {
        service = new UserService(ghc);
        log = LoggerFactory.getLogger(UserMinerV3.class);
    }

    
    public User getUser(String login) {
        try {
            return service.getUser(login);
        } catch (IOException e) {
            log.error("IOException in getting user {} {}", login, e);
            return null;
        }
    }   
}
