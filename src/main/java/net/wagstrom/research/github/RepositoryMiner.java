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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.api.v2.schema.Repository;
import com.github.api.v2.services.RepositoryService;

public class RepositoryMiner {
	private RepositoryService service = null;
	private Logger log;
	
	public RepositoryMiner(RepositoryService service) {
		this.service = service;
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	public Repository getRepositoryInformation(String username, String reponame) {
		Repository repo = service.getRepository(username, reponame);
		log.debug("Fetched repository: " + username + "/" + reponame);
		return repo;
	}

}
