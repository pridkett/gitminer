/*
 * Copyright (c) 2011-2012 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        } catch (NullPointerException npe) {
            log.error("NullPointerException fetching repository {}/{}", new Object[]{username, reponame, npe});
        }
        return repository;
    }

    public List<Repository> getRepositories(final String login) {
        List<Repository> repos = null;
        try {
            repos = service.getRepositories(login);
        } catch (IOException e) {
            log.error("IOException in getRepositories: {}", login, e);
        } catch (NullPointerException npe) {
            log.error("NullPointerException in getRepositories: {}", login, npe);
        }
        return repos;
    }

    public List<Contributor> getContributors(final IRepositoryIdProvider repo) {
        List<Contributor> contributors = null;
        try {
            contributors = service.getContributors(repo, false);
        } catch (IOException e) {
            log.error("Error fetching contributors for repository: {}", repo.generateId(), e);
        } catch (NullPointerException e) {
            log.error("NullPointerException fecthing contributors for repository: {}", repo.generateId(), e);
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
