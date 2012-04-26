package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.WatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WatcherMinerV3 extends AbstractMiner {
    private WatcherService service;
    
    private static final Logger log = LoggerFactory.getLogger(WatcherMinerV3.class); // NOPMD

    public WatcherMinerV3(IGitHubClient ghc) {
        service = new WatcherService(ghc);
    }

    
    public List<User> getWatchers(IRepositoryIdProvider repo) {
        try {
            return service.getWatchers(repo);
        } catch (IOException e) {
            log.error("IOException in getting watchers for repository: {}", repo.generateId(), e);
            return null;
        }
    }
    
    public List<Repository> getWatched(String login) {
        try {
            return service.getWatched(login);
        } catch (IOException e) {
            log.error("IOException in getWatched: {}", login, e);
            return null;
        }
    }
}
