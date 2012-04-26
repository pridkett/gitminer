package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationMinerV3 extends AbstractMiner {
    private OrganizationService service;
    
    private static final Logger log = LoggerFactory.getLogger(OrganizationMinerV3.class); // NOPMD

    public OrganizationMinerV3(IGitHubClient ghc) {
        service = new OrganizationService(ghc);
    }

   
    public Collection<User> getMembers(String organization) {
        try {
            return service.getMembers(organization);
        } catch (IOException e) {
            log.error("IOException getting organization members for {}: {}", organization, e);
            return null;
        }
    }
    
    public Collection<User> getPublicMembers(String organization) {
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
