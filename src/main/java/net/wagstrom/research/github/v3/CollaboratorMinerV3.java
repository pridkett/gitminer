package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollaboratorMinerV3 extends V3Miner {
    private CollaboratorService service;
    
    private Logger log;

    public CollaboratorMinerV3(IGitHubClient ghc) {
        service = new CollaboratorService(ghc);
        log = LoggerFactory.getLogger(CollaboratorMinerV3.class);
    }

    
    public List<User> getCollaborators(IRepositoryIdProvider repo) {
        try {
            return service.getCollaborators(repo);
        } catch (IOException e) {
            log.error("IOException in getting watchers for repository: {}", repo.generateId(), e);
            return null;
        }
    }   
}
