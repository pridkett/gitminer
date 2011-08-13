/*
 * Copyright 2011 IBM Corporation
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
package net.wagstrom.research.github;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Repository;
import com.github.api.v2.schema.User;
import com.github.api.v2.services.RepositoryService;

public class RepositoryMiner {
	private RepositoryService service = null;
	private Logger log;
	
	public RepositoryMiner(RepositoryService service) {
		this.service = service;
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	public Repository getRepositoryInformation(String username, String reponame) {
		log.trace("Fetching repository: {}/{}", username, reponame);
		Repository repo = service.getRepository(username, reponame);
		log.debug("Fetched repository: {}/{}", username, reponame);
		return repo;
	}
	
	public List<String> getRepositoryCollaborators(String username, String reponame) {
		log.trace("Fetching collaborators: {}/{}", username, reponame);
		List<String> collabs = service.getCollaborators(username, reponame);
		log.debug("Fetched collaborators: {}/{} number: {}", new Object[] {username, reponame, collabs==null?"null":collabs.size()});
		return collabs;
	}
	
	public List<User> getRepositoryContributors(String username, String reponame) {
		log.trace("Fetching contributors: {}/{}", username, reponame);
		List<User> contributors = service.getContributors(username, reponame);
		log.debug("Fetched contributors: {}/{} number: {}", new Object[] {username, reponame, contributors==null?"null":contributors.size()});
		return contributors;
	}
	
	public List<Repository> getUserRepositories(String username) {
		log.trace("Fetching repositories for user: {}", username);
		List<Repository> repos = service.getRepositories(username);
		log.debug("Fetched repositories for user: {} number: {}", username, repos==null?"null":repos.size());
		return repos;
	}
	
	public List<String> getWatchers(String username, String reponame) {
		log.trace("Fetching watchers for repository: {}/{}", username, reponame);
		List<String> watchers = service.getWatchers(username,  reponame);
		log.debug("Fetched watchers for repository: {}/{} number: {}", new Object[] {username, reponame, watchers==null?"null":watchers.size()});
		return watchers;
	}
	
	public List<Repository> getForks(String username, String reponame) {
		log.trace("Fetching forks for repository: {}/{}", username, reponame);
		List<Repository> forks = service.getForks(username, reponame);
		log.debug("Fetched forks for repository: {}/{} number: {}", new Object[] {username, reponame, forks==null?"null":forks.size()});
		return forks;
	}
	
}
