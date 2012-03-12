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
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationMinerV3 extends V3Miner {
    private OrganizationService service;
    
    private Logger log;

    public OrganizationMinerV3(IGitHubClient ghc) {
        service = new OrganizationService(ghc);
        log = LoggerFactory.getLogger(OrganizationMinerV3.class);
    }

   
    public Collection<User> getOrganizationMembers(String organization) {
        try {
            return service.getMembers(organization);
        } catch (IOException e) {
            log.error("IOException getting organization members for {}: {}", organization, e);
            return null;
        }
    }
    
    public Collection<User> getPublicOrganizationMembers(String organization) {
        try {
            return service.getPublicMembers(organization);
        } catch (IOException e) {
            log.error("IOException getting public organization members for {}: {}", organization, e);
            return null;
        }
    }
    
    public User getOrganization(String organization) {
        try {
            return service.getOrganization(organization);
        } catch (IOException e) {
            log.error("IOException getting organization {}: {}", organization, e);
            return null;
        }
    }
}
