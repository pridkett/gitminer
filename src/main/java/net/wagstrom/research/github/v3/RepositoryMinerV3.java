package net.wagstrom.research.github.v3;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryMinerV3 extends AbstractMiner {
    private RepositoryService service;

    private static final Logger log = LoggerFactory.getLogger(RepositoryMinerV3.class); // NOPMD

    public RepositoryMinerV3(IGitHubClient ghc) {
        service = new RepositoryService(ghc);
    }

    public Repository getRepository(IRepositoryIdProvider repo) {
        try {
            return service.getRepository(repo);
        } catch (IOException e) {
            log.error("IO exception fetching Repository: {}", repo.generateId(), e);
            return null;
        }
    }

    public Repository getRepository(String username, String reponame) {
        try {
            return service.getRepository(username, reponame);
        } catch (IOException e) {
            log.error("IO exception fetchin Repository {}/{}", new Object[]{username, reponame, e});
            return null;
        }
    }

    public List<Repository> getRepositories(String login) {
        try {
            return service.getRepositories(login);
        } catch (IOException e) {
            log.error("IOException in getRepositories: {}", login, e);
            return null;
        }
    }

    public List<Contributor> getContributors(IRepositoryIdProvider repo) {
        try {
            return service.getContributors(repo, false);
        } catch (IOException e) {
            log.error("Error fetching contributors for repository: {}", repo.generateId(), e);
            return null;
        }
    }
    
    public Map<String, Long> getLanguages(IRepositoryIdProvider repo) {
        try {
            return service.getLanguages(repo);
        } catch (IOException e) {
            log.error("Error fetching languages for repository: {}", repo.generateId(), e);
            return null;
        }
    }
    
    public List<Repository> getForks(IRepositoryIdProvider repo) {
        try {
            return service.getForks(repo);
        } catch (IOException e) {
            log.error("Error fetching forks for repository: {}", repo.generateId(), e);
            return null;
        }
    }
}
