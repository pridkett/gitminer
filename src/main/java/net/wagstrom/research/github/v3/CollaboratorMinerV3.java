package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollaboratorMinerV3 extends AbstractMiner {
    private final CollaboratorService service;
    
    private static final Logger log = LoggerFactory.getLogger(CollaboratorMinerV3.class); // NOPMD

    public CollaboratorMinerV3(final IGitHubClient ghc) {
        super();
        service = new CollaboratorService(ghc);
    }

    
    public List<User> getCollaborators(final IRepositoryIdProvider repo) {
        List<User> collaborators = null;
        try {
            collaborators = service.getCollaborators(repo);
        } catch (IOException e) {
            log.error("IOException in getting watchers for repository: {}", repo.generateId(), e);
        }
        return collaborators;
    }
}
