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
    private final RepositoryService service;

    private static final Logger log = LoggerFactory.getLogger(RepositoryMinerV3.class); // NOPMD

    public RepositoryMinerV3(final IGitHubClient ghc) {
        super();
        service = new RepositoryService(ghc);
    }

    public Repository getRepository(final IRepositoryIdProvider repo) {
        Repository repository = null;
        try {
            repository = service.getRepository(repo);
        } catch (IOException e) {
            log.error("IO exception fetching Repository: {}", repo.generateId(), e);
        }
        return repository;
    }

    public Repository getRepository(final String username, final String reponame) {
        Repository repository = null;
        try {
            repository = service.getRepository(username, reponame);
        } catch (IOException e) {
            log.error("IO exception fetchin Repository {}/{}", new Object[]{username, reponame, e});
        }
        return repository;
    }

    public List<Repository> getRepositories(final String login) {
        List<Repository> repos = null;
        try {
            repos = service.getRepositories(login);
        } catch (IOException e) {
            log.error("IOException in getRepositories: {}", login, e);
        }
        return repos;
    }

    public List<Contributor> getContributors(final IRepositoryIdProvider repo) {
        List<Contributor> contributors = null;
        try {
            contributors = service.getContributors(repo, false);
        } catch (IOException e) {
            log.error("Error fetching contributors for repository: {}", repo.generateId(), e);
        }
        return contributors;
    }
    
    public Map<String, Long> getLanguages(final IRepositoryIdProvider repo) {
        Map<String, Long> languages = null;
        try {
            languages = service.getLanguages(repo);
        } catch (IOException e) {
            log.error("Error fetching languages for repository: {}", repo.generateId(), e);
        }
        return languages;
    }
    
    public List<Repository> getForks(final IRepositoryIdProvider repo) {
        List<Repository> forks = null;
        try {
            forks = service.getForks(repo);
        } catch (IOException e) {
            log.error("Error fetching forks for repository: {}", repo.generateId(), e);
        }
        return forks;
    }
}
