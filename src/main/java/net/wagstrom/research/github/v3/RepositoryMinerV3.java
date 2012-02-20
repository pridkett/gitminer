package net.wagstrom.research.github.v3;

import java.io.IOException;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.IGitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryMinerV3 extends V3Miner {
    private RepositoryService service;

    private Logger log;

    public RepositoryMinerV3(IGitHubClient ghc) {
        service = new RepositoryService(ghc);
        log = LoggerFactory.getLogger(RepositoryMinerV3.class);
    }

    public Repository getRepository(String username, String reponame) {
        try {
            return service.getRepository(username, reponame);
        } catch (IOException e) {
            log.error("IO exception fetchin Repository {}/{}", new Object[]{username, reponame, e});
            return null;
        }
    }

    // TODO: implement these methods
    //	public List<String> getRepositoryCollaborators(String username, String reponame) {
    //		log.trace("Fetching collaborators: {}/{}", username, reponame);
    //		List<String> collabs = service.getCollaborators(username, reponame);
    //		log.debug("Fetched collaborators: {}/{} number: {}", new Object[] {username, reponame, collabs==null?"null":collabs.size()});
    //		return collabs;
    //	}
    //	
    //	public List<User> getRepositoryContributors(String username, String reponame) {
    //		log.trace("Fetching contributors: {}/{}", username, reponame);
    //		List<User> contributors = service.getContributors(username, reponame);
    //		log.debug("Fetched contributors: {}/{} number: {}", new Object[] {username, reponame, contributors==null?"null":contributors.size()});
    //		return contributors;
    //	}
    //	
    //	public List<Repository> getUserRepositories(String username) {
    //		log.trace("Fetching repositories for user: {}", username);
    //		List<Repository> repos = service.getRepositories(username);
    //		log.debug("Fetched repositories for user: {} number: {}", username, repos==null?"null":repos.size());
    //		return repos;
    //	}
    //	
    //	public List<String> getWatchers(String username, String reponame) {
    //		log.trace("Fetching watchers for repository: {}/{}", username, reponame);
    //		List<String> watchers = service.getWatchers(username,  reponame);
    //		log.debug("Fetched watchers for repository: {}/{} number: {}", new Object[] {username, reponame, watchers==null?"null":watchers.size()});
    //		return watchers;
    //	}
    //	
    //	public List<Repository> getForks(String username, String reponame) {
    //		log.trace("Fetching forks for repository: {}/{}", username, reponame);
    //		List<Repository> forks = service.getForks(username, reponame);
    //		log.debug("Fetched forks for repository: {}/{} number: {}", new Object[] {username, reponame, forks==null?"null":forks.size()});
    //		return forks;
    //	}
}
